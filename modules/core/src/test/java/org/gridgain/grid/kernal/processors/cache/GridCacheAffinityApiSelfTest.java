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

package org.gridgain.grid.kernal.processors.cache;

import org.apache.ignite.cluster.*;
import org.gridgain.grid.cache.*;
import org.gridgain.grid.cache.affinity.*;
import org.gridgain.grid.kernal.*;
import org.gridgain.grid.kernal.processors.affinity.*;
import org.gridgain.grid.util.typedef.*;

import java.util.*;

import static org.gridgain.grid.cache.GridCacheMode.*;
import static org.gridgain.grid.cache.GridCacheWriteSynchronizationMode.*;

/**
 * Affinity API tests.
 */
public class GridCacheAffinityApiSelfTest extends GridCacheAbstractSelfTest {
    /** */
    private static final int GRID_CNT = 4;

    /** */
    private static final Random RND = new Random();

    /** {@inheritDoc} */
    @Override protected GridCacheConfiguration cacheConfiguration(String gridName) throws Exception {
        GridCacheConfiguration cfg = super.cacheConfiguration(gridName);

        cfg.setCacheMode(PARTITIONED);
        cfg.setWriteSynchronizationMode(FULL_SYNC);

        cfg.setBackups(1);

        return cfg;
    }

    /** {@inheritDoc} */
    @Override protected int gridCount() {
        return GRID_CNT;
    }

    /**
     * @return Affinity.
     */
    private GridCacheAffinityFunction affinity() {
        return ((GridKernal)grid(0)).internalCache().configuration().getAffinity();
    }

    /**
     * @return Affinity mapper.
     */
    private GridCacheAffinityKeyMapper affinityMapper() {
        return ((GridKernal)grid(0)).internalCache().configuration().getAffinityMapper();
    }

    /**
     * JUnit.
     *
     * @throws Exception If failed.
     */
    public void testPartitions() throws Exception {
        assertEquals(affinity().partitions(), cache().affinity().partitions());
    }

    /**
     * JUnit.
     *
     * @throws Exception If failed.
     */
    public void testPartition() throws Exception {
        String key = "key";

        assertEquals(affinity().partition(key), cache().affinity().partition(key));
    }

    /**
     * JUnit.
     *
     * @throws Exception If failed.
     */
    public void testPrimaryPartitionsOneNode() throws Exception {
        GridCacheAffinityFunctionContext ctx =
            new GridCacheAffinityFunctionContextImpl(new ArrayList<>(grid(0).nodes()), null, null, 1, 1);

        List<List<ClusterNode>> assignment = affinity().assignPartitions(ctx);

        for (ClusterNode node : grid(0).nodes()) {
            int[] parts = cache().affinity().primaryPartitions(node);

            assert !F.isEmpty(parts);

            for (int p : parts) {
                Collection<ClusterNode> owners = nodes(assignment, p);

                assert !F.isEmpty(owners);

                ClusterNode primary = F.first(owners);

                assert F.eqNodes(node, primary);
            }
        }
    }

    /**
     * JUnit.
     *
     * @throws Exception If failed.
     */
    public void testPrimaryPartitions() throws Exception {
        // Pick 2 nodes and create a projection over them.
        ClusterNode n0 = grid(0).localNode();

        int[] parts = cache().affinity().primaryPartitions(n0);

        info("Primary partitions count: " + parts.length);

        assert parts.length > 1 : "Invalid partitions: " + Arrays.toString(parts);

        for (int part : parts)
            assert part >= 0;

        assert !F.isEmpty(parts);

        GridCacheAffinityFunctionContext ctx =
            new GridCacheAffinityFunctionContextImpl(new ArrayList<>(grid(0).nodes()), null, null, 1, 1);

        List<List<ClusterNode>> assignment = affinity().assignPartitions(ctx);

        for (int p : parts) {
            Collection<ClusterNode> owners = nodes(assignment, p);

            assert !F.isEmpty(owners);

            ClusterNode primary = F.first(owners);

            assert F.eqNodes(n0, primary);
        }
    }

    /**
     * JUnit.
     *
     * @throws Exception If failed.
     */
    public void testBackupPartitions() throws Exception {
        // Pick 2 nodes and create a projection over them.
        ClusterNode n0 = grid(0).localNode();

        // Get backup partitions without explicitly specified levels.
        int[] parts = cache().affinity().backupPartitions(n0);

        assert !F.isEmpty(parts);

        GridCacheAffinityFunctionContext ctx =
            new GridCacheAffinityFunctionContextImpl(new ArrayList<>(grid(0).nodes()), null, null, 1, 1);

        List<List<ClusterNode>> assignment = affinity().assignPartitions(ctx);

        for (int p : parts) {
            Collection<ClusterNode> owners = new ArrayList<>(nodes(assignment, p));

            assert !F.isEmpty(owners);

            // Remove primary.
            Iterator<ClusterNode> iter = owners.iterator();

            iter.next();
            iter.remove();

            assert owners.contains(n0);
        }
    }

