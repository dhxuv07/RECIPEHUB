package com.recipeapp.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.recipeapp.config.FirebaseConfig;
import com.recipeapp.exception.ValidationException;
import com.recipeapp.model.Comment;
import com.recipeapp.repository.impl.FirestoreCommentRepository;
import com.recipeapp.repository.ActivityLogRepository;
import com.recipeapp.repository.impl.FirestoreActivityLogRepository;
import com.recipeapp.service.ActivityLogService;
import com.recipeapp.service.CommentService;
import com.recipeapp.service.impl.ActivityLogServiceImpl;
import com.recipeapp.service.impl.CommentServiceImpl;
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
 * Servlet for handling comment operations.
 * 
 * Endpoints:
 * POST /comment/add - Add new comment
 * GET  /comment/recipe/{recipeId} - Get comments for recipe
 * POST /comment/{commentId}/update - Update comment
 * POST /comment/{commentId}/delete - Delete comment
 * 
 * Authorization:
 * - Add: authenticated users (USER or ADMIN)
 * - Update: comment owner only
 * - Delete: comment owner only
 * - View: all authenticated users
 */
@WebServlet(urlPatterns = {"/comment/*"})
public class CommentServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(CommentServlet.class);
    private static final Gson gson = new Gson();
    
    private CommentService commentService;

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            FirebaseConfig.initialize();
            
            var commentRepository = new FirestoreCommentRepository();
            ActivityLogRepository activityLogRepository = new FirestoreActivityLogRepository();
            ActivityLogService activityLogService = new ActivityLogServiceImpl(activityLogRepository);
            
            this.commentService = new CommentServiceImpl(commentRepository, activityLogService);
        } catch (Exception e) {
            logger.error("Failed to initialize CommentServlet: {}", e.getMessage());
            throw new ServletException("Servlet initialization failed", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        resp.setContentType("application/json");
        String pathInfo = req.getPathInfo();
        
        try {
            if (pathInfo != null && pathInfo.matches("^/recipe/[a-zA-Z0-9-]+$")) {
                String recipeId = pathInfo.replaceAll("^/recipe/", "");
                getRecipeComments(recipeId, resp);
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\": \"Invalid request\"}");
            }
        } catch (Exception e) {
            logger.error("Error in CommentServlet GET: {}", e.getMessage());
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
            if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/add")) {
                addComment(req, resp, userId);
            } else if (pathInfo.matches("^/[a-zA-Z0-9-]+/update$")) {
                String commentId = pathInfo.split("/")[1];
                updateComment(req, resp, userId, commentId);
            } else if (pathInfo.matches("^/[a-zA-Z0-9-]+/delete$")) {
                String commentId = pathInfo.split("/")[1];
                deleteComment(resp, userId, commentId);
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\": \"Invalid request\"}");
            }
        } catch (Exception e) {
            logger.error("Error in CommentServlet POST: {}", e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Server error\"}");
        }
    }

    private void addComment(HttpServletRequest req, HttpServletResponse resp, String userId) 
            throws IOException {
        try {
            JsonObject body = gson.fromJson(req.getReader(), JsonObject.class);
            
            Comment comment = new Comment();
            comment.setRecipeId(body.get("recipeId").getAsString());
            comment.setUserId(userId); // Set from authenticated session
            comment.setContent(body.get("content").getAsString());
            
            Comment created = commentService.addComment(comment);
            
            JsonObject response = new JsonObject();
            response.addProperty("success", true);
            response.addProperty("message", "Comment added successfully");
            response.add("comment", gson.toJsonTree(created));
            
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().write(response.toString());
            
        } catch (ValidationException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.error("Error adding comment: {}", e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Failed to add comment\"}");
        }
    }

    private void getRecipeComments(String recipeId, HttpServletResponse resp) throws IOException {
        try {
            List<Comment> comments = commentService.getCommentsForRecipe(recipeId);
            
            JsonObject response = new JsonObject();
            response.addProperty("success", true);
            response.add("comments", gson.toJsonTree(comments));
            
            resp.getWriter().write(response.toString());
        } catch (Exception e) {
            logger.error("Error getting comments: {}", e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Failed to fetch comments\"}");
        }
    }

    private void updateComment(HttpServletRequest req, HttpServletResponse resp, String userId, String commentId) 
            throws IOException {
        try {
            Optional<Comment> existing = commentService.getCommentById(commentId);
            if (existing.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\": \"Comment not found\"}");
                return;
            }
            
            Comment comment = existing.get();
            
            // Verify ownership
            if (!comment.getUserId().equals(userId)) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.getWriter().write("{\"error\": \"You can only edit your own comments\"}");
                return;
            }
            
            JsonObject body = gson.fromJson(req.getReader(), JsonObject.class);
            comment.setContent(body.get("content").getAsString());
            
            Comment updated = commentService.updateComment(comment, userId);
            
            JsonObject response = new JsonObject();
            response.addProperty("success", true);
            response.addProperty("message", "Comment updated successfully");
            response.add("comment", gson.toJsonTree(updated));
            
            resp.getWriter().write(response.toString());
            
        } catch (ValidationException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.error("Error updating comment: {}", e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Failed to update comment\"}");
        }
    }

    private void deleteComment(HttpServletResponse resp, String userId, String commentId) throws IOException {
        try {
            Optional<Comment> comment = commentService.getCommentById(commentId);
            if (comment.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\": \"Comment not found\"}");
                return;
            }
            
            // Verify ownership
            if (!comment.get().getUserId().equals(userId)) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.getWriter().write("{\"error\": \"You can only delete your own comments\"}");
                return;
            }
            
            commentService.deleteComment(commentId, userId);
            
            JsonObject response = new JsonObject();
            response.addProperty("success", true);
            response.addProperty("message", "Comment deleted successfully");
            
            resp.getWriter().write(response.toString());
            
        } catch (Exception e) {
            logger.error("Error deleting comment: {}", e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Failed to delete comment\"}");
        }
    }
}
