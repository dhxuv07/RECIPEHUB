package com.recipeapp.repository;

import com.recipeapp.model.Comment;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Comment persistence operations.
 */
public interface CommentRepository {
    
    /**
     * Create a new comment.
     */
    Comment create(Comment comment);
    
    /**
     * Find a comment by ID.
     */
    Optional<Comment> findById(String id);
    
    /**
     * Find all comments for a recipe.
     */
    List<Comment> findByRecipeId(String recipeId);
    
    /**
     * Find all comments by a user.
     */
    List<Comment> findByUserId(String userId);
    
    /**
     * Update an existing comment.
     */
    Comment update(Comment comment);
    
    /**
     * Delete a comment by ID.
     */
    void delete(String id);
    
    /**
     * Count comments for a recipe.
     */
    long countByRecipeId(String recipeId);
    
    /**
     * Count total number of comments.
     */
    long count();
}
