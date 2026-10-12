package com.recipeapp.repository;

import com.recipeapp.model.ActivityLog;
import com.recipeapp.model.ActivityType;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ActivityLog persistence operations.
 */
public interface ActivityLogRepository {
    
    /**
     * Create a new activity log entry.
     */
    ActivityLog create(ActivityLog log);
    
    /**
     * Find an activity log by ID.
     */
    Optional<ActivityLog> findById(String id);
    
    /**
     * Find recent activity logs (sorted by timestamp, most recent first).
     * 
     * @param limit maximum number of entries to return
     * @return list of recent activity logs
     */
    List<ActivityLog> findRecent(int limit);
    
    /**
     * Find all activity logs for a user.
     */
    List<ActivityLog> findByUserId(String userId);
    
    /**
     * Find activity logs by action type.
     */
    List<ActivityLog> findByActionType(ActivityType actionType);
    
    /**
     * Count total number of activity logs.
     */
    long count();
}
