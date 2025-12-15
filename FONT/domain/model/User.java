package domain.model;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Set;

import domain.exception.RegisterException;

import java.util.Map;

/**
 * Representa un usuario del sistema que puede crear y responder encuestas.
 * 
 * Esta clase actúa como modelo de dominio y contiene la información básica
 * asociada a un usuario, incluyendo su nombre de usuario, correo electrónico,
 * contraseña y las encuestas creadas o respondidas.
 * 
 * Además, valida los datos de entrada tanto en el constructor como en los setters,
 * lanzando una {@link RegisterException} si alguno de los valores es nulo o vacío.
 * 
 */

public class User {
    // Attributes
    /** Nombre de usuario único que identifica al usuario en el sistema. */
    private final String USERNAME; // identifier for user
    /** Correo electrónico asociado al usuario. */
    private String email; // could allow changing email in the future
    /** Contraseña del usuario. */
    private String passwordHash;
    /** Campos nuevos para recuperación de contraseña */
    private String securityQuestion;
    /** Respuesta a la pregunta de seguridad del usuario. */
    private String securityAnswer;
    /** Conjunto con los identificadores de las encuestas creadas por el usuario. */
    private final Set<String> createdSurveysId; // allows accessing surveys almost directly with the username in database
    /**
     * Mapa con las encuestas respondidas por el usuario.
     * 
     * Cada entrada asocia un {@code surveyId} con un conjunto de {@code responseId}
     * que representan las respuestas del usuario en esa encuesta.
     */
    private final Map<String, Set<String>> respondedSurveysId; // <surveyId, <responseId1, responseId2, ...>>


    // ───────────────────────────────────────────────
    // Constructores
    // ───────────────────────────────────────────────

    /**
     * Crea un nuevo usuario con los parámetros especificados.
     * 
     * @param USERNAME nombre de usuario (no puede ser nulo ni vacío)
     * @param email    correo electrónico (no puede ser nulo ni vacío)
     * @param passwordHash contraseña (no puede ser nula ni vacía)
     * @throws RegisterException si alguno de los parámetros es nulo o vacío
     */
    public User(String USERNAME, String email, String passwordHash) {
        this.USERNAME = USERNAME;
        this.email = email;
        this.passwordHash = passwordHash;
        this.securityQuestion = null;
        this.securityAnswer = null;
        this.createdSurveysId = new HashSet<>();
        this.respondedSurveysId = new HashMap<>();
    }

    /**
     * Constructor con pregunta y respuesta de seguridad.
     */
    public User(String USERNAME, String email, String passwordHash,
                String securityQuestion, String securityAnswer) {
        this.USERNAME = USERNAME;
        this.email = email;
        this.passwordHash = passwordHash;
        this.securityQuestion = securityQuestion;
        this.securityAnswer = securityAnswer;
        this.createdSurveysId = new HashSet<>();
        this.respondedSurveysId = new HashMap<>();
    }

    // ───────────────────────────────────────────────
    // Métodos de acceso (Getters)
    // ───────────────────────────────────────────────

    /**
     * Devuelve el nombre de usuario.
     * 
     * @return el nombre de usuario
     */
    public String getUsername() {
        return USERNAME;
    }

    /**
     * Devuelve el correo electrónico del usuario.
     * 
     * @return el correo electrónico
     */
    public String getEmail() {
        return email;
    }

    /**
     * Devuelve la contraseña del usuario.
     * 
     * @return la contraseña
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * Devuelve el conjunto de identificadores de encuestas creadas por el usuario.
     * 
     * @return conjunto de identificadores de encuestas creadas
     */
    public Set<String> getCreatedSurveysId() {
        return createdSurveysId;
    }

    /**
     * Devuelve el mapa de encuestas respondidas por el usuario.
     * 
     * @return mapa de encuestas respondidas
     */
    public Map<String, Set<String>> getRespondedSurveysId() {
        return respondedSurveysId;
    }

    // Getters para recuperación de contraseña
    /**
     * Devuelve la pregunta de seguridad del usuario.
     * 
     * @return la pregunta de seguridad
     */
    public String getSecurityQuestion() {
        return securityQuestion;
    }

    /**
     * Devuelve la respuesta de seguridad del usuario.
     * 
     * @return la respuesta de seguridad
     */
    public String getSecurityAnswer() {
        return securityAnswer;
    }

    // ───────────────────────────────────────────────
    // Métodos de modificación (Setters) con validación
    // ───────────────────────────────────────────────
    /**
     * Actualiza el correo electrónico del usuario.
     * 
     * @param newEmail nuevo correo electrónico (no puede ser nulo ni vacío)
     */
    public void setEmail(String newEmail) {
        this.email = newEmail;
    }

