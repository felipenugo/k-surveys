package domain.controller;

import domain.model.Survey;
import domain.service.SurveyService;

import java.util.List;

public class SurveyController {
    private final SurveyService surveyService;

    public SurveyController(SurveyService surveyService) {
        this.surveyService = surveyService;

    }
    public void createSurvey(Survey survey) {
        surveyService.createSurvey(survey);
    }
    public Survey initializeNewSurvey(String title, String description, String creatorUsername) {
        return surveyService.initializeNewSurvey(title, description, creatorUsername);
    }

    // In the future it'll return a selected number of surveys depending on pageNumber and pageSize
    public List<Survey> getSelectedSurveys(){
        return surveyService.getSelectedSurveys();
    }

    public List<String> getSurveysId()
    {
        return surveyService.getSurveysId();
    }

    public Survey getSurvey(String surveyId) {
        return surveyService.getSurvey(surveyId);
    }

    public List<Survey> getMySurveys() {
        return surveyService.getMySurveys();
    }

    public boolean existsSurvey(String surveyId)
    {
        return surveyService.existsSurvey(surveyId);
    }
}
