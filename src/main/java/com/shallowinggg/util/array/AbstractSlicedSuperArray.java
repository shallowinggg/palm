package com.shallowinggg.util.array;

import com.shallowinggg.util.PreConditions;

import java.util.function.Consumer;

import static com.shallowinggg.util.PreConditions.checkNotNull;

/**
 * @author dingshimin
 */
public abstract class AbstractSlicedSuperArray<T extends Number> extends AbstractSuperArray<T> {
    private final SuperArray<T> delegate;
    private final long offset;

    public AbstractSlicedSuperArray(SuperArray<T> superArray, long from, long len) {
        super(len);
        checkSliceOutOfBounds(superArray, from, len);
        if(superArray instanceof AbstractSlicedSuperArray) {
            this.delegate = superArray.unwrap();
            this.offset = ((AbstractSlicedSuperArray<Number>) superArray).offset + from;
        } else {
            this.delegate = superArray;
            this.offset = from;
        }
    }

    @Override
    public void set(long index, T val) {
        checkIndex(index);
        unwrap().set(idx(index), val);
    }

    @Override
    public T get(long index) {
        checkIndex(index);
        return unwrap().get(idx(index));
    }

    @Override
    public long memoryAddress() {
        return unwrap().memoryAddress() + offset;
    }

    @Override
    public SuperArray<T> slice(long fromIndex, long len) {
        checkSliceOutOfBounds(this, fromIndex, len);
        return unwrap().slice(idx(fromIndex), len);
    }

    @Override
    public SuperArray<T> duplicate(long fromIndex, long len) {
        checkSliceOutOfBounds(this, fromIndex, len);
        return unwrap().duplicate(idx(fromIndex), len);
    }

    @Override
    public final void free() {
        throw new UnsupportedOperationException("SlicedSuperArray can't invoke free()");
    }

    @Override
    public SuperArray<T> unwrap() {
        return delegate;
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        checkNotNull(action);
        long size = this.size();
        for(long i = 0; i < size; ++i) {
            action.accept(get(i));
        }
    }

    private long idx(long index) {
        return index + offset;
    }

    private static void checkSliceOutOfBounds(SuperArray<?> superArray, long from, long len) {
        PreConditions.checkIndex(!outOfRange(from, len, superArray.size()),
                "SuperArray.slice(%d, %d)", from, len);
    }
}
