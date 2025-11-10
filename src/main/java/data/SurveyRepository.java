package data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import domain.model.Survey;
import domain.model.Question;

public class SurveyRepository {
    private final Map<String, Survey> surveys; // <surveyId, Survey>, Survey contains its questions

    public SurveyRepository() {
        surveys = new HashMap<>();
    }

    // Survey methods
    public void addSurvey(Survey survey) {
        surveys.put(survey.getSURVEY_ID(), survey);
    }

    public void deleteSurvey(String surveyId) {
        surveys.remove(surveyId);
    }

    public Survey getSurvey(String surveyId) {
        return surveys.get(surveyId);
    }

    public List<Survey> getAllSurveys() {
        return new ArrayList<>(surveys.values());
    }

    public void updateSurvey(String surveyId, Survey updatedSurvey) {
        surveys.put(surveyId, updatedSurvey);
    }

    public boolean existsSurvey(String surveyId) {
        return surveys.containsKey(surveyId);
    }

    // Question methods
    public void addQuestion(String surveyId, Question question) {
        surveys.get(surveyId).addQuestion(question);
    }

    public void deleteQuestion(String surveyId, int questionIndex) {
        surveys.get(surveyId).removeQuestion(questionIndex);
    }

    public void updateQuestion(String surveyId, int questionIndex, Question question) {
        surveys.get(surveyId).updateQuestion(questionIndex, question);
    }

    public Question getQuestion(String surveyId, int questionIndex) {
        return surveys.get(surveyId).getQuestion(questionIndex);
    }

    public List<Question> getAllQuestions(String surveyId) {
        return surveys.get(surveyId).getQuestions();
    }

    public int getNumQuestions(String surveyId) {
        return surveys.get(surveyId).getSize();
    }

    public void swapQuestions(String surveyId, int oldQuestionIndex, int newQuestionIndex) {
        surveys.get(surveyId).reorderQuestion(oldQuestionIndex, newQuestionIndex);
    }

    public void deleteAllQuestions(String surveyId) {
        surveys.get(surveyId).clearQuestions();
    }
}
