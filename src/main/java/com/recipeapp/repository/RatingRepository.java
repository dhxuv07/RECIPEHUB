package com.recipeapp.repository;

import com.recipeapp.model.Rating;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Rating persistence operations.
 */
public interface RatingRepository {
    
    /**
     * Create a new rating.
     */
    Rating create(Rating rating);
    
    /**
     * Find a rating by ID.
     */
    Optional<Rating> findById(String id);
    
    /**
     * Find a rating by recipe and user (ensuring one rating per user per recipe).
     */
    Optional<Rating> findByRecipeAndUser(String recipeId, String userId);
    
    /**
     * Find all ratings for a recipe.
     */
    List<Rating> findByRecipeId(String recipeId);
    
    /**
     * Find all ratings by a user.
     */
    List<Rating> findByUserId(String userId);
    
    /**
     * Update an existing rating.
     */
    Rating update(Rating rating);
    
    /**
     * Delete a rating by ID.
     */
    void delete(String id);
    
    /**
     * Calculate average rating for a recipe.
     * 
     * @param recipeId the recipe ID
     * @return average rating, or 0 if no ratings exist
     */
    double calculateAverageRating(String recipeId);
    
    /**
     * Count ratings for a recipe.
     */
    long countByRecipeId(String recipeId);
    
    /**
     * Count total number of ratings.
     */
    long count();
}
