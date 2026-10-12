package com.recipeapp.servlet;

import com.recipeapp.model.ActivityType;
import com.recipeapp.config.FirebaseConfig;
import com.recipeapp.repository.ActivityLogRepository;
import com.recipeapp.repository.impl.FirestoreActivityLogRepository;
import com.recipeapp.service.ActivityLogService;
import com.recipeapp.service.impl.ActivityLogServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Servlet for handling user logout.
 * 
 * POST /auth/logout
 * 
 * Invalidates the HttpSession and logs the logout activity.
 * 
 * After logout, the user must:
 * 1. Sign out from Firebase Authentication on the client side
 * 2. Redirect to login page
 */
@WebServlet(urlPatterns = {"/auth/logout"})
public class LogoutServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(LogoutServlet.class);
    
    private ActivityLogService activityLogService;

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            FirebaseConfig.initialize();
            ActivityLogRepository activityLogRepository = new FirestoreActivityLogRepository();
            this.activityLogService = new ActivityLogServiceImpl(activityLogRepository);
        } catch (Exception e) {
            logger.error("Failed to initialize LogoutServlet: {}", e.getMessage());
            throw new ServletException("Servlet initialization failed", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        HttpSession session = req.getSession(false);
        
        if (session != null) {
            String userId = (String) session.getAttribute("USER_ID");
            String userEmail = (String) session.getAttribute("USER_EMAIL");
            
            // Log logout activity
            if (userId != null && !userId.isBlank()) {
                try {
                    activityLogService.logActivity(userId, ActivityType.USER_DEACTIVATED,
                        "User logged out");
                } catch (Exception e) {
                    logger.warn("Failed to log logout activity: {}", e.getMessage());
                    // Don't fail logout if logging fails
                }
            }
            
            // Invalidate session
            session.invalidate();
            logger.info("Session invalidated for user: {}", userEmail);
        }
        
        // Redirect to login
        resp.sendRedirect(req.getContextPath() + "/login.jsp");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        // Allow GET for convenience (though POST is preferred)
        doPost(req, resp);
    }
}
