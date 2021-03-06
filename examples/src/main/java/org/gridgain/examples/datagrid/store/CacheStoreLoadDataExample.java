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

package org.gridgain.examples.datagrid.store;

import org.apache.ignite.*;
import org.apache.ignite.lang.*;
import org.gridgain.examples.*;
import org.gridgain.grid.*;
import org.gridgain.grid.cache.*;

/**
 * Loads data from persistent store at cache startup by calling
 * {@link GridCache#loadCache(org.apache.ignite.lang.IgniteBiPredicate, long, Object...)} method on
 * all nodes.
 * <p>
 * Remote nodes should always be started using {@link CacheNodeWithStoreStartup}.
 * Also you can change type of underlying store modifying configuration in the
 * {@link CacheNodeWithStoreStartup#configure()} method.
 */
public class CacheStoreLoadDataExample {
    /** Heap size required to run this example. */
    public static final int MIN_MEMORY = 1024 * 1024 * 1024;

    /** Number of entries to load. */
    private static final int ENTRY_COUNT = 1000000;

    /**
     * Executes example.
     *
     * @param args Command line arguments, none required.
     * @throws IgniteCheckedException If example execution failed.
     */
    public static void main(String[] args) throws Exception {
        ExamplesUtils.checkMinMemory(MIN_MEMORY);

        try (Ignite g = Ignition.start(CacheNodeWithStoreStartup.configure())) {
            System.out.println();
            System.out.println(">>> Cache store load data example started.");

            final GridCache<String, Integer> cache = g.cache(null);

            // Clean up caches on all nodes before run.
            cache.globalClearAll(0);

            long start = System.currentTimeMillis();

            // Start loading cache on all caching nodes.
            g.compute(g.cluster().forCache(null)).broadcast(new IgniteCallable<Object>() {
                @Override public Object call() throws Exception {
                    // Load cache from persistent store.
                    cache.loadCache(null, 0, ENTRY_COUNT);

                    return null;
                }
            });

            long end = System.currentTimeMillis();

            System.out.println(">>> Loaded " + ENTRY_COUNT + " keys with backups in " + (end - start) + "ms.");
        }
    }
}
