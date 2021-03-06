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

package org.gridgain.grid.kernal.processors.cache.query;

import org.apache.ignite.*;
import org.gridgain.grid.kernal.*;
import org.gridgain.grid.kernal.processors.cache.*;
import org.gridgain.grid.kernal.processors.query.*;
import org.gridgain.grid.util.direct.*;
import org.gridgain.grid.util.tostring.*;
import org.gridgain.grid.util.typedef.*;
import org.gridgain.grid.util.typedef.internal.*;
import org.jetbrains.annotations.*;

import java.io.*;
import java.nio.*;
import java.util.*;

/**
 * Query request.
 */
public class GridCacheQueryResponse<K, V> extends GridCacheMessage<K, V> implements GridCacheDeployable {
    /** */
    private static final long serialVersionUID = 0L;

    /** */
    private boolean finished;

    /** */
    private long reqId;

    /** */
    @GridDirectTransient
    private Throwable err;

    /** */
    private byte[] errBytes;

    /** */
    private boolean fields;

    /** */
    @GridDirectCollection(byte[].class)
    private Collection<byte[]> metaDataBytes;

    /** */
    @GridToStringInclude
    @GridDirectTransient
    private List<GridQueryFieldMetadata> metadata;

    /** */
    @GridDirectCollection(byte[].class)
    private Collection<byte[]> dataBytes;

    /** */
    @GridDirectTransient
    private Collection<Object> data;

    /**
     * Empty constructor for {@link Externalizable}
     */
    public GridCacheQueryResponse() {
        //No-op.
    }

    /**
     * @param cacheId Cache ID.
     * @param reqId Request id.
     * @param finished Last response or not.
     * @param fields Fields query or not.
     */
    public GridCacheQueryResponse(int cacheId, long reqId, boolean finished, boolean fields) {
        this.cacheId = cacheId;
        this.reqId = reqId;
        this.finished = finished;
        this.fields = fields;
    }

    /**
     * @param cacheId Cache ID.
     * @param reqId Request id.
     * @param err Error.
     */
    public GridCacheQueryResponse(int cacheId, long reqId, Throwable err) {
        this.cacheId = cacheId;
        this.reqId = reqId;
        this.err = err;
        finished = true;
    }

