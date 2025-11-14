package exclude;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import data.UserRepository;
import domain.controller.UserController;
import domain.exception.LogInException;
import domain.exception.RegisterException;
import domain.service.UserService;

/**
 * Tests integrados para la clase {@link UserController}.
 * 
 * Se utilizan instancias reales de {@link UserService} y {@link UserRepository},
 * siguiendo el estilo PROP (sin mocks). Se validan los principales flujos:
 * registro, login, logout y eliminación de usuarios.
 * 
 * @author Arnau
 */
public class TestUserController {

    private UserRepository userRepository;
    private UserService userService;
    private UserController ctrl;

    @Before
    public void setUp() {
        userRepository = new UserRepository();  // repositorio real en memoria
        userService = new UserService(userRepository);
        ctrl = new UserController(userService);
    }

    // ───────────────────────────────
    // REGISTRO
    // ───────────────────────────────

    @Test
    public void testRegistrarUsuarioCorrecto() {
        ctrl.registerUser("arnau", "arnau@fib.upc.edu", "1234");

        // Comprobamos que el usuario fue guardado correctamente
        assertTrue(userRepository.existsUser("arnau"));
    }

    @Test(expected = RegisterException.class)
    public void testRegistrarUsuarioDuplicado() {
        ctrl.registerUser("arnau", "arnau@fib.upc.edu", "1234");
        ctrl.registerUser("arnau", "arnau@fib.upc.edu", "abcd");
    }

    @Test(expected = RegisterException.class)
    public void testRegistrarUsuarioConEmailVacio() {
        ctrl.registerUser("marc", "", "abcd");
    }

    @Test(expected = RegisterException.class)
    public void testRegistrarUsuarioConPasswordVacia() {
        ctrl.registerUser("julia", "julia@fib.upc.edu", "");
    }

    // ───────────────────────────────
    // LOGIN
    // ───────────────────────────────

    @Test
    public void testLoginCorrecto() {
        ctrl.registerUser("arnau", "arnau@fib.upc.edu", "1234");
        ctrl.loginUser("arnau", "1234");

        assertTrue(ctrl.isLoggedIn());
        assertEquals("arnau", ctrl.getUsernameLoggedIn());
    }

    @Test(expected = LogInException.class)
    public void testLoginContrasenaIncorrecta() {
        ctrl.registerUser("arnau", "arnau@fib.upc.edu", "1234");
        ctrl.loginUser("arnau", "0000");
    }

    @Test(expected = LogInException.class)
    public void testLoginUsuarioNoExistente() {
        ctrl.loginUser("ghost", "1234");
    }

    @Test(expected = LogInException.class)
    public void testLoginSinHaberseRegistrado() {
        ctrl.loginUser("arnau", "1234");
    }

    @Test(expected = LogInException.class)
    public void testLoginYaLogueado() {
        ctrl.registerUser("arnau", "arnau@fib.upc.edu", "1234");
        ctrl.loginUser("arnau", "1234");
        ctrl.loginUser("arnau", "1234"); // segundo login → excepción
    }

    // ───────────────────────────────
    // LOGOUT
    // ───────────────────────────────

    @Test
    public void testLogoutCorrecto() {
        ctrl.registerUser("arnau", "arnau@fib.upc.edu", "1234");
        ctrl.loginUser("arnau", "1234");
        ctrl.logoutUser();

        assertFalse(ctrl.isLoggedIn());
        assertNull(ctrl.getUsernameLoggedIn());
    }

    @Test(expected = LogInException.class)
    public void testLogoutSinUsuarioLogueado() {
        ctrl.logoutUser();
    }

    // ───────────────────────────────
    // DELETE
    // ───────────────────────────────

    @Test
    public void testEliminarUsuarioCorrecto() {
        ctrl.registerUser("arnau", "arnau@fib.upc.edu", "1234");
        ctrl.deleteUser("arnau");

        assertFalse(userRepository.existsUser("arnau"));
    }

    @Test
    public void testEliminarUsuarioInexistente() {
        ctrl.deleteUser("ghost");

        // Simplemente comprobamos que no lanza excepciones ni cambia el estado
        assertFalse(ctrl.isLoggedIn());
        assertFalse(userRepository.existsUser("ghost"));
    }
}
