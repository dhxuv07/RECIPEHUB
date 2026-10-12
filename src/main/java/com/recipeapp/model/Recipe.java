package com.recipeapp.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Recipe domain model.
 * 
 * Represents a recipe in the Recipe Sharing Platform.
 * 
 * Recipes go through moderation:
 * PENDING -> APPROVED or REJECTED
 * 
 * Only APPROVED recipes are publicly discoverable.
 */
public class Recipe {
    private String id;
    private String authorId;
    private String title;
    private String description;
    private String category;
    private List<String> ingredients;
    private List<String> instructions;
    private String imageUrl;
    private RecipeStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    public Recipe() {
    }

    public Recipe(String id, String authorId, String title, String description,
                  String category, List<String> ingredients, List<String> instructions,
                  String imageUrl, RecipeStatus status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.authorId = authorId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.ingredients = ingredients;
        this.instructions = instructions;
        this.imageUrl = imageUrl;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getAuthorId() {
        return authorId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public List<String> getIngredients() {
        return ingredients;
    }

    public List<String> getInstructions() {
        return instructions;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public RecipeStatus getStatus() {
        return status;
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

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setIngredients(List<String> ingredients) {
        this.ingredients = ingredients;
    }

    public void setInstructions(List<String> instructions) {
        this.instructions = instructions;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setStatus(RecipeStatus status) {
        this.status = status;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Validate recipe data before persistence.
     * 
     * @throws IllegalArgumentException if validation fails
     */
    public void validate() {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Recipe ID cannot be empty");
        }
        if (authorId == null || authorId.isBlank()) {
            throw new IllegalArgumentException("Recipe author ID cannot be empty");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Recipe title cannot be empty");
        }
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("Recipe category cannot be empty");
        }
        if (instructions == null || instructions.isEmpty()) {
            throw new IllegalArgumentException("Recipe must have at least one instruction");
        }
        if (status == null) {
            throw new IllegalArgumentException("Recipe status cannot be null");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Recipe recipe = (Recipe) o;
        return Objects.equals(id, recipe.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Recipe{" +
                "id='" + id + '\'' +
                ", authorId='" + authorId + '\'' +
                ", title='" + title + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}
