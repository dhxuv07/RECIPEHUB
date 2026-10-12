package com.recipeapp.util;

import com.google.cloud.firestore.DocumentSnapshot;
import com.recipeapp.exception.ValidationException;
import com.recipeapp.model.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Firestore Mapper utility for converting between Java domain objects and Firestore documents.
 * 
 * This class handles all conversions between:
 * - Java model objects (User, Recipe, etc.)
 * - Firestore document maps
 * - DocumentSnapshot objects
 * 
 * This centralized approach prevents Firestore-specific logic from leaking into service classes.
 */
public class FirestoreMapper {

    // ==================== USER MAPPING ====================

    /**
     * Convert User object to Firestore document map.
     */
    public static Map<String, Object> userToMap(User user) {
        if (user == null) {
            throw new ValidationException("User cannot be null");
        }
        
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("name", user.getName());
        map.put("email", user.getEmail());
        map.put("role", user.getRole().getValue());
        map.put("active", user.isActive());
        map.put("createdAt", user.getCreatedAt());
        map.put("updatedAt", user.getUpdatedAt());
        return map;
    }

    /**
     * Convert Firestore DocumentSnapshot to User object.
     */
    public static User mapToUser(DocumentSnapshot snapshot) {
        if (snapshot == null || !snapshot.exists()) {
            throw new ValidationException("Document snapshot is null or does not exist");
        }
        
        try {
            User user = new User();
            user.setId(snapshot.getString("id"));
            user.setName(snapshot.getString("name"));
            user.setEmail(snapshot.getString("email"));
            
            String roleValue = snapshot.getString("role");
            user.setRole(Role.fromValue(roleValue));
            
            Boolean active = snapshot.getBoolean("active");
            user.setActive(active != null ? active : false);
            
            com.google.cloud.Timestamp createdTs = snapshot.getTimestamp("createdAt");
            if (createdTs != null) {
                user.setCreatedAt(createdTs.toDate().toInstant());
            }
            
            com.google.cloud.Timestamp updatedTs = snapshot.getTimestamp("updatedAt");
            if (updatedTs != null) {
                user.setUpdatedAt(updatedTs.toDate().toInstant());
            }
            
            return user;
        } catch (Exception e) {
            throw new ValidationException("Error mapping User from Firestore: " + e.getMessage(), e);
        }
    }

    // ==================== RECIPE MAPPING ====================

    /**
     * Convert Recipe object to Firestore document map.
     */
    public static Map<String, Object> recipeToMap(Recipe recipe) {
        if (recipe == null) {
            throw new ValidationException("Recipe cannot be null");
        }
        
        Map<String, Object> map = new HashMap<>();
        map.put("id", recipe.getId());
        map.put("authorId", recipe.getAuthorId());
        map.put("title", recipe.getTitle());
        map.put("description", recipe.getDescription());
        map.put("category", recipe.getCategory());
        map.put("ingredients", recipe.getIngredients());
        map.put("instructions", recipe.getInstructions());
        map.put("imageUrl", recipe.getImageUrl());
        map.put("status", recipe.getStatus().getValue());
        map.put("createdAt", recipe.getCreatedAt());
        map.put("updatedAt", recipe.getUpdatedAt());
        return map;
    }

