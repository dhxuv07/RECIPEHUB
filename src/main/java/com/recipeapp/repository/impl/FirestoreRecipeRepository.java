package com.recipeapp.repository.impl;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.recipeapp.config.FirebaseConfig;
import com.recipeapp.exception.DatabaseException;
import com.recipeapp.model.Recipe;
import com.recipeapp.model.RecipeStatus;
import com.recipeapp.repository.RecipeRepository;
import com.recipeapp.util.FirestoreMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Firestore implementation of RecipeRepository.
 * 
 * Handles all Recipe persistence operations against Firestore.
 */
public class FirestoreRecipeRepository implements RecipeRepository {
    private static final Logger logger = LoggerFactory.getLogger(FirestoreRecipeRepository.class);
    private static final String COLLECTION = "recipes";
    
    private final Firestore firestore;

    public FirestoreRecipeRepository() {
        this.firestore = FirebaseConfig.getFirestore();
    }

    @Override
    public Recipe create(Recipe recipe) {
        try {
            recipe.validate();
            
            if (firestore.collection(COLLECTION).document(recipe.getId()).get().get().exists()) {
                throw new DatabaseException("Recipe with ID " + recipe.getId() + " already exists");
            }
            
            Instant now = Instant.now();
            recipe.setCreatedAt(now);
            recipe.setUpdatedAt(now);
            
            if (recipe.getStatus() == null) {
                recipe.setStatus(RecipeStatus.PENDING);
            }
            
            firestore.collection(COLLECTION)
                    .document(recipe.getId())
                    .set(FirestoreMapper.recipeToMap(recipe))
                    .get();
            
            logger.info("Recipe created: {} by author: {}", recipe.getId(), recipe.getAuthorId());
            return recipe;
        } catch (Exception e) {
            logger.error("Error creating recipe: {}", e.getMessage());
            throw new DatabaseException("Failed to create recipe: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Recipe> findById(String id) {
        try {
            DocumentSnapshot snapshot = firestore.collection(COLLECTION)
                    .document(id)
                    .get()
                    .get();
            
            if (snapshot.exists()) {
                return Optional.of(FirestoreMapper.mapToRecipe(snapshot));
            }
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Error finding recipe by ID: {}", e.getMessage());
            throw new DatabaseException("Failed to find recipe: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Recipe> findByAuthorId(String authorId) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION)
                    .whereEqualTo("authorId", authorId)
                    .get()
                    .get();
            
            return querySnapshot.getDocuments()
                    .stream()
                    .map(FirestoreMapper::mapToRecipe)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error finding recipes by author: {}", e.getMessage());
            throw new DatabaseException("Failed to find recipes by author: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Recipe> findAll() {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION)
                    .get()
                    .get();
            
            return querySnapshot.getDocuments()
                    .stream()
                    .map(FirestoreMapper::mapToRecipe)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error finding all recipes: {}", e.getMessage());
            throw new DatabaseException("Failed to find recipes: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Recipe> findApproved() {
        return findByStatus(RecipeStatus.APPROVED);
    }

    @Override
    public List<Recipe> findPending() {
        return findByStatus(RecipeStatus.PENDING);
    }

    @Override
    public List<Recipe> findByStatus(RecipeStatus status) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION)
                    .whereEqualTo("status", status.getValue())
                    .get()
                    .get();
            
            return querySnapshot.getDocuments()
                    .stream()
                    .map(FirestoreMapper::mapToRecipe)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error finding recipes by status: {}", e.getMessage());
            throw new DatabaseException("Failed to find recipes by status: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Recipe> searchByTitle(String keyword) {
        try {
            List<Recipe> allRecipes = findAll();
            
            return allRecipes.stream()
                    .filter(r -> r.getTitle() != null && 
                            r.getTitle().toLowerCase().contains(keyword.toLowerCase()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error searching recipes by title: {}", e.getMessage());
            throw new DatabaseException("Failed to search recipes: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Recipe> findByCategory(String category) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION)
                    .whereEqualTo("category", category)
                    .get()
                    .get();
            
            return querySnapshot.getDocuments()
                    .stream()
                    .map(FirestoreMapper::mapToRecipe)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error finding recipes by category: {}", e.getMessage());
            throw new DatabaseException("Failed to find recipes by category: " + e.getMessage(), e);
        }
    }

    @Override
    public Recipe update(Recipe recipe) {
        try {
            recipe.validate();
            
            if (!firestore.collection(COLLECTION).document(recipe.getId()).get().get().exists()) {
                throw new DatabaseException("Recipe with ID " + recipe.getId() + " not found");
            }
            
            recipe.setUpdatedAt(Instant.now());
            
            firestore.collection(COLLECTION)
                    .document(recipe.getId())
                    .set(FirestoreMapper.recipeToMap(recipe))
                    .get();
            
            logger.info("Recipe updated: {}", recipe.getId());
            return recipe;
        } catch (Exception e) {
            logger.error("Error updating recipe: {}", e.getMessage());
            throw new DatabaseException("Failed to update recipe: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String id) {
        try {
            firestore.collection(COLLECTION)
                    .document(id)
                    .delete()
                    .get();
            
            logger.info("Recipe deleted: {}", id);
        } catch (Exception e) {
            logger.error("Error deleting recipe: {}", e.getMessage());
            throw new DatabaseException("Failed to delete recipe: " + e.getMessage(), e);
        }
    }

    @Override
    public long countByStatus(RecipeStatus status) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION)
                    .whereEqualTo("status", status.getValue())
                    .get()
                    .get();
            
            return querySnapshot.size();
        } catch (Exception e) {
            logger.error("Error counting recipes by status: {}", e.getMessage());
            throw new DatabaseException("Failed to count recipes: " + e.getMessage(), e);
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
            logger.error("Error counting recipes: {}", e.getMessage());
            throw new DatabaseException("Failed to count recipes: " + e.getMessage(), e);
        }
    }
}
