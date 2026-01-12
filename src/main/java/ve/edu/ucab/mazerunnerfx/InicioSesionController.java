package ve.edu.ucab.mazerunnerfx;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import ve.edu.ucab.mazerunnerfx.models.Usuario;
import ve.edu.ucab.mazerunnerfx.models.Contrasenia;
import ve.edu.ucab.mazerunnerfx.models.CompararDatos;

/**
 * Controlador de la pantalla de inicio de sesión.
 *
 * <p>Valida campos, muestra mensajes de error en línea y navega a la vista del laberinto
 * cuando las credenciales son correctas.</p>
 *
 * @author Equipo
 * @version 2026-01-12
 */
public class InicioSesionController {

    @FXML
    private TextField email;

    @FXML
    private PasswordField password;

    @FXML
    private Button inicioSesionMaze;

    // inline error labels
    @FXML
    private Label emailError;

    @FXML
    private Label passwordError;

    private boolean passwordTouched = false;

    /**
     * Inicializa el controlador: limpia etiquetas de error, deshabilita el botón de inicio
     * y añade listeners para validación en línea sobre los campos de correo y contraseña.
     *
     * <p>Se llama automáticamente tras cargar el FXML.</p>
     *
     * @since 2026-01-12
     */
    @FXML
    protected void initialize() {
        if (emailError != null) emailError.setText("");
        if (passwordError != null) passwordError.setText("");
        if (inicioSesionMaze != null) inicioSesionMaze.setDisable(true);

        ChangeListener<String> fieldListener = new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> obs, String oldVal, String newVal) {
                updateInlineValidation();
            }
        };

        if (email != null) email.textProperty().addListener(fieldListener);
        if (password != null) password.textProperty().addListener(fieldListener);

        if (password != null) {
            password.focusedProperty().addListener((obs, was, isNow) -> {
                if (isNow) passwordTouched = true;
                else updateInlineValidation();
            });
        }
    }

    /**
     * Actualiza la validación en línea de los campos de correo y contraseña.
     *
     * <p>Valida presencia y formato del correo, comprueba si el correo está registrado
     * y muestra mensajes de error junto a los campos. También controla el estado del
     * botón de inicio de sesión (habilitado/deshabilitado) según las comprobaciones básicas.</p>
     *
     * <p>Este método no realiza cambios en el modelo de usuario persistente, sólo actualiza
     * la interfaz y prepara el estado para una posible autenticación.</p>
     *
     * @since 2026-01-12
     */
    private void updateInlineValidation() {
        String emailText = (email != null ? email.getText().trim() : "");
        String pass = (password != null ? password.getText() : "");

        if (emailError != null) emailError.setText("");
        if (passwordError != null) passwordError.setText("");

        boolean emailPresent = !emailText.isEmpty();
        boolean passPresent = !pass.isEmpty();

        if (!emailPresent) {
            if (emailError != null) emailError.setText("El correo es obligatorio");
        } else {
            Usuario u = new Usuario();
            String normalized = u.UsuaValidacion(emailText);
            if (normalized == null) {
                if (emailError != null) emailError.setText("Correo no válido");
            } else {
                // check if user exists
                CompararDatos comparador = new CompararDatos(new Usuario(normalized));
                if (!comparador.EnviarDatosRegistro()) {
                    if (emailError != null) emailError.setText("Correo no registrado");
                }
            }
        }

        if (!passPresent) {
            if (passwordTouched) {
                if (passwordError != null) passwordError.setText("La contraseña es obligatoria");
            }
        }

        // Enable button only when basic presence is satisfied (email valid and password present)
        boolean canEnable = emailPresent && passPresent && (emailError == null || emailError.getText().isEmpty());
        if (inicioSesionMaze != null) inicioSesionMaze.setDisable(!canEnable);
    }

    /**
     * Maneja el evento de volver al menú principal.
     *
     * <p>Carga la vista {@code hello-view.fxml} y la muestra en la misma ventana.</p>
     *
     * @param event evento generado por la interacción del usuario (presionar el botón de regreso)
     * @throws IOException si no se puede cargar la vista FXML
     * @since 2026-01-12
     */
    @FXML
    protected void onRegresoBoton(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("hello-view.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    /**
     * Maneja el intento de inicio de sesión cuando el usuario pulsa el botón correspondiente.
     *
     * <p>Realiza validaciones finales del correo y la contraseña, muestra alertas en caso de
     * error y, si las credenciales son correctas, navega a {@code menu-seleccion.fxml} pasando
     * el correo normalizado al controlador siguiente.</p>
     *
     * @param event evento asociado al botón de inicio de sesión
     * @since 2026-01-12
     */
    @FXML
    public void onInicioSesionMaze(ActionEvent event) {
        // final validation before allowing login
        String emailText = (email != null ? email.getText().trim() : "");
        String pass = (password != null ? password.getText() : "");

        if (emailText.isEmpty()) {
            if (emailError != null) emailError.setText("El correo es obligatorio");
            showAlert(AlertType.ERROR, "El correo es obligatorio");
            return;
        }

        Usuario u = new Usuario();
        String normalized = u.UsuaValidacion(emailText);
        if (normalized == null) {
            if (emailError != null) emailError.setText("Correo no válido");
            showAlert(AlertType.ERROR, "Correo no válido");
            return;
        }

        CompararDatos comparador = new CompararDatos(new Usuario(normalized));
        if (!comparador.EnviarDatosRegistro()) {
            if (emailError != null) emailError.setText("Correo no registrado");
            showAlert(AlertType.ERROR, "Correo no registrado. Regístrate primero.");
            return;
        }

        // check password correctness
        Contrasenia contr = new Contrasenia();
        contr.setContrasenia(pass); // do not run validation here; we need raw password to compare
        CompararDatos sesion = new CompararDatos(new Usuario(normalized), contr);
        if (!sesion.EnviarDatosSesion()) {
            if (passwordError != null) passwordError.setText("Contraseña incorrecta");
            showAlert(AlertType.ERROR, "Contraseña incorrecta");
            return;
        }

        // success: show info then navigate to menu-seleccion.fxml
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Inicio de Sesión");
        alert.setHeaderText(null);
        alert.setContentText("Inicio de sesión exitoso. ¡Bienvenido al Maze Runner!");
        alert.showAndWait();

        try {
            // load FXML with FXMLLoader so we can access its controller and pass the Usuario
            FXMLLoader loader = new FXMLLoader(getClass().getResource("menu-seleccion.fxml"));
            Parent root = loader.load();

            // pass normalized email string to controller
            ve.edu.ucab.mazerunnerfx.MenuSeleccionController controller = loader.getController();
            controller.setUsuario(normalized);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert(AlertType.ERROR, "No se pudo abrir el menú: " + e.getMessage());
        }
    }

    /**
     * Muestra una alerta modal con el tipo y mensaje especificados.
     *
     * @param type  tipo de alerta (ERROR, INFORMATION, etc.)
     * @param message texto a mostrar en la alerta
     * @since 2026-01-12
     */
    private void showAlert(AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
