package com.recipeapp.model;

import org.junit.Test;
import static org.junit.Assert.*;

import java.time.Instant;

/**
 * Unit tests for Rating model validation.
 */
public class RatingTest {

    @Test
    public void testValidRatingCreation() {
        Rating rating = new Rating();
        rating.setId("recipe1_user1");
        rating.setRecipeId("recipe1");
        rating.setUserId("user1");
        rating.setScore(4);
        rating.setCreatedAt(Instant.now());
        rating.setUpdatedAt(Instant.now());

        rating.validate(); // No exception should be thrown
        assertEquals(4, rating.getScore());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRatingValidationFailsWithScoreTooLow() {
        Rating rating = new Rating();
        rating.setId("rating1");
        rating.setRecipeId("recipe1");
        rating.setUserId("user1");
        rating.setScore(0); // Must be 1-5
        rating.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRatingValidationFailsWithScoreTooHigh() {
        Rating rating = new Rating();
        rating.setId("rating1");
        rating.setRecipeId("recipe1");
        rating.setUserId("user1");
        rating.setScore(6); // Must be 1-5
        rating.validate();
    }

    @Test
    public void testDeterministicRatingId() {
        String id1 = Rating.generateId("recipe1", "user1");
        String id2 = Rating.generateId("recipe1", "user1");
        
        assertEquals(id1, id2); // Same inputs = same ID
        assertEquals("recipe1_user1", id1);
        
        String id3 = Rating.generateId("recipe1", "user2");
        assertNotEquals(id1, id3); // Different user = different ID
    }

    @Test
    public void testRatingBoundaryScores() {
        Rating rating1 = new Rating("id", "recipe1", "user1", 1, Instant.now(), Instant.now());
        rating1.validate(); // No exception
        
        Rating rating5 = new Rating("id", "recipe1", "user1", 5, Instant.now(), Instant.now());
        rating5.validate(); // No exception
    }
}
