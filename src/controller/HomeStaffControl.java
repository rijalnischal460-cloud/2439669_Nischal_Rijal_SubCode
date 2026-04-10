package controller;

import Interface.*;
import javafx.animation.FadeTransition;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * HomeStaffControl: Manages navigation logic for Staff members with Caching.
 */
public class HomeStaffControl {

    private final BorderPane mainLayout;
    private final Label activeViewLabel;

    // Cache
    private final Map<String, Node> viewCache = new HashMap<>();

    // 🔥 FIX: store role
    private String userRole = "Staff"; // default (no UI change)

    public HomeStaffControl(BorderPane mainLayout, Label activeViewLabel) {
        this.mainLayout = mainLayout;
        this.activeViewLabel = activeViewLabel;
    }

    /**
     * Switch views
     */
    public void handleViewSwitch(String viewName) {

        if (viewName == null || viewName.trim().isEmpty()) {
            viewName = "dashboard";
        }

        String key = viewName.toLowerCase().trim();
        Node nextView = null;

        try {

            // 1. Load from cache
            if (viewCache.containsKey(key)) {
                nextView = viewCache.get(key);
                updateLabelOnly(key);
            } else {

                // 2. Create views
                switch (key) {
                    case "dashboard":
                        activeViewLabel.setText("OPERATIONAL DASHBOARD");

                        // ✅ FIX: pass role
                        nextView = safeNode(new Dashboard(userRole).getDashboardPane());
                        break;

                    case "parcels":
                        activeViewLabel.setText("SHIPMENT MANIFEST");
                        nextView = safeNode(new Parcels().getParcelPane());
                        break;

                    case "inventory":
                        activeViewLabel.setText("WAREHOUSE INVENTORY");
                        nextView = safeNode(new Inventory().getInventoryPane());
                        break;

                    case "dispatch":
                        activeViewLabel.setText("FLEET DISPATCH");
                        nextView = safeNode(new Dispatch().getDispatchPane());
                        break;

                    default:
                        activeViewLabel.setText("OPERATIONAL DASHBOARD");
                        nextView = safeNode(new Dashboard(userRole).getDashboardPane());
                        key = "dashboard";
                        break;
                }

                // Cache it
                if (nextView != null) {
                    viewCache.put(key, nextView);
                }
            }

            // 3. Show
            if (nextView != null) {
                applyFadeTransition(nextView);
                mainLayout.setCenter(nextView);
            } else {
                mainLayout.setCenter(createErrorPlaceholder(viewName));
            }

        } catch (Exception e) {
            System.err.println("❌ Navigation Error: " + e.getMessage());
            e.printStackTrace();
            mainLayout.setCenter(createErrorPlaceholder(viewName));
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
     * Label update
     */
    private void updateLabelOnly(String key) {
        switch (key) {
            case "parcels":
                activeViewLabel.setText("SHIPMENT MANIFEST");
                break;
            case "inventory":
                activeViewLabel.setText("WAREHOUSE INVENTORY");
                break;
            case "dispatch":
                activeViewLabel.setText("FLEET DISPATCH");
                break;
            default:
                activeViewLabel.setText("OPERATIONAL DASHBOARD");
                break;
        }
    }

    /**
     * Animation
     */
    private void applyFadeTransition(Node node) {
        node.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(300), node);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    /**
     * Error UI (unchanged)
     */
    private StackPane createErrorPlaceholder(String viewName) {
        Label errorLabel = new Label(
                "❌ Could not load module: " + viewName.toUpperCase()
                        + "\nCheck your database connection."
        );

        errorLabel.setStyle(
                "-fx-text-fill: #E53E3E;" +
                "-fx-font-weight: bold;" +
                "-fx-text-alignment: center;"
        );

        StackPane placeholder = new StackPane(errorLabel);
        placeholder.setAlignment(Pos.CENTER);
        return placeholder;
    }
}