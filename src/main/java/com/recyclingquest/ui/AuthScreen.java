package com.recyclingquest.ui;
import com.recyclingquest.dao.UserDao;
import com.recyclingquest.db.Database;
import com.recyclingquest.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import java.util.Objects;
import java.util.function.Consumer;
public class AuthScreen {
    private final StackPane root = new StackPane();
    public enum Mode { LOGIN, CREATE }
    public AuthScreen(Database db, Mode mode, Consumer<User> onLogin, Runnable onBack) {
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
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(10));
        form.setAlignment(Pos.CENTER);
        form.setPrefWidth(560);
        form.setMaxWidth(560);
        form.setPrefHeight(Region.USE_COMPUTED_SIZE);
        form.setMaxHeight(Region.USE_PREF_SIZE);
        form.setStyle("-fx-background-color: rgba(255,255,255,0.85); -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 8, 0, 0, 2);");
        StackPane.setAlignment(form, Pos.CENTER);

        UserDao userDao = new UserDao(db);

        Label heading = new Label(mode == Mode.LOGIN ? "Login" : "Create Your Eco-Hero");
        heading.getStyleClass().add("heading");

        // Common controls
        Button primary = new Button(mode == Mode.LOGIN ? "Login" : "Create Account");
        Button back = new Button("Back");
        Label info = new Label("Welcome, Eco-Hero! Your mission is to clean your city by recycling.");
        Label status = new Label();

        primary.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6;");
        primary.setDefaultButton(true);

        // Layout rows
        int r = 0;
        HBox topBar = new HBox(10, back, heading);
        form.add(topBar, 0, r++, 2, 1);

        if (mode == Mode.LOGIN) {
            Label emailL = new Label("Email or Nickname");
            TextField email = new TextField();
            Label passL = new Label("Password");
            PasswordField pass = new PasswordField();
            

            primary.setOnAction(e -> {
                String em = email.getText().trim();
                String pw = pass.getText();
                System.out.println("Attempting login with email/nickname: " + em);
                if (em.isEmpty() || pw.isEmpty()) {
                    status.setText("Email/Nickname and password required");
                    return;
                }

                // Try email first; if not found, fallback to nickname for legacy accounts
                var existing = userDao.findByEmail(em);
                if (existing.isEmpty()) {
                    System.out.println("No user found with email: " + em + ". Trying nickname.");
                    existing = userDao.findByNickname(em);
                } else {
                    System.out.println("User found with email: " + em);
                }

                if (existing.isPresent()) {
                    System.out.println("User found. Checking password.");
                    if (pw.equals(existing.get().getPassword())) {
                        System.out.println("Password matches. Logging in.");
                        status.setText("");
                        onLogin.accept(existing.get());
                    } else {
                        System.out.println("Invalid credentials for user: " + em);
                        status.setText("Invalid credentials");
                        Alert err = new Alert(Alert.AlertType.ERROR);
                        err.setTitle("Login Failed");
                        err.setHeaderText(null);
                        err.setContentText("Incorrect password. Please try again.");
                        err.show();
                    }
                } else {
                    System.out.println("No user found for: " + em);
                    status.setText("Account not found. Try your nickname or create one.");
                    Alert err = new Alert(Alert.AlertType.WARNING);
                    err.setTitle("Account Not Found");
                    err.setHeaderText(null);
                    err.setContentText("We couldn't find that account. Try your nickname or create one.");
                    err.show();
                }
            });

            form.add(emailL, 0, r); form.add(email, 1, r++);
            form.add(passL, 0, r); form.add(pass, 1, r++);
            form.add(primary, 0, r++, 2, 1);
            form.add(info, 0, r++, 2, 1);
            form.add(status, 0, r, 2, 1);
        } else { // CREATE
            Label nickL = new Label("Nickname");
            TextField nick = new TextField();
            Label cityL = new Label("City");
            TextField city = new TextField();
            Label avatarL = new Label("Avatar");
            ComboBox<String> avatar = new ComboBox<>();
            avatar.getItems().addAll("EcoKnight", "EcoBlade", "Captain Compost", "BioBella", "GreenSiren");
            avatar.getSelectionModel().selectFirst();
            Label emailL = new Label("Email");
            TextField email = new TextField();
            Label passL = new Label("Password");
            PasswordField pass = new PasswordField();

            primary.setOnAction(e -> {
                String n = nick.getText().trim();
                String c = city.getText().trim();
                String a = avatar.getValue();
                String em = email.getText().trim();
                String pw = pass.getText();
                if (n.isEmpty() || em.isEmpty() || pw.isEmpty()) { 
                    status.setText("Nickname, email, password required"); 
                    Alert err = new Alert(Alert.AlertType.WARNING);
                    err.setTitle("Missing Fields");
                    err.setHeaderText(null);
                    err.setContentText("Please fill nickname, email and password.");
                    err.show();
                    return; 
                }
                if (pw.length() < 4) { 
                    status.setText("Password must be at least 4 characters"); 
                    Alert err = new Alert(Alert.AlertType.WARNING);
                    err.setTitle("Weak Password");
                    err.setHeaderText(null);
                    err.setContentText("Password must be at least 4 characters.");
                    err.show();
                    return; 
                }
                if (userDao.findByNickname(n).isPresent()) { 
                    status.setText("Nickname already exists"); 
                    Alert err = new Alert(Alert.AlertType.ERROR);
                    err.setTitle("Nickname Taken");
                    err.setHeaderText(null);
                    err.setContentText("Please choose a different nickname.");
                    err.show();
                    return; 
                }
                if (userDao.findByEmail(em).isPresent()) { 
                    status.setText("Email already in use"); 
                    Alert err = new Alert(Alert.AlertType.ERROR);
                    err.setTitle("Email In Use");
                    err.setHeaderText(null);
                    err.setContentText("That email is already registered. Try logging in.");
                    err.show();
                    return; 
                }
                try {
                    System.out.println("Creating user: nick=" + n + ", city=" + c + ", avatar=" + a + ", email=" + em);
                    User user = userDao.create(n, a, c, em, pw);
                    status.setText("");
                    onLogin.accept(user);
                } catch (RuntimeException ex) {
                    status.setText("Failed to create account: " + ex.getMessage());
                    Alert err = new Alert(Alert.AlertType.ERROR);
                    err.setTitle("Create Account Failed");
                    err.setHeaderText(null);
                    err.setContentText("Could not create account. " + ex.getMessage());
                    err.show();
                }
            });

            form.add(nickL, 0, r); form.add(nick, 1, r++);
            form.add(cityL, 0, r); form.add(city, 1, r++);
            form.add(avatarL, 0, r); form.add(avatar, 1, r++);
            form.add(emailL, 0, r); form.add(email, 1, r++);
            form.add(passL, 0, r); form.add(pass, 1, r++);
            form.add(primary, 0, r++, 2, 1);
            form.add(info, 0, r++, 2, 1);
            form.add(status, 0, r, 2, 1);
        }

        back.setOnAction(e -> {
            if (onBack != null) onBack.run();
        });

        root.getChildren().add(form);
    }

    public Parent getRoot() { return root; }
}
