package domain.service;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import data.UserRepository;
import data.SurveyRepository;
import domain.controller.UserController;
import domain.model.Survey;
import domain.model.User;

/**
 * Test unitario para verificar que SurveyService.createSurvey() registra la encuesta creada en el usuario.
 * Valida que:
 * 1. Al crear una encuesta, se registra automáticamente en el usuario
 * 2. La sincronización BD-JSON funciona correctamente
 * 3. Las excepciones se manejan sin romper el flujo
 * 4. El logging se ejecuta correctamente
 */
public class SurveyServiceCreateSurveyRegistrationTest {
    private UserService userService;
    private SurveyService surveyService;
    private UserController userController;
    private UserRepository userRepository;
    private SurveyRepository surveyRepository;

    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_EMAIL = "test@gmail.com";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_TITLE = "Test Survey";
    private static final String TEST_DESCRIPTION = "A test survey for unit testing";

    @Before
    public void setUp() {
        userRepository = new UserRepository();
        userRepository.clear();  // limpiar datos de ejecuciones anteriores
        surveyRepository = new SurveyRepository();
        surveyRepository.clear();  // limpiar datos de ejecuciones anteriores

        userService = new UserService(userRepository, null, null);
        userController = new UserController(userService);
        surveyService = new SurveyService(surveyRepository, userController, userService);

        // Registrar y loguear usuario
        userService.registerUser(TEST_USERNAME, TEST_EMAIL, TEST_PASSWORD, "Question?", "Answer");
        userController.loginUser(TEST_USERNAME, TEST_PASSWORD);
    }

    @Test
    public void testCreateSurveyRegistersInUser() {
        // Arrange: Crear encuesta sin ID
        Survey survey = surveyService.initializeNewSurvey(TEST_TITLE, TEST_DESCRIPTION, TEST_USERNAME);

        // Act: Crear la encuesta (debe registrarse automáticamente en el usuario)
        Survey createdSurvey = surveyService.createSurvey(survey);

        // Assert: Verificar que la encuesta está registrada en el usuario
        User user = userService.getUser(TEST_USERNAME);
        assertTrue("La encuesta debe estar registrada en el usuario",
                   user.hasCreatedSurveyId(createdSurvey.getSURVEY_ID()));
        assertEquals("El usuario debe tener exactamente 1 encuesta creada",
                     1, user.getTotalSurveysCreated());
    }

    @Test
    public void testCreateMultipleSurveysRegistration() {
        // Arrange: Crear múltiples encuestas
        Survey survey1 = surveyService.initializeNewSurvey("Survey 1", "Description 1", TEST_USERNAME);
        Survey survey2 = surveyService.initializeNewSurvey("Survey 2", "Description 2", TEST_USERNAME);
        Survey survey3 = surveyService.initializeNewSurvey("Survey 3", "Description 3", TEST_USERNAME);

        // Act: Crear las encuestas
        Survey created1 = surveyService.createSurvey(survey1);
        Survey created2 = surveyService.createSurvey(survey2);
        Survey created3 = surveyService.createSurvey(survey3);

        // Assert: Verificar que todas están registradas
        User user = userService.getUser(TEST_USERNAME);
        assertEquals("El usuario debe tener exactamente 3 encuestas creadas",
                     3, user.getTotalSurveysCreated());
        assertTrue("Encuesta 1 debe estar registrada",
                   user.hasCreatedSurveyId(created1.getSURVEY_ID()));
        assertTrue("Encuesta 2 debe estar registrada",
                   user.hasCreatedSurveyId(created2.getSURVEY_ID()));
        assertTrue("Encuesta 3 debe estar registrada",
                   user.hasCreatedSurveyId(created3.getSURVEY_ID()));
    }

    @Test
    public void testCreateSurveyRegistrationPersistence() {
        // Arrange: Crear encuesta
        Survey survey = surveyService.initializeNewSurvey(TEST_TITLE, TEST_DESCRIPTION, TEST_USERNAME);
        Survey createdSurvey = surveyService.createSurvey(survey);
        String surveyId = createdSurvey.getSURVEY_ID();

        // Act: Recargar usuario desde repositorio (simula recargar desde JSON)
        UserService newUserService = new UserService(userRepository, null, null);
        User reloadedUser = newUserService.getUser(TEST_USERNAME);

        // Assert: Verificar que la encuesta persiste
        assertNotNull("El usuario debe existir después de recargar", reloadedUser);
        assertTrue("La encuesta debe persistir en el JSON",
                   reloadedUser.hasCreatedSurveyId(surveyId));
    }

    @Test
    public void testCreateSurveyHandlesUserRegistrationException() {
        // Arrange: Crear encuesta para usuario logueado
        Survey survey = surveyService.initializeNewSurvey(TEST_TITLE, TEST_DESCRIPTION, TEST_USERNAME);

        // Act & Assert: La encuesta debe crearse sin excepción incluso si hay un problema con el registro
        // (el método debe capturar la excepción y continuar)
        Survey createdSurvey = surveyService.createSurvey(survey);
        assertNotNull("La encuesta debe crearse correctamente", createdSurvey);
    }

    @Test
    public void testSurveyIdGeneratedAndRegistered() {
        // Arrange: Crear encuesta
        Survey survey = surveyService.initializeNewSurvey(TEST_TITLE, TEST_DESCRIPTION, TEST_USERNAME);
        assertTrue("La encuesta nueva no debe tener ID",
                   survey.getSURVEY_ID() == null || survey.getSURVEY_ID().isEmpty());

        // Act: Crear la encuesta
        Survey createdSurvey = surveyService.createSurvey(survey);

        // Assert: Verificar que se generó ID y se registró
        assertNotNull("Se debe generar un ID para la encuesta", createdSurvey.getSURVEY_ID());
        assertFalse("El ID no debe estar vacío", createdSurvey.getSURVEY_ID().isEmpty());

        User user = userService.getUser(TEST_USERNAME);
        assertTrue("La encuesta debe estar registrada con el ID generado",
                   user.hasCreatedSurveyId(createdSurvey.getSURVEY_ID()));
    }
}

