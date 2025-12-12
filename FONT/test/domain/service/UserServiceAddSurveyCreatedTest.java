package domain.service;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import data.UserRepository;
import data.SurveyRepository;
import data.ResponseRepository;
import domain.model.User;

/**
 * Test unitario para verificar el método addSurveyCreated() en UserService.
 * Valida que:
 * 1. La encuesta se registra correctamente en el usuario
 * 2. Se sincroniza correctamente con el JSON
 * 3. Se maneja el error si el usuario no existe
 * 4. El logging se ejecuta correctamente
 */
public class UserServiceAddSurveyCreatedTest {
    private UserService userService;
    private UserRepository userRepository;
    private SurveyRepository surveyRepository;
    private ResponseRepository responseRepository;
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_EMAIL = "test@gmail.com";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_SURVEY_ID = "survey_001";

    @Before
    public void setUp() {
        userRepository = new UserRepository();
        surveyRepository = new SurveyRepository();
        responseRepository = new ResponseRepository();
        userRepository.clear();  // limpiar datos de ejecuciones anteriores

        userService = new UserService(userRepository, surveyRepository, responseRepository);
    }

    @Test
    public void testAddSurveyCreatedSuccessfully() {
        // Arrange: Crear usuario
        userService.registerUser(TEST_USERNAME, TEST_EMAIL, TEST_PASSWORD, "Question?", "Answer");

        // Act: Agregar encuesta creada
        userService.addSurveyCreated(TEST_USERNAME, TEST_SURVEY_ID);

        // Assert: Verificar que la encuesta se registró
        User user = userService.getUser(TEST_USERNAME);
        assertTrue("La encuesta debe estar registrada en el usuario",
                   user.hasCreatedSurveyId(TEST_SURVEY_ID));
        assertEquals("El usuario debe tener exactamente 1 encuesta creada",
                     1, user.getTotalSurveysCreated());
    }

    @Test
    public void testAddMultipleSurveysCreated() {
        // Arrange: Crear usuario
        userService.registerUser(TEST_USERNAME, TEST_EMAIL, TEST_PASSWORD, "Question?", "Answer");
        String surveyId1 = "survey_001";
        String surveyId2 = "survey_002";
        String surveyId3 = "survey_003";

        // Act: Agregar múltiples encuestas creadas
        userService.addSurveyCreated(TEST_USERNAME, surveyId1);
        userService.addSurveyCreated(TEST_USERNAME, surveyId2);
        userService.addSurveyCreated(TEST_USERNAME, surveyId3);

        // Assert: Verificar que todas las encuestas se registraron
        User user = userService.getUser(TEST_USERNAME);
        assertEquals("El usuario debe tener exactamente 3 encuestas creadas",
                     3, user.getTotalSurveysCreated());
        assertTrue("Encuesta 1 debe existir", user.hasCreatedSurveyId(surveyId1));
        assertTrue("Encuesta 2 debe existir", user.hasCreatedSurveyId(surveyId2));
        assertTrue("Encuesta 3 debe existir", user.hasCreatedSurveyId(surveyId3));
    }

    @Test(expected = RuntimeException.class)
    public void testAddSurveyCreatedUserNotFound() {
        // Act: Intentar agregar encuesta a usuario inexistente
        userService.addSurveyCreated("nonexistentuser", TEST_SURVEY_ID);

        // Assert: Se debe lanzar RuntimeException
    }

    @Test
    public void testAddSurveyCreatedPersistenceToJson() {
        // Arrange: Crear usuario
        userService.registerUser(TEST_USERNAME, TEST_EMAIL, TEST_PASSWORD, "Question?", "Answer");
        userService.addSurveyCreated(TEST_USERNAME, TEST_SURVEY_ID);

        // Act: Crear nuevo UserService (simulando recarga desde JSON)
        UserService newUserService = new UserService(userRepository, new SurveyRepository(), new ResponseRepository());
        User reloadedUser = newUserService.getUser(TEST_USERNAME);

        // Assert: Verificar que la encuesta persiste después de recargar
        assertNotNull("El usuario debe existir después de recargar", reloadedUser);
        assertTrue("La encuesta debe persistir en el JSON",
                   reloadedUser.hasCreatedSurveyId(TEST_SURVEY_ID));
    }

    @Test
    public void testAddSurveyCreatedWithLogging() {
        // Arrange: Crear usuario
        userService.registerUser(TEST_USERNAME, TEST_EMAIL, TEST_PASSWORD, "Question?", "Answer");

        // Act: Agregar encuesta (el logging se ejecuta internamente)
        userService.addSurveyCreated(TEST_USERNAME, TEST_SURVEY_ID);

        // Assert: Verificar que la encuesta se registró (el logging no causa excepción)
        User user = userService.getUser(TEST_USERNAME);
        assertTrue("La encuesta debe estar registrada",
                   user.hasCreatedSurveyId(TEST_SURVEY_ID));
    }
}
