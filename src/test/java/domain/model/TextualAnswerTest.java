package domain.model;

import domain.model.enums.TypeQuestion;

import org.junit.Before;
import org .junit.Test;
import static org.junit.Assert.*;

public class TextualAnswerTest {
    private TextualAnswer textualAnswer;
    private final int QUESTION_INDEX = 0;
    private final String RESPONSE_ID = "1";

    @Before
    public void setUp(){
        textualAnswer = new TextualAnswer(QUESTION_INDEX, RESPONSE_ID);
    }

    @Test
    public void testTextualAnswerCreation(){
        assertNotNull(textualAnswer);
        assertEquals(QUESTION_INDEX, textualAnswer.getQUESTION_INDEX());
        assertEquals(RESPONSE_ID, textualAnswer.getResponseId());
        assertEquals(TypeQuestion.TEXTUAL, textualAnswer.getTypeAnswer());
        assertFalse(textualAnswer.getIsAnswered());
        assertEquals("", textualAnswer.getAnswerText());
    }

    @Test
    public void testSetTextualAnswer()
    {
        textualAnswer.setAnswerText("answer");
        assertEquals("answer", textualAnswer.getAnswerText());
        assertTrue( textualAnswer.getIsAnswered());
    }

    @Test
    public void testClearAnswer()
    {
        textualAnswer.setAnswerText("answer");
        textualAnswer.clearAnswer();
        assertNotEquals("answer", textualAnswer.getAnswerText());
        assertFalse(textualAnswer.getIsAnswered());
    }
}
