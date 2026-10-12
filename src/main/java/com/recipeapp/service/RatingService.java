package com.recipeapp.service;

import com.recipeapp.model.Rating;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for Rating operations.
 * 
 * Handles business logic for recipe ratings.
 * 
 * Business rule: One rating per user per recipe.
 */
public interface RatingService {
    
    /**
     * Submit a rating for a recipe.
     * 
     * If user already rated this recipe, update the existing rating.
     * 
     * @param rating the rating to submit
     * @return the created or updated rating
     */
    Rating submitRating(Rating rating);
    
    /**
     * Update an existing rating.
     * 
     * @param rating the rating with updated data
     * @return the updated rating
     */
    Rating updateRating(Rating rating);
    
    /**
     * Get a user's rating for a specific recipe.
     */
    Optional<Rating> getUserRating(String recipeId, String userId);
    
    /**
     * Get all ratings for a recipe.
     */
    List<Rating> getRecipeRatings(String recipeId);
    
    /**
     * Calculate average rating for a recipe.
     * 
     * @param recipeId the recipe ID
     * @return average rating (0 if no ratings)
     */
    double calculateAverageRating(String recipeId);
    
    /**
     * Get count of ratings for a recipe.
     */
    long countRatingsForRecipe(String recipeId);
    
    /**
     * Get total rating count.
     */
    long getTotalRatingCount();
    
    /**
     * Delete a rating.
     */
    void deleteRating(String ratingId);
}
