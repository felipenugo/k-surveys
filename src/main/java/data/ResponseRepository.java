package data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import domain.model.MultipleChoiceAnswer;
import domain.model.Response;
import domain.model.Answer;

public class ResponseRepository {
    private final Map<String, Map<String, Response>> responses; // <surveyId, <responseId, Response>>

    public ResponseRepository() {
        responses = new HashMap<>();
    }

    // when a new survey is created, its response map must be initialized
    // SurveyService will call ResponseService -> ResponseRepository to do this
    public void addSurveyEntry(String surveyId) {
        responses.putIfAbsent(surveyId, new HashMap<>());
    }

    public void deleteSurveyEntry(String surveyId) {
        responses.remove(surveyId);
    }

    public boolean existsSurveyEntry(String surveyId) {
        return responses.containsKey(surveyId);
    }

    public void addResponse(String surveyId, Response response) {
        responses.get(surveyId).putIfAbsent(response.getRESPONSE_ID(), response);
    }

    public Response getResponse(String surveyId, String responseId) {
        return responses.get(surveyId).get(responseId);
    }

    public List<Response> getAllResponses(String surveyId) {
        return new ArrayList<>(responses.get(surveyId).values());
    }

    public void deleteResponse(String surveyId, String responseId) {
        responses.get(surveyId).remove(responseId);
    }

    public void updateResponse(String surveyId, Response response) {
        responses.get(surveyId).put(response.getRESPONSE_ID(), response);
    }

    public boolean existsResponse(String surveyId, String responseId) {
        return responses.containsKey(surveyId) && responses.get(surveyId).containsKey(responseId);
    }

    // Answer methods
    public void updateAnswer(String surveyId, String responseId, int answerIndex, Answer answer) {
        responses.get(surveyId).get(responseId).updateAnswer(answerIndex, answer);
    }

    public Answer getAnswer(String surveyId, String responseId, int answerIndex) {
        return responses.get(surveyId).get(responseId).getAnswer(answerIndex);
    }


}
