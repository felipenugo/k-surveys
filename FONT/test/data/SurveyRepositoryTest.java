package data;

import domain.model.Survey;
import domain.model.Question;
import domain.model.MultipleChoiceQuestion;
import domain.model.OptionQuestion;
import domain.model.enums.TypeQuestion;
import domain.model.enums.SurveyStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests para SurveyRepository.
 * Verifica operaciones CRUD y persistencia de encuestas en JSON.
 */
public class SurveyRepositoryTest {

    private SurveyRepository surveyRepository;
    private static final String TEST_FILE_PATH = "./test_surveys.json";

    @Before
    public void setUp() {
        surveyRepository = new SurveyRepository(TEST_FILE_PATH);
        surveyRepository.clear();
    }

    @After
    public void tearDown() {
        File testFile = new File(TEST_FILE_PATH);
        if (testFile.exists()) {
            testFile.delete();
        }
    }

    // ───────────────────────────────────────────────
    // Tests de creación
    // ───────────────────────────────────────────────

    @Test
    public void testAddSurvey() {
        Survey survey = createBasicSurvey("Test Survey", "testUser");
        surveyRepository.addSurvey(survey);

        assertNotNull(survey.getSURVEY_ID());
        assertTrue(surveyRepository.existsSurvey(survey.getSURVEY_ID()));
    }

    @Test
    public void testGetSurvey() {
        Survey survey = createBasicSurvey("Test Survey", "testUser");
        surveyRepository.addSurvey(survey);

        Survey retrieved = surveyRepository.getSurvey(survey.getSURVEY_ID());

        assertNotNull(retrieved);
        assertEquals("Test Survey", retrieved.getTitle());
        assertEquals("testUser", retrieved.getCREATOR_USERNAME());
    }

    @Test
    public void testGetSurveyReturnsNullForNonExistent() {
        Survey retrieved = surveyRepository.getSurvey("nonExistentId");
        assertNull(retrieved);
    }

    // ───────────────────────────────────────────────
    // Tests de existencia
    // ───────────────────────────────────────────────

    @Test
    public void testExistsSurvey() {
        Survey survey = createBasicSurvey("Test Survey", "testUser");
        surveyRepository.addSurvey(survey);

        assertTrue(surveyRepository.existsSurvey(survey.getSURVEY_ID()));
        assertFalse(surveyRepository.existsSurvey("nonExistentId"));
    }

    // ───────────────────────────────────────────────
    // Tests de eliminación
    // ───────────────────────────────────────────────

    @Test
    public void testDeleteSurvey() {
        Survey survey = createBasicSurvey("Test Survey", "testUser");
        surveyRepository.addSurvey(survey);
        String surveyId = survey.getSURVEY_ID();

        assertTrue(surveyRepository.existsSurvey(surveyId));

        surveyRepository.deleteSurvey(surveyId);

        assertFalse(surveyRepository.existsSurvey(surveyId));
    }

    // ───────────────────────────────────────────────
    // Tests de actualización
    // ───────────────────────────────────────────────

    @Test
    public void testUpdateSurvey() {
        Survey survey = createBasicSurvey("Original Title", "testUser");
        surveyRepository.addSurvey(survey);
        String surveyId = survey.getSURVEY_ID();

        survey.setTitle("Updated Title");
        surveyRepository.updateSurvey(surveyId, survey);

        Survey retrieved = surveyRepository.getSurvey(surveyId);
        assertEquals("Updated Title", retrieved.getTitle());
    }

    // ───────────────────────────────────────────────
    // Tests de encuestas con preguntas
    // ───────────────────────────────────────────────

    @Test
    public void testSurveyWithQuestions() {
        Survey survey = createBasicSurvey("Survey with Questions", "testUser");

        // Añadir pregunta textual
        Question q1 = new Question(0, survey.getSURVEY_ID());
        q1.setQuestionText("¿Cuál es tu nombre?");
        q1.setTypeQuestion(TypeQuestion.TEXTUAL);
        survey.addQuestion(q1);

        // Añadir pregunta numérica
        Question q2 = new Question(1, survey.getSURVEY_ID());
        q2.setQuestionText("¿Cuántos años tienes?");
        q2.setTypeQuestion(TypeQuestion.NUMERICAL);
        survey.addQuestion(q2);

        surveyRepository.addSurvey(survey);

        Survey retrieved = surveyRepository.getSurvey(survey.getSURVEY_ID());
        assertEquals(2, retrieved.getQuestions().size());
    }

