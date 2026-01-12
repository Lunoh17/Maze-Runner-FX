package ve.edu.ucab.mazerunnerfx.models;

/**
 * Representa un usuario del sistema identificado por su correo electrónico.
 * Ofrece validación básica del formato de correo.
 *
 * @author Equipo
 * @version 2026-01-12
 */
public class Usuario{
    private String correo;
    private static final String Email = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final String Email2 = "^[A-Za-z0-9._%+-]+@$";

    /**
     * Constructor por defecto.
     */
    public Usuario(){}

    /**
     * Crea un usuario validando el formato de correo.
     * Si solo se provee el usuario con '@', completa con dominio por defecto.
     * @param correo correo electrónico del usuario
     */
    public Usuario(String correo) {
        if(UsuaValidacion(correo) != null) {
            this.correo = UsuaValidacion(correo);
        }
    }

    /**
     * Obtiene el correo electrónico del usuario.
     * @return correo electrónico actual
     */
    public String getCorreo() {
        return correo;
    }

    /**
     * Establece el correo electrónico sin validación adicional.
     * Use el constructor o UsuaValidacion para validar.
     * @param correo nuevo correo electrónico
     */
    public void setCorreo(String correo) {
        this.correo = correo;
    }

    /**
     * Valida y normaliza el correo electrónico.
     * Acepta un correo completo válido o un usuario que termine en '@',
     * en cuyo caso completa con el dominio "@email.com".
     * @param correo entrada a validar
     * @return correo válido/normalizado o null si es inválido
     */
    public String UsuaValidacion(String correo){
        if(correo != null){
            if(correo.matches(Email)){
                return correo;
            }else if(correo.matches(Email2)){
                int indice;
                indice = correo.indexOf('@');
                String usuario = correo.substring(0,indice);
                correo = usuario + "@email.com";
                return correo;
            }else{
                System.out.println("❌ERROR Correo no valido, debe contener una dirrecion de email valida o contener un @");
                return null;
            }
        }else{
            System.out.println("❌ERROR el Correo no puede ser NULL");
            return null;
        }
    }
}
