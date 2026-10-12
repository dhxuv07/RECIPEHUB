package com.recipeapp.service;

import com.recipeapp.model.AppSetting;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for Application Settings.
 * 
 * Handles system configuration and settings.
 */
public interface SettingsService {
    
    /**
     * Get a setting by key.
     */
    Optional<AppSetting> getSetting(String key);
    
    /**
     * Get a setting value as String.
     */
    Optional<String> getSettingValue(String key);
    
    /**
     * Get a setting value as boolean.
     */
    boolean getSettingBoolean(String key, boolean defaultValue);
    
    /**
     * Get all settings.
     */
    List<AppSetting> getAllSettings();
    
    /**
     * Create or update a setting.
     */
    AppSetting setSetting(String key, String value, String description, String updatedBy);
    
    /**
     * Delete a setting.
     */
    void deleteSetting(String key);
}
