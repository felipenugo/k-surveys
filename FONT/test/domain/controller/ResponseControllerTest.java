package domain.controller;

import data.ResponseRepository;
import data.SurveyRepository;
import data.UserRepository;
import domain.model.*;
import domain.model.enums.TypeQuestion;
import domain.service.ResponseService;
import domain.service.SurveyService;
import domain.service.UserService;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests completos para ResponseController.
 * Cubre creación, actualización y gestión de respuestas a encuestas.
 */
public class ResponseControllerTest {

    private UserController userController;
    private SurveyController surveyController;
    private ResponseController responseController;
    private ResponseRepository responseRepository;
    private SurveyRepository surveyRepository;

    private Survey survey;
    private List<Question> questions;

    @Before
    public void setUp() {
        UserRepository userRepository = new UserRepository();
        this.surveyRepository = new SurveyRepository();
        this.responseRepository = new ResponseRepository();
        userRepository.clear();
        this.surveyRepository.clear();
        this.responseRepository.clear();
        UserService userService = new UserService(userRepository, this.surveyRepository, this.responseRepository);

        // Initialize services and controllers
        userController = new UserController(userService);

        SurveyService surveyService = new SurveyService(surveyRepository, userController, userService);
        surveyController = new SurveyController(surveyService);

        ResponseService responseService = new ResponseService(this.responseRepository, userController, surveyService);
        responseController = new ResponseController(responseService);

    }