    /**
     * Convert Firestore DocumentSnapshot to Recipe object.
     */
    public static Recipe mapToRecipe(DocumentSnapshot snapshot) {
        if (snapshot == null || !snapshot.exists()) {
            throw new ValidationException("Document snapshot is null or does not exist");
        }
        
        try {
            Recipe recipe = new Recipe();
            recipe.setId(snapshot.getString("id"));
            recipe.setAuthorId(snapshot.getString("authorId"));
            recipe.setTitle(snapshot.getString("title"));
            recipe.setDescription(snapshot.getString("description"));
            recipe.setCategory(snapshot.getString("category"));
            recipe.setIngredients((List<String>) snapshot.get("ingredients"));
            recipe.setInstructions((List<String>) snapshot.get("instructions"));
            recipe.setImageUrl(snapshot.getString("imageUrl"));
            
            String statusValue = snapshot.getString("status");
            recipe.setStatus(RecipeStatus.fromValue(statusValue));
            
            com.google.cloud.Timestamp createdTs = snapshot.getTimestamp("createdAt");
            if (createdTs != null) {
                recipe.setCreatedAt(createdTs.toDate().toInstant());
            }
            
            com.google.cloud.Timestamp updatedTs = snapshot.getTimestamp("updatedAt");
            if (updatedTs != null) {
                recipe.setUpdatedAt(updatedTs.toDate().toInstant());
            }
            
            return recipe;
        } catch (Exception e) {
            throw new ValidationException("Error mapping Recipe from Firestore: " + e.getMessage(), e);
        }
    }

    // ==================== RATING MAPPING ====================

    /**
     * Convert Rating object to Firestore document map.
     */
    public static Map<String, Object> ratingToMap(Rating rating) {
        if (rating == null) {
            throw new ValidationException("Rating cannot be null");
        }
        
        Map<String, Object> map = new HashMap<>();
        map.put("id", rating.getId());
        map.put("recipeId", rating.getRecipeId());
        map.put("userId", rating.getUserId());
        map.put("score", rating.getScore());
        map.put("createdAt", rating.getCreatedAt());
        map.put("updatedAt", rating.getUpdatedAt());
        return map;
    }

    /**
     * Convert Firestore DocumentSnapshot to Rating object.
     */
    public static Rating mapToRating(DocumentSnapshot snapshot) {
        if (snapshot == null || !snapshot.exists()) {
            throw new ValidationException("Document snapshot is null or does not exist");
        }
        
        try {
            Rating rating = new Rating();
            rating.setId(snapshot.getString("id"));
            rating.setRecipeId(snapshot.getString("recipeId"));
            rating.setUserId(snapshot.getString("userId"));
            
            Long score = snapshot.getLong("score");
            rating.setScore(score != null ? score.intValue() : 0);
            
            com.google.cloud.Timestamp createdTs = snapshot.getTimestamp("createdAt");
            if (createdTs != null) {
                rating.setCreatedAt(createdTs.toDate().toInstant());
            }
            
            com.google.cloud.Timestamp updatedTs = snapshot.getTimestamp("updatedAt");
            if (updatedTs != null) {
                rating.setUpdatedAt(updatedTs.toDate().toInstant());
            }
            
            return rating;
        } catch (Exception e) {
            throw new ValidationException("Error mapping Rating from Firestore: " + e.getMessage(), e);
        }
    }

    // ==================== COMMENT MAPPING ====================

    /**
     * Convert Comment object to Firestore document map.
     */
    public static Map<String, Object> commentToMap(Comment comment) {
        if (comment == null) {
            throw new ValidationException("Comment cannot be null");
        }
        
        Map<String, Object> map = new HashMap<>();
        map.put("id", comment.getId());
        map.put("recipeId", comment.getRecipeId());
        map.put("userId", comment.getUserId());
        map.put("content", comment.getContent());
        map.put("createdAt", comment.getCreatedAt());
        map.put("updatedAt", comment.getUpdatedAt());
        return map;
    }

    /**
     * Convert Firestore DocumentSnapshot to Comment object.
     */
    public static Comment mapToComment(DocumentSnapshot snapshot) {
        if (snapshot == null || !snapshot.exists()) {
            throw new ValidationException("Document snapshot is null or does not exist");
        }
        
        try {
            Comment comment = new Comment();
            comment.setId(snapshot.getString("id"));
            comment.setRecipeId(snapshot.getString("recipeId"));
            comment.setUserId(snapshot.getString("userId"));
            comment.setContent(snapshot.getString("content"));
            
            com.google.cloud.Timestamp createdTs = snapshot.getTimestamp("createdAt");
            if (createdTs != null) {
                comment.setCreatedAt(createdTs.toDate().toInstant());
            }
            
            com.google.cloud.Timestamp updatedTs = snapshot.getTimestamp("updatedAt");
            if (updatedTs != null) {
                comment.setUpdatedAt(updatedTs.toDate().toInstant());
            }
            
            return comment;
        } catch (Exception e) {
            throw new ValidationException("Error mapping Comment from Firestore: " + e.getMessage(), e);
        }
    }

