package com.shallowinggg.util.array;

/**
 * 资源跟踪器
 *
 * @author dingshimin
 */
public interface ResourceLeakTracker<T> {
    /**
     * 关闭对资源的跟踪。
     * 当资源被释放后，调用此方法取消跟踪。
     *
     * @param obj 资源对象
     * @return true 如果是第一次调用
     */
    boolean close(T obj);
}
