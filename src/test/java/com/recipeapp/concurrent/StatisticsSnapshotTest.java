package com.recipeapp.concurrent;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Unit tests for StatisticsSnapshot thread safety.
 * 
 * Tests concurrent access and atomic operations.
 */
public class StatisticsSnapshotTest {

    @Test
    public void testAtomicCounterOperations() {
        StatisticsSnapshot snapshot = new StatisticsSnapshot();
        
        snapshot.setCounter("totalUsers", 10);
        assertEquals(10, snapshot.getCounter("totalUsers"));
        
        snapshot.increment("totalUsers");
        assertEquals(11, snapshot.getCounter("totalUsers"));
        
        snapshot.decrement("totalUsers");
        assertEquals(10, snapshot.getCounter("totalUsers"));
    }

    @Test
    public void testConcurrentCounterUpdates() throws InterruptedException {
        StatisticsSnapshot snapshot = new StatisticsSnapshot();
        int numThreads = 10;
        int increments = 100;
        CountDownLatch latch = new CountDownLatch(numThreads);
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < increments; j++) {
                        snapshot.increment("totalRecipes");
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // 10 threads * 100 increments = 1000
        assertEquals(1000, snapshot.getCounter("totalRecipes"));
    }

    @Test
    public void testSnapshotIsolation() {
        StatisticsSnapshot snapshot = new StatisticsSnapshot();
        snapshot.setCounter("totalRecipes", 5);
        snapshot.setCounter("totalUsers", 3);

        Map<String, Long> snap1 = snapshot.getSnapshot();
        snapshot.increment("totalRecipes");
        Map<String, Long> snap2 = snapshot.getSnapshot();

        // snap1 should not be affected by later changes
        assertEquals(5, snap1.get("totalRecipes").longValue());
        assertEquals(6, snap2.get("totalRecipes").longValue());
    }

    @Test
    public void testNeedsRefresh() throws InterruptedException {
        StatisticsSnapshot snapshot = new StatisticsSnapshot();
        
        assertFalse(snapshot.needsRefresh(1000)); // Just updated, doesn't need refresh
        
        Thread.sleep(150);
        assertTrue(snapshot.needsRefresh(100)); // Wait 150ms, threshold 100ms = needs refresh
    }
}
