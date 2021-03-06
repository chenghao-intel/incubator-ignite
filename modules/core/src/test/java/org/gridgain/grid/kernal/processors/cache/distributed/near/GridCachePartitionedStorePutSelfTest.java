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

package org.gridgain.grid.kernal.processors.cache.distributed.near;

import org.apache.ignite.*;
import org.apache.ignite.configuration.*;
import org.apache.ignite.spi.discovery.*;
import org.apache.ignite.spi.discovery.tcp.*;
import org.apache.ignite.spi.discovery.tcp.ipfinder.*;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.*;
import org.apache.ignite.transactions.*;
import org.gridgain.grid.cache.*;
import org.gridgain.grid.cache.store.*;
import org.gridgain.grid.kernal.processors.cache.distributed.*;
import org.gridgain.grid.util.typedef.*;
import org.gridgain.testframework.junits.common.*;
import org.jetbrains.annotations.*;

import java.util.concurrent.atomic.*;

import static org.gridgain.grid.cache.GridCacheMode.*;
import static org.gridgain.grid.cache.GridCacheWriteSynchronizationMode.*;
import static org.gridgain.grid.kernal.processors.cache.distributed.GridCacheModuloAffinityFunction.*;

/**
 * Test that store is called correctly on puts.
 */
public class GridCachePartitionedStorePutSelfTest extends GridCommonAbstractTest {
    /** */
    private static final TcpDiscoveryIpFinder IP_FINDER = new TcpDiscoveryVmIpFinder(true);

    /** */
    private static final AtomicInteger CNT = new AtomicInteger(0);

    /** */
    private GridCache<Integer, Integer> cache1;

    /** */
    private GridCache<Integer, Integer> cache2;

    /** */
    private GridCache<Integer, Integer> cache3;

    /** {@inheritDoc} */
    @Override protected IgniteConfiguration getConfiguration(String gridName) throws Exception {
        IgniteConfiguration cfg = super.getConfiguration(gridName);

        cfg.setDiscoverySpi(discoverySpi());
        cfg.setCacheConfiguration(cacheConfiguration());
        cfg.setUserAttributes(F.asMap(IDX_ATTR, CNT.getAndIncrement()));

        return cfg;
    }

    /**
     * @return Discovery SPI.
     */
    private DiscoverySpi discoverySpi() {
        TcpDiscoverySpi spi = new TcpDiscoverySpi();

        spi.setIpFinder(IP_FINDER);

        return spi;
    }

    /**
     * @return Cache configuration.
     */
    private GridCacheConfiguration cacheConfiguration() {
        GridCacheConfiguration cfg = defaultCacheConfiguration();

        cfg.setCacheMode(PARTITIONED);
        cfg.setStore(new TestStore());
        cfg.setAffinity(new GridCacheModuloAffinityFunction(3, 1));
        cfg.setWriteSynchronizationMode(FULL_SYNC);

        return cfg;
    }

    /** {@inheritDoc} */
    @Override protected void beforeTest() throws Exception {
        cache1 = startGrid(1).cache(null);
        cache2 = startGrid(2).cache(null);
        cache3 = startGrid(3).cache(null);
    }

    /** {@inheritDoc} */
    @Override protected void afterTest() throws Exception {
        stopAllGrids();
    }

    /**
     * @throws Exception If failed.
     */
    public void testPutx() throws Throwable {
        info("Putting to the first node.");

        cache1.putx(0, 1);

        info("Putting to the second node.");

        cache2.putx(0, 2);

        info("Putting to the third node.");

        cache3.putx(0, 3);
    }

    /**
     * Test store.
     */
    private static class TestStore extends GridCacheStoreAdapter<Object, Object> {
        /** {@inheritDoc} */
        @Override public Object load(@Nullable IgniteTx tx, Object key)
            throws IgniteCheckedException {
            assert false;

            return null;
        }

        /** {@inheritDoc} */
        @Override public void put(IgniteTx tx, Object key, @Nullable Object val)
            throws IgniteCheckedException {
            // No-op
        }

        /** {@inheritDoc} */
        @Override public void remove(IgniteTx tx, Object key) throws IgniteCheckedException {
            // No-op
        }
    }
}
