package domain.service;

import domain.model.*;
import domain.controller.*;
import data.*;
import domain.exception.*;

import java.util.List;

public class ResponseService {
    private final ResponseRepository responseRepository;
    private final UserController userController;
    public final SurveyService surveyService;

    public ResponseService(ResponseRepository responseRepository, UserController userController, SurveyService surveyService) {
        this.responseRepository = responseRepository;
        this.userController = userController;
        this.surveyService = surveyService;
    }

    public void checkUserLoggedin() {
        if (!userController.isLoggedIn())
            throw new SurveyException("Debes iniciar sesión para poder responder encuestas.");
    }

    public void checkResponseExists(String surveyId, String responseId) {
        if (!responseRepository.existsResponse(surveyId, responseId))
            throw new ResponseException("La respuesta con id " + responseId + " no existe.");
    }

    public String getValidResponseId() {
        String lastResponseId = responseRepository.getLastResponseId();
        Integer responseId = Integer.parseInt(lastResponseId) + 1;
        return responseId.toString();
    }

    public String startResponse(String surveyId) {
        checkUserLoggedin();
        int numQuestions = surveyService.getNumQuestions(surveyId); // this method verify that the survey exists
        String responseId = getValidResponseId();
        String responderUsername = userController.getUsernameLoggedIn();
        Response response = new Response(responseId, surveyId, responderUsername, numQuestions);
        responseRepository.addResponse(surveyId, response);
        userController.addResponseId(responderUsername, surveyId, responseId); // this keeps the coherence with the double index
        return responseId;
    }

    public List<Question> getQuestions(String surveyId) {
        return surveyService.getQuestions(surveyId);
    }

    public List<Answer> getAnswers(String surveyid, String responseId) {
        surveyService.checkSurveyExists(surveyid);
        checkResponseExists(surveyid, responseId);
        List<Answer> answers = responseRepository.getAllAnswers(surveyid, responseId);
        if (answers.isEmpty())
            throw new ResponseException("Todavía no has respondido ninguna pregunta.");
        return answers;
    }
}
