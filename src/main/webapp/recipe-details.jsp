<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.google.gson.Gson" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Recipe Details - Recipe Sharing Platform</title>
    <link href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.0/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
    <style>
        body {
            background-color: #f8f9fa;
        }
        .recipe-header {
            background: white;
            padding: 2rem;
            border-radius: 8px;
            margin-bottom: 2rem;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }
        .recipe-image-container {
            height: 400px;
            background-color: #e9ecef;
            border-radius: 8px;
            overflow: hidden;
            display: flex;
            align-items: center;
            justify-content: center;
            margin-bottom: 2rem;
        }
        .recipe-image-container img {
            width: 100%;
            height: 100%;
            object-fit: cover;
        }
        .recipe-image-container.placeholder {
            font-size: 4rem;
            color: #dee2e6;
        }
        .recipe-meta {
            background: #f8f9fa;
            padding: 1.5rem;
            border-radius: 8px;
            margin-bottom: 2rem;
        }
        .meta-item {
            text-align: center;
            padding: 1rem;
        }
        .meta-item i {
            font-size: 1.5rem;
            color: #0066cc;
            margin-bottom: 0.5rem;
        }
        .ingredients, .instructions {
            background: white;
            padding: 2rem;
            border-radius: 8px;
            margin-bottom: 2rem;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }
        .ingredients ul, .instructions ol {
            margin-bottom: 0;
        }
        .ingredients li, .instructions li {
            padding: 0.5rem 0;
            border-bottom: 1px solid #e9ecef;
        }
        .ingredients li:last-child, .instructions li:last-child {
            border-bottom: none;
        }
        .rating-section, .comments-section {
            background: white;
            padding: 2rem;
            border-radius: 8px;
            margin-bottom: 2rem;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }
        .star-rating {
            font-size: 2rem;
            cursor: pointer;
            color: #dee2e6;
            user-select: none;
        }
        .star-rating i {
            margin-right: 0.5rem;
            transition: color 0.2s;
        }
        .star-rating i:hover,
        .star-rating i.active {
            color: #ffc107;
        }
        .comment-item {
            padding: 1rem;
            border: 1px solid #e9ecef;
            border-radius: 4px;
            margin-bottom: 1rem;
        }
        .comment-header {
            display: flex;
            justify-content: space-between;
            margin-bottom: 0.5rem;
        }
        .comment-author {
            font-weight: bold;
            color: #333;
        }
        .comment-date {
            font-size: 0.85rem;
            color: #999;
        }
        .comment-actions {
            margin-top: 0.5rem;
            font-size: 0.85rem;
        }
        .comment-actions a {
            color: #0066cc;
            cursor: pointer;
            margin-right: 1rem;
            text-decoration: none;
        }
        .comment-actions a:hover {
            text-decoration: underline;
        }
        .average-rating {
            font-size: 1.5rem;
            color: #ffc107;
            margin-right: 1rem;
        }
        .edit-delete-buttons {
            margin-top: 1rem;
        }
        .loading {
            display: none;
            text-align: center;
            padding: 2rem;
        }
        .error-message {
            display: none;
            padding: 1rem;
            background-color: #f8d7da;
            border: 1px solid #f5c6cb;
            border-radius: 4px;
            color: #721c24;
            margin-bottom: 2rem;
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
        <div id="errorMessage" class="error-message"></div>

        <!-- Loading Indicator -->
        <div id="loadingSpinner" class="loading">
            <div class="spinner-border" role="status">
                <span class="visually-hidden">Loading...</span>
            </div>
            <p class="mt-2">Loading recipe...</p>
        </div>

        <!-- Recipe Content -->
        <div id="recipeContent" style="display: none;">
            <!-- Back Button -->
            <div class="mb-3">
                <a href="recipes.jsp" class="btn btn-secondary btn-sm">
                    <i class="fas fa-arrow-left"></i> Back to Recipes
                </a>
            </div>

            <!-- Recipe Header -->
            <div class="recipe-header">
                <div class="row">
                    <div class="col-md-8">
                        <div class="category-badge mb-3" id="categoryBadge"></div>
                        <h1 id="recipeTitle"></h1>
                        <p class="text-muted mb-3" id="recipeDescription"></p>
                        <div class="d-flex align-items-center mb-3">
                            <div class="average-rating">
                                <span id="averageRating">0</span>
                                <i class="fas fa-star"></i>
                            </div>
                            <span class="text-muted">(<span id="ratingCount">0</span> ratings)</span>
                        </div>
                        <div id="editDeleteButtons" class="edit-delete-buttons"></div>
                    </div>
                    <div class="col-md-4 text-end">
                        <div class="text-muted">
                            <p><strong>Author:</strong> <span id="authorName"></span></p>
                            <p><strong>Created:</strong> <span id="createdDate"></span></p>
                            <p id="statusInfo"></p>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Recipe Image -->
            <div class="row">
                <div class="col-md-12">
                    <div id="imageContainer" class="recipe-image-container placeholder">
                        <i class="fas fa-image"></i>
                    </div>
                </div>
            </div>

            <!-- Recipe Metadata -->
            <div class="recipe-meta">
                <div class="row">
                    <div class="col-md-3 meta-item">
                        <div><i class="fas fa-clock"></i></div>
                        <div>Prep Time</div>
                        <strong id="prepTime">N/A</strong>
                    </div>
                    <div class="col-md-3 meta-item">
                        <div><i class="fas fa-fire"></i></div>
                        <div>Servings</div>
                        <strong id="servings">N/A</strong>
                    </div>
                    <div class="col-md-3 meta-item">
                        <div><i class="fas fa-utensils"></i></div>
                        <div>Difficulty</div>
                        <strong id="difficulty">N/A</strong>
                    </div>
                    <div class="col-md-3 meta-item">
                        <div><i class="fas fa-comments"></i></div>
                        <div>Comments</div>
                        <strong id="commentCount">0</strong>
                    </div>
                </div>
            </div>

            <!-- Ingredients -->
            <div class="ingredients">
                <h3><i class="fas fa-list"></i> Ingredients</h3>
                <ul id="ingredientsList" class="mt-3">
                    <!-- Populated by JavaScript -->
                </ul>
            </div>

            <!-- Instructions -->
            <div class="instructions">
                <h3><i class="fas fa-tasks"></i> Instructions</h3>
                <ol id="instructionsList" class="mt-3">
                    <!-- Populated by JavaScript -->
                </ol>
            </div>

            <!-- Rating Section -->
            <% if (userId != null) { %>
            <div class="rating-section">
                <h3><i class="fas fa-star"></i> Rate This Recipe</h3>
                <p class="text-muted mb-3">Rate this recipe to help other users (1-5 stars)</p>
                <div class="star-rating" id="ratingStars">
                    <i class="fas fa-star" data-rating="1"></i>
                    <i class="fas fa-star" data-rating="2"></i>
                    <i class="fas fa-star" data-rating="3"></i>
                    <i class="fas fa-star" data-rating="4"></i>
                    <i class="fas fa-star" data-rating="5"></i>
                </div>
                <p class="mt-3 text-muted">Selected: <span id="ratingDisplay">0</span> stars</p>
                <button id="submitRatingBtn" class="btn btn-primary mt-2">Submit Rating</button>
            </div>
            <% } %>

            <!-- Comments Section -->
            <div class="comments-section">
                <h3><i class="fas fa-comments"></i> Comments (<span id="commentCountHeader">0</span>)</h3>
                
                <% if (userId != null) { %>
                <div class="mb-4 mt-4">
                    <h5>Add Your Comment</h5>
                    <form id="commentForm">
                        <textarea id="commentText" class="form-control" rows="3" 
                                  placeholder="Share your thoughts about this recipe..." required></textarea>
                        <button type="submit" class="btn btn-primary mt-2">Post Comment</button>
                    </form>
                </div>
                <% } %>

                <div id="commentsList">
                    <!-- Comments will be loaded here -->
                </div>

                <div id="noComments" class="text-muted text-center py-4">
                    <p>No comments yet. Be the first to comment!</p>
                </div>
            </div>
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
        const contextPath = '<%= request.getContextPath() %>';
        const recipeId = new URLSearchParams(window.location.search).get('id');
        let currentRating = 0;
        let recipeData = null;

        document.addEventListener('DOMContentLoaded', () => {
            if (!recipeId) {
                showError('Recipe ID not provided');
                return;
            }
            loadRecipe();
            setupRatingStars();
            <% if (userId != null) { %>
            setupCommentForm();
            <% } %>
        });

        function loadRecipe() {
            showLoading(true);
            fetch(`${contextPath}/recipe/${recipeId}`)
                .then(response => {
                    if (!response.ok) throw new Error('Recipe not found');
                    return response.json();
                })
                .then(data => {
                    recipeData = data.recipe;
                    displayRecipe(recipeData);
                    loadRatingsAndComments();
                    showLoading(false);
                })
                .catch(error => {
                    console.error('Error loading recipe:', error);
                    showError('Failed to load recipe. Please try again later.');
                    showLoading(false);
                });
        }

        function displayRecipe(recipe) {
            const category = recipe.category || 'Unknown';
            document.getElementById('categoryBadge').innerHTML = `<span class="badge bg-info">${escapeHtml(category)}</span>`;
            document.getElementById('recipeTitle').textContent = escapeHtml(recipe.title);
            document.getElementById('recipeDescription').textContent = escapeHtml(recipe.description);
            document.getElementById('authorName').textContent = escapeHtml(recipe.authorName || 'Unknown');
            document.getElementById('createdDate').textContent = new Date(recipe.createdAt).toLocaleDateString();
            
            if (recipe.status === 'PENDING') {
                document.getElementById('statusInfo').innerHTML = '<p class="text-warning"><strong>Status:</strong> Pending Approval</p>';
            } else if (recipe.status === 'REJECTED') {
                document.getElementById('statusInfo').innerHTML = '<p class="text-danger"><strong>Status:</strong> Rejected</p>';
            }
            
            // Recipe metadata
            document.getElementById('prepTime').textContent = recipe.prepTime ? `${recipe.prepTime} min` : 'N/A';
            document.getElementById('servings').textContent = recipe.servings || 'N/A';
            document.getElementById('difficulty').textContent = recipe.difficulty || 'N/A';
            
            // Image
            const imageContainer = document.getElementById('imageContainer');
            if (recipe.imageUrl) {
                imageContainer.innerHTML = `<img src="${escapeHtml(recipe.imageUrl)}" alt="${escapeHtml(recipe.title)}">`;
                imageContainer.classList.remove('placeholder');
            }
            
            // Ingredients
            const ingredientsList = document.getElementById('ingredientsList');
            ingredientsList.innerHTML = (recipe.ingredients || []).map(ing => 
                `<li>${escapeHtml(ing)}</li>`
            ).join('');
            
            // Instructions
            const instructionsList = document.getElementById('instructionsList');
            instructionsList.innerHTML = (recipe.instructions || []).map(inst => 
                `<li>${escapeHtml(inst)}</li>`
            ).join('');
            
            // Edit/Delete buttons (only for owner)
            const editDeleteButtons = document.getElementById('editDeleteButtons');
            <% if (userId != null) { %>
            const currentUserId = '<%= userId %>';
            if (currentUserId === recipe.authorId) {
                editDeleteButtons.innerHTML = `
                    <a href="recipe-form.jsp?id=${escapeHtml(recipe.id)}" class="btn btn-warning btn-sm">
                        <i class="fas fa-edit"></i> Edit
                    </a>
                    <button class="btn btn-danger btn-sm ms-2" onclick="deleteRecipe('${escapeHtml(recipe.id)}')">
                        <i class="fas fa-trash"></i> Delete
                    </button>
                `;
            }
            <% } %>
            
            document.getElementById('recipeContent').style.display = 'block';
        }

        function loadRatingsAndComments() {
            // Load ratings (to be implemented with RatingServlet)
            // Load comments (to be implemented with CommentServlet)
            loadComments();
        }

        function loadComments() {
            // Fetch comments for this recipe (to be implemented)
            // For now, show empty state
            document.getElementById('commentsList').innerHTML = '';
            document.getElementById('noComments').style.display = 'block';
        }

        function setupRatingStars() {
            const stars = document.querySelectorAll('#ratingStars i');
            stars.forEach(star => {
                star.addEventListener('click', () => {
                    currentRating = parseInt(star.dataset.rating);
                    updateStarDisplay();
                    document.getElementById('ratingDisplay').textContent = currentRating;
                });
                star.addEventListener('mouseenter', () => {
                    const hoverRating = parseInt(star.dataset.rating);
                    stars.forEach((s, index) => {
                        if (index < hoverRating) {
                            s.classList.remove('far');
                            s.classList.add('fas');
                            s.style.color = '#ffc107';
                        } else {
                            s.classList.add('far');
                            s.classList.remove('fas');
                            s.style.color = '#dee2e6';
                        }
                    });
                });
            });
            document.getElementById('ratingStars').addEventListener('mouseleave', updateStarDisplay);
        }

        function updateStarDisplay() {
            const stars = document.querySelectorAll('#ratingStars i');
            stars.forEach((star, index) => {
                if (index < currentRating) {
                    star.classList.remove('far');
                    star.classList.add('fas');
                    star.style.color = '#ffc107';
                } else {
                    star.classList.add('far');
                    star.classList.remove('fas');
                    star.style.color = '#dee2e6';
                }
            });
        }

        document.getElementById('submitRatingBtn')?.addEventListener('click', () => {
            if (currentRating === 0) {
                alert('Please select a rating');
                return;
            }
            
            fetch(`${contextPath}/rating/`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    recipeId: recipeId,
                    score: currentRating
                })
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    alert('Rating submitted successfully!');
                    currentRating = 0;
                    updateStarDisplay();
                    loadRecipe(); // Reload to update average rating
                } else {
                    alert('Failed to submit rating: ' + (data.error || 'Unknown error'));
                }
            })
            .catch(error => {
                console.error('Error submitting rating:', error);
                alert('Error submitting rating');
            });
        });

        function setupCommentForm() {
            document.getElementById('commentForm')?.addEventListener('submit', (e) => {
                e.preventDefault();
                const text = document.getElementById('commentText').value.trim();
                if (!text) {
                    alert('Comment cannot be empty');
                    return;
                }
                
                fetch(`${contextPath}/comment/`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        recipeId: recipeId,
                        text: text
                    })
                })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        document.getElementById('commentText').value = '';
                        loadComments();
                    } else {
                        alert('Failed to post comment: ' + (data.error || 'Unknown error'));
                    }
                })
                .catch(error => {
                    console.error('Error posting comment:', error);
                    alert('Error posting comment');
                });
            });
        }

        function deleteRecipe(id) {
            if (!confirm('Are you sure you want to delete this recipe?')) {
                return;
            }
            
            const formData = new FormData();
            fetch(`${contextPath}/recipe/${id}/delete`, {
                method: 'POST',
                body: formData
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    alert('Recipe deleted successfully');
                    window.location.href = 'my-recipes.jsp';
                } else {
                    alert('Failed to delete recipe: ' + (data.error || 'Unknown error'));
                }
            })
            .catch(error => {
                console.error('Error deleting recipe:', error);
                alert('Error deleting recipe');
            });
        }

        function showLoading(isLoading) {
            document.getElementById('loadingSpinner').style.display = isLoading ? 'block' : 'none';
        }

        function showError(message) {
            const errorDiv = document.getElementById('errorMessage');
            errorDiv.textContent = message;
            errorDiv.style.display = 'block';
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
