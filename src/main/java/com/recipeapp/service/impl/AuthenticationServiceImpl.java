package com.recipeapp.service.impl;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.recipeapp.exception.AuthenticationException;
import com.recipeapp.exception.DatabaseException;
import com.recipeapp.exception.ValidationException;
import com.recipeapp.model.ActivityType;
import com.recipeapp.model.Role;
import com.recipeapp.model.User;
import com.recipeapp.service.ActivityLogService;
import com.recipeapp.service.AuthenticationService;
import com.recipeapp.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Optional;

/**
 * Implementation of AuthenticationService.
 * 
 * Uses Firebase Admin SDK to verify ID tokens and manage user authentication.
 * 
 * IMPORTANT: No passwords are handled in this service. All credential verification
 * is delegated to Firebase Authentication.
 */
public class AuthenticationServiceImpl implements AuthenticationService {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationServiceImpl.class);
    
    private final UserService userService;
    private final ActivityLogService activityLogService;

    public AuthenticationServiceImpl(UserService userService, ActivityLogService activityLogService) {
        this.userService = userService;
        this.activityLogService = activityLogService;
    }

    @Override
    public FirebaseToken verifyIdToken(String idToken) {
        try {
            if (idToken == null || idToken.isBlank()) {
                throw new AuthenticationException("ID token cannot be empty");
            }
            
            // Remove "Bearer " prefix if present
            String token = idToken;
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            // Verify using Firebase Admin SDK
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
            
            logger.debug("ID token verified for user: {}", decodedToken.getUid());
            return decodedToken;
            
        } catch (FirebaseAuthException e) {
            logger.warn("Firebase token verification failed: {}", e.getMessage());
            throw new AuthenticationException("Invalid or expired ID token: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error during token verification: {}", e.getMessage());
            throw new AuthenticationException("Token verification failed: " + e.getMessage(), e);
        }
    }

    @Override
    public User getOrCreateUserProfile(FirebaseToken firebaseToken, String userName) {
        try {
            if (firebaseToken == null) {
                throw new AuthenticationException("FirebaseToken cannot be null");
            }
            
            String uid = firebaseToken.getUid();
            String email = firebaseToken.getEmail();
            
            if (uid == null || uid.isBlank()) {
                throw new AuthenticationException("Firebase UID cannot be empty");
            }
            
            if (email == null || email.isBlank()) {
                throw new AuthenticationException("Firebase email cannot be empty");
            }
            
            // Try to get existing profile
            Optional<User> existingUser = userService.getUserById(uid);
            if (existingUser.isPresent()) {
                User user = existingUser.get();
                
                // Verify user is active
                if (!user.isActive()) {
                    throw new AuthenticationException("User account is inactive");
                }
                
                logger.debug("User profile found for uid: {}", uid);
                return user;
            }
            
            // Create new profile
            logger.info("Creating new user profile for uid: {} ({})", uid, email);
            
            User newUser = new User();
            newUser.setId(uid); // Use Firebase UID as application user ID
            newUser.setEmail(email);
            newUser.setName(userName != null && !userName.isBlank() ? userName : email.split("@")[0]);
            newUser.setRole(Role.USER); // Never create ADMIN through authentication
            newUser.setActive(true);
            newUser.setCreatedAt(Instant.now());
            newUser.setUpdatedAt(Instant.now());
            
            // Create in Firestore
            User created = userService.createUser(newUser);
            
            // Log the registration
            try {
                activityLogService.logActivity(uid, ActivityType.USER_REGISTERED,
                    "User registered via Firebase Authentication");
            } catch (Exception e) {
                logger.warn("Failed to log registration activity: {}", e.getMessage());
                // Don't throw - registration succeeded even if logging failed
            }
            
            return created;
            
        } catch (ValidationException | DatabaseException | AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error creating user profile: {}", e.getMessage());
            throw new DatabaseException("Failed to create user profile: " + e.getMessage(), e);
        }
    }

    @Override
    public User getUserByUid(String uid) {
        try {
            if (uid == null || uid.isBlank()) {
                throw new ValidationException("UID cannot be empty");
            }
            
            Optional<User> user = userService.getUserById(uid);
            return user.orElse(null);
            
        } catch (Exception e) {
            logger.error("Error getting user by uid: {}", e.getMessage());
            throw new DatabaseException("Failed to get user: " + e.getMessage(), e);
        }
    }
}
