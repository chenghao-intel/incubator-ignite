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

package org.gridgain.grid.kernal.processors.cache;

import org.apache.ignite.lang.*;
import org.gridgain.grid.cache.*;
import org.gridgain.grid.kernal.*;
import org.gridgain.grid.util.typedef.internal.*;

import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import static org.gridgain.grid.cache.GridCacheMode.*;

/**
 * Check ttl manager for memory leak.
 */
public class GridCacheTtlManagerLoadTest extends GridCacheTtlManagerSelfTest {
    /**
     * @throws Exception If failed.
     */
    public void testLoad() throws Exception {
        cacheMode = REPLICATED;

        final GridKernal g = (GridKernal)startGrid(0);

        try {
            final AtomicBoolean stop = new AtomicBoolean();

            IgniteFuture<?> fut = multithreadedAsync(new Callable<Object>() {
                @Override public Object call() throws Exception {
                    GridCache<Object,Object> cache = g.cache(null);

                    long key = 0;

                    while (!stop.get()) {
                        GridCacheEntry<Object, Object> entry = cache.entry(key);

                        entry.timeToLive(1000);
                        entry.setValue(key);

                        key++;
                    }

                    return null;
                }
            }, 1);

            GridCacheTtlManager<Object, Object> ttlMgr = g.internalCache().context().ttl();

            for (int i = 0; i < 300; i++) {
                U.sleep(1000);

                ttlMgr.printMemoryStats();
            }

            stop.set(true);

            fut.get();
        }
        finally {
            stopAllGrids();
        }
    }

    /** {@inheritDoc} */
    @Override protected long getTestTimeout() {
        return Long.MAX_VALUE;
    }
}
