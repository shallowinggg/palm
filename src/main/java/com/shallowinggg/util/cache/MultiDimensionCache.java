package com.shallowinggg.util.cache;

import com.shallowinggg.util.reflect.MethodWrapper;
import com.shallowinggg.util.reflect.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

import static com.shallowinggg.util.PreConditions.*;

/**
 * 此类的作用是作为运行时缓存存在，目的是避免构建多个redis hash缓存，
 * 维护良好的可伸缩性以及较少的内存占用。
 * <p>
 * 内存结构如下：
 * <p>
 * field1 --> getterMethod1
 * field2 --> getterMethod2
 * ...
 * <p>
 * field1value1 -> List<V>
 * field1value2 -> List<V>
 * ...
 * <p>
 * 以字段为核心，对每个字段分别进行缓存，构建以此字段的多个值为键，符合此字段值的实例为值的Map，
 * 即 select * from table where fieldX = ...
 * <p>
 * 注意：
 * 1. 此类最多只支持具有64个字段的类对象，如需拓展，可更改lock为线程安全的BitSet
 * 2. 可选择严格模式与非严格模式，严格模式要求每一个字段必须有与之对应的getter方法
 *
 * @author shallowinggg
 * @date 2019-08-04
 */
@Deprecated
public class MultiDimensionCache<V> {
    private static final Logger LOG = LoggerFactory.getLogger(MultiDimensionCache.class);

    private static final int ST_UNINITIALIZED = 0;
    private static final int ST_INITIALIZING = 1;
    private static final int ST_INITIALIZED = 2;


    /**
     * 缓存值列表。
     */
    private List<ReferenceEntry<V>> values;

    /**
     * 类型V的字段名称
     */
    private List<String> fields = new ArrayList<>();

    /**
     * 字段的getter方法，与fields一一对应
     */
    private Map<String, MethodWrapper> getterMethods;

    /**
     * 缓存映射
     * Map的key为字段的值，value对应的缓存值
     */
    private Map<String, Map<Object, List<ReferenceEntry<V>>>> mapping;

    /**
     * 是否严格匹配。
     * 如果此值为true，那么每个字段必须有相应的getter方法。
     */
    private boolean strict;

    private volatile boolean initialized = false;

    private AtomicLong lock = new AtomicLong();

    /**
     * 初始容量
     */
    private int initialCapacity;

    /**
     * 值引用强度
     * STRONG, SOFT, WEAK
     */
    private Strength strength;

    /**
     * 写后过期时间
     */
    private long expireAfterWriteNano;

    /**
     * 刷新时间
     */
    private long refreshNanos;

    private int clearExpireThreshold;
    private int queries;

