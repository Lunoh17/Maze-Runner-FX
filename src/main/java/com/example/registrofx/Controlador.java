package com.example.registrofx;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;

public class Controlador {

    // --- REFERENCIAS FXML (Los fx:id de SceneBuilder) ---
    // Sin estas líneas, Java no sabe qué es 'correoRegistro', 'recuperarCorreo', etc.
    @FXML private TextField correoInicio;
    @FXML private PasswordField contraseniaInicio;

    @FXML private TextField correoRegistro;
    @FXML private PasswordField contraseniaRegistro;
    @FXML private PasswordField confirmacionRegistro;

    @FXML private TextField recuperarCorreo;

    // --- LÓGICA DE NEGOCIO (SOLID) ---
    private final ServicioAuten servicioAuten = new ServicioAuten();

    // --- MÉTODOS DE ACCIÓN ---

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
                mostrarAlerta("Éxito", "Bienvenido " + user.getCorreo(), Alert.AlertType.INFORMATION);
            } else if (resultado.equals("PASS_ERROR")) {
                mostrarAlerta("Error", "Contraseña incorrecta.", Alert.AlertType.ERROR);
            } else {
                mostrarAlerta("Error", "El usuario no existe.", Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
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

    // --- NAVEGACIÓN Y UTILIDADES ---

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
        Parent root = FXMLLoader.load(getClass().getResource(fxml));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}