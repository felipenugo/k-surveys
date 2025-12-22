package data;

import domain.model.*;
import domain.model.enums.TypeQuestion;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests para ResponseRepository.
 * Verifica operaciones CRUD y persistencia de respuestas en JSON.
 */
public class ResponseRepositoryTest {

    private ResponseRepository responseRepository;
    private static final String TEST_FILE_PATH = "./test_responses.json";
    private static final String TEST_SURVEY_ID = "survey_001";

    @Before
    public void setUp() {
        responseRepository = new ResponseRepository(TEST_FILE_PATH);
        responseRepository.clear();
    }

    @After
    public void tearDown() {
        File testFile = new File(TEST_FILE_PATH);
        if (testFile.exists()) {
            testFile.delete();
        }
    }

    // ───────────────────────────────────────────────
    // Helper methods
    // ───────────────────────────────────────────────

    private List<Question> createTestQuestions(String surveyId) {
        List<Question> questions = new ArrayList<>();

        Question q1 = new Question(0, surveyId);
        q1.setQuestionText("Pregunta textual");
        q1.setTypeQuestion(TypeQuestion.TEXTUAL);
        questions.add(q1);

        Question q2 = new Question(1, surveyId);
        q2.setQuestionText("Pregunta numérica");
        q2.setTypeQuestion(TypeQuestion.NUMERICAL);
        questions.add(q2);

        MultipleChoiceQuestion q3 = new MultipleChoiceQuestion(2, surveyId);
        q3.setQuestionText("Pregunta opción múltiple");
        q3.addOption(new OptionQuestion(0, "Opción 1"));
        q3.addOption(new OptionQuestion(1, "Opción 2"));
        q3.addOption(new OptionQuestion(2, "Opción 3"));
        questions.add(q3);

        return questions;
    }

    private Response createBasicResponse(String surveyId, String responderUsername) {
        List<Question> questions = createTestQuestions(surveyId);
        String responseId = responseRepository.getValidResponseId();
        return new Response(responseId, surveyId, responderUsername, questions);
    }

    // ───────────────────────────────────────────────
    // Tests de creación
    // ───────────────────────────────────────────────

    @Test
    public void testAddResponse() {
        Response response = createBasicResponse(TEST_SURVEY_ID, "responder1");
        responseRepository.addResponse(TEST_SURVEY_ID, response);

        assertNotNull(response.getRESPONSE_ID());
        assertTrue(responseRepository.existsResponse(TEST_SURVEY_ID, response.getRESPONSE_ID()));
    }

    @Test
    public void testGetResponse() {
        Response response = createBasicResponse(TEST_SURVEY_ID, "responder1");
        responseRepository.addResponse(TEST_SURVEY_ID, response);

        Response retrieved = responseRepository.getResponse(TEST_SURVEY_ID, response.getRESPONSE_ID());

        assertNotNull(retrieved);
        assertEquals("responder1", retrieved.getResponderUsername());
    }

    @Test
    public void testGetResponseReturnsNullForNonExistent() {
        responseRepository.addSurveyEntry(TEST_SURVEY_ID);
        Response retrieved = responseRepository.getResponse(TEST_SURVEY_ID, "nonExistentId");
        assertNull(retrieved);
    }

    // ───────────────────────────────────────────────
    // Tests de existencia
    // ───────────────────────────────────────────────

    @Test
    public void testExistsResponse() {
        Response response = createBasicResponse(TEST_SURVEY_ID, "responder1");
        responseRepository.addResponse(TEST_SURVEY_ID, response);

        assertTrue(responseRepository.existsResponse(TEST_SURVEY_ID, response.getRESPONSE_ID()));
        assertFalse(responseRepository.existsResponse(TEST_SURVEY_ID, "nonExistentId"));
    }

    // ───────────────────────────────────────────────
    // Tests de eliminación
    // ───────────────────────────────────────────────

    @Test
    public void testDeleteResponse() {
        Response response = createBasicResponse(TEST_SURVEY_ID, "responder1");
        responseRepository.addResponse(TEST_SURVEY_ID, response);
        String responseId = response.getRESPONSE_ID();

        assertTrue(responseRepository.existsResponse(TEST_SURVEY_ID, responseId));

        responseRepository.deleteResponse(TEST_SURVEY_ID, responseId);

        assertFalse(responseRepository.existsResponse(TEST_SURVEY_ID, responseId));
    }

