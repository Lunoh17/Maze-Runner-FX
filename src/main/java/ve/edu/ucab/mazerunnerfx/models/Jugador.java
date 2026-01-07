package ve.edu.ucab.mazerunnerfx.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.Map;
import java.util.Scanner;
import java.util.Stack;

/**
 * Representa al jugador dentro del laberinto, con vidas, puntos y llaves.
 * Implementa el control de movimiento y la interacción con otras entidades.
 */
public class Jugador extends Entidad implements Movimiento {
    final static short MAX_VIDA = 10;
    private static final Map<Character, Laberinto.DIR> DIRECTIONS_MAP = Map.of(
            'w', Laberinto.DIR.N,
            's', Laberinto.DIR.S,
            'a', Laberinto.DIR.W,
            'd', Laberinto.DIR.E
    );
    static Scanner scanner = new Scanner(System.in);
    private final String correoElectronico;
    private final String contrasenia;
    private final Stack<Short> vidas;
    public transient Celda celdaActual;
    private int puntos = 0;
    private int llaves = 0;
    private boolean escapado = false;

    /**
     * Crea un jugador con correo y contraseña iniciales.
     * @param correoElectronico correo del jugador
     * @param contrasenia contraseña del jugador
     */
    public Jugador(String correoElectronico, String contrasenia) {
        this.correoElectronico = correoElectronico;
        this.contrasenia = contrasenia;
        this.vidas = new Stack<>();
        // Inicializar con 3 vidas
        for (int i = 0; i < 3; i++) {
            vidas.push(MAX_VIDA);
        }
        // optional internal ascii char (keeps ordering stable)
        // Traducción: carácter ASCII interno opcional (mantiene el orden estable)
        this.ascii = '@';
    }

    /**
     * Devuelve el correo electrónico del jugador.
     * @return correo electrónico
     */
    public String getCorreoElectronico() {
        return correoElectronico;
    }

    /**
     * Devuelve la contraseña del jugador.
     * @return contraseña
     */
    public String getContrasenia() {
        return contrasenia;
    }

    // Static factory to reconstruct a Jugador from a Gson JsonObject
    // Traducción: Fábrica estática para reconstruir un Jugador desde un JsonObject de Gson
    /**
     * Reconstruye un Jugador a partir de su representación JSON (Gson).
     * @param obj objeto JSON con datos del jugador
     * @return instancia reconstruida de Jugador
     */
    public static Jugador fromJson(JsonObject obj) {
        String correo = obj.has("correoElectronico") ? obj.get("correoElectronico").getAsString() : "player@example.com";
        String pass = obj.has("contrasenia") ? obj.get("contrasenia").getAsString() : "password";
        Jugador j = new Jugador(correo, pass);
        if (obj.has("ascii")) {
            String s = obj.get("ascii").getAsString();
            if (s != null && !s.isEmpty()) {
                j.ascii = s.charAt(0);
            }
        }
        if (obj.has("vidas")) {
            JsonArray va = obj.getAsJsonArray("vidas");
            j.vidas.clear();
            for (int i = 0; i < va.size(); i++) {
                short v = (short) va.get(i).getAsInt();
                j.vidas.push(v);
            }
        }
        if (obj.has("puntos")) {
            j.puntos = obj.get("puntos").getAsInt();
        }
        if (obj.has("llaves")) {
            j.llaves = obj.get("llaves").getAsInt();
        }
        if (obj.has("posX") && obj.has("posY")) {
            int px = obj.get("posX").getAsInt();
            int py = obj.get("posY").getAsInt();
            j.setPosition(px, py);
        }
        return j;
    }

    /**
     * Indica si el jugador consiguió escapar del laberinto.
     * @return true si escapó
     */
    public boolean isEscapado() {
        return escapado;
    }

    /**
     * Incrementa el puntaje del jugador.
     * @param puntos puntos a sumar
     */
    public void recibirPuntos(int puntos) {
        this.puntos += puntos;
        System.out.println("Puntuación actual: " + this.puntos);
    }

