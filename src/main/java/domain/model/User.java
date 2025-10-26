package domain.model;

import java.util.ArrayList;

public class User {
    private final String USERNAME; // identifier for user
    private String email; // could allow changing email in the future
    private String password; // could be improved in the future for real security
    private ArrayList<Integer> surveysId;

    // Constructor
    public User(String USERNAME, String email, String password) {
        this.USERNAME = USERNAME;
        this.email = email;
        this.password = password;
        this.surveysId = new ArrayList<>();
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

    public ArrayList<Integer> getSurveysId() {
        return surveysId;
    }
}
