package ve.edu.ucab.mazerunnerfx.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;
import java.util.function.Consumer;

/**
 * Representa el laberinto, su generación, visualización y lógica de juego.
 * Administra celdas, entidades y carga/guardado desde archivos JSON.
 */
public class Laberinto {
    // Allow larger mazes so difficulty ranges up to 75x75 are valid
    private static final int MAX_DIM = 100;
    private static final int MIN_DIM = 1;
    private final int x;
    private final int y;
    private final Celda[][] maze;
    private final Vector<Entidad> entidades = new Vector<>();
    // almacenar el jugador para que persista y se use para iniciar el bucle de entrada
    public Jugador jugador;

    // Persisted elapsed time (in seconds) for the saved maze. Serialized by Gson.
    // This field is new: it stores the time so saved files include the elapsed play time.
    public long tiempoSegundos = 0L;

    // Listener hook for UI to receive the textual representation when display() is called
    private transient Consumer<String> displayListener = null;

    public void setDisplayListener(Consumer<String> listener) {
        this.displayListener = listener;
    }

    /**
     * Crea un laberinto cuadrado de tamaño dado (clamp entre 1 y 50).
     * @param size tamaño del laberinto (ancho=alto)
     */
    public Laberinto(int size) {
        this(clamp(size), clamp(size));
    }

    // Constructor privado usado por cargarJson para construir la cuadrícula vacía sin iniciar el bucle
    /**
     * Constructor interno para inicializar la grilla sin generar contenido de juego.
     * @param x ancho
     * @param y alto
     * @param skipGameLoop indicador para saltar configuración de juego
     */
    private Laberinto(int x, int y, boolean skipGameLoop) {
        this.x = x;
        this.y = y;
        maze = new Celda[this.x][this.y];
        for (int i = 0; i < maze.length; i++) {
            for (int j = 0; j < maze[i].length; j++) {
                maze[i][j] = new Celda();
            }
        }
    }

    /**
     * Crea un laberinto con dimensiones dadas, genera el trazado y coloca entidades iniciales.
     * @param x ancho del laberinto
     * @param y alto del laberinto
     */
    public Laberinto(int x, int y) {
        this.x = Math.max(MIN_DIM, Math.min(MAX_DIM, x));
        this.y = Math.max(MIN_DIM, Math.min(MAX_DIM, y));
        maze = new Celda[this.x][this.y];
        for (int i = 0; i < maze.length; i++) {
            for (int j = 0; j < maze[i].length; j++) {
                maze[i][j] = new Celda();
            }
        }
        // Start generation from the center to avoid strong corner bias
        generateMaze(this.x / 2, this.y / 2);
        // populate entities using shared helper so cargado can reuse it if needed
        populateDefaultEntities();
    }

    private int explosiveWallCount = -1; // if >=0, use this exact count when placing explosive walls

    /**
     * Create a Laberinto with explicit explosive wall count (useful to control difficulty placement).
     * @param x width
     * @param y height
     * @param explosiveCount exact number of explosive walls to place (>=0). If negative, fallback to default rule.
     */
    public Laberinto(int x, int y, int explosiveCount) {
        this.x = Math.max(MIN_DIM, Math.min(MAX_DIM, x));
        this.y = Math.max(MIN_DIM, Math.min(MAX_DIM, y));
        this.explosiveWallCount = explosiveCount;
        maze = new Celda[this.x][this.y];
        for (int i = 0; i < maze.length; i++) {
            for (int j = 0; j < maze[i].length; j++) {
                maze[i][j] = new Celda();
            }
        }
        // Start generation from the center to avoid strong corner bias
        generateMaze(this.x / 2, this.y / 2);
        // populate entities using shared helper so cargado can reuse it if needed
        populateDefaultEntities();
    }

