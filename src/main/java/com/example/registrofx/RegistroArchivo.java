package com.example.registrofx;

import java.util.Scanner;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Type;

public abstract class RegistroArchivo {
    private static final String nombreArchivo = "Registro-Login.json";
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private List<UsuarioCifrado> leerTodos() {
        try (Reader reader = new FileReader(nombreArchivo)) {
            Type listType = new TypeToken<ArrayList<UsuarioCifrado>>(){}.getType();
            List<UsuarioCifrado> lista = gson.fromJson(reader, listType);
            return (lista != null) ? lista : new ArrayList<>();
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }
    public class UsuarioCifrado {
        String usuario;
        String contrasenia;

        public UsuarioCifrado(String usuario, String contrasenia) {
            this.usuario = usuario;
            this.contrasenia = contrasenia;
        }
    }
    public void GuardarArchivo(String palabra) {
        try {
            String[] partes = palabra.split(":");
            String uCifrado = AESCifrado.Cifrado(partes[0]);
            String cCifrado = AESCifrado.Cifrado(partes[1]);

            List<UsuarioCifrado> usuarios = leerTodos();

            usuarios.add(new UsuarioCifrado(uCifrado, cCifrado));

            try (Writer writer = new FileWriter(nombreArchivo)) {
                gson.toJson(usuarios, writer);
            }
        } catch (Exception e) {
            System.out.println("❌ Error al guardar con Gson: " + e.getMessage());
        }
    }

    public boolean BuscarArchivoRegistro(String usuario) {
        List<UsuarioCifrado> usuarios = leerTodos();
        for (UsuarioCifrado uc : usuarios) {
            try {
                if (AESCifrado.Descifrado(uc.usuario).equals(usuario)) {
                    System.out.println("Este Usuario Ya se encuentra Registrado");
                    return true;
                }
            } catch (Exception e) { }
        }
        return false;
    }

    public boolean BuscarArchivoSesion(String usuario, String contrasenia) {
        List<UsuarioCifrado> usuarios = leerTodos();

        for (UsuarioCifrado uc : usuarios) {
            try {
                String auxUserDesc = AESCifrado.Descifrado(uc.usuario);

                if (auxUserDesc.equals(usuario)) {
                    String auxPassDesc = AESCifrado.Descifrado(uc.contrasenia);
                    if (auxPassDesc.equals(contrasenia)) {
                        return true;
                    } else {
                        System.out.println("-----------------------------------------------------");
                        System.out.println("❌ Contraseña Incorrecta");
                        System.out.println("¿Deseas Recuperar Contraseña?");
                        System.out.println("1. SI");
                        System.out.println("0. NO");

                        Scanner leeropc = new Scanner(System.in);
                        int opc = 0;
                        try {
                            opc = leeropc.nextInt();
                        } catch (Exception e) {
                            System.out.println("Opción no válida.");
                        }

                        if (opc == 1) {
                            System.out.println("-----------------------------------------------------");
                            System.out.println("Recuperación de Usuario:Contraseña -> " + auxUserDesc + ":" + auxPassDesc);
                            return true;
                        } else {
                            return false;
                        }
                        // ---------------------------------------------
                    }
                }
            } catch (Exception e) {
            }
        }
        return false;
    }
    public String obtenerContraseniaRecuperada(String usuario) {
        List<UsuarioCifrado> usuarios = leerTodos();

        for (UsuarioCifrado uc : usuarios) {
            try {
                String auxUsuarioDescifrado = AESCifrado.Descifrado(uc.usuario);
                if (auxUsuarioDescifrado.equals(usuario)) {
                    return AESCifrado.Descifrado(uc.contrasenia);
                }
            } catch (Exception e) {
                System.err.println("❌ Error al descifrar un registro: " + e.getMessage());
            }
        }

        System.out.println("-----------------------------------------------------");
        System.out.println("❌ El usuario no existe en nuestros registros.");
        System.out.println("-----------------------------------------------------");
        return null;
    }

}