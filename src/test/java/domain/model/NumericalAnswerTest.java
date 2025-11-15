package domain.model;

import domain.model.enums.TypeQuestion;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class NumericalAnswerTest {
    private NumericalAnswer numericalAnswer;
    private final int QUESTION_INDEX = 0;
    private final String RESPONSE_ID = "1";

    @Before
    public void setUp() {
        numericalAnswer = new NumericalAnswer(QUESTION_INDEX, RESPONSE_ID);
    }

    @Test
    public void testNumericalAnswerCreation() {
        assertNotNull(numericalAnswer);
        assertEquals(QUESTION_INDEX, numericalAnswer.getQUESTION_INDEX());
        assertEquals(RESPONSE_ID, numericalAnswer.getResponseId());
        assertEquals(TypeQuestion.NUMERICAL, numericalAnswer.getTypeAnswer());
        assertFalse(numericalAnswer.getIsAnswered());
        assertNull(numericalAnswer.getAnswerNum());
    }

    @Test
    public void testSetNumericalAnswer()
    {
        numericalAnswer.setAnswerNum(2.5);
        assertEquals((Double)2.5, numericalAnswer.getAnswerNum());
        assertTrue(numericalAnswer.getIsAnswered());
    }

    @Test
    public void testClearNumericalAnswer()
    {
        numericalAnswer.setAnswerNum(2.5);
        numericalAnswer.clearAnswer();
        assertNotEquals((Double)2.5, numericalAnswer.getAnswerNum());
        assertNull(numericalAnswer.getAnswerNum());
        assertFalse(numericalAnswer.getIsAnswered());
    }
}
