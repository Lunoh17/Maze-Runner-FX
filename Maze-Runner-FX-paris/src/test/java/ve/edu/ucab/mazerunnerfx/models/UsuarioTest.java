package ve.edu.ucab.mazerunnerfx.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UsuarioTest {

    @Test
    public void testUsuaValidacion_validEmail() {
        Usuario u = new Usuario();
        String res = u.UsuaValidacion("name@mail.com");
        assertEquals("name@mail.com", res);
    }

    @Test
    public void testUsuaValidacion_trailingAt() {
        Usuario u = new Usuario();
        String res = u.UsuaValidacion("alice@");
        assertEquals("alice@email.com", res);
    }

    @Test
    public void testUsuaValidacion_invalid() {
        Usuario u = new Usuario();
        String res = u.UsuaValidacion("bad-email");
        assertNull(res);
    }
}