    // Helper to populate default entities (used by constructor and when loading a save that lacks entities)
    private void populateDefaultEntities() {
        final int nEntidad = Math.toIntExact(Math.round((double) (this.x * this.y) / 10d)); // 10% de las celdas tendrán peligros
        // Coloca al jugador en la celda de inicio (0,0) si no existe
        if (this.jugador == null) {
            this.jugador = new Jugador("player@example.com", "password");
            this.jugador.setPosition(0, 0);
            this.jugador.celdaActual = maze[0][0];
            maze[0][0].addEntidad(this.jugador);
        } else {
            // ensure jugador is placed at initial position if its position is default
            if (this.jugador.getPosX() == 0 && this.jugador.getPosY() == 0 && this.jugador.celdaActual == null) {
                this.jugador.celdaActual = maze[0][0];
                maze[0][0].addEntidad(this.jugador);
            }
        }

        // Place crystals at half the previous density (previously nEntidad -> now half). Ensure at least 1 crystal.
        int nCristales = Math.max(1, nEntidad / 2);
        for (int i = 0; i < nCristales; i++) {
            int px, py;
            do {
                px = (int) (Math.random() * this.x);
                py = (int) (Math.random() * this.y);
            } while ((px == 0 && py == 0) || !maze[px][py].obtenerContenido().isEmpty());
            Cristal cristales = new Cristal();
            cristales.setPosition(px, py);
            maze[px][py].addEntidad(cristales);
            entidades.add(cristales);
        }
        // Place traps according to the rule: one trap per every 5 rows of the maze (floor(y/5)).
        // Example: y=5 => 1 trap, y=9 => 1 trap, y=10 => 2 traps.
        int numTraps = this.y / 5;
        for (int i = 0; i < numTraps; i++) {
            int px = 0, py = 0;
            int attempts = 0;
            // try to find an empty non-start cell; give up after a large number of attempts to avoid infinite loop
            do {
                px = (int) (Math.random() * this.x);
                py = (int) (Math.random() * this.y);
                attempts++;
            } while (((px == 0 && py == 0) || !maze[px][py].obtenerContenido().isEmpty()) && attempts < 1000);
            if (attempts >= 1000) {
                // unable to place this trap; skip
                continue;
            }
            Trampa trampa = new Trampa();
            trampa.setPosition(px, py);
            maze[px][py].addEntidad(trampa);
            entidades.add(trampa);
        }
        // Place Energia using the same rule as traps: one Energia per every 5 rows (floor(y/5)).
        // Energies will not be placed on the start cell or on occupied cells; retries are bounded.
        int numEnergia = numTraps; // same count as traps
        for (int i = 0; i < numEnergia; i++) {
            int px = 0, py = 0;
            int attempts = 0;
            do {
                px = (int) (Math.random() * this.x);
                py = (int) (Math.random() * this.y);
                attempts++;
            } while (((px == 0 && py == 0) || !maze[px][py].obtenerContenido().isEmpty()) && attempts < 1000);
            if (attempts >= 1000) {
                // skip if unable to find free cell
                continue;
            }
            Energia energia = new Energia();
            energia.setPosition(px, py);
            maze[px][py].addEntidad(energia);
            entidades.add(energia);
        }
        // Place explosive walls as optional shortcuts: one per every 10 rows (floor(y/10)).
        // Each explosive wall is placed on the far side of an existing closed wall so the player
        // can attempt to move through the wall (takes damage and destroys the wall entity).
        int numExplosive;
        if (this.explosiveWallCount >= 0) {
            numExplosive = Math.max(0, this.explosiveWallCount);
        } else {
            numExplosive = Math.max(0, this.y / 10);
        }
        for (int i = 0; i < numExplosive; i++) {
            int attempts = 0;
            boolean placed = false;
            while (!placed && attempts < 2000) {
                attempts++;
                int cx = (int) (Math.random() * this.x);
                int cy = (int) (Math.random() * this.y);
                if (cx == 0 && cy == 0) continue;
                // pick a random direction that currently has a closed wall
                Laberinto.DIR[] dirs = Laberinto.DIR.values();
                Laberinto.DIR dir = dirs[(int) (Math.random() * dirs.length)];
                int nx = cx + dir.direccionX;
                int ny = cy + dir.direccionY;
                if (!between(nx, this.x) || !between(ny, this.y)) continue;
                // ensure there is a wall between cx,cy and nx,ny so this represents a breakable wall
                if ((maze[cx][cy].valor & dir.bit) != 0) continue; // there's already a passage
                // ensure destination cell is empty
                if (!maze[nx][ny].obtenerContenido().isEmpty()) continue;
                // place explosive wall in the destination cell
                ExplosiveWall bw = new ExplosiveWall();
                bw.setPosition(nx, ny);
                maze[nx][ny].addEntidad(bw);
                entidades.add(bw);
                placed = true;
            }
        }
        for (int i = 0; i < 1; i++) {
            int px, py;
            do {
                px = (int) (Math.random() * this.x);
                py = (int) (Math.random() * this.y);
            } while ((px == 0 && py == 0) || !maze[px][py].obtenerContenido().isEmpty());
            Llave llave = new Llave();
            llave.setPosition(px, py);
            maze[px][py].addEntidad(llave);
            entidades.add(llave);
        }
        for (int i = 0; i < 1; i++) {
            int px, py;
            switch (Math.toIntExact(Math.round(Math.random() * 3))) {
                case 0 -> {
                    do {
                        px = (int) (Math.random() * this.x);
                        py = 0;
                    } while ((px == 0 && py == 0) || !maze[px][py].obtenerContenido().isEmpty());
                }
                case 1 -> {
                    do {
                        px = this.x-1 ;
                        py = (int) (Math.random() * this.y);
                    } while ((px == 0 && py == 0) || !maze[px][py].obtenerContenido().isEmpty());

                }
                case 2 -> {
                    do {
                        px = (int) (Math.random() * this.x);
                        py = this.y-1;
                    } while ((px == 0 && py == 0) || !maze[px][py].obtenerContenido().isEmpty());
                }
                default -> {
                    do {
                        px = 0;
                        py = (int) (Math.random() * this.y);
                    } while ((px == 0 && py == 0) || !maze[px][py].obtenerContenido().isEmpty());
                }
            }
            Puerta puerta = new Puerta();
            puerta.setPosition(px, py);
            maze[px][py].addEntidad(puerta);
            entidades.add(puerta);
        }
    }


