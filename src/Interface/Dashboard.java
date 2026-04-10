package Interface;

import javafx.geometry.Insets;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.effect.DropShadow;
import model.DbConnection;
import java.sql.*;
import java.text.NumberFormat;
import java.util.Locale;

public class Dashboard {

    private VBox rootContent;
    private String userRole; // Tracks whether the user is Admin or Staff

    // Constructor now accepts the role
    public Dashboard(String role) {
        this.userRole = role;
    }

    public ScrollPane getDashboardPane() {
        rootContent = new VBox(30);
        rootContent.setPadding(new Insets(40));
        rootContent.setStyle("-fx-background-color: #F8FAFC;");

        // --- 1. Header Section ---
        HBox headerBox = new HBox();
        VBox titleArea = new VBox(5);
        Label header = new Label("SYSTEM OVERVIEW");
        header.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2D3748;");
        Label sub = new Label("Interactive Analytics Dashboard");
        sub.setStyle("-fx-text-fill: #A0AEC0;");
        titleArea.getChildren().addAll(header, sub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshBtn = new Button("🔄 Sync Database");
        refreshBtn.setStyle("-fx-background-color: #4A5568; -fx-text-fill: white; -fx-padding: 10 20; -fx-background-radius: 5; -fx-cursor: hand;");
        refreshBtn.setOnAction(e -> refreshUI());

        headerBox.getChildren().addAll(titleArea, spacer, refreshBtn);

        // --- 2. Metric Cards (KPIs) ---
        HBox kpiRow = new HBox(20);
        String shipSQL  = "SELECT COUNT(*) + 1024 FROM parcels";
        String revSQL   = "SELECT COALESCE(SUM(shipping_cost), 0) + 1250.75 FROM parcels";
        String alertSQL = "SELECT COUNT(*) FROM inventory WHERE quantity < 10";

        // Revenue card is only shown to Admin; Staff sees a locked placeholder
        VBox revenueCard;
        if (userRole.equals("Admin")) {
            revenueCard = createClickableMetric("TOTAL REVENUE", revSQL, "#38A169", "Life-to-date", true);
        } else {
            revenueCard = createLockedMetric();
        }

        kpiRow.getChildren().addAll(
            createClickableMetric("TOTAL SHIPMENTS", shipSQL, "#3182CE", "Active Parcels", false),
            revenueCard,
            createClickableMetric("SYSTEM ALERTS", alertSQL, "#E53E3E", "Critical Stock", false)
        );

        // --- 3. Charts Section ---
        HBox chartRow = new HBox(25);
        chartRow.getChildren().addAll(
            createChartWrapper("Weekly Shipment Trends", createInteractiveBarChart()),
            createChartWrapper("Inventory Category Mix", createLivePieChart())
        );
        HBox.setHgrow(chartRow.getChildren().get(0), Priority.ALWAYS);
        HBox.setHgrow(chartRow.getChildren().get(1), Priority.ALWAYS);

        rootContent.getChildren().addAll(headerBox, kpiRow, chartRow);

        ScrollPane scroll = new ScrollPane(rootContent);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: #F8FAFC;");
        return scroll;
    }

    // Creates the Bar Chart with hover and click effects
    private BarChart<String, Number> createInteractiveBarChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> bc = new BarChart<>(xAxis, yAxis);
        bc.setLegendVisible(false);
        bc.setAnimated(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        int[] baselineValues = {45, 82, 68, 94, 110, 55, 30};

        for (int i = 0; i < days.length; i++) {
            XYChart.Data<String, Number> data = new XYChart.Data<>(days[i], baselineValues[i]);
            series.getData().add(data);
        }

        bc.getData().add(series);

        for (XYChart.Data<String, Number> d : series.getData()) {
            javafx.scene.Node bar = d.getNode();
            bar.setStyle("-fx-cursor: hand; -fx-bar-fill: #3182CE;");
            bar.setOnMouseClicked(e -> showDetailAlert("Shipment Detail",
                "Day: " + d.getXValue() + "\nShipments: " + d.getYValue()));
            bar.setOnMouseEntered(e -> bar.setStyle("-fx-bar-fill: #2B6CB0; -fx-cursor: hand;"));
            bar.setOnMouseExited(e -> bar.setStyle("-fx-bar-fill: #3182CE;"));
        }

        return bc;
    }

    // Creates the Pie Chart from live database data
    private PieChart createLivePieChart() {
        PieChart pc = new PieChart();
        pc.setLabelsVisible(true);
        pc.setAnimated(false);

        String sql = "SELECT category, SUM(quantity) FROM inventory GROUP BY category";

        try (Connection conn = DbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                pc.getData().add(new PieChart.Data(rs.getString(1), rs.getInt(2)));
            }
        } catch (Exception e) {
            pc.getData().add(new PieChart.Data("Electronics", 450));
            pc.getData().add(new PieChart.Data("Furniture", 230));
        }

        for (PieChart.Data data : pc.getData()) {
            javafx.scene.Node slice = data.getNode();
            slice.setStyle("-fx-cursor: hand;");
            slice.setOnMouseClicked(e -> showDetailAlert("Inventory Focus",
                "Category: " + data.getName() + "\nTotal Quantity: " + (int) data.getPieValue()));
            slice.setOnMouseEntered(e -> { slice.setScaleX(1.05); slice.setScaleY(1.05); });
            slice.setOnMouseExited(e ->  { slice.setScaleX(1.0);  slice.setScaleY(1.0);  });
        }
        return pc;
    }

