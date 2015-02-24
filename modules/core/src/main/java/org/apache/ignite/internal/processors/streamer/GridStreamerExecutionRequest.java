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

package org.apache.ignite.internal.processors.streamer;

import org.apache.ignite.configuration.*;
import org.apache.ignite.internal.*;
import org.apache.ignite.internal.util.tostring.*;
import org.apache.ignite.internal.util.typedef.internal.*;
import org.apache.ignite.lang.*;
import org.apache.ignite.plugin.extensions.communication.*;
import org.jetbrains.annotations.*;

import java.nio.*;
import java.util.*;

/**
 *
 */
public class GridStreamerExecutionRequest implements Message {
    /** */
    private static final long serialVersionUID = 0L;

    /** Force local deployment flag. */
    private boolean forceLocDep;

    /** Serialized batch in case if P2P class loading is enabled. */
    @GridToStringExclude
    private byte[] batchBytes;

    /** Deployment mode. */
    private DeploymentMode depMode;

    /** Deployment sample class name. */
    private String sampleClsName;

    /** Deployment user version. */
    private String userVer;

    /** Node class loader participants. */
    @GridToStringInclude
    @GridDirectMap(keyType = UUID.class, valueType = IgniteUuid.class)
    private Map<UUID, IgniteUuid> ldrParticipants;

    /** Class loader ID. */
    private IgniteUuid clsLdrId;

    /**
     *
     */
    public GridStreamerExecutionRequest() {
        // No-op.
    }

    /**
     * @param forceLocDep Force local deployment flag.
     * @param batchBytes Batch serialized bytes.
     * @param depMode Deployment mode.
     * @param sampleClsName Sample class name.
     * @param userVer User version.
     * @param ldrParticipants Loader participants.
     * @param clsLdrId Class loader ID.
     */
    public GridStreamerExecutionRequest(
        boolean forceLocDep,
        byte[] batchBytes,
        @Nullable DeploymentMode depMode,
        @Nullable String sampleClsName,
        @Nullable String userVer,
        @Nullable Map<UUID, IgniteUuid> ldrParticipants,
        @Nullable IgniteUuid clsLdrId
    ) {
        assert batchBytes != null;

        this.forceLocDep = forceLocDep;
        this.batchBytes = batchBytes;
        this.depMode = depMode;
        this.sampleClsName = sampleClsName;
        this.userVer = userVer;
        this.ldrParticipants = ldrParticipants;
        this.clsLdrId = clsLdrId;
    }

    /**
     * @return Force local deployment flag.
     */
    public boolean forceLocalDeployment() {
        return forceLocDep;
    }

    /**
     * @return Deployment mode.
     */
    public DeploymentMode deploymentMode() {
        return depMode;
    }

    /**
     * @return Deployment sample class name.
     */
    public String sampleClassName() {
        return sampleClsName;
    }

    /**
     * @return Deployment user version.
     */
    public String userVersion() {
        return userVer;
    }

    /**
     * @return Node class loader participants.
     */
    public Map<UUID, IgniteUuid> loaderParticipants() {
        return ldrParticipants;
    }

    /**
     * @return Class loader ID.
     */
    public IgniteUuid classLoaderId() {
        return clsLdrId;
    }

    /**
     * @return Serialized batch in case if P2P class loading is enabled.
     */
    public byte[] batchBytes() {
        return batchBytes;
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return S.toString(GridStreamerExecutionRequest.class, this);
    }

    /** {@inheritDoc} */
    @Override public boolean writeTo(ByteBuffer buf, MessageWriter writer) {
        writer.setBuffer(buf);

        if (!writer.isHeaderWritten()) {
            if (!writer.writeHeader(directType(), fieldsCount()))
                return false;

            writer.onHeaderWritten();
        }

        switch (writer.state()) {
            case 0:
                if (!writer.writeByteArray("batchBytes", batchBytes))
                    return false;

                writer.incrementState();

            case 1:
                if (!writer.writeIgniteUuid("clsLdrId", clsLdrId))
                    return false;

                writer.incrementState();

            case 2:
                if (!writer.writeByte("depMode", depMode != null ? (byte)depMode.ordinal() : -1))
                    return false;

                writer.incrementState();

            case 3:
                if (!writer.writeBoolean("forceLocDep", forceLocDep))
                    return false;

                writer.incrementState();

            case 4:
                if (!writer.writeMap("ldrParticipants", ldrParticipants, MessageCollectionItemType.UUID, MessageCollectionItemType.IGNITE_UUID))
                    return false;

                writer.incrementState();

            case 5:
                if (!writer.writeString("sampleClsName", sampleClsName))
                    return false;

                writer.incrementState();

            case 6:
                if (!writer.writeString("userVer", userVer))
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

        switch (reader.state()) {
            case 0:
                batchBytes = reader.readByteArray("batchBytes");

                if (!reader.isLastRead())
                    return false;

                reader.incrementState();

            case 1:
                clsLdrId = reader.readIgniteUuid("clsLdrId");

                if (!reader.isLastRead())
                    return false;

                reader.incrementState();

            case 2:
                byte depModeOrd;

                depModeOrd = reader.readByte("depMode");

                if (!reader.isLastRead())
                    return false;

                depMode = DeploymentMode.fromOrdinal(depModeOrd);

                reader.incrementState();

            case 3:
                forceLocDep = reader.readBoolean("forceLocDep");

                if (!reader.isLastRead())
                    return false;

                reader.incrementState();

            case 4:
                ldrParticipants = reader.readMap("ldrParticipants", MessageCollectionItemType.UUID, MessageCollectionItemType.IGNITE_UUID, false);

                if (!reader.isLastRead())
                    return false;

                reader.incrementState();

            case 5:
                sampleClsName = reader.readString("sampleClsName");

                if (!reader.isLastRead())
                    return false;

                reader.incrementState();

            case 6:
                userVer = reader.readString("userVer");

                if (!reader.isLastRead())
                    return false;

                reader.incrementState();

        }

        return true;
    }

    /** {@inheritDoc} */
    @Override public byte directType() {
        return 80;
    }

    /** {@inheritDoc} */
    @Override public byte fieldsCount() {
        return 7;
    }
}
