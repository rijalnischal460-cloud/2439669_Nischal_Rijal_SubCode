package controller;

import model.DbConnection;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * ReportControl: Aggregates data from multiple tables (Parcels, Inventory, Users)
 * to provide high-level business intelligence.
 */
public class ReportControl {

    /**
     * ANALYTICS: Fetches live summary statistics from the database.
     * It uses a Map (a key-value dictionary) to send all the numbers to the Dashboard at once.
     */
    public Map<String, Integer> getSummaryStats() {
        Map<String, Integer> stats = new HashMap<>();
        
        // 1. Define the SQL "Count" queries
        String sqlParcels = "SELECT COUNT(*) FROM parcels WHERE status = 'Pending'";
        String sqlDispatched = "SELECT COUNT(*) FROM parcels WHERE status = 'Dispatched' AND DATE(updated_at) = CURDATE()";
        String sqlInventory = "SELECT SUM(quantity) FROM inventory";
        String sqlStaff = "SELECT COUNT(*) FROM users WHERE role = 'Staff' AND status = 'Active'";

        try (Connection conn = DbConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            // Execute each query and store the result in the Map with a friendly name
            ResultSet rs1 = stmt.executeQuery(sqlParcels);
            if (rs1.next()) stats.put("Pending Parcels", rs1.getInt(1));

            ResultSet rs2 = stmt.executeQuery(sqlDispatched);
            if (rs2.next()) stats.put("Dispatched Today", rs2.getInt(1));

            ResultSet rs3 = stmt.executeQuery(sqlInventory);
            if (rs3.next()) stats.put("Inventory Items", rs3.getInt(1));

            ResultSet rs4 = stmt.executeQuery(sqlStaff);
            if (rs4.next()) stats.put("Active Staff", rs4.getInt(1));

        } catch (SQLException e) {
            System.err.println("❌ Analytics Error: " + e.getMessage());
            // Fallback: If the database is down, return zeros so the UI doesn't crash
            return getMockStats();
        }
        return stats;
    }

    /**
     * LOGIC: Calculates the "Dispatch Efficiency" percentage.
     * This tells the manager what percentage of the workload is being completed today.
     */
    public double getDispatchEfficiency() {
        Map<String, Integer> stats = getSummaryStats();
        int pending = stats.getOrDefault("Pending Parcels", 0);
        int dispatched = stats.getOrDefault("Dispatched Today", 0);
        
        // Prevent "Division by Zero" error if there are no parcels at all
        if (pending + dispatched == 0) return 0.0;
        
        // Formula: (Finished / Total) * 100
        return (dispatched / (double) (dispatched + pending)) * 100;
    }

    /**
     * SAFETY: Provides empty data if the database connection fails.
     */
    private Map<String, Integer> getMockStats() {
        Map<String, Integer> mock = new HashMap<>();
        mock.put("Pending Parcels", 0);
        mock.put("Dispatched Today", 0);
        mock.put("Inventory Items", 0);
        mock.put("Active Staff", 0);
        return mock;
    }
}