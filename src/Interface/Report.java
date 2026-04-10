package Interface;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.effect.DropShadow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.DbConnection;
import java.io.File;
import java.io.PrintWriter;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Report {

    // --- CRITICAL: Class-level variables ---
    // We declare these here so they can be accessed by the refreshDashboard() method later
    private VBox feedList; 
    private HBox statsContainer; 

    public ScrollPane getReportPane() {
        VBox content = new VBox(30);
        content.setPadding(new Insets(40));
        content.setStyle("-fx-background-color: #F7FAFC;");

        // --- 1. HEADER ---
        HBox header = new HBox();
        VBox titleArea = new VBox(5);
        Label title = new Label("System Analytics");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #1A202C;");
        Label sub = new Label("Real-time summary of warehouse performance.");
        sub.setStyle("-fx-text-fill: #718096;");
        titleArea.getChildren().addAll(title, sub);
        
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        // Refresh Button: Manually triggers a database re-scan
        Button btnRefresh = new Button("Refresh Data");
        btnRefresh.setStyle("-fx-background-color: #EDF2F7; -fx-text-fill: #4A5568; -fx-font-weight: bold; -fx-padding: 12 20; -fx-background-radius: 8; -fx-cursor: hand;");
        btnRefresh.setOnAction(e -> refreshDashboard());

        // Export Button: Saves data to a file on your computer
        Button btnDownload = new Button("Export CSV");
        btnDownload.setStyle("-fx-background-color: #38A169; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12 25; -fx-background-radius: 8; -fx-cursor: hand;");
        btnDownload.setOnAction(e -> handleExport((Stage) btnDownload.getScene().getWindow()));

        HBox buttonGroup = new HBox(10, btnRefresh, btnDownload);
        header.getChildren().addAll(titleArea, sp, buttonGroup);

        // --- 2. INITIALIZE CONTAINERS ---
        statsContainer = new HBox(20); 
        feedList = new VBox(10);
        
        // Load data immediately when the page opens
        refreshDashboard();

        Label feedTitle = new Label("Recent System Activity");
        feedTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");

        // Add all sections to the main layout
        content.getChildren().addAll(header, statsContainer, feedTitle, feedList);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: #F7FAFC;");
        return scroll;
    }

    // ================== LOGIC METHODS ==================

    /**
     * Re-fetches all numbers and the activity feed from the database
     */
    private void refreshDashboard() {
        // Clear the old visual elements
        statsContainer.getChildren().clear();
        feedList.getChildren().clear();

        // Fetch numbers using SQL queries
        String totalParcels = fetchSingleValue("SELECT COUNT(*) FROM parcels");
        String lowStock = fetchSingleValue("SELECT COUNT(*) FROM inventory WHERE quantity < reorder_level");
        String totalRev = fetchSingleValue("SELECT COALESCE(SUM(shipping_cost), 0) FROM parcels");

        // Re-draw the 3 main statistic cards at the top
        statsContainer.getChildren().addAll(
            createReportStat("Total Shipments", totalParcels, "Last Update: " + now(), "#3182CE"), // Blue
            createReportStat("Critical Alerts", lowStock, "Inventory Low", "#E53E3E"),          // Red
            createReportStat("Total Revenue", "$" + totalRev, "Gross earnings", "#38A169")       // Green
        );

        // Reload the list of recent parcel updates
        loadActivityLogs();
    }

    /**
     * Fetches the 5 most recent parcel updates to show in a list
     */
    private void loadActivityLogs() {
        String sql = "SELECT tracking_id, status, created_at FROM parcels ORDER BY created_at DESC LIMIT 5";
        try (Connection conn = DbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                String id = rs.getString("tracking_id");
                String status = rs.getString("status");
                String time = rs.getString("created_at");
                
                // Create a row for the activity list
                HBox row = createActivityRow("Parcel " + id + " is " + status, time, status.equals("Delivered") ? "#38A169" : "#3182CE");
                
                // If you click a row, it shows an alert with details
                row.setOnMouseClicked(e -> {
                    new Alert(Alert.AlertType.INFORMATION, "Tracking ID: " + id + "\nStatus: " + status).show();
                });
                row.setStyle(row.getStyle() + "-fx-cursor: hand;");

                feedList.getChildren().add(row);
            }
        } catch (SQLException e) {
            feedList.getChildren().add(new Label("Database error: " + e.getMessage()));
        }
    }

    /**
     * Opens a "Save As" window to download a CSV file of the report
     */
    private void handleExport(Stage stage) {
        FileChooser fc = new FileChooser();
        fc.setInitialFileName("Report.csv");
        File file = fc.showSaveDialog(stage);
        
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                writer.println("ID,Status,Date"); // CSV Headers
                // Here you would normally loop through your DB results to write rows
                writer.flush();
                new Alert(Alert.AlertType.INFORMATION, "Saved to " + file.getAbsolutePath()).show();
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Error: " + e.getMessage()).show();
            }
        }
    }

    // ================== HELPERS ==================

    // Runs a simple SQL query that returns one number (like a COUNT or SUM)
    private String fetchSingleValue(String sql) {
        try (Connection conn = DbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getString(1);
        } catch (SQLException e) { return "0"; }
        return "0";
    }

    // Returns the current time formatted as HH:mm:ss
    private String now() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    // Creates the visual design for the large stat cards
    private VBox createReportStat(String title, String val, String trend, String color) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(25));
        card.setPrefWidth(300);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12;");
        card.setEffect(new DropShadow(8, Color.rgb(0, 0, 0, 0.1)));

        Label t = new Label(title); t.setStyle("-fx-text-fill: #718096; -fx-font-weight: bold;");
        Label v = new Label(val); v.setStyle("-fx-font-size: 32px; -fx-font-weight: bold;");
        Label tr = new Label(trend); tr.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");

        card.getChildren().addAll(t, v, tr);
        return card;
    }

    // Creates the visual design for a single row in the "Recent Activity" list
    private HBox createActivityRow(String text, String time, String color) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(15, 20, 15, 20));
        row.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #EDF2F7; -fx-border-width: 2;");

        Circle indicator = new Circle(5, Color.web(color));
        Label lblText = new Label(text);
        Region s = new Region(); HBox.setHgrow(s, Priority.ALWAYS);
        Label lblTime = new Label(time);

        row.getChildren().addAll(indicator, lblText, s, lblTime);
        return row;
    }
}