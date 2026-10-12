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
    String userEmail = (String) session.getAttribute("USER_EMAIL");
    String userRole = (String) session.getAttribute("USER_ROLE");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Profile - Recipe Sharing Platform</title>
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
        
        .profile-container {
            background: white;
            border-radius: 8px;
            padding: 40px;
            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
            margin-top: 30px;
            margin-bottom: 30px;
        }
        
        .profile-header {
            text-align: center;
            margin-bottom: 40px;
            padding-bottom: 30px;
            border-bottom: 2px solid #f0f0f0;
        }
        
        .profile-avatar {
            width: 100px;
            height: 100px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            margin: 0 auto 20px;
            font-size: 48px;
            color: white;
        }
        
        .profile-name {
            font-size: 28px;
            font-weight: bold;
            margin-bottom: 10px;
            color: #333;
        }
        
        .profile-email {
            font-size: 16px;
            color: #666;
            margin-bottom: 10px;
        }
        
        .profile-role {
            display: inline-block;
            background: #667eea;
            color: white;
            padding: 4px 12px;
            border-radius: 20px;
            font-size: 12px;
            font-weight: bold;
        }
        
        .form-group {
            margin-bottom: 25px;
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
        
        .form-control:disabled {
            background: #f5f5f5;
            cursor: not-allowed;
        }
        
        .info-section {
            background: #f9f9f9;
            padding: 20px;
            border-radius: 6px;
            margin-bottom: 20px;
        }
        
        .info-label {
            font-weight: 600;
            color: #333;
            margin-bottom: 5px;
        }
        
        .info-value {
            color: #666;
            margin-bottom: 15px;
        }
        
        .button-group {
            display: flex;
            gap: 10px;
            margin-top: 30px;
        }
        
        .btn-update {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border: none;
            padding: 12px 30px;
            border-radius: 6px;
            font-weight: 600;
            cursor: pointer;
            transition: transform 0.2s;
        }
        
        .btn-update:hover {
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
        
        #successAlert, #errorAlert {
            display: none;
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
                        <a class="nav-link" href="<%= contextPath %>/user/dashboard.jsp">Dashboard</a>
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
        <div class="profile-container">
            <div class="profile-header">
                <div class="profile-avatar">👤</div>
                <div class="profile-name"><%= userName %></div>
                <div class="profile-email"><%= userEmail %></div>
                <span class="profile-role"><%= userRole %></span>
            </div>
            
            <div id="successAlert" class="alert alert-success alert-dismissible fade show" role="alert">
                Profile updated successfully
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
            
            <div id="errorAlert" class="alert alert-danger alert-dismissible fade show" role="alert">
                <span id="errorMessage"></span>
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
            
            <div class="row">
                <div class="col-md-6">
                    <div class="info-section">
                        <div class="info-label">User ID</div>
                        <div class="info-value"><%= userId %></div>
                        
                        <div class="info-label">Email</div>
                        <div class="info-value"><%= userEmail %></div>
                        
                        <div class="info-label">Role</div>
                        <div class="info-value"><%= userRole %></div>
                    </div>
                </div>
                
                <div class="col-md-6">
                    <form id="profileForm">
                        <div class="form-group">
                            <label for="name" class="form-label">Display Name</label>
                            <input type="text" class="form-control" id="name" name="name" 
                                   value="<%= userName %>" placeholder="Your display name">
                        </div>
                        
                        <div class="button-group">
                            <button type="submit" class="btn-update">Update Profile</button>
                            <a href="<%= contextPath %>/user/dashboard.jsp" class="btn-cancel">Cancel</a>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
    
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    
    <script>
        const profileForm = document.getElementById('profileForm');
        const successAlert = document.getElementById('successAlert');
        const errorAlert = document.getElementById('errorAlert');
        const errorMessage = document.getElementById('errorMessage');
        
        profileForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            
            const name = document.getElementById('name').value.trim();
            
            if (!name) {
                errorMessage.textContent = 'Display name is required';
                errorAlert.style.display = 'block';
                return;
            }
            
            try {
                const response = await fetch('<%= contextPath %>/profile', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({ name: name })
                });
                
                const data = await response.json();
                
                if (response.ok && data.success) {
                    successAlert.style.display = 'block';
                    errorAlert.style.display = 'none';
                    
                    // Refresh page after 2 seconds
                    setTimeout(() => {
                        window.location.reload();
                    }, 2000);
                } else {
                    errorMessage.textContent = data.error || 'Failed to update profile';
                    errorAlert.style.display = 'block';
                    successAlert.style.display = 'none';
                }
            } catch (error) {
                console.error('Error:', error);
                errorMessage.textContent = 'An error occurred while updating your profile';
                errorAlert.style.display = 'block';
                successAlert.style.display = 'none';
            }
        });
    </script>
</body>
</html>