    @Test
    public void testSurveyWithMultipleChoiceQuestion() {
        Survey survey = createBasicSurvey("Multiple Choice Survey", "testUser");

        MultipleChoiceQuestion mcq = new MultipleChoiceQuestion(0, survey.getSURVEY_ID());
        mcq.setQuestionText("¿Cuál es tu color favorito?");
        mcq.addOption(new OptionQuestion(0, "Rojo"));
        mcq.addOption(new OptionQuestion(1, "Azul"));
        mcq.addOption(new OptionQuestion(2, "Verde"));
        mcq.setMinSelections(1);
        mcq.setMaxSelections(2);
        survey.addQuestion(mcq);

        surveyRepository.addSurvey(survey);

        Survey retrieved = surveyRepository.getSurvey(survey.getSURVEY_ID());
        assertEquals(1, retrieved.getQuestions().size());

        Question retrievedQ = retrieved.getQuestions().get(0);
        assertTrue(retrievedQ instanceof MultipleChoiceQuestion);

        MultipleChoiceQuestion retrievedMcq = (MultipleChoiceQuestion) retrievedQ;
        assertEquals(3, retrievedMcq.getOptionsSize());
    }

    // ───────────────────────────────────────────────
    // Tests de estados de encuesta
    // ───────────────────────────────────────────────

    @Test
    public void testSurveyStatusChange() {
        Survey survey = createBasicSurvey("State Test Survey", "testUser");
        surveyRepository.addSurvey(survey);
        String surveyId = survey.getSURVEY_ID();

        survey.setSurveyStatus(SurveyStatus.PUBLISHED);
        surveyRepository.updateSurvey(surveyId, survey);

        Survey retrieved = surveyRepository.getSurvey(surveyId);
        assertEquals(SurveyStatus.PUBLISHED, retrieved.getSurveyStatus());
    }

    // ───────────────────────────────────────────────
    // Tests de persistencia
    // ───────────────────────────────────────────────

    @Test
    public void testPersistenceAfterReload() {
        Survey survey = createBasicSurvey("Persistent Survey", "testUser");
        surveyRepository.addSurvey(survey);
        String surveyId = survey.getSURVEY_ID();

        // Crear nueva instancia para simular reinicio
        SurveyRepository newRepository = new SurveyRepository(TEST_FILE_PATH);

        assertTrue(newRepository.existsSurvey(surveyId));
        Survey retrieved = newRepository.getSurvey(surveyId);
        assertEquals("Persistent Survey", retrieved.getTitle());
    }

    // ───────────────────────────────────────────────
    // Tests de clear
    // ───────────────────────────────────────────────

    @Test
    public void testClear() {
        Survey survey1 = createBasicSurvey("Survey 1", "user1");
        Survey survey2 = createBasicSurvey("Survey 2", "user2");

        surveyRepository.addSurvey(survey1);
        surveyRepository.addSurvey(survey2);

        surveyRepository.clear();

        assertFalse(surveyRepository.existsSurvey(survey1.getSURVEY_ID()));
        assertFalse(surveyRepository.existsSurvey(survey2.getSURVEY_ID()));
    }

    // ───────────────────────────────────────────────
    // Tests de generación de IDs
    // ───────────────────────────────────────────────

    @Test
    public void testAutoIncrementId() {
        Survey survey1 = createBasicSurvey("Survey 1", "user1");
        Survey survey2 = createBasicSurvey("Survey 2", "user2");

        surveyRepository.addSurvey(survey1);
        surveyRepository.addSurvey(survey2);

        assertNotEquals(survey1.getSURVEY_ID(), survey2.getSURVEY_ID());
    }

    // ───────────────────────────────────────────────
    // Helper methods
    // ───────────────────────────────────────────────

    private Survey createBasicSurvey(String title, String creator) {
        // Generar un ID usando el repositorio, similar a como lo hace el servicio
        String surveyId = surveyRepository.generateNextSurveyId();
        return new Survey(surveyId, title, "Descripción de prueba", creator);
    }
}

