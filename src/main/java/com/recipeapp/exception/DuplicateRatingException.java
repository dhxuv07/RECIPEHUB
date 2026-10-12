package com.recipeapp.exception;

/**
 * Exception thrown when attempting to create a duplicate rating.
 * 
 * A user can only have one rating per recipe.
 */
public class DuplicateRatingException extends RuntimeException {
    public DuplicateRatingException(String message) {
        super(message);
    }

    public DuplicateRatingException(String message, Throwable cause) {
        super(message, cause);
    }
}
