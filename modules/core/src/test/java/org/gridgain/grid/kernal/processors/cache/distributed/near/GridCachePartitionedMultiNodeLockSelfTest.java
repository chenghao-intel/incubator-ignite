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
import org.gridgain.grid.cache.*;
import org.gridgain.grid.kernal.processors.cache.distributed.*;

import static org.gridgain.grid.cache.GridCacheAtomicityMode.*;
import static org.gridgain.grid.cache.GridCacheDistributionMode.*;
import static org.gridgain.grid.cache.GridCacheMode.*;

/**
 * Test cases for multi-threaded tests.
 */
public class GridCachePartitionedMultiNodeLockSelfTest extends GridCacheMultiNodeLockAbstractTest {
    /** {@inheritDoc} */
    @Override protected IgniteConfiguration getConfiguration(String gridName) throws Exception {
        IgniteConfiguration c = super.getConfiguration(gridName);

        GridCacheConfiguration cc = defaultCacheConfiguration();

        cc.setCacheMode(PARTITIONED);
        cc.setBackups(2); // 2 backups, so all nodes are involved.
        cc.setAtomicityMode(TRANSACTIONAL);
        cc.setDistributionMode(NEAR_PARTITIONED);

        c.setCacheConfiguration(cc);

        return c;
    }

    /** {@inheritDoc} */
    @Override protected boolean partitioned() {
        return true;
    }

    /** {@inheritDoc} */
    @Override public void testBasicLock() throws Exception {
        super.testBasicLock();
    }

    /** {@inheritDoc} */
    @Override public void testLockMultithreaded() throws Exception {
        super.testLockMultithreaded();
    }

    /** {@inheritDoc} */
    @Override public void testLockReentry() throws IgniteCheckedException {
        super.testLockReentry();
    }

    /** {@inheritDoc} */
    @Override public void testMultiNodeLock() throws Exception {
        super.testMultiNodeLock();
    }

    /** {@inheritDoc} */
    @Override public void testMultiNodeLockAsync() throws Exception {
        super.testMultiNodeLockAsync();
    }

    /** {@inheritDoc} */
    @Override public void testMultiNodeLockAsyncWithKeyLists() throws Exception {
        super.testMultiNodeLockAsyncWithKeyLists();
    }

    /** {@inheritDoc} */
    @Override public void testMultiNodeLockWithKeyLists() throws Exception {
        super.testMultiNodeLockWithKeyLists();
    }
}
