package com.recipeapp.repository;

import com.recipeapp.model.AppSetting;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for AppSetting persistence operations.
 */
public interface SettingsRepository {
    
    /**
     * Find a setting by key.
     */
    Optional<AppSetting> findByKey(String key);
    
    /**
     * Find a setting by ID.
     */
    Optional<AppSetting> findById(String id);
    
    /**
     * Find all settings.
     */
    List<AppSetting> findAll();
    
    /**
     * Create a new setting.
     */
    AppSetting create(AppSetting setting);
    
    /**
     * Update an existing setting.
     */
    AppSetting update(AppSetting setting);
    
    /**
     * Delete a setting by ID.
     */
    void delete(String id);
    
    /**
     * Check if a setting exists by key.
     */
    boolean existsByKey(String key);
    
    /**
     * Count total number of settings.
     */
    long count();
}
