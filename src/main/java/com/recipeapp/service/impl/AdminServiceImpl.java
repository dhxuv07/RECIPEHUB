package com.recipeapp.service.impl;

import com.recipeapp.model.Role;
import com.recipeapp.model.User;
import com.recipeapp.service.AdminService;
import com.recipeapp.service.RecipeService;
import com.recipeapp.service.StatisticsService;
import com.recipeapp.service.UserService;
import com.recipeapp.service.ActivityLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of AdminService.
 * 
 * Provides administrative operations for recipe moderation and user management.
 */
public class AdminServiceImpl implements AdminService {
    private static final Logger logger = LoggerFactory.getLogger(AdminServiceImpl.class);
    
    private final RecipeService recipeService;
    private final UserService userService;
    private final StatisticsService statisticsService;
    private final ActivityLogService activityLogService;

    public AdminServiceImpl(RecipeService recipeService, UserService userService,
                          StatisticsService statisticsService, ActivityLogService activityLogService) {
        this.recipeService = recipeService;
        this.userService = userService;
        this.statisticsService = statisticsService;
        this.activityLogService = activityLogService;
    }

    @Override
    public void approveRecipe(String recipeId) {
        recipeService.approveRecipe(recipeId);
        logger.info("Admin approved recipe: {}", recipeId);
    }

    @Override
    public void rejectRecipe(String recipeId) {
        recipeService.rejectRecipe(recipeId);
        logger.info("Admin rejected recipe: {}", recipeId);
    }

    @Override
    public void deleteUser(String userId) {
        userService.deleteUser(userId);
        logger.info("Admin deleted user: {}", userId);
    }

    @Override
    public User changeUserRole(String userId, Role newRole) {
        User updated = userService.changeRole(userId, newRole);
        logger.info("Admin changed user {} role to: {}", userId, newRole);
        return updated;
    }

    @Override
    public User activateUser(String userId) {
        User updated = userService.activateUser(userId);
        logger.info("Admin activated user: {}", userId);
        return updated;
    }

    @Override
    public User deactivateUser(String userId) {
        User updated = userService.deactivateUser(userId);
        logger.info("Admin deactivated user: {}", userId);
        return updated;
    }

    @Override
    public Map<String, Object> getStatistics() {
        return statisticsService.getStatisticsSnapshot();
    }

    @Override
    public Map<String, Object> getRecentActivity(int limit) {
        Map<String, Object> result = new HashMap<>();
        result.put("activities", activityLogService.getRecentActivity(limit));
        result.put("limit", limit);
        return result;
    }
}
