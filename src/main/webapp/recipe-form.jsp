<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    String contextPath = request.getContextPath();
    Object authenticated = session.getAttribute("AUTHENTICATED");
    
    if (authenticated == null || !authenticated.equals(true)) {
        response.sendRedirect(contextPath + "/login.jsp");
        return;
    }
    
    String userName = (String) session.getAttribute("USER_NAME");
    String recipeId = request.getParameter("id");
    String mode = (recipeId != null && !recipeId.isBlank()) ? "edit" : "create";
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= mode.equals("edit") ? "Edit Recipe" : "Create Recipe" %> - Recipe Sharing Platform</title>
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
        
        .form-container {
            background: white;
            border-radius: 8px;
            padding: 30px;
            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
            margin-top: 30px;
            margin-bottom: 30px;
        }
        
        .form-header {
            margin-bottom: 30px;
        }
        
        .form-header h1 {
            color: #333;
            margin-bottom: 10px;
        }
        
        .form-header p {
            color: #666;
        }
        
        .form-section {
            margin-bottom: 30px;
        }
        
        .form-section h3 {
            color: #667eea;
            font-size: 18px;
            margin-bottom: 15px;
            padding-bottom: 10px;
            border-bottom: 2px solid #f0f0f0;
        }
        
        .form-label {
            font-weight: 600;
            color: #333;
            margin-bottom: 8px;
        }
        
        .form-control {
            border: 1px solid #e0e0e0;
            border-radius: 6px;
            padding: 10px 15px;
            font-size: 14px;
        }
        
        .form-control:focus {
            border-color: #667eea;
            box-shadow: 0 0 0 0.2rem rgba(102, 126, 234, 0.25);
        }
        
        .ingredient-item {
            display: flex;
            gap: 10px;
            margin-bottom: 10px;
            align-items: center;
        }
        
        .ingredient-input {
            flex: 1;
        }
        
        .btn-remove {
            background: #dc3545;
            color: white;
            border: none;
            padding: 8px 12px;
            border-radius: 4px;
            cursor: pointer;
        }
        
        .btn-remove:hover {
            background: #c82333;
        }
        
        .btn-add {
            background: #28a745;
            color: white;
            border: none;
            padding: 8px 15px;
            border-radius: 4px;
            cursor: pointer;
            margin-top: 5px;
        }
        
        .btn-add:hover {
            background: #218838;
        }
        
        .button-group {
            display: flex;
            gap: 10px;
            margin-top: 30px;
        }
        
        .btn-submit {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border: none;
            padding: 12px 30px;
            border-radius: 6px;
            font-weight: 600;
            cursor: pointer;
            transition: transform 0.2s;
        }
        
        .btn-submit:hover {
            color: white;
            transform: translateY(-2px);
        }
        
        .btn-cancel {
            background: #e0e0e0;
            color: #333;
            border: none;
            padding: 12px 30px;
            border-radius: 6px;
            font-weight: 600;
            cursor: pointer;
            text-decoration: none;
            display: inline-block;
        }
        
        .btn-cancel:hover {
            background: #d0d0d0;
            color: #333;
        }
        
        .alert {
            margin-bottom: 20px;
        }
        
        #errorAlert, #successAlert {
            display: none;
        }
        
        .required {
            color: #dc3545;
        }
        
        .help-text {
            font-size: 12px;
            color: #999;
            margin-top: 5px;
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
                        <a class="nav-link" href="<%= contextPath %>/recipes.jsp">Browse Recipes</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="<%= contextPath %>/my-recipes.jsp">My Recipes</a>
                    </li>
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
    <div class="container">
        <div class="form-container">
            <div class="form-header">
                <h1><% if (mode.equals("edit")) { %>Edit Recipe<% } else { %>Create New Recipe<% } %></h1>
                <p>Share your favorite recipe with the community. Recipes will be reviewed before appearing publicly.</p>
            </div>
            
            <div id="errorAlert" class="alert alert-danger alert-dismissible fade show" role="alert">
                <span id="errorMessage"></span>
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
            
            <div id="successAlert" class="alert alert-success alert-dismissible fade show" role="alert">
                Recipe saved successfully! <a href="<%= contextPath %>/user/my-recipes.jsp">View your recipes</a>
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
            
            <form id="recipeForm">
                <input type="hidden" id="recipeId" name="id" value="<%= recipeId != null ? recipeId : "" %>">
                
                <!-- Basic Information Section -->
                <div class="form-section">
                    <h3>Basic Information</h3>
                    
                    <div class="mb-3">
                        <label for="title" class="form-label">Recipe Title <span class="required">*</span></label>
                        <input type="text" class="form-control" id="title" name="title" 
                               placeholder="e.g., Homemade Pasta Carbonara" required
                               minlength="3" maxlength="200">
                        <div class="help-text">3-200 characters</div>
                    </div>
                    
                    <div class="mb-3">
                        <label for="category" class="form-label">Category <span class="required">*</span></label>
                        <select class="form-control" id="category" name="category" required>
                            <option value="">Select a category</option>
                            <option value="Breakfast">Breakfast</option>
                            <option value="Lunch">Lunch</option>
                            <option value="Dinner">Dinner</option>
                            <option value="Dessert">Dessert</option>
                            <option value="Appetizer">Appetizer</option>
                            <option value="Side Dish">Side Dish</option>
                            <option value="Salad">Salad</option>
                            <option value="Soup">Soup</option>
                            <option value="Beverage">Beverage</option>
                            <option value="Other">Other</option>
                        </select>
                    </div>
                    
                    <div class="mb-3">
                        <label for="description" class="form-label">Description</label>
                        <textarea class="form-control" id="description" name="description" 
                                  placeholder="Brief description of your recipe"
                                  rows="3" maxlength="500"></textarea>
                        <div class="help-text">Optional, up to 500 characters</div>
                    </div>
                    
                    <div class="mb-3">
                        <label for="imageUrl" class="form-label">Image URL</label>
                        <input type="url" class="form-control" id="imageUrl" name="imageUrl" 
                               placeholder="https://example.com/recipe-image.jpg">
                        <div class="help-text">Optional, must be a valid image URL</div>
                    </div>
                </div>
                
                <!-- Ingredients Section -->
                <div class="form-section">
                    <h3>Ingredients <span class="required">*</span></h3>
                    <div id="ingredientsList"></div>
                    <button type="button" class="btn-add" onclick="addIngredientField()">+ Add Ingredient</button>
                </div>
                
                <!-- Instructions Section -->
                <div class="form-section">
                    <h3>Instructions <span class="required">*</span></h3>
                    <div id="instructionsList"></div>
                    <button type="button" class="btn-add" onclick="addInstructionField()">+ Add Step</button>
                </div>
                
                <!-- Form Actions -->
                <div class="button-group">
                    <button type="submit" class="btn-submit">
                        <% if (mode.equals("edit")) { %>Save Changes<% } else { %>Create Recipe<% } %>
                    </button>
                    <% if (mode.equals("edit")) { %>
                        <a href="<%= contextPath %>/my-recipes.jsp" class="btn-cancel">Cancel</a>
                    <% } else { %>
                        <a href="<%= contextPath %>/recipes.jsp" class="btn-cancel">Cancel</a>
                    <% } %>
                </div>
            </form>
        </div>
    </div>
    
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    
    <script>
        const contextPath = "<%= contextPath %>";
        const mode = "<%= mode %>";
        const recipeId = "<%= recipeId != null ? recipeId : \"\" %>";
        
        // Initialize form
        document.addEventListener('DOMContentLoaded', function() {
            addIngredientField(); // Add one empty field
            addInstructionField(); // Add one empty field
            
            if (mode === 'edit' && recipeId) {
                loadRecipeData();
            }
        });
        
        // Add ingredient field
        function addIngredientField(value = '') {
            const list = document.getElementById('ingredientsList');
            const div = document.createElement('div');
            div.className = 'ingredient-item';
            div.innerHTML = `
                <input type="text" class="form-control ingredient-input" placeholder="e.g., 2 cups flour"
                       value="${escapeHtml(value)}" maxlength="200">
                <button type="button" class="btn-remove" onclick="this.parentElement.remove()">Remove</button>
            `;
            list.appendChild(div);
        }
        
        // Add instruction field
        function addInstructionField(value = '') {
            const list = document.getElementById('instructionsList');
            const div = document.createElement('div');
            div.className = 'ingredient-item';
            div.innerHTML = `
                <textarea class="form-control ingredient-input" placeholder="e.g., Preheat oven to 350°F"
                          rows="2" maxlength="500">${escapeHtml(value)}</textarea>
                <button type="button" class="btn-remove" onclick="this.parentElement.remove()">Remove</button>
            `;
            list.appendChild(div);
        }
        
        // Escape HTML to prevent XSS
        function escapeHtml(text) {
            const div = document.createElement('div');
            div.textContent = text;
            return div.innerHTML;
        }
        
        // Load recipe data for editing
        async function loadRecipeData() {
            try {
                const response = await fetch(contextPath + '/recipe/' + recipeId);
                if (!response.ok) throw new Error('Recipe not found');
                const data = await response.json();
                
                const recipe = data.recipe;
                document.getElementById('title').value = recipe.title || '';
                document.getElementById('category').value = recipe.category || '';
                document.getElementById('description').value = recipe.description || '';
                document.getElementById('imageUrl').value = recipe.imageUrl || '';
                
                // Clear and populate ingredients
                document.getElementById('ingredientsList').innerHTML = '';
                if (recipe.ingredients && recipe.ingredients.length > 0) {
                    recipe.ingredients.forEach(ingredient => addIngredientField(ingredient));
                }
                
                // Clear and populate instructions
                document.getElementById('instructionsList').innerHTML = '';
                if (recipe.instructions && recipe.instructions.length > 0) {
                    recipe.instructions.forEach(instruction => addInstructionField(instruction));
                }
            } catch (error) {
                console.error('Error loading recipe:', error);
                document.getElementById('errorMessage').textContent = 'Failed to load recipe data';
                document.getElementById('errorAlert').style.display = 'block';
            }
        }
        
        // Form submission
        document.getElementById('recipeForm').addEventListener('submit', async (e) => {
            e.preventDefault();
            
            // Validate
            const title = document.getElementById('title').value.trim();
            const category = document.getElementById('category').value.trim();
            
            if (!title || !category) {
                document.getElementById('errorMessage').textContent = 'Title and category are required';
                document.getElementById('errorAlert').style.display = 'block';
                return;
            }
            
            // Collect ingredients
            const ingredients = [];
            document.querySelectorAll('#ingredientsList .ingredient-input').forEach(input => {
                if (input.value.trim()) ingredients.push(input.value.trim());
            });
            
            if (ingredients.length === 0) {
                document.getElementById('errorMessage').textContent = 'Please add at least one ingredient';
                document.getElementById('errorAlert').style.display = 'block';
                return;
            }
            
            // Collect instructions
            const instructions = [];
            document.querySelectorAll('#instructionsList .ingredient-input').forEach(input => {
                if (input.value.trim()) instructions.push(input.value.trim());
            });
            
            if (instructions.length === 0) {
                document.getElementById('errorMessage').textContent = 'Please add at least one instruction';
                document.getElementById('errorAlert').style.display = 'block';
                return;
            }
            
            // Build request
            const isEdit = mode === 'edit' && recipeId;
            const endpoint = isEdit 
                ? contextPath + '/recipe/' + recipeId + '/update'
                : contextPath + '/recipe/create';
            
            const payload = {
                title: title,
                category: category,
                description: document.getElementById('description').value.trim(),
                imageUrl: document.getElementById('imageUrl').value.trim(),
                ingredients: ingredients,
                instructions: instructions
            };
            
            try {
                const response = await fetch(endpoint, {
                    method: 'POST',
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify(payload)
                });
                
                const data = await response.json();
                
                if (response.ok && data.success) {
                    document.getElementById('errorAlert').style.display = 'none';
                    document.getElementById('successAlert').style.display = 'block';
                    
                    // Redirect after 2 seconds
                    setTimeout(() => {
                        window.location.href = contextPath + '/my-recipes.jsp';
                    }, 2000);
                } else {
                    document.getElementById('errorMessage').textContent = data.error || 'Failed to save recipe';
                    document.getElementById('errorAlert').style.display = 'block';
                }
            } catch (error) {
                console.error('Error:', error);
                document.getElementById('errorMessage').textContent = 'An error occurred while saving the recipe';
                document.getElementById('errorAlert').style.display = 'block';
            }
        });
    </script>
</body>
</html>
