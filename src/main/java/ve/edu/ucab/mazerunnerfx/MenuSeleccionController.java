package ve.edu.ucab.mazerunnerfx;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class MenuSeleccionController {

    @FXML
    @SuppressWarnings("unused")
    private Label usuarioEmail;

    private String usuarioCorreo;

    // Called after FXML elements are injected
    @FXML
    protected void initialize() {
        if (usuarioEmail != null && usuarioCorreo != null) {
            usuarioEmail.setText(usuarioCorreo);
        }
    }

    // Setter to be called by the previous controller when switching scenes
    public void setUsuario(String correo) {
        this.usuarioCorreo = correo;
        if (usuarioEmail != null && usuarioCorreo != null) {
            usuarioEmail.setText(usuarioCorreo);
        }
    }

    // Placeholder event handlers for the buttons referenced in the FXML
    @FXML
    protected void onNuevaPartida(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("eleccion-laberinto.fxml"));
            Parent root = loader.load();

            EleccionLaberintoController controller = loader.getController();
            if (controller != null && usuarioCorreo != null) {
                controller.setUsuario(usuarioCorreo);
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setContentText("No se pudo abrir la selección de laberinto: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    protected void onCargarPartida() {
        // TODO: Implement load game logic
    }

    @FXML
    protected void onEstadisticas() {
        // TODO: Implement statistics logic
    }

    @FXML
    protected void onSalir(ActionEvent event) {
        // Close the current window and exit the JavaFX application
        Stage stage = null;
        if (event != null && event.getSource() instanceof Node) {
            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.close();
        }
        javafx.application.Platform.exit();
    }
}
