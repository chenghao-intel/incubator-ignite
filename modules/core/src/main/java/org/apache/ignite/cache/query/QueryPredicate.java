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

package org.apache.ignite.cache.query;

import org.apache.ignite.lang.*;
import org.gridgain.grid.util.typedef.internal.*;

import javax.cache.*;

/**
 * Query predicate to pass into any of {@code Cache.query(...)} methods.
 * Use {@link QuerySqlPredicate} and {@link QueryTextPredicate} for SQL and
 * text queries accordingly.
 *
 * @author @java.author
 * @version @java.version
 */
public abstract class QueryPredicate<K, V> implements IgnitePredicate<Cache.Entry<K, V>> {
    /** Page size. */
    private int pageSize;

    /**
     * Empty constructor.
     */
    protected QueryPredicate() {
        // No-op.
    }

    /**
     * Constructs query predicate with optional page size, if {@code 0},
     * then {@link QueryConfiguration#getPageSize()} is used.
     *
     * @param pageSize Optional page size.
     */
    protected QueryPredicate(int pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * Gets optional page size, if {@code 0}, then {@link QueryConfiguration#getPageSize()} is used.
     *
     * @return Optional page size.
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * Sets optional page size, if {@code 0}, then {@link QueryConfiguration#getPageSize()} is used.
     *
     * @param pageSize Optional page size.
     */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return S.toString(QueryPredicate.class, this);
    }
}
