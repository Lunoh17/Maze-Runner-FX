package ve.edu.ucab.mazerunnerfx.models;

/**
 * Representa una puerta dentro del laberinto.
 *
 * <p>Puede actuar como salida o como elemento que cambia su estado de abierto/cerrado.
 * Requiere una llave para activar la victoria. Representada por el carácter 'X'.</p>
 *
 * @author Equipo
 * @version 2026-01-12
 */
public class Puerta extends Entidad {
    /**
     * Crea una puerta cerrada representada por 'X'.
     */
    public Puerta() {
        super();
        this.ascii = 'X';
    }

    /**
     * Intenta abrir la puerta; si el jugador tiene una llave, se marca la victoria.
     * @param jugador jugador que interactúa con la puerta
     */
    @Override
    public void interact(Jugador jugador) {
        if (jugador.tieneLlave()) {
            System.out.println("¡Has abierto la puerta y escapado del laberinto! ¡Felicidades!");
            jugador.escapar();
        } else {
            System.out.println("La puerta está cerrada. Necesitas una llave para abrirla.");
        }
    }
}
