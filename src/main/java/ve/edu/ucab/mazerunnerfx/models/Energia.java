package ve.edu.ucab.mazerunnerfx.models;

/**
 * Representa una fuente de energía o power-up que el jugador puede recoger.
 *
 * <p>Normalmente restaura o incrementa alguna estadística del jugador.</p>
 *
 * @author Equipo
 * @version 2026-01-12
 */
public class Energia extends Entidad{
    private final short energiaRecuperada = 2;

    public Energia() {
        super();
        this.ascii = 'G';
    }

    @Override
    public void interact(Jugador jugador) {
        System.out.println("¡Has encontrado energía! Recuperas " + energiaRecuperada + " puntos de vida.");
        jugador.recuperarVida((short) energiaRecuperada);
    }
}
