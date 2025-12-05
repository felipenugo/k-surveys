package domain.controller;

import data.AnswerRepository;
import data.QuestionRepository;
import data.ResponseRepository;
import data.SurveyRepository;
import data.UserRepository;
import domain.model.*;
import domain.model.enums.TypeQuestion;
import domain.service.AnswerService;
import domain.service.ResponseService;
import domain.service.SurveyService;
import domain.service.UserService;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests para AnswerController.
 * Enfocado en la integración con ResponseController y actualización de respuestas.
 */
public class AnswerControllerTest {

    private UserController userController;
    private SurveyController surveyController;
    private ResponseController responseController;
    private AnswerController answerController;
    private ResponseRepository responseRepository;

    private Survey survey;
    private List<Question> questions;
    private Response response;

    @Before
    public void setUp() {
        // Initialize repositories
        UserRepository userRepository = new UserRepository();
        SurveyRepository surveyRepository = new SurveyRepository();
        QuestionRepository questionRepository = new QuestionRepository();
        responseRepository = new ResponseRepository();
        AnswerRepository answerRepository = new AnswerRepository();

        // Initialize services and controllers
        UserService userService = new UserService(userRepository);
        userController = new UserController(userService);

        SurveyService surveyService = new SurveyService(surveyRepository, userController, userService);
        surveyController = new SurveyController(surveyService);

        ResponseService responseService = new ResponseService(responseRepository, userController, surveyService);
        responseController = new ResponseController(responseService);

        AnswerService answerService = new AnswerService(answerRepository, userController);
        answerController = new AnswerController(answerService);
    }

    private void createTestSurvey() {
        survey = surveyController.initializeNewSurvey("Test Survey", "Survey for answers", userController.getUsernameLoggedIn());
        questions = new ArrayList<>();

        // Question 1: Textual
        Question q1 = new Question(0, survey.getSURVEY_ID());
        q1.setTypeQuestion(TypeQuestion.TEXTUAL);
        q1.setQuestionText("What is your name?");
        questions.add(q1);

        // Question 2: Numerical
        Question q2 = new Question(1, survey.getSURVEY_ID());
        q2.setTypeQuestion(TypeQuestion.NUMERICAL);
        q2.setQuestionText("What is your age?");
        questions.add(q2);

        // Question 3: Multiple Choice
        MultipleChoiceQuestion q3 = new MultipleChoiceQuestion(2, survey.getSURVEY_ID());
        q3.setQuestionText("What is your favorite color?");
        q3.addOption(new OptionQuestion(0, "Red"));
        q3.addOption(new OptionQuestion(1, "Blue"));
        q3.addOption(new OptionQuestion(2, "Green"));
        questions.add(q3);

        survey.getQuestions().addAll(questions);
        survey = surveyController.createSurvey(survey);
    }

    private void createTestResponse() {
        String mainUsername = userController.getUsernameLoggedIn();
        userController.logoutUser();

        String responderUsername = "testResponder";
        userController.registerUser(responderUsername, "responder@gmail.com", "password", "What is your pet's name?", "Fluffy");
        userController.loginUser(responderUsername, "password");

        String responseId = responseController.startResponse(survey.getSURVEY_ID());
        response = responseRepository.getResponse(survey.getSURVEY_ID(), responseId);

        userController.logoutUser();
        userController.loginUser(mainUsername, "password");
    }

    @Test
    public void testAnswerControllerCreation() {
        assertNotNull(answerController);
    }

    @Test
    public void testUpdateTextualAnswer() {
        String username = "testUser1";
        userController.registerUser(username, "test1@gmail.com", "password", "What is your pet's name?", "Fluffy");
        userController.loginUser(username, "password");

        createTestSurvey();
        createTestResponse();

        // Update textual answer using ResponseController
        responseController.updateAnswer(survey.getSURVEY_ID(), response.getRESPONSE_ID(), 0, "John Doe", TypeQuestion.TEXTUAL);

        // Verify answer was updated
        Answer answer = response.getAnswer(0);
        assertNotNull(answer);
        assertEquals("John Doe", ((TextualAnswer) answer).getAnswerText());
    }

