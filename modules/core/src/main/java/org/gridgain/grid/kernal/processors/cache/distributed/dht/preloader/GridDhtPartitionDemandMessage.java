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

package org.gridgain.grid.kernal.processors.cache.distributed.dht.preloader;

import org.apache.ignite.*;
import org.gridgain.grid.kernal.*;
import org.gridgain.grid.kernal.processors.cache.*;
import org.gridgain.grid.util.direct.*;
import org.gridgain.grid.util.tostring.*;
import org.gridgain.grid.util.typedef.internal.*;

import java.io.*;
import java.nio.*;
import java.util.*;

/**
 * Partition demand request.
 */
public class GridDhtPartitionDemandMessage<K, V> extends GridCacheMessage<K, V> {
    /** */
    private static final long serialVersionUID = 0L;

    /** Update sequence. */
    private long updateSeq;

    /** Partition. */
    @GridToStringInclude
    @GridDirectCollection(int.class)
    private Set<Integer> parts;

    /** Topic. */
    @GridDirectTransient
    private Object topic;

    /** Serialized topic. */
    private byte[] topicBytes;

    /** Timeout. */
    private long timeout;

    /** Worker ID. */
    private int workerId = -1;

    /** Topology version. */
    private long topVer;

    /**
     * @param updateSeq Update sequence for this node.
     * @param topVer Topology version.
     */
    GridDhtPartitionDemandMessage(long updateSeq, long topVer, int cacheId) {
        assert updateSeq > 0;

        this.cacheId = cacheId;
        this.updateSeq = updateSeq;
        this.topVer = topVer;
    }

    /**
     * @param cp Message to copy from.
     */
    GridDhtPartitionDemandMessage(GridDhtPartitionDemandMessage<K, V> cp, Collection<Integer> parts) {
        cacheId = cp.cacheId;
        updateSeq = cp.updateSeq;
        topic = cp.topic;
        timeout = cp.timeout;
        workerId = cp.workerId;
        topVer = cp.topVer;

        // Create a copy of passed in collection since it can be modified when this message is being sent.
        this.parts = new HashSet<>(parts);
    }

    /**
     * Empty constructor required for {@link Externalizable}.
     */
    public GridDhtPartitionDemandMessage() {
        // No-op.
    }

    /** {@inheritDoc} */
    @Override public boolean allowForStartup() {
        return true;
    }

    /**
     * @param p Partition.
     */
    void addPartition(int p) {
        if (parts == null)
            parts = new HashSet<>();

        parts.add(p);
    }


    /**
     * @return Partition.
     */
    Set<Integer> partitions() {
        return parts;
    }

    /**
     * @return Update sequence.
     */
    long updateSequence() {
        return updateSeq;
    }

    /**
     * @return Reply message timeout.
     */
    long timeout() {
        return timeout;
    }

    /**
     * @param timeout Timeout.
     */
    void timeout(long timeout) {
        this.timeout = timeout;
    }

    /**
     * @return Topic.
     */
    Object topic() {
        return topic;
    }

    /**
     * @param topic Topic.
     */
    void topic(Object topic) {
        this.topic = topic;
    }

    /**
     * @return Worker ID.
     */
    int workerId() {
        return workerId;
    }

    /**
     * @param workerId Worker ID.
     */
    void workerId(int workerId) {
        this.workerId = workerId;
    }

    /**
     * @return Topology version for which demand message is sent.
     */
    @Override public long topologyVersion() {
        return topVer;
    }

    /** {@inheritDoc}
     * @param ctx*/
    @Override public void prepareMarshal(GridCacheSharedContext<K, V> ctx) throws IgniteCheckedException {
        super.prepareMarshal(ctx);

        if (topic != null)
            topicBytes = ctx.marshaller().marshal(topic);
    }

    /** {@inheritDoc} */
    @Override public void finishUnmarshal(GridCacheSharedContext<K, V> ctx, ClassLoader ldr) throws IgniteCheckedException {
        super.finishUnmarshal(ctx, ldr);

        if (topicBytes != null)
            topic = ctx.marshaller().unmarshal(topicBytes, ldr);
    }

