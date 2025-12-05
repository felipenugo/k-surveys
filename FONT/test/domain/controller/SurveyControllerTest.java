package domain.controller;

import data.SurveyRepository;
import data.UserRepository;
import domain.model.Survey;
import domain.model.Question;
import domain.model.enums.TypeQuestion;
import domain.service.SurveyService;
import domain.service.UserService;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class SurveyControllerTest {

    private SurveyController surveyController;
    private SurveyService surveyService;
    private SurveyRepository surveyRepository;
    private UserController userController;

    private static final String TEST_TITLE = "Test Survey";
    private static final String TEST_DESCRIPTION = "A survey for testing purposes";
    private static final String TEST_CREATOR = "testuser";
    private static final String TEST_EMAIL = "test@gmail.com";
    private static final String TEST_PASSWORD = "password123";

    @Before
    public void setUp() {
        // Inicializar repositorios
        surveyRepository = new SurveyRepository();
        UserRepository userRepository = new UserRepository();

        // Inicializar servicios y controllers
        UserService userService = new UserService(userRepository);
        userController = new UserController(userService);
        surveyService = new SurveyService(surveyRepository, userController, userService);
        surveyController = new SurveyController(surveyService);

        // Asegurar logout antes de setup
        try {
            userController.logoutUser();
        } catch (Exception e) {
            // No había nadie logueado, ignorar
        }
        userController.registerUser(TEST_CREATOR, TEST_EMAIL, TEST_PASSWORD, "What is your pet's name?", "Fluffy");
        userController.loginUser(TEST_CREATOR, TEST_PASSWORD);
    }

    @Test
    public void testInitializeNewSurvey() {
        Survey survey = surveyController.initializeNewSurvey(TEST_TITLE, TEST_DESCRIPTION, TEST_CREATOR);

        // Añadir al menos una pregunta para poder crear la encuesta
        Question question = new Question(0, survey.getSURVEY_ID());
        question.setQuestionText("Pregunta inicial");
        question.setTypeQuestion(TypeQuestion.TEXTUAL);
        survey.addQuestion(question);

        // Guardar la encuesta para que tenga un ID válido
        Survey result = surveyController.createSurvey(survey);

        assertNotNull(result);
        assertNotNull(result.getSURVEY_ID());
        assertEquals(TEST_TITLE, result.getTitle());
        assertEquals(TEST_DESCRIPTION, result.getDescription());
        assertEquals(TEST_CREATOR, result.getCREATOR_USERNAME());
        assertNotNull(result.getCREATED_AT());
    }

    @Test
    public void testCreateSurvey() {
        Survey survey = new Survey(TEST_TITLE, TEST_DESCRIPTION, TEST_CREATOR);

        // Añadir una pregunta para cumplir con la validación
        Question question = new Question(0, survey.getSURVEY_ID());
        question.setQuestionText("Pregunta de prueba");
        question.setTypeQuestion(TypeQuestion.TEXTUAL);
        survey.addQuestion(question);

        Survey result = surveyController.createSurvey(survey);

        assertNotNull(result);
        assertNotNull(result.getSURVEY_ID());
        assertEquals(TEST_TITLE, result.getTitle());
        assertEquals(TEST_DESCRIPTION, result.getDescription());
        assertEquals(TEST_CREATOR, result.getCREATOR_USERNAME());
    }

    @Test
    public void testAddQuestionToSurvey() {
        Survey survey = surveyController.initializeNewSurvey(TEST_TITLE, TEST_DESCRIPTION, TEST_CREATOR);

        Question question1 = new Question(0, survey.getSURVEY_ID());
        question1.setQuestionText("Primera pregunta");
        question1.setTypeQuestion(TypeQuestion.TEXTUAL);
        survey.addQuestion(question1);

        // Guardar la encuesta
        survey = surveyController.createSurvey(survey);
        String surveyId = survey.getSURVEY_ID();

        Question question2 = new Question(1, surveyId);
        question2.setQuestionText("¿Cuál es tu nombre?");
        question2.setTypeQuestion(TypeQuestion.TEXTUAL);

        survey.addQuestion(question2);

        assertEquals(2, survey.getSize());
        assertEquals("¿Cuál es tu nombre?", survey.getQuestion(1).getQuestionText());
    }

    @Test
    public void testGetSurvey() {
        Survey survey = surveyController.initializeNewSurvey(TEST_TITLE, TEST_DESCRIPTION, TEST_CREATOR);

        Question question = new Question(0, survey.getSURVEY_ID());
        question.setQuestionText("Pregunta");
        question.setTypeQuestion(TypeQuestion.TEXTUAL);
        survey.addQuestion(question);

        Survey createdSurvey = surveyController.createSurvey(survey);
        String surveyId = createdSurvey.getSURVEY_ID();

        Survey retrievedSurvey = surveyController.getSurvey(surveyId);

        assertNotNull(retrievedSurvey);
        assertEquals(surveyId, retrievedSurvey.getSURVEY_ID());
        assertEquals(TEST_TITLE, retrievedSurvey.getTitle());
    }

    @Test
    public void testExistsSurvey() {
        Survey survey = surveyController.initializeNewSurvey(TEST_TITLE, TEST_DESCRIPTION, TEST_CREATOR);

        Question question = new Question(0, survey.getSURVEY_ID());
        question.setQuestionText("Pregunta");
        question.setTypeQuestion(TypeQuestion.TEXTUAL);
        survey.addQuestion(question);

        survey = surveyController.createSurvey(survey);
        String surveyId = survey.getSURVEY_ID();

        boolean exists = surveyController.existsSurvey(surveyId);

        assertTrue(exists);
    }

    @Test
    public void testExistsSurveyReturnsFalse() {
        boolean exists = surveyController.existsSurvey("non-existent-id");

        assertFalse(exists);
    }

    @Test
    public void testGetMySurveys() {
        // Crear primera encuesta
        Survey survey1 = surveyController.initializeNewSurvey("Survey 1", "Description 1", TEST_CREATOR);
        Question q1 = new Question(0, survey1.getSURVEY_ID());
        q1.setQuestionText("Pregunta 1");
        q1.setTypeQuestion(TypeQuestion.TEXTUAL);
        survey1.addQuestion(q1);
        surveyController.createSurvey(survey1);

        // Crear segunda encuesta
        Survey survey2 = surveyController.initializeNewSurvey("Survey 2", "Description 2", TEST_CREATOR);
        Question q2 = new Question(0, survey2.getSURVEY_ID());
        q2.setQuestionText("Pregunta 2");
        q2.setTypeQuestion(TypeQuestion.TEXTUAL);
        survey2.addQuestion(q2);
        surveyController.createSurvey(survey2);

        assertNotNull(surveyController.getMySurveys());
        assertTrue(surveyController.getMySurveys().size() >= 2);
    }

    @Test
    public void testIncrementResponseCount() {
        Survey survey = surveyController.initializeNewSurvey(TEST_TITLE, TEST_DESCRIPTION, TEST_CREATOR);

        Question question = new Question(0, survey.getSURVEY_ID());
        question.setQuestionText("Pregunta");
        question.setTypeQuestion(TypeQuestion.TEXTUAL);
        survey.addQuestion(question);

        survey = surveyController.createSurvey(survey);
        String surveyId = survey.getSURVEY_ID();

        int initialViews = survey.getViews();

        surveyController.incrementResponseCount(surveyId);

        Survey updatedSurvey = surveyController.getSurvey(surveyId);

        assertNotNull(updatedSurvey);
        assertEquals(initialViews + 1, updatedSurvey.getViews());
    }

    @Test
    public void testGetSurveysId() {
        // Crear primera encuesta
        Survey survey1 = surveyController.initializeNewSurvey("Survey 1", "Description 1", TEST_CREATOR);
        Question q1 = new Question(0, survey1.getSURVEY_ID());
        q1.setQuestionText("Pregunta 1");
        q1.setTypeQuestion(TypeQuestion.TEXTUAL);
        survey1.addQuestion(q1);
        surveyController.createSurvey(survey1);

        // Crear segunda encuesta
        Survey survey2 = surveyController.initializeNewSurvey("Survey 2", "Description 2", TEST_CREATOR);
        Question q2 = new Question(0, survey2.getSURVEY_ID());
        q2.setQuestionText("Pregunta 2");
        q2.setTypeQuestion(TypeQuestion.TEXTUAL);
        survey2.addQuestion(q2);
        surveyController.createSurvey(survey2);

        assertNotNull(surveyController.getSurveysId());
        assertTrue(surveyController.getSurveysId().size() >= 2);
    }

    @Test
    public void testCreateMultipleSurveys() {
        Survey survey1 = surveyController.initializeNewSurvey("Survey 1", "Description 1", TEST_CREATOR);
        Question q1 = new Question(0, survey1.getSURVEY_ID());
        q1.setQuestionText("Pregunta 1");
        q1.setTypeQuestion(TypeQuestion.TEXTUAL);
        survey1.addQuestion(q1);
        survey1 = surveyController.createSurvey(survey1);

        Survey survey2 = surveyController.initializeNewSurvey("Survey 2", "Description 2", TEST_CREATOR);
        Question q2 = new Question(0, survey2.getSURVEY_ID());
        q2.setQuestionText("Pregunta 2");
        q2.setTypeQuestion(TypeQuestion.TEXTUAL);
        survey2.addQuestion(q2);
        survey2 = surveyController.createSurvey(survey2);

        Survey survey3 = surveyController.initializeNewSurvey("Survey 3", "Description 3", TEST_CREATOR);
        Question q3 = new Question(0, survey3.getSURVEY_ID());
        q3.setQuestionText("Pregunta 3");
        q3.setTypeQuestion(TypeQuestion.TEXTUAL);
        survey3.addQuestion(q3);
        survey3 = surveyController.createSurvey(survey3);

        assertNotNull(survey1);
        assertNotNull(survey2);
        assertNotNull(survey3);

        assertNotEquals(survey1.getSURVEY_ID(), survey2.getSURVEY_ID());
        assertNotEquals(survey2.getSURVEY_ID(), survey3.getSURVEY_ID());
    }

    @Test
    public void testCreateSurveyWithMultipleQuestions() {
        Survey survey = new Survey(TEST_TITLE, TEST_DESCRIPTION, TEST_CREATOR);

        // Añadir múltiples preguntas
        Question question1 = new Question(0, survey.getSURVEY_ID());
        question1.setQuestionText("Pregunta 1");
        question1.setTypeQuestion(TypeQuestion.TEXTUAL);

        Question question2 = new Question(1, survey.getSURVEY_ID());
        question2.setQuestionText("Pregunta 2");
        question2.setTypeQuestion(TypeQuestion.NUMERICAL);

        survey.addQuestion(question1);
        survey.addQuestion(question2);

        Survey result = surveyController.createSurvey(survey);

        assertNotNull(result);
        assertNotNull(result.getSURVEY_ID());
        assertEquals(2, result.getSize());
    }
}
