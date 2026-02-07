package com.recyclingquest.ui;

import com.recyclingquest.dao.CouponDao;
import com.recyclingquest.dao.RecycleDao;
import com.recyclingquest.dao.SpotDao;
import com.recyclingquest.dao.UserDao;
import com.recyclingquest.db.Database;
import com.recyclingquest.model.Coupon;
import com.recyclingquest.model.RecycleEntry;
import com.recyclingquest.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.scene.web.WebView;
import javafx.concurrent.Worker;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;

import java.io.File;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.awt.image.BufferedImage;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

public class DashboardScreen {
    private final BorderPane root = new BorderPane();
    private final long userId;
    private final RecycleDao recycleDao;
    private final UserDao userDao;
    private final CouponDao couponDao;
    private final Runnable onBack;
    private TableView<RecycleEntry> historyTable;

    public DashboardScreen(Database db, long userId, Runnable onBack) {
        root.getStyleClass().add("screen-root");
        
        // Set background image
        try {
            Image bgImage = new Image(Objects.requireNonNull(getClass().getResource("/images/bg2.jpeg")).toExternalForm());
            BackgroundSize bgSize = new BackgroundSize(100, 100, true, true, false, true);
            BackgroundImage backgroundImage = new BackgroundImage(
                bgImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                bgSize
            );
            root.setBackground(new Background(backgroundImage));
        } catch (Exception e) {
            System.err.println("Could not load background image: " + e.getMessage());
        }
        
        this.userDao = new UserDao(db);
        this.recycleDao = new RecycleDao(db);
        this.couponDao = new CouponDao(db);
        this.userId = userId;
        this.onBack = onBack;
        SpotDao spotDao = new SpotDao(db);
        spotDao.seedIfEmpty();

        TabPane tabs = new TabPane();
        tabs.setStyle("-fx-background-color: transparent;");
        
        Tab recycleTab = new Tab("Recycle", buildRecycleTab());
        Tab historyTab = new Tab("History", buildHistoryTab());
        Tab tradeTab = new Tab("ScrapTrade", buildTradeTab());
        Tab rulebookTab = new Tab("Rulebook", buildRulebookTab());
        Tab spotsTab = new Tab("Recycling Spots", buildSpotsTab(spotDao));
        Tab couponsTab = new Tab("Coupons", buildCouponsTab());

        tabs.getTabs().addAll(recycleTab, historyTab, tradeTab, rulebookTab, spotsTab, couponsTab);
        tabs.getTabs().forEach(t -> t.setClosable(false));

        // Top bar with Back button
        Button back = new Button("Back");
        back.setOnAction(e -> { if (this.onBack != null) this.onBack.run(); });
        HBox top = new HBox(10, back, new Label("Dashboard"));
        top.setPadding(new Insets(6));

        root.setTop(top);
        root.setCenter(tabs);
        root.setPadding(new Insets(10));
    }

    private int calculateEcoPoints(double weight) {
        if (weight <= 0.05) {
            return 1;
        } else if (weight <= 0.25) {
            return 3;
        } else if (weight <= 0.5) {
            return 5;
        } else if (weight <= 1.0) {
            return 10;
        } else if (weight <= 2.0) {
            return 20;
        } else if (weight <= 5.0) {
            return 40;
        } else {
            return 70;
        }
    }

