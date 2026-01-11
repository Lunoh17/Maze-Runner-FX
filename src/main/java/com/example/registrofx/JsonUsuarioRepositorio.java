package com.example.registrofx;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class JsonUsuarioRepositorio implements UsuarioRepositorio {
    private static JsonUsuarioRepositorio instancia;
    private final String PATH = "Registro-Login.json";
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private JsonUsuarioRepositorio() {} // Privado para Singleton

    public static JsonUsuarioRepositorio getInstancia() {
        if (instancia == null) instancia = new JsonUsuarioRepositorio();
        return instancia;
    }

    @Override
    public List<UsuarioCifrado> obtenerTodos() {
        try (Reader reader = new FileReader(PATH)) {
            return gson.fromJson(reader, new TypeToken<List<UsuarioCifrado>>(){}.getType());
        } catch (IOException e) { return new ArrayList<>(); }
    }

    @Override
    public void guardar(UsuarioCifrado dto) {
        List<UsuarioCifrado> lista = obtenerTodos();
        lista.add(dto);
        try (Writer writer = new FileWriter(PATH)) {
            gson.toJson(lista, writer);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @Override
    public boolean existe(String correoCifrado) {
        return obtenerTodos().stream().anyMatch(u -> u.getUsuario().equals(correoCifrado));
    }
}