package ve.edu.ucab.mazerunnerfx.models;

import java.util.Set;

/**
 * Representa una celda del laberinto que puede contener múltiples entidades.
 * Proporciona utilidades para gestionar y mostrar su contenido.
 */
public class Celda {
    private final Set<Entidad> contenido = new java.util.TreeSet<>();
    public int valor = 0;

    /**
     * Devuelve la cantidad de entidades presentes en la celda.
     * @return número de entidades
     */
    public int cantidadEntidades() {
        return contenido.size();
    }

    /**
     * Obtiene el carácter ASCII representativo de la celda según su contenido.
     * @return carácter a mostrar para la celda
     */
    public char obtenerAscii() {
        if (contenido.isEmpty()) {
            return ' ';
        }
        if (contenido.size() == 1) {
            return contenido.iterator().next().obtenerAscii();
        }
        // Múltiples entidades: priorizar mostrar Puerta 'X'; luego Jugador '@'; si no, mostrar el conteo
        boolean hasPlayer = false;
        for (Entidad e : contenido) {
            char c = e.obtenerAscii();
            if (c == 'X') {
                return 'X';
            }
            if (c == '@') {
                hasPlayer = true;
            }
        }
        if (hasPlayer) {
            return '@';
        }
        // Retorna un carácter especial si hay múltiples entidades
        return (Integer.toString(cantidadEntidades()).charAt(0));
    }

    // Agregar / eliminar entidades de la celda
    /**
     * Agrega una entidad a la celda si no es nula.
     * @param e entidad a agregar
     */
    public void addEntidad(Entidad e) {
        if (e != null) {
            contenido.add(e);
        }
    }

    /**
     * Elimina una entidad de la celda si no es nula.
     * @param e entidad a eliminar
     */
    public void removeEntidad(Entidad e) {
        if (e != null) {
            contenido.remove(e);
        }
    }

    /**
     * Obtiene una vista inmodificable del contenido de la celda.
     * @return conjunto inmodificable de entidades
     */
    public java.util.Set<Entidad> obtenerContenido() {
        return java.util.Collections.unmodifiableSet(contenido);
    }
}