    // ==================== ACTIVITY LOG MAPPING ====================

    /**
     * Convert ActivityLog object to Firestore document map.
     */
    public static Map<String, Object> activityLogToMap(ActivityLog log) {
        if (log == null) {
            throw new ValidationException("ActivityLog cannot be null");
        }
        
        Map<String, Object> map = new HashMap<>();
        map.put("id", log.getId());
        map.put("userId", log.getUserId());
        map.put("action", log.getAction().getValue());
        map.put("details", log.getDetails());
        map.put("timestamp", log.getTimestamp());
        return map;
    }

    /**
     * Convert Firestore DocumentSnapshot to ActivityLog object.
     */
    public static ActivityLog mapToActivityLog(DocumentSnapshot snapshot) {
        if (snapshot == null || !snapshot.exists()) {
            throw new ValidationException("Document snapshot is null or does not exist");
        }
        
        try {
            ActivityLog log = new ActivityLog();
            log.setId(snapshot.getString("id"));
            log.setUserId(snapshot.getString("userId"));
            
            String actionValue = snapshot.getString("action");
            log.setAction(ActivityType.fromValue(actionValue));
            
            log.setDetails(snapshot.getString("details"));
            
            com.google.cloud.Timestamp timestamp = snapshot.getTimestamp("timestamp");
            if (timestamp != null) {
                log.setTimestamp(timestamp.toDate().toInstant());
            }
            
            return log;
        } catch (Exception e) {
            throw new ValidationException("Error mapping ActivityLog from Firestore: " + e.getMessage(), e);
        }
    }

    // ==================== APP SETTING MAPPING ====================

    /**
     * Convert AppSetting object to Firestore document map.
     */
    public static Map<String, Object> settingToMap(AppSetting setting) {
        if (setting == null) {
            throw new ValidationException("AppSetting cannot be null");
        }
        
        Map<String, Object> map = new HashMap<>();
        map.put("id", setting.getId());
        map.put("key", setting.getKey());
        map.put("value", setting.getValue());
        map.put("description", setting.getDescription());
        map.put("updatedAt", setting.getUpdatedAt());
        map.put("updatedBy", setting.getUpdatedBy());
        return map;
    }

    /**
     * Convert Firestore DocumentSnapshot to AppSetting object.
     */
    public static AppSetting mapToSetting(DocumentSnapshot snapshot) {
        if (snapshot == null || !snapshot.exists()) {
            throw new ValidationException("Document snapshot is null or does not exist");
        }
        
        try {
            AppSetting setting = new AppSetting();
            setting.setId(snapshot.getString("id"));
            setting.setKey(snapshot.getString("key"));
            setting.setValue(snapshot.getString("value"));
            setting.setDescription(snapshot.getString("description"));
            
            com.google.cloud.Timestamp updatedTs = snapshot.getTimestamp("updatedAt");
            if (updatedTs != null) {
                setting.setUpdatedAt(updatedTs.toDate().toInstant());
            }
            
            setting.setUpdatedBy(snapshot.getString("updatedBy"));
            
            return setting;
        } catch (Exception e) {
            throw new ValidationException("Error mapping AppSetting from Firestore: " + e.getMessage(), e);
        }
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Convert Java Instant to Firestore-compatible timestamp.
     */
    public static com.google.cloud.Timestamp instantToTimestamp(Instant instant) {
        if (instant == null) {
            return null;
        }
        return com.google.cloud.Timestamp.of(java.util.Date.from(instant));
    }

    /**
     * Convert Firestore timestamp to Java Instant.
     */
    public static Instant timestampToInstant(com.google.cloud.Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toDate().toInstant();
    }
}
