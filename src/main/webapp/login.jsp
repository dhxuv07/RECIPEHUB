<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.net.URLEncoder" %>
<%
    // Check if already authenticated
    if (session.getAttribute("AUTHENTICATED") != null && session.getAttribute("AUTHENTICATED").equals(true)) {
        response.sendRedirect(request.getContextPath() + "/user/dashboard.jsp");
        return;
    }
    
    String contextPath = request.getContextPath();
    String error = request.getParameter("error");
    String message = request.getParameter("message");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login - Recipe Sharing Platform</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
        }
        
        .login-container {
            background: white;
            border-radius: 10px;
            box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
            padding: 40px;
            width: 100%;
            max-width: 400px;
        }
        
        .login-header {
            text-align: center;
            margin-bottom: 30px;
        }
        
        .login-header h1 {
            font-size: 28px;
            color: #333;
            margin-bottom: 10px;
        }
        
        .login-header p {
            color: #666;
            font-size: 14px;
        }
        
        .form-group {
            margin-bottom: 20px;
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
        
        .btn-login {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            border: none;
            border-radius: 6px;
            padding: 12px;
            color: white;
            font-weight: 600;
            width: 100%;
            cursor: pointer;
            transition: transform 0.2s;
        }
        
        .btn-login:hover {
            transform: translateY(-2px);
            color: white;
        }
        
        .btn-login:disabled {
            opacity: 0.6;
            cursor: not-allowed;
        }
        
        .alert {
            margin-bottom: 20px;
        }
        
        .signup-link {
            text-align: center;
            margin-top: 20px;
        }
        
        .signup-link a {
            color: #667eea;
            text-decoration: none;
            font-weight: 600;
        }
        
        .signup-link a:hover {
            text-decoration: underline;
        }
        
        .loading-spinner {
            display: none;
            text-align: center;
            margin-bottom: 20px;
        }
        
        .spinner-border {
            width: 1.5rem;
            height: 1.5rem;
        }
        
        #errorAlert, #successAlert {
            display: none;
        }
    </style>
</head>
<body>
    <div class="login-container">
        <div class="login-header">
            <h1>Recipe Sharing</h1>
            <p>Sign in to your account</p>
        </div>
        
        <% if (error != null) { %>
            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                <%= error %>
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        <% } %>
        
        <% if (message != null) { %>
            <div class="alert alert-info alert-dismissible fade show" role="alert">
                <%= message %>
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        <% } %>
        
        <div id="errorAlert" class="alert alert-danger" role="alert"></div>
        <div id="successAlert" class="alert alert-success" role="alert"></div>
        
        <div class="loading-spinner">
            <div class="spinner-border text-primary" role="status">
                <span class="visually-hidden">Loading...</span>
            </div>
            <p>Signing in...</p>
        </div>
        
        <form id="loginForm">
            <div class="form-group">
                <label for="email" class="form-label">Email</label>
                <input type="email" class="form-control" id="email" name="email" 
                       placeholder="your@email.com" required>
            </div>
            
            <div class="form-group">
                <label for="password" class="form-label">Password</label>
                <input type="password" class="form-control" id="password" name="password" 
                       placeholder="Enter your password" required>
            </div>
            
            <button type="submit" class="btn btn-login" id="loginBtn">
                Sign In
            </button>
        </form>
        
        <div class="signup-link">
            <p>Don't have an account? <a href="<%= contextPath %>/register.jsp">Register here</a></p>
        </div>
    </div>
    
    <!-- Firebase SDK -->
    <script src="https://www.gstatic.com/firebasejs/10.5.0/firebase-app.js"></script>
    <script src="https://www.gstatic.com/firebasejs/10.5.0/firebase-auth.js"></script>
    
    <!-- Application config and auth module -->
    <script>
        const contextPath = "<%= contextPath %>";
    </script>
    <script src="<%= contextPath %>/js/firebase-config.js"></script>
    <script src="<%= contextPath %>/js/firebase-auth.js"></script>
    
    <script>
        const loginForm = document.getElementById('loginForm');
        const loginBtn = document.getElementById('loginBtn');
        const errorAlert = document.getElementById('errorAlert');
        const successAlert = document.getElementById('successAlert');
        const loadingSpinner = document.querySelector('.loading-spinner');
        
        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            
            const email = document.getElementById('email').value;
            const password = document.getElementById('password').value;
            
            // Clear alerts
            errorAlert.style.display = 'none';
            successAlert.style.display = 'none';
            
            // Show loading
            loadingSpinner.style.display = 'block';
            loginBtn.disabled = true;
            
            try {
                // Step 1: Sign in with Firebase
                const credentials = await FirebaseAuth.signInUser(email, password);
                successAlert.textContent = 'Authentication successful. Establishing session...';
                successAlert.style.display = 'block';
                
                // Step 2: Get ID token
                const idToken = await FirebaseAuth.getIdToken();
                
                // Step 3: Establish backend session
                await FirebaseAuth.establishBackendSession(idToken);
                
                // Step 4: Redirect to dashboard
                window.location.href = contextPath + '/user/dashboard.jsp';
                
            } catch (error) {
                console.error('Login error:', error);
                errorAlert.textContent = error.message || 'Login failed. Please try again.';
                errorAlert.style.display = 'block';
                
                loadingSpinner.style.display = 'none';
                loginBtn.disabled = false;
            }
        });
    </script>
</body>
</html>
