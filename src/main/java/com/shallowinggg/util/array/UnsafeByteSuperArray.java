package com.shallowinggg.util.array;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.function.Consumer;

import static com.shallowinggg.util.PreConditions.checkNotNull;
import static com.shallowinggg.util.PreConditions.checkState;

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
    public Spliterator<Byte> spliterator() {
        return null;
    }

    @Override
    public Iterator<Byte> iterator() {
        return new Itr();
    }

    @Override
    public void clear() {
        final long sz = size();
        long tmp;
        long len4;
        long len2;
        long len1;

        long len8 = sz / 8;
        tmp = sz % 8;
        len4 = tmp / 4;
        tmp = tmp % 4;
        len2 = tmp / 2;
        len1 = tmp % 2;

        fill0(len8, len4, len2, len1);
    }

    @Override
    public SuperArray<Byte> subArray(long fromIndex, long toIndex) {
        return null;
    }

    private class Itr implements Iterator<Byte> {
        long cursor;       // index of next element to return
        long lastRet = -1; // index of last element returned; -1 if no such

        @Override
        public void remove() {
            checkState(lastRet >= 0);
            set(lastRet, (byte)0);
            cursor = lastRet;
            lastRet = -1;
        }

        @Override
        public void forEachRemaining(Consumer<? super Byte> action) {
            long i = cursor;
            final long size = size();
            if(i > size) {
                return;
            }
            while (i != size) {
                action.accept(get(i++));
            }
            cursor = i;
            lastRet = i - 1;
        }

        @Override
        public boolean hasNext() {
            return cursor != size();
        }

        @Override
        public Byte next() {
            long i = cursor;
            if(i > size()) {
                throw new NoSuchElementException();
            }
            cursor++;
            return get(lastRet = i);
        }
    }
}
