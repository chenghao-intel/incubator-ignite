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

package org.gridgain.grid.kernal.managers.communication;

import org.apache.ignite.configuration.*;
import org.gridgain.grid.kernal.*;
import org.apache.ignite.spi.communication.tcp.*;
import org.apache.ignite.spi.discovery.tcp.*;
import org.apache.ignite.spi.discovery.tcp.ipfinder.*;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.*;
import org.gridgain.grid.util.direct.*;
import org.gridgain.testframework.junits.common.*;

import java.nio.*;
import java.util.*;
import java.util.concurrent.*;

import static java.util.concurrent.TimeUnit.*;

/**
 * Send message test.
 */
public class GridCommunicationSendMessageSelfTest extends GridCommonAbstractTest {
    /** IP finder. */
    private static final TcpDiscoveryIpFinder ipFinder = new TcpDiscoveryVmIpFinder(true);

    /** Sample count. */
    private static final int SAMPLE_CNT = 1;

    /** */
    private static final byte DIRECT_TYPE = (byte)202;

    /** */
    private int bufSize;

    static {
        GridTcpCommunicationMessageFactory.registerCustom(new GridTcpCommunicationMessageProducer() {
            @Override public GridTcpCommunicationMessageAdapter create(byte type) {
                return new TestMessage();
            }
        }, DIRECT_TYPE);
    }

    /** {@inheritDoc} */
    @Override protected IgniteConfiguration getConfiguration(String gridName) throws Exception {
        IgniteConfiguration c = super.getConfiguration(gridName);

        TcpDiscoverySpi discoSpi = new TcpDiscoverySpi();

        discoSpi.setIpFinder(ipFinder);

        c.setDiscoverySpi(discoSpi);

        TcpCommunicationSpi commSpi = new TcpCommunicationSpi();

        commSpi.setConnectionBufferSize(bufSize);

        c.setCommunicationSpi(commSpi);

        return c;
    }

    /**
     * @throws Exception If failed.
     */
    public void testSendMessage() throws Exception {
        try {
            startGridsMultiThreaded(2);

            doSend();
        }
        finally {
            stopAllGrids();
        }
    }

    /**
     * @throws Exception If failed.
     */
    public void testSendMessageWithBuffer() throws Exception {
        bufSize = 8192;

        try {
            startGridsMultiThreaded(2);

            doSend();
        }
        finally {
            stopAllGrids();
        }
    }

    /**
     * @throws Exception If failed.
     */
    private void doSend() throws Exception {
        GridIoManager mgr0 = ((GridKernal)grid(0)).context().io();
        GridIoManager mgr1 = ((GridKernal)grid(1)).context().io();

        String topic = "test-topic";

        final CountDownLatch latch = new CountDownLatch(SAMPLE_CNT);

        mgr1.addMessageListener(topic, new GridMessageListener() {
            @Override public void onMessage(UUID nodeId, Object msg) {
                latch.countDown();
            }
        });

        long time = System.nanoTime();

        for (int i = 1; i <= SAMPLE_CNT; i++) {
            mgr0.send(grid(1).localNode(), topic, new TestMessage(), GridIoPolicy.PUBLIC_POOL);

            if (i % 500 == 0)
                info("Sent messages count: " + i);
        }

        assert latch.await(3, SECONDS);

        time = System.nanoTime() - time;

        info(">>>");
        info(">>> send() time (ms): " + MILLISECONDS.convert(time, NANOSECONDS));
        info(">>>");
    }

    /** */
    private static class TestMessage extends GridTcpCommunicationMessageAdapter {
        /** {@inheritDoc} */
        @SuppressWarnings("CloneDoesntCallSuperClone")
        @Override public GridTcpCommunicationMessageAdapter clone() {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override protected void clone0(GridTcpCommunicationMessageAdapter _msg) {
            // No-op.
        }

        /** {@inheritDoc} */
        @Override public boolean writeTo(ByteBuffer buf) {
            commState.setBuffer(buf);

            return commState.putByte(directType());
        }

        /** {@inheritDoc} */
        @Override public boolean readFrom(ByteBuffer buf) {
            return true;
        }

        /** {@inheritDoc} */
        @Override public byte directType() {
            return DIRECT_TYPE;
        }
    }
}
