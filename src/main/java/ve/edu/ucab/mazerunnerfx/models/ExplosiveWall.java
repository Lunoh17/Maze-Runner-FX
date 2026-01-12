package ve.edu.ucab.mazerunnerfx.models;

/**
 * Explosive wall entity: represented by 'B'. When the player enters the cell containing
 * this entity it "explodes" and deals 3 points of damage to the player.
 * The controller removes the entity from the cell/global list after interaction.
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

