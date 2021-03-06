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

package org.gridgain.examples;

import org.apache.ignite.*;
import org.apache.ignite.lang.*;
import org.gridgain.grid.*;
import org.apache.ignite.lang.IgniteCallable;

/**
 * Demonstrates broadcasting and unicasting computations within grid projection.
 * <p>
 * Remote nodes should always be started with special configuration file which
 * enables P2P class loading: {@code 'ggstart.{sh|bat} examples/config/example-compute.xml'}.
 */
public class ComputeExample {
    /**
     * Executes example.
     *
     * @param args Command line arguments, none required.
     * @throws IgniteCheckedException If example execution failed.
     */
    public static void main(String[] args) throws Exception {
        try (Ignite ignite = Ignition.start("examples/config/example-compute.xml")) {
            System.out.println();
            System.out.println(">>> Compute broadcast example started.");

            // Broadcast closure to all grid nodes.
            ignite.compute().broadcast((IgniteRunnable)() -> System.out.println("Hello World")).get();

            // Unicast closure to some grid node picked by load balancer.
            ignite.compute().run((IgniteRunnable)() -> System.out.println("Hello World")).get();

            // Unicast closure to some grid node picked by load balancer and return result.
            int length = ignite.compute().call((IgniteCallable<Integer>)"Hello World"::length).get();

            System.out.println();
            System.out.println(">>> Computed length: " + length);

            System.out.println();
            System.out.println(">>> Check all nodes for hello message output.");
        }
    }
}
