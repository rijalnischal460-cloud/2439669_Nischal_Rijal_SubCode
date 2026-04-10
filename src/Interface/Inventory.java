package Interface;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.effect.DropShadow;
import model.DbConnection;
import java.sql.*;
import java.util.Optional;

public class Inventory {

    private FlowPane inventoryGrid;
    private TextField searchField;
    private HBox categoryBox;
    private String currentCategory = "All Items";

    public ScrollPane getInventoryPane() {
        VBox mainContainer = new VBox(30);
        mainContainer.setPadding(new Insets(40));
        mainContainer.setStyle("-fx-background-color: #F7FAFC;");

        // --- 1. HEADER ---
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        
        VBox titleArea = new VBox(5);
        Label title = new Label("Warehouse Inventory");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #1A202C;");
        Label sub = new Label("Live stock monitor and asset management.");
        sub.setStyle("-fx-text-fill: #718096;");
        titleArea.getChildren().addAll(title, sub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnAdd = new Button("+ Add New Item");
        btnAdd.setStyle("-fx-background-color: #3182CE; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand;");
        btnAdd.setOnAction(e -> handleAddNewItem());

        header.getChildren().addAll(titleArea, spacer, btnAdd);

        // --- 2. FILTER & SEARCH ---
        HBox filterBar = new HBox(15);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        searchField = new TextField();
        searchField.setPromptText("Search by item name...");
        searchField.setPrefWidth(320);
        searchField.setStyle("-fx-background-radius: 10; -fx-padding: 10; -fx-border-color: #E2E8F0;");
        searchField.textProperty().addListener((obs, old, newVal) -> refreshUI());

        categoryBox = new HBox(10);
        String[] cats = {"All Items", "Packaging", "Shipping", "Office", "Electronics"};
        for (String c : cats) {
            categoryBox.getChildren().add(createFilterPill(c));
        }

        filterBar.getChildren().addAll(searchField, categoryBox);

        // --- 3. GRID ---
        inventoryGrid = new FlowPane();
        inventoryGrid.setHgap(20);
        inventoryGrid.setVgap(20);

        refreshUI(); 

        mainContainer.getChildren().addAll(header, filterBar, inventoryGrid);
        
        ScrollPane scroll = new ScrollPane(mainContainer);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: #F7FAFC; -fx-border-color: transparent;");
        return scroll;
    }

    // ================== CORE UI LOGIC ==================

    private void refreshUI() {
        inventoryGrid.getChildren().clear();
        String search = searchField.getText().trim();

        String sql = "SELECT * FROM inventory WHERE item_name LIKE ?";
        if (!currentCategory.equals("All Items")) {
            sql += " AND category = ?";
        }

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, "%" + search + "%");
            if (!currentCategory.equals("All Items")) {
                ps.setString(2, currentCategory);
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                inventoryGrid.getChildren().add(createStockCard(
                    rs.getString("item_name"),
                    rs.getInt("quantity"),
                    rs.getInt("reorder_level"),
                    rs.getString("category")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private VBox createStockCard(String name, int qty, int reorder, String cat) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.setPrefWidth(270);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12;");
        card.setEffect(new DropShadow(10, Color.rgb(0,0,0,0.05)));

        // Status Logic
        String status = "IN STOCK";
        String color = "#38A169";
        if (qty <= 0) { status = "OUT OF STOCK"; color = "#E53E3E"; }
        else if (qty < reorder) { status = "LOW STOCK"; color = "#D69E2E"; }

        Label lblStat = new Label(status);
        lblStat.setStyle("-fx-text-fill: "+color+"; -fx-background-color: "+color+"20; -fx-padding: 4 10; -fx-background-radius: 5; -fx-font-weight: bold; -fx-font-size: 10px;");

        Label lblName = new Label(name);
        lblName.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #2D3748;");
        
        Label lblQty = new Label(qty + " Units");
        lblQty.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1A202C;");

        Hyperlink adjust = new Hyperlink("Adjust Stock →");
        adjust.setStyle("-fx-text-fill: #3182CE; -fx-font-weight: bold; -fx-underline: false;");
        adjust.setOnAction(e -> handleUpdateQuantity(name, qty));

        card.getChildren().addAll(lblStat, lblName, lblQty, adjust);
        return card;
    }

    private Button createFilterPill(String catName) {
        Button btn = new Button(catName);
        updatePillStyle(btn, catName.equals(currentCategory));
        
        btn.setOnAction(e -> {
            currentCategory = catName;
            categoryBox.getChildren().forEach(n -> updatePillStyle((Button)n, ((Button)n).getText().equals(currentCategory)));
            refreshUI();
        });
        return btn;
    }

    private void updatePillStyle(Button btn, boolean isActive) {
        btn.setStyle(isActive ? 
            "-fx-background-color: #2D3748; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 8 18; -fx-cursor: hand;" :
            "-fx-background-color: white; -fx-text-fill: #4A5568; -fx-background-radius: 20; -fx-border-color: #E2E8F0; -fx-padding: 8 18; -fx-cursor: hand;");
    }

    // ================== DATABASE ACTIONS ==================

    private void handleUpdateQuantity(String name, int oldQty) {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(oldQty));
        dialog.setTitle("Adjust Inventory");
        dialog.setHeaderText("Updating: " + name);
        dialog.setContentText("Enter current quantity in warehouse:");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(val -> {
            try {
                int newQty = Integer.parseInt(val.trim());
                String sql = "UPDATE inventory SET quantity = ? WHERE item_name = ?";
                try (Connection conn = DbConnection.getConnection(); 
                     PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, newQty);
                    ps.setString(2, name);
                    if (ps.executeUpdate() > 0) refreshUI();
                }
            } catch (Exception ex) { showError("Please enter a valid whole number."); }
        });
    }

    private void handleAddNewItem() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("New Inventory Item");
        dialog.setHeaderText("Register a new asset to the database.");

        GridPane grid = new GridPane();
        grid.setHgap(15); grid.setVgap(15); grid.setPadding(new Insets(20));

        TextField fName = new TextField(); fName.setPromptText("Item Name");
        TextField fCat = new TextField(); fCat.setPromptText("Category");
        TextField fQty = new TextField(); fQty.setPromptText("Quantity");
        TextField fReorder = new TextField("10"); // Default 10

        grid.add(new Label("Name:"), 0, 0);       grid.add(fName, 1, 0);
        grid.add(new Label("Category:"), 0, 1);   grid.add(fCat, 1, 1);
        grid.add(new Label("Initial Qty:"), 0, 2); grid.add(fQty, 1, 2);
        grid.add(new Label("Reorder Point:"), 0, 3); grid.add(fReorder, 1, 3);

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                try {
                    String sql = "INSERT INTO inventory (item_name, category, quantity, reorder_level) VALUES (?, ?, ?, ?)";
                    try (Connection conn = DbConnection.getConnection(); 
                         PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setString(1, fName.getText().trim());
                        ps.setString(2, fCat.getText().trim());
                        ps.setInt(3, Integer.parseInt(fQty.getText().trim()));
                        ps.setInt(4, Integer.parseInt(fReorder.getText().trim()));
                        ps.executeUpdate();
                        refreshUI();
                    }
                } catch (Exception ex) { showError("Validation Error: Check your inputs."); }
            }
        });
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg);
        alert.setTitle("Error Encountered");
        alert.show();
    }
}