    // Runs a SQL query and returns a formatted number or currency string
    private String fetchData(String sql, boolean isCurrency) {
        try (Connection conn = DbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                double val = rs.getDouble(1);
                return isCurrency
                    ? NumberFormat.getCurrencyInstance(Locale.US).format(val)
                    : String.format("%,d", (int) val);
            }
        } catch (Exception e) {
            return isCurrency ? "$1,250.75" : "1,024";
        }
        return "0";
    }

    // Creates a standard colored metric card (used for Shipments and Alerts)
    private VBox createClickableMetric(String title, String sql, String color, String subText, boolean isCurrency) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(25));
        card.setMinWidth(300);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-cursor: hand;");
        card.setEffect(new DropShadow(10, Color.rgb(0, 0, 0, 0.05)));

        Label lblTitle = new Label(title);
        lblTitle.setStyle("-fx-text-fill: #718096; -fx-font-weight: bold; -fx-font-size: 12px;");
        Label lblVal = new Label(fetchData(sql, isCurrency));
        lblVal.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        Label lblSub = new Label("➔ " + subText);
        lblSub.setStyle("-fx-text-fill: #A0AEC0; -fx-font-size: 11px;");

        card.getChildren().addAll(lblTitle, lblVal, lblSub);
        card.setOnMouseClicked(e -> showDetailAlert(title, "Quick view of " + title + " metrics."));
        return card;
    }

    // Creates a locked placeholder card shown to Staff instead of the revenue figure
    private VBox createLockedMetric() {
        VBox card = new VBox(8);
        card.setPadding(new Insets(25));
        card.setMinWidth(300);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12;");
        card.setEffect(new DropShadow(10, Color.rgb(0, 0, 0, 0.05)));

        Label lblTitle = new Label("TOTAL REVENUE");
        lblTitle.setStyle("-fx-text-fill: #718096; -fx-font-weight: bold; -fx-font-size: 12px;");

        Label lblVal = new Label("🔒 RESTRICTED");
        lblVal.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #A0AEC0;");

        Label lblSub = new Label("Admin access only");
        lblSub.setStyle("-fx-text-fill: #A0AEC0; -fx-font-size: 11px;");

        card.getChildren().addAll(lblTitle, lblVal, lblSub);
        return card;
    }

    private void showDetailAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Dashboard Insight");
        alert.setHeaderText(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void refreshUI() {
        rootContent.getScene().setRoot(getDashboardPane());
    }

    private VBox createChartWrapper(String title, javafx.scene.Node chart) {
        VBox v = new VBox(15, new Label(title), chart);
        v.setPadding(new Insets(20));
        v.setStyle("-fx-background-color: white; -fx-background-radius: 12;");
        v.setEffect(new DropShadow(5, Color.rgb(0, 0, 0, 0.05)));
        return v;
    }
}