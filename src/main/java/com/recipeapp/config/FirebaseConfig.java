package com.recipeapp.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Firebase configuration and initialization class.
 * Initializes Firebase Admin SDK using environment-based credentials.
 * 
 * Requires GOOGLE_APPLICATION_CREDENTIALS environment variable to be set
 * to the path of the Firebase service-account JSON file.
 * 
 * The private key is NEVER hardcoded or logged.
 */
public class FirebaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);
    private static Firestore firestore;
    private static boolean initialized = false;

    /**
     * Initialize Firebase Admin SDK.
     * This method is idempotent - calling it multiple times is safe.
     * 
     * @throws RuntimeException if initialization fails
     */
    public synchronized static void initialize() {
        if (initialized && firestore != null) {
            logger.debug("Firebase already initialized");
            return;
        }

        try {
            String credentialPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
            if (credentialPath == null || credentialPath.isEmpty()) {
                throw new RuntimeException(
                    "GOOGLE_APPLICATION_CREDENTIALS environment variable not set. " +
                    "Please configure it to point to the Firebase service-account JSON file."
                );
            }

            GoogleCredentials credentials = GoogleCredentials.fromStream(
                new FileInputStream(credentialPath)
            );

            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .setProjectId("recipe-sharing-63c2b")
                .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                logger.info("Firebase Admin SDK initialized successfully");
            } else {
                logger.debug("Firebase Admin SDK already initialized");
            }

            firestore = FirestoreClient.getFirestore();
            initialized = true;
            logger.info("Firestore instance obtained successfully");

        } catch (IOException e) {
            logger.error("Failed to initialize Firebase: {}", e.getMessage());
            throw new RuntimeException("Firebase initialization failed: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error during Firebase initialization: {}", e.getMessage());
            throw new RuntimeException("Unexpected Firebase initialization error: " + e.getMessage(), e);
        }
    }

    /**
     * Get the Firestore instance.
     * Initializes Firebase if not already done.
     * 
     * @return Firestore instance
     * @throws RuntimeException if Firebase is not properly configured
     */
    public static Firestore getFirestore() {
        if (!initialized || firestore == null) {
            initialize();
        }
        return firestore;
    }

    /**
     * Check if Firebase is initialized.
     * 
     * @return true if initialized, false otherwise
     */
    public static boolean isInitialized() {
        return initialized && firestore != null;
    }
}
