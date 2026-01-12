package ve.edu.ucab.mazerunnerfx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Punto de entrada JavaFX de la aplicación.
 *
 * <p>Inicia la aplicación y carga la vista principal.</p>
 *
 * @author Equipo
 * @version 2026-01-12
 */
public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 541, 400);
        stage.setTitle("Sign In");
        stage.setScene(scene);
        stage.show();
    }
}
