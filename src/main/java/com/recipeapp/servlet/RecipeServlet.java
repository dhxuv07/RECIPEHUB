package com.recipeapp.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.recipeapp.config.FirebaseConfig;
import com.recipeapp.exception.RecipeNotFoundException;
import com.recipeapp.exception.ValidationException;
import com.recipeapp.model.Recipe;
import com.recipeapp.model.RecipeStatus;
import com.recipeapp.repository.impl.FirestoreRecipeRepository;
import com.recipeapp.repository.ActivityLogRepository;
import com.recipeapp.repository.impl.FirestoreActivityLogRepository;
import com.recipeapp.service.ActivityLogService;
import com.recipeapp.service.RecipeService;
import com.recipeapp.service.impl.ActivityLogServiceImpl;
import com.recipeapp.service.impl.RecipeServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Servlet for handling recipe operations.
 * 
 * Endpoints:
 * GET  /recipe/list - Get approved recipes
 * GET  /recipe/{id} - Get recipe details
 * GET  /recipe/my-recipes - Get user's recipes
 * POST /recipe/create - Create new recipe
 * POST /recipe/{id}/update - Update recipe
 * POST /recipe/{id}/delete - Delete recipe
 * 
 * Authorization:
 * - Create: authenticated users (USER or ADMIN)
 * - Update: owner or ADMIN
 * - Delete: owner or ADMIN
 * - View approved: all users
 * - View own pending: owner only
 */