    /**
     * Actualiza la contraseña del usuario.
     * 
     * @param newPassword nueva contraseña (no puede ser nula ni vacía)
     */
    public void setPasswordHash(String newPassword) {
        this.passwordHash = newPassword;
    }

    // Setters para recuperación de contraseña
    /**
     * Actualiza la pregunta de seguridad del usuario.
     * 
     * @param securityQuestion nueva pregunta de seguridad
     */
    public void setSecurityQuestion(String securityQuestion) {
        this.securityQuestion = securityQuestion;
    }

    /**
     * Actualiza la respuesta de seguridad del usuario.
     * 
     * @param securityAnswer nueva respuesta de seguridad
     */
    public void setSecurityAnswer(String securityAnswer) {
        this.securityAnswer = securityAnswer;
    }

    // ───────────────────────────────────────────────
    // Métodos relacionados con encuestas creadas
    // ───────────────────────────────────────────────

    /**
     * Añade una nueva encuesta creada por el usuario.
     * 
     * @param surveyId identificador de la encuesta a añadir
     */
    public void addCreatedSurveyId(String surveyId) {
        this.createdSurveysId.add(surveyId);
    }

    /**
     * Elimina una encuesta creada por el usuario.
     * 
     * @param surveyId identificador de la encuesta a eliminar
     */
    public void removeCreatedSurveyId(String surveyId) {
        this.createdSurveysId.remove(surveyId);
    }

    /**
     * Comprueba si el usuario ha creado una encuesta específica.
     * 
     * @param surveyId identificador de la encuesta a comprobar
     * @return {@code true} si el usuario ha creado la encuesta, {@code false} en caso contrario
     */
    public boolean hasCreatedSurveyId(String surveyId) {
        return this.createdSurveysId.contains(surveyId);
    }

    /**
     * Devuelve el número total de encuestas creadas por el usuario.
     * 
     * @return número total de encuestas creadas
     */
    public int getTotalSurveysCreated() {
        return createdSurveysId.size();
    }

    // ───────────────────────────────────────────────
    // Métodos relacionados con encuestas respondidas
    // ───────────────────────────────────────────────
    /**
     * Añade una nueva entrada para una encuesta respondida por el usuario.
     * 
     * @param surveyId identificador de la encuesta
     */

    // surveyId entry management
    public void addRespondedSurveyIdEntry(String surveyId) {
        this.respondedSurveysId.putIfAbsent(surveyId, new HashSet<>());
    }

    /**
     * Elimina una entrada de encuesta respondida por el usuario.
     * 
     * @param surveyId identificador de la encuesta
     */
    public void removeRespondedSurveyIdEntry(String surveyId) {
        this.respondedSurveysId.remove(surveyId);
    }

    /**
     * Comprueba si existe una entrada para una encuesta respondida por el usuario.
     * 
     * @param surveyId identificador de la encuesta
     * @return {@code true} si existe la entrada, {@code false} en caso contrario
     */
    public boolean existsRespondedSurveyIdEntry(String surveyId) {
        return this.respondedSurveysId.containsKey(surveyId);
    }

    /**
     * Añade un identificador de respuesta a una encuesta respondida por el usuario.
     * 
     * @param surveyId   identificador de la encuesta
     * @param responseId identificador de la respuesta
     */
    public void addResponseId(String surveyId, String responseId) {
        this.respondedSurveysId.get(surveyId).add(responseId);
    }

    /**
     * Devuelve el conjunto de identificadores de respuestas para una encuesta específica.
     * 
     * @param surveyId identificador de la encuesta
     * @return conjunto de identificadores de respuestas
     */
    public Set<String> getResponseIdsForSurvey(String surveyId) {
        return this.respondedSurveysId.get(surveyId);
    }

    /**
     * Elimina un identificador de respuesta de una encuesta respondida por el usuario.
     * 
     * @param surveyId   identificador de la encuesta
     * @param responseId identificador de la respuesta a eliminar
     */
    public void removeResponseId(String surveyId, String responseId) {
        this.respondedSurveysId.get(surveyId).remove(responseId);
    }

    /**
     * Comprueba si un identificador de respuesta existe para una encuesta respondida por el usuario.
     * 
     * @param surveyId   identificador de la encuesta
     * @param responseId identificador de la respuesta
     * @return {@code true} si el identificador de respuesta existe, {@code false} en caso contrario
     */
    public boolean hasResponseId(String surveyId, String responseId) {
        return this.respondedSurveysId.containsKey(surveyId) && this.respondedSurveysId.get(surveyId).contains(responseId);
    }

    /**
     * Devuelve el conjunto de identificadores de encuestas respondidas por el usuario.
     * 
     * @return conjunto de identificadores de encuestas respondidas
     */
    public Set<String> getRespondedSurveysIds() {
        return this.respondedSurveysId.keySet();
    }
}
