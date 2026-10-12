package com.recipeapp.util;

import com.recipeapp.exception.ValidationException;
import com.recipeapp.model.*;
import org.junit.Test;
import static org.junit.Assert.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Unit tests for FirestoreMapper conversion logic.
 */
public class FirestoreMapperTest {

    @Test
    public void testUserToMapConversion() {
        User user = new User("user1", "John", "john@example.com", Role.USER, true, 
                Instant.now(), Instant.now());
        
        Map<String, Object> map = FirestoreMapper.userToMap(user);
        
        assertEquals("user1", map.get("id"));
        assertEquals("John", map.get("name"));
        assertEquals("john@example.com", map.get("email"));
        assertEquals("user", map.get("role"));
        assertEquals(true, map.get("active"));
    }

    @Test(expected = ValidationException.class)
    public void testUserToMapFailsWithNull() {
        FirestoreMapper.userToMap(null);
    }

    @Test
    public void testRecipeToMapConversion() {
        Recipe recipe = new Recipe("recipe1", "user1", "Pasta", "Italian pasta",
                "Italian", Arrays.asList("flour"), Arrays.asList("boil"), 
                null, RecipeStatus.PENDING, Instant.now(), Instant.now());
        
        Map<String, Object> map = FirestoreMapper.recipeToMap(recipe);
        
        assertEquals("recipe1", map.get("id"));
        assertEquals("user1", map.get("authorId"));
        assertEquals("Pasta", map.get("title"));
        assertEquals("pending", map.get("status"));
    }

    @Test
    public void testRatingToMapConversion() {
        Rating rating = new Rating("recipe1_user1", "recipe1", "user1", 4, 
                Instant.now(), Instant.now());
        
        Map<String, Object> map = FirestoreMapper.ratingToMap(rating);
        
        assertEquals("recipe1_user1", map.get("id"));
        assertEquals("recipe1", map.get("recipeId"));
        assertEquals("user1", map.get("userId"));
        assertEquals(Integer.valueOf(4), map.get("score")); // Map stores as Integer, not Long
    }

    @Test
    public void testCommentToMapConversion() {
        Comment comment = new Comment("comment1", "recipe1", "user1", "Great!", 
                Instant.now(), Instant.now());
        
        Map<String, Object> map = FirestoreMapper.commentToMap(comment);
        
        assertEquals("comment1", map.get("id"));
        assertEquals("recipe1", map.get("recipeId"));
        assertEquals("user1", map.get("userId"));
        assertEquals("Great!", map.get("content"));
    }

    @Test
    public void testActivityLogToMapConversion() {
        ActivityLog log = new ActivityLog("log1", "user1", ActivityType.RECIPE_CREATED, 
                "Created recipe", Instant.now());
        
        Map<String, Object> map = FirestoreMapper.activityLogToMap(log);
        
        assertEquals("log1", map.get("id"));
        assertEquals("user1", map.get("userId"));
        assertEquals("recipe_created", map.get("action"));
        assertEquals("Created recipe", map.get("details"));
    }

    @Test
    public void testAppSettingToMapConversion() {
        AppSetting setting = new AppSetting("setting1", "allowComments", "true", 
                "Allow user comments", Instant.now(), "admin");
        
        Map<String, Object> map = FirestoreMapper.settingToMap(setting);
        
        assertEquals("setting1", map.get("id"));
        assertEquals("allowComments", map.get("key"));
        assertEquals("true", map.get("value"));
    }

    @Test
    public void testTimestampConversion() {
        Instant instant = Instant.now();
        com.google.cloud.Timestamp ts = FirestoreMapper.instantToTimestamp(instant);
        Instant converted = FirestoreMapper.timestampToInstant(ts);
        
        // Should be equal (within second precision)
        assertEquals(instant.getEpochSecond(), converted.getEpochSecond());
    }
}
