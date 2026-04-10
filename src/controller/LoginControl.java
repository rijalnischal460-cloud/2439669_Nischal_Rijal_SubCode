package controller;

import javafx.scene.control.Label;
import javafx.stage.Stage;
import model.DbConnection;
import Utility.PasswordHasher;
import java.sql.*;

/**
 * LoginControl - Handles all login and authentication logic
 */
public class LoginControl {

    /**
     * AUTHENTICATION: The "Check-in" process.
     * It looks up the user, checks if their account is active, 
     * and compares their typed password against the encrypted one in the DB.
     */
    public static User authenticateUser(String username, String password, String role) {
        // 1. Basic Validation: Don't even hit the DB if the fields are empty
        if (username == null || username.trim().isEmpty() || password == null || role == null) {
            return null;
        }
        
        String query = "SELECT id, username, password, role, email, phone, status FROM users WHERE username=? AND role=?";
        
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setString(1, username);
            ps.setString(2, role);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                // 2. Check Account Status: Even with a right password, "Banned" users can't enter
                String status = rs.getString("status");
                if (!status.equals("Active")) {
                    return null;
                }
                
                // 3. Password Verification: Compare plain text input with the Hashed DB version
                String storedHash = rs.getString("password");
                if (PasswordHasher.verifyPassword(password, storedHash)) {
                    
                    // Create the session object
                    User user = new User(rs.getInt("id"), rs.getString("username"), 
                                         rs.getString("role"), rs.getString("email"), 
                                         rs.getString("phone"), status);
                    
                    // 4. Audit Trail: Log that this person just logged in
                    logActivity(user.getId(), "LOGIN", "User", user.getId(), "Logged in successfully");
                    
                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Return null if anything fails (wrong pass, no user, or DB error)
    }

    /**
     * REGISTRATION: The "Sign-up" process.
     * Encrypts the password before saving it so that even DB admins can't see it.
     */
    public static boolean registerNewUser(String username, String password, String confirmPassword) {
        // Validation: Passwords must match and meet length requirements
        if (username == null || !password.equals(confirmPassword) || password.length() < 4) {
            return false;
        }
        
        String insertQuery = "INSERT INTO users (username, password, role, status, email) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertQuery)) {
            
            // CRITICAL SECURITY: Never save plain text passwords. Always Hash them.
            String hashedPassword = PasswordHasher.hashPassword(password);
            
            ps.setString(1, username);
            ps.setString(2, hashedPassword);
            ps.setString(3, "Staff"); // New signups are Staff by default
            ps.setString(4, "Active");
            ps.setString(5, username + "@cims.com");
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * LOGGING: The "Security Camera".
     * Records every major action to the 'activity_log' table for accountability.
     */
    public static void logActivity(int userId, String actionType, String entityType, int entityId, String description) {
        String query = "INSERT INTO activity_log (user_id, action_type, entity_type, entity_id, description, ip_address) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setInt(1, userId);
            ps.setString(2, actionType);
            ps.setString(3, entityType);
            ps.setInt(4, entityId);
            ps.setString(5, description);
            ps.setString(6, getClientIP()); // Grabs the user's computer IP
            
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("⚠ Warning: Logging failed.");
        }
    }

    /**
     * SESSION DATA: A simple container to hold the logged-in user's info.
     */
    public static class User {
        private int id;
        private String username, role, email, phone, status;
        
        public User(int id, String username, String role, String email, String phone, String status) {
            this.id = id; this.username = username; this.role = role;
            this.email = email; this.phone = phone; this.status = status;
        }

        public int getId() { return id; }
        public String getUsername() { return username; }
        public String getRole() { return role; }
        // ... other getters
    }

    private static String getClientIP() {
        try { return java.net.InetAddress.getLocalHost().getHostAddress(); }
        catch (Exception e) { return "127.0.0.1"; }
    }
}