    /** {@inheritDoc}
     * @param ctx*/
    @Override public void prepareMarshal(GridCacheSharedContext<K, V> ctx) throws IgniteCheckedException {
        super.prepareMarshal(ctx);

        if (err != null)
            errBytes = ctx.marshaller().marshal(err);

        metaDataBytes = marshalCollection(metadata, ctx);
        dataBytes = marshalCollection(data, ctx);

        if (ctx.deploymentEnabled() && !F.isEmpty(data)) {
            for (Object o : data) {
                if (o instanceof Map.Entry) {
                    Map.Entry e = (Map.Entry)o;

                    prepareObject(e.getKey(), ctx);
                    prepareObject(e.getValue(), ctx);
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override public void finishUnmarshal(GridCacheSharedContext<K, V> ctx, ClassLoader ldr) throws IgniteCheckedException {
        super.finishUnmarshal(ctx, ldr);

        if (errBytes != null)
            err = ctx.marshaller().unmarshal(errBytes, ldr);

        metadata = unmarshalCollection(metaDataBytes, ctx, ldr);
        data = unmarshalCollection(dataBytes, ctx, ldr);
    }

    /**
     * @return Metadata.
     */
    public List<GridQueryFieldMetadata> metadata() {
        return metadata;
    }

    /**
     * @param metadata Metadata.
     */
    public void metadata(@Nullable List<GridQueryFieldMetadata> metadata) {
        this.metadata = metadata;
    }

    /**
     * @return Query data.
     */
    public Collection<Object> data() {
        return data;
    }

    /**
     * @param data Query data.
     */
    @SuppressWarnings("unchecked")
    public void data(Collection<?> data) {
        this.data = (Collection<Object>)data;
    }

    /**
     * @return If this is last response for this request or not.
     */
    public boolean isFinished() {
        return finished;
    }

    /**
     * @param finished If this is last response for this request or not.
     */
    public void finished(boolean finished) {
        this.finished = finished;
    }

    /**
     * @return Request id.
     */
    public long requestId() {
        return reqId;
    }

    /**
     * @return Error.
     */
    public Throwable error() {
        return err;
    }

    /**
     * @return If fields query.
     */
    public boolean fields() {
        return fields;
    }

    /** {@inheritDoc} */
    @SuppressWarnings({"CloneDoesntCallSuperClone", "CloneCallsConstructors"})
    @Override public GridTcpCommunicationMessageAdapter clone() {
        GridCacheQueryResponse _clone = new GridCacheQueryResponse();

        clone0(_clone);

        return _clone;
    }

    /** {@inheritDoc} */
    @Override protected void clone0(GridTcpCommunicationMessageAdapter _msg) {
        super.clone0(_msg);

        GridCacheQueryResponse _clone = (GridCacheQueryResponse)_msg;

        _clone.finished = finished;
        _clone.reqId = reqId;
        _clone.err = err;
        _clone.errBytes = errBytes;
        _clone.fields = fields;
        _clone.metaDataBytes = metaDataBytes;
        _clone.metadata = metadata;
        _clone.dataBytes = dataBytes;
        _clone.data = data;
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
                if (dataBytes != null) {
                    if (commState.it == null) {
                        if (!commState.putInt(dataBytes.size()))
                            return false;

                        commState.it = dataBytes.iterator();
                    }

                    while (commState.it.hasNext() || commState.cur != NULL) {
                        if (commState.cur == NULL)
                            commState.cur = commState.it.next();

                        if (!commState.putByteArray((byte[])commState.cur))
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
                if (!commState.putByteArray(errBytes))
                    return false;

                commState.idx++;

            case 5:
                if (!commState.putBoolean(fields))
                    return false;

                commState.idx++;

            case 6:
                if (!commState.putBoolean(finished))
                    return false;

                commState.idx++;

            case 7:
                if (metaDataBytes != null) {
                    if (commState.it == null) {
                        if (!commState.putInt(metaDataBytes.size()))
                            return false;

                        commState.it = metaDataBytes.iterator();
                    }

                    while (commState.it.hasNext() || commState.cur != NULL) {
                        if (commState.cur == NULL)
                            commState.cur = commState.it.next();

                        if (!commState.putByteArray((byte[])commState.cur))
                            return false;

                        commState.cur = NULL;
                    }

                    commState.it = null;
                } else {
                    if (!commState.putInt(-1))
                        return false;
                }

                commState.idx++;

            case 8:
                if (!commState.putLong(reqId))
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
                    if (dataBytes == null)
                        dataBytes = new ArrayList<>(commState.readSize);

                    for (int i = commState.readItems; i < commState.readSize; i++) {
                        byte[] _val = commState.getByteArray();

                        if (_val == BYTE_ARR_NOT_READ)
                            return false;

                        dataBytes.add((byte[])_val);

                        commState.readItems++;
                    }
                }

                commState.readSize = -1;
                commState.readItems = 0;

                commState.idx++;

            case 4:
                byte[] errBytes0 = commState.getByteArray();

                if (errBytes0 == BYTE_ARR_NOT_READ)
                    return false;

                errBytes = errBytes0;

                commState.idx++;

            case 5:
                if (buf.remaining() < 1)
                    return false;

                fields = commState.getBoolean();

                commState.idx++;

            case 6:
                if (buf.remaining() < 1)
                    return false;

                finished = commState.getBoolean();

                commState.idx++;

            case 7:
                if (commState.readSize == -1) {
                    if (buf.remaining() < 4)
                        return false;

                    commState.readSize = commState.getInt();
                }

                if (commState.readSize >= 0) {
                    if (metaDataBytes == null)
                        metaDataBytes = new ArrayList<>(commState.readSize);

                    for (int i = commState.readItems; i < commState.readSize; i++) {
                        byte[] _val = commState.getByteArray();

                        if (_val == BYTE_ARR_NOT_READ)
                            return false;

                        metaDataBytes.add((byte[])_val);

                        commState.readItems++;
                    }
                }

                commState.readSize = -1;
                commState.readItems = 0;

                commState.idx++;

            case 8:
                if (buf.remaining() < 8)
                    return false;

                reqId = commState.getLong();

                commState.idx++;

        }

        return true;
    }

    /** {@inheritDoc} */
    @Override public byte directType() {
        return 58;
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return S.toString(GridCacheQueryResponse.class, this);
    }
}
