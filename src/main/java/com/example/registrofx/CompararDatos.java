package com.example.registrofx;

public class CompararDatos extends RegistroArchivo{
    private Usuario usuario;
    private Contrasenia contrasenia;

    public CompararDatos(Usuario usuario){
        this.usuario = usuario;
    }
    public CompararDatos(Usuario usuario, Contrasenia contrasenia){
        this.usuario = usuario;
        this.contrasenia = contrasenia;
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

    public boolean EnviarDatosRegistro(){
        String AuxUsuario = usuario.getCorreo();
        String AuxContrasenia = contrasenia.getContrasenia();
        if(this.BuscarArchivoRegistro(AuxUsuario)){
            return true;
        }else{
            return false;
        }
    }
    public boolean EnviarDatosSesion(){
        String AuxUsuario = usuario.getCorreo();
        String AuxContrasenia = contrasenia.getContrasenia();
        if(this.BuscarArchivoSesion(AuxUsuario,AuxContrasenia)){
            return true;
        }else{
            return false;
        }
    }
    public String EnviarDatosRecuperacion() {
        // 'usuario.getCorreo()' obtiene el email que el usuario escribió en la interfaz
        // 'obtenerContraseniaRecuperada' es el método que está en la clase padre (RegistroArchivo)
        return this.obtenerContraseniaRecuperada(this.usuario.getCorreo());
    }


}
