package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConnection {

    // 1. DATABASE CREDENTIALS
    // The "Address" of your database, your username, and your password.
    private static final String URL = "jdbc:mysql://localhost:3306/courier_db?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "nischal10";

    // This variable holds the active connection so we don't keep opening new ones
    private static Connection connection;

    /**
     * THE CONNECTION BUILDER:
     * This method is called whenever another class needs to talk to the DB.
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Load the MySQL "Translator" (Driver) so Java understands MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");

            // If we don't have a connection yet, or it was closed, create a new one
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USER, PASS);
                System.out.println("✅ Connected to database.");
            }

            return connection;

        } catch (ClassNotFoundException e) {
            // This happens if you forgot to add the MySQL Connector JAR to your project
            throw new SQLException("❌ MySQL Driver not found.", e);
        } catch (SQLException e) {
            System.out.println("❌ Database connection failed! Check if MySQL is running.");
            throw e;
        }
    }

    /**
     * THE LOCK: 
     * Closes the connection to free up computer memory when the app shuts down.
     */
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("🔒 Database connection closed.");
            }
        } catch (SQLException e) {
            System.out.println("⚠ Error closing connection.");
        }
    }

    /**
     * THE HEALTH CHECK:
     * A quick way to see if the database is actually "listening."
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * QUICK TEST: 
     * You can run this class by itself to verify your setup.
     */
    public static void main(String[] args) {
        if (testConnection()) {
            System.out.println("✅ Database is working perfectly!");
        } else {
            System.out.println("❌ Database connection failed! Check credentials.");
        }
    }
}