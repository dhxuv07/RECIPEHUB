<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    String contextPath = request.getContextPath();
    Object authenticated = session.getAttribute("AUTHENTICATED");
    String userRole = (String) session.getAttribute("USER_ROLE");
    
    if (authenticated == null || !authenticated.equals(true)) {
        response.sendRedirect(contextPath + "/login.jsp");
        return;
    }
    
    if (!"ADMIN".equals(userRole)) {
        response.sendRedirect(contextPath + "/error.jsp?code=403&message=Admin access required");
        return;
    }
    
    String userName = (String) session.getAttribute("USER_NAME");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Panel - Recipe Sharing Platform</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
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
        
        .admin-badge {
            background: #ff6b6b;
            color: white;
            padding: 4px 8px;
            border-radius: 4px;
            font-size: 12px;
            margin-left: 10px;
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
        
        .admin-card {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 30px;
            border-radius: 8px;
            margin-bottom: 30px;
        }
        
        .admin-card h1 {
            margin: 0;
            margin-bottom: 10px;
            font-size: 32px;
        }
        
        .stat-card {
            background: white;
            border: 1px solid #e0e0e0;
            border-radius: 8px;
            padding: 20px;
            margin-bottom: 20px;
            text-align: center;
        }
        
        .stat-card .stat-number {
            font-size: 36px;
            font-weight: bold;
            color: #667eea;
        }
        
        .stat-card .stat-label {
            color: #666;
            margin-top: 10px;
        }
        
        .action-button {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border: none;
            padding: 10px 20px;
            border-radius: 6px;
            cursor: pointer;
            text-decoration: none;
            display: inline-block;
            margin: 5px;
            transition: transform 0.2s;
        }
        
        .action-button:hover {
            color: white;
            transform: translateY(-2px);
        }
    </style>
</head>
<body>
    <!-- Navigation -->
    <nav class="navbar navbar-expand-lg navbar-dark">
        <div class="container">
            <a class="navbar-brand" href="<%= contextPath %>/index.jsp">
                🍳 Recipe Sharing 
                <span class="admin-badge">ADMIN</span>
            </a>
            <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
                <span class="navbar-toggler-icon"></span>
            </button>
            <div class="collapse navbar-collapse" id="navbarNav">
                <ul class="navbar-nav ms-auto">
                    <li class="nav-item">
                        <span class="nav-link">Admin: <%= userName %></span>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="<%= contextPath %>/user/dashboard.jsp">User Dashboard</a>
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
                    <h5>Admin Menu</h5>
                    <a href="<%= contextPath %>/admin/dashboard.jsp" class="sidebar-link active">Dashboard</a>
                    <a href="<%= contextPath %>/admin/users" class="sidebar-link">Manage Users</a>
                    <a href="<%= contextPath %>/admin/recipes" class="sidebar-link">Manage Recipes</a>
                    <a href="<%= contextPath %>/admin/activity" class="sidebar-link">Activity Log</a>
                </div>
            </div>
            
            <div class="col-md-9">
                <div class="admin-card">
                    <h1>Admin Panel</h1>
                    <p>Manage users, recipes, and platform settings</p>
                </div>
                
                <div class="content-area">
                    <h2 style="margin-bottom: 30px;">Dashboard Overview</h2>
                    
                    <div class="row" id="statsContainer">
                        <div class="col-md-3">
                            <div class="stat-card">
                                <div class="stat-number">-</div>
                                <div class="stat-label">Total Users</div>
                            </div>
                        </div>
                        <div class="col-md-3">
                            <div class="stat-card">
                                <div class="stat-number">-</div>
                                <div class="stat-label">Total Recipes</div>
                            </div>
                        </div>
                        <div class="col-md-3">
                            <div class="stat-card">
                                <div class="stat-number">-</div>
                                <div class="stat-label">Pending Recipes</div>
                            </div>
                        </div>
                        <div class="col-md-3">
                            <div class="stat-card">
                                <div class="stat-number">-</div>
                                <div class="stat-label">Total Ratings</div>
                            </div>
                        </div>
                    </div>
                    
                    <hr style="margin: 30px 0;">
                    
                    <h3 style="margin-bottom: 20px;">Admin Operations</h3>
                    
                    <div>
                        <a href="<%= contextPath %>/admin/users" class="action-button">Manage Users</a>
                        <a href="<%= contextPath %>/admin/recipes" class="action-button">Manage Recipes</a>
                        <a href="<%= contextPath %>/admin/activity" class="action-button">View Activity</a>
                    </div>
                </div>
            </div>
        </div>
    </div>
    
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    
    <script>
        // Load statistics
        fetch('<%= contextPath %>/admin/statistics')
            .then(response => response.json())
            .then(data => {
                if (data.success && data.statistics) {
                    const stats = data.statistics;
                    const statsHtml = `
                        <div class="col-md-3">
                            <div class="stat-card">
                                <div class="stat-number">${stats.totalUsers || 0}</div>
                                <div class="stat-label">Total Users</div>
                            </div>
                        </div>
                        <div class="col-md-3">
                            <div class="stat-card">
                                <div class="stat-number">${stats.totalRecipes || 0}</div>
                                <div class="stat-label">Total Recipes</div>
                            </div>
                        </div>
                        <div class="col-md-3">
                            <div class="stat-card">
                                <div class="stat-number">${stats.pendingRecipes || 0}</div>
                                <div class="stat-label">Pending Recipes</div>
                            </div>
                        </div>
                        <div class="col-md-3">
                            <div class="stat-card">
                                <div class="stat-number">${stats.totalRatings || 0}</div>
                                <div class="stat-label">Total Ratings</div>
                            </div>
                        </div>
                    `;
                    document.getElementById('statsContainer').innerHTML = statsHtml;
                }
            })
            .catch(error => console.error('Error loading statistics:', error));
    </script>
</body>
</html>
