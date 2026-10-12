package com.recipeapp.service;

import com.recipeapp.model.Comment;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for Comment operations.
 * 
 * Handles business logic for recipe comments.
 */
public interface CommentService {
    
    /**
     * Add a comment to a recipe.
     * 
     * @param comment the comment to add
     * @return the created comment
     */
    Comment addComment(Comment comment);
    
    /**
     * Update a comment.
     * 
     * @param comment the comment with updated data
     * @param userId the current user ID (for ownership check)
     * @return the updated comment
     */
    Comment updateComment(Comment comment, String userId);
    
    /**
     * Delete a comment.
     * 
     * @param commentId the comment ID
     * @param userId the current user ID (for ownership check)
     */
    void deleteComment(String commentId, String userId);
    
    /**
     * Get a comment by ID.
     */
    Optional<Comment> getCommentById(String id);
    
    /**
     * Get all comments for a recipe.
     */
    List<Comment> getCommentsForRecipe(String recipeId);
    
    /**
     * Get comments by user.
     */
    List<Comment> getCommentsByUser(String userId);
    
    /**
     * Get count of comments for a recipe.
     */
    long countCommentsForRecipe(String recipeId);
    
    /**
     * Get total comment count.
     */
    long getTotalCommentCount();
}
