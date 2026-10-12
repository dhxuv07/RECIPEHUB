package com.recipeapp.seed;

import com.recipeapp.config.FirebaseConfig;
import com.recipeapp.model.*;
import com.recipeapp.repository.impl.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

/**
 * Data seeder for prototype/testing purposes.
 * 
 * Creates sample data in Firestore:
 * - 1 admin user
 * - 3 regular users
 * - 5 sample recipes
 * - Ratings and comments
 * 
 * Note: This is NOT used in production. Data is clearly marked as prototype.
 */
public class DataSeeder {
    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    public static void seedDatabase() {
        try {
            FirebaseConfig.initialize();
            
            logger.info("Starting database seeding...");

            // Initialize repositories
            FirestoreUserRepository userRepo = new FirestoreUserRepository();
            FirestoreRecipeRepository recipeRepo = new FirestoreRecipeRepository();
            FirestoreRatingRepository ratingRepo = new FirestoreRatingRepository();
            FirestoreCommentRepository commentRepo = new FirestoreCommentRepository();

            // Create admin user
            User admin = new User();
            admin.setId("admin_prototype_001");
            admin.setName("Admin User");
            admin.setEmail("admin@prototype.local");
            admin.setRole(Role.ADMIN);
            admin.setActive(true);
            admin.setCreatedAt(Instant.now());
            admin.setUpdatedAt(Instant.now());
            userRepo.create(admin);
            logger.info("Created admin user");

            // Create regular users
            User user1 = createUser("user_001", "Alice Johnson", "alice@prototype.local");
            User user2 = createUser("user_002", "Bob Smith", "bob@prototype.local");
            User user3 = createUser("user_003", "Charlie Brown", "charlie@prototype.local");
            
            userRepo.create(user1);
            userRepo.create(user2);
            userRepo.create(user3);
            logger.info("Created 3 regular users");

            // Create sample recipes
            Recipe recipe1 = createRecipe("recipe_001", user1.getId(), "Spaghetti Carbonara", 
                    "Classic Italian pasta dish");
            Recipe recipe2 = createRecipe("recipe_002", user2.getId(), "Margherita Pizza", 
                    "Traditional pizza from Naples");
            Recipe recipe3 = createRecipe("recipe_003", user1.getId(), "Risotto Milanese", 
                    "Creamy Italian rice dish");
            Recipe recipe4 = createRecipe("recipe_004", user3.getId(), "Tiramisu", 
                    "Classic Italian dessert");
            Recipe recipe5 = createRecipe("recipe_005", user2.getId(), "Ravioli", 
                    "Italian filled pasta");

            recipeRepo.create(recipe1);
            recipeRepo.create(recipe2);
            recipeRepo.create(recipe3);
            recipeRepo.create(recipe4);
            recipeRepo.create(recipe5);
            logger.info("Created 5 sample recipes");

            // Approve some recipes
            recipe1.setStatus(RecipeStatus.APPROVED);
            recipe2.setStatus(RecipeStatus.APPROVED);
            recipe3.setStatus(RecipeStatus.APPROVED);
            recipeRepo.update(recipe1);
            recipeRepo.update(recipe2);
            recipeRepo.update(recipe3);
            logger.info("Approved 3 recipes");

            // Create ratings
            createAndSaveRating(ratingRepo, recipe1.getId(), user2.getId(), 5, "Excellent!");
            createAndSaveRating(ratingRepo, recipe1.getId(), user3.getId(), 4, "Very good");
            createAndSaveRating(ratingRepo, recipe2.getId(), user1.getId(), 5, "Perfect pizza");
            createAndSaveRating(ratingRepo, recipe3.getId(), user2.getId(), 4, "Great risotto");
            logger.info("Created 4 ratings");

            // Create comments
            createAndSaveComment(commentRepo, recipe1.getId(), user2.getId(), "This is the best carbonara recipe I've tried!");
            createAndSaveComment(commentRepo, recipe2.getId(), user1.getId(), "Perfect authentic Margherita!");
            createAndSaveComment(commentRepo, recipe3.getId(), user3.getId(), "My family loved this!");
            logger.info("Created 3 comments");

            logger.info("Database seeding completed successfully");

        } catch (Exception e) {
            logger.error("Error during database seeding: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    private static User createUser(String id, String name, String email) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        user.setRole(Role.USER);
        user.setActive(true);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        return user;
    }

    private static Recipe createRecipe(String id, String authorId, String title, String description) {
        Recipe recipe = new Recipe();
        recipe.setId(id);
        recipe.setAuthorId(authorId);
        recipe.setTitle(title);
        recipe.setDescription(description);
        recipe.setCategory("Italian");
        recipe.setIngredients(Arrays.asList("Ingredient 1", "Ingredient 2", "Ingredient 3"));
        recipe.setInstructions(Arrays.asList("Step 1", "Step 2", "Step 3"));
        recipe.setStatus(RecipeStatus.PENDING);
        recipe.setCreatedAt(Instant.now());
        recipe.setUpdatedAt(Instant.now());
        return recipe;
    }

    private static void createAndSaveRating(FirestoreRatingRepository repo, String recipeId, 
                                            String userId, int score, String comment) {
        Rating rating = new Rating();
        rating.setId(Rating.generateId(recipeId, userId));
        rating.setRecipeId(recipeId);
        rating.setUserId(userId);
        rating.setScore(score);
        rating.setCreatedAt(Instant.now());
        rating.setUpdatedAt(Instant.now());
        repo.create(rating);
    }

    private static void createAndSaveComment(FirestoreCommentRepository repo, String recipeId, 
                                            String userId, String content) {
        Comment comment = new Comment();
        comment.setId(UUID.randomUUID().toString());
        comment.setRecipeId(recipeId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setCreatedAt(Instant.now());
        comment.setUpdatedAt(Instant.now());
        repo.create(comment);
    }

    public static void main(String[] args) {
        seedDatabase();
    }
}
