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

package org.gridgain.grid.kernal;

import org.apache.ignite.*;
import org.apache.ignite.configuration.*;
import org.apache.ignite.lifecycle.*;
import org.gridgain.grid.*;
import org.gridgain.grid.util.typedef.*;
import org.jetbrains.annotations.*;

import java.util.concurrent.*;

import static org.apache.ignite.IgniteState.*;

/**
 * Tests for {@link org.apache.ignite.Ignition}.
 */
public class GridFactoryVmShutdownTest {
    /**
     *
     */
    private GridFactoryVmShutdownTest() {
        // No-op.
    }

    /**
     * @param args Args (optional).
     * @throws Exception If failed.
     */
    public static void main(String[] args) throws Exception {
        final ConcurrentMap<String, IgniteState> states = new ConcurrentHashMap<>();

        G.addListener(new IgniteListener() {
            @Override public void onStateChange(@Nullable String name, IgniteState state) {
                if (state == STARTED) {
                    IgniteState state0 = states.put(maskNull(name), STARTED);

                    assert state0 == null;
                }
                else {
                    assert state == STOPPED;

                    boolean replaced = states.replace(maskNull(name), STARTED, STOPPED);

                    assert replaced;
                }
            }
        });

        // Test with shutdown hook enabled and disabled.
        // System.setProperty(GridSystemProperties.GG_NO_SHUTDOWN_HOOK, "true");

        // Grid will start and add shutdown hook.
        G.start();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override public void run() {
                IgniteConfiguration cfg = new IgniteConfiguration();

                cfg.setGridName("test1");

                try {
                    G.start(cfg);
                }
                catch (IgniteCheckedException e) {
                    throw new IgniteException("Failed to start grid in shutdown hook.", e);
                }
                finally {
                    X.println("States: " + states);
                }
            }
        }));

        System.exit(0);
    }

    /**
     * Masks {@code null} string.
     *
     * @param s String to mask.
     * @return Mask value or string itself if it is not {@code null}.
     */
    private static String maskNull(String s) {
        return s != null ? s : "null-mask-8AE34BF8";
    }
}
