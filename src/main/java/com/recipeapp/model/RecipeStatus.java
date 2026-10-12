package com.recipeapp.model;

/**
 * Recipe moderation status enumeration.
 * 
 * PENDING: Recipe submitted by user, awaiting admin approval
 * APPROVED: Recipe approved by admin, visible in public discovery
 * REJECTED: Recipe rejected by admin, kept in history with REJECTED status
 */
public enum RecipeStatus {
    PENDING("pending"),
    APPROVED("approved"),
    REJECTED("rejected");

    private final String value;

    RecipeStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Get RecipeStatus from string value.
     * 
     * @param value the string value (case-insensitive)
     * @return RecipeStatus enum value
     * @throws IllegalArgumentException if value is invalid
     */
    public static RecipeStatus fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("RecipeStatus value cannot be null");
        }
        for (RecipeStatus status : RecipeStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid recipe status: " + value);
    }
}
