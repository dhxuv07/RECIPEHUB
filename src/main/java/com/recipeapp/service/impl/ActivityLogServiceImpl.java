package com.recipeapp.service.impl;

import com.recipeapp.exception.DatabaseException;
import com.recipeapp.model.ActivityLog;
import com.recipeapp.model.ActivityType;
import com.recipeapp.repository.ActivityLogRepository;
import com.recipeapp.service.ActivityLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of ActivityLogService.
 * 
 * Logs user actions and system events for audit trails.
 */
public class ActivityLogServiceImpl implements ActivityLogService {
    private static final Logger logger = LoggerFactory.getLogger(ActivityLogServiceImpl.class);
    
    private final ActivityLogRepository activityLogRepository;

    public ActivityLogServiceImpl(ActivityLogRepository activityLogRepository) {
        this.activityLogRepository = activityLogRepository;
    }

    @Override
    public void logActivity(String userId, ActivityType actionType, String details) {
        try {
            ActivityLog log = new ActivityLog();
            log.setId(UUID.randomUUID().toString());
            log.setUserId(userId);
            log.setAction(actionType);
            log.setDetails(details);
            log.setTimestamp(Instant.now());
            
            activityLogRepository.create(log);
        } catch (Exception e) {
            logger.warn("Failed to log activity (non-fatal): {}", e.getMessage());
            // Don't throw exception for logging failures - logging shouldn't break main operations
        }
    }

    @Override
    public List<ActivityLog> getRecentActivity(int limit) {
        try {
            return activityLogRepository.findRecent(limit);
        } catch (Exception e) {
            logger.error("Error retrieving recent activity: {}", e.getMessage());
            throw new DatabaseException("Failed to retrieve activity: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ActivityLog> getActivityByUser(String userId) {
        try {
            return activityLogRepository.findByUserId(userId);
        } catch (Exception e) {
            logger.error("Error retrieving user activity: {}", e.getMessage());
            throw new DatabaseException("Failed to retrieve activity: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ActivityLog> getActivityByType(ActivityType actionType) {
        try {
            return activityLogRepository.findByActionType(actionType);
        } catch (Exception e) {
            logger.error("Error retrieving activity by type: {}", e.getMessage());
            throw new DatabaseException("Failed to retrieve activity: " + e.getMessage(), e);
        }
    }
}
