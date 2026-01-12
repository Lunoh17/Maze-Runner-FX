package ve.edu.ucab.mazerunnerfx.models;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;
import ve.edu.ucab.mazerunnerfx.MenuSeleccionController;

public class Controlador {

    @FXML private TextField correoInicio;
    @FXML private PasswordField contraseniaInicio;

    @FXML private TextField correoRegistro;
    @FXML private PasswordField contraseniaRegistro;
    @FXML private PasswordField confirmacionRegistro;

    @FXML private TextField recuperarCorreo;

    private final ServicioAuten servicioAuten = new ServicioAuten();


    @FXML
    public void manejarInicioSesion(ActionEvent event) {
        try {
            Usuario user = new Usuario(correoInicio.getText());
            Contrasenia pass = new Contrasenia(contraseniaInicio.getText());

            if (user.getCorreo() == null || pass.getContrasenia() == null) {
                mostrarAlerta("Error", "Formato de correo o contraseña inválido.", Alert.AlertType.ERROR);
                return;
            }

            String resultado = servicioAuten.login(user.getCorreo(), pass.getContrasenia());

            if (resultado.equals("EXITO")) {
                // 1. Mostramos el mensaje de éxito
                mostrarAlerta("Éxito", "Bienvenido " + user.getCorreo(), Alert.AlertType.INFORMATION);

                // 2. Saltamos al menú de selección
                cambiarEscena(event, "menu-seleccion.fxml");

            } else if (resultado.equals("PASS_ERROR")) {
                mostrarAlerta("Error", "Contraseña incorrecta.", Alert.AlertType.ERROR);
            } else {
                mostrarAlerta("Error", "El usuario no existe.", Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            e.printStackTrace(); // Importante para ver errores en consola
            mostrarAlerta("Error", "Fallo en el sistema de seguridad.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void manejarRegistro(ActionEvent event) {
        try {
            Usuario user = new Usuario(correoRegistro.getText());
            Contrasenia pass = new Contrasenia(contraseniaRegistro.getText(), confirmacionRegistro.getText());

            if (user.getCorreo() == null || pass.getContrasenia() == null) {
                mostrarAlerta("Error", "Datos inválidos o contraseñas no coinciden.", Alert.AlertType.ERROR);
                return;
            }
            if (!contraseniaRegistro.getText().equals(confirmacionRegistro.getText())) {
                // Mostrar alerta de que las contraseñas no coinciden
                return;
            }

            if (servicioAuten.registrar(user.getCorreo(), pass.getContrasenia())) {
                mostrarAlerta("Éxito", "Usuario registrado correctamente.", Alert.AlertType.INFORMATION);
            } else {
                mostrarAlerta("Error", "El usuario ya existe.", Alert.AlertType.WARNING);
            }
        } catch (Exception e) {
            mostrarAlerta("Error", "Error al procesar el registro.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void manejarRecuperacion(ActionEvent event) {
        try {
            String correo = recuperarCorreo.getText();
            String pass = servicioAuten.recuperarPass(correo);

            if (pass != null) {
                mostrarAlerta("Recuperada", "Su contraseña es: " + pass, Alert.AlertType.INFORMATION);
            } else {
                mostrarAlerta("Error", "Usuario no encontrado.", Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            mostrarAlerta("Error", "Error al recuperar datos.", Alert.AlertType.ERROR);
        }
    }


    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public void irALogin(ActionEvent event) throws IOException {
        cambiarEscena(event, "panel-principal.fxml");
    }

    public void irARegistro(ActionEvent event) throws IOException {
        cambiarEscena(event, "panel-registro.fxml");
    }

    public void irARecuperar(ActionEvent event) throws IOException {
        cambiarEscena(event, "panel-olcontrasenia.fxml");
    }

    private void cambiarEscena(ActionEvent event, String fxml) throws IOException {
        // 1. Construir la ruta y verificar el recurso
        String ruta = "/ve/edu/ucab/mazerunnerfx/" + fxml;
        java.net.URL url = getClass().getResource(ruta);

        if (url == null) {
            throw new IOException("Error: No se encontró el archivo FXML en: " + ruta);
        }

        // 2. Cargar el FXML
        FXMLLoader loader = new FXMLLoader(url);
        Parent root = loader.load();

        // 3. Lógica especial para pasar datos al Menú de Selección
        if (fxml.equals("menu-seleccion.fxml")) {
            MenuSeleccionController menuController = loader.getController();
            if (menuController != null) {
                // Pasamos el correo que está en el campo de texto del login
                menuController.setUsuario(correoInicio.getText());
            }
        }

        // 4. Cambiar la ventana (Stage)
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}