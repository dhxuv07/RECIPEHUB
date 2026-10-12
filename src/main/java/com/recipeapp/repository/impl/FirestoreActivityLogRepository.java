package com.recipeapp.repository.impl;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import com.recipeapp.config.FirebaseConfig;
import com.recipeapp.exception.DatabaseException;
import com.recipeapp.model.ActivityLog;
import com.recipeapp.model.ActivityType;
import com.recipeapp.repository.ActivityLogRepository;
import com.recipeapp.util.FirestoreMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Firestore implementation of ActivityLogRepository.
 * 
 * Handles all ActivityLog persistence operations against Firestore.
 */
public class FirestoreActivityLogRepository implements ActivityLogRepository {
    private static final Logger logger = LoggerFactory.getLogger(FirestoreActivityLogRepository.class);
    private static final String COLLECTION = "activityLogs";
    
    private final Firestore firestore;

    public FirestoreActivityLogRepository() {
        this.firestore = FirebaseConfig.getFirestore();
    }

    @Override
    public ActivityLog create(ActivityLog log) {
        try {
            log.validate();
            
            if (firestore.collection(COLLECTION).document(log.getId()).get().get().exists()) {
                throw new DatabaseException("ActivityLog with ID " + log.getId() + " already exists");
            }
            
            if (log.getTimestamp() == null) {
                log.setTimestamp(Instant.now());
            }
            
            firestore.collection(COLLECTION)
                    .document(log.getId())
                    .set(FirestoreMapper.activityLogToMap(log))
                    .get();
            
            logger.debug("Activity logged: {} by user: {}", log.getAction(), log.getUserId());
            return log;
        } catch (Exception e) {
            logger.error("Error creating activity log: {}", e.getMessage());
            throw new DatabaseException("Failed to create activity log: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<ActivityLog> findById(String id) {
        try {
            DocumentSnapshot snapshot = firestore.collection(COLLECTION)
                    .document(id)
                    .get()
                    .get();
            
            if (snapshot.exists()) {
                return Optional.of(FirestoreMapper.mapToActivityLog(snapshot));
            }
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Error finding activity log by ID: {}", e.getMessage());
            throw new DatabaseException("Failed to find activity log: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ActivityLog> findRecent(int limit) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(limit)
                    .get()
                    .get();
            
            return querySnapshot.getDocuments()
                    .stream()
                    .map(FirestoreMapper::mapToActivityLog)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error finding recent activity logs: {}", e.getMessage());
            throw new DatabaseException("Failed to find recent activity logs: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ActivityLog> findByUserId(String userId) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION)
                    .whereEqualTo("userId", userId)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .get();
            
            return querySnapshot.getDocuments()
                    .stream()
                    .map(FirestoreMapper::mapToActivityLog)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error finding activity logs by user: {}", e.getMessage());
            throw new DatabaseException("Failed to find activity logs: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ActivityLog> findByActionType(ActivityType actionType) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION)
                    .whereEqualTo("action", actionType.getValue())
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .get();
            
            return querySnapshot.getDocuments()
                    .stream()
                    .map(FirestoreMapper::mapToActivityLog)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error finding activity logs by action type: {}", e.getMessage());
            throw new DatabaseException("Failed to find activity logs: " + e.getMessage(), e);
        }
    }

    @Override
    public long count() {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION)
                    .get()
                    .get();
            
            return querySnapshot.size();
        } catch (Exception e) {
            logger.error("Error counting activity logs: {}", e.getMessage());
            throw new DatabaseException("Failed to count activity logs: " + e.getMessage(), e);
        }
    }
}
