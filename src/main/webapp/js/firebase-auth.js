/**
 * Firebase Authentication Module
 * 
 * Provides functions for:
 * - User registration
 * - User login
 * - User logout
 * - ID token retrieval
 * - Session establishment with backend
 * 
 * IMPORTANT: This module handles client-side authentication ONLY.
 * All server-side identity verification is done by the backend using Firebase Admin SDK.
 */

const FirebaseAuth = {
    
    /**
     * Register a new user with Firebase Authentication.
     * 
     * @param {string} email - User email
     * @param {string} password - User password
     * @param {string} name - User name
     * @return {Promise} resolves with user credentials
     */
    registerUser: async function(email, password, name) {
        try {
            // Create user in Firebase Authentication
            const userCredential = await auth.createUserWithEmailAndPassword(email, password);
            const user = userCredential.user;
            
            // Update profile with name
            await user.updateProfile({
                displayName: name
            });
            
            console.log("User registered successfully:", user.uid);
            
            return {
                uid: user.uid,
                email: user.email,
                name: user.displayName,
                idToken: await user.getIdToken()
            };
            
        } catch (error) {
            console.error("Registration error:", error);
            throw new Error(this.formatFirebaseError(error));
        }
    },
    
    /**
     * Sign in an existing user.
     * 
     * @param {string} email - User email
     * @param {string} password - User password
     * @return {Promise} resolves with user credentials
     */
    signInUser: async function(email, password) {
        try {
            const userCredential = await auth.signInWithEmailAndPassword(email, password);
            const user = userCredential.user;
            
            console.log("User signed in successfully:", user.uid);
            
            return {
                uid: user.uid,
                email: user.email,
                name: user.displayName,
                idToken: await user.getIdToken()
            };
            
        } catch (error) {
            console.error("Sign in error:", error);
            throw new Error(this.formatFirebaseError(error));
        }
    },
    
    /**
     * Sign out the current user from Firebase.
     * 
     * @return {Promise}
     */
    signOutUser: async function() {
        try {
            await auth.signOut();
            console.log("User signed out successfully");
            return true;
        } catch (error) {
            console.error("Sign out error:", error);
            throw new Error(this.formatFirebaseError(error));
        }
    },
    
    /**
     * Get the current Firebase ID token.
     * 
     * @return {Promise<string>} the ID token
     */
    getIdToken: async function() {
        try {
            const user = auth.currentUser;
            if (!user) {
                throw new Error("No user currently signed in");
            }
            
            return await user.getIdToken();
        } catch (error) {
            console.error("Error getting ID token:", error);
            throw new Error("Failed to get ID token");
        }
    },
    
    /**
     * Establish a session with the backend by exchanging Firebase ID token.
     * 
     * @param {string} idToken - Firebase ID token
     * @return {Promise}
     */
    establishBackendSession: async function(idToken) {
        try {
            const response = await fetch(contextPath + "/auth/session", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": "Bearer " + idToken
                },
                body: JSON.stringify({
                    idToken: idToken
                })
            });
            
            const data = await response.json();
            
            if (!response.ok) {
                throw new Error(data.error || "Failed to establish session");
            }
            
            console.log("Backend session established");
            return data;
            
        } catch (error) {
            console.error("Error establishing backend session:", error);
            throw error;
        }
    },
    
    /**
     * Get current authentication state.
     * 
     * @return {Promise}
     */
    getCurrentUser: async function() {
        return new Promise((resolve) => {
            auth.onAuthStateChanged((user) => {
                if (user) {
                    resolve({
                        uid: user.uid,
                        email: user.email,
                        name: user.displayName
                    });
                } else {
                    resolve(null);
                }
            });
        });
    },
    
    /**
     * Format Firebase error messages for display.
     * 
     * @param {Error} error - Firebase error
     * @return {string} user-friendly error message
     */
    formatFirebaseError: function(error) {
        const errorCode = error.code;
        
        const messages = {
            "auth/email-already-in-use": "Email already registered. Please sign in or use a different email.",
            "auth/weak-password": "Password is too weak. Please use at least 6 characters.",
            "auth/invalid-email": "Invalid email format.",
            "auth/user-not-found": "Email not found. Please check or register.",
            "auth/wrong-password": "Incorrect password. Please try again.",
            "auth/too-many-requests": "Too many login attempts. Please try again later.",
            "auth/operation-not-allowed": "This operation is not allowed.",
            "auth/user-disabled": "User account is disabled."
        };
        
        return messages[errorCode] || error.message || "An error occurred during authentication";
    }
};
