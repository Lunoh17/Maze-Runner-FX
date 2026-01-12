package ve.edu.ucab.mazerunnerfx.models;

/**
 * Utilidades varias para la interfaz de consola.
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
