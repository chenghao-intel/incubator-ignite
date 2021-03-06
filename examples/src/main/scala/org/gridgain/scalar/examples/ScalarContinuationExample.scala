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

package org.gridgain.scalar.examples

import org.apache.ignite.compute.ComputeJobContext
import org.apache.ignite.lang.{IgniteFuture, IgniteClosure}
import org.apache.ignite.resources.IgniteJobContextResource
import org.gridgain.scalar._
import scalar._
import java.math._
import org.jetbrains.annotations.Nullable
import java.util

/**
 * This example recursively calculates `Fibonacci` numbers on the grid. This is
 * a powerful design pattern which allows for creation of fully distributively recursive
 * (a.k.a. nested) tasks or closures with continuations. This example also shows
 * usage of `continuations`, which allows us to wait for results from remote nodes
 * without blocking threads.
 * <p>
 * Note that because this example utilizes local node storage via `GridNodeLocal`,
 * it gets faster if you execute it multiple times, as the more you execute it,
 * the more values it will be cached on remote nodes.
 * <p>
 * Remote nodes should always be started with special configuration file which
 * enables P2P class loading: `'ggstart.{sh|bat} examples/config/example-compute.xml'`.
 */
object ScalarContinuationExample {
    def main(args: Array[String]) {
        scalar("examples/config/example-compute.xml") {
            // Calculate fibonacci for N.
            val N: Long = 100

            val thisNode = grid$.cluster().localNode

            val start = System.currentTimeMillis

            // Projection that excludes this node if others exists.
            val prj = if (grid$.cluster().nodes().size() > 1) grid$.cluster().forOthers(thisNode) else grid$.cluster().forNode(thisNode)

            val fib = grid$.compute(prj).apply(new FibonacciClosure(thisNode.id()), N)

            val duration = System.currentTimeMillis - start

            println(">>>")
            println(">>> Finished executing Fibonacci for '" + N + "' in " + duration + " ms.")
            println(">>> Fibonacci sequence for input number '" + N + "' is '" + fib + "'.")
            println(">>> You should see prints out every recursive Fibonacci execution on grid nodes.")
            println(">>> Check remote nodes for output.")
            println(">>>")
        }
    }
}

/**
 * Closure to execute.
 *
 * @param excludeNodeId Node to exclude from execution if there are more then 1 node in grid.
 */
class FibonacciClosure (
    private[this] val excludeNodeId: util.UUID
) extends IgniteClosure[Long, BigInteger] {
    // These fields must be *transient* so they do not get
    // serialized and sent to remote nodes.
    // However, these fields will be preserved locally while
    // this closure is being "held", i.e. while it is suspended
    // and is waiting to be continued.
    @transient private var fut1, fut2: IgniteFuture[BigInteger] = null

    // Auto-inject job context.
    @IgniteJobContextResource
    private val jobCtx: ComputeJobContext = null

    @Nullable override def apply(num: Long): BigInteger = {
        if (fut1 == null || fut2 == null) {
            println(">>> Starting fibonacci execution for number: " + num)

            // Make sure n is not negative.
            val n = math.abs(num)

            val g = grid$

            if (n <= 2)
                return if (n == 0)
                    BigInteger.ZERO
                else
                    BigInteger.ONE

            // Get properly typed node-local storage.
            val store = g.cluster().nodeLocalMap[Long, IgniteFuture[BigInteger]]()

            // Check if value is cached in node-local store first.
            fut1 = store.get(n - 1)
            fut2 = store.get(n - 2)

            val excludeNode = grid$.cluster().node(excludeNodeId)

            // Projection that excludes node with id passed in constructor if others exists.
            val prj = if (grid$.cluster().nodes().size() > 1) grid$.cluster().forOthers(excludeNode) else grid$.cluster().forNode(excludeNode)

            val comp = grid$.compute(prj).enableAsync()

            // If future is not cached in node-local store, cache it.
            // Note recursive grid execution!
            if (fut1 == null) {
                comp.apply(new FibonacciClosure(excludeNodeId), n - 1)

                fut1 = store.addIfAbsent(n - 1, comp.future[BigInteger]())
            }

            // If future is not cached in node-local store, cache it.
            if (fut2 == null) {
                comp.apply(new FibonacciClosure(excludeNodeId), n - 2)

                fut2 = store.addIfAbsent(n - 2, comp.future[BigInteger]())
            }

            // If futures are not done, then wait asynchronously for the result
            if (!fut1.isDone || !fut2.isDone) {
                val lsnr = (fut: IgniteFuture[BigInteger]) => {
                    // This method will be called twice, once for each future.
                    // On the second call - we have to have both futures to be done
                    // - therefore we can call the continuation.
                    if (fut1.isDone && fut2.isDone)
                        jobCtx.callcc() // Resume job execution.
                }

                // Attach the same listener to both futures.
                fut1.listenAsync(lsnr)
                fut2.listenAsync(lsnr)

                // Hold (suspend) job execution.
                // It will be resumed in listener above via 'callcc()' call
                // once both futures are done.
                return jobCtx.holdcc()
            }
        }

        assert(fut1.isDone && fut2.isDone)

        // Return cached results.
        fut1.get.add(fut2.get)
    }
}
