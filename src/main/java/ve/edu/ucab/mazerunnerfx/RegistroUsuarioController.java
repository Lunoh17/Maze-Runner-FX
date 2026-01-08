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

public class RegistroUsuarioController {

    @FXML
    private TextField email;

    @FXML
    private PasswordField password;

    @FXML
    private PasswordField confirmPassword;

    @FXML
    private Button registroButton;

    @FXML
    private Button regreso; // linked to fx:id in FXML

    // New inline error labels
    @FXML
    private Label emailError;

    @FXML
    private Label passwordError;

    @FXML
    private Label confirmPasswordError;

    // --- ADDED: touched flags to avoid showing password errors before interaction
    private boolean passwordTouched = false;
    private boolean confirmTouched = false;
    // --- end added

    @FXML
    protected void initialize() {
        // Start with empty error labels
        if (emailError != null) emailError.setText("");
        if (passwordError != null) passwordError.setText("");
        if (confirmPasswordError != null) confirmPasswordError.setText("");

        // Disable register button until basic fields are present
        if (registroButton != null) registroButton.setDisable(true);

        ChangeListener<String> fieldListener = new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> obs, String oldVal, String newVal) {
                updateInlineValidation();
            }
        };

        if (email != null) email.textProperty().addListener(fieldListener);
        if (password != null) password.textProperty().addListener(fieldListener);
        if (confirmPassword != null) confirmPassword.textProperty().addListener(fieldListener);

        // --- ADDED: focus listeners to set touched flags and trigger validation on focus loss
        if (password != null) {
            password.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                if (isNowFocused) {
                    passwordTouched = true;
                } else {
                    // validate after user leaves the field
                    updateInlineValidation();
                }
            });
        }

        if (confirmPassword != null) {
            confirmPassword.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                if (isNowFocused) {
                    confirmTouched = true;
                } else {
                    // validate after user leaves the field
                    updateInlineValidation();
                }
            });
        }
        // --- end added
    }

    private void updateInlineValidation() {
        String emailText = (email != null ? email.getText().trim() : "");
        String pass = (password != null ? password.getText() : "");
        String conf = (confirmPassword != null ? confirmPassword.getText() : "");

        // Clear inline messages by default
        if (emailError != null) emailError.setText("");
        if (passwordError != null) passwordError.setText("");
        if (confirmPasswordError != null) confirmPasswordError.setText("");

        // Basic presence checks
        boolean emailPresent = !emailText.isEmpty();
        boolean passPresent = !pass.isEmpty();
        boolean confPresent = !conf.isEmpty();

        // Show simple inline hints only after interaction for password fields
        if (!emailPresent) {
            if (emailError != null) emailError.setText("El correo es obligatorio");
        }

        // Only show password "obligatoria" after the user interacted with the password field
        if (!passPresent) {
            if (passwordTouched) {
                if (passwordError != null) passwordError.setText("La contraseña es obligatoria");
            }
        }

        // Only show confirm password "Confirma la contraseña" after the user interacted with confirm field
        if (!confPresent) {
            if (confirmTouched) {
                if (confirmPasswordError != null) confirmPasswordError.setText("Confirma la contraseña");
            }
        }

        // Show mismatch only when user has interacted with at least one password field (avoids showing before clicking)
        if ((passwordTouched || confirmTouched) && passPresent && confPresent && !pass.equals(conf)) {
            if (confirmPasswordError != null) confirmPasswordError.setText("Las contraseñas no coinciden");
            // optionally also set passwordError if desired:
            // if (passwordError != null) passwordError.setText("Las contraseñas no coinciden");
        }

        // Enable the register button only when all fields have some value
        if (registroButton != null) registroButton.setDisable(!(emailPresent && passPresent && confPresent));
    }

    @FXML
    protected void onRegreso(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("hello-view.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    /**
     * Validate email, check registration existence and validate passwords.
     */
    @FXML
    protected void onRegistro(ActionEvent event) {
        String emailText = (email != null ? email.getText().trim() : "");
        String pass = (password != null ? password.getText() : "");
        String conf = (confirmPassword != null ? confirmPassword.getText() : "");

        // Clear inline errors
        if (emailError != null) emailError.setText("");
        if (passwordError != null) passwordError.setText("");
        if (confirmPasswordError != null) confirmPasswordError.setText("");

        if (emailText.isEmpty()) {
            if (emailError != null) emailError.setText("El correo es obligatorio");
            showAlert(AlertType.ERROR, "El correo es obligatorio");
            return;
        }

        // Validate/normalize email using Usuario's validator
        Usuario tmpUsuario = new Usuario();
        String normalized = tmpUsuario.UsuaValidacion(emailText);
        if (normalized == null) {
            if (emailError != null) emailError.setText("Correo no válido");
            showAlert(AlertType.ERROR, "Correo no válido. Debe ser una dirección de email válida o terminar en @");
            return;
        }

        // Check if already registered
        Usuario usuarioToCheck = new Usuario(normalized);
        CompararDatos comparador = new CompararDatos(usuarioToCheck);
        if (comparador.EnviarDatosRegistro()) {
            if (emailError != null) emailError.setText("Correo ya registrado");
            showAlert(AlertType.ERROR, "Correo ya registrado");
            return;
        }

        // Validate passwords (matching and pattern) using Contrasenia
        if (pass.isEmpty() || conf.isEmpty()) {
            if (passwordError != null) passwordError.setText("La contraseña es obligatoria");
            if (confirmPasswordError != null) confirmPasswordError.setText("Confirma la contraseña");
            showAlert(AlertType.ERROR, "Las contraseñas son obligatorias");
            return;
        }

        Contrasenia validadorPass = new Contrasenia();
        if (!validadorPass.PassValidacion(pass, conf)) {
            if (passwordError != null) passwordError.setText("Contraseña inválida o no coinciden");
            if (confirmPasswordError != null) confirmPasswordError.setText("Contraseña inválida o no coinciden");
            showAlert(AlertType.ERROR, "Contraseña inválida o no coinciden. Requiere mínimo 6 caracteres, una mayúscula y un carácter especial.");
            return;
        }

        // All validations passed -> save the user to Registro-Login.txt
        try {
            comparador.GuardarArchivo(normalized + ":" + pass);

            showAlert(AlertType.INFORMATION, "Registro exitoso. Usuario guardado correctamente.");

            // Clear fields after successful registration
            if (email != null) email.clear();
            if (password != null) password.clear();
            if (confirmPassword != null) confirmPassword.clear();

            // Reset inline UI and touched flags
            passwordTouched = false;
            confirmTouched = false;
            updateInlineValidation();

        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Ocurrió un error al guardar el usuario: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
