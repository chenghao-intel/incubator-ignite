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

package org.apache.ignite.internal.processors.cache.distributed.near;

import org.apache.ignite.*;
import org.apache.ignite.internal.*;
import org.apache.ignite.internal.processors.cache.*;
import org.apache.ignite.internal.processors.cache.distributed.*;
import org.apache.ignite.internal.processors.cache.transactions.*;
import org.apache.ignite.internal.processors.cache.version.*;
import org.apache.ignite.internal.util.tostring.*;
import org.apache.ignite.internal.util.typedef.internal.*;
import org.apache.ignite.lang.*;
import org.apache.ignite.plugin.extensions.communication.*;
import org.apache.ignite.transactions.*;
import org.jetbrains.annotations.*;

import javax.cache.*;
import java.io.*;
import java.nio.*;
import java.util.*;

/**
 * Near cache lock request.
 */
public class GridNearLockRequest<K, V> extends GridDistributedLockRequest<K, V> {
    /** */
    private static final long serialVersionUID = 0L;

    /** Topology version. */
    private long topVer;

    /** Mini future ID. */
    private IgniteUuid miniId;

    /** Filter. */
    private byte[][] filterBytes;

    /** Filter. */
    @GridDirectTransient
    private IgnitePredicate<Cache.Entry<K, V>>[] filter;

    /** Implicit flag. */
    private boolean implicitTx;

    /** Implicit transaction with one key flag. */
    private boolean implicitSingleTx;

    /** One phase commit flag. */
    private boolean onePhaseCommit;

    /** Array of mapped DHT versions for this entry. */
    @GridToStringInclude
    private GridCacheVersion[] dhtVers;

    /** Subject ID. */
    private UUID subjId;

    /** Task name hash. */
    private int taskNameHash;

    /** Has transforms flag. */
    private boolean hasTransforms;

    /** Sync commit flag. */
    private boolean syncCommit;

    /** TTL for read operation. */
    private long accessTtl;

    /**
     * Empty constructor required for {@link Externalizable}.
     */
    public GridNearLockRequest() {
        // No-op.
    }

    /**
     * @param cacheId Cache ID.
     * @param topVer Topology version.
     * @param nodeId Node ID.
     * @param threadId Thread ID.
     * @param futId Future ID.
     * @param lockVer Cache version.
     * @param isInTx {@code True} if implicit transaction lock.
     * @param implicitTx Flag to indicate that transaction is implicit.
     * @param implicitSingleTx Implicit-transaction-with-one-key flag.
     * @param isRead Indicates whether implicit lock is for read or write operation.
     * @param isolation Transaction isolation.
     * @param isInvalidate Invalidation flag.
     * @param timeout Lock timeout.
     * @param keyCnt Number of keys.
     * @param txSize Expected transaction size.
     * @param syncCommit Synchronous commit flag.
     * @param grpLockKey Group lock key if this is a group-lock transaction.
     * @param partLock If partition is locked.
     * @param subjId Subject ID.
     * @param taskNameHash Task name hash code.
     * @param accessTtl TTL for read operation.
     */
    public GridNearLockRequest(
        int cacheId,
        long topVer,
        UUID nodeId,
        long threadId,
        IgniteUuid futId,
        GridCacheVersion lockVer,
        boolean isInTx,
        boolean implicitTx,
        boolean implicitSingleTx,
        boolean isRead,
        TransactionIsolation isolation,
        boolean isInvalidate,
        long timeout,
        int keyCnt,
        int txSize,
        boolean syncCommit,
        @Nullable IgniteTxKey grpLockKey,
        boolean partLock,
        @Nullable UUID subjId,
        int taskNameHash,
        long accessTtl
    ) {
        super(
            cacheId,
            nodeId,
            lockVer,
            threadId,
            futId,
            lockVer,
            isInTx,
            isRead,
            isolation,
            isInvalidate,
            timeout,
            keyCnt,
            txSize,
            grpLockKey,
            partLock);

        assert topVer > 0;

        this.topVer = topVer;
        this.implicitTx = implicitTx;
        this.implicitSingleTx = implicitSingleTx;
        this.syncCommit = syncCommit;
        this.subjId = subjId;
        this.taskNameHash = taskNameHash;
        this.accessTtl = accessTtl;

        dhtVers = new GridCacheVersion[keyCnt];
    }

