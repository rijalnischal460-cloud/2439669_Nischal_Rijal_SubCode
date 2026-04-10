package Utility;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Password Hashing Utility
 * Provides secure password hashing and verification using SHA-256 with salt.
 * This ensures that even if the database is compromised, actual passwords remain hidden.
 */
public class PasswordHasher {
    // Length of the random salt in bytes (16 bytes = 128 bits of entropy)
    private static final int SALT_LENGTH = 16;
    // The hashing algorithm used; SHA-256 provides a high level of security
    private static final String ALGORITHM = "SHA-256";

    /**
     * Hashes a password with salt using SHA-256
     * @param password Plain text password
     * @return Hashed password with salt (Base64 encoded)
     */
    public static String hashPassword(String password) {
        try {
            // Basic check for valid input
            if (password == null || password.isEmpty()) {
                System.err.println("❌ Password cannot be null or empty");
                return null;
            }

            // 1. Generate a cryptographically strong random salt
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);

            // 2. Initialize the SHA-256 digest and update it with the salt
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            md.update(salt);
            
            // 3. Perform the actual hashing of the password
            byte[] hashedPassword = md.digest(password.getBytes());

            // 4. Combine salt and hash into a single byte array for storage
            // Format: [SALT (16 bytes)][HASH (32 bytes)]
            byte[] saltAndHash = new byte[salt.length + hashedPassword.length];
            System.arraycopy(salt, 0, saltAndHash, 0, salt.length);
            System.arraycopy(hashedPassword, 0, saltAndHash, salt.length, hashedPassword.length);

            // 5. Convert the byte array to a Base64 String so it can be stored in a SQL VARCHAR column
            String encoded = Base64.getEncoder().encodeToString(saltAndHash);
            System.out.println("✅ Password hashed successfully");
            return encoded;
            
        } catch (NoSuchAlgorithmException e) {
            System.out.println("❌ Error hashing password: Algorithm not found - " + e.getMessage());
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            System.out.println("❌ Error hashing password: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Verifies a password against a stored hash
     * @param password Plain text password to verify
     * @param hash Stored hashed password from the database
     * @return true if password matches, false otherwise
     */
    public static boolean verifyPassword(String password, String hash) {
        try {
            if (password == null || password.isEmpty()) {
                System.err.println("❌ Password cannot be null or empty");
                return false;
            }

            if (hash == null || hash.isEmpty()) {
                System.err.println("❌ Hash cannot be null or empty");
                return false;
            }

            // 1. Decode the Base64 string back into a byte array
            byte[] saltAndHash;
            try {
                saltAndHash = Base64.getDecoder().decode(hash);
            } catch (IllegalArgumentException e) {
                System.err.println("❌ Invalid Base64 format in stored hash");
                // Fallback for legacy systems: try direct string comparison
                return password.equals(hash);
            }

            // 2. Validate that the decoded data is at least as long as our salt
            if (saltAndHash.length < SALT_LENGTH) {
                System.err.println("❌ Invalid hash length. Expected at least " + SALT_LENGTH + ", got " + saltAndHash.length);
                return false;
            }

            // 3. Extract the salt from the first 16 bytes of the stored data
            byte[] salt = new byte[SALT_LENGTH];
            System.arraycopy(saltAndHash, 0, salt, 0, SALT_LENGTH);

            // 4. Hash the user-provided password using the same salt extracted from the database
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes());

            // 5. Compare the newly generated hash with the one from the database
            // We use a constant-time method to prevent side-channel (timing) attacks
            return constantTimeEquals(saltAndHash, SALT_LENGTH, hashedPassword);
            
        } catch (NoSuchAlgorithmException e) {
            System.out.println("❌ Algorithm not found: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.out.println("❌ Error verifying password: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Constant-time comparison to prevent timing attacks.
     * Standard .equals() stops at the first difference, which allows hackers 
     * to guess characters by measuring how long the CPU takes to respond.
     * This method always checks every byte, taking the same amount of time.
     */
    private static boolean constantTimeEquals(byte[] saltAndHash, int saltLength, byte[] hashedPassword) {
        if (saltAndHash.length < saltLength + hashedPassword.length) {
            return false;
        }

        int result = 0;
        for (int i = 0; i < hashedPassword.length; i++) {
            // XOR each byte and accumulate differences in 'result'
            result |= saltAndHash[saltLength + i] ^ hashedPassword[i];
        }
        // If result is 0, all bytes matched perfectly
        return result == 0;
    }

    /**
     * Tests password hashing and verification flow.
     */
    public static void testPasswordHashing() {
        System.out.println("\n" + "═".repeat(50));
        System.out.println("PASSWORD HASHING TEST");
        System.out.println("═".repeat(50));
        
        String testPassword = "testPassword123";
        System.out.println("Original Password: " + testPassword);
        
        String hash = hashPassword(testPassword);
        System.out.println("Generated Hash: " + hash);
        
        boolean isValid = verifyPassword(testPassword, hash);
        System.out.println("✅ Verification Result: " + (isValid ? "SUCCESS" : "FAILED"));
        
        boolean isInvalid = verifyPassword("wrongPassword", hash);
        System.out.println("✅ Wrong Password Test: " + (isInvalid ? "FAILED (Should be false)" : "PASSED (Correctly rejected)"));
        
        System.out.println("═".repeat(50) + "\n");
    }

    /**
     * Generates a new hash for a given password (useful for password reset)
     * @param password The password to hash
     * @return The hashed password
     */
    public static String generateNewHash(String password) {
        return hashPassword(password);
    }
}