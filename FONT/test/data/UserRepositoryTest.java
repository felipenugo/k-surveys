package data;

import domain.model.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

/**
 * Tests para UserRepository.
 * Verifica operaciones CRUD y persistencia en JSON.
 */
public class UserRepositoryTest {

    private UserRepository userRepository;
    private static final String TEST_FILE_PATH = "./test_users.json";

    @Before
    public void setUp() {
        // Usar archivo de test separado para no afectar datos reales
        userRepository = new UserRepository(TEST_FILE_PATH);
        userRepository.clear();
    }

    @After
    public void tearDown() {
        // Limpiar archivo de test
        File testFile = new File(TEST_FILE_PATH);
        if (testFile.exists()) {
            testFile.delete();
        }
    }

    // ───────────────────────────────────────────────
    // Tests de creación y existencia
    // ───────────────────────────────────────────────

    @Test
    public void testAddUser() {
        User user = new User("testUser", "test@email.com", "hashedPassword", "Pregunta?", "Respuesta");
        userRepository.addUser(user);

        assertTrue(userRepository.existsUser("testUser"));
    }

    @Test
    public void testExistsUserReturnsFalseForNonExistent() {
        assertFalse(userRepository.existsUser("nonExistentUser"));
    }

    @Test
    public void testGetUser() {
        User user = new User("testUser", "test@email.com", "hashedPassword", "Pregunta?", "Respuesta");
        userRepository.addUser(user);

        User retrieved = userRepository.getUser("testUser");

        assertNotNull(retrieved);
        assertEquals("testUser", retrieved.getUsername());
        assertEquals("test@email.com", retrieved.getEmail());
    }

    @Test
    public void testGetUserReturnsNullForNonExistent() {
        User retrieved = userRepository.getUser("nonExistentUser");
        assertNull(retrieved);
    }

    // ───────────────────────────────────────────────
    // Tests de eliminación
    // ───────────────────────────────────────────────

    @Test
    public void testDeleteUser() {
        User user = new User("testUser", "test@email.com", "hashedPassword", "Pregunta?", "Respuesta");
        userRepository.addUser(user);

        assertTrue(userRepository.existsUser("testUser"));

        userRepository.deleteUser("testUser");

        assertFalse(userRepository.existsUser("testUser"));
    }

    @Test
    public void testDeleteNonExistentUserDoesNotThrow() {
        // No debería lanzar excepción
        userRepository.deleteUser("nonExistentUser");
    }

    // ───────────────────────────────────────────────
    // Tests de clear
    // ───────────────────────────────────────────────

    @Test
    public void testClear() {
        User user1 = new User("user1", "user1@email.com", "pass1", "Q1?", "A1");
        User user2 = new User("user2", "user2@email.com", "pass2", "Q2?", "A2");

        userRepository.addUser(user1);
        userRepository.addUser(user2);

        assertTrue(userRepository.existsUser("user1"));
        assertTrue(userRepository.existsUser("user2"));

        userRepository.clear();

        assertFalse(userRepository.existsUser("user1"));
        assertFalse(userRepository.existsUser("user2"));
    }

    // ───────────────────────────────────────────────
    // Tests de persistencia
    // ───────────────────────────────────────────────

    @Test
    public void testPersistenceAfterReload() {
        User user = new User("persistentUser", "persistent@email.com", "hashedPass", "Q?", "A");
        userRepository.addUser(user);

        // Crear nueva instancia para simular reinicio de aplicación
        UserRepository newRepository = new UserRepository(TEST_FILE_PATH);

        assertTrue(newRepository.existsUser("persistentUser"));
        User retrieved = newRepository.getUser("persistentUser");
        assertEquals("persistent@email.com", retrieved.getEmail());
    }

    // ───────────────────────────────────────────────
    // Tests de múltiples usuarios
    // ───────────────────────────────────────────────

    @Test
    public void testMultipleUsers() {
        for (int i = 0; i < 5; i++) {
            User user = new User("user" + i, "user" + i + "@email.com", "pass" + i, "Q" + i + "?", "A" + i);
            userRepository.addUser(user);
        }

        for (int i = 0; i < 5; i++) {
            assertTrue(userRepository.existsUser("user" + i));
        }
    }

    @Test
    public void testUpdateUser() {
        User user = new User("testUser", "old@email.com", "oldPass", "OldQ?", "OldA");
        userRepository.addUser(user);

        // Modificar usuario y re-añadir
        User updatedUser = new User("testUser", "new@email.com", "newPass", "NewQ?", "NewA");
        userRepository.addUser(updatedUser);

        User retrieved = userRepository.getUser("testUser");
        assertEquals("new@email.com", retrieved.getEmail());
    }
}

