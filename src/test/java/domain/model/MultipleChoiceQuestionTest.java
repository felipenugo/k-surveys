package domain.model;

import domain.model.enums.TypeQuestion;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class MultipleChoiceQuestionTest {

    private MultipleChoiceQuestion mcQuestion;
    private static final int TEST_QUESTION_INDEX = 0;
    private static final String TEST_SURVEY_ID = "survey-123";

    @Before
    public void setUp() {
        mcQuestion = new MultipleChoiceQuestion(TEST_QUESTION_INDEX, TEST_SURVEY_ID);
    }

    @Test
    public void testMultipleChoiceQuestionCreation() {
        assertNotNull(mcQuestion);
        assertEquals(TEST_QUESTION_INDEX, mcQuestion.getQuestionIndex());
        assertEquals(TEST_SURVEY_ID, mcQuestion.getSURVEY_ID());
        assertEquals(TypeQuestion.MULTIPLE_CHOICE, mcQuestion.getTypeQuestion());
        assertEquals("", mcQuestion.getQuestionText());
        assertTrue(mcQuestion.isRequired());
        assertEquals(1, mcQuestion.getMinSelections()); // default value
        assertEquals(1, mcQuestion.getMaxSelections()); // default value
        assertEquals(0, mcQuestion.getOptionsSize()); // no options added yet
    }

    @Test
    public void testAddOption() {
        OptionQuestion option1 = new OptionQuestion(TEST_QUESTION_INDEX, TEST_SURVEY_ID);
        option1.setOptionText("Opción A");
        mcQuestion.addOption(option1);

        assertEquals(1, mcQuestion.getOptionsSize());
        assertEquals("Opción A", mcQuestion.getOption(0).getOptionText());
    }

    @Test
    public void testAddMultipleOptions() {
        OptionQuestion option1 = new OptionQuestion(TEST_QUESTION_INDEX, TEST_SURVEY_ID);
        option1.setOptionText("Opción A");
        OptionQuestion option2 = new OptionQuestion(TEST_QUESTION_INDEX, TEST_SURVEY_ID);
        option2.setOptionText("Opción B");
        OptionQuestion option3 = new OptionQuestion(TEST_QUESTION_INDEX, TEST_SURVEY_ID);
        option3.setOptionText("Opción C");

        mcQuestion.addOption(option1);
        mcQuestion.addOption(option2);
        mcQuestion.addOption(option3);

        assertEquals(3, mcQuestion.getOptionsSize());
        assertEquals("Opción A", mcQuestion.getOption(0).getOptionText());
        assertEquals("Opción B", mcQuestion.getOption(1).getOptionText());
        assertEquals("Opción C", mcQuestion.getOption(2).getOptionText());
    }

    @Test
    public void testRemoveOption() {
        OptionQuestion option1 = new OptionQuestion(TEST_QUESTION_INDEX, TEST_SURVEY_ID);
        option1.setOptionText("Opción A");
        OptionQuestion option2 = new OptionQuestion(TEST_QUESTION_INDEX, TEST_SURVEY_ID);
        option2.setOptionText("Opción B");

        mcQuestion.addOption(option1);
        mcQuestion.addOption(option2);
        assertEquals(2, mcQuestion.getOptionsSize());

        mcQuestion.removeOption(0);
        assertEquals(1, mcQuestion.getOptionsSize());
        assertEquals("Opción B", mcQuestion.getOption(0).getOptionText());
    }

    @Test
    public void testUpdateOption() {
        OptionQuestion option1 = new OptionQuestion(TEST_QUESTION_INDEX, TEST_SURVEY_ID);
        option1.setOptionText("Opción Original");
        mcQuestion.addOption(option1);

        OptionQuestion newOption = new OptionQuestion(TEST_QUESTION_INDEX, TEST_SURVEY_ID);
        newOption.setOptionText("Opción Actualizada");
        mcQuestion.updateOption(0, newOption);

        assertEquals("Opción Actualizada", mcQuestion.getOption(0).getOptionText());
    }

    @Test
    public void testReorderOption() {
        OptionQuestion option1 = new OptionQuestion(TEST_QUESTION_INDEX, TEST_SURVEY_ID);
        option1.setOptionText("Primera");
        OptionQuestion option2 = new OptionQuestion(TEST_QUESTION_INDEX, TEST_SURVEY_ID);
        option2.setOptionText("Segunda");
        OptionQuestion option3 = new OptionQuestion(TEST_QUESTION_INDEX, TEST_SURVEY_ID);
        option3.setOptionText("Tercera");

        mcQuestion.addOption(option1);
        mcQuestion.addOption(option2);
        mcQuestion.addOption(option3);

        // Mover la primera opción a la tercera posición
        mcQuestion.reorderOption(0, 2);

        assertEquals("Segunda", mcQuestion.getOption(0).getOptionText());
        assertEquals("Tercera", mcQuestion.getOption(1).getOptionText());
        assertEquals("Primera", mcQuestion.getOption(2).getOptionText());
    }

    @Test
    public void testClearOptions() {
        OptionQuestion option1 = new OptionQuestion(TEST_QUESTION_INDEX, TEST_SURVEY_ID);
        option1.setOptionText("Opción A");
        OptionQuestion option2 = new OptionQuestion(TEST_QUESTION_INDEX, TEST_SURVEY_ID);
        option2.setOptionText("Opción B");

        mcQuestion.addOption(option1);
        mcQuestion.addOption(option2);
        assertEquals(2, mcQuestion.getOptionsSize());

        mcQuestion.clearOptions();
        assertEquals(0, mcQuestion.getOptionsSize());
    }

    @Test
    public void testSetMinSelections() {
        // Añadir opciones primero
        addThreeOptions();

        // Primero establecer maxSelections para permitir el cambio de minSelections
        mcQuestion.setMaxSelections(3);
        mcQuestion.setMinSelections(2);
        assertEquals(2, mcQuestion.getMinSelections());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetMinSelectionsLessThanOne() {
        mcQuestion.setMinSelections(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetMinSelectionsGreaterThanMax() {
        addThreeOptions();
        mcQuestion.setMaxSelections(2);
        mcQuestion.setMinSelections(3); // Should throw exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetMinSelectionsGreaterThanOptionsSize() {
        addThreeOptions();
        mcQuestion.setMaxSelections(3); // Establecer max primero
        mcQuestion.setMinSelections(5); // Should throw exception (only 3 options)
    }

    @Test
    public void testSetMaxSelections() {
        addThreeOptions();
        mcQuestion.setMaxSelections(3);
        assertEquals(3, mcQuestion.getMaxSelections());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetMaxSelectionsLessThanMin() {
        addThreeOptions();
        mcQuestion.setMaxSelections(3); // Establecer max primero para poder cambiar min
        mcQuestion.setMinSelections(2);
        mcQuestion.setMaxSelections(1); // Should throw exception
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetMaxSelectionsGreaterThanOptionsSize() {
        addThreeOptions();
        mcQuestion.setMaxSelections(5); // Should throw exception (only 3 options)
    }

    @Test
    public void testInRange() {
        addThreeOptions();

        assertTrue(mcQuestion.inRange(0));
        assertTrue(mcQuestion.inRange(1));
        assertTrue(mcQuestion.inRange(2));
        assertFalse(mcQuestion.inRange(3));
        assertFalse(mcQuestion.inRange(-1));
    }

    @Test
    public void testGetOptions() {
        addThreeOptions();

        assertEquals(3, mcQuestion.getOptions().size());
        assertEquals("Opción 1", mcQuestion.getOptions().get(0).getOptionText());
        assertEquals("Opción 2", mcQuestion.getOptions().get(1).getOptionText());
        assertEquals("Opción 3", mcQuestion.getOptions().get(2).getOptionText());
    }

    @Test
    public void testCopy() {
        // Configurar la pregunta original
        mcQuestion.setQuestionText("¿Cuál es tu color favorito?");
        mcQuestion.setRequired(false);

        addThreeOptions();
        mcQuestion.setMaxSelections(2); // Establecer max primero
        mcQuestion.setMinSelections(1);

        // Crear una copia
        MultipleChoiceQuestion copy = mcQuestion.copy();

        // Verificar que la copia tiene los mismos valores
        assertNotNull(copy);
        assertEquals(mcQuestion.getQuestionIndex(), copy.getQuestionIndex());
        assertEquals(mcQuestion.getSURVEY_ID(), copy.getSURVEY_ID());
        assertEquals(mcQuestion.getQuestionText(), copy.getQuestionText());
        assertEquals(mcQuestion.getTypeQuestion(), copy.getTypeQuestion());
        assertEquals(mcQuestion.isRequired(), copy.isRequired());
        assertEquals(mcQuestion.getMinSelections(), copy.getMinSelections());
        assertEquals(mcQuestion.getMaxSelections(), copy.getMaxSelections());
        assertEquals(mcQuestion.getOptionsSize(), copy.getOptionsSize());

        // Verificar que las opciones también fueron copiadas
        for (int i = 0; i < mcQuestion.getOptionsSize(); i++) {
            assertEquals(mcQuestion.getOption(i).getOptionText(),
                    copy.getOption(i).getOptionText());
        }

        // Verificar que es una copia independiente (deep copy)
        copy.setQuestionText("Pregunta modificada");
        assertNotEquals(mcQuestion.getQuestionText(), copy.getQuestionText());

        // Modificar una opción de la copia
        copy.getOption(0).setOptionText("Opción modificada");
        assertNotEquals(mcQuestion.getOption(0).getOptionText(),
                copy.getOption(0).getOptionText());
    }

    @Test
    public void testCompleteMultipleChoiceQuestionConfiguration() {
        // Configurar una pregunta de opción múltiple completa
        mcQuestion.setQuestionText("¿Qué lenguajes de programación conoces?");
        mcQuestion.setRequired(true);

        OptionQuestion option1 = new OptionQuestion(TEST_QUESTION_INDEX, TEST_SURVEY_ID);
        option1.setOptionText("Java");
        OptionQuestion option2 = new OptionQuestion(TEST_QUESTION_INDEX, TEST_SURVEY_ID);
        option2.setOptionText("Python");
        OptionQuestion option3 = new OptionQuestion(TEST_QUESTION_INDEX, TEST_SURVEY_ID);
        option3.setOptionText("C++");
        OptionQuestion option4 = new OptionQuestion(TEST_QUESTION_INDEX, TEST_SURVEY_ID);
        option4.setOptionText("JavaScript");

        mcQuestion.addOption(option1);
        mcQuestion.addOption(option2);
        mcQuestion.addOption(option3);
        mcQuestion.addOption(option4);

        mcQuestion.setMaxSelections(3); // Establecer max primero
        mcQuestion.setMinSelections(1);

        // Verificar la configuración
        assertEquals("¿Qué lenguajes de programación conoces?", mcQuestion.getQuestionText());
        assertTrue(mcQuestion.isRequired());
        assertEquals(TypeQuestion.MULTIPLE_CHOICE, mcQuestion.getTypeQuestion());
        assertEquals(4, mcQuestion.getOptionsSize());
        assertEquals(1, mcQuestion.getMinSelections());
        assertEquals(3, mcQuestion.getMaxSelections());
    }

    @Test
    public void testSingleSelectionQuestion() {
        // Test para pregunta de selección única (radio buttons)
        mcQuestion.setQuestionText("¿Cuál es tu género?");

        OptionQuestion option1 = new OptionQuestion(TEST_QUESTION_INDEX, TEST_SURVEY_ID);
        option1.setOptionText("Masculino");
        OptionQuestion option2 = new OptionQuestion(TEST_QUESTION_INDEX, TEST_SURVEY_ID);
        option2.setOptionText("Femenino");
        OptionQuestion option3 = new OptionQuestion(TEST_QUESTION_INDEX, TEST_SURVEY_ID);
        option3.setOptionText("Otro");

        mcQuestion.addOption(option1);
        mcQuestion.addOption(option2);
        mcQuestion.addOption(option3);

        // Valores por defecto ya son 1-1
        assertEquals(1, mcQuestion.getMinSelections());
        assertEquals(1, mcQuestion.getMaxSelections());
    }

    @Test
    public void testMultipleSelectionsQuestion() {
        // Test para pregunta de selección múltiple (checkboxes)
        mcQuestion.setQuestionText("¿Qué hobbies tienes?");

        for (int i = 1; i <= 5; i++) {
            OptionQuestion option = new OptionQuestion(TEST_QUESTION_INDEX, TEST_SURVEY_ID);
            option.setOptionText("Hobby " + i);
            mcQuestion.addOption(option);
        }

        mcQuestion.setMaxSelections(5); // Establecer max primero
        mcQuestion.setMinSelections(1);

        assertEquals(5, mcQuestion.getOptionsSize());
        assertEquals(1, mcQuestion.getMinSelections());
        assertEquals(5, mcQuestion.getMaxSelections());
    }

    @Test
    public void testInheritedMethodsFromQuestion() {
        // Verificar que los métodos heredados de Question funcionan correctamente
        mcQuestion.setQuestionText("Pregunta de prueba");
        assertEquals("Pregunta de prueba", mcQuestion.getQuestionText());

        mcQuestion.setRequired(false);
        assertFalse(mcQuestion.isRequired());

        mcQuestion.setQuestionIndex(5);
        assertEquals(5, mcQuestion.getQuestionIndex());

        assertEquals(TEST_SURVEY_ID, mcQuestion.getSURVEY_ID());
        assertEquals(TypeQuestion.MULTIPLE_CHOICE, mcQuestion.getTypeQuestion());
    }

    // Helper method para añadir tres opciones básicas
    private void addThreeOptions() {
        OptionQuestion option1 = new OptionQuestion(TEST_QUESTION_INDEX, TEST_SURVEY_ID);
        option1.setOptionText("Opción 1");
        OptionQuestion option2 = new OptionQuestion(TEST_QUESTION_INDEX, TEST_SURVEY_ID);
        option2.setOptionText("Opción 2");
        OptionQuestion option3 = new OptionQuestion(TEST_QUESTION_INDEX, TEST_SURVEY_ID);
        option3.setOptionText("Opción 3");

        mcQuestion.addOption(option1);
        mcQuestion.addOption(option2);
        mcQuestion.addOption(option3);
    }
}
