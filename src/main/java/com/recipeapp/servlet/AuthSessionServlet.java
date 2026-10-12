package com.recipeapp.servlet;

import com.google.firebase.auth.FirebaseToken;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.recipeapp.config.FirebaseConfig;
import com.recipeapp.exception.AuthenticationException;
import com.recipeapp.model.User;
import com.recipeapp.repository.UserRepository;
import com.recipeapp.repository.impl.FirestoreUserRepository;
import com.recipeapp.service.ActivityLogService;
import com.recipeapp.service.AuthenticationService;
import com.recipeapp.service.impl.ActivityLogServiceImpl;
import com.recipeapp.service.impl.AuthenticationServiceImpl;
import com.recipeapp.repository.ActivityLogRepository;
import com.recipeapp.repository.impl.FirestoreActivityLogRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.BufferedReader;

/**
 * Servlet for establishing authenticated HTTP sessions.
 * 
 * This servlet exchanges a Firebase ID token for an authenticated HttpSession.
 * 
 * POST /auth/session
 * 
 * Request:
 *   Authorization: Bearer <Firebase ID Token>
 *   or
 *   Content-Type: application/json
 *   { "idToken": "<Firebase ID Token>" }
 * 
 * Response on success (200 OK):
 *   { "success": true, "message": "Session established" }
 * 
 * Response on failure (401/403/500):
 *   { "success": false, "error": "descriptive message" }
 * 
 * IMPORTANT:
 * - The Firebase ID token is the ONLY trusted source of identity
 * - Do not trust userId or role from request parameters
 * - Session stores: USER_ID, USER_EMAIL, USER_NAME, USER_ROLE
 */
@WebServlet(urlPatterns = {"/auth/session"}, loadOnStartup = 1)
public class AuthSessionServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(AuthSessionServlet.class);
    private static final Gson gson = new Gson();
    
    private AuthenticationService authenticationService;

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            // Initialize Firebase
            FirebaseConfig.initialize();
            
            // Initialize services
            UserRepository userRepository = new FirestoreUserRepository();
            ActivityLogRepository activityLogRepository = new FirestoreActivityLogRepository();
            ActivityLogService activityLogService = new ActivityLogServiceImpl(activityLogRepository);
            
            com.recipeapp.service.UserService userService = new com.recipeapp.service.impl.UserServiceImpl(
                userRepository, activityLogService
            );
            
            this.authenticationService = new AuthenticationServiceImpl(
                userService,
                activityLogService
            );
        } catch (Exception e) {
            logger.error("Failed to initialize AuthSessionServlet: {}", e.getMessage());
            throw new ServletException("Servlet initialization failed", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        resp.setContentType("application/json");
        
        try {
            // Extract ID token from request
            String idToken = extractIdToken(req);
            
            if (idToken == null || idToken.isBlank()) {
                logger.warn("No ID token provided");
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.getWriter().write(gson.toJson(createErrorResponse("No ID token provided")));
                return;
            }
            
            // Verify token
            FirebaseToken firebaseToken = authenticationService.verifyIdToken(idToken);
            String uid = firebaseToken.getUid();
            
            // Get or create user profile
            User user = authenticationService.getOrCreateUserProfile(firebaseToken, null);
            
            if (user == null) {
                logger.error("Failed to get or create user profile for uid: {}", uid);
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write(gson.toJson(createErrorResponse("Failed to create user profile")));
                return;
            }
            
            // Verify user is active
            if (!user.isActive()) {
                logger.warn("Attempt to authenticate inactive user: {}", uid);
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.getWriter().write(gson.toJson(createErrorResponse("User account is inactive")));
                return;
            }
            
            // Establish HttpSession
            HttpSession session = req.getSession(true);
            session.setAttribute("USER_ID", user.getId());
            session.setAttribute("USER_EMAIL", user.getEmail());
            session.setAttribute("USER_NAME", user.getName());
            session.setAttribute("USER_ROLE", user.getRole().getValue());
            session.setAttribute("AUTHENTICATED", true);
            
            logger.info("Session established for user: {} ({})", user.getId(), user.getEmail());
            
            // Success response
            resp.setStatus(HttpServletResponse.SC_OK);
            JsonObject responseObj = new JsonObject();
            responseObj.addProperty("success", true);
            responseObj.addProperty("message", "Session established");
            resp.getWriter().write(responseObj.toString());
            
        } catch (AuthenticationException e) {
            logger.warn("Authentication failed: {}", e.getMessage());
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write(gson.toJson(createErrorResponse(e.getMessage())));
            
        } catch (Exception e) {
            logger.error("Unexpected error in AuthSessionServlet: {}", e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(gson.toJson(createErrorResponse("Session establishment failed")));
        }
    }

    /**
     * Extract ID token from request.
     * 
     * Tries multiple sources:
     * 1. Authorization header (Bearer token)
     * 2. JSON body with idToken field
     */
    private String extractIdToken(HttpServletRequest req) throws IOException {
        // Try Authorization header
        String authHeader = req.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        
        // Try JSON body
        try {
            BufferedReader reader = req.getReader();
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            
            if (sb.length() > 0) {
                JsonObject body = gson.fromJson(sb.toString(), JsonObject.class);
                if (body != null && body.has("idToken")) {
                    return body.get("idToken").getAsString();
                }
            }
        } catch (Exception e) {
            logger.debug("Failed to parse JSON body: {}", e.getMessage());
        }
        
        return null;
    }

    private JsonObject createErrorResponse(String message) {
        JsonObject response = new JsonObject();
        response.addProperty("success", false);
        response.addProperty("error", message);
        return response;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        resp.setContentType("application/json");
        resp.getWriter().write("{\"error\": \"Method not allowed\"}");
    }
}
