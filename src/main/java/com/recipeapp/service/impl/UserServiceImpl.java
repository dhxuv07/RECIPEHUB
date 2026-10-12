package com.recipeapp.service.impl;

import com.recipeapp.exception.DatabaseException;
import com.recipeapp.exception.UnauthorizedException;
import com.recipeapp.exception.UserNotFoundException;
import com.recipeapp.exception.ValidationException;
import com.recipeapp.model.ActivityType;
import com.recipeapp.model.Role;
import com.recipeapp.model.User;
import com.recipeapp.repository.UserRepository;
import com.recipeapp.service.ActivityLogService;
import com.recipeapp.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of UserService.
 * 
 * Handles business logic for user management.
 */
public class UserServiceImpl implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    
    private final UserRepository userRepository;
    private final ActivityLogService activityLogService;

    public UserServiceImpl(UserRepository userRepository, ActivityLogService activityLogService) {
        this.userRepository = userRepository;
        this.activityLogService = activityLogService;
    }

    @Override
    public User createUser(User user) {
        try {
            // Validation
            if (user == null) {
                throw new ValidationException("User cannot be null");
            }
            
            user.validate();
            
            // Business rule: email must be unique
            if (userRepository.existsByEmail(user.getEmail())) {
                throw new ValidationException("Email already registered: " + user.getEmail());
            }
            
            // Business rule: public registration must never create ADMIN account
            if (user.getRole() == Role.ADMIN) {
                throw new UnauthorizedException("Cannot create admin user through public registration");
            }
            
            // Generate ID if not provided
            if (user.getId() == null || user.getId().isBlank()) {
                user.setId(UUID.randomUUID().toString());
            }
            
            // Set defaults
            user.setActive(true);
            user.setRole(Role.USER);
            
            User created = userRepository.create(user);
            
            // Log activity
            activityLogService.logActivity(user.getId(), ActivityType.USER_REGISTERED,
                    "User registered: " + user.getEmail());
            
            logger.info("User created: {} ({})", user.getId(), user.getEmail());
            return created;
        } catch (ValidationException | UnauthorizedException | DatabaseException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error creating user: {}", e.getMessage());
            throw new DatabaseException("Failed to create user: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<User> getUserById(String id) {
        try {
            return userRepository.findById(id);
        } catch (Exception e) {
            logger.error("Error retrieving user: {}", e.getMessage());
            throw new DatabaseException("Failed to retrieve user: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        try {
            return userRepository.findByEmail(email);
        } catch (Exception e) {
            logger.error("Error retrieving user by email: {}", e.getMessage());
            throw new DatabaseException("Failed to retrieve user: " + e.getMessage(), e);
        }
    }

    @Override
    public User updateUser(User user) {
        try {
            if (user == null) {
                throw new ValidationException("User cannot be null");
            }
            
            user.validate();
            
            // Verify user exists
            if (!userRepository.findById(user.getId()).isPresent()) {
                throw new UserNotFoundException("User not found: " + user.getId());
            }
            
            user.setUpdatedAt(Instant.now());
            User updated = userRepository.update(user);
            
            activityLogService.logActivity(user.getId(), ActivityType.PROFILE_UPDATED,
                    "Profile updated");
            
            logger.info("User updated: {}", user.getId());
            return updated;
        } catch (ValidationException | UserNotFoundException | DatabaseException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error updating user: {}", e.getMessage());
            throw new DatabaseException("Failed to update user: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteUser(String id) {
        try {
            if (id == null || id.isBlank()) {
                throw new ValidationException("User ID cannot be empty");
            }
            
            Optional<User> user = userRepository.findById(id);
            if (!user.isPresent()) {
                throw new UserNotFoundException("User not found: " + id);
            }
            
            userRepository.delete(id);
            
            activityLogService.logActivity(id, ActivityType.USER_DELETED,
                    "User account deleted");
            
            logger.info("User deleted: {}", id);
        } catch (ValidationException | UserNotFoundException | DatabaseException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting user: {}", e.getMessage());
            throw new DatabaseException("Failed to delete user: " + e.getMessage(), e);
        }
    }

    @Override
    public List<User> listUsers() {
        try {
            return userRepository.findAll();
        } catch (Exception e) {
            logger.error("Error listing users: {}", e.getMessage());
            throw new DatabaseException("Failed to list users: " + e.getMessage(), e);
        }
    }

    @Override
    public User changeRole(String userId, Role newRole) {
        try {
            if (userId == null || userId.isBlank()) {
                throw new ValidationException("User ID cannot be empty");
            }
            
            if (newRole == null) {
                throw new ValidationException("Role cannot be null");
            }
            
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
            
            Role oldRole = user.getRole();
            user.setRole(newRole);
            user.setUpdatedAt(Instant.now());
            
            User updated = userRepository.update(user);
            
            activityLogService.logActivity(userId, ActivityType.USER_ROLE_UPDATED,
                    "Role changed from " + oldRole + " to " + newRole);
            
            logger.info("User role changed: {} from {} to {}", userId, oldRole, newRole);
            return updated;
        } catch (ValidationException | UserNotFoundException | DatabaseException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error changing user role: {}", e.getMessage());
            throw new DatabaseException("Failed to change user role: " + e.getMessage(), e);
        }
    }

    @Override
    public User activateUser(String userId) {
        try {
            if (userId == null || userId.isBlank()) {
                throw new ValidationException("User ID cannot be empty");
            }
            
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
            
            if (user.isActive()) {
                return user; // Already active
            }
            
            user.setActive(true);
            user.setUpdatedAt(Instant.now());
            
            User updated = userRepository.update(user);
            
            activityLogService.logActivity(userId, ActivityType.USER_ACTIVATED,
                    "User account activated");
            
            logger.info("User activated: {}", userId);
            return updated;
        } catch (ValidationException | UserNotFoundException | DatabaseException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error activating user: {}", e.getMessage());
            throw new DatabaseException("Failed to activate user: " + e.getMessage(), e);
        }
    }

    @Override
    public User deactivateUser(String userId) {
        try {
            if (userId == null || userId.isBlank()) {
                throw new ValidationException("User ID cannot be empty");
            }
            
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
            
            if (!user.isActive()) {
                return user; // Already inactive
            }
            
            user.setActive(false);
            user.setUpdatedAt(Instant.now());
            
            User updated = userRepository.update(user);
            
            activityLogService.logActivity(userId, ActivityType.USER_DEACTIVATED,
                    "User account deactivated");
            
            logger.info("User deactivated: {}", userId);
            return updated;
        } catch (ValidationException | UserNotFoundException | DatabaseException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error deactivating user: {}", e.getMessage());
            throw new DatabaseException("Failed to deactivate user: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean emailExists(String email) {
        try {
            return userRepository.existsByEmail(email);
        } catch (Exception e) {
            logger.error("Error checking email existence: {}", e.getMessage());
            throw new DatabaseException("Failed to check email: " + e.getMessage(), e);
        }
    }

    @Override
    public long getTotalUserCount() {
        try {
            return userRepository.count();
        } catch (Exception e) {
            logger.error("Error counting users: {}", e.getMessage());
            throw new DatabaseException("Failed to count users: " + e.getMessage(), e);
        }
    }
}
