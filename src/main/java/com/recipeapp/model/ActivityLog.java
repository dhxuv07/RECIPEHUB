package com.recipeapp.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Activity Log domain model.
 * 
 * Represents a logged activity in the system.
 * Used for audit trails and analytics.
 */
public class ActivityLog {
    private String id;
    private String userId;
    private ActivityType action;
    private String details;
    private Instant timestamp;

    public ActivityLog() {
    }

    public ActivityLog(String id, String userId, ActivityType action, String details, Instant timestamp) {
        this.id = id;
        this.userId = userId;
        this.action = action;
        this.details = details;
        this.timestamp = timestamp;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public ActivityType getAction() {
        return action;
    }

    public String getDetails() {
        return details;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setAction(ActivityType action) {
        this.action = action;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Validate activity log data before persistence.
     * 
     * @throws IllegalArgumentException if validation fails
     */
    public void validate() {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Activity log ID cannot be empty");
        }
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID cannot be empty");
        }
        if (action == null) {
            throw new IllegalArgumentException("Activity action cannot be null");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActivityLog that = (ActivityLog) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ActivityLog{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", action=" + action +
                ", timestamp=" + timestamp +
                '}';
    }
}
