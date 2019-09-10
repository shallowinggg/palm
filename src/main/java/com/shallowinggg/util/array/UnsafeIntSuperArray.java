package com.shallowinggg.util.array;

import com.shallowinggg.util.PreConditions;

import java.util.Iterator;
import java.util.function.Consumer;

import static com.shallowinggg.util.PreConditions.checkNotNull;
import static com.shallowinggg.util.PreConditions.checkState;

/**
 * @author dingshimin
 */
public class UnsafeIntSuperArray extends AbstractSuperArray<Integer> {
    public UnsafeIntSuperArray(long sz) {
        super(sz, PrimitiveType.INT);
    }

    @Override
    public void set(long index, Integer val) {
        setInt(index, val);
    }

    @Override
    public Integer get(long index) {
        return getInt(index);
    }

    @Override
    public void forEach(Consumer<? super Integer> action) {
        checkNotNull(action);
        long size = this.size();
        for (long i = 0; i < size; ++i) {
            action.accept(get(i));
        }
    }

    @Override
    public Iterator<Integer> iterator() {
        return new Itr();
    }

    @Override
    public void clear() {
        SuperArrayUtil.clearInts(size(), this);
    }

    @Override
    public SuperArray<Integer> slice(long fromIndex, long len) {
        return new IntSlicedSuperArray(this, fromIndex, len);
    }

    @Override
    public SuperArray<Integer> duplicate(long fromIndex, long len) {
        PreConditions.checkIndex(!outOfRange(fromIndex, len, size()),
                "SuperArray.duplicate(%d, %d)", fromIndex, len);
        UnsafeIntSuperArray retArray = new UnsafeIntSuperArray(len);
        copyMemory(memoryAddress() + fromIndex, retArray.memoryAddress(), len * PrimitiveType.INT.getSize());
        return retArray;
    }

    @Override
    public SuperArray<Integer> unwrap() {
        return null;
    }

    private class Itr extends AbstractItr {
        @Override
        public void remove() {
            checkState(lastRet >= 0);
            set(lastRet, 0);
            cursor = lastRet;
            lastRet = -1;
        }
    }

    private static class IntSlicedSuperArray extends AbstractSlicedSuperArray<Integer> {
        IntSlicedSuperArray(SuperArray<Integer> superArray, long from, long len) {
            super(superArray, from, len);
        }

        @Override
        public Iterator<Integer> iterator() {
            return new AbstractItr() {
                @Override
                public void remove() {
                    checkState(lastRet >= 0);
                    set(lastRet, 0);
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
