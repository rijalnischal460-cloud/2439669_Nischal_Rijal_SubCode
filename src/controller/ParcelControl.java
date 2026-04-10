package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * ParcelControl: Manages the search, filtering, and display of the parcel registry.
 */
public class ParcelControl {

    private TableView<ParcelData> table;
    private ObservableList<ParcelData> masterData;

    public Node getLayout() {
        VBox container = new VBox(25);
        container.setPadding(new Insets(30));
        container.setStyle("-fx-background-color: #F7FAFC;");

        // --- 1. HEADER & SEARCH BAR ---
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label title = new Label("Parcel Registry");
        title.setFont(Font.font("System", FontWeight.BOLD, 26));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // The Search Input field
        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Search Tracking ID or Recipient...");
        searchField.setPrefWidth(300);
        searchField.setStyle("-fx-background-radius: 10; -fx-padding: 10 15; -fx-border-color: #E2E8F0;");

        header.getChildren().addAll(title, spacer, searchField);

        // --- 2. TABLE SETUP ---
        table = new TableView<>();
        setupTableColumns(); // Configures ID, Recipient, Destination, and Status columns
        
        // Mock Data: In a final version, this would be: 
        // masterData = new DatabaseController().getAllParcels();
        masterData = FXCollections.observableArrayList(
            new ParcelData("TRK-8821", "John Doe", "New York", "In Transit"),
            new ParcelData("TRK-4490", "Jane Smith", "Chicago", "Delivered"),
            new ParcelData("TRK-1102", "Alex Wong", "Miami", "Pending"),
            new ParcelData("TRK-5520", "Sara Connor", "Austin", "Delayed")
        );

        // --- 3. LIVE SEARCH FILTER LOGIC ---
        // We wrap the list in a "FilteredList" so we can hide rows without deleting them
        FilteredList<ParcelData> filteredData = new FilteredList<>(masterData, p -> true);

        // This "Listener" runs every time a character is typed in the search box
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(parcel -> {
                // If search is empty, show everything
                if (newValue == null || newValue.isEmpty()) return true;
                
                String lowerCaseFilter = newValue.toLowerCase();
                
                // Match against Tracking ID or Recipient Name
                if (parcel.getTrackingId().toLowerCase().contains(lowerCaseFilter)) return true;
                if (parcel.getRecipient().toLowerCase().contains(lowerCaseFilter)) return true;
                
                return false; // Hide this row if no match
            });
        });

        // Bind the filtered data to the table
        table.setItems(filteredData);
        table.setPlaceholder(new Label("No parcels match your search criteria."));

        container.getChildren().addAll(header, table);
        VBox.setVgrow(table, Priority.ALWAYS); // Table takes up remaining vertical space
        
        return container;
    }

    /**
     * Helper: Defines the columns and connects them to the ParcelData variables.
     */
    private void setupTableColumns() {
        TableColumn<ParcelData, String> idCol = new TableColumn<>("Tracking ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("trackingId"));

        TableColumn<ParcelData, String> userCol = new TableColumn<>("Recipient");
        userCol.setCellValueFactory(new PropertyValueFactory<>("recipient"));

        TableColumn<ParcelData, String> destCol = new TableColumn<>("Destination");
        destCol.setCellValueFactory(new PropertyValueFactory<>("destination"));

        TableColumn<ParcelData, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        table.getColumns().addAll(idCol, userCol, destCol, statusCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    /**
     * Model Class: Defines what a "Parcel" looks like in the app.
     */
    public static class ParcelData {
        private String trackingId, recipient, destination, status;

        public ParcelData(String id, String name, String dest, String stat) {
            this.trackingId = id; this.recipient = name;
            this.destination = dest; this.status = stat;
        }

        public String getTrackingId() { return trackingId; }
        public String getRecipient() { return recipient; }
        public String getDestination() { return destination; }
        public String getStatus() { return status; }
    }
}