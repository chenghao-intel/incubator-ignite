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

package org.gridgain.grid.kernal.processors.cache.distributed.dht.atomic;

import org.apache.ignite.*;
import org.apache.ignite.cluster.*;
import org.apache.ignite.configuration.*;
import org.apache.ignite.transactions.*;
import org.gridgain.grid.cache.*;
import org.gridgain.grid.cache.affinity.*;
import org.gridgain.grid.kernal.*;
import org.gridgain.grid.kernal.processors.cache.transactions.*;
import org.gridgain.testframework.junits.common.*;

import java.util.*;

import static org.gridgain.grid.cache.GridCacheDistributionMode.*;
import static org.apache.ignite.transactions.IgniteTxConcurrency.*;
import static org.apache.ignite.transactions.IgniteTxIsolation.*;
import static org.gridgain.grid.cache.GridCacheWriteSynchronizationMode.*;

/**
 * Simple test for preloading in ATOMIC cache.
 */
public class GridCacheAtomicPreloadSelfTest extends GridCommonAbstractTest {
    /** */
    private boolean nearEnabled;

    /** {@inheritDoc} */
    @Override protected IgniteConfiguration getConfiguration(String gridName) throws Exception {
        IgniteConfiguration cfg = super.getConfiguration(gridName);

        GridCacheConfiguration cacheCfg = new GridCacheConfiguration();

        cacheCfg.setCacheMode(GridCacheMode.PARTITIONED);
        cacheCfg.setAtomicityMode(GridCacheAtomicityMode.TRANSACTIONAL);
        cacheCfg.setWriteSynchronizationMode(FULL_SYNC);
        cacheCfg.setDistributionMode(nearEnabled ? NEAR_PARTITIONED : PARTITIONED_ONLY);
        cacheCfg.setBackups(1);

        cfg.setCacheConfiguration(cacheCfg);

        return cfg;
    }

    /**
     * @throws Exception If failed.
     */
    public void testPessimisticSimpleTxsNear() throws Exception {
        checkSimpleTxs(true, PESSIMISTIC);
    }

    /**
     * @throws Exception If failed.
     */
    public void testPessimisticSimpleTxsColocated() throws Exception {
        checkSimpleTxs(false, PESSIMISTIC);
    }

    /**
     * @throws Exception If failed.
     */
    public void testOptimisticSimpleTxsColocated() throws Exception {
        checkSimpleTxs(false, OPTIMISTIC);
    }

    /**
     * @throws Exception If failed.
     */
    public void testOptimisticSimpleTxsNear() throws Exception {
        checkSimpleTxs(false, OPTIMISTIC);
    }

    /**
     * @throws Exception If failed.
     */
    private void checkSimpleTxs(boolean nearEnabled, IgniteTxConcurrency concurrency) throws Exception {
        try {
            this.nearEnabled = nearEnabled;

            startGrids(3);

            awaitPartitionMapExchange();

            GridCache<Object, Object> cache = grid(0).cache(null);

            List<Integer> keys = generateKeys(grid(0).localNode(), cache);

            IgniteTransactions txs = grid(0).transactions();

            assert txs != null;

            for (int i = 0; i < keys.size(); i++) {
                Integer key = keys.get(i);

                info(">>>>>>>>>>>>>>>");
                info("Checking transaction for key [idx=" + i + ", key=" + key + ']');
                info(">>>>>>>>>>>>>>>");

                try (IgniteTx tx = txs.txStart(concurrency, REPEATABLE_READ)) {
                    try {
                        // Lock if pessimistic, read if optimistic.
                        cache.get(key);

                        cache.put(key, key + 1);

                        tx.commit();
                    }
                    catch (Exception e) {
                        // Print exception in case if
                        e.printStackTrace();

                        throw e;
                    }
                }

//                Thread.sleep(500);

                info(">>>>>>>>>>>>>>>");
                info("Finished checking transaction for key [idx=" + i + ", key=" + key + ']');
                info(">>>>>>>>>>>>>>>");

                checkTransactions();
                checkValues(key, key + 1);
            }
        }
        finally {
            stopAllGrids();
        }
    }

    /**
     *
     */
    private void checkTransactions() {
        for (int i = 0; i < 3; i++) {
            IgniteTxManager<Object, Object> tm = ((GridKernal)grid(i)).context().cache().context().tm();

            assertEquals("Uncommitted transactions found on node [idx=" + i + ", mapSize=" + tm.idMapSize() + ']',
                0, tm.idMapSize());
        }
    }

    /**
     * @param key Key to check.
     * @param val Expected value.
     */
    private void checkValues(int key, int val) {
        for (int i = 0; i < 3; i++) {
            GridEx grid = grid(i);

            ClusterNode node = grid.localNode();

            GridCache<Object, Object> cache = grid.cache(null);

            boolean primary = cache.affinity().isPrimary(node, key);
            boolean backup = cache.affinity().isBackup(node, key);

            if (primary || backup)
                assertEquals("Invalid cache value [nodeId=" + node.id() + ", primary=" + primary +
                    ", backup=" + backup + ", key=" + key + ']', val, cache.peek(key));
        }
    }

    /**
     * Generates a set of keys: near, primary, backup.
     *
     * @param node Node for which keys are generated.
     * @param cache Cache to get affinity for.
     * @return Collection of keys.
     */
    private List<Integer> generateKeys(ClusterNode node, GridCache<Object, Object> cache) {
        List<Integer> keys = new ArrayList<>(3);

        GridCacheAffinity<Object> aff = cache.affinity();

        int base = 0;

//        // Near key.
        while (aff.isPrimary(node, base) || aff.isBackup(node, base))
            base++;

        keys.add(base);

//        Primary key.
        while (!aff.isPrimary(node, base))
            base++;

        keys.add(base);

        // Backup key.
        while (!aff.isBackup(node, base))
            base++;

        keys.add(base);

        return keys;
    }
}
