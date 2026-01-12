package ve.edu.ucab.mazerunnerfx.models;

public class ServicioAuten {
    private final UsuarioRepositorio repository = JsonUsuarioRepositorio.getInstancia();

    public boolean registrar(String correo, String pass) throws Exception {
        String uCif = AESCifrado.Cifrado(correo);
        if (repository.existe(uCif)) return false;

        repository.guardar(new UsuarioCifrado(uCif, AESCifrado.Cifrado(pass)));
        return true;
    }

    public String login(String correo, String pass) throws Exception {
        for (UsuarioCifrado dto : repository.obtenerTodos()) {
            if (AESCifrado.Descifrado(dto.getUsuario()).equals(correo)) {
                if (AESCifrado.Descifrado(dto.getContrasenia()).equals(pass)) return "EXITO";
                return "PASS_ERROR";
            }
        }
        return "NO_EXISTE";
    }

    public String recuperarPass(String correo) throws Exception {
        for (UsuarioCifrado dto : repository.obtenerTodos()) {
            if (AESCifrado.Descifrado(dto.getUsuario()).equals(correo)) {
                return AESCifrado.Descifrado(dto.getContrasenia());
            }
        }
        return null;
    }
}