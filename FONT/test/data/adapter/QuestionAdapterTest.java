package data.adapter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import domain.model.MultipleChoiceQuestion;
import domain.model.OptionQuestion;
import domain.model.Question;
import domain.model.enums.TypeQuestion;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests para QuestionAdapter.
 * Verifica la deserialización correcta de diferentes tipos de Question.
 */
public class QuestionAdapterTest {

    private Gson gson;

    @Before
    public void setUp() {
        gson = new GsonBuilder()
                .registerTypeAdapter(Question.class, new QuestionAdapter())
                .create();
    }

    // ───────────────────────────────────────────────
    // Tests de deserialización de Question textual
    // ───────────────────────────────────────────────

    @Test
    public void testDeserializeTextualQuestion() {
        // Crear pregunta original, serializar y deserializar (round-trip)
        Question original = new Question(0, "survey_001");
        original.setQuestionText("¿Cuál es tu nombre?");
        original.setTypeQuestion(TypeQuestion.TEXTUAL);
        original.setRequired(true);

        String json = gson.toJson(original);
        Question question = gson.fromJson(json, Question.class);

        assertNotNull(question);
        assertEquals(0, question.getQuestionIndex());
        assertEquals("survey_001", question.getSURVEY_ID());
        assertEquals("¿Cuál es tu nombre?", question.getQuestionText());
        assertEquals(TypeQuestion.TEXTUAL, question.getTypeQuestion());
        assertTrue(question.isRequired());
    }

    // ───────────────────────────────────────────────
    // Tests de deserialización de Question numérica
    // ───────────────────────────────────────────────

    @Test
    public void testDeserializeNumericalQuestion() {
        // Crear pregunta original, serializar y deserializar (round-trip)
        Question original = new Question(1, "survey_001");
        original.setQuestionText("¿Cuántos años tienes?");
        original.setTypeQuestion(TypeQuestion.NUMERICAL);
        original.setRequired(false);

        String json = gson.toJson(original);
        Question question = gson.fromJson(json, Question.class);

        assertNotNull(question);
        assertEquals(TypeQuestion.NUMERICAL, question.getTypeQuestion());
        assertFalse(question.isRequired());
    }

    // ───────────────────────────────────────────────
    // Tests de deserialización de MultipleChoiceQuestion
    // ───────────────────────────────────────────────

    @Test
    public void testDeserializeMultipleChoiceQuestion() {
        String json = "{" +
                "\"questionIndex\": 2," +
                "\"SURVEY_ID\": \"survey_001\"," +
                "\"questionText\": \"¿Cuál es tu color favorito?\"," +
                "\"typeQuestion\": \"MULTIPLE_CHOICE\"," +
                "\"required\": true," +
                "\"minSelections\": 1," +
                "\"maxSelections\": 2," +
                "\"options\": [" +
                "  {\"questionIndex\": 0, \"optionText\": \"Rojo\"}," +
                "  {\"questionIndex\": 1, \"optionText\": \"Azul\"}," +
                "  {\"questionIndex\": 2, \"optionText\": \"Verde\"}" +
                "]" +
                "}";

        Question question = gson.fromJson(json, Question.class);

        assertTrue(question instanceof MultipleChoiceQuestion);
        MultipleChoiceQuestion mcq = (MultipleChoiceQuestion) question;

        assertEquals(TypeQuestion.MULTIPLE_CHOICE, mcq.getTypeQuestion());
        assertEquals(1, mcq.getMinSelections());
        assertEquals(2, mcq.getMaxSelections());
        assertEquals(3, mcq.getOptionsSize());
    }

    // ───────────────────────────────────────────────
    // Tests de compatibilidad con formato antiguo
    // ───────────────────────────────────────────────