    /**
     * @return Topology version.
     */
    @Override public long topologyVersion() {
        return topVer;
    }

    /**
     * @return Subject ID.
     */
    public UUID subjectId() {
        return subjId;
    }

    /**
     * @return Task name hash.q
     */
    public int taskNameHash() {
        return taskNameHash;
    }

    /**
     * @return Implicit transaction flag.
     */
    public boolean implicitTx() {
        return implicitTx;
    }

    /**
     * @return Implicit-transaction-with-one-key flag.
     */
    public boolean implicitSingleTx() {
        return implicitSingleTx;
    }

    /**
     * @return One phase commit flag.
     */
    public boolean onePhaseCommit() {
        return onePhaseCommit;
    }

    /**
     * @param onePhaseCommit One phase commit flag.
     */
    public void onePhaseCommit(boolean onePhaseCommit) {
        this.onePhaseCommit = onePhaseCommit;
    }

    /**
     * @return Sync commit flag.
     */
    public boolean syncCommit() {
        return syncCommit;
    }

    /**
     * @return Filter.
     */
    public IgnitePredicate<Cache.Entry<K, V>>[] filter() {
        return filter;
    }

    /**
     * @param filter Filter.
     * @param ctx Context.
     * @throws IgniteCheckedException If failed.
     */
    public void filter(IgnitePredicate<Cache.Entry<K, V>>[] filter, GridCacheContext<K, V> ctx)
        throws IgniteCheckedException {
        this.filter = filter;
    }

    /**
     * @return Mini future ID.
     */
    public IgniteUuid miniId() {
        return miniId;
    }

    /**
     * @param miniId Mini future Id.
     */
    public void miniId(IgniteUuid miniId) {
        this.miniId = miniId;
    }

    /**
     * @param hasTransforms {@code True} if originating transaction has transform entries.
     */
    public void hasTransforms(boolean hasTransforms) {
        this.hasTransforms = hasTransforms;
    }

    /**
     * @return {@code True} if originating transaction has transform entries.
     */
    public boolean hasTransforms() {
        return hasTransforms;
    }

    /**
     * Adds a key.
     *
     * @param key Key.
     * @param retVal Flag indicating whether value should be returned.
     * @param keyBytes Key bytes.
     * @param dhtVer DHT version.
     * @param ctx Context.
     * @throws IgniteCheckedException If failed.
     */
    public void addKeyBytes(
        K key,
        byte[] keyBytes,
        boolean retVal,
        @Nullable GridCacheVersion dhtVer,
        GridCacheContext<K, V> ctx
    ) throws IgniteCheckedException {
        dhtVers[idx] = dhtVer;

        // Delegate to super.
        addKeyBytes(key, keyBytes, retVal, (Collection<GridCacheMvccCandidate<K>>)null, ctx);
    }

    /**
     * @param idx Index of the key.
     * @return DHT version for key at given index.
     */
    public GridCacheVersion dhtVersion(int idx) {
        return dhtVers[idx];
    }

    /**
     * @return TTL for read operation.
     */
    public long accessTtl() {
        return accessTtl;
    }

    /** {@inheritDoc} */
    @Override public void prepareMarshal(GridCacheSharedContext<K, V> ctx) throws IgniteCheckedException {
        super.prepareMarshal(ctx);

        if (filterBytes == null)
            filterBytes = marshalFilter(filter, ctx);
    }

    /** {@inheritDoc} */
    @Override public void finishUnmarshal(GridCacheSharedContext<K, V> ctx, ClassLoader ldr) throws IgniteCheckedException {
        super.finishUnmarshal(ctx, ldr);

        if (filter == null && filterBytes != null)
            filter = unmarshalFilter(filterBytes, ctx, ldr);
    }

