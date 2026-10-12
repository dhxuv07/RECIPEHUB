package com.recipeapp.repository;

import com.recipeapp.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User persistence operations.
 */
public interface UserRepository {
    
    /**
     * Create a new user.
     */
    User create(User user);
    
    /**
     * Find a user by ID.
     */
    Optional<User> findById(String id);
    
    /**
     * Find a user by email.
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Find all users.
     */
    List<User> findAll();
    
    /**
     * Update an existing user.
     */
    User update(User user);
    
    /**
     * Delete a user by ID.
     */
    void delete(String id);
    
    /**
     * Check if a user exists by email.
     */
    boolean existsByEmail(String email);
    
    /**
     * Count total number of users.
     */
    long count();
}