    /** {@inheritDoc} */
    @SuppressWarnings({"CloneDoesntCallSuperClone", "CloneCallsConstructors"})
    @Override public GridTcpCommunicationMessageAdapter clone() {
        GridDhtPartitionDemandMessage _clone = new GridDhtPartitionDemandMessage();

        clone0(_clone);

        return _clone;
    }

    /** {@inheritDoc} */
    @Override protected void clone0(GridTcpCommunicationMessageAdapter _msg) {
        super.clone0(_msg);

        GridDhtPartitionDemandMessage _clone = (GridDhtPartitionDemandMessage)_msg;

        _clone.updateSeq = updateSeq;
        _clone.parts = parts;
        _clone.topic = topic;
        _clone.topicBytes = topicBytes;
        _clone.timeout = timeout;
        _clone.workerId = workerId;
        _clone.topVer = topVer;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("all")
    @Override public boolean writeTo(ByteBuffer buf) {
        commState.setBuffer(buf);

        if (!super.writeTo(buf))
            return false;

        if (!commState.typeWritten) {
            if (!commState.putByte(directType()))
                return false;

            commState.typeWritten = true;
        }

        switch (commState.idx) {
            case 3:
                if (parts != null) {
                    if (commState.it == null) {
                        if (!commState.putInt(parts.size()))
                            return false;

                        commState.it = parts.iterator();
                    }

                    while (commState.it.hasNext() || commState.cur != NULL) {
                        if (commState.cur == NULL)
                            commState.cur = commState.it.next();

                        if (!commState.putInt((int)commState.cur))
                            return false;

                        commState.cur = NULL;
                    }

                    commState.it = null;
                } else {
                    if (!commState.putInt(-1))
                        return false;
                }

                commState.idx++;

            case 4:
                if (!commState.putLong(timeout))
                    return false;

                commState.idx++;

            case 5:
                if (!commState.putLong(topVer))
                    return false;

                commState.idx++;

            case 6:
                if (!commState.putByteArray(topicBytes))
                    return false;

                commState.idx++;

            case 7:
                if (!commState.putLong(updateSeq))
                    return false;

                commState.idx++;

            case 8:
                if (!commState.putInt(workerId))
                    return false;

                commState.idx++;

        }

        return true;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("all")
    @Override public boolean readFrom(ByteBuffer buf) {
        commState.setBuffer(buf);

        if (!super.readFrom(buf))
            return false;

        switch (commState.idx) {
            case 3:
                if (commState.readSize == -1) {
                    if (buf.remaining() < 4)
                        return false;

                    commState.readSize = commState.getInt();
                }

                if (commState.readSize >= 0) {
                    if (parts == null)
                        parts = new HashSet<>(commState.readSize);

                    for (int i = commState.readItems; i < commState.readSize; i++) {
                        if (buf.remaining() < 4)
                            return false;

                        int _val = commState.getInt();

                        parts.add((Integer)_val);

                        commState.readItems++;
                    }
                }

                commState.readSize = -1;
                commState.readItems = 0;

                commState.idx++;

            case 4:
                if (buf.remaining() < 8)
                    return false;

                timeout = commState.getLong();

                commState.idx++;

            case 5:
                if (buf.remaining() < 8)
                    return false;

                topVer = commState.getLong();

                commState.idx++;

            case 6:
                byte[] topicBytes0 = commState.getByteArray();

                if (topicBytes0 == BYTE_ARR_NOT_READ)
                    return false;

                topicBytes = topicBytes0;

                commState.idx++;

            case 7:
                if (buf.remaining() < 8)
                    return false;

                updateSeq = commState.getLong();

                commState.idx++;

            case 8:
                if (buf.remaining() < 4)
                    return false;

                workerId = commState.getInt();

                commState.idx++;

        }

        return true;
    }

    /** {@inheritDoc} */
    @Override public byte directType() {
        return 43;
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return S.toString(GridDhtPartitionDemandMessage.class, this, "partCnt", parts.size(), "super",
            super.toString());
    }
}
