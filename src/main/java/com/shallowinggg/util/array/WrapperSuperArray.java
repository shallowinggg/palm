package com.shallowinggg.util.array;

import java.util.Iterator;
import java.util.function.Consumer;

class WrapperSuperArray<T extends Number> extends AbstractSuperArray<T> {
    private SuperArray<T> wrapped;

    WrapperSuperArray(SuperArray<T> superArray) {
        super(superArray.size());
        this.wrapped = superArray;
    }

    @Override
    public void set(long index, T val) {
        wrapped.set(index, val);
    }

    @Override
    public T get(long index) {
        return wrapped.get(index);
    }

    @Override
    public Iterator<T> iterator() {
        return wrapped.iterator();
    }

    @Override
    public void clear() {
        wrapped.clear();
    }

    @Override
    public SuperArray<T> slice(long fromIndex, long len) {
        return wrapped.slice(fromIndex, len);
    }

    @Override
    public SuperArray<T> duplicate(long fromIndex, long len) {
        return wrapped.duplicate(fromIndex, len);
    }

    @Override
    public SuperArray<T> unwrap() {
        return wrapped;
    }

    @Override
    public long size() {
        return wrapped.size();
    }

    @Override
    public long memoryAddress() {
        return wrapped.memoryAddress();
    }

    @Override
    public void free() {
        wrapped.free();
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        wrapped.forEach(action);
    }
}
