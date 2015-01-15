/* @java.file.header */

/*  _________        _____ __________________        _____
 *  __  ____/___________(_)______  /__  ____/______ ____(_)_______
 *  _  / __  __  ___/__  / _  __  / _  / __  _  __ `/__  / __  __ \
 *  / /_/ /  _  /    _  /  / /_/ /  / /_/ /  / /_/ / _  /  _  / / /
 *  \____/   /_/     /_/   \_,__/   \____/   \__,_/  /_/   /_/ /_/
 */

package org.gridgain.grid.kernal.processors.cache;

import org.apache.ignite.lang.*;
import org.gridgain.grid.cache.GridCacheProjection;

import java.io.*;

/**
 */
public final class GridCacheTransformComputeClosure<V, R> implements IgniteClosure<V, V>, Externalizable {
    /** */
    private static final long serialVersionUID = 0L;

    /** */
    private IgniteClosure<V, IgniteBiTuple<V, R>> transformer;

    /** */
    private R retVal;

    /**
     * Required by {@link Externalizable}.
     */
    public GridCacheTransformComputeClosure() {
        // No-op.
    }

    /**
     * @param transformer Transformer closure.
     */
    public GridCacheTransformComputeClosure(IgniteClosure<V, IgniteBiTuple<V, R>> transformer) {
        this.transformer = transformer;
    }

    /**
     * @return Return value for {@link GridCacheProjection#transformAndCompute(Object, org.apache.ignite.lang.IgniteClosure)}
     */
    public R returnValue() {
        return retVal;
    }

    /** {@inheritDoc} */
    @Override public V apply(V v) {
        IgniteBiTuple<V, R> t = transformer.apply(v);

        retVal = t.get2();

        return t.get1();
    }

    /** {@inheritDoc} */
    @Override public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(transformer);
    }

    /** {@inheritDoc} */
    @Override public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        transformer = (IgniteClosure<V, IgniteBiTuple<V, R>>)in.readObject();
    }
}