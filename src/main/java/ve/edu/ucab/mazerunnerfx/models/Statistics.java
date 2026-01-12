package ve.edu.ucab.mazerunnerfx.models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Clase para manejar estadísticas de juego y del jugador.
 *
 * <p>Proporciona métodos para registrar y recuperar métricas relevantes de las partidas.</p>
 *
 * @author Equipo
 * @version 2026-01-12
 */
public class Statistics {
    private static final String STATS_FILE_NAME = "stats.json";

    /**
     * Registro de una partida individual para historial.
     */
    public static class GameRecord {
        public long timestampMillis;
        public long durationSeconds;
        public int score;
        public String result; // "WIN", "LOSS", "QUIT"

        public GameRecord() {}

        public GameRecord(long timestampMillis, long durationSeconds, int score, String result) {
            this.timestampMillis = timestampMillis;
            this.durationSeconds = durationSeconds;
            this.score = score;
            this.result = result;
        }

        /**
         * Devuelve la marca de tiempo en milisegundos de la partida.
         * @return timestamp en milisegundos
         * @since 2026-01-12
         */
        public long getTimestampMillis() { return timestampMillis; }

        /**
         * Devuelve la duración de la partida en segundos.
         * @return duración en segundos
         * @since 2026-01-12
         */
        public long getDurationSeconds() { return durationSeconds; }

        /**
         * Devuelve el puntaje obtenido en la partida.
         * @return puntaje (score)
         * @since 2026-01-12
         */
        public int getScore() { return score; }

        /**
         * Devuelve el resultado de la partida (por ejemplo "WIN" o "LOSS").
         * @return resultado como cadena
         * @since 2026-01-12
         */
        public String getResult() { return result; }

        /**
         * Obtiene la fecha formateada a partir del timestamp.
         * @return fecha legible en formato yyyy-MM-dd HH:mm:ss
         * @since 2026-01-12
         */
        public String getFormattedDate() {
            Date d = new Date(timestampMillis);
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(d);
        }

        /**
         * Devuelve la duración en formato mm:ss.
         * @return duración formateada como cadena
         * @since 2026-01-12
         */
        public String getFormattedDuration() {
            long mins = durationSeconds / 60;
            long secs = durationSeconds % 60;
            return String.format("%02d:%02d", mins, secs);
        }
    }

    /**
     * Estructura de estadísticas asociadas a un jugador individual.
     */
    public static class PlayerStats {
        public int wins;
        public int losses;
        public int games;
        // caché de conveniencia: ganadas + perdidas
        public int highestScore;
        // historial de partidas (más reciente al final)
        public List<GameRecord> records = new ArrayList<>();

        /**
         * Recalcula el total de partidas jugadas a partir de ganadas y perdidas.
         *
         * @since 2026-01-12
         */
        public void recompute() {
            this.games = this.wins + this.losses;
        }
    }

    // En archivo se representa como un mapa simple email -> PlayerStats
    private Map<String, PlayerStats> players = new HashMap<>();

    /**
     * Devuelve el mapa de estadísticas por jugador.
     * @return mapa email -> PlayerStats
     * @since 2026-01-12
     */
    public Map<String, PlayerStats> getPlayers() {
        return players;
    }

    /**
     * Obtiene el archivo de estadísticas dentro del directorio `saves` del proyecto.
     *
     * @return referencia al archivo de estadísticas
     * @since 2026-01-12
     */
    private static File statsFile() {
        String projectRoot = System.getProperty("user.dir");
        File saveDir = new File(projectRoot, "saves");
        return new File(saveDir, STATS_FILE_NAME);
    }