    @Test
    public void testDeleteResponsesBySurvey() {
        Response response1 = createBasicResponse(TEST_SURVEY_ID, "responder1");
        Response response2 = createBasicResponse(TEST_SURVEY_ID, "responder2");

        responseRepository.addResponse(TEST_SURVEY_ID, response1);
        responseRepository.addResponse(TEST_SURVEY_ID, response2);

        responseRepository.deleteResponsesBySurvey(TEST_SURVEY_ID);

        assertFalse(responseRepository.existsResponse(TEST_SURVEY_ID, response1.getRESPONSE_ID()));
        assertFalse(responseRepository.existsResponse(TEST_SURVEY_ID, response2.getRESPONSE_ID()));
    }

    // ───────────────────────────────────────────────
    // Tests de actualización
    // ───────────────────────────────────────────────

    @Test
    public void testUpdateResponse() {
        Response response = createBasicResponse(TEST_SURVEY_ID, "responder1");
        responseRepository.addResponse(TEST_SURVEY_ID, response);

        // Actualizar una respuesta textual
        TextualAnswer answer = new TextualAnswer(0, response.getRESPONSE_ID());
        answer.setAnswerText("Mi respuesta");
        response.updateAnswer(0, answer);

        responseRepository.updateResponse(TEST_SURVEY_ID, response);

        Response retrieved = responseRepository.getResponse(TEST_SURVEY_ID, response.getRESPONSE_ID());
        Answer retrievedAnswer = retrieved.getAnswer(0);
        assertTrue(retrievedAnswer instanceof TextualAnswer);
        assertEquals("Mi respuesta", ((TextualAnswer) retrievedAnswer).getAnswerText());
    }

    // ───────────────────────────────────────────────
    // Tests de respuestas con diferentes tipos de answers
    // ───────────────────────────────────────────────

    @Test
    public void testResponseWithTextualAnswer() {
        Response response = createBasicResponse(TEST_SURVEY_ID, "responder1");

        TextualAnswer answer = new TextualAnswer(0, response.getRESPONSE_ID());
        answer.setAnswerText("Respuesta de texto");
        response.updateAnswer(0, answer);

        responseRepository.addResponse(TEST_SURVEY_ID, response);

        Response retrieved = responseRepository.getResponse(TEST_SURVEY_ID, response.getRESPONSE_ID());
        Answer retrievedAnswer = retrieved.getAnswer(0);

        assertTrue(retrievedAnswer instanceof TextualAnswer);
        assertEquals("Respuesta de texto", ((TextualAnswer) retrievedAnswer).getAnswerText());
    }

    @Test
    public void testResponseWithNumericalAnswer() {
        Response response = createBasicResponse(TEST_SURVEY_ID, "responder1");

        NumericalAnswer answer = new NumericalAnswer(1, response.getRESPONSE_ID());
        answer.setAnswerNum(42.5);
        response.updateAnswer(1, answer);

        responseRepository.addResponse(TEST_SURVEY_ID, response);

        Response retrieved = responseRepository.getResponse(TEST_SURVEY_ID, response.getRESPONSE_ID());
        Answer retrievedAnswer = retrieved.getAnswer(1);

        assertTrue(retrievedAnswer instanceof NumericalAnswer);
        assertEquals(42.5, ((NumericalAnswer) retrievedAnswer).getAnswerNum(), 0.01);
    }

    @Test
    public void testResponseWithMultipleChoiceAnswer() {
        Response response = createBasicResponse(TEST_SURVEY_ID, "responder1");

        // El MultipleChoiceAnswer ya se crea con el tamaño correcto en Response
        MultipleChoiceAnswer answer = (MultipleChoiceAnswer) response.getAnswer(2);
        answer.setOption(0, true);
        answer.setOption(2, true);

        responseRepository.addResponse(TEST_SURVEY_ID, response);

        Response retrieved = responseRepository.getResponse(TEST_SURVEY_ID, response.getRESPONSE_ID());
        Answer retrievedAnswer = retrieved.getAnswer(2);

        assertTrue(retrievedAnswer instanceof MultipleChoiceAnswer);
        MultipleChoiceAnswer mcAnswer = (MultipleChoiceAnswer) retrievedAnswer;
        assertTrue(mcAnswer.isOptionSelected(0));
        assertTrue(mcAnswer.isOptionSelected(2));
    }

