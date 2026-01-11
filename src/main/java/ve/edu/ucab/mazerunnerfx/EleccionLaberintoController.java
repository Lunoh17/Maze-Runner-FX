package ve.edu.ucab.mazerunnerfx;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import java.io.IOException;
import ve.edu.ucab.mazerunnerfx.models.Laberinto;
import ve.edu.ucab.mazerunnerfx.models.Jugador;
import ve.edu.ucab.mazerunnerfx.models.AESCifrado;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;

public class EleccionLaberintoController {

    private String usuarioCorreo;

    // Setter to receive the logged-in user's correo from previous controller
    public void setUsuario(String correo) {
        this.usuarioCorreo = correo;
    }

    @FXML
    protected void onRegresar(ActionEvent event) {
        navigateToMenu(event);
    }

    @FXML
    protected void onFacil(ActionEvent event) {
        startGameWithUI("Fácil", event);
    }

    @FXML
    protected void onIntermedio(ActionEvent event) {
        startGameWithUI("Intermedio", event);
    }

    @FXML
    protected void onDificil(ActionEvent event) {
        startGameWithUI("Difícil", event);
    }

    @FXML
    protected void onAvanzado(ActionEvent event) {
        startGameWithUI("Avanzado", event);
    }

    private void startGameWithUI(String nivel, ActionEvent event) {
        // compute random dimensions and create Laberinto and Jugador
        int minX, maxX, minY, maxY;
        switch (nivel) {
            case "Fácil" -> { minX = 5; minY = 10; maxX = 15; maxY = 25; }
            case "Intermedio" -> { minX = 16; minY = 26; maxX = 25; maxY = 35; }
            case "Difícil" -> { minX = 26; minY = 36; maxX = 45; maxY = 65; }
            case "Avanzado" -> { minX = 46; minY = 66; maxX = 75; maxY = 100; }
            default -> { minX = 10; minY = 10; maxX = 12; maxY = 12; }
        }
        int x, y;
        do { x = randomBetween(minX, maxX); y = randomBetween(minY, maxY); } while (x == y);

        // determine explosive wall count per difficulty
        int explosiveCount = switch (nivel) {
            case "Fácil" -> 5;
            case "Intermedio" -> 15;
            case "Difícil" -> 20;
            case "Avanzado" -> 25;
            default -> -1;
        };
        Laberinto lab = new Laberinto(x, y, explosiveCount);
        String correo = (usuarioCorreo != null && !usuarioCorreo.isEmpty()) ? usuarioCorreo : "player@example.com";
        String password = getPasswordForEmail(correo);
        Jugador jugador = new Jugador(correo, password);
        lab.setJugador(jugador);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("laberinto.fxml"));
            Parent root = loader.load();
            LaberintoController controller = loader.getController();
            if (controller != null) controller.setLaberinto(lab);

            // determine desired window size based on difficulty
            double desiredW, desiredH;
            switch (nivel) {
                case "Fácil" -> { desiredW = 800; desiredH = 600; }
                case "Intermedio" -> { desiredW = 1024; desiredH = 768; }
                case "Difícil" -> { desiredW = 1280; desiredH = 800; }
                case "Avanzado" -> { desiredW = 1600; desiredH = 1000; }
                default -> { desiredW = 900; desiredH = 600; }
            }
            // clamp to available screen bounds with small margin
            Rectangle2D vb = Screen.getPrimary().getVisualBounds();
            desiredW = Math.min(desiredW, Math.max(400, vb.getWidth() - 80));
            desiredH = Math.min(desiredH, Math.max(300, vb.getHeight() - 120));

            // allow controller to adjust canvas sizes to scene
            if (controller != null) controller.setWindowSize(desiredW, desiredH);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, desiredW, desiredH));
            stage.show();

            Thread gameThread = new Thread(() -> lab.jugar(), "GameThread-" + nivel + "-" + x + "x" + y);
            gameThread.setDaemon(true);
            gameThread.start();

        } catch (IOException e) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setContentText("No se pudo abrir la vista de juego: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private int randomBetween(int min, int max) {
        if (min >= max) return min;
        return min + (int) (Math.random() * (max - min + 1));
    }

    private String getPasswordForEmail(String email) {
        if (email == null || email.isEmpty()) return "password";
        File f = new File(System.getProperty("user.dir"), "Registro-Login.txt");
        if (!f.exists()) return "password";
        try (Scanner sc = new Scanner(f)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split(":");
                if (parts.length != 2) continue;
                String u = parts[0].trim();
                String enc = parts[1].trim();
                if (u.equals(email)) {
                    try {
                        String dec = AESCifrado.Descifrado(enc);
                        if (dec != null && !dec.isEmpty()) return dec;
                    } catch (Exception ignored) {
                    }
                }
            }
        } catch (FileNotFoundException ex) {
        }
        return "password";
    }

    private void navigateToMenu(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("menu-seleccion.fxml"));
            Parent root = loader.load();

            MenuSeleccionController menuController = loader.getController();
            if (menuController != null && usuarioCorreo != null) {
                menuController.setUsuario(usuarioCorreo);
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setContentText("No se pudo regresar al menú: " + e.getMessage());
            alert.showAndWait();
        }
    }
}
