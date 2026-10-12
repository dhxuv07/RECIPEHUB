<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    String contextPath = request.getContextPath();
    boolean isAuthenticated = session.getAttribute("AUTHENTICATED") != null && 
                              session.getAttribute("AUTHENTICATED").equals(true);
    String userRole = (String) session.getAttribute("USER_ROLE");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Home - Recipe Sharing Platform</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
        }
        
        .navbar {
            background: rgba(255, 255, 255, 0.95) !important;
            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
        }
        
        .navbar-brand {
            font-weight: bold;
            color: #667eea !important;
            font-size: 24px;
        }
        
        .hero {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 100px 20px;
            text-align: center;
        }
        
        .hero h1 {
            font-size: 48px;
            margin-bottom: 20px;
            font-weight: bold;
        }
        
        .hero p {
            font-size: 20px;
            margin-bottom: 30px;
            opacity: 0.9;
        }
        
        .hero-actions {
            display: flex;
            gap: 15px;
            justify-content: center;
            flex-wrap: wrap;
        }
        
        .btn-action {
            padding: 12px 30px;
            border-radius: 6px;
            font-weight: 600;
            text-decoration: none;
            font-size: 16px;
            cursor: pointer;
            border: none;
            transition: transform 0.2s;
        }
        
        .btn-action:hover {
            transform: translateY(-2px);
        }
        
        .btn-primary-action {
            background: white;
            color: #667eea;
        }
        
        .btn-secondary-action {
            background: rgba(255, 255, 255, 0.2);
            color: white;
            border: 2px solid white;
        }
        
        .btn-secondary-action:hover {
            background: rgba(255, 255, 255, 0.3);
            color: white;
        }
        
        .features {
            background: white;
            padding: 60px 20px;
        }
        
        .feature-card {
            background: white;
            border-radius: 10px;
            padding: 30px;
            margin: 20px;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
            text-align: center;
            transition: transform 0.3s;
        }
        
        .feature-card:hover {
            transform: translateY(-10px);
        }
        
        .feature-icon {
            font-size: 48px;
            margin-bottom: 15px;
        }
        
        .feature-title {
            font-size: 20px;
            font-weight: bold;
            color: #333;
            margin-bottom: 10px;
        }
        
        .feature-description {
            color: #666;
            line-height: 1.6;
        }
    </style>
</head>
<body>
    <!-- Navigation -->
    <nav class="navbar navbar-expand-lg navbar-light">
        <div class="container">
            <a class="navbar-brand" href="<%= contextPath %>/index.jsp">🍳 Recipe Sharing</a>
            <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
                <span class="navbar-toggler-icon"></span>
            </button>
            <div class="collapse navbar-collapse" id="navbarNav">
                <ul class="navbar-nav ms-auto">
                    <li class="nav-item">
                        <a class="nav-link" href="<%= contextPath %>/recipes.jsp">Discover</a>
                    </li>
                    <% if (isAuthenticated) { %>
                        <li class="nav-item">
                            <a class="nav-link" href="<%= contextPath %>/recipe-form.jsp">Create Recipe</a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" href="<%= contextPath %>/my-recipes.jsp">My Recipes</a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" href="<%= contextPath %>/user/dashboard.jsp">Dashboard</a>
                        </li>
                        <% if ("ADMIN".equals(userRole)) { %>
                            <li class="nav-item">
                                <a class="nav-link" href="<%= contextPath %>/admin/dashboard.jsp">Admin</a>
                            </li>
                        <% } %>
                        <li class="nav-item">
                            <a class="nav-link" href="<%= contextPath %>/profile.jsp">Profile</a>
                        </li>
                        <li class="nav-item">
                            <form action="<%= contextPath %>/auth/logout" method="post" style="display: inline;">
                                <button type="submit" class="btn btn-sm btn-outline-danger">Logout</button>
                            </form>
                        </li>
                    <% } else { %>
                        <li class="nav-item">
                            <a class="nav-link" href="<%= contextPath %>/login.jsp">Login</a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" href="<%= contextPath %>/register.jsp">Register</a>
                        </li>
                    <% } %>
                </ul>
            </div>
        </div>
    </nav>
    
    <!-- Hero Section -->
    <div class="hero">
        <h1>Share Your Culinary Creations</h1>
        <p>Discover, rate, and share delicious recipes with a community of food enthusiasts</p>
        <div class="hero-actions">
            <% if (isAuthenticated) { %>
                <a href="<%= contextPath %>/user/dashboard.jsp" class="btn-action btn-primary-action">Go to Dashboard</a>
                <a href="<%= contextPath %>/recipes.jsp" class="btn-action btn-secondary-action">Discover Recipes</a>
            <% } else { %>
                <a href="<%= contextPath %>/register.jsp" class="btn-action btn-primary-action">Get Started</a>
                <a href="<%= contextPath %>/recipes.jsp" class="btn-action btn-secondary-action">Discover Recipes</a>
            <% } %>
        </div>
    </div>
    
    <!-- Features Section -->
    <div class="features">
        <div class="container">
            <h2 style="text-align: center; margin-bottom: 50px; color: #333;">Features</h2>
            <div class="row">
                <div class="col-md-4">
                    <div class="feature-card">
                        <div class="feature-icon">📝</div>
                        <div class="feature-title">Share Recipes</div>
                        <div class="feature-description">
                            Create and share your favorite recipes with detailed ingredients and instructions.
                        </div>
                    </div>
                </div>
                <div class="col-md-4">
                    <div class="feature-card">
                        <div class="feature-icon">⭐</div>
                        <div class="feature-title">Rate & Review</div>
                        <div class="feature-description">
                            Rate recipes and leave comments to help other users discover great dishes.
                        </div>
                    </div>
                </div>
                <div class="col-md-4">
                    <div class="feature-card">
                        <div class="feature-icon">🔍</div>
                        <div class="feature-title">Discover</div>
                        <div class="feature-description">
                            Explore approved recipes from the community and find inspiration for your next meal.
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
