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

    @FXML private TextField correoInicio;
    @FXML private PasswordField contraseniaInicio;

    @FXML private TextField correoRegistro;
    @FXML private PasswordField contraseniaRegistro;
    @FXML private PasswordField confirmacionRegistro;

    @FXML private TextField recuperarCorreo;

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public void irALogin(ActionEvent event) throws IOException {
        cambiarEscena(event, "panel-principal.fxml"); // Ajusta el nombre exacto de tu archivo
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

    @FXML
    public void manejarInicioSesion(ActionEvent event) {
        Usuario user = new Usuario(correoInicio.getText());
        Contrasenia pass = new Contrasenia(contraseniaInicio.getText());

        if (user.getCorreo() == null || pass.getContrasenia() == null) {
            mostrarAlerta("Error", "Formato de correo o contraseña inválido.", Alert.AlertType.ERROR);
            return;
        }

        CompararDatos comparar = new CompararDatos(user, pass);
        if (comparar.EnviarDatosSesion()) {
            mostrarAlerta("Éxito", "Bienvenido " + user.getCorreo(), Alert.AlertType.INFORMATION);
        } else {
            mostrarAlerta("Error", "Usuario no registrado o contraseña incorrecta.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void manejarRegistro(ActionEvent event) {
        Usuario user = new Usuario(correoRegistro.getText());
        Contrasenia pass = new Contrasenia(contraseniaRegistro.getText(), confirmacionRegistro.getText());

        if (user.getCorreo() == null) {
            mostrarAlerta("Error", "Correo inválido.", Alert.AlertType.ERROR);
            return;
        }
        if (pass.getContrasenia() == null) {
            mostrarAlerta("Error", "La contraseña no coincide o no cumple los requisitos (6 caracteres, Mayúscula, Especial).", Alert.AlertType.ERROR);
            return;
        }

        CompararDatos comparar = new CompararDatos(user, pass);
        if (!comparar.EnviarDatosRegistro()) {
            GuardarDatos guardar = new GuardarDatos(user, pass);
            guardar.GuardarFormatoRegistro();
            mostrarAlerta("Éxito", "Usuario registrado correctamente.", Alert.AlertType.INFORMATION);
        } else {
            mostrarAlerta("Error", "El usuario ya existe.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    public void manejarRecuperacion(ActionEvent event) {
        String correo = recuperarCorreo.getText();
        Usuario user = new Usuario(correo);

        if (user.getCorreo() == null) {
            mostrarAlerta("Error", "Ingrese un correo válido.", Alert.AlertType.ERROR);
            return;
        }

        CompararDatos buscador = new CompararDatos(user);
        String passRecuperada = buscador.obtenerContraseniaRecuperada(correo);

        if (passRecuperada != null) {
            mostrarAlerta("Recuperación Exitosa",
                    "La contraseña para " + correo + " es: " + passRecuperada,
                    Alert.AlertType.INFORMATION);
        } else {
            mostrarAlerta("Error", "El usuario no existe en nuestros registros.", Alert.AlertType.ERROR);
        }
    }
}