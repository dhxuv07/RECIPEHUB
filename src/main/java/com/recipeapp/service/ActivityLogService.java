package com.recipeapp.service;

import com.recipeapp.model.ActivityLog;
import com.recipeapp.model.ActivityType;

import java.util.List;

/**
 * Service interface for Activity Logging.
 * 
 * Handles logging of user actions and system events for audit trails.
 */
public interface ActivityLogService {
    
    /**
     * Log an activity.
     * 
     * @param userId the user ID
     * @param actionType the activity type
     * @param details optional details about the activity
     */
    void logActivity(String userId, ActivityType actionType, String details);
    
    /**
     * Get recent activity logs.
     * 
     * @param limit maximum number of entries
     * @return list of recent activities
     */
    List<ActivityLog> getRecentActivity(int limit);
    
    /**
     * Get activity logs for a user.
     */
    List<ActivityLog> getActivityByUser(String userId);
    
    /**
     * Get activity logs by action type.
     */
    List<ActivityLog> getActivityByType(ActivityType actionType);
}
