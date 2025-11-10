package domain.service;

import data.SurveyRepository;

public class SurveyService {
    private final SurveyRepository surveyRepository;

    public SurveyService(SurveyRepository surveyRepository) {
        this.surveyRepository = surveyRepository;
    }

    // if a survey is created correctly, call ResponseService to initialize its response map
}
