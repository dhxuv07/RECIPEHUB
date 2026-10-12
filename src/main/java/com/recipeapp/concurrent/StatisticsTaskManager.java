package com.recipeapp.concurrent;

import com.recipeapp.service.StatisticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Background task manager for statistics refreshing.
 * 
 * Multithreading rationale:
 * - Uses ExecutorService with controlled thread pool (not unbounded)
 * - Statistics calculation can be expensive - run in background
 * - Non-blocking: main application doesn't wait for stats updates
 * - Graceful shutdown mechanism prevents resource leaks
 * 
 * Thread pool size: 1 - statistics updates are sequential, no parallelism needed
 */
public class StatisticsTaskManager {
    private static final Logger logger = LoggerFactory.getLogger(StatisticsTaskManager.class);
    
    private final ExecutorService executor;
    private final StatisticsSnapshot snapshot;
    private final StatisticsService statisticsService;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final long updateIntervalMillis;

    /**
     * Create statistics task manager.
     * 
     * @param statisticsService service to refresh statistics
     * @param updateIntervalMillis interval between updates (in milliseconds)
     */
    public StatisticsTaskManager(StatisticsService statisticsService, long updateIntervalMillis) {
        this.statisticsService = statisticsService;
        this.snapshot = new StatisticsSnapshot();
        this.updateIntervalMillis = updateIntervalMillis;
        
        // Single-threaded executor - statistics updates are sequential
        this.executor = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "StatisticsRefreshThread");
            thread.setDaemon(true);
            return thread;
        });
    }

    /**
     * Start background statistics refresh task.
     */
    public void start() {
        if (running.compareAndSet(false, true)) {
            executor.submit(this::refreshLoop);
            logger.info("Statistics task manager started");
        }
    }

    /**
     * Stop background task and wait for completion.
     */
    public void stop() {
        if (running.compareAndSet(true, false)) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                    logger.warn("Statistics task manager shutdown forced");
                } else {
                    logger.info("Statistics task manager stopped gracefully");
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
                logger.warn("Statistics task manager shutdown interrupted");
            }
        }
    }

    /**
     * Manually refresh statistics (outside the scheduled interval).
     */
    public void refreshNow() {
        if (running.get()) {
            executor.submit(this::performRefresh);
        }
    }

    /**
     * Get current statistics snapshot.
     */
    public StatisticsSnapshot getSnapshot() {
        return snapshot;
    }

    /**
     * Background refresh loop.
     */
    private void refreshLoop() {
        while (running.get()) {
            try {
                Thread.sleep(updateIntervalMillis);
                
                if (running.get()) {
                    performRefresh();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.debug("Statistics refresh loop interrupted");
                break;
            } catch (Exception e) {
                logger.error("Error in statistics refresh loop: {}", e.getMessage());
            }
        }
    }

    /**
     * Perform statistics refresh.
     */
    private void performRefresh() {
        try {
            var stats = statisticsService.getStatisticsSnapshot();
            
            // Update snapshot atomically
            snapshot.setCounter("totalUsers", ((Number) stats.getOrDefault("totalUsers", 0L)).longValue());
            snapshot.setCounter("totalRecipes", ((Number) stats.getOrDefault("totalRecipes", 0L)).longValue());
            snapshot.setCounter("pendingRecipes", ((Number) stats.getOrDefault("pendingRecipes", 0L)).longValue());
            snapshot.setCounter("approvedRecipes", ((Number) stats.getOrDefault("approvedRecipes", 0L)).longValue());
            snapshot.setCounter("rejectedRecipes", ((Number) stats.getOrDefault("rejectedRecipes", 0L)).longValue());
            snapshot.setCounter("totalRatings", ((Number) stats.getOrDefault("totalRatings", 0L)).longValue());
            snapshot.setCounter("totalComments", ((Number) stats.getOrDefault("totalComments", 0L)).longValue());
            
            logger.debug("Statistics refreshed: {}", snapshot);
        } catch (Exception e) {
            logger.error("Error refreshing statistics: {}", e.getMessage());
        }
    }

    /**
     * Check if manager is running.
     */
    public boolean isRunning() {
        return running.get();
    }
}