    private void createTestSurvey() {
        survey = surveyController.initializeNewSurvey("Test Survey", "Survey for responses", userController.getUsernameLoggedIn());
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

    @Test
    public void testResponseControllerCreation() {
        assertNotNull(responseController);
    }

    @Test
    public void testStartResponse() {
        String username = "testUser1";
        userController.registerUser(username, "test1@gmail.com", "password" , "What is your pet's name?", "Fluffy");
        userController.loginUser(username, "password");

        createTestSurvey();

        String mainUsername = userController.getUsernameLoggedIn();
        userController.logoutUser();

        String responderUsername = "responder1";
        userController.registerUser(responderUsername, "responder1@gmail.com", "password" , "What is your pet's name?", "Fluffy");
        userController.loginUser(responderUsername, "password");

        String responseId = responseController.startResponse(survey.getSURVEY_ID());

        assertNotNull(responseId);
        assertFalse(responseId.isEmpty());

        userController.logoutUser();
        userController.loginUser(mainUsername, "password");
    }

    @Test
    public void testUpdateAnswerTextual() {
        String username = "testUser2";
        userController.registerUser(username, "test2@gmail.com", "password" , "What is your pet's name?", "Fluffy");
        userController.loginUser(username, "password");

        createTestSurvey();

        String mainUsername = userController.getUsernameLoggedIn();
        userController.logoutUser();

        String responderUsername = "responder2";
        userController.registerUser(responderUsername, "responder2@gmail.com", "password" , "What is your pet's name?", "Fluffy");
        userController.loginUser(responderUsername, "password");

        String responseId = responseController.startResponse(survey.getSURVEY_ID());
        responseController.updateAnswer(survey.getSURVEY_ID(), responseId, 0, "John Doe", TypeQuestion.TEXTUAL);

        Response response = responseRepository.getResponse(survey.getSURVEY_ID(), responseId);
        Answer answer = response.getAnswer(0);

        assertNotNull(answer);
        assertEquals("John Doe", ((TextualAnswer) answer).getAnswerText());

        userController.logoutUser();
        userController.loginUser(mainUsername, "password");
    }

    @Test
    public void testUpdateAnswerNumerical() {
        String username = "testUser3";
        userController.registerUser(username, "test3@gmail.com", "password" , "What is your pet's name?", "Fluffy");
        userController.loginUser(username, "password");

        createTestSurvey();

        String mainUsername = userController.getUsernameLoggedIn();
        userController.logoutUser();

        String responderUsername = "responder3";
        userController.registerUser(responderUsername, "responder3@gmail.com", "password" , "What is your pet's name?", "Fluffy");
        userController.loginUser(responderUsername, "password");

        String responseId = responseController.startResponse(survey.getSURVEY_ID());
        responseController.updateAnswer(survey.getSURVEY_ID(), responseId, 1, 25.5);

        Response response = responseRepository.getResponse(survey.getSURVEY_ID(), responseId);
        Answer answer = response.getAnswer(1);

        assertNotNull(answer);
        assertEquals(25.5, (Double) ((NumericalAnswer) answer).getAnswerNum(), 0.01);

        userController.logoutUser();
        userController.loginUser(mainUsername, "password");
    }

    @Test
    public void testUpdateAnswerMultipleChoice() {
        String username = "testUser4";
        userController.registerUser(username, "test4@gmail.com", "password" , "What is your pet's name?", "Fluffy");
        userController.loginUser(username, "password");

        createTestSurvey();

        String mainUsername = userController.getUsernameLoggedIn();
        userController.logoutUser();

        String responderUsername = "responder4";
        userController.registerUser(responderUsername, "responder4@gmail.com", "password" , "What is your pet's name?", "Fluffy");
        userController.loginUser(responderUsername, "password");

        String responseId = responseController.startResponse(survey.getSURVEY_ID());
        responseController.updateAnswer(survey.getSURVEY_ID(), responseId, 2, "1", TypeQuestion.MULTIPLE_CHOICE);

        Response response = responseRepository.getResponse(survey.getSURVEY_ID(), responseId);
        Answer answer = response.getAnswer(2);

        assertNotNull(answer);
        assertTrue(answer instanceof MultipleChoiceAnswer);

        userController.logoutUser();
        userController.loginUser(mainUsername, "password");
    }

    @Test
    public void testUpdateMultipleAnswers() {
        String username = "testUser5";
        userController.registerUser(username, "test5@gmail.com", "password" , "What is your pet's name?", "Fluffy");
        userController.loginUser(username, "password");

        createTestSurvey();

        String mainUsername = userController.getUsernameLoggedIn();
        userController.logoutUser();

        String responderUsername = "responder5";
        userController.registerUser(responderUsername, "responder5@gmail.com", "password" , "What is your pet's name?", "Fluffy");
        userController.loginUser(responderUsername, "password");

        String responseId = responseController.startResponse(survey.getSURVEY_ID());

        // Update all answers
        responseController.updateAnswer(survey.getSURVEY_ID(), responseId, 0, "Alice", TypeQuestion.TEXTUAL);
        responseController.updateAnswer(survey.getSURVEY_ID(), responseId, 1, 30.0);
        responseController.updateAnswer(survey.getSURVEY_ID(), responseId, 2, "2", TypeQuestion.MULTIPLE_CHOICE);

        Response response = responseRepository.getResponse(survey.getSURVEY_ID(), responseId);

        Answer a1 = response.getAnswer(0);
        Answer a2 = response.getAnswer(1);
        Answer a3 = response.getAnswer(2);

        assertNotNull(a1);
        assertNotNull(a2);
        assertNotNull(a3);
        assertEquals("Alice", ((TextualAnswer) a1).getAnswerText());
        assertEquals(30.0, (Double) ((NumericalAnswer) a2).getAnswerNum(), 0.01);

        userController.logoutUser();
        userController.loginUser(mainUsername, "password");
    }

    @Test
    public void testGetResponse() {
        String username = "testUser6";
        userController.registerUser(username, "test6@gmail.com", "password" , "What is your pet's name?", "Fluffy");
        userController.loginUser(username, "password");

        createTestSurvey();

        String mainUsername = userController.getUsernameLoggedIn();
        userController.logoutUser();

        String responderUsername = "responder6";
        userController.registerUser(responderUsername, "responder6@gmail.com", "password" , "What is your pet's name?", "Fluffy");
        userController.loginUser(responderUsername, "password");

        String responseId = responseController.startResponse(survey.getSURVEY_ID());
        responseController.updateAnswer(survey.getSURVEY_ID(), responseId, 0, "Bob", TypeQuestion.TEXTUAL);

        Response response = responseRepository.getResponse(survey.getSURVEY_ID(), responseId);

        assertNotNull(response);
        assertEquals(responseId, response.getRESPONSE_ID());

        userController.logoutUser();
        userController.loginUser(mainUsername, "password");
    }

    @Test
    public void testGetAllResponsesByUser() {
        String username = "testUser7";
        userController.registerUser(username, "test7@gmail.com", "password" , "What is your pet's name?", "Fluffy");
        userController.loginUser(username, "password");

        createTestSurvey();

        String mainUsername = userController.getUsernameLoggedIn();
        userController.logoutUser();

        String responderUsername = "responder7";
        userController.registerUser(responderUsername, "responder7@gmail.com", "password" , "What is your pet's name?", "Fluffy");
        userController.loginUser(responderUsername, "password");

        // Create multiple responses
        String responseId1 = responseController.startResponse(survey.getSURVEY_ID());
        responseController.updateAnswer(survey.getSURVEY_ID(), responseId1, 0, "Response 1", TypeQuestion.TEXTUAL);

        userController.logoutUser();
        userController.loginUser(mainUsername, "password");

        List<Response> responses = responseRepository.getResponsesBySurveyId(survey.getSURVEY_ID());

        assertNotNull(responses);
        assertTrue(responses.size() >= 1);
    }

    @Test
    public void testResponsePersistence() {
        String username = "testUser8";
        userController.registerUser(username, "test8@gmail.com", "password" , "What is your pet's name?", "Fluffy");
        userController.loginUser(username, "password");

        createTestSurvey();

        String mainUsername = userController.getUsernameLoggedIn();
        userController.logoutUser();

        String responderUsername = "responder8";
        userController.registerUser(responderUsername, "responder8@gmail.com", "password" , "What is your pet's name?", "Fluffy");
        userController.loginUser(responderUsername, "password");

        String responseId = responseController.startResponse(survey.getSURVEY_ID());
        responseController.updateAnswer(survey.getSURVEY_ID(), responseId, 0, "Persistent", TypeQuestion.TEXTUAL);
        responseController.updateAnswer(survey.getSURVEY_ID(), responseId, 1, 35.0);

        userController.logoutUser();
        userController.loginUser(mainUsername, "password");

        Response response = responseRepository.getResponse(survey.getSURVEY_ID(), responseId);

        assertNotNull(response);
        Answer a1 = response.getAnswer(0);
        Answer a2 = response.getAnswer(1);

        assertNotNull(a1);
        assertNotNull(a2);
        assertEquals("Persistent", ((TextualAnswer) a1).getAnswerText());
        assertEquals(35.0, (Double) ((NumericalAnswer) a2).getAnswerNum(), 0.01);
    }

    @Test
    public void testResponseUpdateAnswer() {
        String username = "testUser9";
        userController.registerUser(username, "test9@gmail.com", "password" , "What is your pet's name?", "Fluffy");
        userController.loginUser(username, "password");

        createTestSurvey();

        String mainUsername = userController.getUsernameLoggedIn();
        userController.logoutUser();

        String responderUsername = "responder9";
        userController.registerUser(responderUsername, "responder9@gmail.com", "password" , "What is your pet's name?", "Fluffy");
        userController.loginUser(responderUsername, "password");

        String responseId = responseController.startResponse(survey.getSURVEY_ID());

        // Initial answer
        responseController.updateAnswer(survey.getSURVEY_ID(), responseId, 0, "Initial", TypeQuestion.TEXTUAL);
        Response response1 = responseRepository.getResponse(survey.getSURVEY_ID(), responseId);
        Answer answer1 = response1.getAnswer(0);
        assertEquals("Initial", ((TextualAnswer) answer1).getAnswerText());

        // Update answer
        responseController.updateAnswer(survey.getSURVEY_ID(), responseId, 0, "Updated", TypeQuestion.TEXTUAL);
        Response response2 = responseRepository.getResponse(survey.getSURVEY_ID(), responseId);
        Answer answer2 = response2.getAnswer(0);
        assertEquals("Updated", ((TextualAnswer) answer2).getAnswerText());

        userController.logoutUser();
        userController.loginUser(mainUsername, "password");
    }

    @Test
    public void testMultipleResponsesPerSurvey() {
        String username = "testUser10";
        userController.registerUser(username, "test10@gmail.com", "password" , "What is your pet's name?", "Fluffy");
        userController.loginUser(username, "password");

        createTestSurvey();

        String mainUsername = userController.getUsernameLoggedIn();

        // Create multiple responses
        for (int i = 0; i < 3; i++) {
            userController.logoutUser();

            String responderUsername = "responder" + i + "_multi";
            userController.registerUser(responderUsername, responderUsername + "@gmail.com", "password" , "What is your pet's name?", "Fluffy");
            userController.loginUser(responderUsername, "password");

            String responseId = responseController.startResponse(survey.getSURVEY_ID());
            responseController.updateAnswer(survey.getSURVEY_ID(), responseId, 0, "Response " + i, TypeQuestion.TEXTUAL);
        }

        // Logout from last responder
        userController.logoutUser();

        // Login back as main user
        userController.loginUser(mainUsername, "password");

        List<Response> responses = responseRepository.getResponsesBySurveyId(survey.getSURVEY_ID());
        assertTrue(responses.size() >= 3);
    }


    @Test
    public void testResponseWithAllAnswerTypes() {
        String username = "testUser11";
        userController.registerUser(username, "test11@gmail.com", "password" , "What is your pet's name?", "Fluffy");
        userController.loginUser(username, "password");

        createTestSurvey();

        String mainUsername = userController.getUsernameLoggedIn();
        userController.logoutUser();

        String responderUsername = "responder11";
        userController.registerUser(responderUsername, "responder11@gmail.com", "password" , "What is your pet's name?", "Fluffy");
        userController.loginUser(responderUsername, "password");

        String responseId = responseController.startResponse(survey.getSURVEY_ID());

        // Add all answer types
        responseController.updateAnswer(survey.getSURVEY_ID(), responseId, 0, "Complete", TypeQuestion.TEXTUAL);
        responseController.updateAnswer(survey.getSURVEY_ID(), responseId, 1, 40.0);
        responseController.updateAnswer(survey.getSURVEY_ID(), responseId, 2, "0", TypeQuestion.MULTIPLE_CHOICE);

        Response response = responseRepository.getResponse(survey.getSURVEY_ID(), responseId);

        Answer textAnswer = response.getAnswer(0);
        Answer numAnswer = response.getAnswer(1);
        Answer mcAnswer = response.getAnswer(2);

        assertTrue(textAnswer instanceof TextualAnswer);
        assertTrue(numAnswer instanceof NumericalAnswer);
        assertTrue(mcAnswer instanceof MultipleChoiceAnswer);

        assertEquals("Complete", ((TextualAnswer) textAnswer).getAnswerText());
        assertEquals(40.0, (Double) ((NumericalAnswer) numAnswer).getAnswerNum(), 0.01);

        userController.logoutUser();
        userController.loginUser(mainUsername, "password");
    }
}