    /** {@inheritDoc} */
    @Override public boolean writeTo(ByteBuffer buf, MessageWriter writer) {
        writer.setBuffer(buf);

        if (!super.writeTo(buf, writer))
            return false;

        if (!writer.isHeaderWritten()) {
            if (!writer.writeHeader(directType(), fieldsCount()))
                return false;

            writer.onHeaderWritten();
        }

        switch (writer.state()) {
            case 22:
                if (!writer.writeLong("accessTtl", accessTtl))
                    return false;

                writer.incrementState();

            case 23:
                if (!writer.writeObjectArray("dhtVers", dhtVers, MessageCollectionItemType.MSG))
                    return false;

                writer.incrementState();

            case 24:
                if (!writer.writeObjectArray("filterBytes", filterBytes, MessageCollectionItemType.BYTE_ARR))
                    return false;

                writer.incrementState();

            case 25:
                if (!writer.writeBoolean("hasTransforms", hasTransforms))
                    return false;

                writer.incrementState();

            case 26:
                if (!writer.writeBoolean("implicitSingleTx", implicitSingleTx))
                    return false;

                writer.incrementState();

            case 27:
                if (!writer.writeBoolean("implicitTx", implicitTx))
                    return false;

                writer.incrementState();

            case 28:
                if (!writer.writeIgniteUuid("miniId", miniId))
                    return false;

                writer.incrementState();

            case 29:
                if (!writer.writeBoolean("onePhaseCommit", onePhaseCommit))
                    return false;

                writer.incrementState();

            case 30:
                if (!writer.writeUuid("subjId", subjId))
                    return false;

                writer.incrementState();

            case 31:
                if (!writer.writeBoolean("syncCommit", syncCommit))
                    return false;

                writer.incrementState();

            case 32:
                if (!writer.writeInt("taskNameHash", taskNameHash))
                    return false;

                writer.incrementState();

            case 33:
                if (!writer.writeLong("topVer", topVer))
                    return false;

                writer.incrementState();

        }

        return true;
    }

    /** {@inheritDoc} */
    @Override public boolean readFrom(ByteBuffer buf, MessageReader reader) {
        reader.setBuffer(buf);

        if (!reader.beforeMessageRead())
            return false;

        if (!super.readFrom(buf, reader))
            return false;

        switch (reader.state()) {
            case 22:
                accessTtl = reader.readLong("accessTtl");

                if (!reader.isLastRead())
                    return false;

                reader.incrementState();

            case 23:
                dhtVers = reader.readObjectArray("dhtVers", MessageCollectionItemType.MSG, GridCacheVersion.class);

                if (!reader.isLastRead())
                    return false;

                reader.incrementState();

            case 24:
                filterBytes = reader.readObjectArray("filterBytes", MessageCollectionItemType.BYTE_ARR, byte[].class);

                if (!reader.isLastRead())
                    return false;

                reader.incrementState();

            case 25:
                hasTransforms = reader.readBoolean("hasTransforms");

                if (!reader.isLastRead())
                    return false;

                reader.incrementState();

            case 26:
                implicitSingleTx = reader.readBoolean("implicitSingleTx");

                if (!reader.isLastRead())
                    return false;

                reader.incrementState();

            case 27:
                implicitTx = reader.readBoolean("implicitTx");

                if (!reader.isLastRead())
                    return false;

                reader.incrementState();

            case 28:
                miniId = reader.readIgniteUuid("miniId");

                if (!reader.isLastRead())
                    return false;

                reader.incrementState();

            case 29:
                onePhaseCommit = reader.readBoolean("onePhaseCommit");

                if (!reader.isLastRead())
                    return false;

                reader.incrementState();

            case 30:
                subjId = reader.readUuid("subjId");

                if (!reader.isLastRead())
                    return false;

                reader.incrementState();

            case 31:
                syncCommit = reader.readBoolean("syncCommit");

                if (!reader.isLastRead())
                    return false;

                reader.incrementState();

            case 32:
                taskNameHash = reader.readInt("taskNameHash");

                if (!reader.isLastRead())
                    return false;

                reader.incrementState();

            case 33:
                topVer = reader.readLong("topVer");

                if (!reader.isLastRead())
                    return false;

                reader.incrementState();

        }

        return true;
    }

    /** {@inheritDoc} */
    @Override public byte directType() {
        return 51;
    }

    /** {@inheritDoc} */
    @Override public byte fieldsCount() {
        return 34;
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return S.toString(GridNearLockRequest.class, this, "filter", Arrays.toString(filter),
            "super", super.toString());
    }
}
