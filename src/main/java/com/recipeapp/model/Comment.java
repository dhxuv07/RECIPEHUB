package com.recipeapp.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Comment domain model.
 * 
 * Represents a comment on a recipe.
 * 
 * Business rules:
 * - Comments must have non-empty content
 * - Users can edit/delete their own comments
 * - Admins can moderate/delete comments
 */
public class Comment {
    private String id;
    private String recipeId;
    private String userId;
    private String content;
    private Instant createdAt;
    private Instant updatedAt;

    public Comment() {
    }

    public Comment(String id, String recipeId, String userId, String content,
                   Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.recipeId = recipeId;
        this.userId = userId;
        this.content = content;
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

    public String getContent() {
        return content;
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

    public void setContent(String content) {
        this.content = content;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Validate comment data before persistence.
     * 
     * @throws IllegalArgumentException if validation fails
     */
    public void validate() {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Comment ID cannot be empty");
        }
        if (recipeId == null || recipeId.isBlank()) {
            throw new IllegalArgumentException("Recipe ID cannot be empty");
        }
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID cannot be empty");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Comment content cannot be empty");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Comment comment = (Comment) o;
        return Objects.equals(id, comment.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id='" + id + '\'' +
                ", recipeId='" + recipeId + '\'' +
                ", userId='" + userId + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
