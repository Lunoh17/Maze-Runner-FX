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
import javafx.stage.Screen;
import ve.edu.ucab.mazerunnerfx.models.Laberinto;

/**
 * Controlador del menú de selección de la aplicación.
 *
 * <p>Gestiona la navegación desde el menú principal hacia las pantallas de
 * selección de laberinto, estadísticas y carga/guardado de partidas.</p>
 *
 * @author Equipo
 * @version 2026-01-12
 */
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
    protected void onCargarPartida(ActionEvent event) {
        // Use the saved file for the current user
        String correo = (usuarioCorreo == null || usuarioCorreo.isEmpty()) ? "default" : usuarioCorreo;
        Laberinto lab = Laberinto.cargarJson(correo);
        if (lab == null) {
            Alert a = new Alert(AlertType.INFORMATION);
            a.setHeaderText(null);
            a.setContentText("No se encontró una partida guardada para el usuario: " + correo);
            a.showAndWait();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("laberinto.fxml"));
            Parent root = loader.load();
            LaberintoController controller = loader.getController();
            if (controller != null) {
                controller.setLaberinto(lab);
                // restore the timer value stored in the model if any
                // lab.tiempoSegundos is now part of Laberinto and will be applied by controller.setLaberinto
            }

            // Compute a larger window size based on maze dimensions so it's easier to see
            double cellPixels = 14.0; // target pixels per cell
            double marginW = 120.0; // extra width for HUD and padding
            double marginH = 160.0; // extra height for HUD and padding
            double desiredW = Math.max(800, lab.getWidth() * cellPixels + marginW);
            double desiredH = Math.max(600, lab.getHeight() * cellPixels + marginH);

            // Clamp to available screen bounds
            javafx.geometry.Rectangle2D vb = Screen.getPrimary().getVisualBounds();
            desiredW = Math.min(desiredW, Math.max(400, vb.getWidth() - 80));
            desiredH = Math.min(desiredH, Math.max(300, vb.getHeight() - 120));

            if (controller != null) controller.setWindowSize(desiredW, desiredH);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, desiredW, desiredH));
            stage.show();

            // No background game loop started here; UI controller drives the interactions

        } catch (IOException e) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setContentText("No se pudo abrir la vista de juego: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    protected void onEstadisticas() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("estadisticas.fxml"));
            Parent root = loader.load();
            EstadisticasController c = loader.getController();
            if (c != null) c.setUsuario(this.usuarioCorreo);
            // obtain current stage from any node via usuarioEmail if available
            Stage stage = null;
            if (usuarioEmail != null) {
                stage = (Stage) usuarioEmail.getScene().getWindow();
            }
            if (stage == null) {
                // fallback to primary stage
                stage = (Stage) javafx.stage.Stage.getWindows().filtered(javafx.stage.Window::isShowing).get(0);
            }
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
