package com.recipeapp.model;

/**
 * Activity log type enumeration.
 * 
 * Represents different types of actions that can be logged in the system.
 */
public enum ActivityType {
    USER_REGISTERED("user_registered"),
    RECIPE_CREATED("recipe_created"),
    RECIPE_UPDATED("recipe_updated"),
    RECIPE_DELETED("recipe_deleted"),
    RECIPE_APPROVED("recipe_approved"),
    RECIPE_REJECTED("recipe_rejected"),
    RATING_CREATED("rating_created"),
    RATING_UPDATED("rating_updated"),
    COMMENT_CREATED("comment_created"),
    COMMENT_UPDATED("comment_updated"),
    COMMENT_DELETED("comment_deleted"),
    PROFILE_UPDATED("profile_updated"),
    USER_DELETED("user_deleted"),
    USER_ROLE_UPDATED("user_role_updated"),
    USER_ACTIVATED("user_activated"),
    USER_DEACTIVATED("user_deactivated"),
    SETTINGS_UPDATED("settings_updated");

    private final String value;

    ActivityType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Get ActivityType from string value.
     * 
     * @param value the string value (case-insensitive)
     * @return ActivityType enum value
     * @throws IllegalArgumentException if value is invalid
     */
    public static ActivityType fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("ActivityType value cannot be null");
        }
        for (ActivityType type : ActivityType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid activity type: " + value);
    }
}
