/**
 * Firebase Web SDK Configuration
 * 
 * This is the PUBLIC Firebase configuration used by the browser.
 * It is NOT the same as the Admin SDK service-account credentials.
 * 
 * These values are safe to expose in client-side code.
 * Do NOT put service-account credentials or private keys here.
 * 
 * Get these values from Firebase Console > Project Settings > Your apps > Web
 */

// Replace these with your Firebase project configuration
const firebaseConfig = {
    apiKey: "AIzaSyBwQx9TZvVHUQMCFzLqL7V-PZ3P8Z2L1fQ",
    authDomain: "recipe-sharing-63c2b.firebaseapp.com",
    projectId: "recipe-sharing-63c2b",
    storageBucket: "recipe-sharing-63c2b.appspot.com",
    messagingSenderId: "123456789012",
    appId: "1:123456789012:web:abcdef1234567890"
};

// Initialize Firebase
firebase.initializeApp(firebaseConfig);

// Get Firebase Auth instance
const auth = firebase.auth();

// Set authentication state persistence
auth.setPersistence(firebase.auth.Auth.Persistence.LOCAL)
    .catch(error => {
        console.error("Error setting persistence:", error);
    });

console.log("Firebase initialized for project: " + firebaseConfig.projectId);
