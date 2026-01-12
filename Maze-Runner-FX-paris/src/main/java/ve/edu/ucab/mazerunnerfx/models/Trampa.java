package ve.edu.ucab.mazerunnerfx.models;

/**
 * Entidad de peligro que causa daño al jugador al entrar en su celda.
 * Representada por el carácter 'T'.
 */
public class Trampa extends Entidad {
    short danio = 1;

    /**
     * Crea una nueva trampa con daño base.
     */
    public Trampa() {
        super();
        this.ascii = 'T';
    }

    /**
     * Aplica el efecto de la trampa al jugador que entra en la celda.
     * @param jugador jugador afectado por la trampa
     */
    @Override
    public void interact(Jugador jugador) {
        System.out.println("¡Has caído en una trampa! Pierdes " + danio + " punto de vida.");
        jugador.recibirDanio(danio);
    }
}
