<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.google.gson.Gson" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>My Recipes - Recipe Sharing Platform</title>
    <link href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.0/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
    <style>
        body {
            background-color: #f8f9fa;
        }
        .recipe-table {
            background: white;
            border-radius: 8px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
            overflow: hidden;
        }
        .table {
            margin-bottom: 0;
        }
        .table thead {
            background-color: #f8f9fa;
            border-bottom: 2px solid #dee2e6;
        }
        .table tbody tr {
            border-bottom: 1px solid #dee2e6;
        }
        .status-badge {
            display: inline-block;
            padding: 0.5rem 0.75rem;
            border-radius: 4px;
            font-weight: 500;
            font-size: 0.85rem;
        }
        .status-pending {
            background-color: #fff3cd;
            color: #856404;
        }
        .status-approved {
            background-color: #d4edda;
            color: #155724;
        }
        .status-rejected {
            background-color: #f8d7da;
            color: #721c24;
        }
        .action-buttons {
            white-space: nowrap;
        }
        .action-buttons button, .action-buttons a {
            margin-right: 0.5rem;
            font-size: 0.9rem;
        }
        .no-recipes {
            text-align: center;
            padding: 3rem;
            background: white;
            border-radius: 8px;
            color: #6c757d;
        }
        .loading {
            display: none;
            text-align: center;
            padding: 2rem;
        }
        .spinner-border {
            width: 3rem;
            height: 3rem;
        }
        .header-section {
            background: white;
            padding: 2rem;
            border-radius: 8px;
            margin-bottom: 2rem;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }
        .stats-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
            gap: 1rem;
            margin-top: 1rem;
        }
        .stat-card {
            background: #f8f9fa;
            padding: 1rem;
            border-radius: 4px;
            text-align: center;
        }
        .stat-card .stat-number {
            font-size: 1.5rem;
            font-weight: bold;
            color: #0066cc;
        }
        .stat-card .stat-label {
            font-size: 0.85rem;
            color: #6c757d;
            margin-top: 0.5rem;
        }
    </style>
