package ve.edu.ucab.mazerunnerfx.models;

/**
 * Representa una contraseña de usuario y provee validación de formato.
 * Verifica longitud mínima, mayúsculas y caracteres especiales.
 */
public class Contrasenia{
    private String contrasenia;
    private String confirContrasenia;
    private static final String Pass = "^(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?])(?=.*[A-Z]).{6,}$";

    /**
     * Constructor por defecto.
     */
    public Contrasenia(){}
    /**
     * Crea una contraseña válida usando el mismo valor como confirmación.
     * @param contrasenia contraseña propuesta
     */
    public Contrasenia(String contrasenia){
        if(PassValidacion(contrasenia,contrasenia)){
            this.contrasenia = contrasenia;
        }
    }
    /**
     * Crea una contraseña validando coincidencia y formato con confirmación.
     * @param contrasenia contraseña propuesta
     * @param confirContrasenia confirmación de contraseña
     */
    public Contrasenia(String contrasenia, String confirContrasenia) {
        if(PassValidacion(contrasenia,confirContrasenia)){
            this.contrasenia = contrasenia;
            this.confirContrasenia = confirContrasenia;
        }
    }

    /**
     * Devuelve la contraseña almacenada.
     * @return contraseña
     */
    public String getContrasenia() {
        return contrasenia;
    }
    /**
     * Establece la contraseña sin validación adicional.
     * @param contrasenia nueva contraseña
     */
    public void setContrasenia(String contrasenia) {
        this.contrasenia = contrasenia;
    }
    /**
     * Devuelve la confirmación de contraseña.
     * @return confirmación de contraseña
     */
    public String getConfirContrasenia() {
        return confirContrasenia;
    }
    /**
     * Establece la confirmación de contraseña sin validación adicional.
     * @param confirContrasenia nueva confirmación
     */
    public void setConfirContrasenia(String confirContrasenia) {
        this.confirContrasenia = confirContrasenia;
    }

    /**
     * Verifica si la contraseña y su confirmación coinciden y cumplen el patrón requerido.
     * @param contrasenia contraseña a validar
     * @param confirContrasenia confirmación a validar
     * @return true si cumple el formato y coincide; false en caso contrario
     */
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
