package ve.edu.ucab.mazerunnerfx.models;

/**
 * Encapsula la lógica para comparar y verificar datos de usuario contra registros almacenados.
 * Permite validar registro, inicio de sesión y recuperación de contraseña.
 */
public class CompararDatos extends RegistroArchivo{
    private Usuario usuario;
    private Contrasenia contrasenia;

    /**
     * Crea una instancia para operaciones que solo requieren usuario (recuperación de contraseña).
     * @param usuario usuario a verificar
     */
    public CompararDatos(Usuario usuario){
        this.usuario = usuario;
    }
    /**
     * Crea una instancia para operaciones de registro o sesión (usuario + contraseña).
     * @param usuario usuario a verificar
     * @param contrasenia contraseña asociada
     */
    public CompararDatos(Usuario usuario, Contrasenia contrasenia){
        this.usuario = usuario;
        this.contrasenia = contrasenia;
    }

    /**
     * Obtiene el usuario configurado.
     * @return usuario actual
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
     * Obtiene la contraseña configurada.
     * @return contraseña actual
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
     * Verifica si el usuario ya existe en el archivo de registros.
     * @return true si ya está registrado, false en caso contrario
     */
    public boolean EnviarDatosRegistro(){
        if (this.usuario == null || this.usuario.getCorreo() == null) {
            return false;
        }
        String AuxUsuario = usuario.getCorreo();
        // No acceder a contrasenia aquí: la verificación de registro solo necesita el usuario
        return this.BuscarArchivoRegistro(AuxUsuario);
    }
    /**
     * Valida las credenciales del usuario contra el archivo de sesiones.
     * @return true si las credenciales coinciden, false en caso contrario
     */
    public boolean EnviarDatosSesion(){
        String AuxUsuario = usuario.getCorreo();
        String AuxContrasenia = contrasenia.getContrasenia();
        if(this.BuscarArchivoSesion(AuxUsuario,AuxContrasenia)){
            return true;
        }else{
            return false;
        }
    }
    /**
     * Busca y muestra la contraseña del usuario, si existe.
     * @return true si el usuario está registrado, false en caso contrario
     */
    public boolean EnviarDatosContrasenia(){
        String AuxUsuario = usuario.getCorreo();
        if(this.BuscarArchivoContrasenia(AuxUsuario)){
            return true;
        }else{
            return false;
        }
    }


}
