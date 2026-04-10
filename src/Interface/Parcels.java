package Interface;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.effect.DropShadow;
import java.util.Comparator;
import java.util.Optional;

public class Parcels {

    // 1. Data Model
    // This internal class acts as a blueprint for what a "Parcel" is (ID, Sender, Weight, etc.)
    private static class ParcelData {
        String id, sender, dest, weight, status, color;
        double weightValue;

        ParcelData(String id, String sender, String dest, String weight, String status, String color) {
            this.id = id;
            this.sender = sender;
            this.dest = dest;
            this.weight = weight;
            this.status = status;
            this.color = color;
            // Converts the text weight "10kg" into a number 10.0 so we can sort it properly
            this.weightValue = Double.parseDouble(weight.replace("kg", ""));
        }
    }

    // These lists hold the actual parcel data in memory
    private ObservableList<ParcelData> masterData = FXCollections.observableArrayList();
    private VBox listContainer = new VBox(12); // This holds the visual rows on the screen
    private SortedList<ParcelData> sortedData;

    // This method creates the main visual screen for the Parcel list
    public ScrollPane getParcelPane() {
        VBox mainContainer = new VBox(25);
        mainContainer.setPadding(new Insets(40));
        mainContainer.setStyle("-fx-background-color: #F7FAFC;");

        // --- 1. HEADER & REGISTER BUTTON ---
        HBox header = new HBox();
        VBox titleArea = new VBox(5);
        Label title = new Label("Shipment Manifest");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #1A202C;");
        Label sub = new Label("Real-time oversight of all outbound logistics.");
        sub.setStyle("-fx-text-fill: #718096; -fx-font-size: 14px;");
        titleArea.getChildren().addAll(title, sub);
        
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        // This button opens the registration popup form
        Button btnAdd = new Button("+ Register New Parcel");
        btnAdd.setStyle("-fx-background-color: #3182CE; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12 25; -fx-background-radius: 8; -fx-cursor: hand;");
        btnAdd.setOnAction(e -> showRegistrationDialog());

        header.getChildren().addAll(titleArea, sp, btnAdd);

        // --- 2. FILTER & SORTING ---
        // Search bar and dropdown menu to organize the list
        HBox filterBar = new HBox(15);
        TextField search = new TextField();
        search.setPromptText("Search by Tracking ID...");
        search.setPrefWidth(450);
        search.setStyle("-fx-background-radius: 10; -fx-padding: 12; -fx-border-color: #E2E8F0;");

        ComboBox<String> sortFilter = new ComboBox<>();
        sortFilter.getItems().addAll("Newest", "Weight (High to Low)", "Destination (A-Z)");
        sortFilter.getSelectionModel().selectFirst();
        sortFilter.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #E2E8F0;");

        filterBar.getChildren().addAll(search, sortFilter);

        // --- 3. LIST HEADER ---
        // Column titles for the list (Details, Route, Weight, Status)
        HBox listHeader = new HBox(30);
        listHeader.setPadding(new Insets(0, 25, 0, 25));
        Label h1 = new Label("SHIPMENT DETAILS"); h1.setPrefWidth(220);
        Label h2 = new Label("ROUTE PATH"); h2.setPrefWidth(220);
        Label h3 = new Label("WEIGHT"); h3.setPrefWidth(100);
        String hStyle = "-fx-text-fill: #A0AEC0; -fx-font-weight: bold; -fx-font-size: 11px;";
        h1.setStyle(hStyle); h2.setStyle(hStyle); h3.setStyle(hStyle);
        listHeader.getChildren().addAll(h1, h2, h3, new Region(), new Label("STATUS"));

        // --- 4. DATA SETUP ---
        if (masterData.isEmpty()) loadInitialData(); // Load some example parcels
        sortedData = new SortedList<>(masterData);
        
        // This makes the list update immediately when the user changes the sorting dropdown
        sortFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateSort(newVal);
            renderList();
        });

        updateSort("Newest"); 
        renderList(); // Draws the list on the screen

        mainContainer.getChildren().addAll(header, filterBar, listHeader, listContainer);
        
        // Wrap everything in a scrollable pane
        ScrollPane scroll = new ScrollPane(mainContainer);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: #F7FAFC;");
        return scroll;
    }

    // ================== REGISTRATION LOGIC ==================

    // This method creates and shows a popup window to enter new parcel info
    private void showRegistrationDialog() {
        Dialog<ParcelData> dialog = new Dialog<>();
        dialog.setTitle("Register New Shipment");
        dialog.setHeaderText("Enter parcel details to add to the manifest.");

        ButtonType saveButtonType = new ButtonType("Register", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // The form layout (Grid)
        GridPane grid = new GridPane();
        grid.setHgap(15); grid.setVgap(15);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField idTxt = new TextField(); idTxt.setPromptText("TRK-XXXXXX");
        TextField senderTxt = new TextField(); senderTxt.setPromptText("Company Name");
        TextField destTxt = new TextField(); destTxt.setPromptText("City, State");
        TextField weightTxt = new TextField(); weightTxt.setPromptText("e.g. 10.5");
        ComboBox<String> statusCb = new ComboBox<>();
        statusCb.getItems().addAll("PENDING", "IN TRANSIT", "DELIVERED", "DELAYED");
        statusCb.getSelectionModel().selectFirst();

        grid.add(new Label("Tracking ID:"), 0, 0); grid.add(idTxt, 1, 0);
        grid.add(new Label("Sender:"), 0, 1);     grid.add(senderTxt, 1, 1);
        grid.add(new Label("Destination:"), 0, 2); grid.add(destTxt, 1, 2);
        grid.add(new Label("Weight (kg):"), 0, 3); grid.add(weightTxt, 1, 3);
        grid.add(new Label("Initial Status:"), 0, 4); grid.add(statusCb, 1, 4);

        dialog.getDialogPane().setContent(grid);

        // When "Register" is clicked, take the text and create a new Parcel object
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String status = statusCb.getValue();
                String color = getColorForStatus(status);
                return new ParcelData(idTxt.getText(), senderTxt.getText(), destTxt.getText(), 
                                      weightTxt.getText() + "kg", status, color);
            }
            return null;
        });

        // Show the popup and if successful, add the item to our list
        Optional<ParcelData> result = dialog.showAndWait();
        result.ifPresent(newParcel -> {
            masterData.add(newParcel);
            renderList(); // Refresh the list view
        });
    }

    // ================== HELPERS ==================

    // Assigns colors to status text (Green for Delivered, Red for Delayed, etc.)
    private String getColorForStatus(String status) {
        switch (status) {
            case "DELIVERED": return "#38A169";
            case "PENDING":   return "#D69E2E";
            case "DELAYED":   return "#E53E3E";
            default:          return "#3182CE"; // IN TRANSIT (Blue)
        }
    }

    // Changes the rules of how the list is ordered
    private void updateSort(String option) {
        if ("Weight (High to Low)".equals(option)) {
            sortedData.setComparator(Comparator.comparingDouble((ParcelData p) -> p.weightValue).reversed());
        } else if ("Destination (A-Z)".equals(option)) {
            sortedData.setComparator(Comparator.comparing(p -> p.dest));
        } else {
            // Default: Sort by ID (usually newest first if IDs are sequential)
            sortedData.setComparator(Comparator.comparing((ParcelData p) -> p.id).reversed());
        }
    }

    // This clears the screen and redraws all parcel rows from the data list
    private void renderList() {
        listContainer.getChildren().clear();
        for (ParcelData p : sortedData) {
            listContainer.getChildren().add(createModernParcelRow(p.id, p.sender, p.dest, p.weight, p.status, p.color));
        }
    }

    // Hard-coded example data for testing
    private void loadInitialData() {
        masterData.addAll(
            new ParcelData("TRK-882910", "Global Logistics", "Chicago, IL", "12.4kg", "IN TRANSIT", "#3182CE"),
            new ParcelData("TRK-110293", "Amazon Hub", "Miami, FL", "1.2kg", "DELIVERED", "#38A169")
        );
    }

    // Creates the visual design for a single row in the parcel list
    private HBox createModernParcelRow(String id, String sender, String dest, String weight, String status, String color) {
        HBox row = new HBox(30);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(18, 25, 18, 25));
        row.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: transparent; -fx-border-width: 2;");
        row.setEffect(new DropShadow(8, Color.rgb(0, 0, 0, 0.05)));

        // Column 1: ID and Sender name
        VBox col1 = new VBox(4);
        Label lblId = new Label(id); lblId.setStyle("-fx-font-weight: bold; -fx-text-fill: #2D3748;");
        col1.getChildren().addAll(lblId, new Label(sender));
        col1.setPrefWidth(220);

        // Column 2: Route visual (Warehouse -> Destination)
        HBox route = new HBox(10);
        route.setAlignment(Pos.CENTER_LEFT);
        route.getChildren().addAll(new Label("WH-01"), new Label("→"), new Label(dest));
        route.setPrefWidth(220);

        // Column 3: Weight
        Label lblW = new Label(weight); lblW.setPrefWidth(100);

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

        // Column 4: Status Pill (the colored bubble)
        Label pill = new Label(status);
        pill.setPadding(new Insets(6, 15, 6, 15));
        pill.setStyle("-fx-background-color: " + color + "20; -fx-text-fill: " + color + "; -fx-font-weight: bold; -fx-background-radius: 20; -fx-font-size: 11px;");

        row.getChildren().addAll(col1, route, lblW, spacer, pill);
        
        // Hover effects: change background and border color when the mouse moves over a row
        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #F7FAFC; -fx-background-radius: 12; -fx-border-color: " + color + "; -fx-border-width: 2;"));
        row.setOnMouseExited(e -> row.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: transparent; -fx-border-width: 2;"));
        
        return row;
    }
}