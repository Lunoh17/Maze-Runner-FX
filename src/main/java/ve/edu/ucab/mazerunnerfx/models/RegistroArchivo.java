package ve.edu.ucab.mazerunnerfx.models;

import java.util.Scanner;
import java.io.File;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.regex.Pattern;

/**
 * Abstracción de acceso a archivo de registro de usuarios.
 * Ofrece utilidades para guardar, buscar y validar credenciales almacenadas.
 */
public abstract class RegistroArchivo {
    private static final String nombreArchivo = "Registro-Login.txt";

    /**
     * Guarda en el archivo una línea en formato usuario:contraseña (cifrada).
     * @param palabra cadena con formato usuario:contraseña en texto plano
     */
    public void GuardarArchivo(String palabra){
        String[] partes = palabra.split(":");
        if (partes.length != 2) return;
        String usuario = partes[0];
        String contrasenia = partes[1];
        String datosGuardar;
        try {
            String contraseniaCifrada = AESCifrado.Cifrado(contrasenia);
            datosGuardar = usuario+":"+contraseniaCifrada;

            FileWriter archivo = new FileWriter(nombreArchivo,true);
            PrintWriter out = new PrintWriter(archivo);
            out.println(datosGuardar);
            out.close();

        } catch (Exception e){
            System.out.println("Ocurrio un error al agregar el Usuario/Contraseña");
            e.printStackTrace();
        }
    }

    /**
     * Verifica si un usuario ya está registrado en el archivo.
     * @param usuario correo/identificador a buscar
     * @return true si existe; false de lo contrario
     */
    public boolean BuscarArchivoRegistro(String usuario) {
        try {
            File archivo = new File(nombreArchivo);
            Scanner leer = new Scanner(archivo);
            leer.useDelimiter(Pattern.compile(":|\r\n|\n"));
            while(leer.hasNext()){
                String AuxUsuario = leer.next().trim();
                if(leer.hasNext()){
                    String AuxContrasenia = leer.next().trim();
                    if(AuxUsuario.equals(usuario)){
                        leer.close();
                        System.out.println("Este Usuario Ya se encuentra Registrado");
                        return true;
                    }
                }
            }
            return false;
        } catch (FileNotFoundException e) {
            System.out.println("❌Ocurrio un error al verificar el Usuario/Contraseña");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Valida credenciales contra el archivo de registros.
     * Permite recuperación opcional si la contraseña no coincide.
     * @param usuario identificador
     * @param contrasenia contraseña en texto plano
     * @return true si coincide; false de lo contrario
     */
    public boolean BuscarArchivoSesion(String usuario, String contrasenia){
        try{
            File archivo = new File(nombreArchivo);
            Scanner leer = new Scanner(archivo);
            while(leer.hasNext()){
                String linea = leer.nextLine().trim();
                if (linea.isEmpty()) continue;
                String [] partes = linea.split(":");
                if (partes.length != 2) continue;
                String AuxUsuario = partes[0].trim();
                String AuxContraseniaCifrada = partes[1].trim();
                if(AuxUsuario.equals(usuario)){
                    String AuxContraseniaDescifrada = AESCifrado.Descifrado(AuxContraseniaCifrada);
                    leer.close();
                    return AuxContraseniaDescifrada.equals(contrasenia);
                }
            }
            return false;
        }catch (FileNotFoundException e) {
            System.out.println("❌Ocurrio un error al verificar el Usuario/Contraseña");
            e.printStackTrace();
            return false;
        }catch (Exception e) {
            System.out.println("❌ERROR al descifrar los datos: " + e.getMessage());
            e.printStackTrace();
            return false;
        }

    }

    /**
     * Recupera y muestra la contraseña descifrada de un usuario si existe en el archivo.
     * @param usuario identificador a buscar
     * @return true si se encontró y mostró; false de lo contrario
     */
    public boolean BuscarArchivoContrasenia(String usuario) {
        try {
            File archivo = new File(nombreArchivo);
            Scanner leer = new Scanner(archivo);
            while(leer.hasNext()){
                String linea = leer.nextLine().trim();
                if (linea.isEmpty()) continue;
                String [] partes = linea.split(":");
                if (partes.length != 2) continue;
                String AuxUsuario = partes[0].trim();
                String AuxContraseniaCifrada = partes[1].trim();
                if(AuxUsuario.equals(usuario)){
                    String AuxContraseniaDescifrada = AESCifrado.Descifrado(AuxContraseniaCifrada);
                    leer.close();
                    System.out.println("La Contraseña del Usuario "+AuxUsuario+"= "+AuxContraseniaDescifrada);
                    return true;
                }
            }
            return false;
        } catch (FileNotFoundException e) {
            System.out.println("❌Ocurrio un error al verificar el Usuario/Contraseña");
            e.printStackTrace();
            return false;
        }catch (Exception e) {
            System.out.println("❌ERROR al descifrar los datos: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
