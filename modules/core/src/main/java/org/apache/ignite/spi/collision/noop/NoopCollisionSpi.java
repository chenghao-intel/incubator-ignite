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

package org.apache.ignite.spi.collision.noop;

import org.apache.ignite.spi.*;
import org.apache.ignite.spi.collision.*;
import org.gridgain.grid.util.typedef.internal.*;
import org.jetbrains.annotations.*;

/**
 * No-op implementation of {@link org.apache.ignite.spi.collision.CollisionSpi}. This is default implementation
 * since {@code 4.5.0} version. When grid is started with {@link NoopCollisionSpi}
 * jobs are activated immediately on arrival to mapped node. This approach suits well
 * for large amount of small jobs (which is a wide-spread use case). User still can
 * control the number of concurrent jobs by setting maximum thread pool size defined
 * by {@link org.apache.ignite.configuration.IgniteConfiguration#getExecutorService()} configuration property.
 */
@IgniteSpiNoop
@IgniteSpiMultipleInstancesSupport(true)
public class NoopCollisionSpi extends IgniteSpiAdapter implements CollisionSpi {
    /** {@inheritDoc} */
    @Override public void spiStart(@Nullable String gridName) throws IgniteSpiException {
        // No-op.
    }

    /** {@inheritDoc} */
    @Override public void spiStop() throws IgniteSpiException {
        // No-op.
    }

    /** {@inheritDoc} */
    @Override public void onCollision(CollisionContext ctx) {
        // No-op.
    }

    /** {@inheritDoc} */
    @Override public void setExternalCollisionListener(@Nullable CollisionExternalListener lsnr) {
        // No-op.
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return S.toString(NoopCollisionSpi.class, this);
    }
}