    /**
     * Incrementa el contador de llaves del jugador.
     */
    public void recogerLlave() {
        this.llaves++;
    }

    /**
     * Aplica daño a la vida actual; elimina al jugador del tablero si no quedan vidas.
     * @param dano puntos de daño a aplicar
     */
    public void recibirDanio(short dano) {
        if (!vidas.isEmpty()) {
            short vidaActual = vidas.pop();
            vidaActual -= dano;
            if (vidaActual > 0) {
                vidas.push(vidaActual);
            } else {
                System.out.println("¡Has perdido una vida!");
            }
        } else {
//            System.out.println("¡No te quedan vidas!");
            this.celdaActual.removeEntidad(this);
        }
    }

    /**
     * Interacción con otra entidad Jugador (no utilizada para jugador principal).
     * @param player otro jugador
     */
    @Override
    public void interact(Jugador player) {

    }

    /**
     * Indica si el jugador todavía tiene vidas.
     * @return true si tiene al menos una vida
     */
    public boolean taVivo(){
        return !vidas.isEmpty();
    }
    // Implement Movimiento.method(): input loop (W/A/S/D to move, Q to quit)
    // Traducción: Implementación de Movimiento.method(): bucle de entrada (W/A/S/D para moverse, Q para salir)
    /**
     * Bucle de entrada del jugador para moverlo dentro del laberinto mediante W/A/S/D o salir con Q.
     * @param laberinto laberinto actual
     * @return 0 si continúa; 1 si el jugador solicitó salir; -1 si hubo error
     */
    @Override
    public int movimiento(Laberinto laberinto) {
        if (laberinto == null) {
            System.out.println("Jugador no tiene referencia al laberinto.");
            return -1;
        }

        System.out.println("Controls: W (up), A (left), S (down), D (right). Q to quit.");
        // Traducción: Controles: W (arriba), A (izquierda), S (abajo), D (derecha). Q para salir.
        System.out.println("Vidas: " + vidas.size());
        System.out.print("Energia: ");
        if (!vidas.isEmpty()) {
            System.out.println(vidas.peek() + "/" + MAX_VIDA);
        } else {
            System.out.println("0/" + MAX_VIDA);
        }
        System.out.println("Puntos: " + puntos);
        System.out.println("Llaves: " + llaves);
        boolean movedSuccessfully = false;
        while (!movedSuccessfully) {
            System.out.print("Enter move (W/A/S/D) or Q to quit: ");
            // Traducción: Ingrese movimiento (W/A/S/D) o Q para salir:
            String rawInput = scanner.nextLine();
            if (rawInput == null || rawInput.isEmpty()) {
                continue;
            }
            char inputChar = Character.toLowerCase(rawInput.charAt(0));
            if (inputChar == 'q') {
                System.out.println("Exiting player control.");
                // Traducción: Saliendo del control del jugador.
                laberinto.display();
                return 1;
            }
            Laberinto.DIR dir = DIRECTIONS_MAP.get(inputChar);
            if (dir == null) {
                System.out.println("Entrada inválida. Use W/A/S/D para moverse, Q para salir.");
                continue;
            }
            movedSuccessfully = laberinto.movimientoEntidad(this, dir);
            if (!movedSuccessfully) {
                System.out.println("Cannot move in that direction (wall or out of bounds).");
                // Traducción: No se puede mover en esa dirección (pared o fuera de límites).
            }
        }
        laberinto.display();
        return 0;
    }

    /**
     * Indica si el jugador posee al menos una llave.
     * @return true si tiene una o más llaves
     */
    public boolean tieneLlave() {
        return llaves > 0;
    }

    /**
     * Marca al jugador como escapado del laberinto y muestra su puntaje.
     */
    public void escapar() {
        System.out.println("Jugador ha escapado del laberinto con " + puntos + " puntos.");
        this.escapado = true;
    }

    // Expose current score for statistics
    // Traducción: Exponer puntaje actual para estadísticas
    /**
     * Devuelve el puntaje actual del jugador.
     * @return puntos acumulados
     */
    public int getPuntos() {
        return puntos;
    }
}
