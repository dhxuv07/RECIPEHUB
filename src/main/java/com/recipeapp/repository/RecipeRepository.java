package com.recipeapp.repository;

import com.recipeapp.model.Recipe;
import com.recipeapp.model.RecipeStatus;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Recipe persistence operations.
 */
public interface RecipeRepository {
    
    /**
     * Create a new recipe.
     */
    Recipe create(Recipe recipe);
    
    /**
     * Find a recipe by ID.
     */
    Optional<Recipe> findById(String id);
    
    /**
     * Find all recipes by author ID.
     */
    List<Recipe> findByAuthorId(String authorId);
    
    /**
     * Find all recipes.
     */
    List<Recipe> findAll();
    
    /**
     * Find all approved recipes.
     */
    List<Recipe> findApproved();
    
    /**
     * Find all pending recipes.
     */
    List<Recipe> findPending();
    
    /**
     * Find all rejected recipes.
     */
    List<Recipe> findByStatus(RecipeStatus status);
    
    /**
     * Search recipes by title (case-insensitive substring match).
     */
    List<Recipe> searchByTitle(String keyword);
    
    /**
     * Find recipes by category.
     */
    List<Recipe> findByCategory(String category);
    
    /**
     * Update an existing recipe.
     */
    Recipe update(Recipe recipe);
    
    /**
     * Delete a recipe by ID.
     */
    void delete(String id);
    
    /**
     * Count recipes by status.
     */
    long countByStatus(RecipeStatus status);
    
    /**
     * Count total number of recipes.
     */
    long count();
}
