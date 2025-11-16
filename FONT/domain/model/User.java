package domain.model;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Set;
import java.util.Map;
import domain.exception.RegisterException;

public class User {
    private final String USERNAME;
    private String email;
    private String password;
    private final Set<String> createdSurveysId;
    private final Map<String, Set<String>> respondedSurveysId;

    public User(String USERNAME, String email, String password) {
        this.USERNAME = USERNAME;
        this.email = email;
        this.password = password;
        this.createdSurveysId = new HashSet<>();
        this.respondedSurveysId = new HashMap<>();
    }

    public String getUsername() { return USERNAME; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public Set<String> getCreatedSurveysId() { return createdSurveysId; }
    public Map<String, Set<String>> getRespondedSurveysId() { return respondedSurveysId; }
    public void setEmail(String newEmail) { this.email = newEmail; }
    public void setPassword(String newPassword) { this.password = newPassword; }
    public void addCreatedSurveyId(String surveyId) { this.createdSurveysId.add(surveyId); }
    public void removeCreatedSurveyId(String surveyId) { this.createdSurveysId.remove(surveyId); }
    public boolean hasCreatedSurveyId(String surveyId) { return this.createdSurveysId.contains(surveyId); }
    public int getTotalSurveysCreated() { return createdSurveysId.size(); }
    public void addRespondedSurveyIdEntry(String surveyId) { this.respondedSurveysId.putIfAbsent(surveyId, new HashSet<>()); }
    public void removeRespondedSurveyIdEntry(String surveyId) { this.respondedSurveysId.remove(surveyId); }
    public boolean existsRespondedSurveyIdEntry(String surveyId) { return this.respondedSurveysId.containsKey(surveyId); }
    public void addResponseId(String surveyId, String responseId) { this.respondedSurveysId.get(surveyId).add(responseId); }
    public Set<String> getResponseIdsForSurvey(String surveyId) { return this.respondedSurveysId.get(surveyId); }
    public void removeResponseId(String surveyId, String responseId) { this.respondedSurveysId.get(surveyId).remove(responseId); }
    public boolean hasResponseId(String surveyId, String responseId) { return this.respondedSurveysId.containsKey(surveyId) && this.respondedSurveysId.get(surveyId).contains(responseId); }
}
