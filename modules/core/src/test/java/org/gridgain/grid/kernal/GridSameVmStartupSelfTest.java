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

package org.gridgain.grid.kernal;

import org.apache.ignite.*;
import org.apache.ignite.cluster.*;
import org.apache.ignite.events.*;
import org.apache.ignite.lang.*;
import org.gridgain.grid.util.typedef.*;
import org.gridgain.testframework.junits.common.*;

import java.util.*;
import java.util.concurrent.*;

import static org.apache.ignite.events.IgniteEventType.*;

/**
 * Starts two grids on the same vm, checks topologies of each grid and discovery
 * events while stopping one them.
 */
@GridCommonTest(group = "Kernal Self")
public class GridSameVmStartupSelfTest extends GridCommonAbstractTest {
    /**
     *
     */
    public GridSameVmStartupSelfTest() {
        super(false);
    }

    /**
     * JUnit.
     *
     * @throws Exception If failed.
     */
    public void testSameVmStartup() throws Exception {
        Ignite ignite1 = startGrid(1);

        Collection<ClusterNode> top1 = ignite1.cluster().forRemotes().nodes();

        try {
            assert top1.isEmpty() : "Grid1 topology is not empty: " + top1;

            // Start another grid.
            Ignite ignite2 = startGrid(2);

            final CountDownLatch latch = new CountDownLatch(1);

            int size1 = ignite1.cluster().forRemotes().nodes().size();
            int size2 = ignite2.cluster().forRemotes().nodes().size();

            assert size1 == 1 : "Invalid number of remote nodes discovered: " + size1;
            assert size2 == 1 : "Invalid number of remote nodes discovered: " + size2;

            final UUID grid1LocNodeId = ignite1.cluster().localNode().id();

            ignite2.events().localListen(new IgnitePredicate<IgniteEvent>() {
                @Override public boolean apply(IgniteEvent evt) {
                    assert evt.type() != EVT_NODE_FAILED :
                        "Node1 did not exit gracefully.";

                    if (evt instanceof IgniteDiscoveryEvent) {
                        // Local node can send METRICS_UPDATED event.
                        assert ((IgniteDiscoveryEvent) evt).eventNode().id().equals(grid1LocNodeId) ||
                            evt.type() == EVT_NODE_METRICS_UPDATED :
                            "Received event about invalid node [received=" +
                                ((IgniteDiscoveryEvent) evt).eventNode().id() + ", expected=" + grid1LocNodeId +
                                ", type=" + evt.type() + ']';

                        if (evt.type() == EVT_NODE_LEFT)
                            latch.countDown();
                    }

                    return true;
                }
            }, EVTS_DISCOVERY);

            stopGrid(1);

            latch.await();

            Collection<ClusterNode> top2 = ignite2.cluster().forRemotes().nodes();

            assert top2.isEmpty() : "Grid2 topology is not empty: " + top2;
        }
        finally {
            stopGrid(1);
            stopGrid(2);
        }

        assert G.allGrids().isEmpty();
    }
}
