package ve.edu.ucab.mazerunnerfx.models;

/**
 * Utilidades misceláneas usadas en el proyecto.
 *
 * <p>Contiene funciones auxiliares generales reutilizables por varias clases.</p>
 *
 * @author Equipo
 * @version 2026-01-12
 */
public class Misc {
    /**
     * Limpia la pantalla de la consola mediante secuencias ANSI.
     */
    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}
