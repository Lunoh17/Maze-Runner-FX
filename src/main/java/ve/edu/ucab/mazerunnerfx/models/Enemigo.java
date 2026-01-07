package ve.edu.ucab.mazerunnerfx.models;

/**
 * Entidad hostil que se mueve por el laberinto y causa daño al jugador.
 * Implementa comportamiento de Movimiento aleatorio.
 */
public class Enemigo extends Trampa implements Movimiento {
    /**
     * Crea un enemigo con daño mayor que una trampa simple.
     */
    public Enemigo() {
        super();
        this.ascii = 'E';
        this.danio = 2; // Enemigos hacen más daño que trampas normales
    }

    /**
     * Realiza un intento de movimiento aleatorio de la entidad en el laberinto.
     * @param laberinto referencia del laberinto donde se mueve el enemigo
     * @return 0 si continúa el juego; -1 si hubo problema de referencia nula
     */
    @Override
    public int movimiento(Laberinto laberinto) {

        if (laberinto == null) {
            System.out.println("Enemigo no tiene referencia al laberinto.");
            return -1;
        }

        boolean movedSuccessfully = laberinto.movimientoEntidad(this, Laberinto.DIR.values()[(int) (Math.random() * 4)]);
        // movimiento exitoso/fracaso en un intento hacia una dirección aleatoria
        if (!movedSuccessfully) {
            System.out.println("El Enemigo se pego contra la pared.");
        }
        laberinto.display();

        return 0;
    }

}