</head>
<body>
    <!-- Navigation -->
    <nav class="navbar navbar-expand-lg navbar-dark bg-dark">
        <div class="container-fluid">
            <a class="navbar-brand" href="index.jsp">
                <i class="fas fa-utensils"></i> Recipe Sharing Platform
            </a>
            <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
                <span class="navbar-toggler-icon"></span>
            </button>
            <div class="collapse navbar-collapse" id="navbarNav">
                <ul class="navbar-nav ms-auto">
                    <li class="nav-item">
                        <a class="nav-link" href="recipes.jsp">Discover</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="recipe-form.jsp">Create Recipe</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link active" href="my-recipes.jsp">My Recipes</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="user/dashboard.jsp">Dashboard</a>
                    </li>
                    <% 
                        Object userRole = session.getAttribute("USER_ROLE");
                        if ("ADMIN".equals(userRole)) {
                    %>
                    <li class="nav-item">
                        <a class="nav-link" href="admin/dashboard.jsp">Admin</a>
                    </li>
                    <% 
                        }
                    %>
                    <li class="nav-item">
                        <a class="nav-link" href="profile.jsp"><%= session.getAttribute("USER_NAME") %></a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="<%= request.getContextPath() %>/auth/logout">Logout</a>
                    </li>
                </ul>
            </div>
        </div>
    </nav>

    <!-- Main Content -->
    <div class="container mt-5 mb-5">
        <div class="header-section">
            <h1><i class="fas fa-book"></i> My Recipes</h1>
            <p class="text-muted">Manage all your published and pending recipes</p>
            <div class="stats-grid">
                <div class="stat-card">
                    <div class="stat-number" id="totalCount">0</div>
                    <div class="stat-label">Total Recipes</div>
                </div>
                <div class="stat-card">
                    <div class="stat-number" id="approvedCount">0</div>
                    <div class="stat-label">Approved</div>
                </div>
                <div class="stat-card">
                    <div class="stat-number" id="pendingCount">0</div>
                    <div class="stat-label">Pending</div>
                </div>
                <div class="stat-card">
                    <div class="stat-number" id="rejectedCount">0</div>
                    <div class="stat-label">Rejected</div>
                </div>
            </div>
        </div>

        <!-- Loading Indicator -->
        <div id="loadingSpinner" class="loading">
            <div class="spinner-border" role="status">
                <span class="visually-hidden">Loading...</span>
            </div>
            <p class="mt-2">Loading your recipes...</p>
        </div>

        <!-- Recipes Table -->
        <div id="recipesContainer" style="display: none;">
            <div class="recipe-table">
                <table class="table">
                    <thead>
                        <tr>
                            <th>Title</th>
                            <th>Category</th>
                            <th>Status</th>
                            <th>Created</th>
                            <th>Rating</th>
                            <th>Comments</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody id="recipesBody">
                        <!-- Recipes will be inserted here -->
                    </tbody>
                </table>
            </div>
        </div>

        <!-- No Recipes Message -->
        <div id="noRecipes" class="no-recipes" style="display: none;">
            <i class="fas fa-inbox" style="font-size: 3rem; color: #dee2e6;"></i>
            <h3 class="mt-3">No recipes yet</h3>
            <p class="text-muted">You haven't created any recipes yet. Get started by creating your first recipe!</p>
            <a href="recipe-form.jsp" class="btn btn-primary mt-3">Create Your First Recipe</a>
        </div>
    </div>

    <!-- Footer -->
    <footer class="bg-dark text-light py-4 mt-5">
        <div class="container text-center">
            <p>&copy; 2026 Recipe Sharing Platform. All rights reserved.</p>
        </div>
    </footer>

    <!-- Delete Confirmation Modal -->
    <div class="modal fade" id="deleteModal" tabindex="-1">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title">Delete Recipe</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body">
                    <p>Are you sure you want to delete this recipe? This action cannot be undone.</p>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                    <button type="button" class="btn btn-danger" id="confirmDeleteBtn">Delete</button>
                </div>
            </div>
        </div>
    </div>

    <script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.0/js/bootstrap.bundle.min.js"></script>
    <script>
        const contextPath = '<%= request.getContextPath() %>';
        let recipes = [];
        let deleteRecipeId = null;

        document.addEventListener('DOMContentLoaded', () => {
            loadRecipes();
        });

        function loadRecipes() {
            showLoading(true);
            fetch(`${contextPath}/recipe/my-recipes`)
                .then(response => {
                    if (!response.ok) throw new Error('Network response was not ok');
                    return response.json();
                })
                .then(data => {
                    showLoading(false);
                    recipes = data.recipes || [];
                    displayRecipes(recipes);
                })
                .catch(error => {
                    console.error('Error loading recipes:', error);
                    showLoading(false);
                    document.getElementById('noRecipes').style.display = 'block';
                });
        }

        function displayRecipes(recipes) {
            if (!recipes || recipes.length === 0) {
                document.getElementById('recipesContainer').style.display = 'none';
                document.getElementById('noRecipes').style.display = 'block';
                return;
            }

            // Calculate stats
            let totalCount = recipes.length;
            let approvedCount = recipes.filter(r => r.status === 'APPROVED').length;
            let pendingCount = recipes.filter(r => r.status === 'PENDING').length;
            let rejectedCount = recipes.filter(r => r.status === 'REJECTED').length;

            document.getElementById('totalCount').textContent = totalCount;
            document.getElementById('approvedCount').textContent = approvedCount;
            document.getElementById('pendingCount').textContent = pendingCount;
            document.getElementById('rejectedCount').textContent = rejectedCount;

            // Display table
            document.getElementById('noRecipes').style.display = 'none';
            document.getElementById('recipesContainer').style.display = 'block';

            const tbody = document.getElementById('recipesBody');
            tbody.innerHTML = recipes.map(recipe => `
                <tr>
                    <td>
                        <strong>${escapeHtml(recipe.title)}</strong>
                    </td>
                    <td>${escapeHtml(recipe.category)}</td>
                    <td>
                        <span class="status-badge status-${recipe.status.toLowerCase()}">
                            ${recipe.status}
                        </span>
                    </td>
                    <td>${new Date(recipe.createdAt).toLocaleDateString()}</td>
                    <td>
                        <i class="fas fa-star" style="color: #ffc107;"></i>
                        ${recipe.averageRating ? recipe.averageRating.toFixed(1) : 'N/A'}
                    </td>
                    <td>${recipe.commentCount || 0}</td>
                    <td>
                        <div class="action-buttons">
                            <a href="recipe-details.jsp?id=${escapeHtml(recipe.id)}" class="btn btn-sm btn-info">
                                <i class="fas fa-eye"></i> View
                            </a>
                            <a href="recipe-form.jsp?id=${escapeHtml(recipe.id)}" class="btn btn-sm btn-warning">
                                <i class="fas fa-edit"></i> Edit
                            </a>
                            <button class="btn btn-sm btn-danger" onclick="showDeleteConfirm('${escapeHtml(recipe.id)}')">
                                <i class="fas fa-trash"></i> Delete
                            </button>
                        </td>
                </tr>
            `).join('');
        }

        function showDeleteConfirm(recipeId) {
            deleteRecipeId = recipeId;
            const deleteModal = new bootstrap.Modal(document.getElementById('deleteModal'));
            deleteModal.show();
        }

        document.getElementById('confirmDeleteBtn')?.addEventListener('click', () => {
            if (!deleteRecipeId) return;
            
            const formData = new FormData();
            fetch(`${contextPath}/recipe/${deleteRecipeId}/delete`, {
                method: 'POST',
                body: formData
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    bootstrap.Modal.getInstance(document.getElementById('deleteModal')).hide();
                    loadRecipes();
                } else {
                    alert('Failed to delete recipe: ' + (data.error || 'Unknown error'));
                }
            })
            .catch(error => {
                console.error('Error deleting recipe:', error);
                alert('Error deleting recipe');
            });
        });

        function showLoading(isLoading) {
            document.getElementById('loadingSpinner').style.display = isLoading ? 'block' : 'none';
        }

        function escapeHtml(text) {
            if (!text) return '';
            const div = document.createElement('div');
            div.textContent = text;
            return div.innerHTML;
        }
    </script>
</body>
</html>
