package Interface;

import controller.HomeStaffControl;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * HomeStaff Class: The main interface for Staff members.
 * It uses a BorderPane layout to provide a consistent navigation experience.
 */
public class HomeStaff extends Application {
    private BorderPane mainLayout;
    private Label activeViewLabel; // Displays the current section name in the header
    private HomeStaffControl controller; // Manages logic for switching views

    @Override
    public void start(Stage primaryStage) {
        // Initialize the main container (BorderPane)
        mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: #F7FAFC;");
        
        // --- UI COMPONENTS ---
        // Create the top bar and the left-side menu
        mainLayout.setTop(createHeader());
        setupSidebar(primaryStage);
        
        // --- CONTROLLER LOGIC ---
        // Link the layout to the controller so it can swap the middle screen
        controller = new HomeStaffControl(mainLayout, activeViewLabel);
        
        // DEFAULT VIEW: Show the Dashboard automatically when the app starts
        controller.handleViewSwitch("Dashboard");
        activeViewLabel.setText("DASHBOARD");

        // Window settings (size, title, and scene)
        Scene scene = new Scene(mainLayout, 1400, 850);
        primaryStage.setTitle("CIMS | Staff Portal");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Creates the white top bar showing the current view name and user profile.
     */
    private HBox createHeader() {
        HBox header = new HBox();
        header.setPadding(new Insets(15, 30, 15, 30));
        header.setAlignment(Pos.CENTER_LEFT);
        // Modern look with a white background and subtle shadow
        header.setStyle("-fx-background-color: #ffffff; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);");

        activeViewLabel = new Label("OPERATIONS");
        activeViewLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        activeViewLabel.setTextFill(Color.web("#1A202C"));

        // Spacer pushes the profile bubble to the far right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS); 

        Label userProfile = new Label("👤 STAFF MEMBER");
        userProfile.setStyle("-fx-background-color: #3182CE; -fx-text-fill: white; " +
                            "-fx-padding: 8 15; -fx-background-radius: 20; " +
                            "-fx-font-weight: bold; -fx-font-size: 12px;");

        header.getChildren().addAll(activeViewLabel, spacer, userProfile);
        return header;
    }

    /**
     * Builds the dark sidebar on the left with all navigation buttons.
     */
    private void setupSidebar(Stage stage) {
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(30, 20, 30, 20));
        sidebar.setPrefWidth(280);
        sidebar.setStyle("-fx-background-color: #1A202C;"); // Dark theme

        Label brand = new Label("🎯 CIMS PANEL");
        brand.setTextFill(Color.WHITE);
        brand.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 20));
        brand.setPadding(new Insets(0, 0, 30, 0));

        sidebar.getChildren().add(brand);

        // NAVIGATION MENU
        // List of pages the staff can visit
        String[] menuItems = {"Dashboard", "Parcels", "Inventory", "Dispatch"};
        for (String item : menuItems) {
            Button btn = createNavButton(item);
            btn.setOnAction(e -> {
                // When clicked, update the top header and change the center screen
                activeViewLabel.setText(item.toUpperCase());
                controller.handleViewSwitch(item);
            });
            sidebar.getChildren().add(btn);
        }

        // Pushes the logout button to the very bottom of the sidebar
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        // LOGOUT BUTTON
        Button logout = new Button("🚪 LOGOUT");
        logout.setMaxWidth(Double.MAX_VALUE);
        logout.setStyle("-fx-background-color: #E53E3E; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12; -fx-background-radius: 8; -fx-cursor: hand;");
        
        // Hover effects: change red color when mouse enters/leaves
        logout.setOnMouseEntered(e -> logout.setStyle("-fx-background-color: #C53030; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12; -fx-background-radius: 8; -fx-cursor: hand;"));
        logout.setOnMouseExited(e -> logout.setStyle("-fx-background-color: #E53E3E; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12; -fx-background-radius: 8; -fx-cursor: hand;"));
        
        logout.setOnAction(e -> {
            new Login().start(new Stage()); // Go back to Login screen
            stage.close(); // Close the current staff window
        });

        sidebar.getChildren().addAll(spacer, logout);
        mainLayout.setLeft(sidebar);
    }

    /**
     * Helper to style the sidebar buttons (Transparency and Hover effects).
     */
    private Button createNavButton(String text) {
        Button btn = new Button("📍 " + text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(12, 20, 12, 20));
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #A0AEC0; -fx-font-size: 14px; -fx-cursor: hand;");
        
        // Hover effect: turn dark grey when mouse is over it
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #2D3748; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #A0AEC0; -fx-cursor: hand;"));
        
        return btn;
    }

    public static void main(String[] args) {
        launch(args);
    }
}