package com.shallowinggg.util.array;

import com.shallowinggg.util.PreConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.function.Consumer;

import static com.shallowinggg.util.PreConditions.checkNotNull;
import static com.shallowinggg.util.PreConditions.checkState;

/**
 * @author dingshimin
 */
public class UnsafeByteSuperArray extends AbstractSuperArray<Byte> {

    private static final Logger LOG = LoggerFactory.getLogger(UnsafeByteSuperArray.class);

    public UnsafeByteSuperArray(long sz) {
        super(sz, PrimitiveType.BYTE);
    }

    @Override
    public void set(long index, Byte val) {
        setByte(index, val);
    }

    @Override
    public Byte get(long index) {
        return getByte(index);
    }

    @Override
    public void forEach(Consumer<? super Byte> action) {
        checkNotNull(action);
        long size = this.size();
        for(long i = 0; i < size; ++i) {
            action.accept(get(i));
        }
    }

    @Override
    public Iterator<Byte> iterator() {
        return new Itr();
    }

    @Override
    public void clear() {
        SuperArrayUtil.clearBytes(size(), this);
    }

    @Override
    public SuperArray<Byte> slice(long fromIndex, long len) {
        return new ByteSlicedSuperArray(this, fromIndex, len);
    }

    @Override
    public SuperArray<Byte> duplicate(long fromIndex, long len) {
        PreConditions.checkIndex(!outOfRange(fromIndex, len, size()),
                "SuperArray.duplicate(%d, %d)", fromIndex, len);
        UnsafeByteSuperArray retArray = new UnsafeByteSuperArray(len);
        copyMemory(memoryAddress() + fromIndex, retArray.memoryAddress(), len);
        return retArray;
    }

    @Override
    public SuperArray<Byte> unwrap() {
        return null;
    }

    private class Itr extends AbstractItr {
        @Override
        public void remove() {
            checkState(lastRet >= 0);
            set(lastRet, (byte)0);
            cursor = lastRet;
            lastRet = -1;
        }
    }

    private static class ByteSlicedSuperArray extends AbstractSlicedSuperArray<Byte> {
        ByteSlicedSuperArray(SuperArray<Byte> superArray, long from, long len) {
            super(superArray, from, len);
        }

        @Override
        public Iterator<Byte> iterator() {
            return new AbstractItr() {
                @Override
                public void remove() {
                    checkState(lastRet >= 0);
                    set(lastRet, (byte)0);
                    cursor = lastRet;
                    lastRet = -1;
                }
            };
        }

        @Override
        public void clear() {
            SuperArrayUtil.clearBytes(size(), this);
        }
    }
}
