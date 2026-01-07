package ve.edu.ucab.mazerunnerfx.models;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Controla la persistencia del estado del laberinto en archivos JSON.
 * Administra la ruta de guardado por usuario y maneja la escritura en UTF-8.
 */
public class ControladorBD {
    /**
     * Serializa y guarda el estado del laberinto en un archivo JSON por usuario.
     * @param laberinto instancia a persistir
     */
    public static void guardar(Laberinto laberinto){
        Gson gson = new Gson();
//        laberinto.jugador.celdaActual=null; // Avoid circular reference during serialization
//        Traducción: Evitar referencia circular durante la serialización
        // Determine project root and target JSON file per user
        // Traducción: Determinar la raíz del proyecto y el archivo JSON objetivo por usuario
        String projectRoot = System.getProperty("user.dir");
        String email = (laberinto != null && laberinto.jugador != null && laberinto.jugador.getCorreoElectronico() != null)
                ? laberinto.jugador.getCorreoElectronico()
                : "default";
        String safeEmail = sanitizeForFile(email);
        File saveDir = new File(projectRoot, "saves");
        File outFile = new File(saveDir, "laberinto-" + safeEmail + ".json");

        // Ensure parent directories exist
        // Traducción: Asegurar que los directorios padre existan
        File parent = outFile.getParentFile();
        if (parent != null && !parent.exists()) {
            boolean created = parent.mkdirs();
            if (!created) {
                System.err.println("Warning: could not create directory: " + parent.getAbsolutePath());
                // Traducción: Advertencia: no se pudo crear el directorio
            }
        }

        // Write JSON using UTF-8
        // Traducción: Escribir JSON usando UTF-8
        try (FileOutputStream fos = new FileOutputStream(outFile);
             Writer osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             JsonWriter writer = new JsonWriter(osw)) {

            gson.toJson(laberinto, Laberinto.class, writer);
            writer.flush();
            System.out.println("Juego guardado en: " + outFile.getAbsolutePath());

        } catch (IOException e) {
            // Print error so caller can see what happened
            // Traducción: Imprimir el error para que el llamador pueda ver qué ocurrió
            System.err.println("Error saving laberinto to " + outFile.getAbsolutePath());
            // Traducción: Error al guardar el laberinto en
            e.printStackTrace();
        }
    }

    /**
     * Sanitiza una cadena para uso seguro como nombre de archivo en Windows.
     * Reemplaza caracteres problemáticos por guiones bajos.
     * @param input correo o identificador original
     * @return versión segura para nombre de archivo
     */
    private static String sanitizeForFile(String input) {
        // Replace characters that are problematic in Windows filenames with '_'
        // Keep alphanumerics, dot, dash, underscore; replace others
        // Traducción: Reemplazar caracteres problemáticos en nombres de archivo de Windows por '_'
        // Traducción: Mantener alfanuméricos, punto, guion, guion bajo; reemplazar otros
        if (input == null || input.isEmpty()) return "default";
        return input.replaceAll("[^A-Za-z0-9._-]", "_");
    }
}
