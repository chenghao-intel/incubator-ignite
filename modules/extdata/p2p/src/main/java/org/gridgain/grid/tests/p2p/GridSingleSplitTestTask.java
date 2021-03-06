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

package org.gridgain.grid.tests.p2p;

import org.apache.ignite.*;
import org.apache.ignite.compute.*;
import org.gridgain.grid.*;

import java.io.*;
import java.util.*;

/**
 * Test task for P2P deployment tests.
 */
public class GridSingleSplitTestTask extends ComputeTaskSplitAdapter<Integer, Integer> {
    /**
     * {@inheritDoc}
     */
    @Override protected Collection<? extends ComputeJob> split(int gridSize, Integer arg) throws IgniteCheckedException {
        assert gridSize > 0 : "Subgrid cannot be empty.";

        Collection<ComputeJobAdapter> jobs = new ArrayList<>(gridSize);

        for (int i = 0; i < arg; i++)
            jobs.add(new GridSingleSplitTestJob(1));

        return jobs;
    }

    /**
     * {@inheritDoc}
     */
    @Override public Integer reduce(List<ComputeJobResult> results) throws IgniteCheckedException {
        int retVal = 0;

        for (ComputeJobResult res : results) {
            assert res.getException() == null : "Load test jobs can never fail: " + res;

            retVal += (Integer)res.getData();
        }

        return retVal;
    }

    /**
     * Test job for P2P deployment tests.
     */
    @SuppressWarnings("PublicInnerClass")
    public static final class GridSingleSplitTestJob extends ComputeJobAdapter {
        /**
         * @param args Job arguments.
         */
        public GridSingleSplitTestJob(Integer args) {
            super(args);
        }

        /** {@inheritDoc} */
        @Override public Serializable execute() {
            return new GridSingleSplitTestJobTarget().executeLoadTestJob((Integer)argument(0));
        }
    }
}
