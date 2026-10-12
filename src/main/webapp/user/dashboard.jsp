<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    String contextPath = request.getContextPath();
    Object authenticated = session.getAttribute("AUTHENTICATED");
    if (authenticated == null || !authenticated.equals(true)) {
        response.sendRedirect(contextPath + "/login.jsp");
        return;
    }
    
    String userId = (String) session.getAttribute("USER_ID");
    String userName = (String) session.getAttribute("USER_NAME");
    String userRole = (String) session.getAttribute("USER_ROLE");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard - Recipe Sharing Platform</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
    <style>
        body {
            background: #f5f5f5;
        }
        
        .navbar {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        }
        
        .navbar-brand {
            color: white !important;
            font-weight: bold;
            font-size: 24px;
        }
        
        .nav-link {
            color: rgba(255, 255, 255, 0.8) !important;
        }
        
        .nav-link:hover {
            color: white !important;
        }
        
        .sidebar {
            background: white;
            border-radius: 8px;
            padding: 20px;
            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
            margin-bottom: 20px;
        }
        
        .sidebar-link {
            display: block;
            padding: 12px 20px;
            margin: 8px 0;
            border-radius: 6px;
            text-decoration: none;
            color: #333;
            transition: all 0.3s;
        }
        
        .sidebar-link:hover {
            background: #f0f0f0;
            color: #667eea;
        }
        
        .sidebar-link.active {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
        }
        
        .content-area {
            background: white;
            border-radius: 8px;
            padding: 30px;
            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
        }
        
        .welcome-card {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 30px;
            border-radius: 8px;
            margin-bottom: 30px;
        }
        
        .welcome-card h1 {
            margin: 0;
            margin-bottom: 10px;
            font-size: 32px;
        }
        
        .welcome-card p {
            margin: 0;
            opacity: 0.9;
        }
        
        .stat-card {
            background: white;
            border-radius: 8px;
            padding: 20px;
            margin-bottom: 20px;
            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
            border-left: 4px solid #667eea;
        }
        
        .stat-card.approved {
            border-left-color: #28a745;
        }
        
        .stat-card.pending {
            border-left-color: #ffc107;
        }
        
        .stat-card.rejected {
            border-left-color: #dc3545;
        }
        
        .stat-card-title {
            color: #666;
            font-size: 0.9rem;
            margin-bottom: 8px;
        }
        
        .stat-card-value {
            font-size: 2rem;
            font-weight: bold;
            color: #333;
        }
        
        .action-card {
            background: white;
            border: 1px solid #e0e0e0;
            border-radius: 8px;
            padding: 20px;
            margin-bottom: 20px;
            text-align: center;
            transition: transform 0.3s;
        }
        
        .action-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        }
        
        .action-card-icon {
            font-size: 36px;
            margin-bottom: 15px;
        }
        
        .action-card h3 {
            margin-bottom: 10px;
            color: #333;
        }
        
        .action-card p {
            color: #666;
            margin-bottom: 15px;
        }
        
        .btn-action-card {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border: none;
            padding: 8px 20px;
            border-radius: 6px;
            text-decoration: none;
            display: inline-block;
            cursor: pointer;
            transition: transform 0.2s;
        }
        
        .btn-action-card:hover {
            color: white;
            transform: translateY(-2px);
        }
        
        .stats-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
            gap: 1rem;
            margin-bottom: 2rem;
        }
        
        .loading {
            display: none;
            text-align: center;
            padding: 2rem;
        }
    </style>
