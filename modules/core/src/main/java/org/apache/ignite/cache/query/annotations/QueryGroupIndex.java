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

package org.apache.ignite.cache.query.annotations;

import java.lang.annotation.*;

/**
 * Describes group index.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface QueryGroupIndex {
    /**
     * Group index name.
     *
     * @return Name.
     */
    String name();

    /**
     * If this index is unique.
     *
     * @return True if this index is unique, false otherwise.
     * @deprecated No longer supported, will be ignored.
     */
    @Deprecated
    boolean unique() default false;

    /**
     * List of group indexes for type.
     */
    @SuppressWarnings("PublicInnerClass")
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public static @interface List {
        /**
         * Gets array of group indexes.
         *
         * @return Array of group indexes.
         */
        QueryGroupIndex[] value();
    }
}
