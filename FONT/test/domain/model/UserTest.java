package domain.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class UserTest {

    private User usuario;

    @Before
    public void setUp() {
        usuario = new User("arnau", "arnau@fib.upc.edu", "Arnau13.");
    }

    @Test
    public void constructorPorDefecto() {
        User usuario = new User("", "", "");
        assertEquals("", usuario.getUsername());
        assertEquals("", usuario.getEmail());
        assertEquals("", usuario.getPasswordHash());
    }

    @Test
    public void constructorConParametrosValidos() {
        assertEquals("arnau", usuario.getUsername());
        assertEquals("arnau@fib.upc.edu", usuario.getEmail());
        assertEquals("Arnau13.", usuario.getPasswordHash());
    }

    public void constructorNombreNull() {
        User u = new User(null, "arnau@fib.upc.edu", "Arnau13.");
        assertNull(u.getUsername());
    }
    
    @Test
    public void constructorNombreVacio() {
        User u = new User("",  "arnau@fib.upc.edu", "Arnau13.");
        assertEquals("", u.getUsername());
    }
    
    @Test
    public void constructorEmailNull() {
        User u = new User("Arnau", null, "Arnau13.");
        assertNull(u.getEmail());
    }
    
    @Test
    public void constructorEmailVacio() {
        User u = new User("Arnau", "", "Arnau13.");
        assertEquals("", u.getEmail());
    }
    
    @Test
    public void constructorContrasenaNull() {
        User u = new User("Arnau", "arnau@fib.upc.edu", null);
        assertNull(u.getPasswordHash());
    }
    
    @Test
    public void constructorContrasenaVacia() {
        User u = new User("Arnau", "arnau@fib.upc.edu", "");
        assertEquals("", u.getPasswordHash());
    }

    @Test
    public void settersValidos() {
        usuario = new User("Prueba", usuario.getEmail(), usuario.getPasswordHash());
        usuario.setEmail("prueba@fib.upc.edu");
        usuario.setPasswordHash("Arnau13.");

        assertEquals("Prueba", usuario.getUsername());
        assertEquals("prueba@fib.upc.edu", usuario.getEmail());
        assertEquals("Arnau13.", usuario.getPasswordHash());
    }
    @Test
    public void setNombreNull() {
        User u = new User(null, "arnau@fib.upc.edu", "Arnau13.");
        assertNull(u.getUsername());
    }

    @Test
    public void setNombreVacio() {
        User u = new User("", "arnau@fib.upc.edu", "Arnau13.");
        assertEquals("", u.getUsername());
    }

    @Test
    public void setEmailNull() {
        usuario.setEmail(null);
        assertNull(usuario.getEmail());
    }

    @Test
    public void setEmailVacio() {
        usuario.setEmail("");
        assertEquals("", usuario.getEmail());
    }

    @Test
    public void setContrasenaNull() {
        usuario.setPasswordHash(null);
        assertNull(usuario.getPasswordHash());
    }

    @Test
    public void setContrasenaVacia() {
        usuario.setPasswordHash("");
        assertEquals("", usuario.getPasswordHash());
    }

}
