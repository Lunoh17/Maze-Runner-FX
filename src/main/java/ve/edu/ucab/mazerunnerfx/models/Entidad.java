package ve.edu.ucab.mazerunnerfx.models;

/**
 * Clase base abstracta para todas las entidades dentro del laberinto.
 * Define posición, representación ASCII e interacción con el jugador.
 */
public abstract class Entidad implements Comparable<Entidad> {
    protected char ascii;
    protected int posX = 0;
    protected int posY = 0;

    /**
     * Establece la posición de la entidad.
     * @param x coordenada X
     * @param y coordenada Y
     */
    public void setPosition(int x, int y) {
        this.posX = x;
        this.posY = y;
    }

    /**
     * Obtiene la coordenada X.
     * @return posición X
     */
    public int getPosX() {
        return posX;
    }

    /**
     * Obtiene la coordenada Y.
     * @return posición Y
     */
    public int getPosY() {
        return posY;
    }

    // Provide a default way to get the display character for an entity
    // Traducción: Proveer una forma por defecto de obtener el carácter de visualización de una entidad
    /**
     * Devuelve el carácter ASCII que representa a la entidad.
     * @return carácter ASCII de la entidad
     */
    public char obtenerAscii() {
        return this.ascii;
    }

    /**
     * Define la interacción de la entidad con el jugador cuando comparten celda.
     * @param player jugador con el que interactúa la entidad
     */
    public abstract void interact(Jugador player);

    /**
     * Ordena entidades por su carácter ASCII representativo.
     * @param otraEntidad otra instancia de entidad a comparar
     * @return resultado de la comparación por carácter
     */
    @Override
    public int compareTo(Entidad otraEntidad) {
        return Character.compare(this.ascii, otraEntidad.ascii);
    }
}