    // ───────────────────────────────────────────────
    // Tests de obtener todas las respuestas
    // ───────────────────────────────────────────────

    @Test
    public void testGetAllResponsesForSurvey() {
        Response response1 = createBasicResponse(TEST_SURVEY_ID, "responder1");
        Response response2 = createBasicResponse(TEST_SURVEY_ID, "responder2");

        responseRepository.addResponse(TEST_SURVEY_ID, response1);
        responseRepository.addResponse(TEST_SURVEY_ID, response2);

        List<Response> responses = responseRepository.getAllResponses(TEST_SURVEY_ID);

        assertEquals(2, responses.size());
    }

    // ───────────────────────────────────────────────
    // Tests de persistencia
    // ───────────────────────────────────────────────

    @Test
    public void testPersistenceAfterReload() {
        Response response = createBasicResponse(TEST_SURVEY_ID, "responder1");

        TextualAnswer answer = new TextualAnswer(0, response.getRESPONSE_ID());
        answer.setAnswerText("Respuesta persistente");
        response.updateAnswer(0, answer);

        responseRepository.addResponse(TEST_SURVEY_ID, response);
        String responseId = response.getRESPONSE_ID();

        // Crear nueva instancia para simular reinicio
        ResponseRepository newRepository = new ResponseRepository(TEST_FILE_PATH);

        assertTrue(newRepository.existsResponse(TEST_SURVEY_ID, responseId));
        Response retrieved = newRepository.getResponse(TEST_SURVEY_ID, responseId);
        assertEquals("responder1", retrieved.getResponderUsername());
    }

    // ───────────────────────────────────────────────
    // Tests de clear
    // ───────────────────────────────────────────────

    @Test
    public void testClear() {
        Response response1 = createBasicResponse(TEST_SURVEY_ID, "responder1");
        Response response2 = createBasicResponse("survey_002", "responder2");

        responseRepository.addResponse(TEST_SURVEY_ID, response1);
        responseRepository.addResponse("survey_002", response2);

        responseRepository.clear();

        assertFalse(responseRepository.existsResponse(TEST_SURVEY_ID, response1.getRESPONSE_ID()));
        assertFalse(responseRepository.existsResponse("survey_002", response2.getRESPONSE_ID()));
    }

    // ───────────────────────────────────────────────
    // Tests de múltiples encuestas
    // ───────────────────────────────────────────────

    @Test
    public void testResponsesFromDifferentSurveys() {
        Response response1 = createBasicResponse("survey_001", "responder1");
        Response response2 = createBasicResponse("survey_002", "responder2");

        responseRepository.addResponse("survey_001", response1);
        responseRepository.addResponse("survey_002", response2);

        assertTrue(responseRepository.existsResponse("survey_001", response1.getRESPONSE_ID()));
        assertTrue(responseRepository.existsResponse("survey_002", response2.getRESPONSE_ID()));

        // Verificar que no hay cruce de datos
        assertFalse(responseRepository.existsResponse("survey_001", response2.getRESPONSE_ID()));
        assertFalse(responseRepository.existsResponse("survey_002", response1.getRESPONSE_ID()));
    }

    // ───────────────────────────────────────────────
    // Tests de entradas de encuesta
    // ───────────────────────────────────────────────

    @Test
    public void testAddSurveyEntry() {
        assertFalse(responseRepository.existsSurveyEntry(TEST_SURVEY_ID));

        responseRepository.addSurveyEntry(TEST_SURVEY_ID);

        assertTrue(responseRepository.existsSurveyEntry(TEST_SURVEY_ID));
    }

    @Test
    public void testDeleteSurveyEntry() {
        responseRepository.addSurveyEntry(TEST_SURVEY_ID);
        assertTrue(responseRepository.existsSurveyEntry(TEST_SURVEY_ID));

        responseRepository.deleteSurveyEntry(TEST_SURVEY_ID);

        assertFalse(responseRepository.existsSurveyEntry(TEST_SURVEY_ID));
    }
}