@WebServlet(urlPatterns = {"/recipe/*"})
public class RecipeServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(RecipeServlet.class);
    private static final Gson gson = new Gson();
    
    private RecipeService recipeService;

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            FirebaseConfig.initialize();
            
            var recipeRepository = new FirestoreRecipeRepository();
            ActivityLogRepository activityLogRepository = new FirestoreActivityLogRepository();
            ActivityLogService activityLogService = new ActivityLogServiceImpl(activityLogRepository);
            
            this.recipeService = new RecipeServiceImpl(recipeRepository, activityLogService);
        } catch (Exception e) {
            logger.error("Failed to initialize RecipeServlet: {}", e.getMessage());
            throw new ServletException("Servlet initialization failed", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        resp.setContentType("application/json");
        String pathInfo = req.getPathInfo();
        String userId = (String) req.getSession().getAttribute("USER_ID");
        
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // Default: list approved recipes with optional search and filter
                String search = req.getParameter("search");
                String category = req.getParameter("category");
                listApprovedRecipes(resp, search, category);
            } else if (pathInfo.equals("/my-recipes")) {
                // Get user's recipes
                if (userId == null) {
                    resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    resp.getWriter().write("{\"error\": \"Not authenticated\"}");
                    return;
                }
                getUserRecipes(userId, resp);
            } else if (pathInfo.matches("^/[a-zA-Z0-9-]+$")) {
                // Get recipe by ID
                String recipeId = pathInfo.substring(1);
                getRecipeById(recipeId, resp);
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\": \"Invalid request\"}");
            }
        } catch (Exception e) {
            logger.error("Error in RecipeServlet GET: {}", e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Server error\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        resp.setContentType("application/json");
        String pathInfo = req.getPathInfo();
        String userId = (String) req.getSession().getAttribute("USER_ID");
        
        if (userId == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"error\": \"Not authenticated\"}");
            return;
        }
        
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\": \"Invalid request\"}");
            } else if (pathInfo.equals("/create")) {
                createRecipe(req, resp, userId);
            } else if (pathInfo.matches("^/[a-zA-Z0-9-]+/update$")) {
                String recipeId = pathInfo.split("/")[1];
                updateRecipe(req, resp, userId, recipeId);
            } else if (pathInfo.matches("^/[a-zA-Z0-9-]+/delete$")) {
                String recipeId = pathInfo.split("/")[1];
                deleteRecipe(resp, userId, recipeId);
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\": \"Invalid request\"}");
            }
        } catch (Exception e) {
            logger.error("Error in RecipeServlet POST: {}", e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Server error\"}");
        }
    }

    private void listApprovedRecipes(HttpServletResponse resp, String search, String category) throws IOException {
        try {
            List<Recipe> recipes;
            
            if (search != null && !search.trim().isEmpty()) {
                // Search for recipes by keyword
                recipes = recipeService.searchRecipes(search.trim());
                // Filter to approved only
                recipes = recipes.stream()
                    .filter(r -> r.getStatus() == RecipeStatus.APPROVED)
                    .toList();
            } else if (category != null && !category.trim().isEmpty()) {
                // Filter by category
                recipes = recipeService.filterByCategory(category.trim());
                // Filter to approved only
                recipes = recipes.stream()
                    .filter(r -> r.getStatus() == RecipeStatus.APPROVED)
                    .toList();
            } else {
                // Get all approved recipes
                recipes = recipeService.getApprovedRecipes();
            }
            
            JsonObject response = new JsonObject();
            response.addProperty("success", true);
            response.add("recipes", gson.toJsonTree(recipes));
            resp.getWriter().write(response.toString());
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Failed to fetch recipes\"}");
        }
    }

    private void getUserRecipes(String userId, HttpServletResponse resp) throws IOException {
        try {
            List<Recipe> recipes = recipeService.getRecipesByUser(userId);
            JsonObject response = new JsonObject();
            response.addProperty("success", true);
            response.add("recipes", gson.toJsonTree(recipes));
            resp.getWriter().write(response.toString());
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Failed to fetch recipes\"}");
        }
    }

    private void getRecipeById(String recipeId, HttpServletResponse resp) throws IOException {
        try {
            Optional<Recipe> recipe = recipeService.getRecipeById(recipeId);
            if (recipe.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\": \"Recipe not found\"}");
                return;
            }
            
            JsonObject response = new JsonObject();
            response.addProperty("success", true);
            response.add("recipe", gson.toJsonTree(recipe.get()));
            resp.getWriter().write(response.toString());
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Failed to fetch recipe\"}");
        }
    }

    private void createRecipe(HttpServletRequest req, HttpServletResponse resp, String userId) 
            throws IOException {
        try {
            JsonObject body = gson.fromJson(req.getReader(), JsonObject.class);
            
            Recipe recipe = new Recipe();
            recipe.setAuthorId(userId); // Set from authenticated session
            recipe.setTitle(body.get("title").getAsString());
            recipe.setDescription(body.get("description").getAsString());
            recipe.setCategory(body.get("category").getAsString());
            
            // Parse ingredients and instructions as JSON arrays
            java.util.List<String> ingredients = new java.util.ArrayList<>();
            if (body.has("ingredients") && body.get("ingredients").isJsonArray()) {
                body.getAsJsonArray("ingredients").forEach(e -> ingredients.add(e.getAsString()));
            }
            recipe.setIngredients(ingredients);
            
            java.util.List<String> instructions = new java.util.ArrayList<>();
            if (body.has("instructions") && body.get("instructions").isJsonArray()) {
                body.getAsJsonArray("instructions").forEach(e -> instructions.add(e.getAsString()));
            }
            recipe.setInstructions(instructions);
            
            Recipe created = recipeService.createRecipe(recipe, userId);
            
            JsonObject response = new JsonObject();
            response.addProperty("success", true);
            response.addProperty("message", "Recipe created successfully");
            response.add("recipe", gson.toJsonTree(created));
            
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().write(response.toString());
            
        } catch (ValidationException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.error("Error creating recipe: {}", e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Failed to create recipe\"}");
        }
    }

    private void updateRecipe(HttpServletRequest req, HttpServletResponse resp, String userId, String recipeId) 
            throws IOException {
        try {
            Optional<Recipe> existing = recipeService.getRecipeById(recipeId);
            if (existing.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\": \"Recipe not found\"}");
                return;
            }
            
            Recipe recipe = existing.get();
            
            // Verify ownership
            if (!recipe.getAuthorId().equals(userId)) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.getWriter().write("{\"error\": \"You can only edit your own recipes\"}");
                return;
            }
            
            JsonObject body = gson.fromJson(req.getReader(), JsonObject.class);
            recipe.setTitle(body.get("title").getAsString());
            recipe.setDescription(body.get("description").getAsString());
            recipe.setCategory(body.get("category").getAsString());
            
            // Parse ingredients and instructions as JSON arrays
            java.util.List<String> ingredients = new java.util.ArrayList<>();
            if (body.has("ingredients") && body.get("ingredients").isJsonArray()) {
                body.getAsJsonArray("ingredients").forEach(e -> ingredients.add(e.getAsString()));
            }
            recipe.setIngredients(ingredients);
            
            java.util.List<String> instructions = new java.util.ArrayList<>();
            if (body.has("instructions") && body.get("instructions").isJsonArray()) {
                body.getAsJsonArray("instructions").forEach(e -> instructions.add(e.getAsString()));
            }
            recipe.setInstructions(instructions);
            
            Recipe updated = recipeService.updateRecipe(recipe, userId);
            
            JsonObject response = new JsonObject();
            response.addProperty("success", true);
            response.addProperty("message", "Recipe updated successfully");
            response.add("recipe", gson.toJsonTree(updated));
            
            resp.getWriter().write(response.toString());
            
        } catch (ValidationException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.error("Error updating recipe: {}", e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Failed to update recipe\"}");
        }
    }

    private void deleteRecipe(HttpServletResponse resp, String userId, String recipeId) throws IOException {
        try {
            Optional<Recipe> recipe = recipeService.getRecipeById(recipeId);
            if (recipe.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\": \"Recipe not found\"}");
                return;
            }
            
            // Verify ownership
            if (!recipe.get().getAuthorId().equals(userId)) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.getWriter().write("{\"error\": \"You can only delete your own recipes\"}");
                return;
            }
            
            recipeService.deleteRecipe(recipeId, userId);
            
            JsonObject response = new JsonObject();
            response.addProperty("success", true);
            response.addProperty("message", "Recipe deleted successfully");
            
            resp.getWriter().write(response.toString());
            
        } catch (Exception e) {
            logger.error("Error deleting recipe: {}", e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Failed to delete recipe\"}");
        }
    }
}
