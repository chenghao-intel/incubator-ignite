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

package org.gridgain.grid.kernal.processors.cache.distributed.near;

import org.gridgain.grid.cache.*;
import org.gridgain.grid.kernal.processors.cache.*;
import org.gridgain.grid.util.typedef.internal.*;

import java.io.*;

import static org.gridgain.grid.cache.GridCacheMode.PARTITIONED;

/**
 * Specific test case for GG-3946
 */
public class GridCachePutArrayValueSelfTest extends GridCacheAbstractSelfTest {
    /** {@inheritDoc} */
    @Override protected int gridCount() {
        return 4;
    }

    /** {@inheritDoc} */
    @Override protected GridCacheConfiguration cacheConfiguration(String gridName) throws Exception {
        GridCacheConfiguration cacheCfg = super.cacheConfiguration(gridName);

        cacheCfg.setCacheMode(PARTITIONED);
        cacheCfg.setBackups(1);

        return cacheCfg;
    }

    /**
     * @throws Exception If failed.
     */
    public void testInternalKeys() throws Exception {
        assert gridCount() >= 2;

        GridCache<InternalKey, Object> prj = grid(0).cache(null);

        final InternalKey key = new InternalKey(0); // Hangs on the first remote put.

        // Make key belongs to remote node.
        while (prj.affinity().mapKeyToPrimaryAndBackups(key).iterator().next().isLocal())
            key.key++;

        // Put bytes array(!), for integer numbers it works correctly.
        prj.put(key, new byte[] {1});
        assertNotNull(prj.get(key));

        prj.put(key, new byte[] {2});
        assertNotNull(prj.get(key));
    }

    /** Test key without {@link GridCacheInternal} parent interface. */
    @SuppressWarnings("PublicInnerClass")
    public static class InternalKey implements Externalizable, GridCacheInternal {
        private long key;

        /**
         * Empty constructor required for {@link Externalizable}.
         *
         */
        public InternalKey() {
            // No-op.
        }

        /**
         * Constructs test key.
         *
         * @param key Wrapped numeric key.
         */
        public InternalKey(long key) {
            this.key = key;
        }

        /** {@inheritDoc} */
        @Override public void writeExternal(ObjectOutput out) throws IOException {
            out.writeLong(key);
        }

        /** {@inheritDoc} */
        @Override public void readExternal(ObjectInput in) throws IOException {
            key = in.readLong();
        }

        /** {@inheritDoc} */
        @Override public int hashCode() {
            return (int)(key ^ (key >>> 32));
        }

        /** {@inheritDoc} */
        @Override public boolean equals(Object o) {
            return o != null && getClass() == o.getClass() && key == ((InternalKey)o).key;
        }

        /** {@inheritDoc} */
        @Override public String toString() {
            return S.toString(InternalKey.class, this);
        }
    }
}
