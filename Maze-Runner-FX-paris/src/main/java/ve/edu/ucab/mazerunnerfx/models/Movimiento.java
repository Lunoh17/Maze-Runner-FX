package ve.edu.ucab.mazerunnerfx.models;

/**
 * Contrato de movimiento para entidades que pueden desplazarse dentro del laberinto.
 */
public interface Movimiento {
    /**
     * Ejecuta un paso de movimiento de la entidad en el laberinto.
     * @param laberinto contexto del laberinto para validar y aplicar el movimiento
     * @return código de estado del movimiento (0=ok, distinto indica finalización)
     */
    int movimiento(Laberinto laberinto);
}
