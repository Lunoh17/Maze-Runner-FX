package ve.edu.ucab.mazerunnerfx;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * Controlador principal de la vista de inicio (pantalla de bienvenida).
 *
 * <p>Proporciona acciones para navegar a las pantallas de registro e inicio de sesión.</p>
 *
 * @author Equipo
 * @version 2026-01-12
 */
public class HelloController {

    @FXML
    protected void onRegistroUsuario(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("registro-usuario.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    protected void onInicio(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("inicio-sesion.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}
