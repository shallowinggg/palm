package com.shallowinggg.palm.cache;

import java.util.List;

/**
 * @author dingshimin
 * @param <V>
 */
public abstract class CacheLoader<V> {
    protected CacheLoader() {}

    /**
     * 加载所有的缓存值。
     *
     * @return 缓存值
     * @throws Exception 如果无法加载
     */
    public abstract List<V> load() throws Exception;

    /**
     * 重新加载缓存值。
     *
     * @return 缓存值
     * @throws Exception 如果无法加载
     */
    public List<V> reload() throws Exception {
        return load();
    }
}
