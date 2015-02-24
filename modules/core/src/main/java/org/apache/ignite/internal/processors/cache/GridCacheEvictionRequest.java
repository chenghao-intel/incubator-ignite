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

package org.apache.ignite.internal.processors.cache;

import org.apache.ignite.*;
import org.apache.ignite.internal.*;
import org.apache.ignite.internal.processors.cache.version.*;
import org.apache.ignite.internal.util.lang.*;
import org.apache.ignite.internal.util.tostring.*;
import org.apache.ignite.internal.util.typedef.*;
import org.apache.ignite.internal.util.typedef.internal.*;
import org.apache.ignite.plugin.extensions.communication.*;

import java.io.*;
import java.nio.*;
import java.util.*;

/**
 * Cache eviction request.
 */
public class GridCacheEvictionRequest<K, V> extends GridCacheMessage<K, V> implements GridCacheDeployable {
    /** */
    private static final long serialVersionUID = 0L;

    /** Future id. */
    private long futId;

    /** Entries to clear from near and backup nodes. */
    @GridToStringInclude
    @GridDirectTransient
    private Collection<GridTuple3<K, GridCacheVersion, Boolean>> entries;

    /** Serialized entries. */
    @GridToStringExclude
    private byte[] entriesBytes;

    /** Topology version. */
    private long topVer;

    /**
     * Required by {@link Externalizable}.
     */
    public GridCacheEvictionRequest() {
        // No-op.
    }

    /**
     * @param cacheId Cache ID.
     * @param futId Future id.
     * @param size Size.
     * @param topVer Topology version.
     */
    GridCacheEvictionRequest(int cacheId, long futId, int size, long topVer) {
        assert futId > 0;
        assert size > 0;
        assert topVer > 0;

        this.cacheId = cacheId;
        this.futId = futId;

        entries = new ArrayList<>(size);

        this.topVer = topVer;
    }

    /** {@inheritDoc}
     * @param ctx*/
    @Override public void prepareMarshal(GridCacheSharedContext<K, V> ctx) throws IgniteCheckedException {
        super.prepareMarshal(ctx);

        if (entries != null) {
            if (ctx.deploymentEnabled())
                prepareObjects(entries, ctx);

            entriesBytes = ctx.marshaller().marshal(entries);
        }
    }

    /** {@inheritDoc} */
    @Override public void finishUnmarshal(GridCacheSharedContext<K, V> ctx, ClassLoader ldr) throws IgniteCheckedException {
        super.finishUnmarshal(ctx, ldr);

        if (entriesBytes != null)
            entries = ctx.marshaller().unmarshal(entriesBytes, ldr);
    }

    /**
     * @return Future id.
     */
    long futureId() {
        return futId;
    }

    /**
     * @return Entries - {{Key, Version, Boolean (near or not)}, ...}.
     */
    Collection<GridTuple3<K, GridCacheVersion, Boolean>> entries() {
        return entries;
    }

    /**
     * @return Topology version.
     */
    @Override public long topologyVersion() {
        return topVer;
    }

    /**
     * Add key to request.
     *
     * @param key Key to evict.
     * @param ver Entry version.
     * @param near {@code true} if key should be evicted from near cache.
     */
    void addKey(K key, GridCacheVersion ver, boolean near) {
        assert key != null;
        assert ver != null;

        entries.add(F.t(key, ver, near));
    }

    /** {@inheritDoc} */
    @Override public boolean ignoreClassErrors() {
        return true;
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
            case 3:
                if (!writer.writeByteArray("entriesBytes", entriesBytes))
                    return false;

                writer.incrementState();

            case 4:
                if (!writer.writeLong("futId", futId))
                    return false;

                writer.incrementState();

            case 5:
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
            case 3:
                entriesBytes = reader.readByteArray("entriesBytes");

                if (!reader.isLastRead())
                    return false;

                reader.incrementState();

            case 4:
                futId = reader.readLong("futId");

                if (!reader.isLastRead())
                    return false;

                reader.incrementState();

            case 5:
                topVer = reader.readLong("topVer");

                if (!reader.isLastRead())
                    return false;

                reader.incrementState();

        }

        return true;
    }

    /** {@inheritDoc} */
    @Override public byte directType() {
        return 14;
    }

    /** {@inheritDoc} */
    @Override public byte fieldsCount() {
        return 6;
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return S.toString(GridCacheEvictionRequest.class, this);
    }
}
