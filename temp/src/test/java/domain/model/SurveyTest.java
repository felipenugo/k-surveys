package domain.model;

import domain.model.enums.SurveyStatus;
import domain.model.enums.TypeQuestion;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class SurveyTest {

    private Survey survey;
    private static final String TEST_SURVEY_ID = "survey-123";
    private static final String TEST_TITLE = "Test Survey";
    private static final String TEST_DESCRIPTION = "A survey for testing purposes";
    private static final String TEST_CREATOR = "tester";

    @Before
    public void setUp() {
        survey = new Survey(TEST_SURVEY_ID, TEST_TITLE, TEST_DESCRIPTION, TEST_CREATOR);
    }

    @Test
    public void testSurveyCreationWithId() {
        assertNotNull(survey);
        assertEquals(TEST_SURVEY_ID, survey.getSURVEY_ID());
        assertEquals(TEST_TITLE, survey.getTitle());
        assertEquals(TEST_DESCRIPTION, survey.getDescription());
        assertEquals(TEST_CREATOR, survey.getCREATOR_USERNAME());
        assertNotNull(survey.getCREATED_AT());
        assertNull(survey.getPUBLISHED_AT());
        assertEquals(SurveyStatus.DRAFT, survey.getSurveyStatus());  // ← CORREGIDO
        assertEquals(0.0, survey.getAvgRating(), 0.001);
        assertEquals(0, survey.getViews());
        assertEquals(0, survey.getSize());
    }

    @Test
    public void testSurveyCreationWithoutId() {
        Survey newSurvey = new Survey(TEST_TITLE, TEST_DESCRIPTION, TEST_CREATOR);

        assertNotNull(newSurvey);
        assertNull(newSurvey.getSURVEY_ID());
        assertEquals(TEST_TITLE, newSurvey.getTitle());
        assertEquals(TEST_DESCRIPTION, newSurvey.getDescription());
        assertEquals(TEST_CREATOR, newSurvey.getCREATOR_USERNAME());
        assertNotNull(newSurvey.getCREATED_AT());
        assertNull(newSurvey.getPUBLISHED_AT());
        assertEquals(SurveyStatus.DRAFT, newSurvey.getSurveyStatus());
    }

    @Test
    public void testSetTitle() {
        String newTitle = "Updated Survey Title";
        survey.setTitle(newTitle);
        assertEquals(newTitle, survey.getTitle());
    }

    @Test
    public void testSetDescription() {
        String newDescription = "Updated description for the survey";
        survey.setDescription(newDescription);
        assertEquals(newDescription, survey.getDescription());
    }

    @Test
    public void testSetSurveyStatus() {
        survey.setSurveyStatus(SurveyStatus.PUBLISHED);
        assertEquals(SurveyStatus.PUBLISHED, survey.getSurveyStatus());

        survey.setSurveyStatus(SurveyStatus.CLOSED);
        assertEquals(SurveyStatus.CLOSED, survey.getSurveyStatus());
    }

    @Test
    public void testsetPUBLISHED_AT() {
        assertNull(survey.getPUBLISHED_AT());

        survey.setPUBLISHED_AT();

        assertNotNull(survey.getPUBLISHED_AT());
    }

    @Test
    public void testSetAvgRating() {
        survey.setAvgRating(4.5);
        assertEquals(4.5, survey.getAvgRating(), 0.001);
    }

    @Test
    public void testSetViews() {
        survey.setViews(100);
        assertEquals(100, survey.getViews());
    }

    @Test
    public void testAddQuestion() {
        Question question = new Question(0, TEST_SURVEY_ID);
        question.setQuestionText("¿Cuál es tu nombre?");

        survey.addQuestion(question);

        assertEquals(1, survey.getSize());
        assertEquals(question, survey.getQuestion(0));
    }

    @Test
    public void testAddMultipleQuestions() {
        Question question1 = new Question(0, TEST_SURVEY_ID);
        question1.setQuestionText("Pregunta 1");
        Question question2 = new Question(1, TEST_SURVEY_ID);
        question2.setQuestionText("Pregunta 2");
        Question question3 = new Question(2, TEST_SURVEY_ID);
        question3.setQuestionText("Pregunta 3");

        survey.addQuestion(question1);
        survey.addQuestion(question2);
        survey.addQuestion(question3);

        assertEquals(3, survey.getSize());
        assertEquals("Pregunta 1", survey.getQuestion(0).getQuestionText());
        assertEquals("Pregunta 2", survey.getQuestion(1).getQuestionText());
        assertEquals("Pregunta 3", survey.getQuestion(2).getQuestionText());
    }

    @Test
    public void testRemoveQuestion() {
        addThreeQuestions();

        assertEquals(3, survey.getSize());

        survey.removeQuestion(1);

        assertEquals(2, survey.getSize());
        assertEquals("Pregunta 1", survey.getQuestion(0).getQuestionText());
        assertEquals("Pregunta 3", survey.getQuestion(1).getQuestionText());
    }

    @Test
    public void testUpdateQuestion() {
        addThreeQuestions();

        Question newQuestion = new Question(1, TEST_SURVEY_ID);
        newQuestion.setQuestionText("Pregunta Actualizada");

        survey.updateQuestion(1, newQuestion);

        assertEquals("Pregunta Actualizada", survey.getQuestion(1).getQuestionText());
    }

    @Test
    public void testReorderQuestion() {
        addThreeQuestions();

        survey.reorderQuestion(0, 2);

        assertEquals("Pregunta 2", survey.getQuestion(0).getQuestionText());
        assertEquals("Pregunta 3", survey.getQuestion(1).getQuestionText());
        assertEquals("Pregunta 1", survey.getQuestion(2).getQuestionText());
    }

    @Test
    public void testGetQuestions() {
        addThreeQuestions();

        assertEquals(3, survey.getQuestions().size());
        assertEquals("Pregunta 1", survey.getQuestions().get(0).getQuestionText());
        assertEquals("Pregunta 2", survey.getQuestions().get(1).getQuestionText());
        assertEquals("Pregunta 3", survey.getQuestions().get(2).getQuestionText());
    }

    @Test
    public void testClearQuestions() {
        addThreeQuestions();

        assertEquals(3, survey.getSize());

        survey.clearQuestions();

        assertEquals(0, survey.getSize());
    }

    @Test
    public void testGetSize() {
        assertEquals(0, survey.getSize());

        survey.addQuestion(new Question(0, TEST_SURVEY_ID));
        assertEquals(1, survey.getSize());

        survey.addQuestion(new Question(1, TEST_SURVEY_ID));
        assertEquals(2, survey.getSize());

        survey.removeQuestion(0);
        assertEquals(1, survey.getSize());
    }

    @Test
    public void testImmutableFields() {
        assertEquals(TEST_SURVEY_ID, survey.getSURVEY_ID());
        assertEquals(TEST_CREATOR, survey.getCREATOR_USERNAME());

        assertNotNull(survey.getCREATED_AT());
    }

    @Test
    public void testCompleteSurveyConfiguration() {
        survey.setTitle("Encuesta de Satisfacción");
        survey.setDescription("Encuesta para medir la satisfacción de los clientes");

        Question textQuestion = new Question(0, TEST_SURVEY_ID);
        textQuestion.setQuestionText("¿Cuál es tu opinión?");
        textQuestion.setTypeQuestion(TypeQuestion.TEXTUAL);

        MultipleChoiceQuestion mcQuestion = new MultipleChoiceQuestion(1, TEST_SURVEY_ID);
        mcQuestion.setQuestionText("¿Cómo calificarías el servicio?");
        OptionQuestion opt1 = new OptionQuestion(1, TEST_SURVEY_ID);
        opt1.setOptionText("Excelente");
        OptionQuestion opt2 = new OptionQuestion(1, TEST_SURVEY_ID);
        opt2.setOptionText("Bueno");
        mcQuestion.addOption(opt1);
        mcQuestion.addOption(opt2);

        Question numericalQuestion = new Question(2, TEST_SURVEY_ID);
        numericalQuestion.setQuestionText("¿Cuántos años tienes?");
        numericalQuestion.setTypeQuestion(TypeQuestion.NUMERICAL);

        survey.addQuestion(textQuestion);
        survey.addQuestion(mcQuestion);
        survey.addQuestion(numericalQuestion);

        survey.setSurveyStatus(SurveyStatus.PUBLISHED);
        survey.setPUBLISHED_AT();
        survey.setAvgRating(4.2);
        survey.setViews(150);

        assertEquals("Encuesta de Satisfacción", survey.getTitle());
        assertEquals("Encuesta para medir la satisfacción de los clientes", survey.getDescription());
        assertEquals(3, survey.getSize());
        assertEquals(SurveyStatus.PUBLISHED, survey.getSurveyStatus());
        assertNotNull(survey.getPUBLISHED_AT());
        assertEquals(4.2, survey.getAvgRating(), 0.001);
        assertEquals(150, survey.getViews());
    }

    @Test
    public void testSurveyLifecycle() {
        assertEquals(SurveyStatus.DRAFT, survey.getSurveyStatus());
        assertNull(survey.getPUBLISHED_AT());

        addThreeQuestions();
        assertEquals(3, survey.getSize());

        survey.setSurveyStatus(SurveyStatus.PUBLISHED);
        survey.setPUBLISHED_AT();
        assertEquals(SurveyStatus.PUBLISHED, survey.getSurveyStatus());
        assertNotNull(survey.getPUBLISHED_AT());

        survey.setViews(50);
        survey.setAvgRating(4.0);
        assertEquals(50, survey.getViews());
        assertEquals(4.0, survey.getAvgRating(), 0.001);

        survey.setSurveyStatus(SurveyStatus.CLOSED);
        assertEquals(SurveyStatus.CLOSED, survey.getSurveyStatus());
    }

    @Test
    public void testMultipleQuestionTypes() {
        Question textQ = new Question(0, TEST_SURVEY_ID);
        textQ.setQuestionText("Pregunta textual");
        textQ.setTypeQuestion(TypeQuestion.TEXTUAL);

        MultipleChoiceQuestion mcQ = new MultipleChoiceQuestion(1, TEST_SURVEY_ID);
        mcQ.setQuestionText("Pregunta de opción múltiple");

        Question numQ = new Question(2, TEST_SURVEY_ID);
        numQ.setQuestionText("Pregunta numérica");
        numQ.setTypeQuestion(TypeQuestion.NUMERICAL);

        survey.addQuestion(textQ);
        survey.addQuestion(mcQ);
        survey.addQuestion(numQ);

        assertEquals(3, survey.getSize());
        assertEquals(TypeQuestion.TEXTUAL, survey.getQuestion(0).getTypeQuestion());
        assertEquals(TypeQuestion.MULTIPLE_CHOICE, survey.getQuestion(1).getTypeQuestion());  // ← CORREGIDO
        assertEquals(TypeQuestion.NUMERICAL, survey.getQuestion(2).getTypeQuestion());
    }

    @Test
    public void testEmptySurvey() {
        assertEquals(0, survey.getSize());
        assertTrue(survey.getQuestions().isEmpty());

        survey.setSurveyStatus(SurveyStatus.PUBLISHED);
        assertEquals(SurveyStatus.PUBLISHED, survey.getSurveyStatus());
    }

    @Test
    public void testModifyPublishedSurvey() {
        addThreeQuestions();

        survey.setSurveyStatus(SurveyStatus.PUBLISHED);
        survey.setPUBLISHED_AT();

        survey.setTitle("Título Modificado");
        assertEquals("Título Modificado", survey.getTitle());

        survey.setDescription("Descripción Modificada");
        assertEquals("Descripción Modificada", survey.getDescription());

        Question newQuestion = new Question(3, TEST_SURVEY_ID);
        newQuestion.setQuestionText("Nueva pregunta");
        survey.addQuestion(newQuestion);
        assertEquals(4, survey.getSize());
    }

    private void addThreeQuestions() {
        Question question1 = new Question(0, TEST_SURVEY_ID);
        question1.setQuestionText("Pregunta 1");
        Question question2 = new Question(1, TEST_SURVEY_ID);
        question2.setQuestionText("Pregunta 2");
        Question question3 = new Question(2, TEST_SURVEY_ID);
        question3.setQuestionText("Pregunta 3");

        survey.addQuestion(question1);
        survey.addQuestion(question2);
        survey.addQuestion(question3);
    }
}
