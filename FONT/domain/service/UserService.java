package domain.service;

import domain.exception.LogInException;
import domain.exception.RegisterException;

import domain.model.User;
import data.UserRepository;
import domain.utils.PasswordHasher;

/**
 * Servicio encargado de gestionar la lógica de negocio asociada a los usuarios.
 * 
 * Esta clase valida los datos de entrada, aplica las reglas de negocio del sistema
 * y delega en el {@link UserRepository} la persistencia de los usuarios.
 * 
 * Gestiona operaciones como registro, verificación de credenciales, actualización
 * de email o contraseña, eliminación de usuarios y vinculación de respuestas con encuestas.
 * 
 * Todas las validaciones críticas generan excepciones específicas:
 * {@link RegisterException} para problemas durante el registro y
 * {@link LogInException} para errores en el inicio de sesión.
 */
public class UserService {
    /** Repositorio encargado de almacenar y gestionar los usuarios. */
    private final UserRepository userRepository;
    /**
     * Crea una nueva instancia del servicio de usuarios.
     *
     * @param userRepository repositorio donde se almacenan los usuarios
     */
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ───────────────────────────────────────────────
    // Métodos auxiliares
    // ───────────────────────────────────────────────

    /**
     * Comprueba si una cadena es nula o vacía.
     *
     * @param text texto a comprobar
     * @return {@code true} si es nula o vacía, {@code false} en caso contrario
     */
    private boolean isInputBlank(String text) {
        return text == null || text.trim().isEmpty();
    }

    // ───────────────────────────────────────────────
    // Registro de usuarios
    // ───────────────────────────────────────────────

    /**
     * Registra un nuevo usuario en el sistema tras validar sus datos.
     * 
     * Este método:
     * <ul>
     *   <li>verifica que los campos no estén vacíos,</li>
     *   <li>comprueba que el email tenga un formato permitido,</li>
     *   <li>confirma que el usuario no exista previamente,</li>
     *   <li>crea y almacena al nuevo usuario si todo es correcto.</li>
     * </ul>
     *
     * @param username nombre de usuario
     * @param email email del usuario
     * @param password contraseña del usuario
     * 
     * @throws RegisterException si los datos son inválidos o el usuario ya existe
     */
    public void registerUser(String username, String email, String password, String securityQuestion, String securityAnswer) {
        // Input Validation
        if (isInputBlank(username))
            throw new RegisterException("El campo nombre de usuario es obligatorio y no puede estar vacío.");

        if (isInputBlank(email))
            throw new RegisterException("El campo email es obligatorio y no puede estar vacío.");

        if (isInputBlank(password))
            throw new RegisterException("El campo contraseña es obligatorio y no puede estar vacío.");

        if (isInputBlank(securityQuestion))
            throw new RegisterException("El campo pregunta de seguridad es obligatorio y no puede estar vacío.");
        
        if (isInputBlank(securityAnswer))
            throw new RegisterException("El campo respuesta de seguridad es obligatorio y no puede estar vacío.");

        // Business rules validation
        if (!email.contains("@gmail.com") && !email.contains("@fib.upc.edu"))
            throw new RegisterException("El formato del email debe terminar en @gmail.com o @fib.upc.edu.");

        if (userRepository.existsUser(username))
            throw new RegisterException("El usuario " + username + " ya existe. Por favor escoge otro nombre de usuario.");

        // Registration
        String passwordHash = PasswordHasher.hash(password);
        User newUser = new User(username, email, passwordHash, securityQuestion, securityAnswer);
        userRepository.addUser(newUser);
    }

    // ───────────────────────────────────────────────
    // Inicio de sesión
    // ───────────────────────────────────────────────

    /**
     * Verifica las credenciales de un usuario comprobando:
     * <ul>
     *   <li>que el nombre de usuario no es vacío,</li>
     *   <li>que la contraseña no es vacía,</li>
     *   <li>que el usuario existe,</li>
     *   <li>que la contraseña coincide.</li>
     * </ul>
     *
     * @param username nombre de usuario
     * @param password contraseña introducida
     * 
     * @throws LogInException si algún dato es inválido o las credenciales no coinciden
     */
    public void verifyCredentials(String username, String password) {
        // Input Validation
        if (isInputBlank(username))
            throw new LogInException("El campo nombre de usuario es obligatorio y no puede estar vacío.");
        if (isInputBlank(password))
            throw new LogInException("El campo contraseña es obligatorio y no puede estar vacío");

        // Business rules validation
        if (!userRepository.existsUser(username))
            throw new LogInException("El usuario " + username + " no existe.");
        if (!userRepository.getUser(username).getPasswordHash().equals(PasswordHasher.hash(password)))
            throw new LogInException("La contraseña es incorrecta.");
    }
    // ───────────────────────────────────────────────
    // Vinculación de respuestas a encuestas
    // ───────────────────────────────────────────────

    /**
     * Añade una respuesta realizada por un usuario a una encuesta concreta.
     *
     * @param username nombre de usuario que responde
     * @param surveyId identificador de la encuesta
     * @param responseId identificador de la respuesta
     */