    /**
     * Carga las estadísticas persistidas desde disco o crea una instancia vacía si no existe el archivo.
     *
     * @return instancia de {@link Statistics} con los datos cargados
     * @since 2026-01-12
     */
    private static synchronized Statistics load() {
        File f = statsFile();
        if (!f.exists()) {
            return new Statistics();
        }
        try (Reader reader = new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8)) {
            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, PlayerStats>>(){}.getType();
            Map<String, PlayerStats> map = gson.fromJson(reader, type);
            Statistics s = new Statistics();
            if (map != null) s.players.putAll(map);
            // asegurar que el valor en caché de partidas sea consistente
            for (PlayerStats ps : s.players.values()) {
                ps.recompute();
                if (ps.records == null) ps.records = new ArrayList<>();
            }
            return s;
        } catch (IOException e) {
            System.err.println("No se pudo leer stats.json: " + e.getMessage());
            return new Statistics();
        }
    }

    /**
     * Persiste las estadísticas en disco (archivo JSON en `saves/stats.json`).
     *
     * @param s instancia de estadísticas a guardar
     * @since 2026-01-12
     */
    private static synchronized void save(Statistics s) {
        File f = statsFile();
        File parent = f.getParentFile();
        if (parent != null && !parent.exists()) {
            //noinspection ResultOfMethodCallIgnored
            parent.mkdirs();
        }
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            // escribir solo el mapa para mantener el archivo simple
            gson.toJson(s.players, writer);
            writer.flush();
        } catch (IOException e) {
            System.err.println("No se pudo guardar stats.json: " + e.getMessage());
        }
    }

    /**
     * Obtiene o crea las estadísticas de un jugador dentro de la instancia dada.
     *
     * @param s instancia de {@link Statistics} donde buscar/crear
     * @param email identificador del jugador
     * @return objeto {@link PlayerStats} existente o recién creado
     * @since 2026-01-12
     */
    private static PlayerStats getOrCreate(Statistics s, String email) {
        if (email == null || email.isEmpty()) email = "default";
        return s.players.computeIfAbsent(email, k -> new PlayerStats());
    }

    /**
     * Registra una victoria para el usuario e incrementa estadísticas persistidas.
     * @param email identificador del jugador
     * @param score puntaje obtenido en la partida
     * @param durationSeconds duración de la partida en segundos
     */
    public static void recordWin(String email, int score, long durationSeconds) {
        Statistics s = load();
        PlayerStats ps = getOrCreate(s, email);
        ps.wins += 1;
        if (score > ps.highestScore) ps.highestScore = score;
        ps.recompute();
        ps.records.add(new GameRecord(System.currentTimeMillis(), durationSeconds, score, "WIN"));
        save(s);
    }

    /**
     * Registra una derrota para el usuario e incrementa estadísticas persistidas.
     * @param email identificador del jugador
     * @param score puntaje obtenido en la partida
     * @param durationSeconds duración de la partida en segundos
     */
    public static void recordLoss(String email, int score, long durationSeconds) {
        Statistics s = load();
        PlayerStats ps = getOrCreate(s, email);
        ps.losses += 1;
        if (score > ps.highestScore) ps.highestScore = score;
        ps.recompute();
        ps.records.add(new GameRecord(System.currentTimeMillis(), durationSeconds, score, "LOSS"));
        save(s);
    }

    // Opcional: para una salida voluntaria puede decidir no contarla como partida.
    /**
     * Actualiza el puntaje máximo si el usuario abandona la partida sin concluir.
     * @param email identificador del jugador
     * @param score puntaje alcanzado
     * @param durationSeconds duración de la partida en segundos
     * @since 2026-01-12
     */
    public static void recordQuit(String email, int score, long durationSeconds) {
        // Actualmente, no alteramos ganadas/perdidas/partidas por una salida.
        Statistics s = load();
        PlayerStats ps = getOrCreate(s, email);
        if (score > ps.highestScore) ps.highestScore = score;
        ps.records.add(new GameRecord(System.currentTimeMillis(), durationSeconds, score, "QUIT"));
        save(s);
    }

    /**
     * Devuelve el historial de partidas para un usuario dado (orden original: más antiguo -> más reciente).
     * @param email correo del usuario
     * @return lista de GameRecord (vacía si no hay datos)
     * @since 2026-01-12
     */
    public static List<GameRecord> getRecordsForUser(String email) {
        Statistics s = load();
        PlayerStats ps = s.players.get((email == null || email.isEmpty()) ? "default" : email);
        if (ps == null || ps.records == null) return new ArrayList<>();
        return new ArrayList<>(ps.records);
    }

    /**
     * Imprime en consola el resumen de estadísticas de todos los jugadores conocidos.
     *
     * @since 2026-01-12
     */
    public static void printAll() {
        // Cargar estadísticas persistidas
        Statistics s = load();
        // Descubrir jugadores desde archivos de guardado y enriquecer la vista (sin persistir)
        Map<String, PlayerStats> combined = new HashMap<>(s.players);
        seedFromSaves(combined);

        if (combined.isEmpty()) {
            System.out.println("No hay estadísticas para mostrar.");
            return;
        }

        List<Map.Entry<String, PlayerStats>> rows = new ArrayList<>(combined.entrySet());
        // ordenar por email
        rows.sort(Comparator.comparing(Map.Entry::getKey, String.CASE_INSENSITIVE_ORDER));

        // Presentación en columnas
        String header = String.format("%-35s %8s %8s %8s %14s", "Usuario", "Ganadas", "Perdidas", "Partidas", "Puntaje Max");
        System.out.println("================= ESTADÍSTICAS DE JUGADORES =================");
        System.out.println(header);
        System.out.println("-".repeat(header.length()));
        for (Map.Entry<String, PlayerStats> e : rows) {
            PlayerStats ps = e.getValue();
            int games = ps.games;
            // ya recomputado donde aplica
            System.out.printf("%-35s %8d %8d %8d %14d%n", e.getKey(), ps.wins, ps.losses, games, ps.highestScore);
        }
        System.out.println("=============================================================");
    }

    /**
     * Busca en la carpeta de saves archivos tipo "laberinto-*.json" y extrae datos básicos
     * para enriquecer las estadísticas mostradas (no persiste cambios en disco).
     *
     * @param into mapa donde sembrar la información
     * @since 2026-01-12
     */
    private static void seedFromSaves(Map<String, PlayerStats> into) {
        String projectRoot = System.getProperty("user.dir");
        File saveDir = new File(projectRoot, "saves");
        if (!saveDir.exists() || !saveDir.isDirectory()) return;
        File[] files = saveDir.listFiles((dir, name) -> name != null && name.startsWith("laberinto-") && name.endsWith(".json"));
        if (files == null) return;
        for (File f : files) {
            try (Reader reader = new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8)) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                if (root.has("jugador")) {
                    JsonObject jObj = root.getAsJsonObject("jugador");
                    String email = jObj.has("correoElectronico") ? jObj.get("correoElectronico").getAsString() : "default";
                    int puntos = jObj.has("puntos") ? jObj.get("puntos").getAsInt() : 0;
                    PlayerStats ps = into.computeIfAbsent(email, k -> new PlayerStats());
                    // no cambiar ganadas/perdidas/partidas; solo sembrar puntaje máximo para visibilidad
                    if (puntos > ps.highestScore) ps.highestScore = puntos;
                    // seed a simple record if tiempoSegundos exists (for visibility)
                    if (root.has("tiempoSegundos")) {
                        long t = root.get("tiempoSegundos").getAsLong();
                        ps.records.add(new GameRecord(0L, t, puntos, "UNKNOWN"));
                    }
                }
            } catch (Throwable ignored) {
                // ignorar archivos de guardado malformados
            }
        }
        // asegurar que todos tengan 'games' recomputado
        for (PlayerStats ps : into.values()) {
            ps.recompute();
            if (ps.records == null) ps.records = new ArrayList<>();
        }
    }
}
