package com.example.registrofx;

public class Contrasenia{
    private String contrasenia;
    private String confirContrasenia;
    private static final String Pass = "^(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?])(?=.*[A-Z]).{6,}$";

    public Contrasenia(){}
    public Contrasenia(String contrasenia){
        if(PassValidacion(contrasenia,contrasenia)){
            this.contrasenia = contrasenia;
        }
    }
    public Contrasenia(String contrasenia, String confirContrasenia) {
        if(PassValidacion(contrasenia,confirContrasenia)){
            this.contrasenia = contrasenia;
            this.confirContrasenia = confirContrasenia;
        }
    }

    public String getContrasenia() {
        return contrasenia;
    }
    public void setContrasenia(String contrasenia) {
        this.contrasenia = contrasenia;
    }
    public String getConfirContrasenia() {
        return confirContrasenia;
    }
    public void setConfirContrasenia(String confirContrasenia) {
        this.confirContrasenia = confirContrasenia;
    }

    public boolean PassValidacion(String contrasenia, String confirContrasenia){
        if(contrasenia.equals(confirContrasenia)){
            if(contrasenia.matches(Pass)){
                return true;
            }else{
                System.out.println("❌ERROR la Contraseña debe tener un mínimo 6 caracteres, un carácter en mayúscula y un carácter especial");
                return false;
            }
        }else{
            System.out.println("-----------------------------------------------------");
            System.out.println("❌ERROR Contraseña y Confirmacion deben coincidir");
            return false;
        }
    }
}
