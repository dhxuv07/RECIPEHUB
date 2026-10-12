package com.recipeapp.service;

import com.recipeapp.model.Role;
import com.recipeapp.model.User;

import java.util.Map;

/**
 * Service interface for Admin operations.
 * 
 * Handles administrative functionality like user management and recipe moderation.
 * 
 * Note: Authorization checks (verifying caller is admin) should be done in
 * the servlet/controller layer based on session principal.
 */
public interface AdminService {
    
    /**
     * Approve a recipe.
     */
    void approveRecipe(String recipeId);
    
    /**
     * Reject a recipe.
     */
    void rejectRecipe(String recipeId);
    
    /**
     * Delete a user (administrative deletion).
     */
    void deleteUser(String userId);
    
    /**
     * Change a user's role.
     */
    User changeUserRole(String userId, Role newRole);
    
    /**
     * Activate a user account.
     */
    User activateUser(String userId);
    
    /**
     * Deactivate a user account.
     */
    User deactivateUser(String userId);
    
    /**
     * Get system statistics.
     * 
     * @return map containing various statistics
     */
    Map<String, Object> getStatistics();
    
    /**
     * Get recent activity logs.
     * 
     * @param limit maximum number of entries
     * @return activity data
     */
    Map<String, Object> getRecentActivity(int limit);
}
