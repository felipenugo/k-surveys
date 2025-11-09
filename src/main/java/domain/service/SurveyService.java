package domain.service;

import data.SurveyRepository;
import domain.controller.UserController;

public class SurveyService {
    private final SurveyRepository surveyRepository;
    private final UserController userController;

    public SurveyService(SurveyRepository surveyRepository, UserController userController) {
        this.surveyRepository = surveyRepository;
        this.userController = userController;
    }

    // if a survey is created correctly, call ResponseService to initialize its response map
}
