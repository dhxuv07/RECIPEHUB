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
 * Authentication Filter for protecting endpoints.
 * 
 * This filter ensures that only authenticated users can access protected resources.
 * 
 * Public endpoints (no authentication required):
 * - /
 * - /index.jsp
 * - /login.jsp
 * - /register.jsp
 * - /auth/* (except protected operations)
 * - /css/*
 * - /js/*
 * - /images/*
 * 
 * Protected endpoints (authentication required):
 * - /user/*
 * - /recipe/*
 * - /rating/*
 * - /comment/*
 * - /profile/*
 * - /admin/*
 * 
 * Behavior:
 * - If user is authenticated: continue to next filter/servlet
 * - If user is NOT authenticated: redirect to login page
 * - API requests: return 401 Unauthorized
 */
@WebFilter(urlPatterns = {"/*"})
public class AuthenticationFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);
    
    // Public endpoints that do not require authentication
    private static final String[] PUBLIC_PATHS = {
        "/",
        "/index.jsp",
        "/login.jsp",
        "/register.jsp",
        "/auth/logout",
        "/css/",
        "/js/",
        "/images/"
    };
    
    // Protected endpoints that require authentication
    private static final String[] PROTECTED_PATHS = {
        "/user/",
        "/recipe/",
        "/rating/",
        "/comment/",
        "/profile/",
        "/admin/"
    };

    @Override
    public void init(FilterConfig config) throws ServletException {
        logger.debug("AuthenticationFilter initialized");
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
        
        // Remove context path to get the relative path
        String relativePath = requestPath.substring(contextPath.length());
        if (!relativePath.startsWith("/")) {
            relativePath = "/" + relativePath;
        }
        
        // Check if this is a public path
        if (isPublicPath(relativePath)) {
            chain.doFilter(request, response);
            return;
        }
        
        // Check if this is a protected path
        if (isProtectedPath(relativePath)) {
            if (!isAuthenticated(httpRequest)) {
                logger.debug("Unauthenticated access attempt to protected resource: {}", relativePath);
                
                // Determine if this is an API request
                String accept = httpRequest.getHeader("Accept");
                if (accept != null && accept.contains("application/json")) {
                    // API request - return 401
                    httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    httpResponse.setContentType("application/json");
                    httpResponse.getWriter().write("{\"error\": \"Unauthorized\"}");
                } else {
                    // Browser request - redirect to login
                    httpResponse.sendRedirect(contextPath + "/login.jsp");
                }
                return;
            }
        }
        
        // Proceed with the request
        chain.doFilter(request, response);
    }

    /**
     * Check if user is authenticated (has valid session).
     */
    private boolean isAuthenticated(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        
        if (session == null) {
            return false;
        }
        
        Object authenticated = session.getAttribute("AUTHENTICATED");
        Object userId = session.getAttribute("USER_ID");
        
        return authenticated != null && authenticated.equals(true) && userId != null;
    }

    /**
     * Check if the requested path is public (no authentication required).
     */
    private boolean isPublicPath(String path) {
        // Exact matches
        if ("/".equals(path) || "/index.jsp".equals(path) || 
            "/login.jsp".equals(path) || "/register.jsp".equals(path) ||
            "/auth/logout".equals(path)) {
            return true;
        }
        
        // Prefix matches
        for (String publicPath : PUBLIC_PATHS) {
            if (publicPath.endsWith("/") && path.startsWith(publicPath)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Check if the requested path is protected (authentication required).
     */
    private boolean isProtectedPath(String path) {
        for (String protectedPath : PROTECTED_PATHS) {
            if (path.startsWith(protectedPath)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void destroy() {
        logger.debug("AuthenticationFilter destroyed");
    }
}
