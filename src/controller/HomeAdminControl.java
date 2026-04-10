package controller;

import Interface.*;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import java.util.HashMap;
import java.util.Map;

/**
 * HomeAdminControl: Optimized with View Caching and robust error handling.
 */
public class HomeAdminControl {

    private final BorderPane mainLayout;
    private final Label activeViewLabel;

    // Cache
    private final Map<String, Node> viewCache = new HashMap<>();

    public HomeAdminControl(BorderPane mainLayout, Label activeViewLabel) {
        this.mainLayout = mainLayout;
        this.activeViewLabel = activeViewLabel;
    }

    /**
     * Switch view
     */
    public void handleViewSwitch(String view) {

        if (view == null || view.trim().isEmpty()) {
            view = "Dashboard";
        }

        String key = view.trim();

        // Update header
        activeViewLabel.setText("ADMIN CONSOLE > " + key.toUpperCase());

        try {

            // 1. Create if not cached
            if (!viewCache.containsKey(key)) {

                Node newNode;

                switch (key) {
                    case "Dashboard":
                        // ✅ FIX: pass Admin role
                        newNode = safeNode(new Dashboard("Admin").getDashboardPane());
                        break;

                    case "Parcels":
                        newNode = safeNode(new Parcels().getParcelPane());
                        break;

                    case "Inventory":
                        newNode = safeNode(new Inventory().getInventoryPane());
                        break;

                    case "Dispatch":
                        newNode = safeNode(new Dispatch().getDispatchPane());
                        break;

                    case "Reports":
                        newNode = safeNode(new Report().getReportPane());
                        break;

                    case "User Management":
                        newNode = createPlaceholder("👥 User Management & Permissions");
                        break;

                    default:
                        newNode = createPlaceholder("⚠️ View Not Found: " + key);
                        break;
                }

                viewCache.put(key, newNode);
            }

            // 2. Show cached view
            mainLayout.setCenter(viewCache.get(key));

        } catch (Exception e) {
            System.err.println("❌ Critical Error loading view: " + key);
            e.printStackTrace();
            mainLayout.setCenter(createErrorPlaceholder(key));
        }
    }

    /**
     * Prevent null crash
     */
    private Node safeNode(Node node) {
        if (node == null) {
            return createErrorPlaceholder("NULL VIEW");
        }
        return node;
    }

    /**
     * Sidebar highlight
     */
    public void updateActiveButtonStyle(VBox sidebar, String activeText) {
        for (Node node : sidebar.getChildren()) {
            if (node instanceof Button btn) {

                boolean isActive = btn.getText().toLowerCase().contains(activeText.toLowerCase());

                if (isActive) {
                    btn.setStyle(
                        "-fx-background-color: #2D3748; -fx-text-fill: white; " +
                        "-fx-padding: 12 20; -fx-background-radius: 8; -fx-font-weight: bold;"
                    );
                } else if (!btn.getText().contains("Logout")) {
                    btn.setStyle(
                        "-fx-background-color: transparent; -fx-text-fill: #A0AEC0; -fx-padding: 12 20;"
                    );
                }
            }
        }
    }

    /**
     * Error UI
     */
    private StackPane createErrorPlaceholder(String viewName) {
        Label errorMsg = new Label(
            "❌ Error loading module: " + viewName +
            "\nPlease check your database connection."
        );

        errorMsg.setStyle("-fx-text-fill: #E53E3E; -fx-font-weight: bold;");

        StackPane p = new StackPane(errorMsg);
        p.setStyle("-fx-background-color: #FFF5F5;");
        return p;
    }

    /**
     * Placeholder UI
     */
    private StackPane createPlaceholder(String title) {
        VBox vbox = new VBox(15,
            new Label(title) {{
                setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #4A5568;");
            }},
            new Label("This module is currently being integrated.") {{
                setStyle("-fx-text-fill: #718096;");
            }}
        );

        vbox.setAlignment(Pos.CENTER);

        StackPane p = new StackPane(vbox);
        p.setStyle("-fx-background-color: white; -fx-padding: 50;");
        return p;
    }
}