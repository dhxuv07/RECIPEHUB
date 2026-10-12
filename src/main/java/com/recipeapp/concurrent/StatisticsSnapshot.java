package com.recipeapp.concurrent;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Thread-safe statistics snapshot holder.
 * 
 * Synchronization rationale:
 * - Multiple background tasks may update statistics concurrently
 * - Using ConcurrentHashMap for thread-safe map operations
 * - Using AtomicLong for individual counters (compare-and-swap atomicity)
 * - These provide visibility guarantees across threads without explicit locking
 * 
 * This is process-level synchronization. Firestore provides database-level atomicity.
 */
public class StatisticsSnapshot {
    private final ConcurrentHashMap<String, AtomicLong> counters = new ConcurrentHashMap<>();
    private volatile long lastUpdated;

    public StatisticsSnapshot() {
        // Initialize counters
        counters.put("totalUsers", new AtomicLong(0));
        counters.put("totalRecipes", new AtomicLong(0));
        counters.put("pendingRecipes", new AtomicLong(0));
        counters.put("approvedRecipes", new AtomicLong(0));
        counters.put("rejectedRecipes", new AtomicLong(0));
        counters.put("totalRatings", new AtomicLong(0));
        counters.put("totalComments", new AtomicLong(0));
        this.lastUpdated = System.currentTimeMillis();
    }

    /**
     * Set counter value atomically.
     * 
     * @param key counter key
     * @param value value to set
     */
    public void setCounter(String key, long value) {
        counters.putIfAbsent(key, new AtomicLong(0));
        counters.get(key).set(value);
        this.lastUpdated = System.currentTimeMillis();
    }

    /**
     * Get counter value atomically.
     * 
     * @param key counter key
     * @return counter value
     */
    public long getCounter(String key) {
        AtomicLong counter = counters.get(key);
        return counter != null ? counter.get() : 0;
    }

    /**
     * Increment counter atomically.
     * 
     * @param key counter key
     */
    public void increment(String key) {
        counters.putIfAbsent(key, new AtomicLong(0));
        counters.get(key).incrementAndGet();
        this.lastUpdated = System.currentTimeMillis();
    }

    /**
     * Decrement counter atomically.
     * 
     * @param key counter key
     */
    public void decrement(String key) {
        counters.putIfAbsent(key, new AtomicLong(0));
        counters.get(key).decrementAndGet();
        this.lastUpdated = System.currentTimeMillis();
    }

    /**
     * Get snapshot of all counters.
     * 
     * Returns a copy to prevent external modification.
     */
    public Map<String, Long> getSnapshot() {
        Map<String, Long> snapshot = new ConcurrentHashMap<>();
        counters.forEach((key, value) -> snapshot.put(key, value.get()));
        return snapshot;
    }

    /**
     * Get last update timestamp.
     */
    public long getLastUpdated() {
        return lastUpdated;
    }

    /**
     * Check if snapshot needs refresh (older than threshold).
     */
    public boolean needsRefresh(long thresholdMillis) {
        return System.currentTimeMillis() - lastUpdated > thresholdMillis;
    }

    @Override
    public String toString() {
        return "StatisticsSnapshot{" +
                "counters=" + getSnapshot() +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}
