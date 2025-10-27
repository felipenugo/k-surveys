package data;

import java.util.HashMap;
import java.util.Map;

import domain.model.Survey;

public class SurveyRepository {
    private final Map<String, Survey> surveys; // <surveyId, Survey>, Survey contains its questions

    public SurveyRepository() {
        surveys = new HashMap<>();
    }

    public void addSurvey(Survey survey) {
        surveys.put(survey.getSURVEY_ID(), survey);
    }

    public void deleteSurvey(String surveyId) {
        surveys.remove(surveyId);
    }

    public Survey getSurvey(String surveyId) {
        return surveys.get(surveyId);
    }

    public void updateSurvey(String surveyId, Survey updatedSurvey) {
        surveys.put(surveyId, updatedSurvey);
    }

    public boolean existsSurvey(String surveyId) {
        return surveys.containsKey(surveyId);
    }
}