    @Test
    public void testDeserializeMultipleChoiceWithOptionQuestions() {
        // Formato antiguo: 'optionQuestions' en lugar de 'options'
        String json = "{" +
                "\"questionIndex\": 0," +
                "\"SURVEY_ID\": \"survey_001\"," +
                "\"questionText\": \"Pregunta de opción múltiple\"," +
                "\"typeQuestion\": \"MULTIPLE_CHOICE\"," +
                "\"required\": true," +
                "\"minSelections\": 1," +
                "\"maxSelections\": 1," +
                "\"optionQuestions\": [" +
                "  {\"questionIndex\": 0, \"optionText\": \"Opción 1\"}," +
                "  {\"questionIndex\": 1, \"optionText\": \"Opción 2\"}" +
                "]" +
                "}";

        Question question = gson.fromJson(json, Question.class);

        assertTrue(question instanceof MultipleChoiceQuestion);
        MultipleChoiceQuestion mcq = (MultipleChoiceQuestion) question;
        assertEquals(2, mcq.getOptionsSize());
    }

    @Test
    public void testDeserializeMultipleChoiceWithStringOptions() {
        // Formato donde las opciones son strings simples
        String json = "{" +
                "\"questionIndex\": 0," +
                "\"SURVEY_ID\": \"survey_001\"," +
                "\"questionText\": \"Pregunta simple\"," +
                "\"typeQuestion\": \"MULTIPLE_CHOICE\"," +
                "\"required\": true," +
                "\"minSelections\": 1," +
                "\"maxSelections\": 1," +
                "\"options\": [\"Sí\", \"No\", \"Tal vez\"]" +
                "}";

        Question question = gson.fromJson(json, Question.class);

        assertTrue(question instanceof MultipleChoiceQuestion);
        MultipleChoiceQuestion mcq = (MultipleChoiceQuestion) question;
        assertEquals(3, mcq.getOptionsSize());
    }

    // ───────────────────────────────────────────────
    // Tests de round-trip
    // ───────────────────────────────────────────────

    @Test
    public void testRoundTripTextualQuestion() {
        Question original = new Question(0, "survey_001");
        original.setQuestionText("¿Cuál es tu opinión?");
        original.setTypeQuestion(TypeQuestion.TEXTUAL);
        original.setRequired(true);

        String json = gson.toJson(original);
        Question deserialized = gson.fromJson(json, Question.class);

        assertEquals(original.getQuestionIndex(), deserialized.getQuestionIndex());
        assertEquals(original.getQuestionText(), deserialized.getQuestionText());
        assertEquals(original.getTypeQuestion(), deserialized.getTypeQuestion());
    }

    @Test
    public void testRoundTripMultipleChoiceQuestion() {
        MultipleChoiceQuestion original = new MultipleChoiceQuestion(0, "survey_001");
        original.setQuestionText("Elige una opción");
        original.addOption(new OptionQuestion(0, "Opción A"));
        original.addOption(new OptionQuestion(1, "Opción B"));
        original.setMinSelections(1);
        original.setMaxSelections(1);

        String json = gson.toJson(original);
        Question deserialized = gson.fromJson(json, Question.class);

        assertTrue(deserialized instanceof MultipleChoiceQuestion);
        MultipleChoiceQuestion mcqDeserialized = (MultipleChoiceQuestion) deserialized;
        assertEquals(2, mcqDeserialized.getOptionsSize());
    }

    // ───────────────────────────────────────────────
    // Tests de casos especiales
    // ───────────────────────────────────────────────

    @Test
    public void testDeserializeQuestionWithEmptyText() {
        Question original = new Question(0, "survey_001");
        original.setQuestionText("");
        original.setTypeQuestion(TypeQuestion.TEXTUAL);
        original.setRequired(false);

        String json = gson.toJson(original);
        Question question = gson.fromJson(json, Question.class);

        assertNotNull(question);
        assertEquals("", question.getQuestionText());
    }

    @Test
    public void testDeserializeQuestionWithSpecialCharacters() {
        Question original = new Question(0, "survey_001");
        original.setQuestionText("¿Cómo estás? ¡Bien! Ñoño @#$%");
        original.setTypeQuestion(TypeQuestion.TEXTUAL);
        original.setRequired(true);

        String json = gson.toJson(original);
        Question question = gson.fromJson(json, Question.class);

        assertNotNull(question);
        assertEquals("¿Cómo estás? ¡Bien! Ñoño @#$%", question.getQuestionText());
    }
}

