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
import org.apache.ignite.spi.discovery.tcp.*;
import org.apache.ignite.spi.discovery.tcp.ipfinder.*;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.*;
import org.gridgain.grid.cache.*;
import org.gridgain.grid.cache.query.*;
import org.gridgain.testframework.*;
import org.gridgain.testframework.junits.common.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import static org.gridgain.grid.cache.GridCacheAtomicityMode.*;
import static org.gridgain.grid.cache.GridCacheDistributionMode.*;
import static org.gridgain.grid.cache.GridCacheMode.*;

/**
 *
 */
public class GridCacheSqlQueryMultiThreadedSelfTest extends GridCommonAbstractTest {
    /** */
    private static final TcpDiscoveryIpFinder ipFinder = new TcpDiscoveryVmIpFinder(true);

    /** {@inheritDoc} */
    @Override protected IgniteConfiguration getConfiguration(String gridName) throws Exception {
        IgniteConfiguration c = super.getConfiguration(gridName);

        TcpDiscoverySpi disco = new TcpDiscoverySpi();

        disco.setIpFinder(ipFinder);

        c.setDiscoverySpi(disco);

        GridCacheConfiguration ccfg = new GridCacheConfiguration();

        ccfg.setCacheMode(PARTITIONED);
        ccfg.setDistributionMode(PARTITIONED_ONLY);
        ccfg.setQueryIndexEnabled(true);
        ccfg.setBackups(1);
        ccfg.setAtomicityMode(TRANSACTIONAL);

        c.setCacheConfiguration(ccfg);

        return c;
    }

    /** {@inheritDoc} */
    @Override protected void beforeTestsStarted() throws Exception {
        super.beforeTestsStarted();

        startGrids(2);

        awaitPartitionMapExchange();
    }

    /** {@inheritDoc} */
    @Override protected void afterTestsStopped() throws Exception {
        super.afterTestsStopped();

        stopAllGrids();
    }

    /**
     * @throws Exception If failed.
     */
    public void testQuery() throws Exception {
        final GridCache<Integer, Person> cache = grid(0).cache(null);

        for (int i = 0; i < 2000; i++)
            cache.put(i, new Person(i));

        GridTestUtils.runMultiThreaded(new Callable<Void>() {
            @Override public Void call() throws Exception {
                for (int i = 0; i < 100; i++) {
                    GridCacheQuery<Map.Entry<Integer, Person>> qry =
                        cache.queries().createSqlQuery("Person", "age >= 0");

                    qry.includeBackups(false);
                    qry.enableDedup(true);
                    qry.keepAll(true);
                    qry.pageSize(50);

                    GridCacheQueryFuture<Map.Entry<Integer, Person>> fut = qry.execute();

                    int cnt = 0;

                    while (fut.next() != null)
                        cnt++;

                    assertEquals(2000, cnt);
                }

                return null;
            }
        }, 16, "test");
    }

    /**
     *
     */
    private static class Person implements Serializable {
        /** */
        @GridCacheQuerySqlField
        private int age;

        /**
         * @param age Age.
         */
        Person(int age) {
            this.age = age;
        }

        /**
         * @return Age/
         */
        public int age() {
            return age;
        }
    }
}
