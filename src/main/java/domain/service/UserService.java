package domain.service;

import domain.controller.UserController;
import domain.exception.LogInException;
import domain.exception.RegisterException;

import domain.model.User;
import data.UserRepository;

import javax.security.auth.login.LoginException;

public class UserService {
    private final UserRepository userRepository;
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    private boolean isInputBlank(String text) {
        return text == null || text.trim().isEmpty();
    }

    public void registerUser(String username, String email, String password) {
        // Input Validation
        if (isInputBlank(username))
            throw new RegisterException("El campo nombre de usuario es obligatorio y no puede estar vacío.");

        if (isInputBlank(email))
            throw new RegisterException("El campo email es obligatorio y no puede estar vacío.");

        if (isInputBlank(password))
            throw new RegisterException("El campo contraseña es obligatorio y no puede estar vacío.");

        // Business rules validation
        if (!email.contains("@gmail.com") && !email.contains("@fib.upc.edu"))
            throw new RegisterException("El formato del email debe terminar en @gmail.com o @fib.upc.edu.");

        if (userRepository.existsUser(username))
            throw new RegisterException("El usuario " + username + " ya existe. Por favor escoge otro nombre de usuario.");

        // Registration
        User newUser = new User(username, email, password);
        userRepository.addUser(newUser);
    }

    public void verifyCredentials(String username, String password) {
        // Input Validation
        if (isInputBlank(username))
            throw new LogInException("El campo nombre de usuario es obligatorio y no puede estar vacío.");
        if (isInputBlank(password))
            throw new LogInException("El campo contraseña es obligatorio y no puede estar vacío");

        // Business rules validation
        if (!userRepository.existsUser(username))
            throw new LogInException("El usuario " + username + " no existe.");
        if (!userRepository.getUser(username).getPassword().equals(password))
            throw new LogInException("La contraseña es incorrecta.");
    }

    public void addResponseId(String username, String surveyId, String responseId) {
        userRepository.addRespondedSurveyIdEntry(username, surveyId);
        userRepository.addResponseId(username, surveyId, responseId);
    }

    public void loginUser(String username, String password) {
        String result; // can be improved by using enum before implementing errors
        if (!userRepository.existsUser(username))
            result = "user_not_exists";
        else if (!userRepository.getUser(username).getPassword().equals(password))
            result = "incorrect_password";
        else
            result = "success";
        System.out.println("result");
        ;
    }

    public String logoutUser(String username) {
        String result;
        if (!userRepository.existsUser(username))
            result = "user_not_exists";
        else
            result = "success";
        return result;
    }

    public boolean deleteUser(String username) {
        if (!userRepository.existsUser(username))
            return false;
        userRepository.deleteUser(username);
        return true;
    }

    public String updateUserEmail(String username, String oldEmail, String newEmail) {
        String result;
        if (!userRepository.existsUser(username))
            result = "user_not_exists";
        else if (!userRepository.getUser(username).getEmail().equals(oldEmail))
            result = "incorrect_email";
        else if (oldEmail.equals(newEmail))
            result = "same_email";
        else {
            result = "success";
            User user = userRepository.getUser(username);
            user.setEmail(newEmail);
            userRepository.updateUser(username, user);
        }
        return result;
    }

    public String changePassword(String username, String email, String oldPassword, String newPassword) {
        String result;
        if (!userRepository.existsUser(username))
            result = "user_not_exists";
        else if (!userRepository.getUser(username).getEmail().equals(email))
            result = "incorrect_email";
        else if (!userRepository.getUser(username).getPassword().equals(oldPassword))
            result = "incorrect_password";
        else if (oldPassword.equals(newPassword))
            result = "same_password";
        else {
            result = "success";
            User user = userRepository.getUser(username);
            user.setPassword(newPassword);
            userRepository.updateUser(username, user);
        }
        return result;
    }

    public User getUser(String username) {
        return userRepository.getUser(username);
    }
}
