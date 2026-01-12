package ve.edu.ucab.mazerunnerfx.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para la validación de contraseñas.
 *
 * <p>Comprueba que las reglas de contraseñas se apliquen correctamente.</p>
 *
 * @author Equipo
 * @version 2026-01-12
 */
public class ContraseniaTest {

    @Test
    public void testPassValidacion_matchingValid() {
        Contrasenia c = new Contrasenia();
        assertTrue(c.PassValidacion("Abcdef@1", "Abcdef@1"));
    }

    @Test
    public void testPassValidacion_mismatch() {
        Contrasenia c = new Contrasenia();
        assertFalse(c.PassValidacion("Abcdef@1", "Abcdef@2"));
    }

    @Test
    public void testPassValidacion_tooWeak() {
        Contrasenia c = new Contrasenia();
        assertFalse(c.PassValidacion("abcdef", "abcdef"));
    }
}
