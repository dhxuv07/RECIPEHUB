package com.recipeapp.service;

import com.recipeapp.model.Role;
import com.recipeapp.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for User operations.
 * 
 * Handles business logic for user management.
 */
public interface UserService {
    
    /**
     * Create a new user.
     * 
     * @param user the user to create
     * @return the created user
     * @throws IllegalArgumentException if email already exists
     */
    User createUser(User user);
    
    /**
     * Get user by ID.
     */
    Optional<User> getUserById(String id);
    
    /**
     * Get user by email.
     */
    Optional<User> getUserByEmail(String email);
    
    /**
     * Update an existing user.
     */
    User updateUser(User user);
    
    /**
     * Delete a user.
     */
    void deleteUser(String id);
    
    /**
     * List all users.
     */
    List<User> listUsers();
    
    /**
     * Change user role.
     * 
     * @param userId the user ID
     * @param newRole the new role
     * @return the updated user
     */
    User changeRole(String userId, Role newRole);
    
    /**
     * Activate a user account.
     */
    User activateUser(String userId);
    
    /**
     * Deactivate a user account.
     */
    User deactivateUser(String userId);
    
    /**
     * Check if user email already exists.
     */
    boolean emailExists(String email);
    
    /**
     * Get total user count.
     */
    long getTotalUserCount();
}