</head>
<body>
    <!-- Navigation -->
    <nav class="navbar navbar-expand-lg navbar-dark">
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
                    <li class="nav-item">
                        <span class="nav-link">Welcome, <%= userName %></span>
                    </li>
                    <% if ("ADMIN".equals(userRole)) { %>
                        <li class="nav-item">
                            <a class="nav-link" href="<%= contextPath %>/admin/dashboard.jsp">Admin Panel</a>
                        </li>
                    <% } %>
                    <li class="nav-item">
                        <a class="nav-link" href="<%= contextPath %>/profile.jsp">Profile</a>
                    </li>
                    <li class="nav-item">
                        <form action="<%= contextPath %>/auth/logout" method="post" style="display: inline;">
                            <button type="submit" class="btn btn-sm btn-outline-light">Logout</button>
                        </form>
                    </li>
                </ul>
            </div>
        </div>
    </nav>
    
    <!-- Main Content -->
    <div class="container mt-5 mb-5">
        <div class="row">
            <div class="col-md-3">
                <div class="sidebar">
                    <h5><i class="fas fa-bars"></i> Menu</h5>
                    <a href="<%= contextPath %>/user/dashboard.jsp" class="sidebar-link active">Dashboard</a>
                    <a href="<%= contextPath %>/recipes.jsp" class="sidebar-link">Discover Recipes</a>
                    <a href="<%= contextPath %>/my-recipes.jsp" class="sidebar-link">My Recipes</a>
                    <a href="<%= contextPath %>/recipe-form.jsp" class="sidebar-link">Create Recipe</a>
                    <a href="<%= contextPath %>/profile.jsp" class="sidebar-link">Profile</a>
                </div>
            </div>
            
            <div class="col-md-9">
                <div class="welcome-card">
                    <h1><i class="fas fa-wave-hand"></i> Welcome back, <%= userName %>!</h1>
                    <p>Browse and share delicious recipes with our community</p>
                </div>
                
                <div class="content-area">
                    <!-- Stats Section -->
                    <h3 style="margin-bottom: 2rem;"><i class="fas fa-chart-bar"></i> Your Statistics</h3>
                    
                    <div class="loading" id="loadingSpinner">
                        <div class="spinner-border" role="status">
                            <span class="visually-hidden">Loading...</span>
                        </div>
                        <p class="mt-2">Loading your stats...</p>
                    </div>
                    
                    <div id="statsContent" style="display: none;">
                        <div class="stats-grid">
                            <div class="stat-card">
                                <div class="stat-card-title">Total Recipes</div>
                                <div class="stat-card-value" id="totalRecipes">0</div>
                            </div>
                            <div class="stat-card approved">
                                <div class="stat-card-title">Approved</div>
                                <div class="stat-card-value" id="approvedRecipes">0</div>
                            </div>
                            <div class="stat-card pending">
                                <div class="stat-card-title">Pending</div>
                                <div class="stat-card-value" id="pendingRecipes">0</div>
                            </div>
                            <div class="stat-card rejected">
                                <div class="stat-card-title">Rejected</div>
                                <div class="stat-card-value" id="rejectedRecipes">0</div>
                            </div>
                        </div>
                    </div>
                    
                    <!-- Actions Section -->
                    <hr style="margin: 2rem 0;">
                    <h3 style="margin-bottom: 2rem;"><i class="fas fa-star"></i> Quick Actions</h3>
                    
                    <div class="row">
                        <div class="col-md-6">
                            <div class="action-card">
                                <div class="action-card-icon"><i class="fas fa-search"></i></div>
                                <h3>Discover Recipes</h3>
                                <p>Browse approved recipes from our community</p>
                                <a href="<%= contextPath %>/recipes.jsp" class="btn-action-card">Browse</a>
                            </div>
                        </div>
                        
                        <div class="col-md-6">
                            <div class="action-card">
                                <div class="action-card-icon"><i class="fas fa-plus-circle"></i></div>
                                <h3>Create Recipe</h3>
                                <p>Share your favorite recipe with the community</p>
                                <a href="<%= contextPath %>/recipe-form.jsp" class="btn-action-card">Create</a>
                            </div>
                        </div>
                        
                        <div class="col-md-6">
                            <div class="action-card">
                                <div class="action-card-icon"><i class="fas fa-book"></i></div>
                                <h3>My Recipes</h3>
                                <p>View and manage your recipes</p>
                                <a href="<%= contextPath %>/my-recipes.jsp" class="btn-action-card">View</a>
                            </div>
                        </div>
                        
                        <div class="col-md-6">
                            <div class="action-card">
                                <div class="action-card-icon"><i class="fas fa-user-circle"></i></div>
                                <h3>Profile</h3>
                                <p>Update your profile information</p>
                                <a href="<%= contextPath %>/profile.jsp" class="btn-action-card">Edit</a>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        const contextPath = '<%= contextPath %>';
        
        document.addEventListener('DOMContentLoaded', () => {
            loadUserStats();
        });
        
        function loadUserStats() {
            fetch(contextPath + '/recipe/my-recipes')
                .then(response => {
                    if (!response.ok) throw new Error('Failed to load recipes');
                    return response.json();
                })
                .then(data => {
                    const recipes = data.recipes || [];
                    
                    const totalRecipes = recipes.length;
                    const approvedRecipes = recipes.filter(r => r.status === 'APPROVED').length;
                    const pendingRecipes = recipes.filter(r => r.status === 'PENDING').length;
                    const rejectedRecipes = recipes.filter(r => r.status === 'REJECTED').length;
                    
                    document.getElementById('totalRecipes').textContent = totalRecipes;
                    document.getElementById('approvedRecipes').textContent = approvedRecipes;
                    document.getElementById('pendingRecipes').textContent = pendingRecipes;
                    document.getElementById('rejectedRecipes').textContent = rejectedRecipes;
                    
                    document.getElementById('loadingSpinner').style.display = 'none';
                    document.getElementById('statsContent').style.display = 'block';
                })
                .catch(error => {
                    console.error('Error loading stats:', error);
                    document.getElementById('loadingSpinner').style.display = 'none';
                });
        }
    </script>
</body>
</html>
