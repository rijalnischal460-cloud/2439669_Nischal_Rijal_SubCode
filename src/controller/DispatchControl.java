package controller;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

/**
 * DispatchControl: Handles the creation of new dispatch assignments.
 * This class provides the UI form for assigning drivers and routes.
 */
public class DispatchControl {
    
    // This method returns the entire UI layout as a "Node" to be displayed on screen
    public Node getLayout() {
        // --- 1. Root Container ---
        // A VBox stacks elements vertically with 25px of space between them
        VBox root = new VBox(25);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: white; -fx-background-radius: 15;");
        
        // --- 2. Header Section ---
        VBox headerBox = new VBox(5);
        Label header = new Label("New Dispatch Assignment");
        header.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1A202C;");
        
        Label subHeader = new Label("Fill in the details to assign a driver to a specific route.");
        subHeader.setStyle("-fx-text-fill: #718096; -fx-font-size: 13px;");
        headerBox.getChildren().addAll(header, subHeader);

        // --- 3. Form Layout (The Grid) ---
        // We use a GridPane to keep our labels and input boxes aligned like a table
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setAlignment(Pos.TOP_LEFT);

        // Standard styling for our field titles
        String labelStyle = "-fx-font-weight: bold; -fx-text-fill: #4A5568; -fx-font-size: 14px;";

        // Driver Selection (Dropdown Menu)
        Label lblDriver = new Label("Assign Driver");
        lblDriver.setStyle(labelStyle);
        ComboBox<String> drivers = new ComboBox<>();
        drivers.getItems().addAll("Ramesh Thapa", "Suresh Gurung", "Maya Devi", "Anjali Sharma");
        drivers.setPromptText("Select a driver...");
        drivers.setPrefWidth(350);
        drivers.setStyle("-fx-background-color: #F7FAFC; -fx-border-color: #E2E8F0; -fx-border-radius: 8; -fx-padding: 5;");

        // Route Input (Text Box)
        Label lblRoute = new Label("Route Details");
        lblRoute.setStyle(labelStyle);
        TextField routeField = new TextField();
        routeField.setPromptText("e.g. Kathmandu to Butwal");
        routeField.setPrefWidth(350);
        routeField.setStyle("-fx-background-color: #F7FAFC; -fx-border-color: #E2E8F0; -fx-border-radius: 8; -fx-padding: 10;");

        // Vehicle ID Input (Text Box)
        Label lblVehicle = new Label("Vehicle ID");
        lblVehicle.setStyle(labelStyle);
        TextField vehicleField = new TextField();
        vehicleField.setPromptText("e.g. TRK-9902");
        vehicleField.setStyle("-fx-background-color: #F7FAFC; -fx-border-color: #E2E8F0; -fx-border-radius: 8; -fx-padding: 10;");

        // Add everything to the grid by (column, row)
        grid.add(lblDriver, 0, 0);
        grid.add(drivers, 0, 1);
        grid.add(lblVehicle, 0, 2);
        grid.add(vehicleField, 0, 3);
        grid.add(lblRoute, 0, 4);
        grid.add(routeField, 0, 5);

        // --- 4. Action Buttons ---
        HBox actions = new HBox(15);
        actions.setPadding(new Insets(10, 0, 0, 0));

        // The "Confirm" button (Primary action)
        Button confirm = new Button("Confirm Dispatch");
        confirm.setStyle("-fx-background-color: #3182CE; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12 25; -fx-background-radius: 8; -fx-cursor: hand;");
        
        // The "Cancel" button (Secondary action)
        Button cancel = new Button("Cancel");
        cancel.setStyle("-fx-background-color: transparent; -fx-text-fill: #E53E3E; -fx-font-weight: bold; -fx-padding: 12 25; -fx-cursor: hand;");

        // Logic: What happens when you click "Confirm"
        confirm.setOnAction(e -> {
            String selectedDriver = drivers.getValue();
            String route = routeField.getText();
            
            // Basic validation: Check if fields are empty
            if (selectedDriver != null && !route.isEmpty()) {
                showSimpleAlert("Dispatch Successful", "Driver " + selectedDriver + " has been assigned to " + route);
                
                // Clear the form after success
                routeField.clear();
                vehicleField.clear();
                drivers.setValue(null);
            } else {
                showSimpleAlert("Error", "Please fill in all assignment details.");
            }
        });

        actions.getChildren().addAll(confirm, cancel);

        // --- 5. Assemble Everything ---
        // A "Separator" adds a subtle horizontal line for visual organization
        root.getChildren().addAll(headerBox, new Separator(), grid, actions);

        return root;
    }

    // A helper method to pop up a message box
    private void showSimpleAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}