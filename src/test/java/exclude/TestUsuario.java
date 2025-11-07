package exclude;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TestUsuario {

    private Usuario usuario;

    @Before
    public void setUp() {
        usuario = new Usuario("Arnau", "1302");
    }

    @Test
    public void constructorPorDefecto() {
        Usuario user = new Usuario();
        assertNull(user.getNombre());
        assertNull(user.getContrasena());
    }

    @Test
    public void constructorConParametrosValidos() {
        assertEquals("Arnau", usuario.getNombre());
        assertEquals("1302", usuario.getContrasena());
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorNombreNull() {
        new Usuario(null, "1302");
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorNombreVacio() {
        new Usuario("", "1302");
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorContrasenaNull() {
        new Usuario("Arnau", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorContrasenaVacia() {
        new Usuario("Arnau", "");
    }

    @Test
    public void settersValidos() {
        usuario.setNombre("Prueba");
        usuario.setContrasena("1302");
        usuario.setPuntuacionGlobal(100);

        assertEquals("Prueba", usuario.getNombre());
        assertEquals("1302", usuario.getContrasena());
        assertEquals(100, usuario.getPuntuacionGlobal());
    }

    @Test(expected = IllegalArgumentException.class)
    public void setNombreNull() {
        usuario.setNombre(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setNombreVacio() {
        usuario.setNombre("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void setContrasenaNull() {
        usuario.setContrasena(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setContrasenaVacia() {
        usuario.setContrasena("");
    }

}
