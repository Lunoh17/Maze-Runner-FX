package ve.edu.ucab.mazerunnerfx.models;

/**
 * Representa una pared explosiva que puede ser destruida por el jugador.
 *
 * <p>Al detonar, puede alterar celdas adyacentes y cambiar la configuración del laberinto.
 * Representada por 'B'. Cuando el jugador entra en la celda explota y causa daño.</p>
 *
 * @author Equipo
 * @version 2026-01-12
 */
public class ExplosiveWall extends Entidad {
    private static final short DAMAGE = 3;

    public ExplosiveWall() {
        super();
        this.ascii = 'B';
    }

    @Override
    public void interact(Jugador jugador) {
        System.out.println("¡Has activado una pared explosiva! Pierdes " + DAMAGE + " puntos de vida.");
        jugador.recibirDanio(DAMAGE);
        // The controller or game loop will remove this entity from the cell/global list
        // after interact is called. We don't access the laberinto reference here.
    }
}
