package domain.model;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Set;
import java.util.Map;

public class User {
    // Attributes
    private final String USERNAME; // identifier for user
    private String email; // could allow changing email in the future
    private String password; // could be improved in the future for real security
    private final Set<String> createdSurveysId; // allows accessing surveys almost directly with the username in database
    private final Map<String, Set<String>> respondedSurveysId; // <surveyId, <responseId1, responseId2, ...>>


    // Constructor
    public User(String USERNAME, String email, String password) {
        this.USERNAME = USERNAME;
        this.email = email;
        this.password = password;
        this.createdSurveysId = new HashSet<>();
        this.respondedSurveysId = new HashMap<>();
    }

    // Getters
    public String getUsername() {
        return USERNAME;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    } // could be improved in the future for real security

    public Set<String> getCreatedSurveysId() {
        return createdSurveysId;
    }

    public Map<String, Set<String>> getRespondedSurveysId() {
        return respondedSurveysId;
    }


    // Setters
    public void setEmail(String newEmail) {
        this.email = newEmail;
    }

    public void setPassword(String newPassword) {
        this.password = newPassword;
    }

    // createdSurveysId methods, correct usage must be ensured by the caller
    public void addCreatedSurveyId(String surveyId) {
        this.createdSurveysId.add(surveyId);
    }

    public void removeCreatedSurveyId(String surveyId) {
        this.createdSurveysId.remove(surveyId);
    }

    public boolean existsCreatedSurveyId(String surveyId) {
        return this.createdSurveysId.contains(surveyId);
    }

    public int getTotalSurveysCreated() {
        return createdSurveysId.size();
    }

    // respondedSurveysId methods, correct usage must be ensured by the caller

    // surveyId entry management
    public void addRespondedSurveyIdEntry(String surveyId) {
        this.respondedSurveysId.putIfAbsent(surveyId, new HashSet<>());
    }

    public void deleteRespondedSurveyIdEntry(String surveyId) {
        this.respondedSurveysId.remove(surveyId);
    }

    public boolean existsRespondedSurveyIdEntry(String surveyId) {
        return this.respondedSurveysId.containsKey(surveyId);
    }

    // responseId management within a surveyId entry
    public void addResponseIdToSurvey(String surveyId, String responseId) {
        this.respondedSurveysId.get(surveyId).add(responseId);
    }

    public Set<String> getResponseIdsForSurvey(String surveyId) {
        return this.respondedSurveysId.get(surveyId);
    }

    public void deleteResponseIdFromSurvey(String surveyId, String responseId) {
        this.respondedSurveysId.get(surveyId).remove(responseId);
    }

    public boolean existsResponseIdInSurvey(String surveyId, String responseId) {
        return this.respondedSurveysId.containsKey(surveyId) && this.respondedSurveysId.get(surveyId).contains(responseId);
    }

}
