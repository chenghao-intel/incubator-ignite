/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gridgain.grid.kernal.processors.affinity;

import org.apache.ignite.cluster.*;

import java.io.*;
import java.util.*;

/**
 * Cached affinity calculations.
 */
class GridAffinityAssignment implements Serializable {
    /** */
    private static final long serialVersionUID = 0L;

    /** Topology version. */
    private final long topVer;

    /** Collection of calculated affinity nodes. */
    private List<List<ClusterNode>> assignment;

    /** Map of primary node partitions. */
    private final Map<UUID, Set<Integer>> primary;

    /** Map of backup node partitions. */
    private final Map<UUID, Set<Integer>> backup;

    /**
     * Constructs cached affinity calculations item.
     *
     * @param topVer Topology version.
     */
    GridAffinityAssignment(long topVer) {
        this.topVer = topVer;
        primary = new HashMap<>();
        backup = new HashMap<>();
    }

    /**
     * @param topVer Topology version.
     * @param assignment Assignment.
     */
    GridAffinityAssignment(long topVer, List<List<ClusterNode>> assignment) {
        this.topVer = topVer;
        this.assignment = assignment;

        primary = new HashMap<>();
        backup = new HashMap<>();

        initPrimaryBackupMaps();
    }

    /**
     * @return Affinity assignment.
     */
    public List<List<ClusterNode>> assignment() {
        return assignment;
    }

    /**
     * @return Topology version.
     */
    public long topologyVersion() {
        return topVer;
    }

    /**
     * Get affinity nodes for partition.
     *
     * @param part Partition.
     * @return Affinity nodes.
     */
    public List<ClusterNode> get(int part) {
        assert part >= 0 && part < assignment.size() : "Affinity partition is out of range" +
            " [part=" + part + ", partitions=" + assignment.size() + ']';

        return assignment.get(part);
    }

    /**
     * Get primary partitions for specified node ID.
     *
     * @param nodeId Node ID to get primary partitions for.
     * @return Primary partitions for specified node ID.
     */
    public Set<Integer> primaryPartitions(UUID nodeId) {
        Set<Integer> set = primary.get(nodeId);

        return set == null ? Collections.<Integer>emptySet() : Collections.unmodifiableSet(set);
    }

    /**
     * Get backup partitions for specified node ID.
     *
     * @param nodeId Node ID to get backup partitions for.
     * @return Backup partitions for specified node ID.
     */
    public Set<Integer> backupPartitions(UUID nodeId) {
        Set<Integer> set = backup.get(nodeId);

        return set == null ? Collections.<Integer>emptySet() : Collections.unmodifiableSet(set);
    }

    /**
     * Initializes primary and backup maps.
     */
    private void initPrimaryBackupMaps() {
        // Temporary mirrors with modifiable partition's collections.
        Map<UUID, Set<Integer>> tmpPrm = new HashMap<>();
        Map<UUID, Set<Integer>> tmpBkp = new HashMap<>();

        for (int partsCnt = assignment.size(), p = 0; p < partsCnt; p++) {
            // Use the first node as primary, other - backups.
            Map<UUID, Set<Integer>> tmp = tmpPrm;
            Map<UUID, Set<Integer>> map = primary;

            for (ClusterNode node : assignment.get(p)) {
                UUID id = node.id();

                Set<Integer> set = tmp.get(id);

                if (set == null) {
                    tmp.put(id, set = new HashSet<>());
                    map.put(id, Collections.unmodifiableSet(set));
                }

                set.add(p);

                // Use the first node as primary, other - backups.
                tmp = tmpBkp;
                map = backup;
            }
        }
    }

    /** {@inheritDoc} */
    @Override public int hashCode() {
        return (int)(topVer ^ (topVer >>> 32));
    }

    /** {@inheritDoc} */
    @SuppressWarnings("SimplifiableIfStatement")
    @Override public boolean equals(Object o) {
        if (o == this)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        return topVer == ((GridAffinityAssignment)o).topVer;
    }
}
