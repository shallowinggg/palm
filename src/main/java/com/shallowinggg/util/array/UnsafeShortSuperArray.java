package com.shallowinggg.util.array;

import com.shallowinggg.util.PreConditions;

import java.util.Iterator;
import java.util.function.Consumer;

import static com.shallowinggg.util.PreConditions.checkNotNull;
import static com.shallowinggg.util.PreConditions.checkState;

/**
 * @author dingshimin
 */
public class UnsafeShortSuperArray extends AbstractSuperArray<Short> {

    public UnsafeShortSuperArray(long size) {
        super(size, PrimitiveType.SHORT);
    }

    @Override
    public void set(long index, Short val) {
        setShort(index, val);
    }

    @Override
    public Short get(long index) {
        return getShort(index);
    }

    @Override
    public void forEach(Consumer<? super Short> action) {
        checkNotNull(action);
        long size = this.size();
        for (long i = 0; i < size; ++i) {
            action.accept(get(i));
        }
    }

    @Override
    public Iterator<Short> iterator() {
        return new Itr();
    }

    @Override
    public void clear() {
        SuperArrayUtil.clearShorts(size(), this);
    }

    @Override
    public SuperArray<Short> slice(long fromIndex, long len) {
        return new ShortSlicedSuperArray(this, fromIndex, len);
    }

    @Override
    public SuperArray<Short> duplicate(long fromIndex, long len) {
        PreConditions.checkIndex(!outOfRange(fromIndex, len, size()),
                "SuperArray.duplicate(%d, %d)", fromIndex, len);
        UnsafeShortSuperArray retArray = new UnsafeShortSuperArray(len);
        copyMemory(memoryAddress() + fromIndex, retArray.memoryAddress(), len * PrimitiveType.SHORT.getSize());
        return retArray;
    }

    @Override
    public SuperArray<Short> unwrap() {
        return null;
    }

    private class Itr extends AbstractItr {
        @Override
        public void remove() {
            checkState(lastRet >= 0);
            set(lastRet, (short) 0);
            cursor = lastRet;
            lastRet = -1;
        }
    }

    private static class ShortSlicedSuperArray extends AbstractSlicedSuperArray<Short> {
        ShortSlicedSuperArray(SuperArray<Short> superArray, long from, long len) {
            super(superArray, from, len);
        }

        @Override
        public Iterator<Short> iterator() {
            return new AbstractItr() {
                @Override
                public void remove() {
                    checkState(lastRet >= 0);
                    set(lastRet, (short) 0);
                    cursor = lastRet;
                    lastRet = -1;
                }
            };
        }

        @Override
        public void clear() {
            SuperArrayUtil.clearShorts(size(), this);
        }
    }
}
