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

package org.apache.ignite.spi.discovery.tcp.messages;

import org.gridgain.grid.util.typedef.internal.*;

import java.io.*;
import java.util.*;

/**
 * Message telling joining node that it has loopback problem (misconfiguration).
 * This means that remote node is configured to use loopback address, but joining node is not, or vise versa.
 */
public class TcpDiscoveryLoopbackProblemMessage extends TcpDiscoveryAbstractMessage {
    /** */
    private static final long serialVersionUID = 0L;

    /** Remote node addresses. */
    private Collection<String> addrs;

    /** Remote node host names. */
    private Collection<String> hostNames;

    /**
     * Public default no-arg constructor for {@link Externalizable} interface.
     */
    public TcpDiscoveryLoopbackProblemMessage() {
        // No-op.
    }

    /**
     * Constructor.
     *
     * @param creatorNodeId Creator node ID.
     * @param addrs Remote node addresses.
     * @param hostNames Remote node host names.
     */
    public TcpDiscoveryLoopbackProblemMessage(UUID creatorNodeId, Collection<String> addrs,
                                              Collection<String> hostNames) {
        super(creatorNodeId);

        this.addrs = addrs;
        this.hostNames = hostNames;
    }

    /**
     * @return Remote node addresses.
     */
    public Collection<String> addresses() {
        return addrs;
    }

    /**
     * @return Remote node host names.
     */
    public Collection<String> hostNames() {
        return hostNames;
    }

    /** {@inheritDoc} */
    @Override public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);

        U.writeCollection(out, addrs);
        U.writeCollection(out, hostNames);
    }

    /** {@inheritDoc} */
    @Override public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);

        addrs = U.readCollection(in);
        hostNames = U.readCollection(in);
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return S.toString(TcpDiscoveryLoopbackProblemMessage.class, this, "super", super.toString());
    }
}
