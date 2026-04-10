package Interface;

import controller.LoginControl;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.effect.DropShadow;
import model.DbConnection;
import java.sql.*;
import java.util.Optional;

public class Dispatch {

    private FlowPane grid;

    public ScrollPane getDispatchPane() {
        VBox content = new VBox(30);
        content.setPadding(new Insets(40));
        content.setStyle("-fx-background-color: #F7FAFC;");

        Label header = new Label("Fleet Dispatch Center");
        header.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #1A202C;");
        Label subheader = new Label("Live monitor: Click cards to update status (Delivered items are locked).");
        subheader.setStyle("-fx-text-fill: #718096; -fx-font-size: 14px;");

        grid = new FlowPane();
        grid.setHgap(25);
        grid.setVgap(25);
        grid.setAlignment(Pos.TOP_LEFT);

        refreshFleetData();

        content.getChildren().addAll(new VBox(5, header, subheader), grid);
        
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: #F7FAFC;");
        return scroll;
    }

    private void refreshFleetData() {
        grid.getChildren().clear();
        String query = "SELECT tracking_id, recipient_name, destination, status FROM parcels";

        try (Connection conn = DbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String id = rs.getString("tracking_id");
                String name = rs.getString("recipient_name");
                String dest = rs.getString("destination");
                String status = rs.getString("status");
                
                String color = "#D69E2E"; // Pending
                if (status.equalsIgnoreCase("In Transit")) color = "#3182CE"; 
                if (status.equalsIgnoreCase("Delivered")) color = "#38A169";  
                if (status.equalsIgnoreCase("Delayed")) color = "#E53E3E";   

                grid.getChildren().add(createDispatchCard(id, name, status, dest, color));
            }
        } catch (SQLException e) {
            System.err.println("❌ Database Error: " + e.getMessage());
        }
    }

    private VBox createDispatchCard(String id, String driver, String status, String dest, String color) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(25));
        card.setPrefWidth(300);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-border-color: transparent; -fx-border-width: 2;");
        card.setEffect(new DropShadow(10, Color.rgb(0, 0, 0, 0.08)));

        Label lblID = new Label(id);
        lblID.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        Circle dot = new Circle(6, Color.web(color));
        HBox top = new HBox(lblID, new Region(), dot);
        HBox.setHgrow(top.getChildren().get(1), Priority.ALWAYS);

        VBox info = new VBox(5, 
            new Label("Recipient: " + driver), 
            new Label("📍 " + dest)
        );
        
        Label lblStatus = new Label(status.toUpperCase());
        lblStatus.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold; -fx-font-size: 12px;");

        card.getChildren().addAll(top, new Separator(), info, lblStatus);

        // --- LOGIC: RESTRICT UPDATES ---
        card.setOnMouseClicked(e -> {
            // Rule 1: Cannot change status if already Delivered
            if (status.equalsIgnoreCase("Delivered")) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Status Locked");
                alert.setHeaderText("Parcel Delivered");
                alert.setContentText("This parcel has reached its final destination and cannot be modified.");
                alert.showAndWait();
                return; 
            }

            // Rule 2: ChoiceDialog without "In Transit"
            ChoiceDialog<String> dialog = new ChoiceDialog<>(status, "Pending", "Delivered", "Delayed");
            dialog.setTitle("Update Status");
            dialog.setHeaderText("Change status for " + id);
            
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(newStatus -> {
                updateStatusInDb(id, newStatus);
                refreshFleetData();
            });
        });

        // --- HOVER EFFECTS ---
        card.setOnMouseEntered(e -> {
            if (!status.equalsIgnoreCase("Delivered")) {
                card.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 15; -fx-border-color: " + color + "; -fx-border-width: 2;");
                card.setCursor(javafx.scene.Cursor.HAND);
            }
        });
        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-border-color: transparent;");
            card.setCursor(javafx.scene.Cursor.DEFAULT);
        });

        return card;
    }

    private void updateStatusInDb(String trackingId, String newStatus) {
        String sql = "UPDATE parcels SET status = ? WHERE tracking_id = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, newStatus);
            ps.setString(2, trackingId);
            ps.executeUpdate();
            System.out.println("✅ Status updated to " + newStatus);
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}