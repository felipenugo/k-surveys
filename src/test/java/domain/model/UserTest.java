package domain.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class UserTest {

    private User usuario;

    @Before
    public void setUp() {
        usuario = new User("arnau", "arnau@fib.upc.edu", "1234");
    }

    @Test
    public void constructorPorDefecto() {
        User usuario = new User("", "", "");
        assertEquals("", usuario.getUsername());
        assertEquals("", usuario.getEmail());
        assertEquals("", usuario.getPassword());
    }

    @Test
    public void constructorConParametrosValidos() {
        assertEquals("arnau", usuario.getUsername());
        assertEquals("arnau@fib.upc.edu", usuario.getEmail());
        assertEquals("1234", usuario.getPassword());
    }

    public void constructorNombreNull() {
        User u = new User(null, "arnau@fib.upc.edu", "1302");
        assertNull(u.getUsername());
    }
    
    @Test
    public void constructorNombreVacio() {
        User u = new User("",  "arnau@fib.upc.edu", "1302");
        assertEquals("", u.getUsername());
    }
    
    @Test
    public void constructorEmailNull() {
        User u = new User("Arnau", null, "1302");
        assertNull(u.getEmail());
    }
    
    @Test
    public void constructorEmailVacio() {
        User u = new User("Arnau", "", "1302");
        assertEquals("", u.getEmail());
    }
    
    @Test
    public void constructorContrasenaNull() {
        User u = new User("Arnau", "arnau@fib.upc.edu", null);
        assertNull(u.getPassword());
    }
    
    @Test
    public void constructorContrasenaVacia() {
        User u = new User("Arnau", "arnau@fib.upc.edu", "");
        assertEquals("", u.getPassword());
    }

    @Test
    public void settersValidos() {
        usuario = new User("Prueba", usuario.getEmail(), usuario.getPassword());
        usuario.setEmail("prueba@fib.upc.edu");
        usuario.setPassword("1302");

        assertEquals("Prueba", usuario.getUsername());
        assertEquals("prueba@fib.upc.edu", usuario.getEmail());
        assertEquals("1302", usuario.getPassword());
    }
    @Test
    public void setNombreNull() {
        User u = new User(null, "arnau@fib.upc.edu", "1234");
        assertNull(u.getUsername());
    }

    @Test
    public void setNombreVacio() {
        User u = new User("", "arnau@fib.upc.edu", "1234");
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
        usuario.setPassword(null);
        assertNull(usuario.getPassword());
    }

    @Test
    public void setContrasenaVacia() {
        usuario.setPassword("");
        assertEquals("", usuario.getPassword());
    }

}
