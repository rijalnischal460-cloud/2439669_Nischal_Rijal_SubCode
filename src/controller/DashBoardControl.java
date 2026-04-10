package controller;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * DashBoardControl: Manages the logic and UI generation for the main dashboard.
 * This class keeps the dashboard design separate from the main window logic.
 */
public class DashBoardControl {
    // These variables hold the "Hardcoded" numbers for now (can be replaced by DB data)
    private int totalParcels = 450;
    private int delivered = 320;
    private int pending = 95;
    private int failed = 35;
    private double revenue = 45000.00;
    private LoginControl.User currentUser;

    public DashBoardControl(LoginControl.User user) {
        this.currentUser = user;
    }

    /**
     * This is the main method that creates the entire Dashboard view.
     * It puts the Header, the Stat Cards, and the Activity List together.
     */
    public VBox createDashboardPane() {
        VBox main = new VBox(25); 
        main.setPadding(new Insets(30));
        main.setStyle("-fx-background-color: #F7FAFC;"); // Light grey background

        // Combine all sections into one vertical box
        main.getChildren().addAll(
            createHeader(),
            createStatsGrid(),
            createActivitySection()
        );

        return main;
    }

    /**
     * Creates the top section with the title and the current date.
     */
    private HBox createHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        
        VBox titleBox = new VBox(5);
        Label title = new Label("📊 Operational Dashboard");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#1A202C"));
        
        // Personalized welcome message
        String welcomeMsg = (currentUser != null) ? "Welcome back, " + currentUser.getUsername() : "Overview for today";
        Label welcome = new Label(welcomeMsg);
        welcome.setStyle("-fx-text-fill: #718096; -fx-font-size: 14px;");
        titleBox.getChildren().addAll(title, welcome);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Shows the current date (e.g., Monday, Oct 24, 2023)
        Label time = new Label(LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, MMM dd, yyyy")));
        time.setStyle("-fx-text-fill: #4A5568; -fx-font-weight: bold; -fx-background-color: white; -fx-padding: 10 20; -fx-background-radius: 10;");

        header.getChildren().addAll(titleBox, spacer, time);
        return header;
    }

    /**
     * Creates a grid of colored cards showing key metrics (Revenue, Delivered, etc.)
     */
    private GridPane createStatsGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);

        // Individual cards with specific colors
        grid.add(createCard("📦 Total Parcels", String.valueOf(totalParcels), "#3182CE"), 0, 0); // Blue
        grid.add(createCard("✅ Delivered", String.valueOf(delivered), "#48BB78"), 1, 0);     // Green
        grid.add(createCard("⏳ Pending", String.valueOf(pending), "#ED8936"), 2, 0);       // Orange
        grid.add(createCard("❌ Failed", String.valueOf(failed), "#F56565"), 3, 0);         // Red
        
        // Revenue card is special; it spans two columns (0 and 1) on the second row
        VBox revCard = createCard("💰 Total Revenue", formatCurrency(revenue), "#9F7AEA"); // Purple
        grid.add(revCard, 0, 1, 2, 1); 

        return grid;
    }

    /**
     * Helper to design a single colored "Stat Card".
     */
    private VBox createCard(String title, String value, String color) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(25));
        card.setPrefWidth(240);
        // Uses the passed color string and adds a soft shadow
        card.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 4);");

        Label titleLabel = new Label(title.toUpperCase());
        titleLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: white; -fx-opacity: 0.9; -fx-letter-spacing: 1px;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white;");

        card.getChildren().addAll(titleLabel, valueLabel);
        return card;
    }

    /**
     * Creates the "Recent Logs" box at the bottom of the dashboard.
     */
    private VBox createActivitySection() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(25));
        container.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 4);");

        Label title = new Label("🔔 Recent System Logs");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2D3748;");

        VBox list = new VBox(10);
        // Hardcoded log items for display
        list.getChildren().addAll(
            createActivityItem("Parcel #8829 delivered successfully", "2 hours ago", "#48BB78"),
            createActivityItem("Dispatch delayed for Route A1", "4 hours ago", "#ED8936"),
            createActivityItem("Stock alert: Packaging Boxes low", "6 hours ago", "#F56565")
        );

        container.getChildren().addAll(title, list);
        return container;
    }

    /**
     * Helper to create a single row item for the activity log.
     */
    private HBox createActivityItem(String msg, String time, String color) {
        HBox item = new HBox(15);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(12, 15, 12, 15));
        item.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 8;");

        // Small colored dot to indicate status (Green/Orange/Red)
        Region indicator = new Region();
        indicator.setPrefSize(8, 8);
        indicator.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 4;");

        Label msgLabel = new Label(msg);
        msgLabel.setStyle("-fx-text-fill: #4A5568; -fx-font-size: 13px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label timeLabel = new Label(time);
        timeLabel.setStyle("-fx-text-fill: #A0AEC0; -fx-font-size: 11px;");

        item.getChildren().addAll(indicator, msgLabel, spacer, timeLabel);
        return item;
    }

    // --- HELPER METHODS ---

    // Turns a number like 45000.0 into "$45,000.00"
    private String formatCurrency(double amount) {
        return NumberFormat.getCurrencyInstance(Locale.US).format(amount);
    }

    // Calculates what percentage of parcels were successfully delivered
    public double getSuccessRate() { 
        return totalParcels == 0 ? 0 : (delivered / (double) totalParcels) * 100; 
    }
}