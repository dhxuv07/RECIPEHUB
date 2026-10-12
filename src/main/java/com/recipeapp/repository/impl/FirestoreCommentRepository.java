package com.recipeapp.repository.impl;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.recipeapp.config.FirebaseConfig;
import com.recipeapp.exception.DatabaseException;
import com.recipeapp.model.Comment;
import com.recipeapp.repository.CommentRepository;
import com.recipeapp.util.FirestoreMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Firestore implementation of CommentRepository.
 * 
 * Handles all Comment persistence operations against Firestore.
 */
public class FirestoreCommentRepository implements CommentRepository {
    private static final Logger logger = LoggerFactory.getLogger(FirestoreCommentRepository.class);
    private static final String COLLECTION = "comments";
    
    private final Firestore firestore;

    public FirestoreCommentRepository() {
        this.firestore = FirebaseConfig.getFirestore();
    }

    @Override
    public Comment create(Comment comment) {
        try {
            comment.validate();
            
            if (firestore.collection(COLLECTION).document(comment.getId()).get().get().exists()) {
                throw new DatabaseException("Comment with ID " + comment.getId() + " already exists");
            }
            
            Instant now = Instant.now();
            comment.setCreatedAt(now);
            comment.setUpdatedAt(now);
            
            firestore.collection(COLLECTION)
                    .document(comment.getId())
                    .set(FirestoreMapper.commentToMap(comment))
                    .get();
            
            logger.info("Comment created: {}", comment.getId());
            return comment;
        } catch (Exception e) {
            logger.error("Error creating comment: {}", e.getMessage());
            throw new DatabaseException("Failed to create comment: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Comment> findById(String id) {
        try {
            DocumentSnapshot snapshot = firestore.collection(COLLECTION)
                    .document(id)
                    .get()
                    .get();
            
            if (snapshot.exists()) {
                return Optional.of(FirestoreMapper.mapToComment(snapshot));
            }
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Error finding comment by ID: {}", e.getMessage());
            throw new DatabaseException("Failed to find comment: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Comment> findByRecipeId(String recipeId) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION)
                    .whereEqualTo("recipeId", recipeId)
                    .get()
                    .get();
            
            return querySnapshot.getDocuments()
                    .stream()
                    .map(FirestoreMapper::mapToComment)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error finding comments by recipe: {}", e.getMessage());
            throw new DatabaseException("Failed to find comments: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Comment> findByUserId(String userId) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION)
                    .whereEqualTo("userId", userId)
                    .get()
                    .get();
            
            return querySnapshot.getDocuments()
                    .stream()
                    .map(FirestoreMapper::mapToComment)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error finding comments by user: {}", e.getMessage());
            throw new DatabaseException("Failed to find comments: " + e.getMessage(), e);
        }
    }

    @Override
    public Comment update(Comment comment) {
        try {
            comment.validate();
            
            if (!firestore.collection(COLLECTION).document(comment.getId()).get().get().exists()) {
                throw new DatabaseException("Comment with ID " + comment.getId() + " not found");
            }
            
            comment.setUpdatedAt(Instant.now());
            
            firestore.collection(COLLECTION)
                    .document(comment.getId())
                    .set(FirestoreMapper.commentToMap(comment))
                    .get();
            
            logger.info("Comment updated: {}", comment.getId());
            return comment;
        } catch (Exception e) {
            logger.error("Error updating comment: {}", e.getMessage());
            throw new DatabaseException("Failed to update comment: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String id) {
        try {
            firestore.collection(COLLECTION)
                    .document(id)
                    .delete()
                    .get();
            
            logger.info("Comment deleted: {}", id);
        } catch (Exception e) {
            logger.error("Error deleting comment: {}", e.getMessage());
            throw new DatabaseException("Failed to delete comment: " + e.getMessage(), e);
        }
    }

    @Override
    public long countByRecipeId(String recipeId) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION)
                    .whereEqualTo("recipeId", recipeId)
                    .get()
                    .get();
            
            return querySnapshot.size();
        } catch (Exception e) {
            logger.error("Error counting comments by recipe: {}", e.getMessage());
            throw new DatabaseException("Failed to count comments: " + e.getMessage(), e);
        }
    }

    @Override
    public long count() {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION)
                    .get()
                    .get();
            
            return querySnapshot.size();
        } catch (Exception e) {
            logger.error("Error counting comments: {}", e.getMessage());
            throw new DatabaseException("Failed to count comments: " + e.getMessage(), e);
        }
    }
}
