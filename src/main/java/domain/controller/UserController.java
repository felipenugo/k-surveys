package domain.controller;

import domain.exception.LogInException;
import domain.model.User;
import domain.service.UserService;

public class UserController {

    private final UserService userService;
    private String usernameLoggedIn;
    private boolean loggedIn;

    public UserController(UserService userService) {
        this.userService = userService;
        this.usernameLoggedIn = null;
        this.loggedIn = false;
    }

    public String getUsernameLoggedIn() {
        return this.usernameLoggedIn;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void registerUser(String username, String email, String password) {
        userService.registerUser(username, email, password);
    }

    public void loginUser(String username, String password) {
        if (this.loggedIn) {
            throw new LogInException("El usuario " + this.usernameLoggedIn + " ya ha iniciado sesión. Por favor, cierra sesión antes de iniciar sesión con otra cuenta.");
        }
        userService.verifyCredentials(username, password);
        this.usernameLoggedIn = username; // not executed if exception in loginUser is thrown
        this.loggedIn = true;
    }

    public void logoutUser() {
        if (!this.loggedIn)
            throw new LogInException("Ningún usuario ha iniciado sesión.");
        this.usernameLoggedIn = null;
        this.loggedIn = false;
    }

    public User getLoggedUser() {
        if (!this.loggedIn) {
            throw new LogInException("Ningún usuario ha iniciado sesión.");
        }
        return userService.getUser(this.usernameLoggedIn);
    }

    public void deleteUser(String username) {
        boolean success = userService.deleteUser(username);
        if (success)
            System.out.println("El usuario " + username + " ha sido eliminado con éxito.");
        else
            System.out.println("El usuario " + username + " no existe.");
    }

    public void updateUserEmail(String username, String oldEmail, String newEmail) {
        String result = userService.updateUserEmail(username, oldEmail, newEmail);
        switch (result) {
            case "user_not_exists" -> System.out.println("El usuario " + username + " no existe.");
            case "incorrect_email" -> System.out.println("El email " + oldEmail + " es incorrecto.");
            case "same_email" -> System.out.println("El nuevo email es el mismo que el anterior");
            case "success" -> System.out.println("El nuevo email del usuario " + username + " es " + newEmail + ".");
        }
    }

    public void changePassword(String username, String email, String password, String newPassword) {
        String result = userService.changePassword(username, email, password, newPassword);
        switch (result) {
            case "user_not_exists" -> System.out.println("El usuario " + username + " no existe.");
            case "incorrect_email" -> System.out.println("El email " + email + " es incorrecto.");
            case "incorrect_password" -> System.out.println("La contraseña " + password + "no existe.");
            case "same_password" -> System.out.println("La nueva contraseña es la misma que la anterior");
            case "success" ->
                    System.out.println("La nueva contraseña del usuario " + username + " es " + newPassword + ".");
        }
    }
}
