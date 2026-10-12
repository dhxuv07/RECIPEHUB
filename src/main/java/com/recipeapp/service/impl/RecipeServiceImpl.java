package com.recipeapp.service.impl;

import com.recipeapp.exception.DatabaseException;
import com.recipeapp.exception.RecipeNotFoundException;
import com.recipeapp.exception.UnauthorizedException;
import com.recipeapp.exception.ValidationException;
import com.recipeapp.model.ActivityType;
import com.recipeapp.model.Recipe;
import com.recipeapp.model.RecipeStatus;
import com.recipeapp.repository.RecipeRepository;
import com.recipeapp.service.ActivityLogService;
import com.recipeapp.service.RecipeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of RecipeService.
 * 
 * Handles business logic for recipe management, moderation, and discovery.
 * 
 * Key business rules:
 * - New recipes start in PENDING status
 * - Only APPROVED recipes are publicly discoverable
 * - Users can only modify their own recipes
 * - Only admins can approve/reject recipes
 */
public class RecipeServiceImpl implements RecipeService {
    private static final Logger logger = LoggerFactory.getLogger(RecipeServiceImpl.class);
    
    private final RecipeRepository recipeRepository;
    private final ActivityLogService activityLogService;

    public RecipeServiceImpl(RecipeRepository recipeRepository, ActivityLogService activityLogService) {
        this.recipeRepository = recipeRepository;
        this.activityLogService = activityLogService;
    }

