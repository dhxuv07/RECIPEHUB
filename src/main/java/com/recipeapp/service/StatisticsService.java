package com.recipeapp.service;

import com.recipeapp.model.RecipeStatus;

import java.util.Map;

/**
 * Service interface for Statistics.
 * 
 * Handles calculation and management of system statistics for the dashboard.
 */
public interface StatisticsService {
    
    /**
     * Get a snapshot of current statistics.
     * 
     * @return map containing statistics snapshot
     */
    Map<String, Object> getStatisticsSnapshot();
    
    /**
     * Get recipe statistics.
     * 
     * @return map with recipe counts by status
     */
    Map<String, Long> getRecipeStatistics();
    
    /**
     * Get total user count.
     */
    long getTotalUsers();
    
    /**
     * Get count of recipes by status.
     */
    long getRecipeCount(RecipeStatus status);
    
    /**
     * Get total recipe count.
     */
    long getTotalRecipes();
    
    /**
     * Get total rating count.
     */
    long getTotalRatings();
    
    /**
     * Get total comment count.
     */
    long getTotalComments();
    
    /**
     * Get average rating across all recipes.
     */
    double getAverageRating();
    
    /**
     * Refresh statistics (for background updates).
     */
    void refreshStatistics();
}
