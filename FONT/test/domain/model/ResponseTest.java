package domain.model;

import domain.model.enums.ResponseStatus;
import domain.model.enums.TypeQuestion;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ResponseTest {
    private Response response;
    private final String RESPONSE_ID = "1";
    private final String SURVEY_ID = "1";
    private final String RESPONDER_USERNAME = "testUser";
    private  final List<Question> QUESTIONS = new ArrayList<>();


    @Before
    public void setUp() {
        QUESTIONS.add(new Question(0, SURVEY_ID));
        QUESTIONS.get(0).setTypeQuestion(TypeQuestion.TEXTUAL);
        QUESTIONS.add(new Question(1, SURVEY_ID));
        QUESTIONS.get(1).setTypeQuestion(TypeQuestion.NUMERICAL);
        QUESTIONS.add(new MultipleChoiceQuestion(2, SURVEY_ID));
        response = new Response(RESPONSE_ID, SURVEY_ID, RESPONDER_USERNAME, QUESTIONS);
    }

    @Test
    public void testResponseCreation()
    {
        assertNotNull(response);
        assertEquals(RESPONSE_ID, response.getRESPONSE_ID());
        assertEquals(SURVEY_ID, response.getSurveyId());
        assertEquals(RESPONDER_USERNAME, response.getResponderUsername());
        assertEquals(ResponseStatus.DRAFT, response.getResponseStatus());
        assertNull(response.getSUBMITTED_AT());
        assertNotNull(response.getANSWERS());
        assertFalse(response.inRange(-1));
        assertFalse(response.inRange(QUESTIONS.size()));
    }

    @Test
    public void testSetResponseStatus()
    {
        response.setResponseStatus(ResponseStatus.SUBMITTED);
        assertEquals(ResponseStatus.SUBMITTED, response.getResponseStatus());
    }

    @Test
    public void testSetSubmittedAt()
    {
        LocalDateTime date = LocalDateTime.of(2025, 11, 17, 12, 59);
        response.setSUBMITTED_AT(date);
        assertEquals(date, response.getSUBMITTED_AT());
    }

    @Test
    public void testUpdateAnswer()
    {
        TextualAnswer textualAnswer = new TextualAnswer(0, RESPONSE_ID);
        textualAnswer.setAnswerText("textual answer");
        response.updateAnswer(0, textualAnswer);
        assertEquals(textualAnswer, response.getAnswer(0));
        assertTrue(response.getAnswer(0).getIsAnswered());

        NumericalAnswer numericalAnswer = new NumericalAnswer(0, RESPONSE_ID);
        numericalAnswer.setAnswerNum(2.5);
        response.updateAnswer(0, numericalAnswer);
        assertEquals(numericalAnswer, response.getAnswer(0));
        assertTrue(response.getAnswer(0).getIsAnswered());

        MultipleChoiceAnswer mcAnswer = new MultipleChoiceAnswer(0, RESPONSE_ID, 3);
        mcAnswer.setOptions(new boolean[]{true, true, false});
        response.updateAnswer(0, mcAnswer);
        assertEquals(mcAnswer, response.getAnswer(0));
        assertTrue(response.getAnswer(0).getIsAnswered());
    }

    @Test
    public void testClearAnswer()
    {
        TextualAnswer textualAnswer = new TextualAnswer(0, RESPONSE_ID);
        textualAnswer.setAnswerText("textual answer");
        response.updateAnswer(0, textualAnswer);

        NumericalAnswer numericalAnswer = new NumericalAnswer(1, RESPONSE_ID);
        numericalAnswer.setAnswerNum(2.5);
        response.updateAnswer(1, numericalAnswer);

        MultipleChoiceAnswer mcAnswer = new MultipleChoiceAnswer(2, RESPONSE_ID, 3);
        mcAnswer.setOptions(new boolean[]{true, true, false});
        response.updateAnswer(2, mcAnswer);

        response.clearAnswer(0);
        assertFalse(response.getAnswer(0).getIsAnswered());

        response.clearAnswer(1);
        assertFalse(response.getAnswer(1).getIsAnswered());

        response.clearAnswer(2);
        assertFalse(response.getAnswer(2).getIsAnswered());
    }
}
