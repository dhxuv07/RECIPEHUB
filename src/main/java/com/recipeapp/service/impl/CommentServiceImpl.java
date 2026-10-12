package com.recipeapp.service.impl;

import com.recipeapp.exception.CommentNotFoundException;
import com.recipeapp.exception.DatabaseException;
import com.recipeapp.exception.UnauthorizedException;
import com.recipeapp.exception.ValidationException;
import com.recipeapp.model.ActivityType;
import com.recipeapp.model.Comment;
import com.recipeapp.repository.CommentRepository;
import com.recipeapp.service.ActivityLogService;
import com.recipeapp.service.CommentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of CommentService.
 */
public class CommentServiceImpl implements CommentService {
    private static final Logger logger = LoggerFactory.getLogger(CommentServiceImpl.class);
    
    private final CommentRepository commentRepository;
    private final ActivityLogService activityLogService;

    public CommentServiceImpl(CommentRepository commentRepository, ActivityLogService activityLogService) {
        this.commentRepository = commentRepository;
        this.activityLogService = activityLogService;
    }

    @Override
    public Comment addComment(Comment comment) {
        try {
            if (comment == null) {
                throw new ValidationException("Comment cannot be null");
            }
            
            comment.validate();
            
            if (comment.getId() == null || comment.getId().isBlank()) {
                comment.setId(UUID.randomUUID().toString());
            }
            
            Comment created = commentRepository.create(comment);
            
            activityLogService.logActivity(comment.getUserId(), ActivityType.COMMENT_CREATED,
                    "Comment added to recipe: " + comment.getRecipeId());
            
            logger.info("Comment created: {}", comment.getId());
            return created;
        } catch (ValidationException | DatabaseException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error adding comment: {}", e.getMessage());
            throw new DatabaseException("Failed to add comment: " + e.getMessage(), e);
        }
    }

    @Override
    public Comment updateComment(Comment comment, String userId) {
        try {
            if (comment == null) {
                throw new ValidationException("Comment cannot be null");
            }
            
            comment.validate();
            
            Comment existing = commentRepository.findById(comment.getId())
                    .orElseThrow(() -> new CommentNotFoundException("Comment not found: " + comment.getId()));
            
            // Ownership check
            if (!existing.getUserId().equals(userId)) {
                throw new UnauthorizedException("You cannot edit this comment");
            }
            
            comment.setCreatedAt(existing.getCreatedAt());
            comment.setUpdatedAt(Instant.now());
            
            Comment updated = commentRepository.update(comment);
            
            activityLogService.logActivity(userId, ActivityType.COMMENT_UPDATED,
                    "Comment updated");
            
            logger.info("Comment updated: {}", comment.getId());
            return updated;
        } catch (ValidationException | CommentNotFoundException | UnauthorizedException | DatabaseException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error updating comment: {}", e.getMessage());
            throw new DatabaseException("Failed to update comment: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteComment(String commentId, String userId) {
        try {
            Comment comment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new CommentNotFoundException("Comment not found: " + commentId));
            
            if (!comment.getUserId().equals(userId)) {
                throw new UnauthorizedException("You cannot delete this comment");
            }
            
            commentRepository.delete(commentId);
            
            activityLogService.logActivity(userId, ActivityType.COMMENT_DELETED,
                    "Comment deleted");
            
            logger.info("Comment deleted: {}", commentId);
        } catch (CommentNotFoundException | UnauthorizedException | DatabaseException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting comment: {}", e.getMessage());
            throw new DatabaseException("Failed to delete comment: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Comment> getCommentById(String id) {
        try {
            return commentRepository.findById(id);
        } catch (Exception e) {
            logger.error("Error retrieving comment: {}", e.getMessage());
            throw new DatabaseException("Failed to retrieve comment: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Comment> getCommentsForRecipe(String recipeId) {
        try {
            return commentRepository.findByRecipeId(recipeId);
        } catch (Exception e) {
            logger.error("Error retrieving comments: {}", e.getMessage());
            throw new DatabaseException("Failed to retrieve comments: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Comment> getCommentsByUser(String userId) {
        try {
            return commentRepository.findByUserId(userId);
        } catch (Exception e) {
            logger.error("Error retrieving user comments: {}", e.getMessage());
            throw new DatabaseException("Failed to retrieve comments: " + e.getMessage(), e);
        }
    }

    @Override
    public long countCommentsForRecipe(String recipeId) {
        try {
            return commentRepository.countByRecipeId(recipeId);
        } catch (Exception e) {
            logger.error("Error counting comments: {}", e.getMessage());
            throw new DatabaseException("Failed to count comments: " + e.getMessage(), e);
        }
    }

    @Override
    public long getTotalCommentCount() {
        try {
            return commentRepository.count();
        } catch (Exception e) {
            logger.error("Error counting total comments: {}", e.getMessage());
            throw new DatabaseException("Failed to count comments: " + e.getMessage(), e);
        }
    }
}
