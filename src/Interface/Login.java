package Interface;

import javafx.application.Application; // JavaFX Application superclass
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import model.DbConnection; // Custom class for database abstraction
import Utility.PasswordHasher; // Utility class for password security
import java.sql.*;

/**
 * Class: Login
 * This class represents the login module of the system.
 * Inheritance: extends Application to inherit JavaFX application features.
 */
public class Login extends Application { 

    /**
     * Encapsulation:
     * private variable hides internal state inside class.
     */
    private boolean isRegisterMode = false;

    /**
     * Polymorphism:
     * start() method overrides Application class method.
     */
    @ Override
    public void start(Stage primaryStage) {

        /**
         * Object Creation:
         * root is an object of HBox class.
         */
        HBox root = new HBox();
        root.setStyle("-fx-background-color: #ffffff;");

        // --- LEFT BRAND PANEL ---

        /**
         * Object Creation:
         * brandPanel is object of VBox class.
         */
        VBox brandPanel = new VBox(15);
        brandPanel.setAlignment(Pos.CENTER);
        brandPanel.setPrefWidth(400);
        brandPanel.setStyle("-fx-background-color: #1A202C;");

        /**
         * Object Creation:
         * brandLogo is object of Label class.
         */
        Label brandLogo = new Label("CIMS");
        brandLogo.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 60));
        brandLogo.setTextFill(Color.WHITE);

        /**
         * Object Creation:
         * tagLine is another Label object.
         */
        Label tagLine = new Label("COURIER & INVENTORY");
        tagLine.setTextFill(Color.web("#aaaaaa"));
        tagLine.setStyle("-fx-letter-spacing: 2px; -fx-font-weight: bold;");

        brandPanel.getChildren().addAll(brandLogo, tagLine);

        // --- RIGHT FORM PANEL ---

        /**
         * Object Creation:
         * formPanel is object of VBox class.
         */
        VBox formPanel = new VBox();
        formPanel.setAlignment(Pos.CENTER);
        HBox.setHgrow(formPanel, Priority.ALWAYS);

        /**
         * Object Creation:
         * formContainer is object of VBox class.
         */
        VBox formContainer = new VBox(15);
        formContainer.setMaxWidth(350);
        formContainer.setAlignment(Pos.CENTER_LEFT);

        /**
         * Object Creation:
         * welcomeLabel is object of Label class.
         */
        Label welcomeLabel = new Label("SECURE ACCESS");
        welcomeLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));

        /**
         * Abstraction:
         * createStyledField hides internal styling details.
         */
        TextField userField = createStyledField("Username");

        /**
         * Abstraction:
         * createStyledPassField hides password field styling.
         */
        PasswordField passField = createStyledPassField("Password");
        PasswordField confirmField = createStyledPassField("Confirm Password");

        confirmField.setVisible(false);
        confirmField.setManaged(false);

        /**
         * Object Creation:
         * Buttons created from Button class.
         */
        Button adminBtn = createStyledButton("SIGN IN AS ADMIN", true);
        Button staffBtn = createStyledButton("SIGN IN AS STAFF", false);
        Button registerBtn = createStyledButton("CREATE ACCOUNT", true);

        registerBtn.setVisible(false);
        registerBtn.setManaged(false);

        /**
         * Object Creation:
         * toggleLink is object of Hyperlink class.
         */
        Hyperlink toggleLink = new Hyperlink("Don't have an account? Register");
        toggleLink.setStyle("-fx-text-fill: #555555; -fx-underline: false;");

        // --- LOGIC: UI TOGGLE MECHANISM ---

        toggleLink.setOnAction(e -> {
            isRegisterMode = !isRegisterMode;

            welcomeLabel.setText(isRegisterMode ? "REGISTER USER" : "SECURE ACCESS");
            toggleLink.setText(isRegisterMode ? "Already have an account? Sign In" : "Don't have an account? Register");

            confirmField.setVisible(isRegisterMode);
            confirmField.setManaged(isRegisterMode);

            registerBtn.setVisible(isRegisterMode);
            registerBtn.setManaged(isRegisterMode);

            adminBtn.setVisible(!isRegisterMode);
            adminBtn.setManaged(!isRegisterMode);

            staffBtn.setVisible(!isRegisterMode);
            staffBtn.setManaged(!isRegisterMode);

            userField.clear();
            passField.clear();
            confirmField.clear();
        });

        // --- BUTTON ACTIONS ---

        /**
         * Method Call:
         * Calls login method when admin button clicked.
         */
        adminBtn.setOnAction(e -> handleLogin(userField.getText(), passField.getText(), "Admin", primaryStage));

        /**
         * Method Call:
         * Calls login method when staff button clicked.
         */
        staffBtn.setOnAction(e -> handleLogin(userField.getText(), passField.getText(), "Staff", primaryStage));

        /**
         * Method Call:
         * Calls registration method.
         */
        registerBtn.setOnAction(e -> handleRegistration(userField.getText(), passField.getText(), confirmField.getText(), toggleLink));

        formContainer.getChildren().addAll(welcomeLabel, userField, passField, confirmField, adminBtn, staffBtn, registerBtn, toggleLink);
        formPanel.getChildren().add(formContainer);

        root.getChildren().addAll(brandPanel, formPanel);

        primaryStage.setTitle("CIMS - Login");

        /**
         * Object Creation:
         * Scene object created.
         */
        primaryStage.setScene(new Scene(root, 950, 600));
        primaryStage.show();
    }

    /**
     * Method:
     * Encapsulates login functionality.
     */
    private void handleLogin(String user, String pass, String role, Stage stage) {
        if (user.isEmpty() || pass.isEmpty()) {
            showDialog("Error", "Please fill in all fields.");
            return;
        }

        String query = "SELECT * FROM users WHERE username=? AND role=?";

        /**
         * Abstraction:
         * Database connection hidden inside DbConnection class.
         */
        try (Connection conn = DbConnection.getConnection();

             /**
              * Object Creation:
              * PreparedStatement object for secure SQL query.
              */
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, user);
            ps.setString(2, role);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String hashedPass = rs.getString("password");

                /**
                 * Abstraction:
                 * Password verification hidden inside PasswordHasher.
                 */
                if (PasswordHasher.verifyPassword(pass, hashedPass)) {
                    showDialog("Success", "Login successful! Welcome " + user);
                    redirect(role, stage);
                } else {
                    showDialog("Login Failed", "Invalid password.");
                }
            } else {
                showDialog("Login Failed", "User not found with the role: " + role);
            }
        } catch (SQLException e) {
            showDialog("Database Error", "Check your connection.");
            e.printStackTrace();
        }
    }

    /**
     * Method:
     * Encapsulates registration logic.
     */
    private void handleRegistration(String user, String pass, String conf, Hyperlink toggle) {
        if (user.isEmpty() || pass.isEmpty() || conf.isEmpty()) {
            showDialog("Error", "Fields cannot be empty.");
            return;
        }
        if (!pass.equals(conf)) {
            showDialog("Error", "Passwords do not match!");
            return;
        }

        String insertQuery = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertQuery)) {

            ps.setString(1, user);

            /**
             * Abstraction:
             * Password hashing hidden inside utility class.
             */
            ps.setString(2, PasswordHasher.hashPassword(pass));

            ps.setString(3, "Staff");

            int result = ps.executeUpdate();
            if (result > 0) {
                showDialog("Success", "Account created! Please sign in.");
                toggle.fire();
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                showDialog("Error", "Username already exists!");
            } else {
                showDialog("Error", "Registration failed.");
            }
        }
    }

    /**
     * Method:
     * Redirects user based on role.
     */
    private void redirect(String role, Stage currentStage) {
        try {

            /**
             * Object Creation:
             * Dynamic object creation based on role.
             */
            if (role.equals("Admin")) {
                new HomeAdmin().start(new Stage());
            } else {
                new HomeStaff().start(new Stage());
            }

            currentStage.close();

        } catch (Exception e) {
            showDialog("Error", "Navigation failed.");
        }
    }

    /**
     * Abstraction:
     * Helper method hides TextField styling.
     */
    private TextField createStyledField(String p) {
        TextField t = new TextField();
        t.setPromptText(p);
        t.setPrefHeight(45);
        t.setStyle("-fx-background-color: #ffffff; -fx-border-color: #CBD5E0; -fx-border-radius: 8; -fx-padding: 12;");
        return t;
    }

    /**
     * Abstraction:
     * Helper method hides PasswordField styling.
     */
    private PasswordField createStyledPassField(String p) {
        PasswordField t = new PasswordField();
        t.setPromptText(p);
        t.setPrefHeight(45);
        t.setStyle("-fx-background-color: #ffffff; -fx-border-color: #CBD5E0; -fx-border-radius: 8; -fx-padding: 12;");
        return t;
    }

    /**
     * Abstraction:
     * Helper method hides button styling.
     */
    private Button createStyledButton(String text, boolean isPrimary) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPrefHeight(45);

        String bg = isPrimary ? "#3182CE" : "#ffffff";
        String fg = isPrimary ? "#ffffff" : "#3182CE";
        String border = isPrimary ? "transparent" : "#3182CE";

        btn.setStyle(String.format("-fx-background-color: %s; -fx-text-fill: %s; -fx-border-color: %s; -fx-border-width: 2; -fx-cursor: hand; -fx-font-weight: bold; -fx-background-radius: 8; -fx-border-radius: 8;", bg, fg, border));

        return btn;
    }

    /**
     * Method:
     * Encapsulates alert display.
     */
    private void showDialog(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    /**
     * Main Method:
     * Program execution starts here.
     */
    public static void main(String[] args) {
        launch(args);
    }
}