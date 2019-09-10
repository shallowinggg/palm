package com.shallowinggg.util.array;

import com.shallowinggg.util.PreConditions;

import java.util.Iterator;
import java.util.function.Consumer;

import static com.shallowinggg.util.PreConditions.checkNotNull;
import static com.shallowinggg.util.PreConditions.checkState;

/**
 * @author dingshimin
 */
public class UnsafeFloatSuperArray extends AbstractSuperArray<Float> {

    public UnsafeFloatSuperArray(long size) {
        super(size, PrimitiveType.FLOAT);
    }

    @Override
    public void set(long index, Float val) {
        setFloat(index, val);
    }

    @Override
    public Float get(long index) {
        return getFloat(index);
    }

    @Override
    public void forEach(Consumer<? super Float> action) {
        checkNotNull(action);
        long size = this.size();
        for (long i = 0; i < size; ++i) {
            action.accept(get(i));
        }
    }

    @Override
    public Iterator<Float> iterator() {
        return new Itr();
    }

    @Override
    public void clear() {
        SuperArrayUtil.clearInts(size(), this);
    }

    @Override
    public SuperArray<Float> slice(long fromIndex, long len) {
        return new FloatSlicedSuperArray(this, fromIndex, len);
    }

    @Override
    public SuperArray<Float> duplicate(long fromIndex, long len) {
        PreConditions.checkIndex(!outOfRange(fromIndex, len, size()),
                "SuperArray.duplicate(%d, %d)", fromIndex, len);
        UnsafeFloatSuperArray retArray = new UnsafeFloatSuperArray(len);
        copyMemory(memoryAddress() + fromIndex, retArray.memoryAddress(), len * PrimitiveType.FLOAT.getSize());
        return retArray;
    }

    @Override
    public SuperArray<Float> unwrap() {
        return null;
    }

    private class Itr extends AbstractItr {
        @Override
        public void remove() {
            checkState(lastRet >= 0);
            set(lastRet, 0f);
            cursor = lastRet;
            lastRet = -1;
        }
    }

    private static class FloatSlicedSuperArray extends AbstractSlicedSuperArray<Float> {
        FloatSlicedSuperArray(SuperArray<Float> superArray, long from, long len) {
            super(superArray, from, len);
        }

        @Override
        public Iterator<Float> iterator() {
            return new AbstractItr() {
                @Override
                public void remove() {
                    checkState(lastRet >= 0);
                    set(lastRet, 0f);
                    cursor = lastRet;
                    lastRet = -1;
                }
            };
        }

        @Override
        public void clear() {
            SuperArrayUtil.clearInts(size(), this);
        }
    }
}
