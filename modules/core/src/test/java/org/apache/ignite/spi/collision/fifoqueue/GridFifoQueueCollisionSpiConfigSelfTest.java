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

package org.apache.ignite.spi.collision.fifoqueue;

import org.gridgain.testframework.junits.spi.*;

/**
 * Unit tests for {@link FifoQueueCollisionSpi} config.
 */
@GridSpiTest(spi = FifoQueueCollisionSpi.class, group = "Collision SPI")
public class GridFifoQueueCollisionSpiConfigSelfTest extends GridSpiAbstractConfigTest<FifoQueueCollisionSpi> {
    /**
     * @throws Exception If failed.
     */
    public void testNegativeConfig() throws Exception {
        checkNegativeSpiProperty(new FifoQueueCollisionSpi(), "parallelJobsNumber", 0);
        checkNegativeSpiProperty(new FifoQueueCollisionSpi(), "waitingJobsNumber", -1);
    }
}
