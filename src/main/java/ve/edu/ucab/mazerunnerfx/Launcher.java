package ve.edu.ucab.mazerunnerfx;

import javafx.application.Application;

/**
 * Clase lanzadora para ejecutar la aplicación en entornos donde la clase principal
 * no puede ser detectada directamente (helper para IDEs y empaquetado).
 *
 * @author Equipo
 * @version 2026-01-12
 */
public class Launcher {
    public static void main(String[] args) {
        Application.launch(HelloApplication.class, args);
    }
}
