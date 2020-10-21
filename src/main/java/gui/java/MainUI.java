package gui.java;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainUI extends Application {
    public static void startUI() {
        launch();
    }

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/MainUI.fxml"));
        Scene scene = new Scene(root, 1000, 1000);
        stage.setTitle("Visual A*");
        stage.setScene(scene);
        stage.show();
    }
}
