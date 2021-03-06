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

package org.gridgain.grid.kernal.processors.ggfs;

import org.apache.ignite.*;
import org.apache.ignite.compute.*;
import org.apache.ignite.fs.*;
import org.apache.ignite.fs.mapreduce.*;
import org.gridgain.grid.kernal.*;
import org.gridgain.grid.util.ipc.*;
import org.gridgain.grid.util.typedef.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * Nop GGFS processor implementation.
 */
public class GridNoopGgfsProcessor extends GridGgfsProcessorAdapter {
    /**
     * Constructor.
     *
     * @param ctx Kernal context.
     */
    public GridNoopGgfsProcessor(GridKernalContext ctx) {
        super(ctx);
    }

    /** {@inheritDoc} */
    @Override public void printMemoryStats() {
        X.println(">>>");
        X.println(">>> GGFS processor memory stats [grid=" + ctx.gridName() + ']');
        X.println(">>>   ggfsCacheSize: " + 0);
    }

    /** {@inheritDoc} */
    @Override public Collection<IgniteFs> ggfss() {
        return Collections.emptyList();
    }

    /** {@inheritDoc} */
    @Nullable @Override public IgniteFs ggfs(@Nullable String name) {
        return null;
    }

    /** {@inheritDoc} */
    @Override public Collection<GridIpcServerEndpoint> endpoints(@Nullable String name) {
        return Collections.emptyList();
    }

    /** {@inheritDoc} */
    @Nullable @Override public ComputeJob createJob(IgniteFsJob job, @Nullable String ggfsName, IgniteFsPath path,
        long start, long length, IgniteFsRecordResolver recRslv) {
        return null;
    }
}
