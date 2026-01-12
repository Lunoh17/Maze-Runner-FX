package ve.edu.ucab.mazerunnerfx.models;

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
