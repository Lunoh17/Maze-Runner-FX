package com.example.registrofx;

import java.io.File;

public class GuardarDatos extends RegistroArchivo{
    private Usuario usuario;
    private Contrasenia contrasenia;

    public GuardarDatos(Usuario usuario, Contrasenia contrasenia) {
        if(usuario != null && contrasenia != null){
            this.usuario = usuario;
            this.contrasenia = contrasenia;
        }
    }
    public Usuario getUsuario() {
        return usuario;
    }
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
    public Contrasenia getContrasenia() {
        return contrasenia;
    }
    public void setContrasenia(Contrasenia contrasenia) {
        this.contrasenia = contrasenia;
    }
    public String FormatoRegistro(){
        if(usuario != null && contrasenia != null){
            return usuario.getCorreo()+":"+contrasenia.getContrasenia();
        }
        return null;
    }

    public void GuardarFormatoRegistro(){
        String linea = FormatoRegistro();
        if(linea != null){
            this.GuardarArchivo(linea);
        }else{
            System.out.println("Error al Guardar datos son incompletos/inválidos.");
        }
    }
}
