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

import org.apache.ignite.configuration.*;
import org.gridgain.grid.cache.*;
import org.gridgain.grid.kernal.*;
import org.apache.ignite.spi.discovery.tcp.*;
import org.apache.ignite.spi.discovery.tcp.ipfinder.*;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.*;
import org.gridgain.grid.util.typedef.*;
import org.gridgain.testframework.junits.common.*;

import java.util.*;

import static org.gridgain.grid.cache.GridCacheAtomicWriteOrderMode.*;
import static org.gridgain.grid.cache.GridCacheAtomicityMode.*;
import static org.gridgain.grid.cache.GridCacheMode.*;
import static org.gridgain.grid.cache.GridCacheWriteSynchronizationMode.*;
import static org.gridgain.grid.kernal.processors.cache.GridCacheVersionManager.*;

/**
 * Tests that entry version is
 */
public class GridCacheEntryVersionSelfTest extends GridCommonAbstractTest {
    /** IP finder. */
    private static final TcpDiscoveryIpFinder IP_FINDER = new TcpDiscoveryVmIpFinder(true);

    /** Atomicity mode. */
    private GridCacheAtomicityMode atomicityMode;

    /** {@inheritDoc} */
    @Override protected IgniteConfiguration getConfiguration(String gridName) throws Exception {
        IgniteConfiguration cfg = super.getConfiguration(gridName);

        TcpDiscoverySpi discoSpi = new TcpDiscoverySpi();

        discoSpi.setIpFinder(IP_FINDER);

        GridCacheConfiguration ccfg = new GridCacheConfiguration();

        ccfg.setCacheMode(PARTITIONED);
        ccfg.setAtomicWriteOrderMode(PRIMARY);
        ccfg.setBackups(1);
        ccfg.setAtomicityMode(atomicityMode);
        ccfg.setWriteSynchronizationMode(FULL_SYNC);

        cfg.setCacheConfiguration(ccfg);

        cfg.setDiscoverySpi(discoSpi);

        return cfg;
    }

    /**
     * @throws Exception If failed.
     */
    public void testVersionAtomic() throws Exception {
        atomicityMode = ATOMIC;

        checkVersion();
    }

    /**
     * @throws Exception If failed.
     */
    public void testVersionTransactional() throws Exception {
        atomicityMode = TRANSACTIONAL;

        checkVersion();
    }

    /**
     * @throws Exception If failed.
     */
    private void checkVersion() throws Exception {
        startGridsMultiThreaded(3);

        try {
            Map<Integer,Integer> map = F.asMap(1, 1, 2, 2, 3, 3);

            for (Integer key : map.keySet()) {
                info("Affinity nodes [key=" + key + ", nodes=" +
                    F.viewReadOnly(grid(0).cache(null).affinity().mapKeyToPrimaryAndBackups(key), F.node2id()) + ']');
            }

            grid(0).cache(null).putAll(map);

            for (int g = 0; g < 3; g++) {
                GridKernal grid = (GridKernal)grid(g);

                for (Integer key : map.keySet()) {
                    GridCacheAdapter<Object, Object> cache = grid.internalCache();

                    GridCacheEntryEx<Object, Object> entry = cache.peekEx(key);

                    if (entry != null) {
                        GridCacheVersion ver = entry.version();

                        long order = cache.affinity().mapKeyToNode(key).order();

                        // Check topology version.
                        assertEquals(3, ver.topologyVersion() -
                            (grid.context().discovery().gridStartTime() - TOP_VER_BASE_TIME) / 1000);

                        // Check node order.
                        assertEquals("Failed for key: " + key, order, ver.nodeOrder());
                    }
                }
            }

            startGrid(3);

            grid(0).cache(null).putAll(map);

            for (int g = 0; g < 4; g++) {
                GridKernal grid = (GridKernal)grid(g);

                for (Integer key : map.keySet()) {
                    GridCacheAdapter<Object, Object> cache = grid.internalCache();

                    GridCacheEntryEx<Object, Object> entry = cache.peekEx(key);

                    if (entry != null) {
                        GridCacheVersion ver = entry.version();

                        long order = cache.affinity().mapKeyToNode(key).order();

                        // Check topology version.
                        assertEquals(4, ver.topologyVersion() -
                            (grid.context().discovery().gridStartTime() - TOP_VER_BASE_TIME) / 1000);

                        // Check node order.
                        assertEquals("Failed for key: " + key, order, ver.nodeOrder());
                    }
                }
            }
        }
        finally {
            stopAllGrids();
        }
    }
}
