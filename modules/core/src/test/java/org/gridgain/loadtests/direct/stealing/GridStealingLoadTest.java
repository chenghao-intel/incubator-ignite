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

package org.gridgain.loadtests.direct.stealing;

import org.apache.ignite.*;
import org.apache.ignite.compute.*;
import org.apache.ignite.configuration.*;
import org.gridgain.grid.loadtest.*;
import org.apache.ignite.spi.collision.jobstealing.*;
import org.apache.ignite.spi.discovery.*;
import org.apache.ignite.spi.discovery.tcp.*;
import org.apache.ignite.spi.failover.jobstealing.*;
import org.gridgain.testframework.*;
import org.gridgain.testframework.config.*;
import org.gridgain.testframework.junits.common.*;

import java.util.*;
import java.util.concurrent.atomic.*;

/**
 *
 */
@GridCommonTest(group = "Load Test")
public class GridStealingLoadTest extends GridCommonAbstractTest {
    /** */
    public GridStealingLoadTest() {
        super(false);
    }

    /**
     * @return Number of threads for the test.
     */
    private int getThreadCount() {
        return Integer.valueOf(GridTestProperties.getProperty("load.test.threadnum"));
    }

    /** {@inheritDoc} */
    @Override protected void beforeTestsStarted() throws Exception {
        startGrids(2);
    }

    /** {@inheritDoc} */
    @Override protected void afterTestsStopped() throws Exception {
        stopAllGrids();
    }

    /** {@inheritDoc} */
    @Override protected IgniteConfiguration getConfiguration(String name) throws Exception {
        IgniteConfiguration cfg = super.getConfiguration(name);

        JobStealingCollisionSpi colSpi = new JobStealingCollisionSpi();

        assert colSpi.getActiveJobsThreshold() == JobStealingCollisionSpi.DFLT_ACTIVE_JOBS_THRESHOLD;
        assert colSpi.getWaitJobsThreshold() == JobStealingCollisionSpi.DFLT_WAIT_JOBS_THRESHOLD;

        // One job at a time.
        colSpi.setActiveJobsThreshold(5);
        colSpi.setWaitJobsThreshold(0);
        colSpi.setMessageExpireTime(5000);

        JobStealingFailoverSpi failSpi = new JobStealingFailoverSpi();

        // Verify defaults.
        assert failSpi.getMaximumFailoverAttempts() == JobStealingFailoverSpi.DFLT_MAX_FAILOVER_ATTEMPTS;

        DiscoverySpi discoSpi = new TcpDiscoverySpi();

        cfg.setDiscoverySpi(discoSpi);
        cfg.setCollisionSpi(colSpi);
        cfg.setFailoverSpi(failSpi);

        return cfg;
    }

    /**
     * @throws Exception If failed.
     */
    @SuppressWarnings("unchecked")
    public void testStealingLoad() throws Exception {
        final Ignite ignite = grid(0);

        assert ignite != null;

        assert !ignite.cluster().forRemotes().nodes().isEmpty() : "Test requires at least 2 nodes.";

        final UUID stealingNodeId = ignite.cluster().forRemotes().nodes().iterator().next().id();

        info("Set stealing node id to: " + stealingNodeId);

        ignite.compute().localDeployTask(GridStealingLoadTestTask.class, GridStealingLoadTestTask.class.getClassLoader());

        final long end = 2 * 60 * 1000 + System.currentTimeMillis();

        info("Test timeout: " + getTestTimeout() + " ms.");
        info("Thread count: " + getThreadCount());

        final GridLoadTestStatistics stats = new GridLoadTestStatistics();

        final AtomicBoolean failed = new AtomicBoolean(false);

        final AtomicInteger stolen = new AtomicInteger(0);

        GridTestUtils.runMultiThreaded(new Runnable() {
            /** {@inheritDoc} */
            @Override public void run() {
                try {
                    while (end - System.currentTimeMillis() > 0) {
                        long start = System.currentTimeMillis();

                        // Pass stealing node id.
                        ComputeTaskFuture<?> fut = ignite.compute().withTimeout(20000).
                            execute(GridStealingLoadTestTask.class.getName(), stealingNodeId);

                        stolen.addAndGet((Integer)fut.get());

                        long taskCnt = stats.onTaskCompleted(fut, 1, System.currentTimeMillis() - start);

                        if (taskCnt % 500 == 0)
                            info("Stats [stats=" + stats.toString() + ", stolen=" + stolen + ']');
                    }
                }
                catch (Throwable e) {
                    error("Load test failed.", e);

                    failed.set(true);
                }
            }
        }, getThreadCount(), "grid-load-test-thread");

        info("Final test statistics: " + stats);

        if (failed.get())
            fail();

        assert stolen.get() != 0: "No jobs were stolen by stealing node.";

        info("Stolen jobs: " + stolen.get());
    }
}
