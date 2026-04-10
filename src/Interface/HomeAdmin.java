package Interface;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import controller.LoginControl;
import controller.HomeAdminControl;

/**
 * HomeAdmin Class: Refined to use HomeAdminControl for navigation.
 */
public class HomeAdmin extends Application {
    private BorderPane mainLayout;
    private LoginControl.User currentUser;
    private HomeAdminControl controller;
    private VBox sidebar;
    private Label activeViewLabel; // The "Command Overview" label in your top bar

    @Override
    public void start(Stage primaryStage) {
        start(primaryStage, null);
    }

    public void start(Stage primaryStage, LoginControl.User user) {
        this.currentUser = user;
        
        mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: #F7FAFC;");

        // --- TOP BAR SETUP ---
        // We initialize this first so the controller can update the label text
        activeViewLabel = new Label("COMMAND OVERVIEW");
        mainLayout.setTop(createTopBar());

        // --- CONTROLLER INITIALIZATION ---
        controller = new HomeAdminControl(mainLayout, activeViewLabel);

        // --- SIDEBAR NAVIGATION ---
        sidebar = new VBox(15);
        sidebar.setPadding(new Insets(30, 20, 30, 20));
        sidebar.setPrefWidth(260);
        sidebar.setStyle("-fx-background-color: #1A202C;");

        Label logo = new Label("📊 CIMS ADMIN");
        logo.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold; -fx-padding: 0 0 30 0;");

        // Define Navigation Buttons
        Button btnDashboard = createSidebarBtn("🏠 Dashboard");
        Button btnAnalytics = createSidebarBtn("📈 System Reports");

        // --- INTEGRATED NAVIGATION LOGIC ---
        btnDashboard.setOnAction(e -> {
            controller.handleViewSwitch("Dashboard");
            controller.updateActiveButtonStyle(sidebar, "Dashboard");
        });

        btnAnalytics.setOnAction(e -> {
            controller.handleViewSwitch("Reports");
            controller.updateActiveButtonStyle(sidebar, "Reports");
        });

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button btnLogout = createLogoutButton(primaryStage);

        sidebar.getChildren().addAll(logo, btnDashboard, btnAnalytics, spacer, btnLogout);
        mainLayout.setLeft(sidebar);

        // --- INITIAL VIEW ---
        // Set the default state via the controller
        controller.handleViewSwitch("Dashboard");
        controller.updateActiveButtonStyle(sidebar, "Dashboard");

        Scene scene = new Scene(mainLayout, 1300, 800);
        primaryStage.setTitle("CIMS | Admin Command Center");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private HBox createTopBar() {
        HBox topBar = new HBox(20);
        topBar.setPadding(new Insets(15, 30, 15, 30));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: white; -fx-border-color: #E2E8F0; -fx-border-width: 0 0 1 0;");

        activeViewLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #4A5568;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox userBox = new VBox(2);
        userBox.setAlignment(Pos.CENTER_RIGHT);
        
        String name = (currentUser != null) ? currentUser.getUsername() : "Admin User";
        Label userLabel = new Label("👤 " + name);
        userLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2D3748;");
        
        Label statusLabel = new Label("System Administrator • Active");
        statusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #718096;");
        
        userBox.getChildren().addAll(userLabel, statusLabel);
        topBar.getChildren().addAll(activeViewLabel, spacer, userBox);
        
        return topBar;
    }

    private Button createSidebarBtn(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        // Default style (Inactive)
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #A0AEC0; -fx-font-size: 14px; -fx-padding: 12 15; -fx-cursor: hand;");
        return btn;
    }

    private Button createLogoutButton(Stage currentStage) {
        Button btn = new Button("🚪 Secure Logout");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("-fx-background-color: transparent; -fx-border-color: #E53E3E; -fx-text-fill: #E53E3E; -fx-padding: 10; -fx-border-radius: 8; -fx-cursor: hand; -fx-font-weight: bold;");
        
        btn.setOnAction(e -> { 
            new Login().start(new Stage()); 
            currentStage.close(); 
        });
        
        return btn;
    }

    public static void main(String[] args) {
        launch(args);
    }
}