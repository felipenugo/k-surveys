package data;

import com.google.gson.Gson;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import domain.model.User;

/**
 * Repositorio encargado de la gestión en memoria de los usuarios del sistema.
 * Mantiene una estructura &lt;username, User&gt; y gestiona encuestas creadas y respondidas.
 * No realiza validaciones de negocio; actúa como capa de persistencia en memoria.
 */
public class UserRepository {
    /**
     * Mapa que almacena los usuarios registrados del sistema.
     */
    private final Map<String, User> users; // <username, User>
    /**
     * String que almacena la dirección del fichero donde se guardan los usuarios.
     */
    private final String FILE_PATH;
    /**
     * Tipo de mapa que se utiliza para cargar y guardar los usuarios.
     */
    private final Type mapType;
    private final Gson gson;

    // cargar mapa users desde el fichero DATA/db/users.json

    private Map<String, User> loadUsersFromJson() {
        File file = new File(FILE_PATH);
        if (!file.exists() || file.length() == 0) {
            return new HashMap<>();
        }
        try (Reader reader = new FileReader(file)) {
            return gson.fromJson(reader, mapType);
        }catch(Exception e) {
            throw new RuntimeException(
                    "Error al cargar los usuarios desde el fichero: " + FILE_PATH, e);
        }
    }

    private void saveUsersToJson() {
        // try-with-resources -> forzar escritura inmediata (writer.flush()) y cerrar el canal de escriture (writer.close())
        // crea el fichero si no existe
        /*
        try anidado
        try{
        File file = new File(FILE_PATH);
        file.getParentFile().mkdirs(); // crea el directorio padre si no existe
         */
        try (Writer writer = new FileWriter(FILE_PATH)) {
            gson.toJson(users, writer);
        }  catch (Exception e) {
            throw new RuntimeException("Error al guardar los usuarios en el fichero: " + FILE_PATH, e);
        }
    }

    /**
     * Crea un nuevo repositorio de usuarios en memoria.
     * Inicializa la estructura de almacenamiento.
     */
    public UserRepository() {
        this("DATA/db/users.json");
    }

    /**
     * Crea un nuevo repositorio de usuarios con un path personalizado.
     * @param filePath ruta al archivo JSON de usuarios
     */
    public UserRepository(String filePath) {
        this.FILE_PATH = filePath;
        this.mapType = new TypeToken<Map<String, User>>() {
        }.getType();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.users = loadUsersFromJson();
    }

    /**
     * Elimina todos los usuarios del repositorio.
     */
    public void clear() {
        users.clear();
        saveUsersToJson();
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
        saveUsersToJson();
    }

    /**
     * Elimina un usuario del repositorio.
     *
     * @param username nombre de usuario del usuario a eliminar
     */
    public void deleteUser(String username) {
        users.remove(username);
        saveUsersToJson();
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
        saveUsersToJson();
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
        saveUsersToJson();
    }

    /**
     * Elimina una encuesta del listado de encuestas creadas.
     *
     * @param username usuario creador
     * @param surveyId id de la encuesta
     */
    public void deleteSurveyId(String username, String surveyId) {
        users.get(username).removeCreatedSurveyId(surveyId);
        saveUsersToJson();
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
        users.get(username).addRespondedSurveyIdEntry(surveyId); // mantener coherencia con el doble índice
        saveUsersToJson();
    }

    /**
     * Elimina una entrada de encuesta respondida por un usuario.
     * Esta operación mantiene la coherencia del doble índice entre usuario, encuesta y respuesta.
     *
     * @param username nombre de usuario
     * @param surveyId id de la encuesta
     */
    public void deleteRespondedSurveyIdEntry(String username, String surveyId) {
        users.get(username).removeRespondedSurveyIdEntry(surveyId); // mantener coherencia con el doble índice
        saveUsersToJson();

    }

    /**
     * Añade un responseId a las respuestas de un usuario para una encuesta específica.
     * Esta operación mantiene la coherencia del doble índice entre usuario, encuesta y respuesta.
     *
     * @param username   nombre de usuario
     * @param surveyId   id de la encuesta
     * @param responseId id de la respuesta a añadir
     */
    public void addResponseId(String username, String surveyId, String responseId) {
        users.get(username).addResponseId(surveyId, responseId);
        saveUsersToJson();
    }

    /**
     * Elimina un responseId de las respuestas de un usuario para una encuesta específica.
     * Esta operación mantiene la coherencia del doble índice entre usuario, encuesta y respuesta.
     *
     * @param username   nombre de usuario
     * @param surveyId   id de la encuesta
     * @param responseId id de la respuesta a eliminar
     */
    public void deleteResponseId(String username, String surveyId, String responseId) {
        users.get(username).removeResponseId(surveyId, responseId);
        saveUsersToJson();
    }

    /**
     * Verifica si un responseId existe en las respuestas de un usuario para una encuesta específica.
     * Esta verificación mantiene la coherencia del doble índice entre usuario, encuesta y respuesta.
     *
     * @param username   nombre de usuario
     * @param surveyId   id de la encuesta
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
