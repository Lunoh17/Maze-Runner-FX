package ve.edu.ucab.mazerunnerfx;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import ve.edu.ucab.mazerunnerfx.models.Entidad;
import ve.edu.ucab.mazerunnerfx.models.Laberinto;
import ve.edu.ucab.mazerunnerfx.models.ControladorBD;

import java.util.Set;

/**
 * Controller for laberinto.fxml: renders the maze on a Canvas using model accessors.
 * Handles WASD input for the player and moves enemies after each player move.
 */
public class LaberintoController {

    @FXML
    private Canvas mazeCanvas;

    @FXML
    private Label scoreLabel;

    @FXML
    private Label timeLabel;

    @FXML
    private Label livesLabel;

    @FXML
    private Label healthLabel;

    @FXML
    @SuppressWarnings("unused")
    private javafx.scene.control.Button regresar;

    private Laberinto laberinto;

    private Timeline timeline;
    private long elapsedSeconds = 0;

    public void setLaberinto(Laberinto lab) {
        this.laberinto = lab;
        if (this.laberinto != null && mazeCanvas != null) {
            // register listener to redraw when model calls display()
            this.laberinto.setDisplayListener(this::onDisplay);
            setupInput();
            render();
            // ensure the canvas has focus so it can receive WASD/arrow keys
            // Request focus after the Scene is realized so key events are delivered reliably
            Platform.runLater(() -> {
                try {
                    mazeCanvas.requestFocus();
                } catch (Throwable ignored) {
                }
            });
            // init labels
            updateScoreLabel();
            updateLivesAndHealth();
            // restore elapsed time from loaded model if present
            try {
                this.elapsedSeconds = Math.max(0L, this.laberinto.tiempoSegundos);
            } catch (Throwable ignored) {
                this.elapsedSeconds = 0L;
            }
            updateTimeLabel();
            startTimer();
        }
    }

    private void onDisplay(String s) {
        // parameter is intentionally available if needed by future features
        Platform.runLater(this::render);
    }

