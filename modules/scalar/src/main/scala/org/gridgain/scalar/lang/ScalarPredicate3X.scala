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

package org.gridgain.scalar.lang

import org.apache.ignite._
import org.gridgain.grid._
import org.gridgain.grid.util.lang.GridPredicate3X

/**
 * Peer deploy aware adapter for Java's `GridPredicate3X`.
 */
class ScalarPredicate3X[T1, T2, T3](private val p: (T1, T2, T3) => Boolean) extends GridPredicate3X[T1, T2, T3] {
    assert(p != null)

    /**
     * Delegates to passed in function.
     */
    @throws(classOf[IgniteCheckedException])
    def applyx(e1: T1, e2: T2, e3: T3): Boolean = {
        p(e1, e2, e3)
    }
}
