package domain.controller;

import domain.exception.LogInException;
import domain.model.User;
import domain.service.UserService;
/**
 * Controlador responsable de gestionar las acciones relacionadas con los usuarios.
 * 
 * Esta clase actúa como intermediaria entre la capa de presentación (interfaz de usuario)
 * y la capa de servicio ({@link UserService}), encargándose de coordinar las operaciones
 * de registro, inicio de sesión, cierre de sesión y eliminación de usuarios.
 * 
 * Además, mantiene el estado del usuario actualmente logueado en el sistema.
 * 
 */

public class UserController {
 /** Servicio que contiene la lógica de negocio relacionada con los usuarios. */
    private final UserService userService;

    /** Nombre del usuario actualmente logueado. */
    private String usernameLoggedIn;

    /** Indica si hay un usuario logueado en el sistema. */
    private boolean loggedIn;

    // ───────────────────────────────────────────────
    // Constructores
    // ───────────────────────────────────────────────

    /**
     * Crea un nuevo controlador de usuarios con el servicio especificado.
     *
     * @param userService instancia del servicio de usuarios que gestionará las operaciones
     */

    public UserController(UserService userService) {
        this.userService = userService;
        this.usernameLoggedIn = null;
        this.loggedIn = false;
    }
    // ───────────────────────────────────────────────
    // Getters
    // ───────────────────────────────────────────────

    /**
     * Devuelve el nombre del usuario actualmente logueado.
     *
     * @return nombre del usuario logueado, o {@code null} si no hay ninguno
     */

    public String getUsernameLoggedIn() {
        return this.usernameLoggedIn;
    }

     /**
     * Indica si hay un usuario logueado en el sistema.
     *
     * @return {@code true} si hay un usuario logueado, {@code false} en caso contrario
     */

    public boolean isLoggedIn() {
        return loggedIn;
    }

    // ───────────────────────────────────────────────
    // Métodos principales
    // ───────────────────────────────────────────────

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * Este método delega la operación al {@link UserService}, que se encarga de validar
     * los datos y almacenar al nuevo usuario.
     *
     * @param username nombre del usuario a registrar
     * @param email    correo electrónico del usuario
     * @param password contraseña del usuario
     * @throws domain.exception.RegisterException si los datos de entrada no son válidos
     */

    public void registerUser(String username, String email, String password, String securityQuestion, String securityAnswer) {
        userService.registerUser(username, email, password, securityQuestion, securityAnswer);
    }

    /**
     * Inicia sesión con las credenciales proporcionadas.
     *
     * Si ya hay un usuario logueado, lanza una {@link LogInException}.
     * En caso contrario, delega la validación de credenciales al {@link UserService}.
     *
     * @param username nombre del usuario
     * @param password contraseña del usuario
     * @throws LogInException si las credenciales son incorrectas o ya hay una sesión activa
     */
    public void loginUser(String username, String password) {
        if (this.loggedIn) {
            throw new LogInException("El usuario " + this.usernameLoggedIn + " ya ha iniciado sesión. Por favor, cierra sesión antes de iniciar sesión con otra cuenta.");
        }
        userService.verifyCredentials(username, password);
        this.usernameLoggedIn = username; // not executed if exception in loginUser is thrown
        this.loggedIn = true;
    }

    /**
     * Cierra la sesión del usuario actualmente logueado.
     *
     * Si no hay ningún usuario logueado, lanza una {@link LogInException}.
     *
     * @throws LogInException si no hay ningún usuario logueado
     */

    public void logoutUser() {
        if (!this.loggedIn)
            throw new LogInException("Ningún usuario ha iniciado sesión.");
        this.usernameLoggedIn = null;
        this.loggedIn = false;
    }

    /**
     * Devuelve el usuario que ha iniciado sesión actualmente en el sistema.
     *
     * <p>Si ningún usuario ha iniciado sesión, este método lanza una
     * {@link LogInException} para indicar que la operación requiere una sesión activa.</p>
     *
     * @return el objeto {@link User} asociado al nombre de usuario actualmente autenticado
     * @throws LogInException si no hay ningún usuario con sesión iniciada
     */
    public User getLoggedUser() {
        if (!this.loggedIn) {
            throw new LogInException("Ningún usuario ha iniciado sesión.");
        }
        return userService.getUser(this.usernameLoggedIn);
    }

    /**
     * Añade un identificador de respuesta asociado a un usuario y a una encuesta específica.
     *
     * <p>Este método delega la operación al {@link UserService}, que se encarga de almacenar
     * la relación entre el usuario, la encuesta y la respuesta correspondiente.</p>
     *
     * @param username   nombre de usuario al que pertenece la respuesta
     * @param surveyId   identificador de la encuesta
     * @param responseId identificador de la respuesta enviada por el usuario
     */
    public void addResponseId(String username, String surveyId, String responseId){
        userService.addResponseId(username, surveyId, responseId);
    }

    /**
     * Elimina un usuario del sistema.
     *
     * Este método invoca el correspondiente método del {@link UserService} para
     * realizar la operación de eliminación y muestra un mensaje informativo por consola
     * según el resultado.
     *
     * @param username nombre del usuario a eliminar
     */

    public void deleteUser(String username) {
        boolean success = userService.deleteUser(username);
        if (success)
            System.out.println("El usuario " + username + " ha sido eliminado con éxito.");
        else
            System.out.println("El usuario " + username + " no existe.");
    }
     /*
     * Métodos adicionales (comentados actualmente):
     * - updateUserEmail(...)
     * - changePassword(...)
     * 
     * En versiones futuras del sistema, estos métodos permitirán al usuario modificar
     * su correo electrónico o contraseña de forma segura, delegando la lógica de negocio
     * al UserService.
     */


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
    public String startPasswordRecovery(String username) {
    return userService.startPasswordRecovery(username);
    }

    public boolean verifySecurityAnswer(String username, String answer) {
        return userService.verifySecurityAnswer(username, answer);
    }

    public void resetPassword(String username, String newPassword) {
        userService.resetPassword(username, newPassword);
    }
}
