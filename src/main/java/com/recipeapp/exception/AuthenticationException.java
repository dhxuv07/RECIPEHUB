package com.recipeapp.exception;

/**
 * Exception thrown when authentication fails.
 * 
 * This includes token verification failures, invalid credentials, or session issues.
 */
public class AuthenticationException extends RuntimeException {
    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
