package domain.controller;

import domain.model.User;
import domain.service.UserService;

public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    public void registerUser(String username, String email, String password) {
         service.registerUser(username, email, password);
    }

    public String loginUser(String username, String password) {
        return service.loginUser(username, password);
    }

    public void logoutUser(String username) {
        String result = service.logoutUser(username);
        switch (result) {
            case "user_not_exists" -> System.out.println("El usuario " + username + " no existe.");
            case "success" -> System.out.println("El usuario " + username + " ha cerrado sesión con éxito.");
        }

    }

    public void deleteUser(String username) {
        boolean success = service.deleteUser(username);
        if (success)
            System.out.println("El usuario " + username + " ha sido eliminado con éxito.");
        else
            System.out.println("El usuario " + username + " no existe.");
    }

    public void updateUserEmail(String username, String oldEmail, String newEmail) {
        String result = service.updateUserEmail(username, oldEmail, newEmail);
        switch (result) {
            case "user_not_exists" -> System.out.println("El usuario " + username + " no existe.");
            case "incorrect_email" -> System.out.println("El email " + oldEmail + " es incorrecto.");
            case "same_email" -> System.out.println("El nuevo email es el mismo que el anterior");
            case "success" -> System.out.println("El nuevo email del usuario " + username + " es " + newEmail + ".");
        }
    }

    public void changePassword(String username, String email, String password, String newPassword) {
        String result = service.changePassword(username, email, password, newPassword);
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
