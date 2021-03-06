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

package org.gridgain.grid.kernal.processors.cache.distributed;

import org.apache.ignite.cluster.*;
import org.apache.ignite.configuration.*;
import org.apache.ignite.lang.*;
import org.gridgain.grid.cache.*;
import org.gridgain.grid.cache.affinity.*;
import org.gridgain.grid.cache.affinity.consistenthash.*;
import org.apache.ignite.spi.discovery.tcp.*;
import org.apache.ignite.spi.discovery.tcp.ipfinder.*;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.*;
import org.gridgain.grid.util.typedef.*;
import org.gridgain.testframework.junits.common.*;

import java.util.*;

import static org.gridgain.grid.cache.GridCacheAtomicityMode.*;
import static org.gridgain.grid.cache.GridCacheMode.*;
import static org.gridgain.grid.cache.GridCacheDistributionMode.*;
import static org.gridgain.grid.cache.GridCachePreloadMode.*;

/**
 * Partitioned affinity test.
 */
@SuppressWarnings({"PointlessArithmeticExpression"})
public class GridCachePartitionedAffinityFilterSelfTest extends GridCommonAbstractTest {
    /** */
    private static final TcpDiscoveryIpFinder IP_FINDER = new TcpDiscoveryVmIpFinder(true);

    /** Backup count. */
    private static final int BACKUPS = 1;

    /** Split attribute name. */
    private static final String SPLIT_ATTRIBUTE_NAME = "split-attribute";

    /** Split attribute value. */
    private String splitAttrVal;

    /** Test backup filter. */
    private static final IgniteBiPredicate<ClusterNode, ClusterNode> backupFilter =
        new IgniteBiPredicate<ClusterNode, ClusterNode>() {
            @Override public boolean apply(ClusterNode primary, ClusterNode backup) {
                assert primary != null : "primary is null";
                assert backup != null : "backup is null";

                return !F.eq(primary.attribute(SPLIT_ATTRIBUTE_NAME), backup.attribute(SPLIT_ATTRIBUTE_NAME));
            }
        };

    /** {@inheritDoc} */
    @Override protected IgniteConfiguration getConfiguration(String gridName) throws Exception {
        GridCacheConsistentHashAffinityFunction aff = new GridCacheConsistentHashAffinityFunction();

        aff.setBackupFilter(backupFilter);

        GridCacheConfiguration cacheCfg = defaultCacheConfiguration();

        cacheCfg.setCacheMode(PARTITIONED);
        cacheCfg.setBackups(BACKUPS);
        cacheCfg.setAffinity(aff);
        cacheCfg.setWriteSynchronizationMode(GridCacheWriteSynchronizationMode.FULL_SYNC);
        cacheCfg.setPreloadMode(SYNC);
        cacheCfg.setAtomicityMode(TRANSACTIONAL);
        cacheCfg.setDistributionMode(NEAR_PARTITIONED);

        TcpDiscoverySpi spi = new TcpDiscoverySpi();

        spi.setIpFinder(IP_FINDER);

        IgniteConfiguration cfg = super.getConfiguration(gridName);

        cfg.setCacheConfiguration(cacheCfg);
        cfg.setDiscoverySpi(spi);

        cfg.setUserAttributes(F.asMap(SPLIT_ATTRIBUTE_NAME, splitAttrVal));

        return cfg;
    }

    /**
     * @throws Exception If failed.
     */
    public void testPartitionDistribution() throws Exception {
        try {
            for (int i = 0; i < 3; i++) {
                splitAttrVal = "A";

                startGrid(2 * i);

                splitAttrVal = "B";

                startGrid(2 * i + 1);

                awaitPartitionMapExchange();

                checkPartitions();
            }
        }
        finally {
            stopAllGrids();
        }
    }

    /**
     * @throws Exception If failed.
     */
    private void checkPartitions() throws Exception {
        int partCnt = GridCacheConsistentHashAffinityFunction.DFLT_PARTITION_COUNT;

        GridCacheAffinityFunction aff = cacheConfiguration(grid(0).configuration(), null).getAffinity();

        GridCache<Object, Object> cache = grid(0).cache(null);

        for (int i = 0; i < partCnt; i++) {
            assertEquals(i, aff.partition(i));

            Collection<ClusterNode> nodes = cache.affinity().mapKeyToPrimaryAndBackups(i);

            assertEquals(2, nodes.size());

            ClusterNode primary = F.first(nodes);
            ClusterNode backup = F.last(nodes);

            assertFalse(F.eq(primary.attribute(SPLIT_ATTRIBUTE_NAME), backup.attribute(SPLIT_ATTRIBUTE_NAME)));
        }
    }
}
