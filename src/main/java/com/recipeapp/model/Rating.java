package com.recipeapp.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Rating domain model.
 * 
 * Represents a user's rating for a recipe.
 * 
 * Business rule:
 * - A user can have only ONE rating for a particular recipe
 * - Score must be between 1 and 5 (inclusive)
 * - Duplicate rating attempts should update the existing rating
 */
public class Rating {
    private String id;
    private String recipeId;
    private String userId;
    private int score;
    private Instant createdAt;
    private Instant updatedAt;

    public Rating() {
    }

    public Rating(String id, String recipeId, String userId, int score, 
                  Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.recipeId = recipeId;
        this.userId = userId;
        this.score = score;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getRecipeId() {
        return recipeId;
    }

    public String getUserId() {
        return userId;
    }

    public int getScore() {
        return score;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Validate rating data before persistence.
     * 
     * @throws IllegalArgumentException if validation fails
     */
    public void validate() {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Rating ID cannot be empty");
        }
        if (recipeId == null || recipeId.isBlank()) {
            throw new IllegalArgumentException("Recipe ID cannot be empty");
        }
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID cannot be empty");
        }
        if (score < 1 || score > 5) {
            throw new IllegalArgumentException("Rating score must be between 1 and 5, got: " + score);
        }
    }

    /**
     * Generate a deterministic ID based on recipe and user.
     * 
     * Format: recipeId_userId
     * This ensures only one rating per user per recipe.
     * 
     * @param recipeId the recipe ID
     * @param userId the user ID
     * @return the deterministic rating ID
     */
    public static String generateId(String recipeId, String userId) {
        return recipeId + "_" + userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rating rating = (Rating) o;
        return Objects.equals(id, rating.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Rating{" +
                "id='" + id + '\'' +
                ", recipeId='" + recipeId + '\'' +
                ", userId='" + userId + '\'' +
                ", score=" + score +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
