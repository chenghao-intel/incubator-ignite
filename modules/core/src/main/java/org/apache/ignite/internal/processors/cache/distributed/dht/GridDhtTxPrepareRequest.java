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

package org.apache.ignite.internal.processors.cache.distributed.dht;

import org.apache.ignite.*;
import org.apache.ignite.internal.*;
import org.apache.ignite.internal.processors.cache.*;
import org.apache.ignite.internal.processors.cache.distributed.*;
import org.apache.ignite.internal.processors.cache.transactions.*;
import org.apache.ignite.internal.processors.cache.version.*;
import org.apache.ignite.internal.util.*;
import org.apache.ignite.internal.util.tostring.*;
import org.apache.ignite.internal.util.typedef.internal.*;
import org.apache.ignite.lang.*;
import org.apache.ignite.plugin.extensions.communication.*;
import org.jetbrains.annotations.*;

import java.io.*;
import java.nio.*;
import java.util.*;

/**
 * DHT prepare request.
 */
public class GridDhtTxPrepareRequest<K, V> extends GridDistributedTxPrepareRequest<K, V> {
    /** */
    private static final long serialVersionUID = 0L;

    /** Max order. */
    private UUID nearNodeId;

    /** Future ID. */
    private IgniteUuid futId;

    /** Mini future ID. */
    private IgniteUuid miniId;

    /** Topology version. */
    private long topVer;

    /** Invalidate near entries flags. */
    private BitSet invalidateNearEntries;

    /** Near writes. */
    @GridToStringInclude
    @GridDirectTransient
    private Collection<IgniteTxEntry<K, V>> nearWrites;

    /** Serialized near writes. */
    @GridDirectCollection(byte[].class)
    private Collection<byte[]> nearWritesBytes;

    /** Owned versions by key. */
    @GridToStringInclude
    @GridDirectTransient
    private Map<IgniteTxKey<K>, GridCacheVersion> owned;

    /** Owned versions bytes. */
    private byte[] ownedBytes;

    /** Near transaction ID. */
    private GridCacheVersion nearXidVer;

    /** {@code True} if this is last prepare request for node. */
    private boolean last;

    /** Subject ID. */
    private UUID subjId;

    /** Task name hash. */
    private int taskNameHash;

    /** Preload keys. */
    private BitSet preloadKeys;

    /**
     * Empty constructor required for {@link Externalizable}.
     */
    public GridDhtTxPrepareRequest() {
        // No-op.
    }

    /**
     * @param futId Future ID.
     * @param miniId Mini future ID.
     * @param topVer Topology version.
     * @param tx Transaction.
     * @param dhtWrites DHT writes.
     * @param nearWrites Near writes.
     * @param grpLockKey Group lock key if preparing group-lock transaction.
     * @param partLock {@code True} if group-lock transaction locks partition.
     * @param txNodes Transaction nodes mapping.
     * @param nearXidVer Near transaction ID.
     * @param last {@code True} if this is last prepare request for node.
     * @param onePhaseCommit One phase commit flag.
     */
    public GridDhtTxPrepareRequest(
        IgniteUuid futId,
        IgniteUuid miniId,
        long topVer,
        GridDhtTxLocalAdapter<K, V> tx,
        Collection<IgniteTxEntry<K, V>> dhtWrites,
        Collection<IgniteTxEntry<K, V>> nearWrites,
        IgniteTxKey grpLockKey,
        boolean partLock,
        Map<UUID, Collection<UUID>> txNodes,
        GridCacheVersion nearXidVer,
        boolean last,
        boolean onePhaseCommit,
        UUID subjId,
        int taskNameHash) {
        super(tx, null, dhtWrites, grpLockKey, partLock, txNodes, onePhaseCommit);

        assert futId != null;
        assert miniId != null;

        this.topVer = topVer;
        this.futId = futId;
        this.nearWrites = nearWrites;
        this.miniId = miniId;
        this.nearXidVer = nearXidVer;
        this.last = last;
        this.subjId = subjId;
        this.taskNameHash = taskNameHash;

        invalidateNearEntries = new BitSet(dhtWrites == null ? 0 : dhtWrites.size());

        nearNodeId = tx.nearNodeId();
    }

