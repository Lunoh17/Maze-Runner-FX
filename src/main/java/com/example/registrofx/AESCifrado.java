package com.example.registrofx;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.security.Key;

public class AESCifrado {
    private static final String Clave = "ClaveUltraSecret";
    private static final String Algoritmo = "AES";
    private static final String AlgoritmoCompleto = "AES/ECB/PKCS5Padding";

    private static Key getKey(){
        return new SecretKeySpec(Clave.getBytes(),Algoritmo);
    }

    public static String Cifrado(String dato) throws Exception{
        Cipher cipher = Cipher.getInstance(AlgoritmoCompleto);
        cipher.init(Cipher.ENCRYPT_MODE, getKey());
        byte[] datosCifrados = cipher.doFinal(dato.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(datosCifrados);
    }

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
