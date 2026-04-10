package Utility;

/**
 * Utility to generate password hashes for database initialization.
 * This is a developer tool to help you set up your MySQL 'users' table.
 */
public class GenerateCorrectHash {
    
    public static void main(String[] args) {
        // Visual header for the console output
        System.out.println("\n" + "═".repeat(80));
        System.out.println("PASSWORD HASH GENERATOR FOR CIMS DATABASE");
        System.out.println("═".repeat(80));
        
        // 1. Generate standard hashes for Admin and Staff
        generateDefaultUserHashes();
        
        // 2. Allow the developer to pass a custom password through command line arguments
        if (args.length > 0) {
            generateCustomHash(args[0]);
        } else {
            System.out.println("\n" + "─".repeat(80));
            System.out.println("USAGE:");
            System.out.println("  java Test.GenerateCorrectHash [password]");
            System.out.println("\nEXAMPLE:");
            System.out.println("  java Test.GenerateCorrectHash mypassword123");
            System.out.println("─".repeat(80));
        }
        
        System.out.println("\n" + "═".repeat(80) + "\n");
    }
    
    /**
     * Creates ready-to-use hashes for the default system accounts.
     */
    public static void generateDefaultUserHashes() {
        System.out.println("\n📋 DEFAULT USER PASSWORDS & HASHES");
        System.out.println("─".repeat(80));
        
        // --- Admin Setup ---
        String adminPassword = "admin123";
        String adminHash = PasswordHasher.hashPassword(adminPassword);
        
        System.out.println("\n1️⃣  ADMIN USER");
        System.out.println("   Password: " + adminPassword);
        System.out.println("   Hash: " + adminHash);
        // This SQL can be copied directly into MySQL Workbench
        System.out.println("   SQL: UPDATE users SET password = '" + adminHash + "' WHERE username = 'admin';");
        System.out.println("   Test: " + (PasswordHasher.verifyPassword(adminPassword, adminHash) ? "✅ VERIFIED" : "❌ FAILED"));
        
        // --- Staff Setup ---
        String staffPassword = "staff123";
        String staffHash = PasswordHasher.hashPassword(staffPassword);
        
        System.out.println("\n2️⃣  STAFF USERS");
        System.out.println("   Password: " + staffPassword);
        System.out.println("   Hash: " + staffHash);
        System.out.println("   SQL: UPDATE users SET password = '" + staffHash + "' WHERE role = 'Staff';");
        System.out.println("   Test: " + (PasswordHasher.verifyPassword(staffPassword, staffHash) ? "✅ VERIFIED" : "❌ FAILED"));
        
        // --- Quick Reference List ---
        System.out.println("\n3️⃣  SAMPLE ADDITIONAL PASSWORDS");
        System.out.println("─".repeat(80));
        
        String[] samplePasswords = {
            "1234",
            "password123",
            "secure@123",
            "cims2024",
            "logistics@pass"
        };
        
        for (String pwd : samplePasswords) {
            String hash = PasswordHasher.hashPassword(pwd);
            System.out.println("Password: " + String.format("%-20s", pwd) + " → Hash: " + hash);
        }
        
        System.out.println("─".repeat(80));
    }
    
    /**
     * Takes a specific password and generates the full SQL 'INSERT' statement for it.
     */
    public static void generateCustomHash(String password) {
        System.out.println("\n🔐 CUSTOM PASSWORD HASH");
        System.out.println("─".repeat(80));
        
        String hash = PasswordHasher.hashPassword(password);
        
        System.out.println("Password: " + password);
        System.out.println("Generated Hash: " + hash);
        System.out.println("─".repeat(80));
        
        // --- Security Verification ---
        // We test the hash immediately to make sure the PasswordHasher logic is consistent
        System.out.println("\n✓ VERIFICATION TEST");
        boolean isVerified = PasswordHasher.verifyPassword(password, hash);
        System.out.println("Password verification: " + (isVerified ? "✅ PASS" : "❌ FAIL"));
        
        // Ensure that wrong passwords are correctly rejected
        boolean isWrongVerified = PasswordHasher.verifyPassword("wrongpassword", hash);
        System.out.println("Wrong password test: " + (isWrongVerified ? "❌ FAILED (Should be false)" : "✅ PASSED (Correctly rejected)"));
        
        // --- Database Scripts ---
        System.out.println("\n" + "─".repeat(80));
        System.out.println("💾 SQL UPDATE STATEMENTS:");
        System.out.println("─".repeat(80));
        System.out.println("\n-- Update specific user:");
        System.out.println("UPDATE users SET password = '" + hash + "' WHERE username = 'username_here';");
        System.out.println("\n-- Update all staff users:");
        System.out.println("UPDATE users SET password = '" + hash + "' WHERE role = 'Staff';");
        System.out.println("\n-- Update all admin users:");
        System.out.println("UPDATE users SET password = '" + hash + "' WHERE role = 'Admin';");
        System.out.println("\n-- Insert new user with this password:");
        System.out.println("INSERT INTO users (username, password, role, email, phone, status)");
        System.out.println("VALUES ('newuser', '" + hash + "', 'Staff', 'newuser@cims.com', '+977-9841234567', 'Active');");
        System.out.println("─".repeat(80));
    }
}