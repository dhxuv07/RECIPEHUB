package com.recipeapp.service;

import com.recipeapp.model.Recipe;
import com.recipeapp.model.RecipeStatus;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for Recipe operations.
 * 
 * Handles business logic for recipe management, moderation, and discovery.
 */
public interface RecipeService {
    
    /**
     * Create a new recipe (starts in PENDING status).
     * 
     * @param recipe the recipe to create
     * @param userId the current user ID (for authorization checks)
     * @return the created recipe
     */
    Recipe createRecipe(Recipe recipe, String userId);
    
    /**
     * Get recipe by ID.
     */
    Optional<Recipe> getRecipeById(String id);
    
    /**
     * Update an existing recipe.
     * 
     * @param recipe the recipe to update
     * @param userId the current user ID (for ownership check)
     * @return the updated recipe
     */
    Recipe updateRecipe(Recipe recipe, String userId);
    
    /**
     * Delete a recipe.
     * 
     * @param recipeId the recipe ID
     * @param userId the current user ID (for ownership check)
     */
    void deleteRecipe(String recipeId, String userId);
    
    /**
     * Get all recipes by author/user.
     */
    List<Recipe> getRecipesByUser(String userId);
    
    /**
     * Get all approved recipes (public discovery).
     */
    List<Recipe> getApprovedRecipes();
    
    /**
     * Get all pending recipes (for admin moderation).
     */
    List<Recipe> getPendingRecipes();
    
    /**
     * Get recipes by status.
     */
    List<Recipe> getRecipesByStatus(RecipeStatus status);
    
    /**
     * Search recipes by title.
     */
    List<Recipe> searchRecipes(String keyword);
    
    /**
     * Filter recipes by category.
     */
    List<Recipe> filterByCategory(String category);
    
    /**
     * Approve a recipe (admin only).
     * 
     * @param recipeId the recipe ID
     * @return the updated recipe
     */
    Recipe approveRecipe(String recipeId);
    
    /**
     * Reject a recipe (admin only).
     * 
     * @param recipeId the recipe ID
     * @return the updated recipe
     */
    Recipe rejectRecipe(String recipeId);
    
    /**
     * Get count of recipes by status.
     */
    long countByStatus(RecipeStatus status);
    
    /**
     * Get total recipe count.
     */
    long getTotalRecipeCount();
}
