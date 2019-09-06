package com.shallowinggg.util.array;

import java.util.Comparator;
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

    @Override
    Iterator<T> iterator();


    /**
     * 使用给定的比较器{@link Comparator}对数组中的元素进行排序。
     * 如果不提供比较器，那么将元素必须实现{@link Comparable}接口，
     * 并且使用元素的自然顺序{@link Comparable}进行排序。
     *
     * @param c 比较器
     */
    void sort(Comparator<? super T> c);

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
     * 获取[fromIndex, toIndex)范围内的元素。如果fromIndex
     * 与toIndex的值相等，那么将会返回一个空数组。返回的数组
     * 由源数组支撑，换句话说，对返回的数组进行改动将会影响
     * 源数组，反之亦然。
     *
     * 注意：禁止对返回数组调用{@link #free()}操作。当源数组
     * 调用{@link #free()}操作后，不能操作返回数组。
     *
     * @param fromIndex
     * @param toIndex
     * @return
     */
    SuperArray<T> subArray(long fromIndex, long toIndex);
}
