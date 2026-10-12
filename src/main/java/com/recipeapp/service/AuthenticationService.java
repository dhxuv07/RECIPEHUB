package com.recipeapp.service;

import com.recipeapp.model.User;
import com.google.firebase.auth.FirebaseToken;

/**
 * Service interface for authentication operations.
 * 
 * Handles Firebase token verification and user authentication.
 * 
 * Important: This service does NOT handle password storage or cryptography.
 * Authentication is delegated to Firebase Authentication.
 */
public interface AuthenticationService {
    
    /**
     * Verify a Firebase ID token.
     * 
     * Uses Firebase Admin SDK to verify the token signature and expiration.
     * 
     * @param idToken the Firebase ID token from the client
     * @return the verified FirebaseToken containing user identity information
     * @throws AuthenticationException if the token is invalid, expired, or verification fails
     */
    FirebaseToken verifyIdToken(String idToken);
    
    /**
     * Get or create a user profile from a verified Firebase token.
     * 
     * If the user profile does not exist in Firestore, creates it with:
     * - id: Firebase UID
     * - email: from Firebase Authentication
     * - name: from request (or Firebase claims if available)
     * - role: USER (never ADMIN for new profiles)
     * - active: true
     * 
     * @param firebaseToken the verified FirebaseToken
     * @param userName optional name to set in profile (can be null)
     * @return the User profile from Firestore
     * @throws Exception if profile creation fails
     */
    User getOrCreateUserProfile(FirebaseToken firebaseToken, String userName);
    
    /**
     * Get an existing user by Firebase UID.
     * 
     * @param uid the Firebase UID
     * @return the User if exists, null otherwise
     */
    User getUserByUid(String uid);
}
