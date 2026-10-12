package com.recipeapp.bootstrap;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.recipeapp.config.FirebaseConfig;
import com.recipeapp.model.Role;
import com.recipeapp.model.User;
import com.recipeapp.repository.UserRepository;
import com.recipeapp.repository.impl.FirestoreUserRepository;
import com.recipeapp.service.UserService;
import com.recipeapp.service.impl.UserServiceImpl;
import com.recipeapp.repository.ActivityLogRepository;
import com.recipeapp.repository.impl.FirestoreActivityLogRepository;
import com.recipeapp.service.ActivityLogService;
import com.recipeapp.service.impl.ActivityLogServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Optional;

/**
 * AdminBootstrap utility for creating an initial admin user.
 * 
 * This is a DEVELOPMENT/BOOTSTRAP utility that should NOT be exposed as a public HTTP endpoint.
 * 
 * It creates an admin user from environment variables:
 * - ADMIN_EMAIL: Email for admin user
 * - ADMIN_PASSWORD: Password for admin user (NOT stored, only used for Firebase Auth creation)
 * - ADMIN_NAME: Display name for admin user
 * 
 * Usage:
 * Set environment variables and call AdminBootstrap.createAdminUser() during application startup
 * or from a separate bootstrap command.
 * 
 * The utility is idempotent - running it multiple times is safe and will not create duplicates.
 * 
 * IMPORTANT:
 * - Do NOT expose this as a public API endpoint
 * - Do NOT include in production WAR without proper access controls
 * - The password is only used during Firebase Authentication account creation
 * - Passwords are NOT stored in the application
 */
public class AdminBootstrap {
    private static final Logger logger = LoggerFactory.getLogger(AdminBootstrap.class);
    
    /**
     * Create an admin user from environment variables.
     * 
     * Environment variables required:
     * - ADMIN_EMAIL
     * - ADMIN_PASSWORD
     * - ADMIN_NAME (optional)
     * 
     * @return true if admin was created or already exists, false if failed
     */
    public static boolean createAdminUser() {
        try {
            String adminEmail = System.getenv("ADMIN_EMAIL");
            String adminPassword = System.getenv("ADMIN_PASSWORD");
            String adminName = System.getenv("ADMIN_NAME");
            
            // Validation
            if (adminEmail == null || adminEmail.isBlank()) {
                logger.error("ADMIN_EMAIL environment variable not set");
                return false;
            }
            
            if (adminPassword == null || adminPassword.isBlank()) {
                logger.error("ADMIN_PASSWORD environment variable not set");
                return false;
            }
            
            if (adminName == null || adminName.isBlank()) {
                adminName = "Administrator";
            }
            
            // Initialize Firebase
            FirebaseConfig.initialize();
            
            // Check if user already exists in Firestore by email
            UserRepository userRepository = new FirestoreUserRepository();
            if (userRepository.existsByEmail(adminEmail)) {
                logger.info("Admin user already exists: {}", adminEmail);
                return true;
            }
            
            // Create in Firebase Authentication
            FirebaseAuth auth = FirebaseAuth.getInstance();
            UserRecord newUser;
            
            try {
                newUser = auth.createUser(
                    new UserRecord.CreateRequest()
                        .setEmail(adminEmail)
                        .setPassword(adminPassword)
                        .setDisplayName(adminName)
                );
                
                logger.info("Firebase Authentication user created: {}", newUser.getUid());
            } catch (FirebaseAuthException e) {
                if (e.getAuthErrorCode().equals("email-already-exists")) {
                    logger.info("Firebase Authentication user already exists: {}", adminEmail);
                    // User exists in Firebase, try to get their UID
                    try {
                        newUser = auth.getUserByEmail(adminEmail);
                    } catch (Exception ex) {
                        logger.error("Failed to get Firebase user by email: {}", ex.getMessage());
                        return false;
                    }
                } else {
                    logger.error("Failed to create Firebase Authentication user: {}", e.getMessage());
                    return false;
                }
            }
            
            String uid = newUser.getUid();
            
            // Create profile in Firestore
            ActivityLogRepository activityLogRepository = new FirestoreActivityLogRepository();
            ActivityLogService activityLogService = new ActivityLogServiceImpl(activityLogRepository);
            UserService userService = new UserServiceImpl(userRepository, activityLogService);
            
            User adminProfile = new User();
            adminProfile.setId(uid);
            adminProfile.setEmail(adminEmail);
            adminProfile.setName(adminName);
            adminProfile.setRole(Role.ADMIN);
            adminProfile.setActive(true);
            adminProfile.setCreatedAt(Instant.now());
            adminProfile.setUpdatedAt(Instant.now());
            
            User created = userService.createUser(adminProfile);
            
            logger.info("Admin user profile created in Firestore: {} ({})", created.getId(), adminEmail);
            return true;
            
        } catch (Exception e) {
            logger.error("Failed to create admin user: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Create admin user and exit with appropriate code.
     * 
     * Useful for command-line bootstrap.
     * 
     * Exit codes:
     * 0 = Success
     * 1 = Failure
     */
    public static void main(String[] args) {
        logger.info("Starting admin bootstrap...");
        
        if (createAdminUser()) {
            logger.info("Admin bootstrap completed successfully");
            System.exit(0);
        } else {
            logger.error("Admin bootstrap failed");
            System.exit(1);
        }
    }
}
