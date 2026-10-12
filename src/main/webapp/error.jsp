<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    String contextPath = request.getContextPath();
    String code = request.getParameter("code");
    String message = request.getParameter("message");
    
    if (code == null) {
        code = "500";
    }
    
    String codeDescription = switch(code) {
        case "403" -> "Access Forbidden";
        case "404" -> "Not Found";
        case "500" -> "Internal Server Error";
        case "401" -> "Unauthorized";
        default -> "Error";
    };
    
    if (message == null || message.isBlank()) {
        message = switch(code) {
            case "403" -> "You do not have permission to access this resource.";
            case "404" -> "The requested resource was not found.";
            case "500" -> "An unexpected error occurred on the server.";
            case "401" -> "You are not authenticated. Please sign in.";
            default -> "An error has occurred.";
        };
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= codeDescription %> - Recipe Sharing Platform</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
        }
        
        .error-container {
            background: white;
            border-radius: 10px;
            box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
            padding: 50px 40px;
            text-align: center;
            max-width: 500px;
        }
        
        .error-code {
            font-size: 72px;
            font-weight: bold;
            color: #667eea;
            margin-bottom: 20px;
        }
        
        .error-title {
            font-size: 28px;
            color: #333;
            margin-bottom: 15px;
        }
        
        .error-message {
            font-size: 16px;
            color: #666;
            margin-bottom: 30px;
            line-height: 1.6;
        }
        
        .error-actions {
            display: flex;
            gap: 10px;
            justify-content: center;
        }
        
        .btn-action {
            padding: 12px 30px;
            border-radius: 6px;
            text-decoration: none;
            font-weight: 600;
            font-size: 14px;
            cursor: pointer;
            border: none;
            transition: transform 0.2s;
        }
        
        .btn-action:hover {
            transform: translateY(-2px);
        }
        
        .btn-home {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
        }
        
        .btn-back {
            background: #f0f0f0;
            color: #333;
            border: 1px solid #ddd;
        }
        
        .error-icon {
            font-size: 60px;
            margin-bottom: 20px;
        }
    </style>
</head>
<body>
    <div class="error-container">
        <div class="error-icon">
            ⚠️
        </div>
        
        <div class="error-code"><%= code %></div>
        <div class="error-title"><%= codeDescription %></div>
        <div class="error-message"><%= message %></div>
        
        <div class="error-actions">
            <a href="javascript:history.back()" class="btn-action btn-back">Go Back</a>
            <a href="<%= contextPath %>/index.jsp" class="btn-action btn-home">Go Home</a>
        </div>
    </div>
</body>
</html>
