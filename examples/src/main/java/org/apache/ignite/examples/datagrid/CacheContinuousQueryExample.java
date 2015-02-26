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

package org.apache.ignite.examples.datagrid;

import org.apache.ignite.*;
import org.apache.ignite.cache.query.*;
import org.apache.ignite.lang.*;

import javax.cache.*;
import javax.cache.event.*;

/**
 * This examples demonstrates continuous query API.
 * <p>
 * Remote nodes should always be started with special configuration file which
 * enables P2P class loading: {@code 'ignite.{sh|bat} examples/config/example-cache.xml'}.
 * <p>
 * Alternatively you can run {@link CacheNodeStartup} in another JVM which will
 * start node with {@code examples/config/example-cache.xml} configuration.
 */
public class CacheContinuousQueryExample {
    /** Cache name. */
    private static final String CACHE_NAME = "partitioned";

    /**
     * Executes example.
     *
     * @param args Command line arguments, none required.
     * @throws Exception If example execution failed.
     */
    public static void main(String[] args) throws Exception {
        try (Ignite ignite = Ignition.start("examples/config/example-cache.xml")) {
            System.out.println();
            System.out.println(">>> Cache continuous query example started.");

            IgniteCache<Integer, String> cache = ignite.jcache(CACHE_NAME);

            // Clean up caches on all nodes before run.
            cache.clear();

            int keyCnt = 20;

            // These entries will be queried by initial predicate.
            for (int i = 0; i < keyCnt; i++)
                cache.put(i, Integer.toString(i));

            // Create new continuous query.
            ContinuousQuery<Integer, String> qry = new ContinuousQuery<>();

            qry.setInitialPredicate(new ScanQuery<>(new IgniteBiPredicate<Integer, String>() {
                @Override public boolean apply(Integer key, String val) {
                    return key > 10;
                }
            }));

            // Callback that is called locally when update notifications are received.
            qry.setLocalListener(new CacheEntryUpdatedListener<Integer, String>() {
                @Override public void onUpdated(Iterable<CacheEntryEvent<? extends Integer, ? extends String>> evts) {
                    for (CacheEntryEvent<? extends Integer, ? extends String> e : evts)
                        System.out.println("Updated entry [key=" + e.getKey() + ", val=" + e.getValue() + ']');
                }
            });

            // This filter will be evaluated remotely on all nodes.
            // Entry that pass this filter will be sent to the caller.
            qry.setRemoteFilter(new CacheEntryEventFilter<Integer, String>() {
                @Override public boolean evaluate(CacheEntryEvent<? extends Integer, ? extends String> e) {
                    return e.getKey() > 10;
                }
            });

            // Execute query.
            try (QueryCursor<Cache.Entry<Integer, String>> cur = cache.query(qry)) {
                // Iterate through existing data.
                for (Cache.Entry<Integer, String> e : cur)
                    System.out.println("Queried existing entry [key=" + e.getKey() + ", val=" + e.getValue() + ']');

                // Add a few more keys and watch more query notifications.
                for (int i = keyCnt; i < keyCnt + 10; i++)
                    cache.put(i, Integer.toString(i));

                // Wait for a while while callback is notified about remaining puts.
                Thread.sleep(2000);
            }
        }
    }
}
