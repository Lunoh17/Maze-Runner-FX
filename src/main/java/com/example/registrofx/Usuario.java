package com.example.registrofx;

public class Usuario{
    private String correo;
    private static final String Email = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";


    public Usuario(){}

    public Usuario(String correo) {
        if(UsuaValidacion(correo) != null) {
            this.correo = UsuaValidacion(correo);
        }
    }

    public String getCorreo() {
        return correo;
    }
    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String UsuaValidacion(String correo){
        if(correo != null){
            if(correo.matches(Email)){
                return correo;
            }else{
                System.out.println("❌ERROR Correo no valido, debe contener una dirrecion de email valida");
                return null;
            }
        }else{
            System.out.println("❌ERROR el Correo no puede ser NULL");
            return null;
        }
    }
}
