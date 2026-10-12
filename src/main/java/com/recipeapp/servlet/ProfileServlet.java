package com.recipeapp.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.recipeapp.config.FirebaseConfig;
import com.recipeapp.exception.ValidationException;
import com.recipeapp.model.User;
import com.recipeapp.repository.impl.FirestoreUserRepository;
import com.recipeapp.repository.ActivityLogRepository;
import com.recipeapp.repository.impl.FirestoreActivityLogRepository;
import com.recipeapp.service.ActivityLogService;
import com.recipeapp.service.UserService;
import com.recipeapp.service.impl.ActivityLogServiceImpl;
import com.recipeapp.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

/**
 * Servlet for handling user profile operations.
 * 
 * Endpoints:
 * GET  /profile - Get current user profile
 * POST /profile/update - Update current user profile
 * 
 * Authorization:
 * - Get own: authenticated users
 * - Update own: authenticated users
 * - Cannot change: role, active status (admin-only operations)
 */
@WebServlet(urlPatterns = {"/profile/*"})
public class ProfileServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(ProfileServlet.class);
    private static final Gson gson = new Gson();
    
    private UserService userService;

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            FirebaseConfig.initialize();
            
            UserRepository userRepository = new FirestoreUserRepository();
            ActivityLogRepository activityLogRepository = new FirestoreActivityLogRepository();
            ActivityLogService activityLogService = new ActivityLogServiceImpl(activityLogRepository);
            
            this.userService = new com.recipeapp.service.impl.UserServiceImpl(userRepository, activityLogService);
        } catch (Exception e) {
            logger.error("Failed to initialize ProfileServlet: {}", e.getMessage());
            throw new ServletException("Servlet initialization failed", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        resp.setContentType("application/json");
        String userId = (String) req.getSession().getAttribute("USER_ID");
        
        if (userId == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"error\": \"Not authenticated\"}");
            return;
        }
        
        try {
            Optional<User> user = userService.getUserById(userId);
            if (user.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\": \"User not found\"}");
                return;
            }
            
            JsonObject response = new JsonObject();
            response.addProperty("success", true);
            response.add("user", gson.toJsonTree(user.get()));
            
            resp.getWriter().write(response.toString());
            
        } catch (Exception e) {
            logger.error("Error getting profile: {}", e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Failed to fetch profile\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        resp.setContentType("application/json");
        HttpSession session = req.getSession();
        String userId = (String) session.getAttribute("USER_ID");
        
        if (userId == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"error\": \"Not authenticated\"}");
            return;
        }
        
        try {
            Optional<User> existing = userService.getUserById(userId);
            if (existing.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\": \"User not found\"}");
                return;
            }
            
            User user = existing.get();
            JsonObject body = gson.fromJson(req.getReader(), JsonObject.class);
            
            // Only allow updating name
            if (body.has("name") && !body.get("name").getAsString().isBlank()) {
                user.setName(body.get("name").getAsString());
            }
            
            // Prevent tampering with role or active status
            // These can only be changed by admins through admin operations
            
            User updated = userService.updateUser(user);
            
            // Update session with new name if changed
            session.setAttribute("USER_NAME", updated.getName());
            
            JsonObject response = new JsonObject();
            response.addProperty("success", true);
            response.addProperty("message", "Profile updated successfully");
            response.add("user", gson.toJsonTree(updated));
            
            resp.getWriter().write(response.toString());
            
        } catch (ValidationException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.error("Error updating profile: {}", e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Failed to update profile\"}");
        }
    }
}
