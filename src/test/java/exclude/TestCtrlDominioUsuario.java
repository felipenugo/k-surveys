package exclude;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

public class TestCtrlDominioUsuario {

    private CtrlDominioUsuario ctrlDominioUsuario;
    private Usuario usuario;
    private StubPersistencia stubPersistencia;

    @Before
    public void setUp() {
        stubPersistencia = StubPersistencia.getInstancia();
        stubPersistencia.reset();

        ctrlDominioUsuario = new CtrlDominioUsuario();
        usuario = mock(Usuario.class);
        ctrlDominioUsuario.setUsuario(usuario);
    }

    @Test
    public void setYGetUsuario() {
        Usuario user = new Usuario("Arnau", "1302");
        ctrlDominioUsuario.setUsuario(user);
        assertEquals(user, ctrlDominioUsuario.getUsuario());
    }

    @Test(expected = IllegalArgumentException.class)
    public void setUsuarioNull() {
        ctrlDominioUsuario.setUsuario(null);
    }

    @Test
    public void testRegistrarUsuario() {
        assertTrue(ctrlDominioUsuario.registrarUsuario("Arnau", "1302", "1302"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void registrarUsuarioNombreNull() {
        ctrlDominioUsuario.registrarUsuario(null, "1302", "1302");
    }

    @Test(expected = IllegalArgumentException.class)
    public void registrarUsuarioNombreVacio() {
        ctrlDominioUsuario.registrarUsuario("", "1302", "1302");
    }

    @Test(expected = IllegalArgumentException.class)
    public void registrarUsuarioContrasena1Null() {
        ctrlDominioUsuario.registrarUsuario("Arnau", null, "1302");
    }

    @Test(expected = IllegalArgumentException.class)
    public void registrarUsuarioContrasena1Vacia() {
        ctrlDominioUsuario.registrarUsuario("Arnau", "", "1302");
    }

    @Test(expected = IllegalArgumentException.class)
    public void registrarUsuarioContrasena2Null() {
        ctrlDominioUsuario.registrarUsuario("Arnau", "1302", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void registrarUsuarioContrasena2Vacia() {
        ctrlDominioUsuario.registrarUsuario("Arnau", "1302", "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void registrarUsuarioContrasenasDistintas() {
        ctrlDominioUsuario.registrarUsuario("Arnau", "1302", "1301");
    }

    @Test
    public void registrarUsuarioYaExistente() {
        assertTrue(ctrlDominioUsuario.registrarUsuario("Arnau", "1302", "1302"));
        assertFalse(ctrlDominioUsuario.registrarUsuario("Arnau", "abcd", "abcd"));
    }


    @Test
    public void iniciarSesion() {
        stubPersistencia.registrarUsuario(new Usuario("Arnau", "1302"));

        assertTrue(ctrlDominioUsuario.iniciarSesion("Arnau", "1302"));
        assertEquals("Arnau", ctrlDominioUsuario.getUsuario().getNombre());
    }

    @Test(expected = IllegalArgumentException.class)
    public void iniciarSesionNombreNull() {
        ctrlDominioUsuario.iniciarSesion(null, "1302");
    }

    @Test(expected = IllegalArgumentException.class)
    public void iniciarSesionNombreVacio() {
        ctrlDominioUsuario.iniciarSesion("", "1302");
    }

    @Test(expected = IllegalArgumentException.class)
    public void iniciarSesionContrasenaNull() {
        ctrlDominioUsuario.iniciarSesion("Arnau", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void iniciarSesionContrasenaVacia() {
        ctrlDominioUsuario.iniciarSesion("Arnau", "");
    }

    @Test
    public void iniciarSesionContrasenaIncorrecta() {
        stubPersistencia.registrarUsuario(new Usuario("Arnau", "1302"));

        assertFalse(ctrlDominioUsuario.iniciarSesion("Arnau", "1301"));
        assertNull(ctrlDominioUsuario.getUsuario());
    }

    @Test
    public void iniciarSesionUsuarioInexistente() {
        assertFalse(ctrlDominioUsuario.iniciarSesion("Arnau", "1301"));
        assertNull(ctrlDominioUsuario.getUsuario());
    }

    @Test
    public void cerrarSesion() {
        stubPersistencia.registrarUsuario(new Usuario("Arnau", "1302"));

        assertTrue(ctrlDominioUsuario.iniciarSesion("Arnau", "1302"));
        assertEquals("Arnau", ctrlDominioUsuario.getUsuario().getNombre());

        ctrlDominioUsuario.cerrarSesion();

        assertNull(ctrlDominioUsuario.getUsuario());
    }

    @Test
    public void cerrarSesionSinUsuario() {
        ctrlDominioUsuario.cerrarSesion();
        assertNull(ctrlDominioUsuario.getUsuario());
    }

    @Test
    public void getNombreUsuario() {
        when(usuario.getNombre()).thenReturn("Arnau");
        assertEquals("Arnau", ctrlDominioUsuario.getNombreUsuario());
    }
}
