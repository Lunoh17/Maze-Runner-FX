package ve.edu.ucab.mazerunnerfx.models;

/**
 * Entidad coleccionable requerida para abrir la puerta de salida.
 * Se representa con el carácter 'K' y aumenta el contador de llaves del jugador.
 *
 * <p>Objeto recogible por el jugador que interactúa con instancias de Puerta.</p>
 *
 * @author Equipo
 * @version 2026-01-12
 */
public class Llave extends Entidad {
    /**
     * Crea una llave representada por 'K'.
     */
    public Llave() {
        this.ascii = 'K';
    }

    /**
     * Agrega una llave al inventario del jugador y elimina la llave de la celda.
     * @param jugador jugador que recoge la llave
     */
    @Override
    public void interact(Jugador jugador) {
        System.out.println("¡Has recogido la llave!, ya puedes ir a la salida.");
        jugador.recogerLlave();
        jugador.celdaActual.removeEntidad(this);
    }
}
