package domain.controller;

import domain.model.Question;
import domain.model.MultipleChoiceQuestion;
import domain.model.enums.TypeQuestion;
import domain.service.QuestionService;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class QuestionControllerTest {

    private QuestionController questionController;
    private QuestionService questionService;

    private static final int TEST_QUESTION_INDEX = 0;
    private static final String TEST_SURVEY_ID = "123";
    private static final String TEST_QUESTION_TEXT = "¿Cuál es tu opinión?";


    @Before
    public void setUp() {
        questionController = new QuestionController(questionService);
    }

    @Test
    public void testCreateTextualQuestion() {
        Question result = questionController.createTextualQuestion(TEST_QUESTION_INDEX, TEST_SURVEY_ID, TEST_QUESTION_TEXT, true);

        assertNotNull(result);
        assertEquals(TEST_QUESTION_INDEX, result.getQuestionIndex());
        assertEquals(TEST_SURVEY_ID, result.getSURVEY_ID());
        assertEquals(TEST_QUESTION_TEXT, result.getQuestionText());
        assertEquals(TypeQuestion.TEXTUAL, result.getTypeQuestion());
        assertTrue(result.isRequired());
    }

    @Test
    public void testCreateTextualQuestionNotRequired() {
        Question result = questionController.createTextualQuestion(TEST_QUESTION_INDEX, TEST_SURVEY_ID, TEST_QUESTION_TEXT, false);

        assertNotNull(result);
        assertEquals(TEST_QUESTION_TEXT, result.getQuestionText());
        assertEquals(TypeQuestion.TEXTUAL, result.getTypeQuestion());
        assertFalse(result.isRequired());
    }

    @Test
    public void testCreateNumericalQuestion() {
        String questionText = "¿Cuántos años tienes?";
        Question result = questionController.createNumericalQuestion(TEST_QUESTION_INDEX, TEST_SURVEY_ID, questionText, true);

        assertNotNull(result);
        assertEquals(TEST_QUESTION_INDEX, result.getQuestionIndex());
        assertEquals(TEST_SURVEY_ID, result.getSURVEY_ID());
        assertEquals(questionText, result.getQuestionText());
        assertEquals(TypeQuestion.NUMERICAL, result.getTypeQuestion());
        assertTrue(result.isRequired());
    }

    @Test
    public void testCreateNumericalQuestionNotRequired() {
        String questionText = "¿Cuántos años tienes?";
        Question result = questionController.createNumericalQuestion(TEST_QUESTION_INDEX, TEST_SURVEY_ID, questionText, false);

        assertNotNull(result);
        assertFalse(result.isRequired());
    }

    @Test
    public void testCreateMultipleChoiceQuestion() {
        List<String> optionTexts = Arrays.asList("Opción 1", "Opción 2", "Opción 3");
        String questionText = "¿Cuál prefieres?";

        MultipleChoiceQuestion result = questionController.createMultipleChoiceQuestion(
                TEST_QUESTION_INDEX, TEST_SURVEY_ID, questionText, true, 1, 2, optionTexts
        );

        assertNotNull(result);
        assertEquals(TEST_QUESTION_INDEX, result.getQuestionIndex());
        assertEquals(TEST_SURVEY_ID, result.getSURVEY_ID());
        assertEquals(questionText, result.getQuestionText());
        assertEquals(TypeQuestion.MULTIPLE_CHOICE, result.getTypeQuestion());
        assertTrue(result.isRequired());
        assertEquals(1, result.getMinSelections());
        assertEquals(2, result.getMaxSelections());
        assertEquals(3, result.getOptionsSize());
    }

    @Test
    public void testCreateMultipleChoiceQuestionWithSingleSelection() {
        List<String> optionTexts = Arrays.asList("Sí", "No", "Tal vez");
        String questionText = "¿Estás de acuerdo?";

        MultipleChoiceQuestion result = questionController.createMultipleChoiceQuestion(
                TEST_QUESTION_INDEX, TEST_SURVEY_ID, questionText, true, 1, 1, optionTexts
        );

        assertNotNull(result);
        assertEquals(1, result.getMinSelections());
        assertEquals(1, result.getMaxSelections());
        assertEquals(3, result.getOptionsSize());
    }

    @Test
    public void testUpdateQuestionText() {
        Question question = questionController.createTextualQuestion(TEST_QUESTION_INDEX, TEST_SURVEY_ID, TEST_QUESTION_TEXT, true);
        String newText = "¿Cuál es tu nueva opinión?";

        questionController.updateQuestionText(question, newText);

        assertEquals(newText, question.getQuestionText());
    }

    @Test
    public void testToggleRequiredStatus() {
        Question question = questionController.createTextualQuestion(TEST_QUESTION_INDEX, TEST_SURVEY_ID, TEST_QUESTION_TEXT, true);

        assertTrue(question.isRequired());

        questionController.toggleRequiredStatus(question);

        assertFalse(question.isRequired());

        questionController.toggleRequiredStatus(question);

        assertTrue(question.isRequired());
    }

    @Test
    public void testAddOption() {
        List<String> optionTexts = Arrays.asList("Opción 1", "Opción 2");
        MultipleChoiceQuestion question = questionController.createMultipleChoiceQuestion(
                TEST_QUESTION_INDEX, TEST_SURVEY_ID, "Pregunta", true, 1, 2, optionTexts
        );

        assertEquals(2, question.getOptionsSize());

        questionController.addOption(question, "Opción 3");

        assertEquals(3, question.getOptionsSize());
        assertEquals("Opción 3", question.getOption(2).getOptionText());
    }

    @Test
    public void testUpdateOption() {
        List<String> optionTexts = Arrays.asList("Opción 1", "Opción 2", "Opción 3");
        MultipleChoiceQuestion question = questionController.createMultipleChoiceQuestion(
                TEST_QUESTION_INDEX, TEST_SURVEY_ID, "Pregunta", true, 1, 2, optionTexts
        );

        String newText = "Opción Actualizada";
        questionController.updateOption(question, 1, newText);

        assertEquals(newText, question.getOption(1).getOptionText());
    }

    @Test
    public void testRemoveOption() {
        List<String> optionTexts = Arrays.asList("Opción 1", "Opción 2", "Opción 3");
        MultipleChoiceQuestion question = questionController.createMultipleChoiceQuestion(
                TEST_QUESTION_INDEX, TEST_SURVEY_ID, "Pregunta", true, 1, 3, optionTexts
        );

        assertEquals(3, question.getOptionsSize());

        questionController.removeOption(question, 1);

        assertEquals(2, question.getOptionsSize());
        assertEquals("Opción 1", question.getOption(0).getOptionText());
        assertEquals("Opción 3", question.getOption(1).getOptionText());
    }

    @Test
    public void testUpdateMinSelections() {
        List<String> optionTexts = Arrays.asList("Opción 1", "Opción 2", "Opción 3", "Opción 4");
        MultipleChoiceQuestion question = questionController.createMultipleChoiceQuestion(
                TEST_QUESTION_INDEX, TEST_SURVEY_ID, "Pregunta", true, 1, 3, optionTexts
        );

        assertEquals(1, question.getMinSelections());

        questionController.updateMinSelections(question, 2);

        assertEquals(2, question.getMinSelections());
    }

    @Test
    public void testUpdateMaxSelections() {
        List<String> optionTexts = Arrays.asList("Opción 1", "Opción 2", "Opción 3", "Opción 4");
        MultipleChoiceQuestion question = questionController.createMultipleChoiceQuestion(
                TEST_QUESTION_INDEX, TEST_SURVEY_ID, "Pregunta", true, 1, 2, optionTexts
        );

        assertEquals(2, question.getMaxSelections());

        questionController.updateMaxSelections(question, 4);

        assertEquals(4, question.getMaxSelections());
    }
}
