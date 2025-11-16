package domain.model;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class OptionQuestionTest {

    private OptionQuestion optionQuestion;
    private static final int TEST_QUESTION_INDEX = 0;
    private static final String TEST_SURVEY_ID = "123";

    @Before
    public void setUp() {
        optionQuestion = new OptionQuestion(TEST_QUESTION_INDEX, TEST_SURVEY_ID);
    }

    @Test
    public void testOptionQuestionCreation() {
        assertNotNull(optionQuestion);
        assertEquals(TEST_QUESTION_INDEX, optionQuestion.getQuestionIndex());
        assertEquals(TEST_SURVEY_ID, optionQuestion.getSurveyId());
        assertEquals("", optionQuestion.getOptionText()); // default empty
    }

    @Test
    public void testSetQuestionIndex() {
        int newIndex = 3;
        optionQuestion.setQuestionIndex(newIndex);
        assertEquals(newIndex, optionQuestion.getQuestionIndex());
    }

    @Test
    public void testSetOptionText() {
        String optionText = "Opción A";
        optionQuestion.setOptionText(optionText);
        assertEquals(optionText, optionQuestion.getOptionText());
    }

    @Test
    public void testSetOptionTextWithSpecialCharacters() {
        String optionText = "Opción con caracteres especiales: áéíóú ñ ¿?";
        optionQuestion.setOptionText(optionText);
        assertEquals(optionText, optionQuestion.getOptionText());
    }

    @Test
    public void testSetEmptyOptionText() {
        optionQuestion.setOptionText("");
        assertEquals("", optionQuestion.getOptionText());
    }

    @Test
    public void testSetOptionTextMultipleTimes() {
        optionQuestion.setOptionText("Primera opción");
        assertEquals("Primera opción", optionQuestion.getOptionText());

        optionQuestion.setOptionText("Segunda opción");
        assertEquals("Segunda opción", optionQuestion.getOptionText());

        optionQuestion.setOptionText("Tercera opción");
        assertEquals("Tercera opción", optionQuestion.getOptionText());
    }

    @Test
    public void testSurveyIdIsImmutable() {
        // El surveyId es final, por lo que no debe tener setter
        assertEquals(TEST_SURVEY_ID, optionQuestion.getSurveyId());

        // Crear otra opción con diferente surveyId
        OptionQuestion anotherOption = new OptionQuestion(1, "another-survey-id");
        assertEquals("another-survey-id", anotherOption.getSurveyId());

        // Verificar que el surveyId original no cambió
        assertEquals(TEST_SURVEY_ID, optionQuestion.getSurveyId());
    }

    @Test
    public void testDefaultValues() {
        OptionQuestion newOption = new OptionQuestion(5, "test-survey-id");

        // Verificar valores por defecto
        assertEquals(5, newOption.getQuestionIndex());
        assertEquals("test-survey-id", newOption.getSurveyId());
        assertEquals("", newOption.getOptionText());
    }

    @Test
    public void testMultipleOptionsForSameQuestion() {
        // Simular múltiples opciones para la misma pregunta (mismo questionIndex y surveyId)
        OptionQuestion option1 = new OptionQuestion(TEST_QUESTION_INDEX, TEST_SURVEY_ID);
        OptionQuestion option2 = new OptionQuestion(TEST_QUESTION_INDEX, TEST_SURVEY_ID);
        OptionQuestion option3 = new OptionQuestion(TEST_QUESTION_INDEX, TEST_SURVEY_ID);

        option1.setOptionText("Opción 1");
        option2.setOptionText("Opción 2");
        option3.setOptionText("Opción 3");

        // Verificar que todas tienen el mismo questionIndex y surveyId
        assertEquals(TEST_QUESTION_INDEX, option1.getQuestionIndex());
        assertEquals(TEST_QUESTION_INDEX, option2.getQuestionIndex());
        assertEquals(TEST_QUESTION_INDEX, option3.getQuestionIndex());

        assertEquals(TEST_SURVEY_ID, option1.getSurveyId());
        assertEquals(TEST_SURVEY_ID, option2.getSurveyId());
        assertEquals(TEST_SURVEY_ID, option3.getSurveyId());

        // Verificar que cada una tiene su propio texto
        assertEquals("Opción 1", option1.getOptionText());
        assertEquals("Opción 2", option2.getOptionText());
        assertEquals("Opción 3", option3.getOptionText());
    }

    @Test
    public void testSetOptionTextWithLongText() {
        String longText = "Esta es una opción muy larga que podría contener mucha información " +
                "y que debería ser manejada correctamente por la clase OptionQuestion " +
                "sin problemas de ningún tipo.";
        optionQuestion.setOptionText(longText);
        assertEquals(longText, optionQuestion.getOptionText());
    }

    @Test
    public void testSetOptionTextWithNumbers() {
        String optionWithNumbers = "Opción 1: De 18 a 25 años";
        optionQuestion.setOptionText(optionWithNumbers);
        assertEquals(optionWithNumbers, optionQuestion.getOptionText());
    }

    @Test
    public void testDifferentQuestionIndexes() {
        OptionQuestion option1 = new OptionQuestion(0, TEST_SURVEY_ID);
        OptionQuestion option2 = new OptionQuestion(1, TEST_SURVEY_ID);
        OptionQuestion option3 = new OptionQuestion(2, TEST_SURVEY_ID);

        option1.setOptionText("Opción de pregunta 0");
        option2.setOptionText("Opción de pregunta 1");
        option3.setOptionText("Opción de pregunta 2");

        assertEquals(0, option1.getQuestionIndex());
        assertEquals(1, option2.getQuestionIndex());
        assertEquals(2, option3.getQuestionIndex());

        assertEquals("Opción de pregunta 0", option1.getOptionText());
        assertEquals("Opción de pregunta 1", option2.getOptionText());
        assertEquals("Opción de pregunta 2", option3.getOptionText());
    }

    @Test
    public void testUpdateQuestionIndex() {
        optionQuestion.setQuestionIndex(5);
        assertEquals(5, optionQuestion.getQuestionIndex());

        optionQuestion.setQuestionIndex(10);
        assertEquals(10, optionQuestion.getQuestionIndex());

        optionQuestion.setQuestionIndex(0);
        assertEquals(0, optionQuestion.getQuestionIndex());
    }

    @Test
    public void testCompleteOptionConfiguration() {
        // Test configurando una opción completa
        int questionIndex = 2;
        String surveyId = "456";
        String optionText = "Muy de acuerdo";

        OptionQuestion completeOption = new OptionQuestion(questionIndex, surveyId);
        completeOption.setOptionText(optionText);

        assertEquals(questionIndex, completeOption.getQuestionIndex());
        assertEquals(surveyId, completeOption.getSurveyId());
        assertEquals(optionText, completeOption.getOptionText());
    }
}
