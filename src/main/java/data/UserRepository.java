package data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import domain.model.User;

/**
 * Repositorio encargado de la gestión en memoria de los usuarios del sistema.
 *
 * Este repositorio actúa como una estructura persistente en memoria basada en un
 * mapa <username, User>, permitiendo almacenar, consultar, actualizar y eliminar
 * usuarios registrados.
 *
 * También gestiona:
 * <ul>
 *     <li>Los identificadores de encuestas creadas por cada usuario.</li>
 *     <li>El registro de encuestas respondidas y sus respectivos responseId.</li>
 *     <li>La coherencia del doble índice entre usuario–encuesta–respuesta.</li>
 * </ul>
 *
 * IMPORTANTE:
 * Antes de llamar a cualquier método que acceda a un usuario (getUser, addSurveyId,
 * addResponseId, etc.), se debe verificar su existencia mediante
 * {@link #existsUser(String)}. De lo contrario, podría producirse un
 * {@link NullPointerException}.
 *
 * Este repositorio no realiza validaciones de negocio; su única responsabilidad es
 * almacenar y recuperar datos de forma consistente. La lógica de validación se gestiona
 * en {@code UserService}.
 */
public class UserRepository {
    /** Mapa que almacena los usuarios registrados del sistema. */
    private final Map<String, User> users; // <username, User>
    /**
     * Crea un nuevo repositorio de usuarios en memoria.
     * Inicializa la estructura de almacenamiento.
     */
    public UserRepository() {
        users = new HashMap<>();
    }

    // ───────────────────────────────────────────────
    // Gestión básica del usuario
    // ───────────────────────────────────────────────

    /**
     * Añade un usuario al repositorio.
     *
     * @param user usuario a almacenar
     */
    public void addUser(User user) {
        users.put(user.getUsername(), user);
    }

    /**
     * Elimina un usuario del repositorio.
     *
     * @param username nombre de usuario del usuario a eliminar
     */
    public void deleteUser(String username) {
        users.remove(username);
    }

    /**
     * Obtiene un usuario del repositorio.
     *
     * @param username nombre de usuario del usuario a obtener
     * @return usuario correspondiente al nombre de usuario
     */
    public User getUser(String username) {
        return users.get(username);
    }

    /**
     * Obtiene todos los nombres de usuario registrados en el sistema.
     *
     * @return conjunto con todos los nombres de usuario
     */
    public Set<String> getAllUsernames() {
        return new HashSet<>(users.keySet());
    }

    /**
     * Actualiza un usuario en el repositorio.
     *
     * @param username    nombre de usuario del usuario a actualizar
     * @param updatedUser usuario con los datos actualizados
     */
    public void updateUser(String username, User updatedUser) {
        users.put(username, updatedUser);
    }

    /**
     * Verifica si un usuario existe en el repositorio.
     *
     * @param username nombre de usuario a verificar
     * @return {@code true} si el usuario existe, {@code false} en caso contrario
     */
    public boolean existsUser(String username) {
        return users.containsKey(username);
    }

    // ───────────────────────────────────────────────
    // Gestión de encuestas creadas por usuario
    // ───────────────────────────────────────────────
    /**
     * Añade una encuesta al listado de encuestas creadas por un usuario.
     *
     * @param username usuario creador
     * @param surveyId id de la encuesta creada
     */
    public void addSurveyId(String username, String surveyId) {
        users.get(username).addCreatedSurveyId(surveyId);
    }

    /**
     * Elimina una encuesta del listado de encuestas creadas.
     *
     * @param username usuario creador
     * @param surveyId id de la encuesta
     */
    public void deleteSurveyId(String username, String surveyId) {
        users.get(username).removeCreatedSurveyId(surveyId);
    }

    /**
     * Verifica si una encuesta existe en el listado de encuestas creadas por un usuario.
     *
     * @param username usuario creador
     * @param surveyId id de la encuesta
     * @return {@code true} si la encuesta existe, {@code false} en caso contrario
     */
    public boolean existsSurveyId(String username, String surveyId) {
        return users.get(username).hasCreatedSurveyId(surveyId);
    }

    /**
     * Obtiene todos los IDs de encuestas creadas por un usuario.
     *
     * @param username usuario creador
     * @return conjunto con los IDs de las encuestas creadas
     */
    public Set<String> getGetAllSurveyIds(String username) {
        return users.get(username).getCreatedSurveysId();
    }

    // ───────────────────────────────────────────────
    // Gestión de encuestas respondidas por usuario
    // ───────────────────────────────────────────────
    /**
     * Añade una entrada para una encuesta respondida por un usuario.
     * Esta entrada mantiene la coherencia del doble índice entre usuario, encuesta y respuesta.
     *
     * @param username nombre de usuario
     * @param surveyId id de la encuesta respondida
     */
    public void addRespondedSurveyIdEntry(String username, String surveyId) {
        users.get(username).addRespondedSurveyIdEntry(surveyId); // keep coherence with de double indexing
    }

    /**
     * Elimina una entrada de encuesta respondida por un usuario.
     * Esta operación mantiene la coherencia del doble índice entre usuario, encuesta y respuesta.
     *
     * @param username nombre de usuario
     * @param surveyId id de la encuesta
     */
    public void deleteRespondedSurveyIdEntry(String username, String surveyId) {
        users.get(username).removeRespondedSurveyIdEntry(surveyId); // keep coherence with de double indexing

    }

    /**
     * Verifica si una entrada de encuesta respondida por un usuario existe.
     * Esta verificación mantiene la coherencia del doble índice entre usuario, encuesta y respuesta.
     *
     * @param username nombre de usuario
     * @param surveyId id de la encuesta
     * @return true si existe, false en caso contrario
     */
    public void addResponseId(String username, String surveyId, String responseId) {
        users.get(username).addResponseId(surveyId, responseId);
    }

    /**
     * Elimina un responseId de las respuestas de un usuario para una encuesta específica.
     * Esta operación mantiene la coherencia del doble índice entre usuario, encuesta y respuesta.
     *
     * @param username nombre de usuario
     * @param surveyId id de la encuesta
     * @param responseId id de la respuesta a eliminar
     */
    public void deleteResponseId(String username, String surveyId, String responseId) {
        users.get(username).removeResponseId(surveyId, responseId);
    }

    /**
     * Verifica si un responseId existe en las respuestas de un usuario para una encuesta específica.
     * Esta verificación mantiene la coherencia del doble índice entre usuario, encuesta y respuesta.
     *
     * @param username nombre de usuario
     * @param surveyId id de la encuesta
     * @param responseId id de la respuesta a verificar
     * @return true si existe, false en caso contrario
     */
    public boolean existsResponseId(String username, String surveyId, String responseId) {
        return users.get(username).hasResponseId(surveyId, responseId);
    }

    /**
     * Obtiene todos los IDs de respuestas de un usuario para una encuesta específica.
     *
     * @param username nombre de usuario
     * @param surveyId id de la encuesta
     * @return conjunto con los IDs de las respuestas
     */
    public Set<String> getAllResponseIdsFromSurvey(String username, String surveyId) {
        return users.get(username).getResponseIdsForSurvey(surveyId);
    }

}
