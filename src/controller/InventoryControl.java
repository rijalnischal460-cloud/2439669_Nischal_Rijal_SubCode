package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.DbConnection;
import java.sql.*;

/**
 * InventoryControl: Handles CRUD (Create, Read, Update, Delete) operations.
 * Connects the Java UI to the 'inventory' table in MySQL.
 */
public class InventoryControl {

    /**
     * READ: Fetches all items from the database.
     * Returns an "ObservableList" so JavaFX tables can automatically show the data.
     */
    public ObservableList<InventoryData> getAllInventory() {
        ObservableList<InventoryData> inventory = FXCollections.observableArrayList();
        String query = "SELECT id, item_code, item_name, category, quantity, unit_price, status, supplier_name FROM inventory";

        // "Try-with-resources" automatically closes the connection to save memory
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                // Converts each row in the database into a Java "InventoryData" object
                inventory.add(mapResultSetToInventory(rs));
            }
        } catch (SQLException e) {
            handleSqlError("Error fetching all inventory", e);
        }
        return inventory;
    }

    /**
     * UPDATE: Changes the stock count and automatically updates the status.
     * Example: If quantity drops to 5, the database automatically sets status to 'LOW_STOCK'.
     */
    public boolean updateQuantity(int itemId, int newQuantity, int updatedBy) {
        // SQL logic: A CASE statement acts like an "if-else" inside the database
        String query = "UPDATE inventory SET quantity = ?, updated_by = ?, " +
                       "status = CASE " +
                       "  WHEN ? <= 0 THEN 'OUT_OF_STOCK' " +
                       "  WHEN ? <= reorder_level THEN 'LOW_STOCK' " +
                       "  ELSE 'IN_STOCK' " +
                       "END WHERE id = ?";

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, newQuantity);
            ps.setInt(2, updatedBy);
            ps.setInt(3, newQuantity); // For status check
            ps.setInt(4, newQuantity); // For status check
            ps.setInt(5, itemId);

            return ps.executeUpdate() > 0; // Returns true if the update worked
        } catch (SQLException e) {
            handleSqlError("Error updating quantity", e);
            return false;
        }
    }

    /**
     * CREATE: Adds a brand-new item to the warehouse manifest.
     */
    public boolean addInventoryItem(String itemCode, String itemName, String category,
                                   int quantity, double unitPrice, int reorderLevel, String location,
                                   String supplierName, int createdBy) {
        
        String query = "INSERT INTO inventory (item_code, item_name, category, quantity, unit_price, " +
                      "reorder_level, location, supplier_name, created_by, status) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            // Calculate the status before sending to the database
            String initialStatus = (quantity <= reorderLevel) ? "LOW_STOCK" : "IN_STOCK";

            ps.setString(1, itemCode);
            ps.setString(2, itemName);
            ps.setString(3, category);
            ps.setInt(4, quantity);
            ps.setDouble(5, unitPrice);
            ps.setInt(6, reorderLevel);
            ps.setString(7, location);
            ps.setString(8, supplierName);
            ps.setInt(9, createdBy);
            ps.setString(10, initialStatus);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            handleSqlError("Error adding new item", e);
            return false;
        }
    }

    // --- HELPERS ---

    // A utility to turn a Database Result row into a clean Java Object
    private InventoryData mapResultSetToInventory(ResultSet rs) throws SQLException {
        return new InventoryData(
            rs.getInt("id"),
            rs.getString("item_code"),
            rs.getString("item_name"),
            rs.getString("category"),
            rs.getInt("quantity"),
            rs.getDouble("unit_price"),
            rs.getString("status"),
            rs.getString("supplier_name")
        );
    }

    private void handleSqlError(String context, SQLException e) {
        System.err.println("❌ " + context + ": " + e.getMessage());
    }

    /**
     * This is a simple data container (Model) that holds one item's info.
     */
    public static class InventoryData {
        private final int id;
        private final String itemCode, itemName, category, status, supplierName;
        private final int quantity;
        private final double unitPrice;

        public InventoryData(int id, String itemCode, String itemName, String category,
                           int quantity, double unitPrice, String status, String supplierName) {
            this.id = id;
            this.itemCode = itemCode;
            this.itemName = itemName;
            this.category = category;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.status = status;
            this.supplierName = supplierName;
        }

        // Standard Getters (needed for JavaFX TableView)
        public int getId() { return id; }
        public String getItemCode() { return itemCode; }
        public String getItemName() { return itemName; }
        public String getCategory() { return category; }
        public int getQuantity() { return quantity; }
        public double getUnitPrice() { return unitPrice; }
        public String getStatus() { return status; }
        public String getSupplierName() { return supplierName; }
    }
}