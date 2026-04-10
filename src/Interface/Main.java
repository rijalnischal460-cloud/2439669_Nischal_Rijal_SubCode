package Interface;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import java.net.URL; // Added missing import

/**
 * Main entry point for the CIMS Application.
 * Version: 3.0 (Modern & Secure Edition)
 * Features: Password Hashing, Unified UI Design, Full Database Integration
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // 1. Initialize the Login application
            Login loginApp = new Login();
            primaryStage.setTitle("CIMS | Courier & Inventory Management System");

            // 2. Set Application Icon
            try {
                URL iconUrl = getClass().getResource("/icons/app_logo.png");
                if (iconUrl != null) {
                    // Removed the undefined 'icon' variable and used the proper stream
                    primaryStage.getIcons().add(new Image(iconUrl.toExternalForm()));
                } else {
                    System.out.println("⚠ Icon not found at path: /icons/app_logo.png");
                }
            } catch (Exception e) {
                System.out.println("⚠ Error loading icon: " + e.getMessage());
            }

            // 3. Start the Login scene
            loginApp.start(primaryStage);
            primaryStage.centerOnScreen();
            
            System.out.println("✅ CIMS Engine Started Successfully...");

        } catch (Exception e) {
            System.err.println("❌ CRITICAL ERROR: Could not launch CIMS Application.");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}