    @Override
    public Recipe createRecipe(Recipe recipe, String userId) {
        try {
            if (recipe == null) {
                throw new ValidationException("Recipe cannot be null");
            }
            
            recipe.validate();
            
            // Business rule: authorId must be the current user (no trusting user-supplied author ID)
            recipe.setAuthorId(userId);
            
            // Generate ID if not provided
            if (recipe.getId() == null || recipe.getId().isBlank()) {
                recipe.setId(UUID.randomUUID().toString());
            }
            
            // Business rule: new recipes start in PENDING status
            recipe.setStatus(RecipeStatus.PENDING);
            
            Recipe created = recipeRepository.create(recipe);
            
            activityLogService.logActivity(userId, ActivityType.RECIPE_CREATED,
                    "Recipe created: " + recipe.getTitle());
            
            logger.info("Recipe created: {} by user: {}", recipe.getId(), userId);
            return created;
        } catch (ValidationException | DatabaseException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error creating recipe: {}", e.getMessage());
            throw new DatabaseException("Failed to create recipe: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Recipe> getRecipeById(String id) {
        try {
            return recipeRepository.findById(id);
        } catch (Exception e) {
            logger.error("Error retrieving recipe: {}", e.getMessage());
            throw new DatabaseException("Failed to retrieve recipe: " + e.getMessage(), e);
        }
    }

    @Override
    public Recipe updateRecipe(Recipe recipe, String userId) {
        try {
            if (recipe == null) {
                throw new ValidationException("Recipe cannot be null");
            }
            
            recipe.validate();
            
            Recipe existing = recipeRepository.findById(recipe.getId())
                    .orElseThrow(() -> new RecipeNotFoundException("Recipe not found: " + recipe.getId()));
            
            // Business rule: users can only modify their own recipes
            if (!existing.getAuthorId().equals(userId)) {
                throw new UnauthorizedException("You cannot modify this recipe");
            }
            
            // Don't allow changing author ID or created timestamp
            recipe.setAuthorId(existing.getAuthorId());
            recipe.setCreatedAt(existing.getCreatedAt());
            
            // Business rule: edited recipes return to PENDING for admin re-approval
            // (unless already REJECTED, in which case stay REJECTED)
            if (existing.getStatus() == RecipeStatus.APPROVED) {
                recipe.setStatus(RecipeStatus.PENDING);
            } else {
                recipe.setStatus(existing.getStatus());
            }
            
            recipe.setUpdatedAt(Instant.now());
            
            Recipe updated = recipeRepository.update(recipe);
            
            activityLogService.logActivity(userId, ActivityType.RECIPE_UPDATED,
                    "Recipe updated: " + recipe.getTitle());
            
            logger.info("Recipe updated: {} by user: {}", recipe.getId(), userId);
            return updated;
        } catch (ValidationException | RecipeNotFoundException | UnauthorizedException | DatabaseException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error updating recipe: {}", e.getMessage());
            throw new DatabaseException("Failed to update recipe: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteRecipe(String recipeId, String userId) {
        try {
            if (recipeId == null || recipeId.isBlank()) {
                throw new ValidationException("Recipe ID cannot be empty");
            }
            
            Recipe recipe = recipeRepository.findById(recipeId)
                    .orElseThrow(() -> new RecipeNotFoundException("Recipe not found: " + recipeId));
            
            // Business rule: users can only delete their own recipes
            if (!recipe.getAuthorId().equals(userId)) {
                throw new UnauthorizedException("You cannot delete this recipe");
            }
            
            recipeRepository.delete(recipeId);
            
            activityLogService.logActivity(userId, ActivityType.RECIPE_DELETED,
                    "Recipe deleted: " + recipe.getTitle());
            
            logger.info("Recipe deleted: {} by user: {}", recipeId, userId);
        } catch (ValidationException | RecipeNotFoundException | UnauthorizedException | DatabaseException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting recipe: {}", e.getMessage());
            throw new DatabaseException("Failed to delete recipe: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Recipe> getRecipesByUser(String userId) {
        try {
            return recipeRepository.findByAuthorId(userId);
        } catch (Exception e) {
            logger.error("Error retrieving recipes by user: {}", e.getMessage());
            throw new DatabaseException("Failed to retrieve recipes: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Recipe> getApprovedRecipes() {
        try {
            // Business rule: only APPROVED recipes are publicly discoverable
            return recipeRepository.findApproved();
        } catch (Exception e) {
            logger.error("Error retrieving approved recipes: {}", e.getMessage());
            throw new DatabaseException("Failed to retrieve recipes: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Recipe> getPendingRecipes() {
        try {
            return recipeRepository.findPending();
        } catch (Exception e) {
            logger.error("Error retrieving pending recipes: {}", e.getMessage());
            throw new DatabaseException("Failed to retrieve recipes: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Recipe> getRecipesByStatus(RecipeStatus status) {
        try {
            if (status == null) {
                throw new ValidationException("Status cannot be null");
            }
            return recipeRepository.findByStatus(status);
        } catch (Exception e) {
            logger.error("Error retrieving recipes by status: {}", e.getMessage());
            throw new DatabaseException("Failed to retrieve recipes: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Recipe> searchRecipes(String keyword) {
        try {
            if (keyword == null || keyword.isBlank()) {
                throw new ValidationException("Search keyword cannot be empty");
            }
            return recipeRepository.searchByTitle(keyword);
        } catch (ValidationException | DatabaseException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error searching recipes: {}", e.getMessage());
            throw new DatabaseException("Failed to search recipes: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Recipe> filterByCategory(String category) {
        try {
            if (category == null || category.isBlank()) {
                throw new ValidationException("Category cannot be empty");
            }
            return recipeRepository.findByCategory(category);
        } catch (Exception e) {
            logger.error("Error filtering recipes by category: {}", e.getMessage());
            throw new DatabaseException("Failed to filter recipes: " + e.getMessage(), e);
        }
    }

    @Override
    public Recipe approveRecipe(String recipeId) {
        try {
            if (recipeId == null || recipeId.isBlank()) {
                throw new ValidationException("Recipe ID cannot be empty");
            }
            
            Recipe recipe = recipeRepository.findById(recipeId)
                    .orElseThrow(() -> new RecipeNotFoundException("Recipe not found: " + recipeId));
            
            recipe.setStatus(RecipeStatus.APPROVED);
            recipe.setUpdatedAt(Instant.now());
            
            Recipe updated = recipeRepository.update(recipe);
            
            activityLogService.logActivity("admin", ActivityType.RECIPE_APPROVED,
                    "Recipe approved: " + recipe.getTitle());
            
            logger.info("Recipe approved: {}", recipeId);
            return updated;
        } catch (RecipeNotFoundException | ValidationException | DatabaseException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error approving recipe: {}", e.getMessage());
            throw new DatabaseException("Failed to approve recipe: " + e.getMessage(), e);
        }
    }

    @Override
    public Recipe rejectRecipe(String recipeId) {
        try {
            if (recipeId == null || recipeId.isBlank()) {
                throw new ValidationException("Recipe ID cannot be empty");
            }
            
            Recipe recipe = recipeRepository.findById(recipeId)
                    .orElseThrow(() -> new RecipeNotFoundException("Recipe not found: " + recipeId));
            
            recipe.setStatus(RecipeStatus.REJECTED);
            recipe.setUpdatedAt(Instant.now());
            
            Recipe updated = recipeRepository.update(recipe);
            
            activityLogService.logActivity("admin", ActivityType.RECIPE_REJECTED,
                    "Recipe rejected: " + recipe.getTitle());
            
            logger.info("Recipe rejected: {}", recipeId);
            return updated;
        } catch (RecipeNotFoundException | ValidationException | DatabaseException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error rejecting recipe: {}", e.getMessage());
            throw new DatabaseException("Failed to reject recipe: " + e.getMessage(), e);
        }
    }

    @Override
    public long countByStatus(RecipeStatus status) {
        try {
            if (status == null) {
                throw new ValidationException("Status cannot be null");
            }
            return recipeRepository.countByStatus(status);
        } catch (Exception e) {
            logger.error("Error counting recipes by status: {}", e.getMessage());
            throw new DatabaseException("Failed to count recipes: " + e.getMessage(), e);
        }
    }

    @Override
    public long getTotalRecipeCount() {
        try {
            return recipeRepository.count();
        } catch (Exception e) {
            logger.error("Error counting recipes: {}", e.getMessage());
            throw new DatabaseException("Failed to count recipes: " + e.getMessage(), e);
        }
    }
}
