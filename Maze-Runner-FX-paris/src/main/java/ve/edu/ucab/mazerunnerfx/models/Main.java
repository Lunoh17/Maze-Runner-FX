package ve.edu.ucab.mazerunnerfx.models;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/ve/edu/ucab/mazerunnerfx/panel-principal.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Maze Runner");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}