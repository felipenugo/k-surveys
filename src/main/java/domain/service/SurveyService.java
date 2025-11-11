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

    public void createSurvey(Survey survey) {
        checkUserLoggedin();

        // Validar que la encuesta tenga título
        if (survey.getTitle() == null || survey.getTitle().trim().isEmpty()) {
            throw new SurveyException("El título de la encuesta no puede estar vacío.");
        }

        // Validar que la encuesta tenga descripción
        if (survey.getDescription() == null || survey.getDescription().trim().isEmpty()) {
            throw new SurveyException("La descripción de la encuesta no puede estar vacía.");
        }

        // Validar que la encuesta tenga al menos una pregunta
        if (survey.getSize() == 0) {
            throw new SurveyException("La encuesta debe tener al menos una pregunta.");
        }

        // Validar que la encuesta tenga un ID válido
        if (survey.getSURVEY_ID() == null || survey.getSURVEY_ID().trim().isEmpty()) {
            throw new SurveyException("La encuesta debe tener un ID válido.");
        }

        // Verificar que no exista ya una encuesta con ese ID
        if (surveyRepository.existsSurvey(survey.getSURVEY_ID())) {
            throw new SurveyException("Ya existe una encuesta con el ID: " + survey.getSURVEY_ID());
        }

        // Verificar que el creador sea el usuario logueado
        if (!survey.getCREATOR_USERNAME().equals(userController.getLoggedUser().getUsername())) {
            throw new SurveyException("Solo puedes crear encuestas a tu nombre.");
        }

        // Guardar en el repositorio
        surveyRepository.addSurvey(survey);
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

    public Survey getSurvey(String surveyId) {
        checkSurveyExists(surveyId);
        return surveyRepository.getSurvey(surveyId);
    }

    public List<Survey> getMySurveys() {
        checkUserLoggedin();
        return surveyRepository.getSurveysByUsername(userController.getLoggedUser().getUsername());
    }
}