    private Parent buildRecycleTab() {
        // Main container - centered
        VBox mainContainer = new VBox();
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setPadding(new Insets(30));
        mainContainer.setStyle("-fx-background-color: transparent;");
        
        // Form container with centered content - equal padding on all sides
        VBox formBox = new VBox(25);
        formBox.setAlignment(Pos.CENTER);
        formBox.setPadding(new Insets(40));
        formBox.setMaxWidth(600);
        formBox.setStyle("-fx-background-color: rgba(255,255,255,0.85); -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 8, 0, 0, 2);");

        Label titleLabel = new Label("Enter details to recycle");
        titleLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #333333;");
        titleLabel.setAlignment(Pos.CENTER);

        // Consistent width for all single elements
        double elementWidth = 500.0;
        
        ComboBox<String> category = new ComboBox<>();
        category.getItems().addAll("Plastic", "Paper", "Glass", "Metal", "E-Waste", "Organic");
        category.getSelectionModel().selectFirst();
        category.setMaxWidth(elementWidth);
        category.setPrefWidth(elementWidth);
        category.setPrefHeight(45);
        category.setStyle("-fx-font-size: 16px;");

        TextField weight = new TextField();
        weight.setPromptText("Weight (kg)");
        weight.setMaxWidth(elementWidth);
        weight.setPrefWidth(elementWidth);
        weight.setPrefHeight(45);
        weight.setStyle("-fx-font-size: 16px;");

        // Photo row: split evenly with equal spacing
        TextField photoPath = new TextField();
        photoPath.setPromptText("Photo path (optional)");
        photoPath.setPrefHeight(45);
        photoPath.setStyle("-fx-font-size: 16px;");
        
        Button browse = new Button("Choose Photo");
        browse.setPrefHeight(45);
        browse.setMinWidth(150);
        browse.setStyle("-fx-font-size: 14px;");
        browse.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            File f = fc.showOpenDialog(null);
            if (f != null) photoPath.setText(f.getAbsolutePath());
        });

        // HBox for photo row with equal spacing - 15px gap between elements
        HBox photoRow = new HBox(15);
        photoRow.setAlignment(Pos.CENTER);
        photoRow.setMaxWidth(elementWidth);
        photoRow.setPrefWidth(elementWidth);
        photoRow.getChildren().addAll(photoPath, browse);
        // Make photoPath field grow to fill available space
        HBox.setHgrow(photoPath, Priority.ALWAYS);

        Button submit = new Button("Submit Recycling");
        submit.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 5;");
        submit.setPrefWidth(elementWidth);
        submit.setPrefHeight(50);
        
        Label status = new Label();
        status.setAlignment(Pos.CENTER);
        status.setMaxWidth(elementWidth);
        status.setStyle("-fx-font-size: 16px;");

        submit.setOnAction(e -> {
            try {
                // Ensure the user exists before inserting to satisfy FK constraint
                if (userDao.findById(this.userId).isEmpty()) {
                    status.setText("Error: You're not logged in. Please log in or create an account first.");
                    status.setStyle("-fx-text-fill: #f44336; -fx-font-weight: bold; -fx-font-size: 16px;");
                    new Alert(Alert.AlertType.ERROR, "No account found for this session. Please go back and log in or create an account.").show();
                    return;
                }
                double w = Double.parseDouble(weight.getText().trim());
                RecycleEntry re = new RecycleEntry();
                re.setUserId(this.userId);
                re.setCategory(category.getValue());
                re.setWeightKg(w);
                re.setPhotoPath(photoPath.getText().trim());
                re.setCreatedAt(Instant.now().toEpochMilli());
                recycleDao.insert(re);

                int points = calculateEcoPoints(w);
                userDao.addPoints(this.userId, points);

                // Update UI to show current and total points
                User updatedUser = userDao.findById(this.userId).orElse(null);
                int totalPoints = (updatedUser != null) ? updatedUser.getPoints() : 0;

                status.setText(String.format("Saved! +%d eco points. Total points: %d", points, totalPoints));
                status.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold; -fx-font-size: 16px;");

                // Automatically award eligible coupons
                java.util.List<Coupon> newlyAwarded = couponDao.awardEligibleCoupons(this.userId, totalPoints);
                if (!newlyAwarded.isEmpty()) {
                    StringBuilder awardedMsg = new StringBuilder("Congratulations! You've earned new coupons:\n\n");
                    for (Coupon coupon : newlyAwarded) {
                        awardedMsg.append(String.format("%s - %s (%s)\n", 
                            coupon.getCompanyName(), coupon.getDescription(), coupon.getDiscount()));
                    }
                    
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("New Coupons Awarded!");
                    alert.setHeaderText("Great job! Keep recycling to earn more rewards!");
                    alert.setContentText(awardedMsg.toString());
                    alert.showAndWait();
                } else {
                    // Show next unlockable coupon if no new ones were awarded
                    Optional<Coupon> nextCoupon = couponDao.getNextUnlockableCoupon(totalPoints);
                    nextCoupon.ifPresent(coupon -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Next Goal");
                        alert.setHeaderText("You can unlock this coupon next! Keep recycling to earn it.");
                        alert.setContentText(String.format(
                            "Coupon: %s - %s (%s)",
                            coupon.getCompanyName(),
                            coupon.getDescription(),
                            coupon.getDiscount()
                        ));
                        alert.showAndWait();
                    });
                }

                weight.clear();
                photoPath.clear();
                reloadHistory();
            } catch (Exception ex) {
                status.setText("Error: " + ex.getMessage());
                status.setStyle("-fx-text-fill: #f44336; -fx-font-weight: bold; -fx-font-size: 16px;");
            }
        });
        
        formBox.getChildren().addAll(titleLabel, category, weight, photoRow, submit, status);
        mainContainer.getChildren().add(formBox);
        
        ScrollPane scrollPane = new ScrollPane(mainContainer);
        scrollPane.setStyle("-fx-background-color: transparent;");
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setPannable(false);
        return scrollPane;
    }

    private Parent buildHistoryTab() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-background-color: transparent;");

        historyTable = new TableView<>();
        TableColumn<RecycleEntry, String> catCol = new TableColumn<>("Category");
        catCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getCategory()));
        TableColumn<RecycleEntry, String> wCol = new TableColumn<>("Weight (kg)");
        wCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(String.valueOf(c.getValue().getWeightKg())));
        TableColumn<RecycleEntry, String> pCol = new TableColumn<>("Photo");
        pCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getPhotoPath() == null ? "" : c.getValue().getPhotoPath()));
        historyTable.getColumns().add(catCol);
        historyTable.getColumns().add(wCol);
        historyTable.getColumns().add(pCol);

        Button refresh = new Button("Refresh");
        refresh.setOnAction(e -> reloadHistory());
        refresh.fire();

        Button edit = new Button("Edit Selected");
        edit.setDisable(true);
        historyTable.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> edit.setDisable(n == null));
        edit.setOnAction(e -> {
            RecycleEntry selected = historyTable.getSelectionModel().getSelectedItem();
            if (selected == null) return;
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Edit Recycling Entry");
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);

            ComboBox<String> categoryField = new ComboBox<>();
            categoryField.getItems().addAll("Plastic", "Paper", "Glass", "Metal", "E-Waste", "Organic");
            categoryField.getSelectionModel().select(selected.getCategory());

            TextField weightField = new TextField(String.valueOf(selected.getWeightKg()));
            TextField photoField = new TextField(selected.getPhotoPath() == null ? "" : selected.getPhotoPath());

            GridPane grid = new GridPane();
            grid.setHgap(8);
            grid.setVgap(8);
            grid.addRow(0, new Label("Category"), categoryField);
            grid.addRow(1, new Label("Weight (kg)"), weightField);
            grid.addRow(2, new Label("Photo path"), photoField);
            dialog.getDialogPane().setContent(grid);

            dialog.setResultConverter(bt -> {
                if (bt == ButtonType.OK) {
                    try {
                        double newW = Double.parseDouble(weightField.getText().trim());
                        selected.setCategory(categoryField.getValue());
                        selected.setWeightKg(newW);
                        selected.setPhotoPath(photoField.getText().trim());
                        recycleDao.update(selected);
                        reloadHistory();
                    } catch (Exception ex) {
                        new Alert(Alert.AlertType.ERROR, "Invalid input: " + ex.getMessage()).showAndWait();
                    }
                }
                return null;
            });
            dialog.showAndWait();
        });

        // Create eco points panel components
        Label currentPointsLabel = new Label("Current Points: Select an entry to view");
        currentPointsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;");
        
        Label couponLabel = new Label("Select an entry to view Eco Points & Coupons.");
        couponLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #555555;");
        couponLabel.setWrapText(true);
        
        VBox ecoPointsPanel = buildEcoPointsPanel(currentPointsLabel, couponLabel);
        
        // Update panel when table selection changes
        historyTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            updateEcoPointsPanel(currentPointsLabel, couponLabel);
        });
        
        // Layout: table and panel side by side, or stacked if not enough space
        HBox mainContent = new HBox(15);
        mainContent.setAlignment(Pos.TOP_LEFT);
        VBox tableContainer = new VBox(5);
        tableContainer.setPadding(new Insets(10));
        tableContainer.setStyle("-fx-background-color: rgba(255,255,255,0.85); -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 8, 0, 0, 2);");
        tableContainer.getChildren().add(historyTable);
        mainContent.getChildren().add(tableContainer);
        mainContent.getChildren().add(ecoPointsPanel);
        
        // Make table grow to fill available space
        HBox.setHgrow(tableContainer, Priority.ALWAYS);
        historyTable.setMaxWidth(Double.MAX_VALUE);
        historyTable.setPrefWidth(600);
        
        // Set panel width
        ecoPointsPanel.setMinWidth(300);
        ecoPointsPanel.setMaxWidth(350);
        ecoPointsPanel.setPrefWidth(320);

        HBox actions = new HBox(8, refresh, edit);
        box.getChildren().addAll(new Label("History"), mainContent, actions);
        return box;
    }

    private Parent buildCouponsTab() {
        Label currentPointsLabel = new Label();
        currentPointsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;");

        Label couponLabel = new Label();
        couponLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #555555;");
        couponLabel.setWrapText(true);

        // Build the panel using the helper method
        VBox panel = buildEcoPointsPanel(currentPointsLabel, couponLabel);

        // Initialize the panel with the user's total points
        int userPoints = userDao.findById(this.userId).map(User::getPoints).orElse(0);
        currentPointsLabel.setText("Your Total Points: " + userPoints);
        couponLabel.setText("🎫 Next Coupon: " + getCouponSuggestion(userPoints));

        return panel;
    }

    private VBox buildEcoPointsPanel(Label currentPointsLabel, Label couponLabel) {
        VBox panel = new VBox(12);
        panel.setPadding(new Insets(16));
        panel.setStyle("-fx-background-color: rgba(255,255,255,0.85); -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 8, 0, 0, 2);");
        
        Label title = new Label("Eco Points Summary");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2e7d32;");
        
        Button redeemBtn = new Button("Redeem Now");
        redeemBtn.setStyle(
            "-fx-font-size: 14px; " +
            "-fx-font-weight: bold; " +
            "-fx-background-color: #4CAF50; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 8px; " +
            "-fx-padding: 10px 20px;"
        );
        redeemBtn.setOnAction(e -> handleRedeemCoupon(currentPointsLabel, couponLabel));
        
        Button viewAllBtn = new Button("View All Coupons");
        viewAllBtn.setStyle(
            "-fx-font-size: 12px; " +
            "-fx-text-fill: #4CAF50; " +
            "-fx-background-color: transparent; " +
            "-fx-underline: true; " +
            "-fx-padding: 5px 0px;"
        );
        viewAllBtn.setOnAction(e -> showAllCouponsDialog());
        
        panel.getChildren().addAll(title, currentPointsLabel, couponLabel, redeemBtn, viewAllBtn);
        return panel;
    }

    private void handleRedeemCoupon(Label currentPointsLabel, Label couponLabel) {
        // Use the user's total points for redemption logic
        int totalPoints = userDao.findById(this.userId).map(User::getPoints).orElse(0);

        // Check if user has enough points
        if (totalPoints < 20) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Insufficient Points");
            alert.setHeaderText(null);
            alert.setContentText("You need at least 20 points to redeem a coupon. Keep recycling to earn more points!");
            alert.showAndWait();
            return;
        }
        
        // Get coupon for these points
        Optional<Coupon> couponOpt = couponDao.getCouponByPoints(totalPoints);
        if (couponOpt.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Could not find a coupon for your points.");
            alert.showAndWait();
            return;
        }
        
        Coupon coupon = couponOpt.get();
        
        // Check if already redeemed
        if (couponDao.hasUserRedeemedCoupon(this.userId, coupon.getCouponCode())) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Already Redeemed");
            alert.setHeaderText(null);
            alert.setContentText("You have already redeemed this coupon. Code: " + coupon.getCouponCode());
            alert.showAndWait();
            return;
        }
        
        // Confirmation dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Redemption");
        confirmAlert.setHeaderText("Redeem Coupon?");
        confirmAlert.setContentText(
            "Coupon: " + coupon.getDescription() + "\n" +
            "Discount: " + coupon.getDiscount() + "\n" +
            "Points Required: " + totalPoints + "\n\n" +
            "Do you want to redeem this coupon?"
        );
        
        confirmAlert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                try {
                    // Redeem the coupon
                    couponDao.redeemCouponForUser(this.userId, coupon);
                    
                    // Add to user's coupon list
                    userDao.findById(this.userId).ifPresent(user -> {
                        user.addCoupon(coupon);
                    });
                    
                    // Show success dialog with coupon code
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Coupon Redeemed!");
                    successAlert.setHeaderText("🎉 Congratulations!");
                    successAlert.setContentText(
                        "Your coupon has been redeemed successfully!\n\n" +
                        "Coupon: " + coupon.getDescription() + "\n" +
                        "Coupon Code: " + coupon.getCouponCode() + "\n" +
                        "Discount: " + coupon.getDiscount() + "\n" +
                        (coupon.getExpiryDate() != null ? "Expires: " + coupon.getExpiryDate() + "\n" : "") +
                        "\nSave this code to use at checkout!"
                    );
                    successAlert.showAndWait();
                    
                    // Update the panel
                    updateEcoPointsPanel(currentPointsLabel, couponLabel);
                } catch (Exception ex) {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Error");
                    errorAlert.setHeaderText(null);
                    errorAlert.setContentText("Failed to redeem coupon: " + ex.getMessage());
                    errorAlert.showAndWait();
                }
            }
        });
    }

    private void showAllCouponsDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("All Available Coupons");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        
        ScrollPane scrollPane = new ScrollPane();
        VBox contentBox = new VBox(10);
        contentBox.setPadding(new Insets(15));
        contentBox.setStyle("-fx-background-color: white;");
        
        Label header = new Label("Available Coupons");
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2e7d32;");
        contentBox.getChildren().add(header);
        
        // Get user's current points
        int userPoints = userDao.findById(this.userId).map(User::getPoints).orElse(0);
        
        // Show coupon tiers
        String[] couponTiers = {
            "5% off Recycled Stationery - 20 points",
            "10% off Eco Toiletries - 40 points",
            "15% off Recycled T-shirt - 70 points",
            "Free Eco Tote Bag - 100 points",
            "20% off LED Bulb - 150 points",
            "Plant a Tree Certificate - 250 points",
            "30% off on eco products - 400+ points"
        };
        
        for (String tier : couponTiers) {
            Label tierLabel = new Label(tier);
            tierLabel.setStyle("-fx-font-size: 14px; -fx-padding: 5px;");
            contentBox.getChildren().add(tierLabel);
        }
        
        Label currentPointsLabel = new Label("\nYour Current Total Points: " + userPoints);
        currentPointsLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #4CAF50; -fx-padding: 10px 0px;");
        contentBox.getChildren().add(currentPointsLabel);
        
        if (userPoints >= 20) {
            String suggestion = getCouponSuggestion(userPoints);
            Label suggestionLabel = new Label("You can redeem: " + suggestion);
            suggestionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2e7d32; -fx-padding: 5px;");
            contentBox.getChildren().add(suggestionLabel);
        } else {
            Label suggestionLabel = new Label("Keep recycling to earn your first coupon at 20 points!");
            suggestionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666; -fx-padding: 5px;");
            contentBox.getChildren().add(suggestionLabel);
        }
        
        scrollPane.setContent(contentBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);
        dialog.getDialogPane().setContent(scrollPane);
        dialog.getDialogPane().setPrefWidth(500);
        dialog.showAndWait();
    }

    private void updateEcoPointsPanel(Label currentPointsLabel, Label couponLabel) {
        var selectedItems = historyTable.getSelectionModel().getSelectedItems();
        
        if (selectedItems.isEmpty()) {
            currentPointsLabel.setText("Current Points: Select an entry to view");
            couponLabel.setText("Select an entry to view Eco Points & Coupons.");
            return;
        }
        
        // Calculate total points from all selected items
        int totalPoints = 0;
        for (RecycleEntry entry : selectedItems) {
            totalPoints += calculateEcoPoints(entry.getWeightKg());
        }
        
        currentPointsLabel.setText("Current Points: " + totalPoints);
        couponLabel.setText("🎫 Available Coupon: " + getCouponSuggestion(totalPoints));
    }
    
    private String getCouponSuggestion(int points) {
        if (points >= 400) return "30% off on eco products";
        else if (points >= 250) return "Plant a Tree Certificate";
        else if (points >= 150) return "20% off LED Bulb";
        else if (points >= 100) return "Free Eco Tote Bag";
        else if (points >= 70) return "15% off Recycled T-shirt";
        else if (points >= 40) return "10% off Eco Toiletries";
        else if (points >= 20) return "5% off Recycled Stationery";
        else return "Keep recycling to earn your first coupon at 20 points!";
    }

    private Parent buildTradeTab() {
        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(4));
        pane.setStyle("-fx-background-color: transparent;");
        WebView webView = new WebView();
        webView.getEngine().load("https://scrapuncle.com");
        pane.setCenter(webView);
        return pane;
    }

    private Parent buildRulebookTab() {
        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(4));
        pane.setStyle("-fx-background-color: transparent;");
        
        // Center area for PDF pages
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        scrollPane.setBackground(Background.EMPTY);
        
        VBox contentBox = new VBox(10);
        contentBox.setPadding(new Insets(10));
        contentBox.setAlignment(Pos.TOP_CENTER);
        contentBox.setStyle("-fx-background-color: transparent;");
        scrollPane.setContent(contentBox);
        
        // Loading indicator
        ProgressIndicator loadingIndicator = new ProgressIndicator();
        loadingIndicator.setStyle("-fx-progress-color: #4CAF50;");
        Label loadingLabel = new Label("Loading PDF...");
        loadingLabel.setStyle("-fx-text-fill: #333333; -fx-font-size: 16px;");
        VBox loadingBox = new VBox(10, loadingIndicator, loadingLabel);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setPadding(new Insets(50));
        loadingBox.setStyle("-fx-background-color: transparent;");
        
        pane.setCenter(loadingBox);
        
        // Load PDF in background
        Task<VBox> loadPdfTask = new Task<VBox>() {
            @Override
            protected VBox call() throws Exception {
                try (InputStream pdfStream = getClass().getResourceAsStream("/images/rulebook.pdf")) {
                    if (pdfStream == null) {
                        throw new Exception("PDF file not found in resources");
                    }
                    
                    // Read PDF into byte array
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    byte[] data = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = pdfStream.read(data, 0, data.length)) != -1) {
                        buffer.write(data, 0, bytesRead);
                    }
                    byte[] pdfBytes = buffer.toByteArray();
                    
                    try (PDDocument document = Loader.loadPDF(pdfBytes)) {
                        PDFRenderer pdfRenderer = new PDFRenderer(document);
                        int pageCount = document.getNumberOfPages();
                        
                        VBox pagesBox = new VBox(10);
                        pagesBox.setAlignment(Pos.TOP_CENTER);
                        pagesBox.setPadding(new Insets(10));
                        
                        // Render all pages
                        for (int pageIndex = 0; pageIndex < pageCount; pageIndex++) {
                            BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(pageIndex, 150);
                            WritableImage fxImage = SwingFXUtils.toFXImage(bufferedImage, null);
                            ImageView imageView = new ImageView(fxImage);
                            imageView.setPreserveRatio(true);
                            imageView.setFitWidth(800); // Set max width
                            pagesBox.getChildren().add(imageView);
                        }
                        
                        return pagesBox;
                    }
                }
            }
        };
        
        loadPdfTask.setOnSucceeded(e -> {
            try {
                VBox pagesBox = loadPdfTask.getValue();
                contentBox.getChildren().setAll(pagesBox.getChildren());
                scrollPane.setContent(contentBox);
                pane.setCenter(scrollPane);
            } catch (Exception ex) {
                VBox errorBox = new VBox(10);
                errorBox.setAlignment(Pos.CENTER);
                errorBox.setPadding(new Insets(20));
                errorBox.setStyle("-fx-background-color: transparent;");
                Label errorLabel = new Label("Error displaying PDF: " + ex.getMessage());
                errorLabel.setStyle("-fx-text-fill: #333333;");
                errorBox.getChildren().add(errorLabel);
                pane.setCenter(errorBox);
            }
        });
        
        loadPdfTask.setOnFailed(e -> {
            Throwable ex = loadPdfTask.getException();
            VBox errorBox = new VBox(10);
            errorBox.setAlignment(Pos.CENTER);
            errorBox.setPadding(new Insets(20));
            errorBox.setStyle("-fx-background-color: transparent;");
            Label errorLabel1 = new Label("Error loading PDF: " + (ex != null ? ex.getMessage() : "Unknown error"));
            errorLabel1.setStyle("-fx-text-fill: #333333;");
            Label errorLabel2 = new Label("Please ensure rulebook.pdf is in src/main/resources/images/");
            errorLabel2.setStyle("-fx-text-fill: #333333;");
            errorBox.getChildren().addAll(errorLabel1, errorLabel2);
            pane.setCenter(errorBox);
        });
        
        new Thread(loadPdfTask).start();
        
        return pane;
    }

    private Parent buildSpotsTab(SpotDao dao) {
        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(4));
        pane.setStyle("-fx-background-color: transparent;");
        WebView centres = new WebView();
        var webEngine = centres.getEngine();
        webEngine.load("https://www.karosambhav.com/collection-centres");
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                webEngine.executeScript("window.scrollTo(0, 1000);");
            }
        });
        pane.setCenter(centres);
        return pane;
    }

    public Parent getRoot() { return root; }

    private void reloadHistory() {
        if (historyTable != null) {
            historyTable.getItems().setAll(recycleDao.listByUser(this.userId));
        }
    }

    @SuppressWarnings("unused")
    private String determineLevel(int points) {
        if (points >= 1000) return "Green Hero";
        if (points >= 500) return "Gold";
        if (points >= 250) return "Silver";
        if (points >= 100) return "Bronze";
        return "";
    }
}