    /**
     * Verifica si un valor está dentro de los límites [0, upper).
     * @param v valor a comprobar
     * @param upper límite superior exclusivo
     * @return true si está dentro del rango
     */
    private static boolean between(int v, int upper) {
        return (v >= 0) && (v < upper);
    }

    /**
     * Restringe un valor a los límites mínimos y máximos permitidos.
     * @param v valor a limitar
     * @return valor ajustado dentro de [MIN_DIM, MAX_DIM]
     */
    private static int clamp(int v) {
        return Math.max(MIN_DIM, Math.min(MAX_DIM, v));
    }

    /**
     * Carga el laberinto desde el archivo laberinto.json ubicado en el directorio del proyecto.
     * Reconstruye celdas, jugador y entidades, y enlaza referencias.
     */
    public static Laberinto cargarJson() {
        String projectRoot = System.getProperty("user.dir");
        File inFile = new File(projectRoot, "laberinto.json");
        if (!inFile.exists()) {
            System.err.println("No se encontró laberinto.json en: " + inFile.getAbsolutePath());
            return null;
        }
        try (FileReader fr = new FileReader(inFile)) {
            JsonObject root = JsonParser.parseReader(fr).getAsJsonObject();
            int x = root.has("x") ? root.get("x").getAsInt() : 0;
            int y = root.has("y") ? root.get("y").getAsInt() : 0;
            Laberinto lab = new Laberinto(x, y, true);

            // Rellenar valores y contenidos de las celdas del laberinto
            if (root.has("maze")) {
                JsonArray mazeArray = root.getAsJsonArray("maze");
                for (int i = 0; i < mazeArray.size() && i < lab.maze.length; i++) {
                    JsonArray col = mazeArray.get(i).getAsJsonArray();
                    for (int j = 0; j < col.size() && j < lab.maze[i].length; j++) {
                        JsonObject cellObj = col.get(j).getAsJsonObject();
                        if (cellObj.has("valor")) {
                            lab.maze[i][j].valor = cellObj.get("valor").getAsInt();
                        }
                        // el contenido se reconstruirá abajo usando root.entidades y root.jugador principalmente
                    }
                }
            }

            // Primero reconstruir el jugador si está presente en la raíz
            if (root.has("jugador")) {
                JsonObject jObj = root.getAsJsonObject("jugador");
                Jugador j = Jugador.fromJson(jObj);
                lab.jugador = j;
                // Traducción: colocar al jugador en el laberinto si existen posiciones válidas
                if (j.getPosX() >= 0 && j.getPosY() >= 0 && j.getPosX() < lab.x && j.getPosY() < lab.y) {
                    j.celdaActual = lab.maze[j.getPosX()][j.getPosY()];
                    lab.maze[j.getPosX()][j.getPosY()].addEntidad(j);
                }
            }

            // Reconstruir la lista de entidades desde root.entidades (preferido) o escanando celdas
            if (root.has("entidades")) {
                JsonArray ents = root.getAsJsonArray("entidades");
                for (JsonElement ee : ents) {
                    JsonObject eo = ee.getAsJsonObject();
                    Entidad entidad = crearEntidadDesdeJson(eo);
                    if (entidad != null) {
                        int px = entidad.getPosX();
                        int py = entidad.getPosY();
                        if (px >= 0 && py >= 0 && px < lab.x && py < lab.y) {
                            lab.maze[px][py].addEntidad(entidad);
                        }
                        // Añadir toda entidad no jugador para persistir en próximos guardados (incluye Puerta 'X')
                        if (!(entidad instanceof Jugador)) {
                            lab.entidades.add(entidad);
                        }
                    }
                }
            } else {
                // No entities saved: do not randomly repopulate – keep maze layout only.
                // Ensure there is at least a player in the maze so UI and input work.
                // Entities not present in this save: do NOT regenerate random entities here.
                // Preserve the maze layout exactly and only ensure a player entity exists and is placed.
                if (lab.jugador == null) {
                    lab.jugador = new Jugador("player@example.com", "password");
                    lab.jugador.setPosition(0, 0);
                    lab.jugador.celdaActual = lab.maze[0][0];
                    lab.maze[0][0].addEntidad(lab.jugador);
                } else {
                    // If jugador has valid position, place it in the corresponding cell; otherwise reset to 0,0
                    if (lab.jugador.getPosX() >= 0 && lab.jugador.getPosX() < lab.x && lab.jugador.getPosY() >= 0 && lab.jugador.getPosY() < lab.y) {
                        lab.jugador.celdaActual = lab.maze[lab.jugador.getPosX()][lab.jugador.getPosY()];
                        lab.maze[lab.jugador.getPosX()][lab.jugador.getPosY()].addEntidad(lab.jugador);
                    } else {
                        lab.jugador.setPosition(0,0);
                        lab.jugador.celdaActual = lab.maze[0][0];
                        lab.maze[0][0].addEntidad(lab.jugador);
                    }
                }
            }

            // If the saved JSON contains an elapsed time field, restore it into the Laberinto
            if (root.has("tiempoSegundos")) {
                try {
                    lab.tiempoSegundos = root.get("tiempoSegundos").getAsLong();
                } catch (Throwable ignored) {
                }
            }

            return lab;
        } catch (IOException ex) {
            System.err.println("Error leyendo laberinto.json: " + ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Carga el laberinto desde un archivo de guardado específico de usuario en /saves.
     * @param email correo del jugador para seleccionar el archivo correspondiente
     * @return instancia de Laberinto o null si no existe el archivo
     */
    public static Laberinto cargarJson(String email) {
        String projectRoot = System.getProperty("user.dir");
        String safeEmail = (email == null || email.isEmpty()) ? "default" : email.replaceAll("[^A-Za-z0-9._-]", "_");
        File inFile = new File(new File(projectRoot, "saves"), "laberinto-" + safeEmail + ".json");
        if (!inFile.exists()) {
            System.err.println("No se encontró archivo de guardado para el usuario en: " + inFile.getAbsolutePath());
            return null;
        }
        try (FileReader fr = new FileReader(inFile)) {
            JsonObject root = JsonParser.parseReader(fr).getAsJsonObject();
            int x = root.has("x") ? root.get("x").getAsInt() : 0;
            int y = root.has("y") ? root.get("y").getAsInt() : 0;
            Laberinto lab = new Laberinto(x, y, true);

            if (root.has("maze")) {
                JsonArray mazeArray = root.getAsJsonArray("maze");
                for (int i = 0; i < mazeArray.size() && i < lab.maze.length; i++) {
                    JsonArray col = mazeArray.get(i).getAsJsonArray();
                    for (int j = 0; j < col.size() && j < lab.maze[i].length; j++) {
                        JsonObject cellObj = col.get(j).getAsJsonObject();
                        if (cellObj.has("valor")) {
                            lab.maze[i][j].valor = cellObj.get("valor").getAsInt();
                        }
                    }
                }
            }

            if (root.has("jugador")) {
                JsonObject jObj = root.getAsJsonObject("jugador");
                Jugador j = Jugador.fromJson(jObj);
                lab.jugador = j;
                if (j.getPosX() >= 0 && j.getPosY() >= 0 && j.getPosX() < lab.x && j.getPosY() < lab.y) {
                    j.celdaActual = lab.maze[j.getPosX()][j.getPosY()];
                    lab.maze[j.getPosX()][j.getPosY()].addEntidad(j);
                }
            }

            if (root.has("entidades")) {
                JsonArray ents = root.getAsJsonArray("entidades");
                for (JsonElement ee : ents) {
                    JsonObject eo = ee.getAsJsonObject();
                    Entidad entidad = crearEntidadDesdeJson(eo);
                    if (entidad != null) {
                        int px = entidad.getPosX();
                        int py = entidad.getPosY();
                        if (px >= 0 && py >= 0 && px < lab.x && py < lab.y) {
                            lab.maze[px][py].addEntidad(entidad);
                        }
                        if (!(entidad instanceof Jugador)) {
                            lab.entidades.add(entidad);
                        }
                    }
                }
            } else {
                // No entities saved: do not randomly repopulate – keep maze layout only.
                // Ensure there is at least a player in the maze so UI and input work.
                // Entities not present in this save: do NOT regenerate random entities here.
                // Preserve the maze layout exactly and only ensure a player entity exists and is placed.
                if (lab.jugador == null) {
                    lab.jugador = new Jugador("player@example.com", "password");
                    lab.jugador.setPosition(0, 0);
                    lab.jugador.celdaActual = lab.maze[0][0];
                    lab.maze[0][0].addEntidad(lab.jugador);
                } else {
                    // If jugador has valid position, place it in the corresponding cell; otherwise reset to 0,0
                    if (lab.jugador.getPosX() >= 0 && lab.jugador.getPosX() < lab.x && lab.jugador.getPosY() >= 0 && lab.jugador.getPosY() < lab.y) {
                        lab.jugador.celdaActual = lab.maze[lab.jugador.getPosX()][lab.jugador.getPosY()];
                        lab.maze[lab.jugador.getPosX()][lab.jugador.getPosY()].addEntidad(lab.jugador);
                    } else {
                        lab.jugador.setPosition(0,0);
                        lab.jugador.celdaActual = lab.maze[0][0];
                        lab.maze[0][0].addEntidad(lab.jugador);
                    }
                }
            }

            // restore saved elapsed time if present
            if (root.has("tiempoSegundos")) {
                try {
                    lab.tiempoSegundos = root.get("tiempoSegundos").getAsLong();
                } catch (Throwable ignored) {
                }
            }

            return lab;
        } catch (IOException ex) {
            System.err.println("Error leyendo archivo de guardado: " + ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }

    // Ayudante para construir la subclase de Entidad apropiada desde la representación JSON
    /**
     * Crea una instancia de Entidad a partir de su representación JSON.
     * Reconoce Jugador, Enemigo, Trampa, Llave y Puerta por su ASCII o campos característicos.
     * @param eo objeto JSON con los campos de la entidad
     * @return entidad reconstruida o null si no es válida
     */
    private static Entidad crearEntidadDesdeJson(JsonObject eo) {
        if (eo == null) return null;
        // El jugador se maneja por separado
        if (eo.has("correoElectronico") || eo.has("contrasenia")) {
            return Jugador.fromJson(eo);
        }
        char ascii = eo.has("ascii") ? eo.get("ascii").getAsString().charAt(0) : '?';
        // Enemigo (E) -- treated as Trampa to eliminate Enemigo usage
        if (ascii == 'E') {
            // For backwards compatibility treat saved 'E' as a plain Trampa (no movement behavior)
            Trampa t = new Trampa();
            if (eo.has("danio")) {
                try {
                    java.lang.reflect.Field f = Trampa.class.getDeclaredField("danio");
                    f.setAccessible(true);
                    f.setShort(t, (short) eo.get("danio").getAsInt());
                } catch (Exception ignored) {
                }
            }
            if (eo.has("posX") && eo.has("posY")) {
                t.setPosition(eo.get("posX").getAsInt(), eo.get("posY").getAsInt());
            }
            return t;
        }
        // Trampa (T)
        else if (ascii == 'T') {
            Trampa t = new Trampa();
            if (eo.has("danio")) {
                try {
                    java.lang.reflect.Field f = Trampa.class.getDeclaredField("danio");
                    f.setAccessible(true);
                    f.setShort(t, (short) eo.get("danio").getAsInt());
                } catch (Exception ignored) {
                }
            }
            if (eo.has("posX") && eo.has("posY")) {
                t.setPosition(eo.get("posX").getAsInt(), eo.get("posY").getAsInt());
            }
            return t;
        }
        // Llave (K)
        else if (ascii == 'K') {
            Llave k = new Llave();
            if (eo.has("posX") && eo.has("posY")) {
                k.setPosition(eo.get("posX").getAsInt(), eo.get("posY").getAsInt());
            }
            return k;
        }
        // Puerta (X)
        else if (ascii == 'X') {
            Puerta p = new Puerta();
            if (eo.has("posX") && eo.has("posY")) {
                p.setPosition(eo.get("posX").getAsInt(), eo.get("posY").getAsInt());
            }
            return p;
        }
        // Energia (G) - new entity
        else if (ascii == 'G') {
            Energia en = new Energia();
            if (eo.has("posX") && eo.has("posY")) {
                en.setPosition(eo.get("posX").getAsInt(), eo.get("posY").getAsInt());
            }
            return en;
        }
        // Explosive wall (B)
        else if (ascii == 'B') {
            ExplosiveWall bw = new ExplosiveWall();
            if (eo.has("posX") && eo.has("posY")) {
                bw.setPosition(eo.get("posX").getAsInt(), eo.get("posY").getAsInt());
            }
            return bw;
        }
        // default: unknown entity -> generic placeholder with ascii and position
        Entidad e = new Entidad() {
            @Override
            public void interact(Jugador player) {
                // no-op
            }
        };
        e.ascii = ascii;
        if (eo.has("posX") && eo.has("posY")) {
            e.setPosition(eo.get("posX").getAsInt(), eo.get("posY").getAsInt());
        }
        return e;
    }

    /**
     * Maneja el fin de la partida: guarda el estado, actualiza estadísticas y muestra mensajes finales.
     * @param estado código devuelto por el bucle de juego (-1 perdió, 0 ganó, 1 salió)
     * @return true siempre tras finalizar
     */
    private boolean finParida(int estado) {
        ControladorBD.guardar(this);
        // Actualizar estadísticas globales
        try {
            String email = (this.jugador != null) ? this.jugador.getCorreoElectronico() : "default";
            int score = (this.jugador != null) ? this.jugador.getPuntos() : 0;
            switch (estado) {
                case -1 -> Statistics.recordLoss(email, score, this.tiempoSegundos);
                // el jugador murió
                case 0 -> Statistics.recordWin(email, score, this.tiempoSegundos);
                // escapó
                case 1 -> Statistics.recordQuit(email, score, this.tiempoSegundos);
                // el usuario salió
                default -> Statistics.recordQuit(email, score, this.tiempoSegundos);
                // salida genérica
            }
        } catch (Throwable t) {
            System.err.println("No se pudieron actualizar las estadísticas: " + t.getMessage());
        }
        switch (estado) {
            case -1 -> System.out.println("Has perdido todas tus vidas. ¡Juego terminado!");
            case 0 -> System.out.println("¡Felicidades! ¡Has escapado del laberinto!");
            case 1 -> System.out.println("¡Te estaremos esperando! ¡Vuelve pronto!");
            default -> System.out.println("Has salido del juego. ¡Hasta la próxima!");
        }
        return true;
    }

    /**
     * Public wrapper to finish the game from external callers (UI controllers).
     * Calls the internal finParida to persist and update stats.
     * @param estado -1 lost, 0 win, 1 quit
     * @return true if finished successfully
     */
    public boolean finalizarPartida(int estado) {
        return this.finParida(estado);
    }

    /**
     * Inicia el bucle principal del juego, alternando turnos entre el jugador y entidades.
     * @return true cuando el juego finaliza correctamente
     */
    public boolean jugar() {
        boolean fin = false;
        // El bucle se implementa dentro de Jugador.method() y saldrá cuando el jugador presione 'Q'.
        int eJugador = 0;
        this.display();
        while (!fin) {
            eJugador = this.jugador.movimiento(this);
            if (eJugador != 0) {
                ControladorBD.guardar(this);
                fin = true;
                break;
            }
            for (Entidad e : entidades) {
                if (e instanceof Movimiento movimientoEntidad) {
                    movimientoEntidad.movimiento(this);
                }
                try {
                    Thread.sleep(250);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
            if (jugador.celdaActual.cantidadEntidades() > 1) {
                for (Entidad e : jugador.celdaActual.obtenerContenido()) {
                    e.interact(jugador);
                    if (e.ascii == 'K' || e.ascii == 'C' || e.ascii == 'G') {
                        // remove from the current cell and global list to match GUI behavior
                        jugador.celdaActual.removeEntidad(e);
                        entidades.removeElement(e);
                    }
                }
                fin = jugador.isEscapado();
                if (!jugador.taVivo()) {
                    eJugador = -1;
                    fin = true;
                }
            }
        }
        return this.finParida(eJugador);
    }

    /**
     * Dibuja el laberinto actual en la consola.
     */
    public void display() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < y; i++) {
            // crea la pared norte
            for (int j = 0; j < x; j++) {
                if ((maze[j][i].valor & DIR.N.bit) == 0) {
                    sb.append("+---");
                } else {
                    sb.append("+   ");
                }
            }
            sb.append("+").append(System.lineSeparator());
            // crea la pared oeste
            for (int j = 0; j < x; j++) {
                if ((maze[j][i].valor & DIR.W.bit) == 0) {
                    // pared oeste cerrada: imprimir '|', luego un espacio, el carácter de celda y un espacio final => 4 caracteres
                    sb.append("| ").append(maze[j][i].obtenerAscii()).append(" ");
                } else {
                    sb.append("  ").append(maze[j][i].obtenerAscii()).append(" ");
                }
            }
            sb.append("|").append(System.lineSeparator());
        }
        // crea la pared sur
        for (int j = 0; j < x; j++) {
            sb.append("+---");
        }
        sb.append("+").append(System.lineSeparator());

        String out = sb.toString();
        // print to console as before
        Misc.clearScreen();
        System.out.print(out);
        // notify UI listener if present
        if (this.displayListener != null) {
            try {
                this.displayListener.accept(out);
            } catch (Throwable t) {
                // swallow listener errors to avoid breaking game loop
            }
        }
    }

    /**
     * Returns the textual representation of the current maze without printing.
     * Useful for UI components that want to render the maze.
     */
    public String getDisplayString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < y; i++) {
            for (int j = 0; j < x; j++) {
                if ((maze[j][i].valor & DIR.N.bit) == 0) {
                    sb.append("+---");
                } else {
                    sb.append("+   ");
                }
            }
            sb.append("+").append(System.lineSeparator());
            for (int j = 0; j < x; j++) {
                if ((maze[j][i].valor & DIR.W.bit) == 0) {
                    sb.append("| ").append(maze[j][i].obtenerAscii()).append(" ");
                } else {
                    sb.append("  ").append(maze[j][i].obtenerAscii()).append(" ");
                }
            }
            sb.append("|").append(System.lineSeparator());
        }
        for (int j = 0; j < x; j++) {
            sb.append("+---");
        }
        sb.append("+").append(System.lineSeparator());
        return sb.toString();
    }

    public enum DIR {
        N(1, 0, -1), S(2, 0, 1), E(4, 1, 0), W(8, -1, 0);

        // utiliza el inicializador estático para resolver las referencias anticipadas
        static {
            N.opposite = S;
            S.opposite = N;
            E.opposite = W;
            W.opposite = E;
        }

        public final int bit;
        private final int direccionX;
        private final int direccionY;
        private DIR opposite;

        DIR(int bit, int direccionX, int direccionY) {
            this.bit = bit;
            this.direccionX = direccionX;
            this.direccionY = direccionY;
        }
    }

    // Recursive backtracker maze generation (depth-first search)
    private void generateMaze(int celdaX, int celdaY) {
        // create an array of directions and shuffle it to get random traversal order
        DIR[] direccion = DIR.values();
        java.util.List<DIR> dirs = java.util.Arrays.asList(direccion);
        java.util.Collections.shuffle(dirs);

        for (DIR dir : dirs) {
            int vecinoX = celdaX + dir.direccionX;
            int vecinoY = celdaY + dir.direccionY;
            if (between(vecinoX, x) && between(vecinoY, y) && (maze[vecinoX][vecinoY].valor == 0)) {
                // carve passage between current cell and neighbor
                maze[celdaX][celdaY].valor |= dir.bit;
                maze[vecinoX][vecinoY].valor |= dir.opposite.bit;
                // recurse
                generateMaze(vecinoX, vecinoY);
            }
        }
    }

    // Permitir inyectar un Jugador (p. ej., desde un Usuario autenticado) antes de iniciar el juego
    /**
     * Establece el jugador activo en el laberinto colocándolo en la posición inicial (0,0).
     * Si existía un jugador anterior, se elimina de su celda.
     * @param jugador instancia de Jugador a usar en la partida
     */
    public void setJugador(Jugador jugador) {
        // eliminar el jugador anterior de su celda actual si está presente
        if (this.jugador != null && this.jugador.celdaActual != null) {
            this.jugador.celdaActual.removeEntidad(this.jugador);
        }
        this.jugador = jugador;
        if (this.jugador != null) {
            // ubicar en la posición inicial (0,0)
            this.jugador.setPosition(0, 0);
            if (maze != null && maze.length > 0 && maze[0].length > 0) {
                this.jugador.celdaActual = maze[0][0];
                maze[0][0].addEntidad(this.jugador);
            }
        }
    }

    /**
     * Intenta mover una entidad en la dirección indicada si no hay paredes ni límites.
     * @param entidad entidad a mover
     * @param direccion dirección de movimiento
     * @return true si el movimiento se realizó; false en caso contrario
     */
    public boolean movimientoEntidad(Entidad entidad, DIR direccion) {
        int entidadX = entidad.getPosX();
        int entidadY = entidad.getPosY();
        int destinoX = entidadX + direccion.direccionX;
        int destinoY = entidadY + direccion.direccionY;
        if (!between(destinoX, x) || !between(destinoY, y)) {
            return false;
        }

        // si la pared en la dirección está cerrada, no se puede mover
        if ((maze[entidadX][entidadY].valor & direccion.bit) == 0) {
            // Closed wall: allow the player to force a passage if the destination cell contains
            // an ExplosiveWall (acts as a breakable/shortcut). The explosive will interact
            // (deal damage) and then be removed; the wall bit will be opened.
            if (entidad instanceof Jugador jugadorMov) {
                boolean foundExplosive = false;
                for (Entidad e : maze[destinoX][destinoY].obtenerContenido()) {
                    if (e instanceof ExplosiveWall) {
                        foundExplosive = true;
                        // trigger explosion (will apply damage to player)
                        try { e.interact(jugadorMov); } catch (Throwable ignored) {}
                        // remove explosive from the destination cell and from global entities
                        maze[destinoX][destinoY].removeEntidad(e);
                        entidades.removeElement(e);
                        break;
                    }
                }
                if (!foundExplosive) return false;
                // open passage in both cells
                maze[entidadX][entidadY].valor |= direccion.bit;
                maze[destinoX][destinoY].valor |= direccion.opposite.bit;
            } else {
                return false;
            }
        }

        // realizar el movimiento: quitar de la celda actual y agregar a la destino
        maze[entidadX][entidadY].removeEntidad(entidad);
        maze[destinoX][destinoY].addEntidad(entidad);

        // actualizar referencias si es jugador
        if (entidad instanceof Jugador jugadorMov) {
            jugadorMov.celdaActual = maze[destinoX][destinoY];
        }

        entidad.setPosition(destinoX, destinoY);
        return true;
    }

    public int getWidth() {
        return this.x;
    }

    public int getHeight() {
        return this.y;
    }

    /**
     * Return the internal 'valor' bitmask for the cell at (x,y). Caller must ensure bounds.
     */
    public int getCellValue(int cx, int cy) {
        if (cx < 0 || cy < 0 || cx >= this.x || cy >= this.y) return 0;
        return maze[cx][cy].valor;
    }

    /**
     * Return the representative ASCII char for the cell content (delegates to Celda.obternerAscii).
     */
    public char getCellChar(int cx, int cy) {
        if (cx < 0 || cy < 0 || cx >= this.x || cy >= this.y) return ' ';
        return maze[cx][cy].obtenerAscii();
    }

    /**
     * Advance all entities that implement Movimiento by calling their movimiento(this).
     * Uses a snapshot to avoid concurrent-modification when entities remove themselves.
     */
    public void stepEntities() {
        Entidad[] snapshot;
        synchronized (entidades) {
            snapshot = entidades.toArray(new Entidad[0]);
        }
        for (Entidad e : snapshot) {
            if (e instanceof Movimiento movimientoEntidad) {
                try {
                    movimientoEntidad.movimiento(this);
                } catch (Throwable ignored) {
                }
            }
        }
    }

    /**
     * Remove an entity from the global entities list (used by UI when a key/crystal is picked).
     */
    public void removeEntidadGlobal(Entidad e) {
        if (e == null) return;
        entidades.removeElement(e);
    }

    /**
     * Return a snapshot of the global entities currently present in the maze.
     * This is used by the persistence layer to serialize exact entity state/positions.
     */
    public Entidad[] getEntidadesSnapshot() {
        synchronized (entidades) {
            return entidades.toArray(new Entidad[0]);
        }
    }
}
