package com.example.registrofx;
import java.util.List;

public interface UsuarioRepositorio {
    void guardar(UsuarioCifrado usuario);
    List<UsuarioCifrado> obtenerTodos();
    boolean existe(String correoCifrado);
}