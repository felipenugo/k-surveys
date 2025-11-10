package domain.controller;

import domain.service.SurveyService;

public class SurveyController {
    private final SurveyService surveyService;

    public SurveyController(SurveyService surveyService) {
        this.surveyService = surveyService;

    }
}