    private CacheLoader<V> cacheLoader;
    private static ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1,
            r -> new Thread("MultiDimensionCache-refreshThread"));

    MultiDimensionCache(CacheBuilder<? super V> builder, CacheLoader<V> loader, TypeReference<V> type) {
        this.cacheLoader = loader;

        this.strict = builder.isStrict();
        this.initialCapacity = builder.getInitialCapacity();
        this.clearExpireThreshold = builder.getClearExpireThreshold();
        this.strength = builder.getStrength();
        this.expireAfterWriteNano = builder.getExpireAfterWriteNanos();
        this.refreshNanos = builder.getRefreshNano();
        this.values = new ArrayList<>(initialCapacity);

        LOG.debug("strict: " + strict + ", initialCapacity: " + initialCapacity + ", clearExpireThreshold: " + clearExpireThreshold
                + ", strength: " + strength + ", expireAfterWriteNano: " + expireAfterWriteNano + ", expireAfterAccessNano: "
                + refreshNanos);

        @SuppressWarnings("unchecked")
        Class<V> clazz = (Class<V>) type.getType();
        resolveClass(clazz);
        this.mapping = new HashMap<>(fields.size());

        try {
            List<V> list = cacheLoader.load();
            for (V val : list) {
                values.add(strength.referenceEntry(val));
            }
        } catch (Exception e) {
            throw new LoadingRuntimeException(e);
        }
        if(refreshNanos > 0) {
            executorService.scheduleAtFixedRate(this::refresh, refreshNanos, refreshNanos, TimeUnit.NANOSECONDS);
        }
    }

    /**
     * 解析Class，获取其所有字段以及相应的getter方法
     *
     * @param clazz Class
     */
    private void resolveClass(Class<?> clazz) {
        LOG.debug("Reflect class: " + clazz);
        checkState(!initialized, "repeat init");

        Field[] fields;
        if (System.getSecurityManager() != null) {
            fields = AccessController.doPrivileged((PrivilegedAction<Field[]>) clazz::getDeclaredFields);
        } else {
            fields = clazz.getDeclaredFields();
        }
        getterMethods = new HashMap<>(fields.length);

        for (Field field : fields) {
            String fieldName = field.getName();
            this.fields.add(fieldName);

            MethodWrapper m;
            try {
                if (field.getType() == boolean.class || field.getType() == Boolean.class) {
                    m = MethodWrapper.findMethod(clazz, getterMethodName(fieldName, true));
                } else {
                    m = MethodWrapper.findMethod(clazz, getterMethodName(fieldName, false));
                }
                getterMethods.put(fieldName, m);
            } catch (NoSuchMethodException e) {
                if (strict) {
                    LOG.error("Class " + clazz.getName() + " don't have getter method for field " + field.getName(), e);
                    throw new RuntimeException(e);
                } else {
                    LOG.warn("Class " + clazz.getName() + " don't have getter method for field " + field.getName());
                    // 占位
                    getterMethods.put(fieldName, null);
                }
            }
        }

        initialized = true;
    }

    /**
     * 构建字段与缓存值之间的映射关系
     *
     * @param name 字段名称
     * @throws IllegalArgumentException 如果类型V中不存在传入的字段
     * @throws IllegalArgumentException 如果传入的字段没有相应的getter方法
     * @throws RuntimeException         如果反射调用失败
     */
    public void mappingValues(String name) {
        checkArgument(fields.contains(name), "Field %s is not exist", name);
        final MethodWrapper method;
        final int index = fields.indexOf(name);
        checkNotNull(method = getterMethods.get(name), "Field %s don't have getter method", name);

        // 映射值时加锁。
        // 以字段为下标，当某个字段正在被映射时，将锁对应的位设置为1
        // 如果已经有其他线程正在映射此字段，那么让出CPU，等待其他线程执行完后返回
        for (; ; ) {
            long lockValue = lock.get();
            if ((lockValue & (1L << index)) != 0) {
                Thread.yield();
            } else {
                if (lock.compareAndSet(lockValue, lockValue | (1L << index))) {
                    Map<Object, List<ReferenceEntry<V>>> map = mapping.get(name);
                    if (map == null) {
                        map = new HashMap<>();
                        mapping.put(name, map);
                    } else {
                        return;
                    }

                    for (ReferenceEntry<V> entry : values) {
                        try {
                            Object key = method.invoke(entry.getVal());
                            map.computeIfAbsent(key, k -> new ArrayList<>());
                            map.get(key).add(entry);
                        } catch (Throwable t) {
                            LOG.error("Invoke method " + method.getMethodName() + " fail", t);
                            throw new RuntimeException(t);
                        }
                    }

                    // 解锁
                    for (; ; ) {
                        lockValue = lock.get();
                        if (lock.compareAndSet(lockValue, lockValue & ~(1 << index))) {
                            return;
                        }
                    }
                }
            }
        }
    }

    /**
     * 返回给定字段以及字段值对应的记录。
     * 注意：如果没有给定字段值对应的记录，那么返回Collections.emptyList()
     *
     * @param field  字段名称
     * @param values 字段值列表
     * @return 对应的记录
     */
    public List<V> getMappingValues(String field, Object... values) {
        if(++queries >= clearExpireThreshold) {
            clearExpiredEntries();
        }

        long now = System.nanoTime();
        // 如果不提供key，那么返回全部值
        if (values.length == 0) {
            List<V> ret = new ArrayList<>(this.values.size() / 2);

            for (ReferenceEntry<V> entry : this.values) {
                if (isAlive(entry, now)) {
                    entry.setAccessTime(now);
                    ret.add(entry.getVal());
                }
            }
            return Collections.unmodifiableList(ret);
        }

        checkArgument(fields.contains(field), "Field %s is not exist", field);
        final Map<Object, List<ReferenceEntry<V>>> map;
        if ((map = mapping.get(field)) != null) {
            List<V> retVal = new ArrayList<V>();
            for (Object val : values) {
                List<ReferenceEntry<V>> entries = map.get(val);
                for (ReferenceEntry<V> entry : entries) {
                    if (isAlive(entry, now)) {
                        entry.setAccessTime(now);
                        retVal.add(entry.getVal());
                    }
                }
            }
            if (retVal.size() == 0) {
                return Collections.emptyList();
            }

            return Collections.unmodifiableList(retVal);
        } else {
            mappingValues(field);
            return getMappingValues(field, values);
        }
    }

    public void refresh() {
        long start = System.currentTimeMillis();
        try {
            this.values.clear();
            List<V> list = cacheLoader.reload();
            for (V val : list) {
                values.add(strength.referenceEntry(val));
            }
            queries = 0;
        } catch (Exception e) {
            LOG.error("Cache refresh fail, caused by: {}", e.getMessage(), e);
        }

        LOG.info("Cache refresh success, cost: {} ms", (System.currentTimeMillis() - start));
    }

    public int size() {
        return values.size();
    }

    private String getterMethodName(String fieldName, boolean isBool) {
        char[] strChar = fieldName.toCharArray();
        strChar[0] -= 32;
        if (isBool) {
            return "is" + String.valueOf(strChar);
        }
        return "get" + String.valueOf(strChar);
    }

    private void clearExpiredEntries() {
        Iterator<ReferenceEntry<V>> itr = values.iterator();
        int removes = 0;
        long now = System.nanoTime();
        long start = System.currentTimeMillis();
        while (itr.hasNext()) {
            ReferenceEntry<V> entry = itr.next();
            if(!isAlive(entry, now)) {
                itr.remove();
                ++removes;
            }
        }
        queries = 0;
        LOG.info("clear expired entries, number: {}, cost time: {} ms", removes, System.currentTimeMillis() - start);
    }

    interface ReferenceEntry<V> {
        /**
         * 设置写时间。
         * 只会在{@link #refresh()}方法调用中使用
         *
         * @param writeTime 新写入时间
         */
        void setWriteTime(long writeTime);

        /**
         * 获取上次写入时间
         *
         * @return 上次写入时间 ns
         */
        long getWriteTime();

        /**
         * 更新访问时间。
         * 每当获取此ReferenceEntry时，都会调用此方法
         *
         * @param accessTime 新访问时间
         */
        void setAccessTime(long accessTime);

        /**
         * 获取上次访问时间。
         *
         * @return 上次访问时间
         */
        long getAccessTime();

        /**
         * 获取引用值
         *
         * @return 引用值
         */
        V getVal();
    }

    static class StrongEntry<V> implements ReferenceEntry<V> {
        long writeTime;
        long accessTime;
        V val;

        StrongEntry(V val) {
            this.val = val;
            this.writeTime = System.nanoTime();
            this.accessTime = Long.MAX_VALUE;
        }

        @Override
        public void setWriteTime(long writeTime) {
            this.writeTime = writeTime;
        }

        @Override
        public void setAccessTime(long accessTime) {
            this.accessTime = accessTime;
        }

        @Override
        public long getWriteTime() {
            return writeTime;
        }

        @Override
        public long getAccessTime() {
            return accessTime;
        }

        @Override
        public V getVal() {
            return val;
        }
    }

    static class WeakEntry<V> extends WeakReference<V> implements ReferenceEntry<V> {
        volatile long writeTime;
        volatile long accessTime;

        WeakEntry(V val) {
            super(val);
            this.writeTime = System.nanoTime();
            this.accessTime = Long.MAX_VALUE;
        }

        @Override
        public void setWriteTime(long writeTime) {
            this.writeTime = writeTime;
        }

        @Override
        public long getWriteTime() {
            return writeTime;
        }

        @Override
        public void setAccessTime(long accessTime) {
            this.accessTime = accessTime;
        }

        @Override
        public long getAccessTime() {
            return accessTime;
        }

        @Override
        public V getVal() {
            return this.get();
        }
    }

    static class SoftEntry<V> extends SoftReference<V> implements ReferenceEntry<V> {
        volatile long writeTime;
        volatile long accessTime;

        SoftEntry(V val) {
            super(val);
            this.writeTime = System.nanoTime();
            this.accessTime = Long.MAX_VALUE;
        }

        @Override
        public void setWriteTime(long writeTime) {
            this.writeTime = writeTime;
        }

        @Override
        public long getWriteTime() {
            return writeTime;
        }

        @Override
        public void setAccessTime(long accessTime) {
            this.accessTime = accessTime;
        }

        @Override
        public long getAccessTime() {
            return accessTime;
        }

        @Override
        public V getVal() {
            return this.get();
        }
    }

    static class SimpleStatsCounter implements StatsCounter {
        LongAdder hits = new LongAdder();
        LongAdder misses = new LongAdder();
        LongAdder loadSuccess = new LongAdder();
        LongAdder loadException = new LongAdder();
        LongAdder totalLoadTime = new LongAdder();

        @Override
        public void recordHits(int count) {
            hits.add(count);
        }

        @Override
        public void recordMisses(int count) {
            misses.add(count);
        }

        @Override
        public void recordLoadSuccess(long loadTime) {
            loadSuccess.increment();
            totalLoadTime.add(loadTime);
        }

        @Override
        public void recordLoadException(long loadTime) {
            loadException.increment();
            totalLoadTime.add(loadTime);
        }

        @Override
        public CacheStats snapshot() {
            return new CacheStats(hits.sum(), misses.sum(), loadSuccess.sum(), loadException.sum(),
                    totalLoadTime.sum());
        }
    }

    enum Strength {
        /**
         * 强引用
         */
        STRONG {
            @Override
            <V> ReferenceEntry<V> referenceEntry(V val) {
                return new StrongEntry<>(val);
            }
        },
        /**
         * 软引用
         */
        SOFT {
            @Override
            <V> ReferenceEntry<V> referenceEntry(V val) {
                return new SoftEntry<>(val);
            }
        },
        /**
         * 弱引用
         */
        WEAK {
            @Override
            <V> ReferenceEntry<V> referenceEntry(V val) {
                return new WeakEntry<>(val);
            }
        };

        abstract <V> ReferenceEntry<V> referenceEntry(V val);
    }

    interface StatsCounter {
        /**
         * 记录命中次数
         *
         * @param count 命中次数
         */
        void recordHits(int count);

        /**
         * 记录未命中次数
         *
         * @param count miss count
         */
        void recordMisses(int count);

        /**
         * 记录成功加载次数以及加载时间
         *
         * @param loadTime 加载时间
         */
        void recordLoadSuccess(long loadTime);

        /**
         * 记录失败加载次数以及加载时间
         *
         * @param loadTime 加载时间
         */
        void recordLoadException(long loadTime);

        /**
         * 获取当前记录次数的镜像
         *
         * @return 镜像
         */
        CacheStats snapshot();
    }

    private boolean isAlive(ReferenceEntry<V> entry, long now) {
        if (expireAfterWriteNano == 0) {
            return true;
        } else {
            return now - entry.getWriteTime() < expireAfterWriteNano;
        }
    }

}
