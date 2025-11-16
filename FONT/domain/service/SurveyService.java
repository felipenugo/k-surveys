package domain.service;

import data.SurveyRepository;
import domain.controller.UserController;
import domain.exception.SurveyException;
import domain.model.*;
import java.util.List;

public class SurveyService {
    private final SurveyRepository surveyRepository;
    private final UserController userController;

    public SurveyService(SurveyRepository surveyRepository, UserController userController) { this.surveyRepository = surveyRepository; this.userController = userController; }

    public void checkUserLoggedin() { if (!userController.isLoggedIn()) throw new SurveyException("Debes iniciar sesión para poder responder encuestas."); }
    public void checkSurveyExists(String surveyId) { if (!surveyRepository.existsSurvey(surveyId)) throw new SurveyException("La encuesta con id " + surveyId + " no existe."); }
    public String generateUniqueSurveyId() { return surveyRepository.generateNextSurveyId(); }
    public Survey createSurvey(Survey survey) { checkUserLoggedin(); if (survey.getTitle() == null || survey.getTitle().trim().isEmpty()) throw new SurveyException("El título de la encuesta no puede estar vacío."); if (survey.getDescription() == null || survey.getDescription().trim().isEmpty()) throw new SurveyException("La descripción de la encuesta no puede estar vacía."); if (survey.getSize() == 0) throw new SurveyException("La encuesta debe tener al menos una pregunta."); if (!survey.getCREATOR_USERNAME().equals(userController.getLoggedUser().getUsername())) throw new SurveyException("Solo puedes crear encuestas a tu nombre."); String surveyId = survey.getSURVEY_ID(); if (surveyId == null || surveyId.trim().isEmpty()) { surveyId = generateUniqueSurveyId(); Survey surveyWithId = new Survey(surveyId, survey.getTitle(), survey.getDescription(), survey.getCREATOR_USERNAME()); surveyWithId.setSurveyStatus(survey.getSurveyStatus()); if (survey.getPUBLISHED_AT() != null) surveyWithId.setPUBLISHED_AT(); for (int i = 0; i < survey.getSize(); i++) surveyWithId.addQuestion(survey.getQuestion(i)); survey = surveyWithId; } else { if (surveyRepository.existsSurvey(surveyId)) throw new SurveyException("Ya existe una encuesta con el ID: " + surveyId); } surveyRepository.addSurvey(survey); return survey; }
    public Survey initializeNewSurvey(String title, String description, String creatorUsername) { checkUserLoggedin(); if (title == null || title.trim().isEmpty()) throw new SurveyException("El título no puede estar vacío."); if (description == null || description.trim().isEmpty()) throw new SurveyException("La descripción no puede estar vacía."); return new Survey(title, description, creatorUsername); }
    public void checkQuestionExists(String surveyId, int questionIndex) { if (!existsQuestion(surveyId, questionIndex)) throw new SurveyException("La pregunta con índice " + questionIndex + " no existe en esta encuesta."); }
    public List<Survey> getSelectedSurveys() { checkUserLoggedin(); List<Survey> surveys = surveyRepository.getAllSurveys(); if (surveys.isEmpty()) throw new SurveyException("No hay encuestas creadas todavía."); return surveys; }
    public List<String> getSurveysId() { checkUserLoggedin(); return surveyRepository.getAllSurveysId(); }
    public int getNumQuestions(String surveyId) { checkSurveyExists(surveyId); return surveyRepository.getNumQuestions(surveyId); }
    public List<Question> getQuestions(String surveyId) { checkSurveyExists(surveyId); List<Question> questions = surveyRepository.getAllQuestions(surveyId); if (questions.isEmpty()) throw new SurveyException("La encuesta con id " + surveyId + " no tiene preguntas."); return questions; }
    public Question getQuestion(String surveyId, int questionIndex) { checkQuestionExists(surveyId, questionIndex); return surveyRepository.getQuestion(surveyId, questionIndex); }
    public Survey getSurvey(String surveyId) { checkSurveyExists(surveyId); return surveyRepository.getSurvey(surveyId); }
    public List<Survey> getMySurveys() { checkUserLoggedin(); return surveyRepository.getSurveysByUsername(userController.getLoggedUser().getUsername()); }
    public boolean existsQuestion(String surveyId, int questionIndex) { checkSurveyExists(surveyId); int numQuestions = surveyRepository.getNumQuestions(surveyId); return questionIndex >= 0 && questionIndex < numQuestions; }
    public boolean existsSurvey(String surveyId) { return surveyRepository.existsSurvey(surveyId); }
    public void incrementResponseCount(String surveyId) { checkSurveyExists(surveyId); Survey survey = surveyRepository.getSurvey(surveyId); int responseCount = survey.getViews() + 1; survey.setViews(responseCount); surveyRepository.addSurvey(survey); }
}
