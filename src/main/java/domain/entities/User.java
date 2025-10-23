package domain.entities;

public class User {
    private final String USERNAME; // identifier for user
    private String password; // could be improved in the future for real security
    private String email; // could allow changing email in the future

    // Constructor
    public User(String USERNAME, String password, String email) {
        this.USERNAME = USERNAME;
        this.password = password;
        this.email = email;
    }

    // Getters
    public String getUsername() {
        return USERNAME;
    }

    public String getPassword() {
        return password; // could be improved in the future for real security
    }

    public String getEmail() {
        return email;
    }
}
