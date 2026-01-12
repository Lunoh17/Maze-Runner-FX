package ve.edu.ucab.mazerunnerfx.models;

/**
 * Encapsula la lógica para guardar y restaurar datos de la partida.
 *
 * <p>Construye y persiste el formato de registro de usuario en un archivo de texto
 * y utiliza RegistroArchivo para realizar la escritura en disco.</p>
 *
 * @author Equipo
 * @version 2026-01-12
 */
public class GuardarDatos extends RegistroArchivo{
    private Usuario usuario;
    private Contrasenia contrasenia;

    /**
     * Crea el helper con usuario y contraseña para persistencia.
     * @param usuario usuario a guardar
     * @param contrasenia contraseña asociada
     */
    public GuardarDatos(Usuario usuario, Contrasenia contrasenia) {
        if(usuario != null && contrasenia != null){
            this.usuario = usuario;
            this.contrasenia = contrasenia;
        }
    }

    /**
     * Obtiene el usuario actual.
     * @return usuario
     */
    public Usuario getUsuario() {
        return usuario;
    }
    /**
     * Establece el usuario.
     * @param usuario nuevo usuario
     */
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    /**
     * Obtiene la contraseña actual.
     * @return contraseña
     */
    public Contrasenia getContrasenia() {
        return contrasenia;
    }
    /**
     * Establece la contraseña.
     * @param contrasenia nueva contraseña
     */
    public void setContrasenia(Contrasenia contrasenia) {
        this.contrasenia = contrasenia;
    }

    /**
     * Construye la línea de registro en formato usuario:contraseña.
     * @return cadena lista para persistir o null si faltan datos
     */
    public String FormatoRegistro(){
        if(usuario != null && contrasenia != null){
            return usuario.getCorreo()+":"+contrasenia.getContrasenia();
        }
        return null;
    }

    /**
     * Persiste en archivo el formato de registro si es válido.
     */
    public void GuardarFormatoRegistro(){
        String linea = FormatoRegistro();
        if(linea != null){
            this.GuardarArchivo(linea);
        }else{
            System.out.println("Error al Guardar datos son incompletos/inválidos.");
        }
    }


}
