package com.recipeapp.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.recipeapp.config.FirebaseConfig;
import com.recipeapp.exception.ValidationException;
import com.recipeapp.model.Rating;
import com.recipeapp.repository.impl.FirestoreRatingRepository;
import com.recipeapp.repository.ActivityLogRepository;
import com.recipeapp.repository.impl.FirestoreActivityLogRepository;
import com.recipeapp.service.ActivityLogService;
import com.recipeapp.service.RatingService;
import com.recipeapp.service.impl.ActivityLogServiceImpl;
import com.recipeapp.service.impl.RatingServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

/**
 * Servlet for handling rating operations.
 * 
 * Endpoints:
 * POST /rating/submit - Submit or update rating
 * GET  /rating/{recipeId} - Get average rating for recipe
 * 
 * Authorization:
 * - Submit: authenticated users (USER or ADMIN)
 * 
 * Business Logic:
 * - One rating per user per recipe (deterministic ID)
 * - Score must be 1-5
 * - Duplicate submission updates existing rating
 */
@WebServlet(urlPatterns = {"/rating/*"})
public class RatingServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(RatingServlet.class);
    private static final Gson gson = new Gson();
    
    private RatingService ratingService;

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            FirebaseConfig.initialize();
            
            var ratingRepository = new FirestoreRatingRepository();
            ActivityLogRepository activityLogRepository = new FirestoreActivityLogRepository();
            ActivityLogService activityLogService = new ActivityLogServiceImpl(activityLogRepository);
            
            this.ratingService = new RatingServiceImpl(ratingRepository, activityLogService);
        } catch (Exception e) {
            logger.error("Failed to initialize RatingServlet: {}", e.getMessage());
            throw new ServletException("Servlet initialization failed", e);
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
            if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/submit")) {
                submitRating(req, resp, userId);
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\": \"Invalid request\"}");
            }
        } catch (Exception e) {
            logger.error("Error in RatingServlet POST: {}", e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Server error\"}");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        resp.setContentType("application/json");
        String pathInfo = req.getPathInfo();
        
        try {
            if (pathInfo != null && pathInfo.matches("^/[a-zA-Z0-9-]+$")) {
                String recipeId = pathInfo.substring(1);
                getAverageRating(recipeId, resp);
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\": \"Invalid request\"}");
            }
        } catch (Exception e) {
            logger.error("Error in RatingServlet GET: {}", e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Server error\"}");
        }
    }

    private void submitRating(HttpServletRequest req, HttpServletResponse resp, String userId) 
            throws IOException {
        try {
            JsonObject body = gson.fromJson(req.getReader(), JsonObject.class);
            
            String recipeId = body.get("recipeId").getAsString();
            int score = body.get("score").getAsInt();
            
            // Validate score
            if (score < 1 || score > 5) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\": \"Score must be between 1 and 5\"}");
                return;
            }
            
            Rating rating = new Rating();
            rating.setRecipeId(recipeId);
            rating.setUserId(userId); // Set from authenticated session
            rating.setScore(score);
            
            Rating submitted = ratingService.submitRating(rating);
            
            JsonObject response = new JsonObject();
            response.addProperty("success", true);
            response.addProperty("message", "Rating submitted successfully");
            response.add("rating", gson.toJsonTree(submitted));
            
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().write(response.toString());
            
        } catch (ValidationException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.error("Error submitting rating: {}", e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Failed to submit rating\"}");
        }
    }

    private void getAverageRating(String recipeId, HttpServletResponse resp) throws IOException {
        try {
            double averageRating = ratingService.calculateAverageRating(recipeId);
            long ratingCount = ratingService.countRatingsForRecipe(recipeId);
            
            JsonObject response = new JsonObject();
            response.addProperty("success", true);
            response.addProperty("recipeId", recipeId);
            response.addProperty("averageRating", averageRating);
            response.addProperty("ratingCount", ratingCount);
            
            resp.getWriter().write(response.toString());
        } catch (Exception e) {
            logger.error("Error getting average rating: {}", e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Failed to get rating\"}");
        }
    }
}
