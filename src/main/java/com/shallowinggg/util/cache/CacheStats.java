package com.shallowinggg.util.cache;

import com.sun.istack.internal.Nullable;

import java.util.Objects;

import static com.shallowinggg.util.PreConditions.checkArgument;

/**
 * @author shallowinggg
 */
public class CacheStats {

    /**
     * 命中次数
     */
    private final long hitCount;

    /**
     * 未命中次数
     */
    private final long missCount;

    /**
     * 加载成功次数
     */
    private final long loadSuccessCount;

    /**
     * 加载失败次数
     */
    private final long loadExceptionCount;
    /**
     * 总加载耗时
     */
    private final long totalLoadTime;

    public CacheStats(
            long hitCount,
            long missCount,
            long loadSuccessCount,
            long loadExceptionCount,
            long totalLoadTime) {
        checkArgument(hitCount >= 0);
        checkArgument(missCount >= 0);
        checkArgument(loadSuccessCount >= 0);
        checkArgument(loadExceptionCount >= 0);
        checkArgument(totalLoadTime >= 0);

        this.hitCount = hitCount;
        this.missCount = missCount;
        this.loadSuccessCount = loadSuccessCount;
        this.loadExceptionCount = loadExceptionCount;
        this.totalLoadTime = totalLoadTime;
    }

    public long requestCount() {
        return hitCount + missCount;
    }

    public long hitCount() {
        return hitCount;
    }

    public double hitRate() {
        long requestCount = requestCount();
        return (requestCount == 0) ? 1.0 : (double) hitCount / requestCount;
    }

    public long missCount() {
        return missCount;
    }

    public double missRate() {
        long requestCount = requestCount();
        return (requestCount == 0) ? 0.0 : (double) missCount / requestCount;
    }

    public long loadCount() {
        return loadSuccessCount + loadExceptionCount;
    }

    public long loadSuccessCount() {
        return loadSuccessCount;
    }

    public long loadExceptionCount() {
        return loadExceptionCount;
    }

    public double loadExceptionRate() {
        long totalLoadCount = loadSuccessCount + loadExceptionCount;
        return (totalLoadCount == 0) ? 0.0 : (double) loadExceptionCount / totalLoadCount;
    }

    public long totalLoadTime() {
        return totalLoadTime;
    }

    public double averageLoadPenalty() {
        long totalLoadCount = loadSuccessCount + loadExceptionCount;
        return (totalLoadCount == 0) ? 0.0 : (double) totalLoadTime / totalLoadCount;
    }

    public CacheStats minus(CacheStats other) {
        return new CacheStats(
                Math.max(0, hitCount - other.hitCount),
                Math.max(0, missCount - other.missCount),
                Math.max(0, loadSuccessCount - other.loadSuccessCount),
                Math.max(0, loadExceptionCount - other.loadExceptionCount),
                Math.max(0, totalLoadTime - other.totalLoadTime));
    }

    public CacheStats plus(CacheStats other) {
        return new CacheStats(
                hitCount + other.hitCount,
                missCount + other.missCount,
                loadSuccessCount + other.loadSuccessCount,
                loadExceptionCount + other.loadExceptionCount,
                totalLoadTime + other.totalLoadTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                hitCount, missCount, loadSuccessCount, loadExceptionCount, totalLoadTime);
    }

    @Override
    public boolean equals(@Nullable Object object) {
        if (object instanceof CacheStats) {
            CacheStats other = (CacheStats) object;
            return hitCount == other.hitCount
                    && missCount == other.missCount
                    && loadSuccessCount == other.loadSuccessCount
                    && loadExceptionCount == other.loadExceptionCount
                    && totalLoadTime == other.totalLoadTime;
        }
        return false;
    }

    @Override
    public String toString() {
        return "CacheStats{" +
                "hitCount=" + hitCount +
                ", missCount=" + missCount +
                ", loadSuccessCount" + loadSuccessCount +
                ", loadExceptionCount" + loadExceptionCount +
                ", totalLoadTime" + totalLoadTime + "}";
    }
}
