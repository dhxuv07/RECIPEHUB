<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    // Check if already authenticated
    if (session.getAttribute("AUTHENTICATED") != null && session.getAttribute("AUTHENTICATED").equals(true)) {
        response.sendRedirect(request.getContextPath() + "/user/dashboard.jsp");
        return;
    }
    
    String contextPath = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Register - Recipe Sharing Platform</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 20px;
        }
        
        .register-container {
            background: white;
            border-radius: 10px;
            box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
            padding: 40px;
            width: 100%;
            max-width: 450px;
        }
        
        .register-header {
            text-align: center;
            margin-bottom: 30px;
        }
        
        .register-header h1 {
            font-size: 28px;
            color: #333;
            margin-bottom: 10px;
        }
        
        .register-header p {
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
        
        .form-text {
            font-size: 12px;
            color: #999;
            margin-top: 5px;
        }
        
        .btn-register {
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
        
        .btn-register:hover {
            transform: translateY(-2px);
            color: white;
        }
        
        .btn-register:disabled {
            opacity: 0.6;
            cursor: not-allowed;
        }
        
        .alert {
            margin-bottom: 20px;
        }
        
        .signin-link {
            text-align: center;
            margin-top: 20px;
        }
        
        .signin-link a {
            color: #667eea;
            text-decoration: none;
            font-weight: 600;
        }
        
        .signin-link a:hover {
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
        
        .password-requirements {
            background: #f5f5f5;
            border-left: 4px solid #667eea;
            padding: 12px;
            border-radius: 4px;
            margin-top: 10px;
            font-size: 12px;
            color: #666;
        }
        
        .requirement {
            margin: 5px 0;
        }
        
        .requirement.met {
            color: #28a745;
        }
        
        .requirement.unmet {
            color: #dc3545;
        }
    </style>
</head>
<body>
    <div class="register-container">
        <div class="register-header">
            <h1>Recipe Sharing</h1>
            <p>Create your account</p>
        </div>
        
        <div id="errorAlert" class="alert alert-danger alert-dismissible fade show" role="alert">
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
        
        <div id="successAlert" class="alert alert-success alert-dismissible fade show" role="alert">
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
        
        <div class="loading-spinner">
            <div class="spinner-border text-primary" role="status">
                <span class="visually-hidden">Loading...</span>
            </div>
            <p>Creating your account...</p>
        </div>
        
        <form id="registerForm">
            <div class="form-group">
                <label for="name" class="form-label">Full Name</label>
                <input type="text" class="form-control" id="name" name="name" 
                       placeholder="Your full name" required>
            </div>
            
            <div class="form-group">
                <label for="email" class="form-label">Email</label>
                <input type="email" class="form-control" id="email" name="email" 
                       placeholder="your@email.com" required>
            </div>
            
            <div class="form-group">
                <label for="password" class="form-label">Password</label>
                <input type="password" class="form-control" id="password" name="password" 
                       placeholder="At least 6 characters" required>
                <div class="password-requirements">
                    <div class="requirement unmet" id="req-length">
                        ✓ At least 6 characters
                    </div>
                    <div class="requirement unmet" id="req-uppercase">
                        ✓ Contains uppercase letter
                    </div>
                    <div class="requirement unmet" id="req-lowercase">
                        ✓ Contains lowercase letter
                    </div>
                    <div class="requirement unmet" id="req-number">
                        ✓ Contains number
                    </div>
                </div>
            </div>
            
            <div class="form-group">
                <label for="confirmPassword" class="form-label">Confirm Password</label>
                <input type="password" class="form-control" id="confirmPassword" name="confirmPassword" 
                       placeholder="Re-enter your password" required>
            </div>
            
            <button type="submit" class="btn btn-register" id="registerBtn">
                Create Account
            </button>
        </form>
        
        <div class="signin-link">
            <p>Already have an account? <a href="<%= contextPath %>/login.jsp">Sign in here</a></p>
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
        const registerForm = document.getElementById('registerForm');
        const registerBtn = document.getElementById('registerBtn');
        const errorAlert = document.getElementById('errorAlert');
        const successAlert = document.getElementById('successAlert');
        const loadingSpinner = document.querySelector('.loading-spinner');
        const passwordInput = document.getElementById('password');
        
        // Password validation
        const requirements = {
            length: /^.{6,}$/,
            uppercase: /[A-Z]/,
            lowercase: /[a-z]/,
            number: /[0-9]/
        };
        
        passwordInput.addEventListener('input', () => {
            const password = passwordInput.value;
            
            document.getElementById('req-length').className = 
                'requirement ' + (requirements.length.test(password) ? 'met' : 'unmet');
            document.getElementById('req-uppercase').className = 
                'requirement ' + (requirements.uppercase.test(password) ? 'met' : 'unmet');
            document.getElementById('req-lowercase').className = 
                'requirement ' + (requirements.lowercase.test(password) ? 'met' : 'unmet');
            document.getElementById('req-number').className = 
                'requirement ' + (requirements.number.test(password) ? 'met' : 'unmet');
        });
        
        registerForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            
            const name = document.getElementById('name').value.trim();
            const email = document.getElementById('email').value.trim();
            const password = document.getElementById('password').value;
            const confirmPassword = document.getElementById('confirmPassword').value;
            
            // Clear alerts
            errorAlert.style.display = 'none';
            errorAlert.textContent = '';
            successAlert.style.display = 'none';
            successAlert.textContent = '';
            
            // Client-side validation
            if (!name || !email || !password || !confirmPassword) {
                errorAlert.textContent = 'All fields are required';
                errorAlert.style.display = 'block';
                return;
            }
            
            if (password !== confirmPassword) {
                errorAlert.textContent = 'Passwords do not match';
                errorAlert.style.display = 'block';
                return;
            }
            
            if (password.length < 6) {
                errorAlert.textContent = 'Password must be at least 6 characters';
                errorAlert.style.display = 'block';
                return;
            }
            
            // Show loading
            loadingSpinner.style.display = 'block';
            registerBtn.disabled = true;
            
            try {
                // Step 1: Register with Firebase
                const credentials = await FirebaseAuth.registerUser(email, password, name);
                successAlert.textContent = 'Account created successfully. Establishing session...';
                successAlert.style.display = 'block';
                
                // Step 2: Get ID token
                const idToken = await FirebaseAuth.getIdToken();
                
                // Step 3: Establish backend session
                await FirebaseAuth.establishBackendSession(idToken);
                
                // Step 4: Redirect to dashboard
                window.location.href = contextPath + '/user/dashboard.jsp';
                
            } catch (error) {
                console.error('Registration error:', error);
                errorAlert.textContent = error.message || 'Registration failed. Please try again.';
                errorAlert.style.display = 'block';
                
                loadingSpinner.style.display = 'none';
                registerBtn.disabled = false;
            }
        });
    </script>
</body>
</html>
