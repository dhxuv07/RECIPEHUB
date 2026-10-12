package com.recipeapp.service.impl;

import com.recipeapp.exception.DatabaseException;
import com.recipeapp.exception.ValidationException;
import com.recipeapp.model.ActivityType;
import com.recipeapp.model.Rating;
import com.recipeapp.repository.RatingRepository;
import com.recipeapp.service.ActivityLogService;
import com.recipeapp.service.RatingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of RatingService.
 * 
 * Business rule: One rating per user per recipe.
 * Duplicate rating attempts update the existing rating.
 */
public class RatingServiceImpl implements RatingService {
    private static final Logger logger = LoggerFactory.getLogger(RatingServiceImpl.class);
    
    private final RatingRepository ratingRepository;
    private final ActivityLogService activityLogService;

    public RatingServiceImpl(RatingRepository ratingRepository, ActivityLogService activityLogService) {
        this.ratingRepository = ratingRepository;
        this.activityLogService = activityLogService;
    }

    @Override
    public Rating submitRating(Rating rating) {
        try {
            if (rating == null) {
                throw new ValidationException("Rating cannot be null");
            }
            
            rating.validate();
            
            // Ensure deterministic ID
            rating.setId(Rating.generateId(rating.getRecipeId(), rating.getUserId()));
            
            // Check if user already rated this recipe
            Optional<Rating> existing = ratingRepository.findByRecipeAndUser(
                    rating.getRecipeId(), 
                    rating.getUserId()
            );
            
            if (existing.isPresent()) {
                // Update existing rating
                return updateRating(rating);
            }
            
            // Create new rating
            Rating created = ratingRepository.create(rating);
            
            activityLogService.logActivity(rating.getUserId(), ActivityType.RATING_CREATED,
                    "Rating submitted: " + rating.getScore() + " stars");
            
            logger.info("Rating created: {}", rating.getId());
            return created;
        } catch (ValidationException | DatabaseException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error submitting rating: {}", e.getMessage());
            throw new DatabaseException("Failed to submit rating: " + e.getMessage(), e);
        }
    }

    @Override
    public Rating updateRating(Rating rating) {
        try {
            if (rating == null) {
                throw new ValidationException("Rating cannot be null");
            }
            
            rating.validate();
            rating.setUpdatedAt(Instant.now());
            
            Rating updated = ratingRepository.update(rating);
            
            activityLogService.logActivity(rating.getUserId(), ActivityType.RATING_UPDATED,
                    "Rating updated: " + rating.getScore() + " stars");
            
            logger.info("Rating updated: {}", rating.getId());
            return updated;
        } catch (ValidationException | DatabaseException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error updating rating: {}", e.getMessage());
            throw new DatabaseException("Failed to update rating: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Rating> getUserRating(String recipeId, String userId) {
        try {
            return ratingRepository.findByRecipeAndUser(recipeId, userId);
        } catch (Exception e) {
            logger.error("Error retrieving user rating: {}", e.getMessage());
            throw new DatabaseException("Failed to retrieve rating: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Rating> getRecipeRatings(String recipeId) {
        try {
            return ratingRepository.findByRecipeId(recipeId);
        } catch (Exception e) {
            logger.error("Error retrieving recipe ratings: {}", e.getMessage());
            throw new DatabaseException("Failed to retrieve ratings: " + e.getMessage(), e);
        }
    }

    @Override
    public double calculateAverageRating(String recipeId) {
        try {
            return ratingRepository.calculateAverageRating(recipeId);
        } catch (Exception e) {
            logger.error("Error calculating average rating: {}", e.getMessage());
            throw new DatabaseException("Failed to calculate average rating: " + e.getMessage(), e);
        }
    }

    @Override
    public long countRatingsForRecipe(String recipeId) {
        try {
            return ratingRepository.countByRecipeId(recipeId);
        } catch (Exception e) {
            logger.error("Error counting ratings: {}", e.getMessage());
            throw new DatabaseException("Failed to count ratings: " + e.getMessage(), e);
        }
    }

    @Override
    public long getTotalRatingCount() {
        try {
            return ratingRepository.count();
        } catch (Exception e) {
            logger.error("Error counting total ratings: {}", e.getMessage());
            throw new DatabaseException("Failed to count ratings: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteRating(String ratingId) {
        try {
            ratingRepository.delete(ratingId);
            logger.info("Rating deleted: {}", ratingId);
        } catch (Exception e) {
            logger.error("Error deleting rating: {}", e.getMessage());
            throw new DatabaseException("Failed to delete rating: " + e.getMessage(), e);
        }
    }
}
