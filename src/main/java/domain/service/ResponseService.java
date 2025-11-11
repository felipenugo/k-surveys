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


    public void startResponse(String surveyId, String responseId) {
        checkUserLoggedin();
        int numQuestions = surveyService.getNumQuestions(surveyId); // this method verify that the survey exists
        if (responseRepository.existsResponse(surveyId, responseId)) {
            throw new RegisterException("Introduce otro id de respuesta."); // responseId will be improved in the future
        }
        String responderUsername = userController.getUsernameLoggedIn();
        Response response = new Response(responseId, surveyId, responderUsername, numQuestions);
        responseRepository.addResponse(surveyId, response);
    }

    public List<Question> getQuestions(String surveyId)
    {
        return surveyService.getQuestions(surveyId);
    }
}
