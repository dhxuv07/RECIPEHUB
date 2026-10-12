package com.recipeapp.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Role-based Authorization Filter.
 * 
 * Enforces role-based access control for admin and user endpoints.
 * 
 * Admin-only endpoints:
 * - /admin/*
 * 
 * User endpoints:
 * - /user/*
 * - /recipe/* (some actions may be restricted)
 * - /rating/*
 * - /comment/*
 * - /profile/*
 * 
 * Behavior:
 * - If user role matches required role: continue
 * - If user role does NOT match: return 403 Forbidden
 * - This filter runs AFTER AuthenticationFilter
 */
@WebFilter(urlPatterns = {"/admin/*", "/user/*"})
public class RoleAuthorizationFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(RoleAuthorizationFilter.class);
    
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_USER = "USER";

    @Override
    public void init(FilterConfig config) throws ServletException {
        logger.debug("RoleAuthorizationFilter initialized");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
            chain.doFilter(request, response);
            return;
        }
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String requestPath = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        String relativePath = requestPath.substring(contextPath.length());
        
        // Get user role from session
        String userRole = getUserRole(httpRequest);
        
        // Check authorization based on path
        if (relativePath.startsWith("/admin/")) {
            if (!ROLE_ADMIN.equals(userRole)) {
                logger.warn("Unauthorized access attempt to admin endpoint by user with role: {}", userRole);
                
                // Determine response type
                String accept = httpRequest.getHeader("Accept");
                if (accept != null && accept.contains("application/json")) {
                    httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    httpResponse.setContentType("application/json");
                    httpResponse.getWriter().write("{\"error\": \"Forbidden - Admin access required\"}");
                } else {
                    httpResponse.sendRedirect(contextPath + "/error.jsp?code=403&message=Admin access required");
                }
                return;
            }
        } else if (relativePath.startsWith("/user/")) {
            // User endpoints require USER or ADMIN role
            if (!ROLE_USER.equals(userRole) && !ROLE_ADMIN.equals(userRole)) {
                logger.warn("Unauthorized access attempt to user endpoint by user with role: {}", userRole);
                httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                httpResponse.setContentType("application/json");
                httpResponse.getWriter().write("{\"error\": \"Forbidden\"}");
                return;
            }
        }
        
        // Authorization passed
        chain.doFilter(request, response);
    }

    /**
     * Get user role from session.
     * 
     * Returns the role stored in the authenticated session, or null if not authenticated.
     */
    private String getUserRole(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        
        if (session == null) {
            return null;
        }
        
        return (String) session.getAttribute("USER_ROLE");
    }

    @Override
    public void destroy() {
        logger.debug("RoleAuthorizationFilter destroyed");
    }
}
