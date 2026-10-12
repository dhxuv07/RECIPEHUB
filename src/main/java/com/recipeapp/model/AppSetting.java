package com.recipeapp.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Application Setting domain model.
 * 
 * Represents a configuration setting for the application.
 */
public class AppSetting {
    private String id;
    private String key;
    private String value;
    private String description;
    private Instant updatedAt;
    private String updatedBy;

    public AppSetting() {
    }

    public AppSetting(String id, String key, String value, String description,
                      Instant updatedAt, String updatedBy) {
        this.id = id;
        this.key = key;
        this.value = value;
        this.description = description;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    /**
     * Validate setting data before persistence.
     * 
     * @throws IllegalArgumentException if validation fails
     */
    public void validate() {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Setting ID cannot be empty");
        }
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Setting key cannot be empty");
        }
        if (value == null) {
            throw new IllegalArgumentException("Setting value cannot be null");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppSetting that = (AppSetting) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "AppSetting{" +
                "id='" + id + '\'' +
                ", key='" + key + '\'' +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
