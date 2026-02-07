package com.recyclingquest.ui;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import java.util.Objects;

public class StartScreen {
    private final StackPane root = new StackPane();

    public StartScreen(Runnable onLogin, Runnable onCreate) {
        root.getStyleClass().add("start-screen");
        
        // Set background image
        try {
            Image bgImage = new Image(Objects.requireNonNull(getClass().getResource("/images/bg1.png")).toExternalForm());
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

        Button login = new Button("Login");
        login.getStyleClass().add("btn-login");
        login.setOnAction(e -> onLogin.run());
        Button create = new Button("Create Account");
        create.getStyleClass().add("btn-create");
        create.setOnAction(e -> onCreate.run());
        VBox actions = new VBox(12, login, create);
        actions.setAlignment(Pos.CENTER);
        StackPane.setAlignment(actions, Pos.CENTER);
        root.getChildren().add(actions);
    }

    public Parent getRoot() { 
        return root; 
    }
}

