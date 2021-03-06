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

package org.apache.ignite.compute;

import org.apache.ignite.*;
import org.jetbrains.annotations.*;

/**
 * This exception defines execution rejection. This exception is used to indicate
 * the situation when execution service provided by the user in configuration
 * rejects execution.
 * @see org.apache.ignite.configuration.IgniteConfiguration#getExecutorService()
 */
public class ComputeExecutionRejectedException extends IgniteCheckedException {
    /** */
    private static final long serialVersionUID = 0L;

    /**
     * Creates new execution rejection exception with given error message.
     *
     * @param msg Error message.
     */
    public ComputeExecutionRejectedException(String msg) {
        super(msg);
    }

    /**
     * Creates new execution rejection given throwable as a cause and
     * source of error message.
     *
     * @param cause Non-null throwable cause.
     */
    public ComputeExecutionRejectedException(Throwable cause) {
        this(cause.getMessage(), cause);
    }

    /**
     * Creates new execution rejection exception with given error message
     * and optional nested exception.
     *
     * @param msg Error message.
     * @param cause Optional nested exception (can be {@code null}).
     */
    public ComputeExecutionRejectedException(String msg, @Nullable Throwable cause) {
        super(msg, cause);
    }
}
