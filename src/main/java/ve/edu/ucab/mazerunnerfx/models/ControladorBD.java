package ve.edu.ucab.mazerunnerfx.models;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Controla la persistencia del estado del laberinto en archivos JSON.
 * Escribe un formato simplificado: { x, y, maze:[[ {valor} ... ], ... ], jugador, tiempoSegundos }
 * Esto evita problemas de serialización con referencias circulares y mantiene el formato legible.
 */
public class ControladorBD {

    private static File makeSaveFile(File base, String safeEmail) {
        File saveDir = new File(base, "saves");
        return new File(saveDir, "laberinto-" + safeEmail + ".json");
    }

    /**
     * Build a simplified JsonObject representing a Laberinto: x,y,maze (2D array of valor), jugador minimal, tiempoSegundos
     */
    private static JsonObject buildSimpleJson(Laberinto lab) {
        JsonObject root = new JsonObject();
        if (lab == null) return root;
        root.addProperty("x", lab.getWidth());
        root.addProperty("y", lab.getHeight());
        JsonArray mazeArr = new JsonArray();
        for (int i = 0; i < lab.getWidth(); i++) {
            JsonArray col = new JsonArray();
            for (int j = 0; j < lab.getHeight(); j++) {
                JsonObject cell = new JsonObject();
                cell.addProperty("valor", lab.getCellValue(i, j));
                col.add(cell);
            }
            mazeArr.add(col);
        }
        root.add("maze", mazeArr);

        if (lab.jugador != null) {
            JsonObject j = new JsonObject();
            try {
                j.addProperty("correoElectronico", lab.jugador.getCorreoElectronico());
            } catch (Throwable ignored) {}
            try {
                j.addProperty("contrasenia", lab.jugador.getContrasenia());
            } catch (Throwable ignored) {}
            try {
                j.addProperty("posX", lab.jugador.getPosX());
                j.addProperty("posY", lab.jugador.getPosY());
            } catch (Throwable ignored) {}
            try {
                j.addProperty("puntos", lab.jugador.getPuntos());
            } catch (Throwable ignored) {}
            try {
                int[] vidas = lab.jugador.getVidasArray();
                if (vidas != null) {
                    JsonArray va = new JsonArray();
                    for (int v : vidas) va.add(v);
                    j.add("vidas", va);
                }
            } catch (Throwable ignored) {}
            try {
                j.addProperty("llaves", lab.jugador.getLlaves());
            } catch (Throwable ignored) {}
            root.add("jugador", j);
        }

        try {
            root.addProperty("tiempoSegundos", lab.tiempoSegundos);
        } catch (Throwable ignored) {}

        return root;
    }

    /**
     * Serializa y guarda el estado del laberinto en un archivo JSON por usuario.
     * Tries: project root/saves, user home/.mazerunnerfx/saves, then temp fallback.
     * Returns the File written on success, or null on failure.
     */
    public static File guardar(Laberinto laberinto){
        if (laberinto == null) return null;
        Gson gson = new Gson();
        String email = (laberinto != null && laberinto.jugador != null && laberinto.jugador.getCorreoElectronico() != null)
                ? laberinto.jugador.getCorreoElectronico()
                : "default";
        String safeEmail = sanitizeForFile(email);

        // locations to try, in order
        File projectRoot = new File(System.getProperty("user.dir"));
        File userHomeBase = new File(System.getProperty("user.home"), ".mazerunnerfx");

        File[] candidates = new File[] {
                makeSaveFile(projectRoot, safeEmail),
                makeSaveFile(userHomeBase, safeEmail)
        };

        JsonObject root = buildSimpleJson(laberinto);

        // Try primary locations
        for (File outFile : candidates) {
            try {
                File parent = outFile.getParentFile();
                if (parent != null && !parent.exists()) parent.mkdirs();
                try (FileOutputStream fos = new FileOutputStream(outFile);
                     Writer osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
                     JsonWriter writer = new JsonWriter(osw)) {
                    gson.toJson(root, writer);
                    writer.flush();
                    System.out.println("Juego guardado en: " + outFile.getAbsolutePath());
                    return outFile;
                }
            } catch (Throwable e) {
                System.err.println("Saving to " + outFile.getAbsolutePath() + " failed: " + e.getMessage());
                // try next candidate
            }
        }

        // Primary locations failed: try temp fallback
        try {
            String tmpRoot = System.getProperty("java.io.tmpdir");
            File tmpBase = new File(tmpRoot, "mazerunnerfx_saves");
            if (!tmpBase.exists()) tmpBase.mkdirs();
            File tmpOut = new File(tmpBase, "laberinto-" + safeEmail + ".json");
            try (FileOutputStream fos2 = new FileOutputStream(tmpOut);
                 Writer osw2 = new OutputStreamWriter(fos2, StandardCharsets.UTF_8);
                 JsonWriter writer2 = new JsonWriter(osw2)) {
                gson.toJson(root, writer2);
                writer2.flush();
                System.out.println("(Fallback) Juego guardado en: " + tmpOut.getAbsolutePath());
                return tmpOut;
            }
        } catch (Throwable e2) {
            System.err.println("Fallback save also failed: " + e2.getMessage());
            e2.printStackTrace();
            return null;
        }
    }

