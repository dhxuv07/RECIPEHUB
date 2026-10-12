package com.recipeapp.util;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.recipeapp.config.FirebaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * Firestore Connectivity Test Utility.
 * 
 * This utility verifies that the Firestore database is properly connected
 * and functional. It performs basic CRUD operations on a temporary test document.
 * 
 * Test data is cleaned up after verification.
 * No credentials or sensitive data is logged.
 */
public class FirestoreConnectivityTest {
    private static final Logger logger = LoggerFactory.getLogger(FirestoreConnectivityTest.class);
    private static final String TEST_COLLECTION = "_system_tests";
    private static final String TEST_FIELD_MESSAGE = "message";
    private static final String TEST_FIELD_TIMESTAMP = "timestamp";

    /**
     * Run the Firestore connectivity test.
     * This method creates, reads, updates, and deletes a temporary test document.
     * 
     * @return true if all operations succeed, false otherwise
     */
    public static boolean runConnectivityTest() {
        logger.info("Starting Firestore connectivity test...");

        try {
            // Initialize Firebase
            FirebaseConfig.initialize();
            logger.info("✓ Firebase initialization successful");

            // Get Firestore instance
            Firestore firestore = FirebaseConfig.getFirestore();
            logger.info("✓ Firestore connection successful");

            // Generate unique test document ID
            String testDocId = "test_" + UUID.randomUUID().toString().substring(0, 8);

            // CREATE: Add a test document
            Map<String, Object> testData = new HashMap<>();
            testData.put(TEST_FIELD_MESSAGE, "Firestore connectivity test");
            testData.put(TEST_FIELD_TIMESTAMP, System.currentTimeMillis());

            DocumentReference docRef = firestore.collection(TEST_COLLECTION).document(testDocId);
            docRef.set(testData).get();
            logger.info("✓ Test document created");

            // READ: Retrieve the document
            var documentSnapshot = docRef.get().get();
            if (!documentSnapshot.exists()) {
                logger.error("✗ Test document not found after creation");
                return false;
            }
            String retrievedMessage = documentSnapshot.getString(TEST_FIELD_MESSAGE);
            if (!"Firestore connectivity test".equals(retrievedMessage)) {
                logger.error("✗ Retrieved message doesn't match expected value");
                return false;
            }
            logger.info("✓ Test document read successfully");

            // UPDATE: Modify the document
            Map<String, Object> updateData = new HashMap<>();
            updateData.put(TEST_FIELD_MESSAGE, "Connectivity test updated");
            docRef.update(updateData).get();
            logger.info("✓ Test document updated successfully");

            // DELETE: Remove the test document
            docRef.delete().get();
            logger.info("✓ Test document deleted successfully");

            // Verify deletion
            var deletedSnapshot = docRef.get().get();
            if (deletedSnapshot.exists()) {
                logger.error("✗ Test document still exists after deletion");
                return false;
            }

            logger.info("✓ All Firestore connectivity tests passed successfully");
            return true;

        } catch (ExecutionException e) {
            logger.error("✗ Firestore operation failed: {}", e.getCause().getMessage());
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("✗ Firestore operation interrupted: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("✗ Unexpected error during connectivity test: {}", e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Main method for standalone testing.
     */
    public static void main(String[] args) {
        boolean success = runConnectivityTest();
        System.exit(success ? 0 : 1);
    }
}
