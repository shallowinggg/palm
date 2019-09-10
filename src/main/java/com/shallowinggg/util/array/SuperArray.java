package com.shallowinggg.util.array;

import java.util.Iterator;

/**
 * @author dingshimin
 * @param <T> 数组元素类型
 */
public interface SuperArray<T extends Number> extends Iterable<T> {

    /**
     * 使用指定值替换数组中指定下标的元素
     *
     * @param index 下标
     * @param val 值
     */
    void set(long index, T val);

    /**
     * 获取数组中指定下标的元素
     *
     * @param index 下标
     * @return 值
     */
    T get(long index);

    /**
     * 获取数组大小
     *
     * @return 数组大小
     */
    long size();

    /**
     * 获取数组在内存中的起始地址
     *
     * @return 内存地址
     */
    long memoryAddress();

    @Override
    Iterator<T> iterator();


    /**
     * 将所有元素的值设置为0
     */
    void clear();

    /**
     * 释放内存。
     *
     * 当数组不再使用时，一定要释放它所占用的内存，否则
     * 将会发生内存泄露。
     */
    void free();

    /**
     * 获取[fromIndex, toIndex = fromIndex + len)范围内的元素。如果fromIndex
     * 与toIndex的值相等，那么将会返回一个空数组。返回的数组
     * 由源数组支撑，换句话说，对返回的数组进行改动将会影响
     * 源数组，反之亦然。
     *
     * 注意：禁止对返回数组调用{@link #free()}操作，当源数组
     * 调用{@link #free()}操作后，不能操作返回数组。
     *
     * @param fromIndex 开始下标
     * @param len 长度
     * @return 子数组
     */
    SuperArray<T> slice(long fromIndex, long len);

    /**
     * 获取[fromIndex, toIndex = fromIndex + len)范围内的元素。
     * 如果fromIndex与toIndex的值相等，那么将会返回一个空数组。
     * 返回数组是源数组的拷贝，改变它将不会影响源数组。
     *
     * @param fromIndex 开始下标
     * @param len 长度
     * @return 子数组
     */
    SuperArray<T> duplicate(long fromIndex, long len);

    /**
     *
     *
     * @return
     */
    SuperArray<T> unwrap();
}
