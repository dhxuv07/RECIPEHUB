package com.recipeapp.model;

import org.junit.Test;
import static org.junit.Assert.*;

import java.time.Instant;

/**
 * Unit tests for User model validation.
 */
public class UserTest {

    @Test
    public void testValidUserCreation() {
        User user = new User();
        user.setId("user1");
        user.setName("John Doe");
        user.setEmail("john@example.com");
        user.setRole(Role.USER);
        user.setActive(true);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());

        user.validate(); // No exception
        assertEquals("user1", user.getId());
        assertEquals(Role.USER, user.getRole());
        assertTrue(user.isActive());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUserValidationFailsWithEmptyId() {
        User user = new User();
        user.setId("");
        user.setName("John Doe");
        user.setEmail("john@example.com");
        user.setRole(Role.USER);
        user.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUserValidationFailsWithInvalidEmail() {
        User user = new User();
        user.setId("user1");
        user.setName("John Doe");
        user.setEmail("invalid-email");
        user.setRole(Role.USER);
        user.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUserValidationFailsWithNullRole() {
        User user = new User();
        user.setId("user1");
        user.setName("John Doe");
        user.setEmail("john@example.com");
        user.setRole(null);
        user.validate();
    }

    @Test
    public void testUserEquality() {
        User user1 = new User("id1", "John", "john@example.com", Role.USER, true, Instant.now(), Instant.now());
        User user2 = new User("id1", "Jane", "jane@example.com", Role.ADMIN, false, Instant.now(), Instant.now());
        
        assertEquals(user1, user2); // Equality based on ID only
    }

    @Test
    public void testRoleEnum() {
        assertEquals("user", Role.USER.getValue());
        assertEquals("admin", Role.ADMIN.getValue());
        assertEquals(Role.USER, Role.fromValue("user"));
        assertEquals(Role.ADMIN, Role.fromValue("ADMIN")); // Case-insensitive
    }
}