    /**
     * @return {@code True} if this is last prepare request for node.
     */
    public boolean last() {
        return last;
    }

    /**
     * @return Near transaction ID.
     */
    public GridCacheVersion nearXidVersion() {
        return nearXidVer;
    }

    /** {@inheritDoc} */
    @Override public boolean allowForStartup() {
        return true;
    }

    /**
     * @return Near node ID.
     */
    public UUID nearNodeId() {
        return nearNodeId;
    }

    /**
     * @return Subject ID.
     */
    @Nullable public UUID subjectId() {
        return subjId;
    }

    /**
     * @return Task name hash.
     */
    public int taskNameHash() {
        return taskNameHash;
    }

    /**
     * @return Near writes.
     */
    public Collection<IgniteTxEntry<K, V>> nearWrites() {
        return nearWrites == null ? Collections.<IgniteTxEntry<K, V>>emptyList() : nearWrites;
    }

    /**
     * @param idx Entry index to set invalidation flag.
     * @param invalidate Invalidation flag value.
     */
    public void invalidateNearEntry(int idx, boolean invalidate) {
        invalidateNearEntries.set(idx, invalidate);
    }

    /**
     * @param idx Index to get invalidation flag value.
     * @return Invalidation flag value.
     */
    public boolean invalidateNearEntry(int idx) {
        return invalidateNearEntries.get(idx);
    }

    /**
     * Marks last added key for preloading.
     */
    public void markKeyForPreload(int idx) {
        if (preloadKeys == null)
            preloadKeys = new BitSet();

        preloadKeys.set(idx, true);
    }

    /**
     * Checks whether entry info should be sent to primary node from backup.
     *
     * @param idx Index.
     * @return {@code True} if value should be sent, {@code false} otherwise.
     */
    public boolean needPreloadKey(int idx) {
        return preloadKeys != null && preloadKeys.get(idx);
    }

    /**
     * @return Future ID.
     */
    public IgniteUuid futureId() {
        return futId;
    }

    /**
     * @return Mini future ID.
     */
    public IgniteUuid miniId() {
        return miniId;
    }

    /**
     * @return Topology version.
     */
    @Override public long topologyVersion() {
        return topVer;
    }

    /**
     * Sets owner and its mapped version.
     *
     * @param key Key.
     * @param ownerMapped Owner mapped version.
     */
    public void owned(IgniteTxKey<K> key, GridCacheVersion ownerMapped) {
        if (owned == null)
            owned = new GridLeanMap<>(3);

        owned.put(key, ownerMapped);
    }

    /**
     * @return Owned versions map.
     */
    public Map<IgniteTxKey<K>, GridCacheVersion> owned() {
        return owned;
    }

    /** {@inheritDoc}
     * @param ctx*/
    @Override public void prepareMarshal(GridCacheSharedContext<K, V> ctx) throws IgniteCheckedException {
        super.prepareMarshal(ctx);

        if (ownedBytes == null && owned != null) {
            ownedBytes = CU.marshal(ctx, owned);

            if (ctx.deploymentEnabled()) {
                for (IgniteTxKey<K> k : owned.keySet())
                    prepareObject(k, ctx);
            }
        }

        if (nearWrites != null) {
            marshalTx(nearWrites, ctx);

            nearWritesBytes = new ArrayList<>(nearWrites.size());

            for (IgniteTxEntry<K, V> e : nearWrites)
                nearWritesBytes.add(ctx.marshaller().marshal(e));
        }
    }

