package domain.model;

import domain.model.enums.TypeQuestion;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class QuestionTest {

    private Question question;
    private static final String TEST_SURVEY_ID = "survey-123";
    private static final int TEST_QUESTION_INDEX = 0;

    @Before
    public void setUp() {
        question = new Question(TEST_QUESTION_INDEX, TEST_SURVEY_ID);
    }

    @Test
    public void testQuestionCreation() {
        assertNotNull(question);
        assertEquals(TEST_QUESTION_INDEX, question.getQuestionIndex());
        assertEquals(TEST_SURVEY_ID, question.getSURVEY_ID());
        assertEquals(TypeQuestion.TEXTUAL, question.getTypeQuestion()); // default value
        assertEquals("", question.getQuestionText()); // default empty
        assertTrue(question.isRequired()); // default true
    }

    @Test
    public void testSetQuestionIndex() {
        int newIndex = 5;
        question.setQuestionIndex(newIndex);
        assertEquals(newIndex, question.getQuestionIndex());
    }

    @Test
    public void testSetQuestionText() {
        String questionText = "¿Cuál es tu color favorito?";
        question.setQuestionText(questionText);
        assertEquals(questionText, question.getQuestionText());
    }

    @Test
    public void testSetTypeQuestion() {
        question.setTypeQuestion(TypeQuestion.MULTIPLE_CHOICE);
        assertEquals(TypeQuestion.MULTIPLE_CHOICE, question.getTypeQuestion());

        question.setTypeQuestion(TypeQuestion.NUMERICAL);
        assertEquals(TypeQuestion.NUMERICAL, question.getTypeQuestion());

        question.setTypeQuestion(TypeQuestion.TEXTUAL);
        assertEquals(TypeQuestion.TEXTUAL, question.getTypeQuestion());
    }

    @Test
    public void testSetRequired() {
        question.setRequired(false);
        assertFalse(question.isRequired());

        question.setRequired(true);
        assertTrue(question.isRequired());
    }

    @Test
    public void testCopy() {
        // Configurar la pregunta original
        question.setQuestionText("Pregunta de prueba");
        question.setTypeQuestion(TypeQuestion.NUMERICAL);
        question.setRequired(false);
        question.setQuestionIndex(3);


        Question copy = question.copy();

        // Verificar que la copia tiene los mismos valores
        assertNotNull(copy);
        assertEquals(question.getQuestionIndex(), copy.getQuestionIndex());
        assertEquals(question.getSURVEY_ID(), copy.getSURVEY_ID());
        assertEquals(question.getQuestionText(), copy.getQuestionText());
        assertEquals(question.getTypeQuestion(), copy.getTypeQuestion());
        assertEquals(question.isRequired(), copy.isRequired());

        // Verificar que es una copia independiente (deep copy)
        copy.setQuestionText("Texto modificado");
        assertNotEquals(question.getQuestionText(), copy.getQuestionText());
    }

    @Test
    public void testSurveyIdIsImmutable() {
        // El SURVEYID es final, por lo que no debe tener setter
        // Este test verifica que el valor se mantiene después de la creación
        assertEquals(TEST_SURVEY_ID, question.getSURVEY_ID());

        // Intentar crear otra pregunta con diferente surveyId
        Question anotherQuestion = new Question(1, "another-survey-id");
        assertEquals("another-survey-id", anotherQuestion.getSURVEY_ID());

        // Verificar que el surveyId original no cambió
        assertEquals(TEST_SURVEY_ID, question.getSURVEY_ID());
    }

    @Test
    public void testDefaultValues() {
        Question newQuestion = new Question(10, "test-survey");

        // Verificar valores por defecto
        assertEquals(TypeQuestion.TEXTUAL, newQuestion.getTypeQuestion());
        assertEquals("", newQuestion.getQuestionText());
        assertTrue(newQuestion.isRequired());
    }

    @Test
    public void testMultipleModifications() {
        // Probar múltiples modificaciones en secuencia
        question.setQuestionText("Primera pregunta");
        question.setTypeQuestion(TypeQuestion.MULTIPLE_CHOICE);
        question.setRequired(false);
        question.setQuestionIndex(7);

        assertEquals("Primera pregunta", question.getQuestionText());
        assertEquals(TypeQuestion.MULTIPLE_CHOICE, question.getTypeQuestion());
        assertFalse(question.isRequired());
        assertEquals(7, question.getQuestionIndex());

        // Modificar de nuevo
        question.setQuestionText("Pregunta modificada");
        question.setTypeQuestion(TypeQuestion.NUMERICAL);
        question.setRequired(true);

        assertEquals("Pregunta modificada", question.getQuestionText());
        assertEquals(TypeQuestion.NUMERICAL, question.getTypeQuestion());
        assertTrue(question.isRequired());
    }
}
