package com.recipeapp.model;

import org.junit.Test;
import static org.junit.Assert.*;

import java.time.Instant;
import java.util.Arrays;

/**
 * Unit tests for Recipe model validation.
 */
public class RecipeTest {

    @Test
    public void testValidRecipeCreation() {
        Recipe recipe = new Recipe();
        recipe.setId("recipe1");
        recipe.setAuthorId("user1");
        recipe.setTitle("Pasta");
        recipe.setCategory("Italian");
        recipe.setInstructions(Arrays.asList("Boil water", "Add pasta"));
        recipe.setStatus(RecipeStatus.PENDING);
        recipe.setCreatedAt(Instant.now());
        recipe.setUpdatedAt(Instant.now());

        recipe.validate(); // No exception
        assertEquals("recipe1", recipe.getId());
        assertEquals(RecipeStatus.PENDING, recipe.getStatus());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRecipeValidationFailsWithEmptyTitle() {
        Recipe recipe = new Recipe();
        recipe.setId("recipe1");
        recipe.setAuthorId("user1");
        recipe.setTitle("");
        recipe.setCategory("Italian");
        recipe.setInstructions(Arrays.asList("Boil water"));
        recipe.setStatus(RecipeStatus.PENDING);
        recipe.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRecipeValidationFailsWithNoInstructions() {
        Recipe recipe = new Recipe();
        recipe.setId("recipe1");
        recipe.setAuthorId("user1");
        recipe.setTitle("Pasta");
        recipe.setCategory("Italian");
        recipe.setInstructions(Arrays.asList()); // Empty
        recipe.setStatus(RecipeStatus.PENDING);
        recipe.validate();
    }

    @Test
    public void testRecipeStatusEnum() {
        assertEquals("pending", RecipeStatus.PENDING.getValue());
        assertEquals("approved", RecipeStatus.APPROVED.getValue());
        assertEquals("rejected", RecipeStatus.REJECTED.getValue());
        
        assertEquals(RecipeStatus.APPROVED, RecipeStatus.fromValue("approved"));
        assertEquals(RecipeStatus.PENDING, RecipeStatus.fromValue("PENDING")); // Case-insensitive
    }

    @Test
    public void testRecipeEquality() {
        Recipe r1 = new Recipe("id1", "user1", "Pasta", "desc", "Italian", 
                Arrays.asList(), Arrays.asList("step"), null, RecipeStatus.PENDING, Instant.now(), Instant.now());
        Recipe r2 = new Recipe("id1", "user2", "Pizza", "desc", "Italian", 
                Arrays.asList(), Arrays.asList("step"), null, RecipeStatus.APPROVED, Instant.now(), Instant.now());
        
        assertEquals(r1, r2); // Equality based on ID only
    }
}