    /** {@inheritDoc} */
    @Override public void finishUnmarshal(GridCacheSharedContext<K, V> ctx, ClassLoader ldr) throws IgniteCheckedException {
        super.finishUnmarshal(ctx, ldr);

        if (ownedBytes != null && owned == null)
            owned = ctx.marshaller().unmarshal(ownedBytes, ldr);

        if (nearWritesBytes != null) {
            nearWrites = new ArrayList<>(nearWritesBytes.size());

            for (byte[] arr : nearWritesBytes)
                nearWrites.add(ctx.marshaller().<IgniteTxEntry<K, V>>unmarshal(arr, ldr));

            unmarshalTx(nearWrites, true, ctx, ldr);
        }
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return S.toString(GridDhtTxPrepareRequest.class, this, "super", super.toString());
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
            case 23:
                if (!writer.writeIgniteUuid("futId", futId))
                    return false;

                writer.incrementState();

            case 24:
                if (!writer.writeBitSet("invalidateNearEntries", invalidateNearEntries))
                    return false;

                writer.incrementState();

            case 25:
                if (!writer.writeBoolean("last", last))
                    return false;

                writer.incrementState();

            case 26:
                if (!writer.writeIgniteUuid("miniId", miniId))
                    return false;

                writer.incrementState();

            case 27:
                if (!writer.writeUuid("nearNodeId", nearNodeId))
                    return false;

                writer.incrementState();

            case 28:
                if (!writer.writeCollection("nearWritesBytes", nearWritesBytes, MessageCollectionItemType.BYTE_ARR))
                    return false;

                writer.incrementState();

            case 29:
                if (!writer.writeMessage("nearXidVer", nearXidVer))
                    return false;

                writer.incrementState();

            case 30:
                if (!writer.writeByteArray("ownedBytes", ownedBytes))
                    return false;

                writer.incrementState();

            case 31:
                if (!writer.writeBitSet("preloadKeys", preloadKeys))
                    return false;

                writer.incrementState();

            case 32:
                if (!writer.writeUuid("subjId", subjId))
                    return false;

                writer.incrementState();

            case 33:
                if (!writer.writeInt("taskNameHash", taskNameHash))
                    return false;

                writer.incrementState();

            case 34:
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
            case 23:
                futId = reader.readIgniteUuid("futId");

                if (!reader.isLastRead())
                    return false;

                reader.incrementState();

            case 24:
                invalidateNearEntries = reader.readBitSet("invalidateNearEntries");

                if (!reader.isLastRead())
                    return false;

                reader.incrementState();

            case 25:
                last = reader.readBoolean("last");

                if (!reader.isLastRead())
                    return false;

                reader.incrementState();

            case 26:
                miniId = reader.readIgniteUuid("miniId");

                if (!reader.isLastRead())
                    return false;

                reader.incrementState();

            case 27:
                nearNodeId = reader.readUuid("nearNodeId");

                if (!reader.isLastRead())
                    return false;

                reader.incrementState();

            case 28:
                nearWritesBytes = reader.readCollection("nearWritesBytes", MessageCollectionItemType.BYTE_ARR);

                if (!reader.isLastRead())
                    return false;

                reader.incrementState();

            case 29:
                nearXidVer = reader.readMessage("nearXidVer");

                if (!reader.isLastRead())
                    return false;

                reader.incrementState();

            case 30:
                ownedBytes = reader.readByteArray("ownedBytes");

                if (!reader.isLastRead())
                    return false;

                reader.incrementState();

            case 31:
                preloadKeys = reader.readBitSet("preloadKeys");

                if (!reader.isLastRead())
                    return false;

                reader.incrementState();

            case 32:
                subjId = reader.readUuid("subjId");

                if (!reader.isLastRead())
                    return false;

                reader.incrementState();

            case 33:
                taskNameHash = reader.readInt("taskNameHash");

                if (!reader.isLastRead())
                    return false;

                reader.incrementState();

            case 34:
                topVer = reader.readLong("topVer");

                if (!reader.isLastRead())
                    return false;

                reader.incrementState();

        }

        return true;
    }

    /** {@inheritDoc} */
    @Override public byte directType() {
        return 34;
    }

    /** {@inheritDoc} */
    @Override public byte fieldsCount() {
        return 35;
    }
}
