package com.example.registrofx;

public class UsuarioCifrado {
    private String usuario;
    private String contrasenia;

    public UsuarioCifrado(String usuario, String contrasenia) {
        this.usuario = usuario;
        this.contrasenia = contrasenia;
    }

    public String getUsuario() { return usuario; }
    public String getContrasenia() { return contrasenia; }
}
