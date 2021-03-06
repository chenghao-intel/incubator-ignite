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

package org.gridgain.grid.kernal.visor.ggfs;

import org.apache.ignite.*;
import org.apache.ignite.fs.*;
import org.gridgain.grid.*;
import org.gridgain.grid.util.typedef.internal.*;

import java.io.*;

/**
 * Data transfer object for {@link org.apache.ignite.IgniteFs}.
 */
public class VisorGgfs implements Serializable {
    /** */
    private static final long serialVersionUID = 0L;

    /** GGFS instance name. */
    private final String name;

    /** GGFS instance working mode. */
    private final IgniteFsMode mode;

    /** GGFS metrics. */
    private final VisorGgfsMetrics metrics;

    /** Whether GGFS has configured secondary file system. */
    private final boolean secondaryFsConfigured;

    /**
     * Create data transfer object.
     *
     * @param name GGFS name.
     * @param mode GGFS mode.
     * @param metrics GGFS metrics.
     * @param secondaryFsConfigured Whether GGFS has configured secondary file system.
     */
    public VisorGgfs(
        String name,
        IgniteFsMode mode,
        VisorGgfsMetrics metrics,
        boolean secondaryFsConfigured
    ) {
        this.name = name;
        this.mode = mode;
        this.metrics = metrics;
        this.secondaryFsConfigured = secondaryFsConfigured;
    }

    /**
     * @param ggfs Source GGFS.
     * @return Data transfer object for given GGFS.
     * @throws IgniteCheckedException
     */
    public static VisorGgfs from(IgniteFs ggfs) throws IgniteCheckedException {
        assert ggfs != null;

        return new VisorGgfs(
            ggfs.name(),
            ggfs.configuration().getDefaultMode(),
            VisorGgfsMetrics.from(ggfs.metrics()),
            ggfs.configuration().getSecondaryFileSystem() != null
        );
    }

    /**
     * @return GGFS instance name.
     */
    public String name() {
        return name;
    }

    /**
     * @return GGFS instance working mode.
     */
    public IgniteFsMode mode() {
        return mode;
    }

    /**
     * @return GGFS metrics.
     */
    public VisorGgfsMetrics metrics() {
        return metrics;
    }

    /**
     * @return Whether GGFS has configured secondary file system.
     */
    public boolean secondaryFileSystemConfigured() {
        return secondaryFsConfigured;
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return S.toString(VisorGgfs.class, this);
    }
}
