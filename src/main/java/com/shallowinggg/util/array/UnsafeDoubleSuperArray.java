package com.shallowinggg.util.array;

import com.shallowinggg.util.PreConditions;

import java.util.Iterator;
import java.util.function.Consumer;

import static com.shallowinggg.util.PreConditions.checkNotNull;
import static com.shallowinggg.util.PreConditions.checkState;

/**
 * @author dingshimin
 */
public class UnsafeDoubleSuperArray extends AbstractSuperArray<Double> {

    public UnsafeDoubleSuperArray(long size) {
        super(size, PrimitiveType.DOUBLE);
    }

    @Override
    public void set(long index, Double val) {
        setDouble(index, val);
    }

    @Override
    public Double get(long index) {
        return getDouble(index);
    }

    @Override
    public void forEach(Consumer<? super Double> action) {
        checkNotNull(action);
        long size = this.size();
        for (long i = 0; i < size; ++i) {
            action.accept(get(i));
        }
    }

    @Override
    public Iterator<Double> iterator() {
        return new Itr();
    }

    @Override
    public void clear() {
        SuperArrayUtil.clearLongs(size(), this);
    }

    @Override
    public SuperArray<Double> slice(long fromIndex, long len) {
        return new DoubleSlicedSuperArray(this, fromIndex, len);
    }

    @Override
    public SuperArray<Double> duplicate(long fromIndex, long len) {
        PreConditions.checkIndex(!outOfRange(fromIndex, len, size()),
                "SuperArray.duplicate(%d, %d)", fromIndex, len);
        UnsafeDoubleSuperArray retArray = new UnsafeDoubleSuperArray(len);
        copyMemory(memoryAddress() + fromIndex, retArray.memoryAddress(), len * PrimitiveType.DOUBLE.getSize());
        return retArray;
    }

    @Override
    public SuperArray<Double> unwrap() {
        return null;
    }

    private class Itr extends AbstractItr {
        @Override
        public void remove() {
            checkState(lastRet >= 0);
            set(lastRet, (double) 0);
            cursor = lastRet;
            lastRet = -1;
        }
    }

    private static class DoubleSlicedSuperArray extends AbstractSlicedSuperArray<Double> {
        DoubleSlicedSuperArray(SuperArray<Double> superArray, long from, long len) {
            super(superArray, from, len);
        }

        @Override
        public Iterator<Double> iterator() {
            return new AbstractItr() {
                @Override
                public void remove() {
                    checkState(lastRet >= 0);
                    set(lastRet, (double) 0);
                    cursor = lastRet;
                    lastRet = -1;
                }
            };
        }

        @Override
        public void clear() {
            SuperArrayUtil.clearLongs(size(), this);
        }
    }
}
