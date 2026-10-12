package com.recipeapp.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.recipeapp.config.FirebaseConfig;
import com.recipeapp.exception.ValidationException;
import com.recipeapp.model.Role;
import com.recipeapp.model.User;
import com.recipeapp.repository.impl.*;
import com.recipeapp.repository.ActivityLogRepository;
import com.recipeapp.repository.RecipeRepository;
import com.recipeapp.repository.UserRepository;
import com.recipeapp.service.*;
import com.recipeapp.service.impl.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Servlet for admin operations.
 * 
 * Endpoints:
 * GET  /admin/statistics - Get platform statistics
 * GET  /admin/users - List all users
 * POST /admin/users/{userId}/promote - Promote user to ADMIN
 * POST /admin/users/{userId}/activate - Activate user
 * POST /admin/users/{userId}/deactivate - Deactivate user
 * GET  /admin/recipes - List all recipes (including pending)
 * POST /admin/recipes/{recipeId}/approve - Approve recipe
 * POST /admin/recipes/{recipeId}/reject - Reject recipe
 * GET  /admin/activity - Get recent activity
 * 
 * Authorization:
 * - All endpoints require ADMIN role
 * - Enforced by RoleAuthorizationFilter
 */
@WebServlet(urlPatterns = {"/admin/*"})
public class AdminServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(AdminServlet.class);
    private static final Gson gson = new Gson();
    
    private AdminService adminService;
    private UserService userService;
    private RecipeService recipeService;
    private ActivityLogService activityLogService;
    private StatisticsService statisticsService;

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            FirebaseConfig.initialize();
            
            // Initialize repositories
            UserRepository userRepository = new FirestoreUserRepository();
            RecipeRepository recipeRepository = new FirestoreRecipeRepository();
            ActivityLogRepository activityLogRepository = new FirestoreActivityLogRepository();
            
            // Initialize services
            this.activityLogService = new ActivityLogServiceImpl(activityLogRepository);
            this.userService = new UserServiceImpl(userRepository, activityLogService);
            this.recipeService = new RecipeServiceImpl(recipeRepository, activityLogService);
            
            // RatingService and CommentService not needed for basic admin operations
            // StatisticsService needs these 4 services
            var ratingRepository = new FirestoreRatingRepository();
            var commentRepository = new FirestoreCommentRepository();
            var ratingService = new com.recipeapp.service.impl.RatingServiceImpl(ratingRepository, activityLogService);
            var commentService = new com.recipeapp.service.impl.CommentServiceImpl(commentRepository, activityLogService);
            
            this.statisticsService = new StatisticsServiceImpl(
                userService, recipeService, ratingService, commentService
            );
            
            this.adminService = new AdminServiceImpl(
                recipeService, userService, statisticsService, activityLogService
            );
        } catch (Exception e) {
            logger.error("Failed to initialize AdminServlet: {}", e.getMessage());
            throw new ServletException("Servlet initialization failed", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        resp.setContentType("application/json");
        String pathInfo = req.getPathInfo();
        
        try {
            if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/statistics")) {
                getStatistics(resp);
            } else if (pathInfo.equals("/users")) {
                listUsers(resp);
            } else if (pathInfo.equals("/recipes")) {
                listRecipes(resp);
            } else if (pathInfo.equals("/activity")) {
                getActivity(resp);
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\": \"Invalid request\"}");
            }
        } catch (Exception e) {
            logger.error("Error in AdminServlet GET: {}", e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Server error\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        resp.setContentType("application/json");
        String pathInfo = req.getPathInfo();
        
        try {
            if (pathInfo != null && pathInfo.matches("^/users/[a-zA-Z0-9-]+/promote$")) {
                String userId = pathInfo.replaceAll("^/users/|/promote$", "");
                promoteUser(resp, userId);
            } else if (pathInfo != null && pathInfo.matches("^/users/[a-zA-Z0-9-]+/activate$")) {
                String userId = pathInfo.replaceAll("^/users/|/activate$", "");
                activateUser(resp, userId);
            } else if (pathInfo != null && pathInfo.matches("^/users/[a-zA-Z0-9-]+/deactivate$")) {
                String userId = pathInfo.replaceAll("^/users/|/deactivate$", "");
                deactivateUser(resp, userId);
            } else if (pathInfo != null && pathInfo.matches("^/recipes/[a-zA-Z0-9-]+/approve$")) {
                String recipeId = pathInfo.replaceAll("^/recipes/|/approve$", "");
                approveRecipe(resp, recipeId);
            } else if (pathInfo != null && pathInfo.matches("^/recipes/[a-zA-Z0-9-]+/reject$")) {
                String recipeId = pathInfo.replaceAll("^/recipes/|/reject$", "");
                rejectRecipe(resp, recipeId);
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\": \"Invalid request\"}");
            }
        } catch (Exception e) {
            logger.error("Error in AdminServlet POST: {}", e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Server error\"}");
        }
    }

    private void getStatistics(HttpServletResponse resp) throws IOException {
        try {
            var stats = adminService.getStatistics();
            
            JsonObject response = new JsonObject();
            response.addProperty("success", true);
            response.add("statistics", gson.toJsonTree(stats));
            
            resp.getWriter().write(response.toString());
        } catch (Exception e) {
            logger.error("Error getting statistics: {}", e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Failed to fetch statistics\"}");
        }
    }

    private void listUsers(HttpServletResponse resp) throws IOException {
        try {
            List<User> users = userService.listUsers();
            
            JsonObject response = new JsonObject();
            response.addProperty("success", true);
            response.add("users", gson.toJsonTree(users));
            
            resp.getWriter().write(response.toString());
        } catch (Exception e) {
            logger.error("Error listing users: {}", e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Failed to fetch users\"}");
        }
    }

    private void listRecipes(HttpServletResponse resp) throws IOException {
        try {
            List<com.recipeapp.model.Recipe> recipes = recipeService.getRecipesByStatus(com.recipeapp.model.RecipeStatus.PENDING);
            List<com.recipeapp.model.Recipe> all = new java.util.ArrayList<>(recipes);
            all.addAll(recipeService.getApprovedRecipes());
            
            JsonObject response = new JsonObject();
            response.addProperty("success", true);
            response.add("recipes", gson.toJsonTree(all));
            
            resp.getWriter().write(response.toString());
        } catch (Exception e) {
            logger.error("Error listing recipes: {}", e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Failed to fetch recipes\"}");
        }
    }

    private void getActivity(HttpServletResponse resp) throws IOException {
        try {
            var activities = activityLogService.getRecentActivity(100);
            
            JsonObject response = new JsonObject();
            response.addProperty("success", true);
            response.add("activities", gson.toJsonTree(activities));
            
            resp.getWriter().write(response.toString());
        } catch (Exception e) {
            logger.error("Error getting activity: {}", e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Failed to fetch activity\"}");
        }
    }

    private void promoteUser(HttpServletResponse resp, String userId) throws IOException {
        try {
            User updated = adminService.changeUserRole(userId, Role.ADMIN);
            
            JsonObject response = new JsonObject();
            response.addProperty("success", true);
            response.addProperty("message", "User promoted to ADMIN");
            response.add("user", gson.toJsonTree(updated));
            
            resp.getWriter().write(response.toString());
        } catch (Exception e) {
            logger.error("Error promoting user: {}", e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Failed to promote user\"}");
        }
    }

    private void activateUser(HttpServletResponse resp, String userId) throws IOException {
        try {
            User updated = adminService.activateUser(userId);
            
            JsonObject response = new JsonObject();
            response.addProperty("success", true);
            response.addProperty("message", "User activated");
            response.add("user", gson.toJsonTree(updated));
            
            resp.getWriter().write(response.toString());
        } catch (Exception e) {
            logger.error("Error activating user: {}", e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Failed to activate user\"}");
        }
    }

    private void deactivateUser(HttpServletResponse resp, String userId) throws IOException {
        try {
            User updated = adminService.deactivateUser(userId);
            
            JsonObject response = new JsonObject();
            response.addProperty("success", true);
            response.addProperty("message", "User deactivated");
            response.add("user", gson.toJsonTree(updated));
            
            resp.getWriter().write(response.toString());
        } catch (Exception e) {
            logger.error("Error deactivating user: {}", e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Failed to deactivate user\"}");
        }
    }

    private void approveRecipe(HttpServletResponse resp, String recipeId) throws IOException {
        try {
            adminService.approveRecipe(recipeId);
            
            JsonObject response = new JsonObject();
            response.addProperty("success", true);
            response.addProperty("message", "Recipe approved");
            
            resp.getWriter().write(response.toString());
        } catch (Exception e) {
            logger.error("Error approving recipe: {}", e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Failed to approve recipe\"}");
        }
    }

    private void rejectRecipe(HttpServletResponse resp, String recipeId) throws IOException {
        try {
            adminService.rejectRecipe(recipeId);
            
            JsonObject response = new JsonObject();
            response.addProperty("success", true);
            response.addProperty("message", "Recipe rejected");
            
            resp.getWriter().write(response.toString());
        } catch (Exception e) {
            logger.error("Error rejecting recipe: {}", e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Failed to reject recipe\"}");
        }
    }
}