    /**
     * JUnit.
     *
     * @throws Exception If failed.
     */
    public void testAllPartitions() throws Exception {
        // Pick 2 nodes and create a projection over them.
        ClusterNode n0 = grid(0).localNode();

        int[] parts = cache().affinity().allPartitions(n0);

        assert !F.isEmpty(parts);

        GridCacheAffinityFunctionContext ctx =
            new GridCacheAffinityFunctionContextImpl(new ArrayList<>(grid(0).nodes()), null, null, 1, 1);

        List<List<ClusterNode>> assignment = affinity().assignPartitions(ctx);

        for (int p : parts) {
            Collection<ClusterNode> owners = nodes(assignment, p);

            assert !F.isEmpty(owners);

            assert owners.contains(n0);
        }
    }

    /**
     * JUnit.
     *
     * @throws Exception If failed.
     */
    public void testMapPartitionToNode() throws Exception {
        int part = RND.nextInt(affinity().partitions());

        GridCacheAffinityFunctionContext ctx =
            new GridCacheAffinityFunctionContextImpl(new ArrayList<>(grid(0).nodes()), null, null, 1, 1);

        GridCacheAffinityFunction aff = affinity();

        List<List<ClusterNode>> assignment = aff.assignPartitions(ctx);

        assertEquals(F.first(nodes(assignment, aff, part)), cache().affinity().mapPartitionToNode(part));
    }

    /**
     * JUnit.
     *
     * @throws Exception If failed.
     */
    public void testMapPartitionsToNode() throws Exception {
        Map<Integer, ClusterNode> map = cache().affinity().mapPartitionsToNodes(F.asList(0, 1, 5, 19, 12));

        GridCacheAffinityFunctionContext ctx =
            new GridCacheAffinityFunctionContextImpl(new ArrayList<>(grid(0).nodes()), null, null, 1, 1);

        GridCacheAffinityFunction aff = affinity();

        List<List<ClusterNode>> assignment = aff.assignPartitions(ctx);

        for (Map.Entry<Integer, ClusterNode> e : map.entrySet())
            assert F.eqNodes(F.first(nodes(assignment, aff, e.getKey())), e.getValue());
    }

    /**
     * JUnit.
     *
     * @throws Exception If failed.
     */
    public void testMapPartitionsToNodeArray() throws Exception {
        Map<Integer, ClusterNode> map = cache().affinity().mapPartitionsToNodes(F.asList(0, 1, 5, 19, 12));

        GridCacheAffinityFunctionContext ctx =
            new GridCacheAffinityFunctionContextImpl(new ArrayList<>(grid(0).nodes()), null, null, 1, 1);

        GridCacheAffinityFunction aff = affinity();

        List<List<ClusterNode>> assignment = aff.assignPartitions(ctx);

        for (Map.Entry<Integer, ClusterNode> e : map.entrySet())
            assert F.eqNodes(F.first(nodes(assignment, aff, e.getKey())), e.getValue());
    }

    /**
     * JUnit.
     *
     * @throws Exception If failed.
     */
    public void testMapPartitionsToNodeCollection() throws Exception {
        Collection<Integer> parts = new LinkedList<>();

        for (int p = 0; p < affinity().partitions(); p++)
            parts.add(p);

        Map<Integer, ClusterNode> map = cache().affinity().mapPartitionsToNodes(parts);

        GridCacheAffinityFunctionContext ctx =
            new GridCacheAffinityFunctionContextImpl(new ArrayList<>(grid(0).nodes()), null, null, 1, 1);

        GridCacheAffinityFunction aff = affinity();

        List<List<ClusterNode>> assignment = aff.assignPartitions(ctx);

        for (Map.Entry<Integer, ClusterNode> e : map.entrySet())
            assert F.eqNodes(F.first(nodes(assignment, aff, e.getKey())), e.getValue());
    }

    /**
     * Gets affinity nodes for partition.
     *
     * @param assignments Assignments.
     * @param part Partition to get affinity nodes for.
     * @return Affinity nodes.
     */
    private List<ClusterNode> nodes(List<List<ClusterNode>> assignments, int part) {
        return assignments.get(part);
    }

    /**
     * Gets affinity nodes for partition.
     *
     * @param key Affinity key.
     * @return Affinity nodes.
     */
    private Iterable<ClusterNode> nodes(List<List<ClusterNode>> assignment, GridCacheAffinityFunction aff, Object key) {
        return assignment.get(aff.partition(key));
    }

    /**
     * JUnit.
     *
     * @throws Exception If failed.
     */
    public void testEntryPartition() throws Exception {
        int keyCnt = 100;

        for (int kv = 0; kv < keyCnt; kv++)
            cache().put(String.valueOf(kv), kv);

        for (int kv = 0; kv < keyCnt; kv++) {
            String key = String.valueOf(kv);

            GridCacheEntry<String, Integer> entry = cache().entry(key);

            assert entry != null;

            assertEquals(affinity().partition(key), entry.partition());
        }
    }

    /**
     * @throws Exception If failed.
     */
    public void testPartitionWithAffinityMapper() throws Exception {
        GridCacheAffinityKey<Integer> key = new GridCacheAffinityKey<>(1, 2);

        int expPart = affinity().partition(affinityMapper().affinityKey(key));

        for (int i = 0; i < gridCount(); i++) {
            assertEquals(expPart, grid(i).cache(null).affinity().partition(key));
            assertEquals(expPart, grid(i).cache(null).entry(key).partition());
        }

        assertTrue(grid(0).cache(null).putx(key, 1));

        for (int i = 0; i < gridCount(); i++) {
            assertEquals(expPart, grid(i).cache(null).affinity().partition(key));
            assertEquals(expPart, grid(i).cache(null).entry(key).partition());
        }
    }
}
