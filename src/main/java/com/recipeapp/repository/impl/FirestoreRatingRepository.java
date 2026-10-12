package com.recipeapp.repository.impl;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.recipeapp.config.FirebaseConfig;
import com.recipeapp.exception.DatabaseException;
import com.recipeapp.model.Rating;
import com.recipeapp.repository.RatingRepository;
import com.recipeapp.util.FirestoreMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Firestore implementation of RatingRepository.
 * 
 * Handles all Rating persistence operations against Firestore.
 * 
 * Note: Uses deterministic ID (recipeId_userId) to ensure one rating per user per recipe.
 */
public class FirestoreRatingRepository implements RatingRepository {
    private static final Logger logger = LoggerFactory.getLogger(FirestoreRatingRepository.class);
    private static final String COLLECTION = "ratings";
    
    private final Firestore firestore;

    public FirestoreRatingRepository() {
        this.firestore = FirebaseConfig.getFirestore();
    }

    @Override
    public Rating create(Rating rating) {
        try {
            rating.validate();
            
            // Ensure deterministic ID format
            if (rating.getId() == null) {
                rating.setId(Rating.generateId(rating.getRecipeId(), rating.getUserId()));
            }
            
            Instant now = Instant.now();
            rating.setCreatedAt(now);
            rating.setUpdatedAt(now);
            
            firestore.collection(COLLECTION)
                    .document(rating.getId())
                    .set(FirestoreMapper.ratingToMap(rating))
                    .get();
            
            logger.info("Rating created: {}", rating.getId());
            return rating;
        } catch (Exception e) {
            logger.error("Error creating rating: {}", e.getMessage());
            throw new DatabaseException("Failed to create rating: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Rating> findById(String id) {
        try {
            DocumentSnapshot snapshot = firestore.collection(COLLECTION)
                    .document(id)
                    .get()
                    .get();
            
            if (snapshot.exists()) {
                return Optional.of(FirestoreMapper.mapToRating(snapshot));
            }
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Error finding rating by ID: {}", e.getMessage());
            throw new DatabaseException("Failed to find rating: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Rating> findByRecipeAndUser(String recipeId, String userId) {
        try {
            String id = Rating.generateId(recipeId, userId);
            return findById(id);
        } catch (Exception e) {
            logger.error("Error finding rating by recipe and user: {}", e.getMessage());
            throw new DatabaseException("Failed to find rating: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Rating> findByRecipeId(String recipeId) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION)
                    .whereEqualTo("recipeId", recipeId)
                    .get()
                    .get();
            
            return querySnapshot.getDocuments()
                    .stream()
                    .map(FirestoreMapper::mapToRating)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error finding ratings by recipe: {}", e.getMessage());
            throw new DatabaseException("Failed to find ratings: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Rating> findByUserId(String userId) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION)
                    .whereEqualTo("userId", userId)
                    .get()
                    .get();
            
            return querySnapshot.getDocuments()
                    .stream()
                    .map(FirestoreMapper::mapToRating)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error finding ratings by user: {}", e.getMessage());
            throw new DatabaseException("Failed to find ratings: " + e.getMessage(), e);
        }
    }

    @Override
    public Rating update(Rating rating) {
        try {
            rating.validate();
            
            if (!firestore.collection(COLLECTION).document(rating.getId()).get().get().exists()) {
                throw new DatabaseException("Rating with ID " + rating.getId() + " not found");
            }
            
            rating.setUpdatedAt(Instant.now());
            
            firestore.collection(COLLECTION)
                    .document(rating.getId())
                    .set(FirestoreMapper.ratingToMap(rating))
                    .get();
            
            logger.info("Rating updated: {}", rating.getId());
            return rating;
        } catch (Exception e) {
            logger.error("Error updating rating: {}", e.getMessage());
            throw new DatabaseException("Failed to update rating: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String id) {
        try {
            firestore.collection(COLLECTION)
                    .document(id)
                    .delete()
                    .get();
            
            logger.info("Rating deleted: {}", id);
        } catch (Exception e) {
            logger.error("Error deleting rating: {}", e.getMessage());
            throw new DatabaseException("Failed to delete rating: " + e.getMessage(), e);
        }
    }

    @Override
    public double calculateAverageRating(String recipeId) {
        try {
            List<Rating> ratings = findByRecipeId(recipeId);
            
            if (ratings.isEmpty()) {
                return 0.0;
            }
            
            double average = ratings.stream()
                    .mapToInt(Rating::getScore)
                    .average()
                    .orElse(0.0);
            
            return Math.round(average * 100.0) / 100.0; // Round to 2 decimal places
        } catch (Exception e) {
            logger.error("Error calculating average rating: {}", e.getMessage());
            throw new DatabaseException("Failed to calculate average rating: " + e.getMessage(), e);
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
            logger.error("Error counting ratings by recipe: {}", e.getMessage());
            throw new DatabaseException("Failed to count ratings: " + e.getMessage(), e);
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
            logger.error("Error counting ratings: {}", e.getMessage());
            throw new DatabaseException("Failed to count ratings: " + e.getMessage(), e);
        }
    }
}