    /**
     * Force save to a specific base directory (creates base/saves and writes laberinto-<safeEmail>.json).
     * Returns the File written on success, or null on failure.
     */
    public static File guardarToBase(Laberinto laberinto, File base) {
        if (laberinto == null || base == null) return null;
        Gson gson = new Gson();
        String email = (laberinto.jugador != null && laberinto.jugador.getCorreoElectronico() != null)
                ? laberinto.jugador.getCorreoElectronico()
                : "default";
        String safeEmail = sanitizeForFile(email);
        File outFile = makeSaveFile(base, safeEmail);

        JsonObject root = buildSimpleJson(laberinto);

        try {
            File parent = outFile.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();
            try (FileOutputStream fos = new FileOutputStream(outFile);
                 Writer osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
                 JsonWriter writer = new JsonWriter(osw)) {
                gson.toJson(root, writer);
                writer.flush();
                System.out.println("(Forced) Juego guardado en: " + outFile.getAbsolutePath());
                return outFile;
            }
        } catch (Throwable t) {
            System.err.println("Forced save to " + outFile.getAbsolutePath() + " failed: " + t.getMessage());
            t.printStackTrace();
            return null;
        }
    }

    /**
     * Return the existing save file for the given email if found in any known location.
     * Checks project root/saves, user home/.mazerunnerfx/saves, then temp.
     */
    public static File getExistingSaveFileForEmail(String email) {
        String safeEmail = (email == null || email.isEmpty()) ? "default" : sanitizeForFile(email);
        File projectRoot = new File(System.getProperty("user.dir"));
        File userHomeBase = new File(System.getProperty("user.home"), ".mazerunnerfx");
        File tmpBase = new File(System.getProperty("java.io.tmpdir"), "mazerunnerfx_saves");

        File[] candidates = new File[] {
                makeSaveFile(projectRoot, safeEmail),
                makeSaveFile(userHomeBase, safeEmail),
                new File(tmpBase, "laberinto-" + safeEmail + ".json")
        };
        for (File f : candidates) {
            if (f.exists()) return f;
        }
        return null;
    }

    /**
     * Sanitiza una cadena para uso seguro como nombre de archivo en Windows.
     */
    private static String sanitizeForFile(String input) {
        if (input == null || input.isEmpty()) return "default";
        return input.replaceAll("[^A-Za-z0-9._-]", "_");
    }

    /**
     * Returns the primary project-root save file path (useful to show attempted path).
     */
    public static File getSaveFileForEmail(String email) {
        String safeEmail = (email == null || email.isEmpty()) ? "default" : sanitizeForFile(email);
        File projectRoot = new File(System.getProperty("user.dir"));
        return makeSaveFile(projectRoot, safeEmail);
    }

    /**
     * Write the Laberinto JSON to an arbitrary file path (creates parent directory if needed).
     * Returns the File on success, or null on failure.
     */
    public static File writeJsonToFile(Laberinto laberinto, File outFile) {
        if (laberinto == null || outFile == null) return null;
        Gson gson = new Gson();
        JsonObject root = buildSimpleJson(laberinto);
        try {
            File parent = outFile.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();
            try (FileOutputStream fos = new FileOutputStream(outFile);
                 Writer osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
                 JsonWriter writer = new JsonWriter(osw)) {
                gson.toJson(root, writer);
                writer.flush();
                System.out.println("Escrito JSON genérico en: " + outFile.getAbsolutePath());
                return outFile;
            }
        } catch (Throwable t) {
            System.err.println("Failed to write JSON to arbitrary file: " + outFile.getAbsolutePath() + ": " + t.getMessage());
            t.printStackTrace();
            return null;
        }
    }
}
