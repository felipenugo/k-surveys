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

    // User methods
    public void addUser(User user) {
        users.put(user.getUsername(), user);
    }

    public void deleteUser(String username) {
        users.remove(username);
    }

    // existsUser method must be used before calling this method to avoid NullPointerException
    public User getUser(String username) {
        return users.get(username);
    }

    // This method will be used to assign valid usernames
    public Set<String> getAllUsernames() {
        return new HashSet<>(users.keySet());
    }

    public void updateUser(String username, User updatedUser) {
        users.put(username, updatedUser);
    }

    public boolean existsUser(String username) {
        return users.containsKey(username);
    }

    // CreatedSurveysId methods
    public void addSurveyId(String username, String surveyId) {
        users.get(username).addCreatedSurveyId(surveyId);
        users.get(username).addRespondedSurveyIdEntry(surveyId); // keep coherence with de double indexing
    }

    public void deleteSurveyId(String username, String surveyId) {
        users.get(username).removeCreatedSurveyId(surveyId);
        users.get(username).removeRespondedSurveyIdEntry(surveyId); // keep coherence with de double indexing
    }

    public boolean existsSurveyId(String username, String surveyId) {
        return users.get(username).hasCreatedSurveyId(surveyId);
    }

    public Set<String> getGetAllSurveyIds(String username) {
        return users.get(username).getCreatedSurveysId();
    }

    //RespondedSurveysId methods
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
