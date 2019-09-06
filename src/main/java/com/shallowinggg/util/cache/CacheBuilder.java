package com.shallowinggg.util.cache;

import com.shallowinggg.util.reflect.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static com.shallowinggg.util.PreConditions.*;
import static com.shallowinggg.util.cache.MultiDimensionCache.*;

public class CacheBuilder<V> {
    private static final int DEFAULT_INITIAL_CAPACITY = 16;
    private static final int DEFAULT_EXPIRATION_NANOS = 0;
    private static final int DEFAULT_REFRESH_NANOS = 0;
    private static final int DEFAULT_CLEAR_EXPIRE_THRESHOLD = 1000;

    private static final Supplier<? extends StatsCounter> NULL_STATS_COUNTER =
            () -> new StatsCounter() {
                @Override
                public void recordHits(int count) {
                }

                @Override
                public void recordMisses(int count) {
                }

                @Override
                public void recordLoadSuccess(long loadTime) {
                }

                @Override
                public void recordLoadException(long loadTime) {
                }

                @Override
                public void recordEviction(int count) {
                }

                @Override
                public CacheStats snapshot() {
                    return EMPTY_STATS;
                }
            };

    private static final Supplier<StatsCounter> CACHE_STATS_COUNTER = SimpleStatsCounter::new;
    private static final CacheStats EMPTY_STATS = new CacheStats(0,0,0,0,0,0);

    private static final Logger LOG = LoggerFactory.getLogger(CacheBuilder.class);

    private static final int UNSET_INT = -1;

    private int initialCapacity = UNSET_INT;
    private int clearExpireThreshold = UNSET_INT;
    private Strength strength;

    private long expireAfterWriteNanos = UNSET_INT;
    private long expireAfterAccessNanos = UNSET_INT;
    private long refreshNanos = UNSET_INT;

    private boolean strict = false;

    private Supplier<? extends StatsCounter> statsCounterSupplier = NULL_STATS_COUNTER;

    private CacheBuilder() {}

    public static CacheBuilder<Object> newBuilder() {
        return new CacheBuilder<Object>();
    }

    public CacheBuilder<V> initialCapacity(int initialCapacity) {
        checkState(
                this.initialCapacity == UNSET_INT,
                "initial capacity was already set to %s",
                this.initialCapacity);
        checkArgument(initialCapacity >= 0);
        this.initialCapacity = initialCapacity;
        return this;
    }

    int getInitialCapacity() {
        return (initialCapacity == UNSET_INT) ? DEFAULT_INITIAL_CAPACITY : initialCapacity;
    }

    CacheBuilder<V> strength(Strength strength) {
        checkState(this.strength == null, "Strength was already set to %s", strength);
        this.strength = checkNotNull(strength);
        return this;
    }

    Strength getStrength() {
        if(strength != null) {
            return strength;
        }
        return Strength.STRONG;
    }

    public CacheBuilder<V> weakValues() {
        return strength(Strength.WEAK);
    }

    public CacheBuilder<V> softValues() {
        return strength(Strength.SOFT);
    }

    /**
     * 当设置过期时间后，为了减少检查条目是否过期的开销，选择每查询clearExpireThreshold次后进行一次过期条目的删除工作
     *
     * @param clearExpireThreshold
     * @return
     */
    public CacheBuilder<V> clearExpireThreshold(int clearExpireThreshold) {
        checkState(this.clearExpireThreshold == UNSET_INT,
                "clearExpireThreshold was already set to %s ns", clearExpireThreshold);
        checkArgument(clearExpireThreshold > 0, "clearExpireThreshold cannot be negative: %s", clearExpireThreshold);
        this.clearExpireThreshold = clearExpireThreshold;
        return this;
    }

    int getClearExpireThreshold() {
        return (clearExpireThreshold == UNSET_INT) ? DEFAULT_CLEAR_EXPIRE_THRESHOLD : clearExpireThreshold;
    }

    public CacheBuilder<V> expireAfterWrite(long duration, TimeUnit unit) {
        checkState(
                expireAfterWriteNanos == UNSET_INT,
                "expireAfterWrite was already set to %s ns",
                expireAfterWriteNanos);
        checkArgument(duration >= 0, "duration cannot be negative: %s %s", duration, unit);
        this.expireAfterWriteNanos = unit.toNanos(duration);
        return this;
    }

    long getExpireAfterWriteNanos() {
        return (expireAfterWriteNanos == UNSET_INT) ? DEFAULT_EXPIRATION_NANOS : expireAfterWriteNanos;
    }


    public CacheBuilder<V> expireAfterAccess(long duration, TimeUnit unit) {
        checkState(
                expireAfterAccessNanos == UNSET_INT,
                "expireAfterAccess was already set to %s ns",
                expireAfterAccessNanos);
        checkArgument(duration >= 0, "duration cannot be negative: %s %s", duration, unit);
        this.expireAfterAccessNanos = unit.toNanos(duration);
        return this;
    }

    long getExpireAfterAccessNanos() {
        return (expireAfterAccessNanos == UNSET_INT)
                ? DEFAULT_EXPIRATION_NANOS
                : expireAfterAccessNanos;
    }

    public CacheBuilder<V> refreshAfterWrite(long duration, TimeUnit unit) {
        checkNotNull(unit);
        checkState(refreshNanos == UNSET_INT, "refresh was already set to %s ns", refreshNanos);
        checkArgument(duration > 0, "duration must be positive: %s %s", duration, unit);
        this.refreshNanos = unit.toNanos(duration);
        return this;
    }

    long getRefreshNanos() {
        return (refreshNanos == UNSET_INT) ? DEFAULT_REFRESH_NANOS : refreshNanos;
    }

    public CacheBuilder<V> strict(boolean strict) {
        this.strict = strict;
        return this;
    }

    boolean isStrict() {
        return strict;
    }

    public CacheBuilder<V> recordStats() {
        statsCounterSupplier = CACHE_STATS_COUNTER;
        return this;
    }

    boolean isRecordingStats() {
        return statsCounterSupplier == CACHE_STATS_COUNTER;
    }

    Supplier<? extends StatsCounter> getStatsCounterSupplier() {
        return statsCounterSupplier;
    }

    public <V1 extends V> MultiDimensionCache<V1> build(CacheLoader<V1> loader, TypeReference<V1> typeReference) {
        return new MultiDimensionCache<V1>(this, loader, typeReference);
    }

}
