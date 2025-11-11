package domain.service;

import data.SurveyRepository;
import domain.controller.UserController;
import domain.exception.SurveyException;
import domain.model.*;

import java.util.List;

public class SurveyService {
    private final SurveyRepository surveyRepository;
    private final UserController userController;

    public SurveyService(SurveyRepository surveyRepository, UserController userController) {
        this.surveyRepository = surveyRepository;
        this.userController = userController;
    }

    public void checkUserLoggedin() {
        if (!userController.isLoggedIn())
            throw new SurveyException("Debes iniciar sesión para poder responder encuestas.");

    }

    public void checkSurveyExists(String surveyId) {
        if (!surveyRepository.existsSurvey(surveyId))
            throw new SurveyException("La encuesta con id " + surveyId + " no existe.");
    }

    // if a survey is created correctly, call ResponseService to initialize its response map
    public List<Survey> getSelectedSurveys() {
        checkUserLoggedin();
        return surveyRepository.getAllSurveys();
    }

    public List<String> getSurveysId() {
        checkUserLoggedin();
        return surveyRepository.getAllSurveysId();
    }

    public int getNumQuestions(String surveyId) {
        checkSurveyExists(surveyId);
        return surveyRepository.getNumQuestions(surveyId);
    }

    public List<Question> getQuestions(String surveyId)
    {
        checkSurveyExists(surveyId);
        return surveyRepository.getAllQuestions(surveyId);
    }
}
