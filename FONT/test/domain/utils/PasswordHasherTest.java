package domain.utils;

import domain.utils.PasswordHasher;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Pruebas unitarias para la clase PasswordHasher.
 * Esta clase contiene métodos para comprobar la funcionalidad del hash y asegurar
 * que produce el resultado correcto y maneja adecuadamente casos límite como
 * entradas nulas.
 */
public class PasswordHasherTest {

    /**
     * Comprueba que el método hash genera correctamente el hash de una contraseña simple.
     */
    @Test
    public void testHashSimplePassword() {
        String password = "hash";
        String hash = PasswordHasher.hash(password);
        assertNotNull("El hash no debería ser null", hash);
        assertEquals("d04b98f48e8f8bcc15c6ae5ac050801cd6dcfd428fb5f9e65c4e16e7807340fa", hash);
    }

    /**
     * Comprueba que el método hash maneja correctamente una cadena vacía.
     */
    @Test
    public void testHashEmptyString() {
        String password = "";
        String hash = PasswordHasher.hash(password);
        assertNotNull("El hash no debería ser null", hash);
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", hash);
    }

    /**
     * Comprueba que el método hash maneja correctamente contraseñas con caracteres especiales.
     */
    @Test
    public void testHashWithSpecialCharacters() {
        String password = "!@#$%^&*()_+[]{}|;':,.<>?";
        String hash = PasswordHasher.hash(password);
        assertNotNull("El hash no debería ser null", hash);
        assertEquals("cc0a63583779fd89050d48c773f5ce933807f93f4ea069e01bc0af2334297df6", hash);
    }

    /**
     * Comprueba que el método hash genera valores distintos para contraseñas diferentes.
     */
    @Test
    public void testHashDifferentPasswords() {
        String password1 = "password";
        String password2 = "Password";
        String hash1 = PasswordHasher.hash(password1);
        String hash2 = PasswordHasher.hash(password2);
        assertNotNull("El hash de password1 no debería ser null", hash1);
        assertNotNull("El hash de password2 no debería ser null", hash2);
        assertNotEquals("Los hashes de contraseñas distintas no deberían ser iguales", hash1, hash2);
    }

    /**
     * Comprueba que el método hash genera siempre el mismo resultado para la misma entrada.
     */
    @Test
    public void testHashConsistency() {
        String password = "consistentPassword";
        String hash1 = PasswordHasher.hash(password);
        String hash2 = PasswordHasher.hash(password);
        assertNotNull("El hash no debería ser null", hash1);
        assertEquals("El hash debería ser consistente para la misma entrada", hash1, hash2);
    }

    /**
     * Comprueba que el método hash lanza una excepción cuando la entrada es null.
     */
    @Test(expected = NullPointerException.class)
    public void testHashNullInput() {
        String password = null;
        PasswordHasher.hash(password);
    }
}