    @Test
    public void testUpdateNumericalAnswer() {
        String username = "testUser2";
        userController.registerUser(username, "test2@gmail.com", "password", "What is your pet's name?", "Fluffy");
        userController.loginUser(username, "password");

        createTestSurvey();
        createTestResponse();

        // Update numerical answer - Usa Double
        responseController.updateAnswer(survey.getSURVEY_ID(), response.getRESPONSE_ID(), 1, 30.0);

        Answer answer = response.getAnswer(1);
        assertNotNull(answer);
        assertEquals(30.0, (Double) ((NumericalAnswer) answer).getAnswerNum(), 0.01);
    }

    @Test
    public void testUpdateMultipleChoiceAnswer() {
        String username = "testUser3";
        userController.registerUser(username, "test3@gmail.com", "password", "What is your pet's name?", "Fluffy");
        userController.loginUser(username, "password");

        createTestSurvey();
        createTestResponse();

        // Update multiple choice answer
        responseController.updateAnswer(survey.getSURVEY_ID(), response.getRESPONSE_ID(), 2, "0", TypeQuestion.MULTIPLE_CHOICE);

        Answer answer = response.getAnswer(2);
        assertNotNull(answer);
        assertTrue(answer instanceof MultipleChoiceAnswer);
    }

    @Test
    public void testUpdateMultipleAnswersPerResponse() {
        String username = "testUser4";
        userController.registerUser(username, "test4@gmail.com", "password", "What is your pet's name?", "Fluffy");
        userController.loginUser(username, "password");

        createTestSurvey();
        createTestResponse();

        // Update all answers
        responseController.updateAnswer(survey.getSURVEY_ID(), response.getRESPONSE_ID(), 0, "Jane Doe", TypeQuestion.TEXTUAL);
        responseController.updateAnswer(survey.getSURVEY_ID(), response.getRESPONSE_ID(), 1, 28.0);
        responseController.updateAnswer(survey.getSURVEY_ID(), response.getRESPONSE_ID(), 2, "1", TypeQuestion.MULTIPLE_CHOICE);

        Answer answer1 = response.getAnswer(0);
        Answer answer2 = response.getAnswer(1);
        Answer answer3 = response.getAnswer(2);

        assertNotNull(answer1);
        assertNotNull(answer2);
        assertNotNull(answer3);
        assertEquals("Jane Doe", ((TextualAnswer) answer1).getAnswerText());
        assertEquals(28.0, (Double) ((NumericalAnswer) answer2).getAnswerNum(), 0.01);
        assertTrue(answer3 instanceof MultipleChoiceAnswer);
    }

    @Test
    public void testAnswerUpdate() {
        String username = "testUser5";
        userController.registerUser(username, "test5@gmail.com", "password", "What is your pet's name?", "Fluffy");
        userController.loginUser(username, "password");

        createTestSurvey();
        createTestResponse();

        // Initial answer
        responseController.updateAnswer(survey.getSURVEY_ID(), response.getRESPONSE_ID(), 0, "Initial", TypeQuestion.TEXTUAL);
        Answer answer1 = response.getAnswer(0);
        assertEquals("Initial", ((TextualAnswer) answer1).getAnswerText());

        // Update answer
        responseController.updateAnswer(survey.getSURVEY_ID(), response.getRESPONSE_ID(), 0, "Updated", TypeQuestion.TEXTUAL);
        Answer answer2 = response.getAnswer(0);
        assertEquals("Updated", ((TextualAnswer) answer2).getAnswerText());
    }

