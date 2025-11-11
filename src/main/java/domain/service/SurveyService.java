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
        List<Survey> surveys = surveyRepository.getAllSurveys();
        if (surveys.isEmpty())
            throw new SurveyException("No hay encuestas creadas todavía.");
        return surveys;
    }

    public List<String> getSurveysId() {
        checkUserLoggedin();
        return surveyRepository.getAllSurveysId();
    }

    public int getNumQuestions(String surveyId) {
        checkSurveyExists(surveyId);
        return surveyRepository.getNumQuestions(surveyId);
    }

    public List<Question> getQuestions(String surveyId) {
        checkSurveyExists(surveyId);
        List<Question> questions = surveyRepository.getAllQuestions(surveyId);
        if (questions.isEmpty())
            throw new SurveyException("La encuesta con id " + surveyId + " no tiene preguntas.");
        return questions;
    }

    public Survey getSurvey(String surveyId) {
        checkSurveyExists(surveyId);
        return surveyRepository.getSurvey(surveyId);
    }

    public List<Survey> getMySurveys() {
        checkUserLoggedin();
        return surveyRepository.getSurveysByUsername(userController.getLoggedUser().getUsername());
    }
}
