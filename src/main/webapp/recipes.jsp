<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Discover Recipes - Recipe Sharing Platform</title>
    <link href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.0/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
    <style>
        body {
            background-color: #f8f9fa;
        }
        .recipe-card {
            transition: transform 0.3s, box-shadow 0.3s;
            border: none;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }
        .recipe-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 4px 12px rgba(0,0,0,0.15);
        }
        .recipe-image {
            height: 200px;
            object-fit: cover;
            background-color: #e9ecef;
        }
        .recipe-image.placeholder {
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 3rem;
            color: #dee2e6;
        }
        .category-badge {
            display: inline-block;
            padding: 0.25rem 0.75rem;
            background-color: #e7f3ff;
            color: #0066cc;
            border-radius: 20px;
            font-size: 0.85rem;
            margin-bottom: 0.5rem;
        }
        .rating-stars {
            color: #ffc107;
            font-size: 0.9rem;
        }
        .search-section {
            background: white;
            padding: 2rem;
            margin-bottom: 2rem;
            border-radius: 8px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }
        .filter-section {
            background: white;
            padding: 1.5rem;
            border-radius: 8px;
            margin-bottom: 2rem;
        }
        .no-recipes {
            text-align: center;
            padding: 3rem;
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
                        <a class="nav-link active" href="recipes.jsp">Discover</a>
                    </li>
                    <% 
                        Object userId = session.getAttribute("USER_ID");
                        if (userId != null) {
                    %>
                    <li class="nav-item">
                        <a class="nav-link" href="recipe-form.jsp">Create Recipe</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="my-recipes.jsp">My Recipes</a>
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
                    <% 
                        } else {
                    %>
                    <li class="nav-item">
                        <a class="nav-link" href="login.jsp">Login</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="register.jsp">Register</a>
                    </li>
                    <% 
                        }
                    %>
                </ul>
            </div>
        </div>
    </nav>

    <!-- Main Content -->
    <div class="container mt-5 mb-5">
        <div class="row">
            <div class="col-md-12">
                <h1 class="mb-4">Discover Recipes</h1>
            </div>
        </div>

        <!-- Search Section -->
        <div class="search-section">
            <div class="row g-3">
                <div class="col-md-6">
                    <input type="text" id="searchInput" class="form-control form-control-lg" 
                           placeholder="Search recipes by title or keyword..." autofocus>
                </div>
                <div class="col-md-4">
                    <select id="categoryFilter" class="form-select form-select-lg">
                        <option value="">All Categories</option>
                        <option value="Italian">Italian</option>
                        <option value="Mexican">Mexican</option>
                        <option value="Asian">Asian</option>
                        <option value="Indian">Indian</option>
                        <option value="American">American</option>
                        <option value="Mediterranean">Mediterranean</option>
                        <option value="Vegetarian">Vegetarian</option>
                        <option value="Vegan">Vegan</option>
                        <option value="Dessert">Dessert</option>
                        <option value="Breakfast">Breakfast</option>
                        <option value="Lunch">Lunch</option>
                        <option value="Dinner">Dinner</option>
                    </select>
                </div>
                <div class="col-md-2">
                    <button id="searchBtn" class="btn btn-primary btn-lg w-100">Search</button>
                </div>
            </div>
        </div>

        <!-- Loading Indicator -->
        <div id="loadingSpinner" class="loading">
            <div class="spinner-border" role="status">
                <span class="visually-hidden">Loading...</span>
            </div>
            <p class="mt-2">Loading recipes...</p>
        </div>

        <!-- Recipes Grid -->
        <div id="recipesContainer" class="row g-4">
            <!-- Recipes will be loaded here -->
        </div>

        <!-- No Results Message -->
        <div id="noRecipes" class="no-recipes" style="display: none;">
            <i class="fas fa-search" style="font-size: 3rem; color: #dee2e6;"></i>
            <p class="mt-3">No recipes found. Try adjusting your search or filters.</p>
        </div>
    </div>

    <!-- Footer -->
    <footer class="bg-dark text-light py-4 mt-5">
        <div class="container text-center">
            <p>&copy; 2026 Recipe Sharing Platform. All rights reserved.</p>
        </div>
    </footer>

    <script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.0/js/bootstrap.bundle.min.js"></script>
    <script>
        // Load recipes on page load
        document.addEventListener('DOMContentLoaded', () => {
            loadRecipes();
        });

        // Search button click
        document.getElementById('searchBtn').addEventListener('click', () => {
            loadRecipes();
        });

        // Search input Enter key
        document.getElementById('searchInput').addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                loadRecipes();
            }
        });

        // Category filter change
        document.getElementById('categoryFilter').addEventListener('change', () => {
            loadRecipes();
        });

        function loadRecipes() {
            const search = document.getElementById('searchInput').value.trim();
            const category = document.getElementById('categoryFilter').value;
            
            let url = '<%= request.getContextPath() %>/recipe/';
            const params = new URLSearchParams();
            
            if (search) {
                params.append('search', search);
            }
            if (category) {
                params.append('category', category);
            }
            
            if (params.toString()) {
                url += '?' + params.toString();
            }
            
            showLoading(true);
            
            fetch(url)
                .then(response => {
                    if (!response.ok) throw new Error('Network response was not ok');
                    return response.json();
                })
                .then(data => {
                    showLoading(false);
                    displayRecipes(data.recipes || []);
                })
                .catch(error => {
                    console.error('Error loading recipes:', error);
                    showLoading(false);
                    showNoRecipes(true);
                });
        }

        function displayRecipes(recipes) {
            const container = document.getElementById('recipesContainer');
            const noRecipes = document.getElementById('noRecipes');
            
            if (!recipes || recipes.length === 0) {
                container.innerHTML = '';
                noRecipes.style.display = 'block';
                return;
            }
            
            noRecipes.style.display = 'none';
            container.innerHTML = recipes.map(recipe => `
                <div class="col-md-6 col-lg-4">
                    <div class="card recipe-card h-100">
                        <div class="recipe-image ${!recipe.imageUrl ? 'placeholder' : ''}">
                            ${recipe.imageUrl ? `<img src="${escapeHtml(recipe.imageUrl)}" alt="${escapeHtml(recipe.title)}" class="card-img-top">` : '<i class="fas fa-image"></i>'}
                        </div>
                        <div class="card-body d-flex flex-column">
                            <div class="category-badge">${escapeHtml(recipe.category)}</div>
                            <h5 class="card-title">${escapeHtml(recipe.title)}</h5>
                            <p class="card-text text-muted flex-grow-1">${escapeHtml(recipe.description.substring(0, 100))}${recipe.description.length > 100 ? '...' : ''}</p>
                            
                            <div class="d-flex justify-content-between align-items-center mb-3">
                                <small class="text-muted">
                                    <i class="fas fa-user"></i> ${escapeHtml(recipe.authorName || 'Unknown')}
                                </small>
                                <small class="text-muted">
                                    <i class="fas fa-clock"></i> ${recipe.prepTime || 'N/A'} min
                                </small>
                            </div>
                            
                            <div class="d-flex justify-content-between align-items-center">
                                <div class="rating-stars">
                                    ${'<i class="fas fa-star"></i>'.repeat(Math.round(recipe.averageRating || 0))}
                                    <span class="text-muted">(${recipe.ratingCount || 0})</span>
                                </div>
                                <a href="recipe-details.jsp?id=${escapeHtml(recipe.id)}" class="btn btn-sm btn-primary">
                                    View <i class="fas fa-arrow-right"></i>
                                </a>
                            </div>
                        </div>
                    </div>
                </div>
            `).join('');
        }

        function showLoading(isLoading) {
            document.getElementById('loadingSpinner').style.display = isLoading ? 'block' : 'none';
        }

        function showNoRecipes(show) {
            document.getElementById('noRecipes').style.display = show ? 'block' : 'none';
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
