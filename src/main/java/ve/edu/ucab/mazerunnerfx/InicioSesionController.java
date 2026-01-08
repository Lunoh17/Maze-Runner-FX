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

    @FXML
    protected void onRegresoBoton(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("hello-view.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

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

        // success
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Inicio de Sesión");
        alert.setHeaderText(null);
        alert.setContentText("Inicio de sesión exitoso. ¡Bienvenido al Maze Runner!");
        alert.showAndWait();
    }

    private void showAlert(AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