    @Test
    public void testTextualAnswerPersistence() {
        String username = "testUser6";
        userController.registerUser(username, "test6@gmail.com", "password", "What is your pet's name?", "Fluffy");
        userController.loginUser(username, "password");

        createTestSurvey();
        createTestResponse();

        // Add textual answer
        responseController.updateAnswer(survey.getSURVEY_ID(), response.getRESPONSE_ID(), 0, "Persistent", TypeQuestion.TEXTUAL);

        // Retrieve from response
        Answer retrieved = response.getAnswer(0);
        assertNotNull(retrieved);
        assertTrue(retrieved instanceof TextualAnswer);
        assertEquals("Persistent", ((TextualAnswer) retrieved).getAnswerText());
    }

    @Test
    public void testNumericalAnswerPersistence() {
        String username = "testUser7";
        userController.registerUser(username, "test7@gmail.com", "password", "What is your pet's name?", "Fluffy");
        userController.loginUser(username, "password");

        createTestSurvey();
        createTestResponse();

        // Add numerical answer
        responseController.updateAnswer(survey.getSURVEY_ID(), response.getRESPONSE_ID(), 1, 42.5);

        // Retrieve from response
        Answer retrieved = response.getAnswer(1);
        assertNotNull(retrieved);
        assertTrue(retrieved instanceof NumericalAnswer);
        assertEquals(42.5, (Double) ((NumericalAnswer) retrieved).getAnswerNum(), 0.01);
    }

    @Test
    public void testMultipleResponsesWithAnswers() {
        String username = "testUser8";
        userController.registerUser(username, "test8@gmail.com", "password", "What is your pet's name?", "Fluffy");
        userController.loginUser(username, "password");

        createTestSurvey();

        // Create first response
        createTestResponse();
        responseController.updateAnswer(survey.getSURVEY_ID(), response.getRESPONSE_ID(), 0, "Response 1", TypeQuestion.TEXTUAL);
        responseController.updateAnswer(survey.getSURVEY_ID(), response.getRESPONSE_ID(), 1, 20.0);

        // Verify answer was saved
        List<Response> allResponses = responseRepository.getResponsesBySurveyId(survey.getSURVEY_ID());
        assertTrue(allResponses.size() >= 1);
    }


    @Test
    public void testAnswerTypes() {
        String username = "testUser9";
        userController.registerUser(username, "test9@gmail.com", "password", "What is your pet's name?", "Fluffy");
        userController.loginUser(username, "password");

        createTestSurvey();
        createTestResponse();

        // Update all answer types
        responseController.updateAnswer(survey.getSURVEY_ID(), response.getRESPONSE_ID(), 0, "Text", TypeQuestion.TEXTUAL);
        responseController.updateAnswer(survey.getSURVEY_ID(), response.getRESPONSE_ID(), 1, 50.0);
        responseController.updateAnswer(survey.getSURVEY_ID(), response.getRESPONSE_ID(), 2, "2", TypeQuestion.MULTIPLE_CHOICE);

        Answer textAnswer = response.getAnswer(0);
        Answer numAnswer = response.getAnswer(1);
        Answer mcAnswer = response.getAnswer(2);

        assertTrue(textAnswer instanceof TextualAnswer);
        assertTrue(numAnswer instanceof NumericalAnswer);
        assertTrue(mcAnswer instanceof MultipleChoiceAnswer);
    }

    @Test
    public void testAnswerRetrievalAfterUpdate() {
        String username = "testUser10";
        userController.registerUser(username, "test10@gmail.com", "password", "What is your pet's name?", "Fluffy");
        userController.loginUser(username, "password");

        createTestSurvey();
        createTestResponse();

        responseController.updateAnswer(survey.getSURVEY_ID(), response.getRESPONSE_ID(), 0, "Test", TypeQuestion.TEXTUAL);
        responseController.updateAnswer(survey.getSURVEY_ID(), response.getRESPONSE_ID(), 1, 33.0);

        Answer a1 = response.getAnswer(0);
        Answer a2 = response.getAnswer(1);

        assertNotNull(a1);
        assertNotNull(a2);
    }
}
