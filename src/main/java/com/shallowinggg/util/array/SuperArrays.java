package com.shallowinggg.util.array;

import com.shallowinggg.util.PreConditions;

/**
 * @author dingshimin
 */
public class SuperArrays {

    private SuperArrays() {
    }

    /**
     * 构造一个指定长度的byte数组，并跟踪其内存泄露情况
     *
     * @param size 长度
     * @return byte数组
     */
    public static SuperArray<Byte> newByteArray(long size) {
        return toLeakAware(new UnsafeByteSuperArray(size));
    }

    /**
     * 造一个指定长度的short数组，并跟踪其内存泄露情况
     *
     * @param size 长度
     * @return short数组
     */
    public static SuperArray<Short> newShortArray(long size) {
        return toLeakAware(new UnsafeShortSuperArray(size));
    }

    /**
     * 造一个指定长度的int数组，并跟踪其内存泄露情况
     *
     * @param size 长度
     * @return int数组
     */
    public static SuperArray<Integer> newIntArray(long size) {
        return toLeakAware(new UnsafeIntSuperArray(size));
    }

    /**
     * 造一个指定长度的float数组，并跟踪其内存泄露情况
     *
     * @param size 长度
     * @return float数组
     */
    public static SuperArray<Float> newFloatArray(long size) {
        return toLeakAware(new UnsafeFloatSuperArray(size));
    }

    /**
     * 造一个指定长度的long数组，并跟踪其内存泄露情况
     *
     * @param size 长度
     * @return long数组
     */
    public static SuperArray<Long> newLongArray(long size) {
        return toLeakAware(new UnsafeLongSuperArray(size));
    }

    /**
     * 造一个指定长度的double数组，并跟踪其内存泄露情况
     *
     * @param size 长度
     * @return double数组
     */
    public static SuperArray<Double> newDoubleArray(long size) {
        return toLeakAware(new UnsafeDoubleSuperArray(size));
    }



    private static <T extends Number> SuperArray<T> toLeakAware(SuperArray<T> superArray) {
        ResourceLeakTracker<SuperArray<?>> tracker = AbstractSuperArray.leakDetector.track(superArray);
        return new SimpleLeakAwareSuperArray<>(superArray, tracker);
    }



    private static class SimpleLeakAwareSuperArray<T extends Number> extends WrapperSuperArray<T> {
        private final SuperArray<T> superArray;
        private ResourceLeakTracker<SuperArray<?>> tracker;

        SimpleLeakAwareSuperArray(SuperArray<T> array, ResourceLeakTracker<SuperArray<?>> tracker) {
            super(array);
            this.superArray = array;
            this.tracker = tracker;
        }

        @Override
        public SuperArray<T> duplicate(long fromIndex, long len) {
            return newLeakAwareSuperArray(super.duplicate(fromIndex, len));
        }

        private SuperArray<T> newLeakAwareSuperArray(SuperArray<T> superArray) {
            return new SimpleLeakAwareSuperArray<>(superArray, AbstractSuperArray.leakDetector.track(superArray));
        }

        @Override
        public void free() {
            super.free();
            PreConditions.checkState(tracker.close(superArray));
        }
    }

}
