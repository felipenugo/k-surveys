package domain.service;

import domain.model.User;
import data.UserRepository;

public class UserService {
    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public boolean registerUser(User user) {
        if (repository.existsUser(user.getUsername())) // use UserAlreadyExistsException (subclass of Exception) in the future
            return false;
        repository.addUser(user);
        return true;
    }

    public String loginUser(String username, String password) {
        String result; // can be improved by using enum before implementing errors
        if (!repository.existsUser(username))
            result = "user_not_exists";
        else if (!repository.getUser(username).getPassword().equals(password))
            result = "incorrect_password";
        else
            result = "success";
        return result;
    }

    public String logoutUser(String username) {
        String result;
        if (!repository.existsUser(username))
            result = "user_not_exists";
        else
            result = "success";
        return result;
    }

    public boolean deleteUser(String username) {
        if (!repository.existsUser(username))
            return false;
        repository.deleteUser(username);
        return true;
    }

    public String updateUserEmail(String username, String oldEmail, String newEmail) {
        String result;
        if (!repository.existsUser(username))
            result = "user_not_exists";
        else if (!repository.getUser(username).getEmail().equals(oldEmail))
            result = "incorrect_email";
        else if (oldEmail.equals(newEmail))
            result = "same_email";
        else {
            result = "success";
            User user = repository.getUser(username);
            user.setEmail(newEmail);
            repository.updateUser(username, user);
        }
        return result;
    }

    public String changePassword(String username, String email, String oldPassword, String newPassword) {
        String result;
        if (!repository.existsUser(username))
            result = "user_not_exists";
        else if (!repository.getUser(username).getEmail().equals(email))
            result = "incorrect_email";
        else if (!repository.getUser(username).getPassword().equals(oldPassword))
            result = "incorrect_password";
        else if (oldPassword.equals(newPassword))
            result = "same_password";
        else{
            result = "success";
            User user = repository.getUser(username);
            user.setPassword(newPassword);
            repository.updateUser(username, user);
        }
        return result;
    }
}
