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

import org.apache.ignite.configuration.*;
import org.gridgain.grid.cache.*;
import org.gridgain.grid.cache.affinity.consistenthash.*;
import org.gridgain.grid.kernal.processors.cache.distributed.*;

import static org.gridgain.grid.cache.GridCacheMode.*;
import static org.gridgain.grid.cache.GridCacheDistributionMode.*;
import static org.apache.ignite.transactions.IgniteTxConcurrency.*;
import static org.gridgain.grid.cache.GridCacheWriteSynchronizationMode.*;

/**
 * Test node restart.
 */
public class GridCachePartitionedNodeRestartTest extends GridCacheAbstractNodeRestartSelfTest {
    /** {@inheritDoc} */
    @Override protected IgniteConfiguration getConfiguration(String gridName) throws Exception {
        IgniteConfiguration c = super.getConfiguration(gridName);

        c.getTransactionsConfiguration().setDefaultTxConcurrency(PESSIMISTIC);

        GridCacheConfiguration cc = defaultCacheConfiguration();

        cc.setName(CACHE_NAME);
        cc.setCacheMode(PARTITIONED);
        cc.setWriteSynchronizationMode(FULL_ASYNC);
        cc.setDistributionMode(PARTITIONED_ONLY);
        cc.setStartSize(20);
        cc.setPreloadMode(preloadMode);
        cc.setPreloadBatchSize(preloadBatchSize);
        cc.setAffinity(new GridCacheConsistentHashAffinityFunction(false, partitions));
        cc.setBackups(backups);

        c.setCacheConfiguration(cc);

        return c;
    }

    /** {@inheritDoc} */
    @Override public void testRestart() throws Exception {
        super.testRestart();
    }

    /** {@inheritDoc} */
    @Override public void testRestartWithPutTwoNodesNoBackups() throws Throwable {
        super.testRestartWithPutTwoNodesNoBackups();
    }

    /** {@inheritDoc} */
    @Override public void testRestartWithPutTwoNodesOneBackup() throws Throwable {
        super.testRestartWithPutTwoNodesOneBackup();
    }

    /** {@inheritDoc} */
    @Override public void testRestartWithPutFourNodesNoBackups() throws Throwable {
        super.testRestartWithPutFourNodesNoBackups();
    }

    /** {@inheritDoc} */
    @Override public void testRestartWithPutFourNodesOneBackups() throws Throwable {
        super.testRestartWithPutFourNodesOneBackups();
    }

    /** {@inheritDoc} */
    @Override public void testRestartWithPutSixNodesTwoBackups() throws Throwable {
        super.testRestartWithPutSixNodesTwoBackups();
    }

    /** {@inheritDoc} */
    @Override public void testRestartWithPutEightNodesTwoBackups() throws Throwable {
        super.testRestartWithPutEightNodesTwoBackups();
    }

    /** {@inheritDoc} */
    @Override public void testRestartWithPutTenNodesTwoBackups() throws Throwable {
        super.testRestartWithPutTenNodesTwoBackups();
    }

    /** {@inheritDoc} */
    @Override public void testRestartWithTxEightNodesTwoBackups() throws Throwable {
        super.testRestartWithTxEightNodesTwoBackups();
    }

    /** {@inheritDoc} */
    @Override public void testRestartWithTxFourNodesNoBackups() throws Throwable {
        super.testRestartWithTxFourNodesNoBackups();
    }

    /** {@inheritDoc} */
    @Override public void testRestartWithTxFourNodesOneBackups() throws Throwable {
        super.testRestartWithTxFourNodesOneBackups();
    }

    /** {@inheritDoc} */
    @Override public void testRestartWithTxSixNodesTwoBackups() throws Throwable {
        super.testRestartWithTxSixNodesTwoBackups();
    }

    /** {@inheritDoc} */
    @Override public void testRestartWithTxTenNodesTwoBackups() throws Throwable {
        super.testRestartWithTxTenNodesTwoBackups();
    }

    /** {@inheritDoc} */
    @Override public void testRestartWithTxTwoNodesNoBackups() throws Throwable {
        super.testRestartWithTxTwoNodesNoBackups();
    }

    /** {@inheritDoc} */
    @Override public void testRestartWithTxTwoNodesOneBackup() throws Throwable {
        super.testRestartWithTxTwoNodesOneBackup();
    }
}