    @FXML
    protected void onRegresar(ActionEvent event) {
        // stop timer and persist current lab state
        stopTimer();
        if (laberinto != null) {
            try {
                // ensure latest elapsed time is stored in model before saving
                laberinto.tiempoSegundos = this.elapsedSeconds;
            } catch (Throwable ignored) {
            }
            ControladorBD.guardar(laberinto);
        }

        // navigate back to menu-seleccion.fxml and pass the player email if available
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("menu-seleccion.fxml"));
            javafx.scene.Parent root = loader.load();
            MenuSeleccionController menuController = loader.getController();
            if (menuController != null && laberinto != null && laberinto.jugador != null) {
                String correo = laberinto.jugador.getCorreoElectronico();
                menuController.setUsuario(correo);
            }
            javafx.stage.Stage stage = (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();
        } catch (java.io.IOException ex) {
            ex.printStackTrace();
        }
    }

    private void setupInput() {
        // ensure canvas can receive keys
        mazeCanvas.setFocusTraversable(true);
        mazeCanvas.setOnKeyPressed(ev -> {
            if (laberinto == null || laberinto.jugador == null) return;
            KeyCode kc = ev.getCode();
            Laberinto.DIR dir = null;
            if (kc == KeyCode.W || kc == KeyCode.UP) dir = Laberinto.DIR.N;
            else if (kc == KeyCode.S || kc == KeyCode.DOWN) dir = Laberinto.DIR.S;
            else if (kc == KeyCode.A || kc == KeyCode.LEFT) dir = Laberinto.DIR.W;
            else if (kc == KeyCode.D || kc == KeyCode.RIGHT) dir = Laberinto.DIR.E;

            if (dir != null) {
                boolean moved = laberinto.movimientoEntidad(laberinto.jugador, dir);
                if (moved) {
                    // handle interactions caused by player's move
                    if (laberinto.jugador.celdaActual != null && laberinto.jugador.celdaActual.cantidadEntidades() > 1) {
                        Set<Entidad> contents = laberinto.jugador.celdaActual.obtenerContenido();
                        for (Entidad e : Set.copyOf(contents)) {
                            e.interact(laberinto.jugador);
                            char ascii = e.obtenerAscii();
                            if (ascii == 'K' || ascii == 'C') {
                                // remove from cell and from global list
                                laberinto.jugador.celdaActual.removeEntidad(e);
                                laberinto.removeEntidadGlobal(e);
                            }
                        }
                    }

                    // After player's move and its immediate interactions, move enemies once
                    laberinto.stepEntities();

                    // handle interactions after enemies moved (player might be attacked/moved into)
                    if (laberinto.jugador != null && laberinto.jugador.celdaActual != null &&
                            laberinto.jugador.celdaActual.cantidadEntidades() > 1) {
                        Set<Entidad> contents2 = laberinto.jugador.celdaActual.obtenerContenido();
                        for (Entidad e : Set.copyOf(contents2)) {
                            e.interact(laberinto.jugador);
                            char ascii = e.obtenerAscii();
                            if (ascii == 'K' || ascii == 'C') {
                                laberinto.jugador.celdaActual.removeEntidad(e);
                                laberinto.removeEntidadGlobal(e);
                            }
                        }
                    }

                    // update score label (player may have gained points)
                    updateScoreLabel();
                    updateLivesAndHealth();
                }

                // update UI after the full step
                laberinto.display(); // triggers UI listener which calls render()

                // if game ended by escaping or death, stop timer
                if (!laberinto.jugador.taVivo() || laberinto.jugador.isEscapado()) {
                    stopTimer();
                    if (!laberinto.jugador.taVivo()) {
                        // show game over and return to menu
                        Platform.runLater(() -> {
                            javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                            a.setHeaderText(null);
                            a.setContentText("Has perdido todas las vidas. Juego terminado.");
                            a.showAndWait();
                            try {
                                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("menu-seleccion.fxml"));
                                javafx.scene.Parent root = loader.load();
                                javafx.stage.Stage stage = (javafx.stage.Stage) mazeCanvas.getScene().getWindow();
                                stage.setScene(new javafx.scene.Scene(root));
                                stage.show();
                            } catch (java.io.IOException ex) {
                                ex.printStackTrace();
                            }
                        });
                    }
                }
            }
        });
    }

    private void startTimer() {
        if (timeline != null) return;
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), this::onTick));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private void onTick(javafx.event.ActionEvent ev) {
        // use the ActionEvent parameter to avoid unused-parameter warnings
        elapsedSeconds++;
        // also persist to model so future saves include the latest time
        try {
            if (laberinto != null) laberinto.tiempoSegundos = elapsedSeconds;
        } catch (Throwable ignored) {
        }
        updateTimeLabel();
    }

    private void stopTimer() {
        if (timeline != null) {
            timeline.stop();
            timeline = null;
        }
    }

    private void updateTimeLabel() {
        long mins = elapsedSeconds / 60;
        long secs = elapsedSeconds % 60;
        String s = String.format("Tiempo: %02d:%02d", mins, secs);
        if (timeLabel != null) timeLabel.setText(s);
    }

    private void updateScoreLabel() {
        if (scoreLabel != null && laberinto != null && laberinto.jugador != null) {
            scoreLabel.setText("Puntos: " + laberinto.jugador.getPuntos());
        }
    }

    private void updateLivesAndHealth() {
        if (livesLabel != null && healthLabel != null && laberinto != null && laberinto.jugador != null) {
            livesLabel.setText("Vidas: " + laberinto.jugador.getVidasCount());
            healthLabel.setText("Energia: " + laberinto.jugador.getVidaActual() + "/" + laberinto.jugador.getMaxVida());
        }
    }

    /**
     * Allow the caller to suggest a scene/window size so the controller can resize the canvas accordingly.
     * This is typically called before the Scene is set on Stage.
     */
    public void setWindowSize(double sceneWidth, double sceneHeight) {
        if (mazeCanvas != null) {
            // leave some space for top HUD; set canvas to occupy most of the scene center area
            double topBar = 48; // estimated HUD height
            double canvasW = Math.max(240, sceneWidth - 40);
            double canvasH = Math.max(160, sceneHeight - topBar - 80);
            mazeCanvas.setWidth(canvasW);
            mazeCanvas.setHeight(canvasH);
        }
    }

    private void render() {
        if (laberinto == null || mazeCanvas == null) return;
        GraphicsContext gc = mazeCanvas.getGraphicsContext2D();
        int w = laberinto.getWidth();
        int h = laberinto.getHeight();
        double canvasW = mazeCanvas.getWidth();
        double canvasH = mazeCanvas.getHeight();
        double cellW = Math.max(4, Math.floor(canvasW / w));
        double cellH = Math.max(4, Math.floor(canvasH / h));

        // Clear
        gc.setFill(Color.web("#f3f3f3"));
        gc.fillRect(0, 0, canvasW, canvasH);

        // draw cells
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                double px = x * cellW;
                double py = y * cellH;
                // background
                gc.setFill(Color.WHITE);
                gc.fillRect(px, py, cellW, cellH);

                // draw walls as lines based on the bitmask
                int val = laberinto.getCellValue(x, y);
                gc.setStroke(Color.BLACK);
                gc.setLineWidth(1.5);
                // North wall
                if ((val & Laberinto.DIR.N.bit) == 0) {
                    gc.strokeLine(px, py, px + cellW, py);
                }
                // West wall
                if ((val & Laberinto.DIR.W.bit) == 0) {
                    gc.strokeLine(px, py, px, py + cellH);
                }
                // East wall (draw on cell's east edge)
                if ((val & Laberinto.DIR.E.bit) == 0) {
                    gc.strokeLine(px + cellW, py, px + cellW, py + cellH);
                }
                // South wall
                if ((val & Laberinto.DIR.S.bit) == 0) {
                    gc.strokeLine(px, py + cellH, px + cellW, py + cellH);
                }

                // draw entity char with color hints
                char c = laberinto.getCellChar(x, y);
                if (c != ' ' && c != '\0') {
                    switch (c) {
                        case '@' -> gc.setFill(Color.DODGERBLUE);
                        case 'E' -> gc.setFill(Color.CRIMSON);
                        case 'T' -> gc.setFill(Color.DARKGRAY);
                        case 'C' -> gc.setFill(Color.GOLD);
                        case 'K' -> gc.setFill(Color.ORANGE);
                        case 'X' -> gc.setFill(Color.FORESTGREEN);
                        default -> gc.setFill(Color.BLACK);
                    }
                    gc.fillOval(px + cellW * 0.2, py + cellH * 0.2, cellW * 0.6, cellH * 0.6);
                }
            }
        }
    }
}
