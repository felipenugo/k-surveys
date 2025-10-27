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
    private Set<String> createdSurveysId; // allows accessing surveys almost directly with the username in database
    private Map<String, Set<String>> respondedSurveys; // <surveyId, <responseId1, responseId2, ...>>


    // Constructor
    public User(String USERNAME, String email, String password) {
        this.USERNAME = USERNAME;
        this.email = email;
        this.password = password;
        this.createdSurveysId = new HashSet<>();
        this.respondedSurveys = new HashMap<>();
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


    // Setters
    public void setEmail(String newEmail) {
        this.email = newEmail;
    }

    public void setPassword(String newPassword) {
        this.password = newPassword;
    }

}
