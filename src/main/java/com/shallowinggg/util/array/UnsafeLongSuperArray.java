package com.shallowinggg.util.array;

import com.shallowinggg.util.PreConditions;

import java.util.Iterator;
import java.util.function.Consumer;

import static com.shallowinggg.util.PreConditions.checkNotNull;
import static com.shallowinggg.util.PreConditions.checkState;

/**
 * @author dingshimin
 */
public class UnsafeLongSuperArray extends AbstractSuperArray<Long> {

    public UnsafeLongSuperArray(long size) {
        super(size, PrimitiveType.LONG);
    }

    @Override
    public void set(long index, Long val) {
        setLong(index, val);
    }

    @Override
    public Long get(long index) {
        return getLong(index);
    }

    @Override
    public void forEach(Consumer<? super Long> action) {
        checkNotNull(action);
        long size = this.size();
        for (long i = 0; i < size; ++i) {
            action.accept(get(i));
        }
    }

    @Override
    public Iterator<Long> iterator() {
        return new Itr();
    }

    @Override
    public void clear() {
        SuperArrayUtil.clearLongs(size(), this);
    }

    @Override
    public SuperArray<Long> slice(long fromIndex, long len) {
        return new LongSlicedSuperArray(this, fromIndex, len);
    }

    @Override
    public SuperArray<Long> duplicate(long fromIndex, long len) {
        PreConditions.checkIndex(!outOfRange(fromIndex, len, size()),
                "SuperArray.duplicate(%d, %d)", fromIndex, len);
        UnsafeLongSuperArray retArray = new UnsafeLongSuperArray(len);
        copyMemory(memoryAddress() + fromIndex, retArray.memoryAddress(), len * PrimitiveType.LONG.getSize());
        return retArray;
    }

    @Override
    public SuperArray<Long> unwrap() {
        return null;
    }

    private class Itr extends AbstractItr {
        @Override
        public void remove() {
            checkState(lastRet >= 0);
            set(lastRet, 0L);
            cursor = lastRet;
            lastRet = -1;
        }
    }

    private static class LongSlicedSuperArray extends AbstractSlicedSuperArray<Long> {
        LongSlicedSuperArray(SuperArray<Long> superArray, long from, long len) {
            super(superArray, from, len);
        }

        @Override
        public Iterator<Long> iterator() {
            return new AbstractItr() {
                @Override
                public void remove() {
                    checkState(lastRet >= 0);
                    set(lastRet, 0L);
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
