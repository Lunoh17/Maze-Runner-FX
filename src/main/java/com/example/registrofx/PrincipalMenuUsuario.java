package com.example.registrofx;
import java.util.Scanner;
// Antiguo Menu de Opciones
public class PrincipalMenuUsuario {
    /*public static void main(String[] args){
        MenuInicio();
    }

    public static void MenuInicio(){
        Scanner leer = new Scanner(System.in);
        int opciones;
        System.out.println("Bienvenido al Juego Maze Runner");
        System.out.println("-------------------------------");
        do{
            System.out.println("1. Registrarse");
            System.out.println("2. Iniciar Sesion");
            System.out.println("3. Recuperar Contraseña");
            System.out.println("0. Salir");
            opciones = leer.nextInt();
            switch (opciones){
                case(1):
                    MenuRegistro(leer);
                    break;
                case(2):
                    MenuSesion(leer);
                    break;
                case(3):
                    MenuRecuperar(leer);
                    break;
                case(0):
                    break;
                default:
                    System.out.println("Opcion Erronea");
            }
        }while(opciones!=0);
        leer.close();
    }

    public static void MenuRegistro(Scanner leer){
        Usuario AuxUsuario = new Usuario();
        Contrasenia AuxContrasenia = new Contrasenia();
        System.out.println("-----------------------Registro----------------------");
        System.out.println("-----------------------------------------------------");
        do{
            System.out.println("-Ingrese Correo (ejemPlo@dominio.com)");
            System.out.println("-----------------------------------------------------");
            Usuario usuario = new Usuario(leer.next());
            if(usuario.getCorreo() != null){
                AuxUsuario = usuario;
            }else{
                AuxUsuario = null;
            }
        }while(AuxUsuario == null);
        do{
            System.out.println("-----------------------------------------------------");
            System.out.println("-Ingrese Contraseña y Su Confirmacion de Contraseña ");
            Contrasenia contrasenia = new Contrasenia(leer.next(),leer.next());
            if(contrasenia.getContrasenia() != null){
                AuxContrasenia = contrasenia;
                System.out.println("-----------------------------------------------------");
            }else{
                AuxContrasenia = null;
            }
        }while(AuxContrasenia == null);
        CompararDatos comparar = new CompararDatos(AuxUsuario,AuxContrasenia);
        if((!comparar.EnviarDatosRegistro())){
            GuardarDatos guardarDatos =  new GuardarDatos(AuxUsuario,AuxContrasenia);
            guardarDatos.FormatoRegistro();
            guardarDatos.GuardarFormatoRegistro();
        }
    }

    public static void MenuSesion(Scanner leer){
        Usuario AuxUsuario = new Usuario();
        Contrasenia AuxContrasenia = new Contrasenia();
        boolean pass = true;
        System.out.println("-------------------Iniciar Sesion--------------------");
        System.out.println("-----------------------------------------------------");
        do{
            System.out.println("-Ingrese Usuario: ");
            Usuario usuario = new Usuario(leer.next());
            if(usuario.getCorreo() != null){
                AuxUsuario = usuario;
            }else{
                AuxUsuario = null;
            }
        }while(AuxUsuario == null);
        do{
            System.out.println("-----------------------------------------------------");
            System.out.println("-Ingrese Contraseña ");
            Contrasenia contrasenia = new Contrasenia(leer.next());
            if(contrasenia.getContrasenia() != null){
                AuxContrasenia = contrasenia;
            }else{
                AuxContrasenia = null;
            }
        }while(AuxContrasenia == null);
        CompararDatos comparar = new CompararDatos(AuxUsuario,AuxContrasenia);
        if(comparar.EnviarDatosSesion()){
            System.out.println("-----------------------------------------------------");
            System.out.println("--------------Inicio de Sesion Exitosa---------------");
            System.out.println("-----------------------------------------------------");
            System.out.println("Bienvenido "+ AuxUsuario.getCorreo());
        }else{
            System.out.println("-----------------------------------------------------");
            System.out.println("❌ERROR Usuario No Registrado/Contraseña Incorrecta");
            System.out.println("-----------------------------------------------------");
        }
    }

    public static void MenuRecuperar(Scanner leer){
        Usuario AuxUsuario = new Usuario();
        Contrasenia AuxContrasenia = new Contrasenia();
        boolean pass = true;
        System.out.println("----------------Recuperar Contraseña-----------------");
        System.out.println("-----------------------------------------------------");
        do{
            System.out.println("-Ingrese Usuario: ");
            Usuario usuario = new Usuario(leer.next());
            if(usuario.getCorreo() != null){
                AuxUsuario = usuario;
            }else{
                AuxUsuario = null;
            }
        }while(AuxUsuario == null);
        CompararDatos comparar = new CompararDatos(AuxUsuario);
        if(comparar.EnviarDatosContrasenia()){
            System.out.println("-----------------------------------------------------");
            System.out.println("Recuperacion Exitosa");
        }else{
            System.out.println("-----------------------------------------------------");
            System.out.println("Usuario No Registrado");
        }
    }*/
}
