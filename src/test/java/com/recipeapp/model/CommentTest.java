package com.recipeapp.model;

import org.junit.Test;
import static org.junit.Assert.*;

import java.time.Instant;

/**
 * Unit tests for Comment model validation.
 */
public class CommentTest {

    @Test
    public void testValidCommentCreation() {
        Comment comment = new Comment();
        comment.setId("comment1");
        comment.setRecipeId("recipe1");
        comment.setUserId("user1");
        comment.setContent("Great recipe!");
        comment.setCreatedAt(Instant.now());
        comment.setUpdatedAt(Instant.now());

        comment.validate(); // No exception
        assertEquals("Great recipe!", comment.getContent());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCommentValidationFailsWithEmptyContent() {
        Comment comment = new Comment();
        comment.setId("comment1");
        comment.setRecipeId("recipe1");
        comment.setUserId("user1");
        comment.setContent("");
        comment.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCommentValidationFailsWithNullContent() {
        Comment comment = new Comment();
        comment.setId("comment1");
        comment.setRecipeId("recipe1");
        comment.setUserId("user1");
        comment.setContent(null);
        comment.validate();
    }

    @Test
    public void testCommentEquality() {
        Comment c1 = new Comment("id1", "recipe1", "user1", "content", Instant.now(), Instant.now());
        Comment c2 = new Comment("id1", "recipe2", "user2", "different", Instant.now(), Instant.now());
        
        assertEquals(c1, c2); // Equality based on ID only
    }
}
