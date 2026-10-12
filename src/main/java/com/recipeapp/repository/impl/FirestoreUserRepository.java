package com.recipeapp.repository.impl;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.recipeapp.config.FirebaseConfig;
import com.recipeapp.exception.DatabaseException;
import com.recipeapp.model.User;
import com.recipeapp.repository.UserRepository;
import com.recipeapp.util.FirestoreMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Firestore implementation of UserRepository.
 * 
 * Handles all User persistence operations against Firestore.
 */
public class FirestoreUserRepository implements UserRepository {
    private static final Logger logger = LoggerFactory.getLogger(FirestoreUserRepository.class);
    private static final String COLLECTION = "users";
    
    private final Firestore firestore;

    public FirestoreUserRepository() {
        this.firestore = FirebaseConfig.getFirestore();
    }

    @Override
    public User create(User user) {
        try {
            user.validate();
            
            if (firestore.collection(COLLECTION).document(user.getId()).get().get().exists()) {
                throw new DatabaseException("User with ID " + user.getId() + " already exists");
            }
            
            Instant now = Instant.now();
            user.setCreatedAt(now);
            user.setUpdatedAt(now);
            
            firestore.collection(COLLECTION)
                    .document(user.getId())
                    .set(FirestoreMapper.userToMap(user))
                    .get();
            
            logger.info("User created: {}", user.getId());
            return user;
        } catch (Exception e) {
            logger.error("Error creating user: {}", e.getMessage());
            throw new DatabaseException("Failed to create user: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<User> findById(String id) {
        try {
            DocumentSnapshot snapshot = firestore.collection(COLLECTION)
                    .document(id)
                    .get()
                    .get();
            
            if (snapshot.exists()) {
                return Optional.of(FirestoreMapper.mapToUser(snapshot));
            }
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Error finding user by ID: {}", e.getMessage());
            throw new DatabaseException("Failed to find user: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION)
                    .whereEqualTo("email", email)
                    .get()
                    .get();
            
            if (querySnapshot.isEmpty()) {
                return Optional.empty();
            }
            
            return Optional.of(FirestoreMapper.mapToUser(querySnapshot.getDocuments().get(0)));
        } catch (Exception e) {
            logger.error("Error finding user by email: {}", e.getMessage());
            throw new DatabaseException("Failed to find user by email: " + e.getMessage(), e);
        }
    }

    @Override
    public List<User> findAll() {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION)
                    .get()
                    .get();
            
            return querySnapshot.getDocuments()
                    .stream()
                    .map(FirestoreMapper::mapToUser)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error finding all users: {}", e.getMessage());
            throw new DatabaseException("Failed to find users: " + e.getMessage(), e);
        }
    }

    @Override
    public User update(User user) {
        try {
            user.validate();
            
            if (!firestore.collection(COLLECTION).document(user.getId()).get().get().exists()) {
                throw new DatabaseException("User with ID " + user.getId() + " not found");
            }
            
            user.setUpdatedAt(Instant.now());
            
            firestore.collection(COLLECTION)
                    .document(user.getId())
                    .set(FirestoreMapper.userToMap(user))
                    .get();
            
            logger.info("User updated: {}", user.getId());
            return user;
        } catch (Exception e) {
            logger.error("Error updating user: {}", e.getMessage());
            throw new DatabaseException("Failed to update user: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String id) {
        try {
            firestore.collection(COLLECTION)
                    .document(id)
                    .delete()
                    .get();
            
            logger.info("User deleted: {}", id);
        } catch (Exception e) {
            logger.error("Error deleting user: {}", e.getMessage());
            throw new DatabaseException("Failed to delete user: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION)
                    .whereEqualTo("email", email)
                    .get()
                    .get();
            
            return !querySnapshot.isEmpty();
        } catch (Exception e) {
            logger.error("Error checking email existence: {}", e.getMessage());
            throw new DatabaseException("Failed to check email existence: " + e.getMessage(), e);
        }
    }

    @Override
    public long count() {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION)
                    .get()
                    .get();
            
            return querySnapshot.size();
        } catch (Exception e) {
            logger.error("Error counting users: {}", e.getMessage());
            throw new DatabaseException("Failed to count users: " + e.getMessage(), e);
        }
    }
}
