package com.recipeapp.exception;

/**
 * Exception thrown when Firebase configuration is invalid or incomplete.
 */
public class FirebaseConfigurationException extends RuntimeException {
    public FirebaseConfigurationException(String message) {
        super(message);
    }

    public FirebaseConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
