package domain.service;

import domain.exception.LogInException;
import domain.exception.RegisterException;

import domain.model.User;
import data.UserRepository;
import data.SurveyRepository;
import data.ResponseRepository;
import java.util.Set;
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
    private final SurveyRepository surveyRepository;
    private final ResponseRepository responseRepository;

    /**
     * Crea una nueva instancia del servicio de usuarios.
     *
     * @param userRepository repositorio donde se almacenan los usuarios
     */
    public UserService(UserRepository userRepository, SurveyRepository surveyRepository, ResponseRepository responseRepository) {
        this.userRepository = userRepository;
        this.surveyRepository = surveyRepository;
        this.responseRepository = responseRepository;
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
        validatePasswordStrength(password);
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
    // Vinculación de encuestas creadas a usuarios
    // ───────────────────────────────────────────────

    /**
     * Registra una encuesta creada por un usuario en su perfil.
     *
     * Este método sincroniza la base de datos relacional con el archivo JSON del usuario,
     * agregando el identificador de la encuesta creada a la lista de encuestas del usuario.
     *
     * Si el usuario no existe, se lanza una excepción.
     *
     * @param username nombre de usuario creador de la encuesta
     * @param surveyId identificador de la encuesta creada
     * @throws RuntimeException si el usuario no existe
     */
    public void addSurveyCreated(String username, String surveyId) {
        if (!userRepository.existsUser(username)) {
            System.err.println("[WARNING] Usuario " + username + " no encontrado al intentar registrar encuesta " + surveyId);
            throw new RuntimeException("El usuario " + username + " no existe en el sistema.");
        }
        userRepository.addSurveyId(username, surveyId);
        System.out.println("[LOG] Encuesta " + surveyId + " registrada para usuario " + username);
    }

    /**
     * Elimina una encuesta creada del perfil del usuario.
     *
     * Este método sincroniza la base de datos relacional con el archivo JSON del usuario,
     * eliminando el identificador de la encuesta de la lista de encuestas creadas.
     *
     * Si el usuario no existe, se lanza una excepción.
     *
     * @param username nombre de usuario creador de la encuesta
     * @param surveyId identificador de la encuesta a eliminar
     * @throws RuntimeException si el usuario no existe
     */
    public void removeSurveyCreated(String username, String surveyId) {
        if (!userRepository.existsUser(username)) {
            System.err.println("[WARNING] Usuario " + username + " no encontrado al intentar eliminar encuesta " + surveyId);
            throw new RuntimeException("El usuario " + username + " no existe en el sistema.");
        }
        userRepository.deleteSurveyId(username, surveyId);
        System.out.println("[LOG] Encuesta " + surveyId + " eliminada del perfil del usuario " + username);
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
     * Elimina completamente un usuario del sistema, junto con:
     * - Todas las encuestas que ha creado.
     * - Todas sus respuestas a encuestas.
     * - Todas las referencias asociadas en UserRepository.
     *
     * @param username nombre de usuario a eliminar
     * @return true si el usuario existía y se ha eliminado, false en caso contrario
     */
    public boolean deleteUserok(String username) {

        if (!userRepository.existsUser(username))
            return false;

        User user = userRepository.getUser(username);

        // 1. Eliminar todas las encuestas creadas
        for (String surveyId : user.getCreatedSurveysId()) {
            // eliminar respuestas asociadas a esa encuesta
            responseRepository.deleteResponsesBySurvey(surveyId);

            // eliminar la encuesta en sí
            surveyRepository.deleteSurvey(surveyId);
        }

        // 2. Eliminar todas las respuestas emitidas por el usuario
        for (String surveyId : user.getRespondedSurveysIds()) {

            Set<String> responseIds = user.getResponseIdsForSurvey(surveyId);

            if (responseIds != null) {
                for (String responseId : responseIds) {
                    responseRepository.deleteResponse(responseId, surveyId);
                }
            }
        }

        // 3. Eliminar usuario de la persistencia
        userRepository.deleteUser(username);

        return true;
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
        validatePasswordStrength(newPassword);
        user.setPasswordHash(PasswordHasher.hash(newPassword));

        userRepository.updateUser(username, user);
    }

    public void validatePasswordStrength(String password) {
    StringBuilder errors = new StringBuilder();

        if (password.length() < 8)
            errors.append("- Debe tener al menos 8 caracteres\n");

        if (!password.matches(".*[A-Z].*"))
            errors.append("- Debe contener al menos una MAYÚSCULA\n");

        if (!password.matches(".*[a-z].*"))
            errors.append("- Debe contener al menos una minúscula\n");

        if (!password.matches(".*\\d.*"))
            errors.append("- Debe contener al menos un número\n");

        if (!password.matches(".*[@#$%^&+=!?.*()\\[\\]{}_-].*"))
            errors.append("- Debe contener al menos un carácter especial (@#$%^&+=!?.*()[]{}_-)\n");

        if (errors.length() > 0) {
            throw new RegisterException(
                "La contraseña no es segura:\n" + errors
            );
        }
    }
}
