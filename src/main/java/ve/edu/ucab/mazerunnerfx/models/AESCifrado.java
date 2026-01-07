package ve.edu.ucab.mazerunnerfx.models;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.security.Key;

/**
 * Gestiona operaciones de cifrado y descifrado mediante el algoritmo AES.
 * Provee utilidades estáticas para cifrar y descifrar cadenas en Base64.
 */
public class AESCifrado {
    private static final String Clave = "ClaveUltraSecret";
    private static final String Algoritmo = "AES";
    private static final String AlgoritmoCompleto = "AES/ECB/PKCS5Padding";

    /**
     * Genera la clave simétrica a partir de la cadena privada usando el algoritmo configurado.
     * @return clave criptográfica para operaciones AES
     */
    private static Key getKey(){
        return new SecretKeySpec(Clave.getBytes(),Algoritmo);
    }

    /**
     * Cifra el texto proporcionado usando AES y lo retorna codificado en Base64.
     * @param dato texto plano a cifrar (UTF-8)
     * @return texto cifrado en Base64
     * @throws Exception si ocurre un error de cifrado
     */
    public static String Cifrado(String dato) throws Exception{
        Cipher cipher = Cipher.getInstance(AlgoritmoCompleto);
        cipher.init(Cipher.ENCRYPT_MODE, getKey());
        byte[] datosCifrados = cipher.doFinal(dato.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(datosCifrados);
    }

    /**
     * Descifra un texto en Base64 previamente cifrado con AES.
     * @param datoCifrado cadena en Base64 con el contenido cifrado
     * @return texto plano en UTF-8
     * @throws Exception si hay problemas de decodificación o descifrado
     */
    public static String Descifrado(String datoCifrado) throws Exception{
        String limpiarCadena = datoCifrado.replaceAll("[^a-zA-Z0-9+/=]", "");
        if (limpiarCadena.isEmpty()) {
            throw new IllegalArgumentException("Dato cifrado corrupto o vacío después de la limpieza.");
        }
        Cipher cipher = Cipher.getInstance(AlgoritmoCompleto);
        cipher.init(Cipher.DECRYPT_MODE, getKey());

        byte[] bytesDescifrados = Base64.getDecoder().decode(limpiarCadena);
        byte[] datos = cipher.doFinal(bytesDescifrados);

        return new String(datos, "UTF-8");
    }
}
