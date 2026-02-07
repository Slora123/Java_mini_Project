package com.recyclingquest;
import com.recyclingquest.db.Database;
import com.recyclingquest.db.Migrations;
import com.recyclingquest.ui.AuthScreen;
import com.recyclingquest.ui.DashboardScreen;
import com.recyclingquest.ui.StartScreen;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    private Stage primaryStage;
    private Database database;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;

        this.database = new Database();
        Migrations.run(database);

        showStart();
        this.primaryStage.show();
    }

    private void showStart() {
        StartScreen screen = new StartScreen(
                () -> showAuth(com.recyclingquest.ui.AuthScreen.Mode.LOGIN),
                () -> showAuth(com.recyclingquest.ui.AuthScreen.Mode.CREATE)
        );
        Scene scene = new Scene(screen.getRoot(), 900, 600);
        scene.getStylesheets().add(getClass().getResource("/styles/app.css").toExternalForm());
        primaryStage.setScene(scene);
    }

    private void showAuth(com.recyclingquest.ui.AuthScreen.Mode mode) {
        AuthScreen auth = new AuthScreen(database, mode, user -> showDashboard(user.getId()), this::showStart);
        Scene scene = new Scene(auth.getRoot(), 900, 600);
        scene.getStylesheets().add(getClass().getResource("/styles/app.css").toExternalForm());
        primaryStage.setScene(scene);
    }

    private void showDashboard(long userId) {
        DashboardScreen dash = new DashboardScreen(database, userId, this::showStart);
        Scene scene = new Scene(dash.getRoot(), 1000, 700);
        scene.getStylesheets().add(getClass().getResource("/styles/app.css").toExternalForm());
        primaryStage.setScene(scene);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
