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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Maneja el registro y la persistencia de estadísticas de jugadores.
 * Guarda y muestra partidas ganadas/perdidas, total de partidas y puntaje máximo.
 */
public class Statistics {
    private static final String STATS_FILE_NAME = "stats.json";

    /**
     * Estructura de estadísticas asociadas a un jugador individual.
     */
    public static class PlayerStats {
        public int wins;
        public int losses;
        public int games;
        // caché de conveniencia: ganadas + perdidas
        public int highestScore;

        /**
         * Recalcula el total de partidas jugadas a partir de ganadas y perdidas.
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
     */
    public Map<String, PlayerStats> getPlayers() {
        return players;
    }

    private static File statsFile() {
        String projectRoot = System.getProperty("user.dir");
        File saveDir = new File(projectRoot, "saves");
        return new File(saveDir, STATS_FILE_NAME);
    }

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
            }
            return s;
        } catch (IOException e) {
            System.err.println("No se pudo leer stats.json: " + e.getMessage());
            return new Statistics();
        }
    }

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

    private static PlayerStats getOrCreate(Statistics s, String email) {
        if (email == null || email.isEmpty()) email = "default";
        return s.players.computeIfAbsent(email, k -> new PlayerStats());
    }

    /**
     * Registra una victoria para el usuario e incrementa estadísticas persistidas.
     * @param email identificador del jugador
     * @param score puntaje obtenido en la partida
     */
    public static void recordWin(String email, int score) {
        Statistics s = load();
        PlayerStats ps = getOrCreate(s, email);
        ps.wins += 1;
        if (score > ps.highestScore) ps.highestScore = score;
        ps.recompute();
        save(s);
    }

    /**
     * Registra una derrota para el usuario e incrementa estadísticas persistidas.
     * @param email identificador del jugador
     * @param score puntaje obtenido en la partida
     */
    public static void recordLoss(String email, int score) {
        Statistics s = load();
        PlayerStats ps = getOrCreate(s, email);
        ps.losses += 1;
        if (score > ps.highestScore) ps.highestScore = score;
        ps.recompute();
        save(s);
    }

    // Opcional: para una salida voluntaria puede decidir no contarla como partida.
    /**
     * Actualiza el puntaje máximo si el usuario abandona la partida sin concluir.
     * @param email identificador del jugador
     * @param score puntaje alcanzado
     */
    public static void recordQuit(String email, int score) {
        // Actualmente, no alteramos ganadas/perdidas/partidas por una salida.
        Statistics s = load();
        PlayerStats ps = getOrCreate(s, email);
        if (score > ps.highestScore) ps.highestScore = score;
        save(s);
    }

    /**
     * Imprime en consola el resumen de estadísticas de todos los jugadores conocidos.
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
                }
            } catch (Throwable ignored) {
                // ignorar archivos de guardado malformados
            }
        }
        // asegurar que todos tengan 'games' recomputado
        for (PlayerStats ps : into.values()) {
            ps.recompute();
        }
    }
}
