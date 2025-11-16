package data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import domain.model.User;

public class UserRepository {
    private final Map<String, User> users; // <username, User>

    public UserRepository() {
        users = new HashMap<>();
    }

    public void addUser(User user) {
        users.put(user.getUsername(), user);
    }

    public void deleteUser(String username) {
        users.remove(username);
    }

    public User getUser(String username) {
        return users.get(username);
    }

    public Set<String> getAllUsernames() {
        return new HashSet<>(users.keySet());
    }

    public void updateUser(String username, User updatedUser) {
        users.put(username, updatedUser);
    }

    public boolean existsUser(String username) {
        return users.containsKey(username);
    }

    public void addSurveyId(String username, String surveyId) {
        users.get(username).addCreatedSurveyId(surveyId);
    }

    public void deleteSurveyId(String username, String surveyId) {
        users.get(username).removeCreatedSurveyId(surveyId);
    }

    public boolean existsSurveyId(String username, String surveyId) {
        return users.get(username).hasCreatedSurveyId(surveyId);
    }

    public Set<String> getGetAllSurveyIds(String username) {
        return users.get(username).getCreatedSurveysId();
    }

    public void addRespondedSurveyIdEntry(String username, String surveyId) {
        users.get(username).addRespondedSurveyIdEntry(surveyId);
    }

    public void deleteRespondedSurveyIdEntry(String username, String surveyId) {
        users.get(username).removeRespondedSurveyIdEntry(surveyId);
    }

    public void addResponseId(String username, String surveyId, String responseId) {
        users.get(username).addResponseId(surveyId, responseId);
    }

    public void deleteResponseId(String username, String surveyId, String responseId) {
        users.get(username).removeResponseId(surveyId, responseId);
    }

    public boolean existsResponseId(String username, String surveyId, String responseId) {
        return users.get(username).hasResponseId(surveyId, responseId);
    }

    public Set<String> getAllResponseIdsFromSurvey(String username, String surveyId) {
        return users.get(username).getResponseIdsForSurvey(surveyId);
    }

}
