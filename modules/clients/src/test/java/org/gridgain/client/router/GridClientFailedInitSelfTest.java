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

package org.gridgain.client.router;

import org.apache.ignite.*;
import org.apache.ignite.compute.*;
import org.apache.ignite.configuration.*;
import org.gridgain.client.*;
import org.gridgain.client.impl.connection.*;
import org.gridgain.grid.*;
import org.apache.ignite.spi.discovery.tcp.*;
import org.apache.ignite.spi.discovery.tcp.ipfinder.*;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.*;
import org.gridgain.grid.util.typedef.*;
import org.gridgain.testframework.junits.common.*;

import java.util.*;

import static org.gridgain.client.GridClientProtocol.*;
import static org.gridgain.client.integration.GridClientAbstractSelfTest.*;
import static org.apache.ignite.IgniteSystemProperties.*;

/**
 *
 */
public class GridClientFailedInitSelfTest extends GridCommonAbstractTest {
    /** */
    private static final int RECONN_CNT = 3;

    /** */
    private static final long TOP_REFRESH_PERIOD = 5000;

    /** */
    private static final int ROUTER_BINARY_PORT = BINARY_PORT + 1;

    /** */
    private static final int ROUTER_JETTY_PORT = 8081;

    /** */
    private static final TcpDiscoveryIpFinder IP_FINDER = new TcpDiscoveryVmIpFinder(true);

    /** {@inheritDoc} */
    @Override protected void afterTest() throws Exception {
        GridClientFactory.stopAll();
        GridRouterFactory.stopAllRouters();
        stopAllGrids();
    }

    /** {@inheritDoc} */
    @Override protected IgniteConfiguration getConfiguration(String gridName) throws Exception {
        IgniteConfiguration cfg = super.getConfiguration(gridName);

        assert cfg.getClientConnectionConfiguration() == null;

        cfg.setLocalHost(HOST);

        ClientConnectionConfiguration clientCfg = new ClientConnectionConfiguration();

        clientCfg.setRestTcpPort(BINARY_PORT);
        clientCfg.setRestJettyPath(REST_JETTY_CFG);

        cfg.setClientConnectionConfiguration(clientCfg);

        TcpDiscoverySpi disco = new TcpDiscoverySpi();

        disco.setIpFinder(IP_FINDER);

        cfg.setDiscoverySpi(disco);

        return cfg;
    }

    /**
     *
     */
    public void testEmptyAddresses() {
        try {
            GridClientFactory.start(new GridClientConfiguration());

            assert false;
        }
        catch (GridClientException e) {
            info("Caught expected exception: " + e);
        }
    }

    /**
     *
     */
    public void testRoutersAndServersAddressesProvided() {
        try {
            GridClientConfiguration c = new GridClientConfiguration();

            c.setRouters(Collections.singleton("127.0.0.1:10000"));
            c.setServers(Collections.singleton("127.0.0.1:10000"));

            GridClientFactory.start(c);

            assert false;
        }
        catch (GridClientException e) {
            info("Caught expected exception: " + e);
        }
    }

    /**
     * @throws Exception If failed.
     */
    public void testTcpClient() throws Exception {
        doTestClient(TCP);
    }

    /**
     * @throws Exception If failed.
     */
    public void testTcpRouter() throws Exception {
        doTestRouter(TCP);
    }

    /**
     * @param p Protocol.
     * @throws Exception If failed.
     */
    @SuppressWarnings("BusyWait")
    private void doTestClient(GridClientProtocol p) throws Exception {
        GridClient c = client(p, false);

        for (int i = 0; i < RECONN_CNT; i++) {
            try {
                c.compute().nodes();
            }
            catch (GridClientDisconnectedException e) {
                assertTrue(X.hasCause(e,
                    GridServerUnreachableException.class, GridClientConnectionResetException.class));
            }

            startGrid();

            try {
                Thread.sleep(TOP_REFRESH_PERIOD * 2);

                c.compute().nodes();

                assertEquals("arg", c.compute().execute(TestTask.class.getName(), "arg"));
            }
            finally {
                stopGrid();
            }

            Thread.sleep(TOP_REFRESH_PERIOD * 2);
        }
    }

    /**
     * @param p Protocol.
     * @throws Exception If failed.
     */
    @SuppressWarnings("BusyWait")
    private void doTestRouter(GridClientProtocol p) throws Exception {
        startRouters();

        GridClient c = client(p, true);

        for (int i = 0; i < RECONN_CNT; i++) {
            try {
                c.compute().nodes();

                fail("Nodes list should fail while grid is stopped.");
            }
            catch (GridClientDisconnectedException e) {
                assertTrue(X.hasCause(e, GridClientException.class));
            }

            startGrid();

            try {
                Thread.sleep(TOP_REFRESH_PERIOD * 2);

                c.compute().nodes();

                assertEquals("arg", c.compute().execute(TestTask.class.getName(), "arg"));
            }
            finally {
                stopGrid();
            }

            Thread.sleep(TOP_REFRESH_PERIOD * 2);
        }
    }

    /**
     * @return Grid.
     * @throws Exception If failed.
     */
    @Override protected Ignite startGrid() throws Exception {
        System.setProperty(GG_JETTY_PORT, Integer.toString(JETTY_PORT));

        try {
            return super.startGrid();
        }
        finally {
            System.clearProperty(GG_JETTY_PORT);
        }
    }

    /**
     * Starts router.
     * @throws IgniteCheckedException If failed.
     */
    private void startRouters() throws IgniteCheckedException {
        GridTcpRouterConfiguration tcpCfg = new GridTcpRouterConfiguration();

        tcpCfg.setHost(HOST);
        tcpCfg.setPort(ROUTER_BINARY_PORT);
        tcpCfg.setPortRange(0);
        tcpCfg.setServers(Collections.singleton(HOST + ":" + BINARY_PORT));

        GridRouterFactory.startTcpRouter(tcpCfg);
    }

    /**
     * @param p Protocol.
     * @param useRouter Use router flag.
     * @return Client instance.
     * @throws GridClientException If failed.
     */
    private GridClient client(GridClientProtocol p, boolean useRouter) throws GridClientException {
        GridClientConfiguration cfg = new GridClientConfiguration();

        int port = p == TCP ?
            (useRouter ? ROUTER_BINARY_PORT : BINARY_PORT) :
            (useRouter ? ROUTER_JETTY_PORT : JETTY_PORT);

        cfg.setProtocol(p);
        cfg.setServers(Arrays.asList(HOST + ":" + port));
        cfg.setTopologyRefreshFrequency(TOP_REFRESH_PERIOD);

        return GridClientFactory.start(cfg);
    }

    /**
     * Test task.
     */
    private static class TestTask extends ComputeTaskSplitAdapter<String, String> {
        /** {@inheritDoc} */
        @Override protected Collection<? extends ComputeJob> split(int gridSize, final String arg) throws IgniteCheckedException {
            return Collections.singleton(new ComputeJobAdapter() {
                @Override public String execute() {
                    return arg;
                }
            });
        }

        /** {@inheritDoc} */
        @Override public String reduce(List<ComputeJobResult> results) throws IgniteCheckedException {
            assertEquals(1, results.size());

            return results.get(0).getData();
        }
    }
}
