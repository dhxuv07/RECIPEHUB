package com.recipeapp.repository.impl;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.recipeapp.config.FirebaseConfig;
import com.recipeapp.exception.DatabaseException;
import com.recipeapp.model.AppSetting;
import com.recipeapp.repository.SettingsRepository;
import com.recipeapp.util.FirestoreMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Firestore implementation of SettingsRepository.
 * 
 * Handles all AppSetting persistence operations against Firestore.
 */
public class FirestoreSettingsRepository implements SettingsRepository {
    private static final Logger logger = LoggerFactory.getLogger(FirestoreSettingsRepository.class);
    private static final String COLLECTION = "settings";
    
    private final Firestore firestore;

    public FirestoreSettingsRepository() {
        this.firestore = FirebaseConfig.getFirestore();
    }

    @Override
    public Optional<AppSetting> findByKey(String key) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION)
                    .whereEqualTo("key", key)
                    .get()
                    .get();
            
            if (querySnapshot.isEmpty()) {
                return Optional.empty();
            }
            
            return Optional.of(FirestoreMapper.mapToSetting(querySnapshot.getDocuments().get(0)));
        } catch (Exception e) {
            logger.error("Error finding setting by key: {}", e.getMessage());
            throw new DatabaseException("Failed to find setting: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<AppSetting> findById(String id) {
        try {
            DocumentSnapshot snapshot = firestore.collection(COLLECTION)
                    .document(id)
                    .get()
                    .get();
            
            if (snapshot.exists()) {
                return Optional.of(FirestoreMapper.mapToSetting(snapshot));
            }
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Error finding setting by ID: {}", e.getMessage());
            throw new DatabaseException("Failed to find setting: " + e.getMessage(), e);
        }
    }

    @Override
    public List<AppSetting> findAll() {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION)
                    .get()
                    .get();
            
            return querySnapshot.getDocuments()
                    .stream()
                    .map(FirestoreMapper::mapToSetting)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error finding all settings: {}", e.getMessage());
            throw new DatabaseException("Failed to find settings: " + e.getMessage(), e);
        }
    }

    @Override
    public AppSetting create(AppSetting setting) {
        try {
            setting.validate();
            
            if (firestore.collection(COLLECTION).document(setting.getId()).get().get().exists()) {
                throw new DatabaseException("Setting with ID " + setting.getId() + " already exists");
            }
            
            if (setting.getUpdatedAt() == null) {
                setting.setUpdatedAt(Instant.now());
            }
            
            firestore.collection(COLLECTION)
                    .document(setting.getId())
                    .set(FirestoreMapper.settingToMap(setting))
                    .get();
            
            logger.info("Setting created: {}", setting.getKey());
            return setting;
        } catch (Exception e) {
            logger.error("Error creating setting: {}", e.getMessage());
            throw new DatabaseException("Failed to create setting: " + e.getMessage(), e);
        }
    }

    @Override
    public AppSetting update(AppSetting setting) {
        try {
            setting.validate();
            
            if (!firestore.collection(COLLECTION).document(setting.getId()).get().get().exists()) {
                throw new DatabaseException("Setting with ID " + setting.getId() + " not found");
            }
            
            setting.setUpdatedAt(Instant.now());
            
            firestore.collection(COLLECTION)
                    .document(setting.getId())
                    .set(FirestoreMapper.settingToMap(setting))
                    .get();
            
            logger.info("Setting updated: {}", setting.getKey());
            return setting;
        } catch (Exception e) {
            logger.error("Error updating setting: {}", e.getMessage());
            throw new DatabaseException("Failed to update setting: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String id) {
        try {
            firestore.collection(COLLECTION)
                    .document(id)
                    .delete()
                    .get();
            
            logger.info("Setting deleted: {}", id);
        } catch (Exception e) {
            logger.error("Error deleting setting: {}", e.getMessage());
            throw new DatabaseException("Failed to delete setting: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existsByKey(String key) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION)
                    .whereEqualTo("key", key)
                    .get()
                    .get();
            
            return !querySnapshot.isEmpty();
        } catch (Exception e) {
            logger.error("Error checking setting key existence: {}", e.getMessage());
            throw new DatabaseException("Failed to check setting existence: " + e.getMessage(), e);
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
            logger.error("Error counting settings: {}", e.getMessage());
            throw new DatabaseException("Failed to count settings: " + e.getMessage(), e);
        }
    }
}