    public void addResponseId(String username, String surveyId, String responseId) {
        userRepository.addRespondedSurveyIdEntry(username, surveyId);
        userRepository.addResponseId(username, surveyId, responseId);
    }

    // ───────────────────────────────────────────────
    // Métodos heredados / placeholders (no usados actualmente)
    // ───────────────────────────────────────────────

    /**
     * Método obsoleto no utilizado por el sistema.
     * 
     * Se mantiene únicamente por compatibilidad con posibles versiones antiguas,
     * pero la lógica real de login se gestiona desde {@link domain.controller.UserController}.
     */
    public void loginUser(String username, String password) {
        String result; // can be improved by using enum before implementing errors
        if (!userRepository.existsUser(username))
            result = "user_not_exists";
        else if (!userRepository.getUser(username).getPasswordHash().equals(password))
            result = "incorrect_password";
        else
            result = "success";
        System.out.println("result");
        ;
    }

    /**
     * Método obsoleto no utilizado por el sistema.
     *
     * @param username nombre del usuario que desea cerrar sesión
     * @return resultado de la operación
     */
    public String logoutUser(String username) {
        String result;
        if (!userRepository.existsUser(username))
            result = "user_not_exists";
        else
            result = "success";
        return result;
    }

    // ───────────────────────────────────────────────
    // Eliminación y actualización de usuarios
    // ───────────────────────────────────────────────

    /**
     * Elimina un usuario si existe en el repositorio.
     *
     * @param username nombre del usuario a eliminar
     * @return {@code true} si fue eliminado, {@code false} si no existía
     */
    public boolean deleteUser(String username) {
        if (!userRepository.existsUser(username))
            return false;
        userRepository.deleteUser(username);
        return true;
    }

     /**
     * Actualiza el email de un usuario tras validar:
     * <ul>
     *   <li>que el usuario existe,</li>
     *   <li>que el email anterior coincide,</li>
     *   <li>que el nuevo email no es igual al anterior.</li>
     * </ul>
     *
     * @param username nombre del usuario
     * @param oldEmail email actual
     * @param newEmail nuevo email
     * @return código de resultado ("success", "user_not_exists", "incorrect_email", "same_email")
     */
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

    /**
     * Cambia la contraseña de un usuario tras validar:
     * <ul>
     *   <li>existencia del usuario,</li>
     *   <li>coincidencia del email,</li>
     *   <li>coincidencia de la contraseña antigua,</li>
     *   <li>que la nueva contraseña no sea igual a la anterior.</li>
     * </ul>
     *
     * @param username nombre del usuario
     * @param email email del usuario
     * @param oldPassword contraseña actual
     * @param newPassword nueva contraseña
     * @return código de resultado ("success", "user_not_exists", "incorrect_email", "incorrect_password", "same_password")
     */
    public String changePassword(String username, String email, String oldPassword, String newPassword) {
        String result;
        if (!userRepository.existsUser(username))
            result = "user_not_exists";
        else if (!userRepository.getUser(username).getEmail().equals(email))
            result = "incorrect_email";
        else if (!userRepository.getUser(username).getPasswordHash().equals(PasswordHasher.hash(oldPassword)))
            result = "incorrect_password";
        else if (oldPassword.equals(newPassword))
            result = "same_password";
        else {
            result = "success";
            User user = userRepository.getUser(username);
            user.setPasswordHash(PasswordHasher.hash(newPassword));
            userRepository.updateUser(username, user);
        }
        return result;
    }

    /**
     * Devuelve un usuario del repositorio.
     *
     * @param username nombre de usuario
     * @return objeto {@link User} correspondiente o {@code null} si no existe
     */
    public User getUser(String username) {
        return userRepository.getUser(username);
    }

    public String startPasswordRecovery(String username) {
    if (isInputBlank(username))
        throw new LogInException("El campo nombre de usuario no puede estar vacío.");

    if (!userRepository.existsUser(username))
        throw new LogInException("El usuario " + username + " no existe.");

    // Recuperar la pregunta secreta
    return userRepository.getUser(username).getSecurityQuestion();
    }

    public boolean verifySecurityAnswer(String username, String answer) {
    if (isInputBlank(answer))
        throw new LogInException("La respuesta no puede estar vacía.");

    if (!userRepository.existsUser(username))
        throw new LogInException("El usuario " + username + " no existe.");

    User user = userRepository.getUser(username);

    return user.getSecurityAnswer().equalsIgnoreCase(answer.trim());
    }

    public void resetPassword(String username, String newPassword) {
    if (isInputBlank(newPassword))
        throw new LogInException("La nueva contraseña no puede estar vacía.");

    if (!userRepository.existsUser(username))
        throw new LogInException("El usuario " + username + " no existe.");

    User user = userRepository.getUser(username);
    user.setPasswordHash(PasswordHasher.hash(newPassword));

    userRepository.updateUser(username, user);
    }
}
