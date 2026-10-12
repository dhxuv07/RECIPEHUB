package com.recipeapp.service.impl;

import com.recipeapp.exception.DatabaseException;
import com.recipeapp.model.RecipeStatus;
import com.recipeapp.service.CommentService;
import com.recipeapp.service.RatingService;
import com.recipeapp.service.RecipeService;
import com.recipeapp.service.StatisticsService;
import com.recipeapp.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of StatisticsService.
 * 
 * Provides system statistics for the admin dashboard.
 */
public class StatisticsServiceImpl implements StatisticsService {
    private static final Logger logger = LoggerFactory.getLogger(StatisticsServiceImpl.class);
    
    private final UserService userService;
    private final RecipeService recipeService;
    private final RatingService ratingService;
    private final CommentService commentService;

    public StatisticsServiceImpl(UserService userService, RecipeService recipeService,
                               RatingService ratingService, CommentService commentService) {
        this.userService = userService;
        this.recipeService = recipeService;
        this.ratingService = ratingService;
        this.commentService = commentService;
    }

    @Override
    public Map<String, Object> getStatisticsSnapshot() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            long totalUsers = userService.getTotalUserCount();
            long totalRecipes = recipeService.getTotalRecipeCount();
            long pendingRecipes = recipeService.countByStatus(RecipeStatus.PENDING);
            long approvedRecipes = recipeService.countByStatus(RecipeStatus.APPROVED);
            long rejectedRecipes = recipeService.countByStatus(RecipeStatus.REJECTED);
            long totalRatings = ratingService.getTotalRatingCount();
            long totalComments = commentService.getTotalCommentCount();
            double averageRating = calculateAverageRating();
            
            stats.put("totalUsers", totalUsers);
            stats.put("totalRecipes", totalRecipes);
            stats.put("pendingRecipes", pendingRecipes);
            stats.put("approvedRecipes", approvedRecipes);
            stats.put("rejectedRecipes", rejectedRecipes);
            stats.put("totalRatings", totalRatings);
            stats.put("totalComments", totalComments);
            stats.put("averageRating", averageRating);
            stats.put("timestamp", System.currentTimeMillis());
            
            logger.debug("Statistics snapshot generated");
            return stats;
        } catch (Exception e) {
            logger.error("Error generating statistics: {}", e.getMessage());
            throw new DatabaseException("Failed to generate statistics: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Long> getRecipeStatistics() {
        try {
            Map<String, Long> stats = new HashMap<>();
            stats.put("total", recipeService.getTotalRecipeCount());
            stats.put("pending", recipeService.countByStatus(RecipeStatus.PENDING));
            stats.put("approved", recipeService.countByStatus(RecipeStatus.APPROVED));
            stats.put("rejected", recipeService.countByStatus(RecipeStatus.REJECTED));
            return stats;
        } catch (Exception e) {
            logger.error("Error generating recipe statistics: {}", e.getMessage());
            throw new DatabaseException("Failed to generate statistics: " + e.getMessage(), e);
        }
    }

    @Override
    public long getTotalUsers() {
        try {
            return userService.getTotalUserCount();
        } catch (Exception e) {
            logger.error("Error getting total users: {}", e.getMessage());
            return 0;
        }
    }

    @Override
    public long getRecipeCount(RecipeStatus status) {
        try {
            return recipeService.countByStatus(status);
        } catch (Exception e) {
            logger.error("Error getting recipe count: {}", e.getMessage());
            return 0;
        }
    }

    @Override
    public long getTotalRecipes() {
        try {
            return recipeService.getTotalRecipeCount();
        } catch (Exception e) {
            logger.error("Error getting total recipes: {}", e.getMessage());
            return 0;
        }
    }

    @Override
    public long getTotalRatings() {
        try {
            return ratingService.getTotalRatingCount();
        } catch (Exception e) {
            logger.error("Error getting total ratings: {}", e.getMessage());
            return 0;
        }
    }

    @Override
    public long getTotalComments() {
        try {
            return commentService.getTotalCommentCount();
        } catch (Exception e) {
            logger.error("Error getting total comments: {}", e.getMessage());
            return 0;
        }
    }

    @Override
    public double getAverageRating() {
        try {
            return calculateAverageRating();
        } catch (Exception e) {
            logger.error("Error calculating average rating: {}", e.getMessage());
            return 0.0;
        }
    }

    @Override
    public void refreshStatistics() {
        // This would be called by background tasks
        logger.debug("Statistics refresh requested");
        try {
            Map<String, Object> snapshot = getStatisticsSnapshot();
            logger.info("Statistics refreshed: {} users, {} recipes", 
                    snapshot.get("totalUsers"), 
                    snapshot.get("totalRecipes"));
        } catch (Exception e) {
            logger.warn("Failed to refresh statistics: {}", e.getMessage());
        }
    }

    /**
     * Calculate average rating across all recipes.
     */
    private double calculateAverageRating() {
        try {
            // This is a simplification - could be more efficient with aggregation
            long totalRatings = ratingService.getTotalRatingCount();
            if (totalRatings == 0) {
                return 0.0;
            }
            
            // For now, return a placeholder
            // In production, this would aggregate actual rating scores
            return 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }
}
