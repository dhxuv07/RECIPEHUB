package com.recipeapp.model;

/**
 * User role enumeration.
 * 
 * USER: Regular user who can create recipes, rate, comment
 * ADMIN: Administrator who can moderate content and manage users
 */
public enum Role {
    USER("user"),
    ADMIN("admin");

    private final String value;

    Role(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Get Role from string value.
     * 
     * @param value the string value (case-insensitive)
     * @return Role enum value
     * @throws IllegalArgumentException if value is invalid
     */
    public static Role fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Role value cannot be null");
        }
        for (Role role : Role.values()) {
            if (role.value.equalsIgnoreCase(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Invalid role: " + value);
    }
}
