package ve.edu.ucab.mazerunnerfx.models;

/**
 * Entidad recolectable que otorga puntos al jugador al interactuar.
 * Se representa en el laberinto con el carácter 'C'.
 */
public class Cristal extends Entidad {
    private final int puntuacion = 50;

    /**
     * Crea un cristal con la puntuación predeterminada.
     */
    public Cristal() {
        super();
        this.ascii = 'C';
    }

    /**
     * Otorga puntos al jugador y se elimina de la celda actual.
     * @param jugador jugador que recoge el cristal
     */
    @Override
    public void interact(Jugador jugador) {
        System.out.println("¡Cristal recogido! Obtienes " + puntuacion + " puntos.");
        jugador.recibirPuntos(puntuacion);
        jugador.celdaActual.removeEntidad(this);
    }
}
