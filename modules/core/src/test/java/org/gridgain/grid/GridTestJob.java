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

package org.gridgain.grid;

import org.apache.ignite.*;
import org.apache.ignite.compute.*;
import org.apache.ignite.resources.*;

/**
 * Test job.
 */
public class GridTestJob extends ComputeJobAdapter {
    /** Logger. */
    @IgniteLoggerResource
    private IgniteLogger log;

    /** */
    public GridTestJob() {
        // No-op.
    }

    /**
     * @param arg Job argument.
     */
    public GridTestJob(String arg) {
        super(arg);
    }

    /** {@inheritDoc} */
    @Override public String execute() throws IgniteCheckedException {
        if (log.isDebugEnabled())
            log.debug("Executing job [job=" + this + ", arg=" + argument(0) + ']');

        return argument(0);
    }
}
