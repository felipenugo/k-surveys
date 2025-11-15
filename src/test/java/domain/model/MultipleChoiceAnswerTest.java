package domain.model;

import domain.model.enums.TypeQuestion;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class MultipleChoiceAnswerTest {
    private MultipleChoiceAnswer mcAnswer;
    private final int QUESTION_INDEX = 0;
    private final String RESPONSE_ID = "1";
    private final int MAX_OPTIONS = 3;

    @Before
    public void setUp() {
        mcAnswer = new MultipleChoiceAnswer(QUESTION_INDEX, RESPONSE_ID, MAX_OPTIONS);
    }

    @Test
    public void testMultipleChoiceAnswerCreation() {
        // Verificamos los atributos heredados
        assertNotNull(mcAnswer);
        assertEquals(QUESTION_INDEX, mcAnswer.getQUESTION_INDEX());
        assertEquals(RESPONSE_ID, mcAnswer.getResponseId());
        assertEquals(TypeQuestion.MULTIPLE_CHOICE, mcAnswer.getTypeAnswer());
        assertFalse(mcAnswer.getIsAnswered());

        // Verificamos los atributos propios de la subclase
        assertEquals(MAX_OPTIONS, mcAnswer.getMaxOptions());
        assertNotNull(mcAnswer.getSelectedOptions());
        assertFalse(mcAnswer.inRange(-1));
        assertFalse(mcAnswer.inRange(MAX_OPTIONS));
        assertFalse(mcAnswer.getIsAnswered());
        for (int i = 0; i < MAX_OPTIONS; i++)
            assertFalse(mcAnswer.isOptionSelected(i));
    }

    @Test
    public void testSetOption() {
        mcAnswer.setOption(0, true);
        assertTrue(mcAnswer.isOptionSelected(0));
        mcAnswer.setOption(MAX_OPTIONS - 1, false);
        assertFalse(mcAnswer.isOptionSelected(MAX_OPTIONS - 1));
        assertTrue(mcAnswer.getIsAnswered());
    }

    @Test
    public void testSetOptions() {
        boolean[] sampleOptions = new boolean[MAX_OPTIONS];
        for (int i = 0; i < MAX_OPTIONS; i++)
            sampleOptions[i] = (i % 2 == 0);
        mcAnswer.setOptions(sampleOptions);
        for (int i = 0; i < MAX_OPTIONS; i++)
            assertEquals(mcAnswer.isOptionSelected(i), sampleOptions[i]);
        assertTrue(mcAnswer.getIsAnswered());
    }

    @Test
    public void testClearMultipleChoiceAnswer() {
        boolean[] sampleOptions = new boolean[MAX_OPTIONS];
        Arrays.fill(sampleOptions, true);
        mcAnswer.setOptions(sampleOptions);
        mcAnswer.clearAnswer();
        for (int i = 0; i < MAX_OPTIONS; i++)
            assertFalse(mcAnswer.isOptionSelected(i));
        assertFalse(mcAnswer.getIsAnswered());
    }
}
