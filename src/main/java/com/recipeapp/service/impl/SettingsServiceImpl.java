package com.recipeapp.service.impl;

import com.recipeapp.exception.DatabaseException;
import com.recipeapp.exception.ValidationException;
import com.recipeapp.model.ActivityType;
import com.recipeapp.model.AppSetting;
import com.recipeapp.repository.SettingsRepository;
import com.recipeapp.service.ActivityLogService;
import com.recipeapp.service.SettingsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of SettingsService.
 */
public class SettingsServiceImpl implements SettingsService {
    private static final Logger logger = LoggerFactory.getLogger(SettingsServiceImpl.class);
    
    private final SettingsRepository settingsRepository;
    private final ActivityLogService activityLogService;

    public SettingsServiceImpl(SettingsRepository settingsRepository, ActivityLogService activityLogService) {
        this.settingsRepository = settingsRepository;
        this.activityLogService = activityLogService;
    }

    @Override
    public Optional<AppSetting> getSetting(String key) {
        try {
            return settingsRepository.findByKey(key);
        } catch (Exception e) {
            logger.error("Error retrieving setting: {}", e.getMessage());
            throw new DatabaseException("Failed to retrieve setting: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<String> getSettingValue(String key) {
        try {
            return getSetting(key).map(AppSetting::getValue);
        } catch (Exception e) {
            logger.error("Error retrieving setting value: {}", e.getMessage());
            throw new DatabaseException("Failed to retrieve setting: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean getSettingBoolean(String key, boolean defaultValue) {
        try {
            Optional<String> value = getSettingValue(key);
            if (value.isPresent()) {
                return Boolean.parseBoolean(value.get());
            }
            return defaultValue;
        } catch (Exception e) {
            logger.error("Error retrieving boolean setting: {}", e.getMessage());
            return defaultValue;
        }
    }

    @Override
    public List<AppSetting> getAllSettings() {
        try {
            return settingsRepository.findAll();
        } catch (Exception e) {
            logger.error("Error retrieving all settings: {}", e.getMessage());
            throw new DatabaseException("Failed to retrieve settings: " + e.getMessage(), e);
        }
    }

    @Override
    public AppSetting setSetting(String key, String value, String description, String updatedBy) {
        try {
            if (key == null || key.isBlank()) {
                throw new ValidationException("Setting key cannot be empty");
            }
            
            Optional<AppSetting> existing = settingsRepository.findByKey(key);
            
            AppSetting setting;
            if (existing.isPresent()) {
                setting = existing.get();
                setting.setValue(value);
                setting.setDescription(description);
                setting.setUpdatedAt(Instant.now());
                setting.setUpdatedBy(updatedBy);
                setting = settingsRepository.update(setting);
            } else {
                setting = new AppSetting();
                setting.setId(UUID.randomUUID().toString());
                setting.setKey(key);
                setting.setValue(value);
                setting.setDescription(description);
                setting.setUpdatedAt(Instant.now());
                setting.setUpdatedBy(updatedBy);
                setting = settingsRepository.create(setting);
            }
            
            activityLogService.logActivity(updatedBy, ActivityType.SETTINGS_UPDATED,
                    "Setting updated: " + key);
            
            logger.info("Setting saved: {}", key);
            return setting;
        } catch (ValidationException | DatabaseException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error saving setting: {}", e.getMessage());
            throw new DatabaseException("Failed to save setting: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteSetting(String key) {
        try {
            Optional<AppSetting> setting = settingsRepository.findByKey(key);
            if (setting.isPresent()) {
                settingsRepository.delete(setting.get().getId());
                logger.info("Setting deleted: {}", key);
            }
        } catch (Exception e) {
            logger.error("Error deleting setting: {}", e.getMessage());
            throw new DatabaseException("Failed to delete setting: " + e.getMessage(), e);
        }
    }
}
