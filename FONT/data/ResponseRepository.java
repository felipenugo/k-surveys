package data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import data.adapter.AnswerAdapter;
import data.adapter.LocalDateTimeAdapter;
import domain.model.Answer;
import domain.model.Response;

import java.io.*;
import java.lang.reflect.Type;
import com.google.gson.reflect.TypeToken;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Repositorio encargado de almacenar en memoria todas las respuestas del sistema.
 * Utiliza una estructura Map&lt;surveyId, Map&lt;responseId, Response&gt;&gt; para indexación eficiente.
 * No realiza validaciones de negocio; actúa como capa de persistencia en memoria.
 */
public class ResponseRepository {
    /** Estructura &lt;surveyId, &lt;responseId, Response&gt;&gt; */
    private final Map<String, Map<String, Response>> responses; // <surveyId, <responseId, Response>>
    /**
     * String que almacena la dirección del fichero donde se guardan los usuarios.
     */
    private final String FILE_PATH;
    /**
     * Tipo de mapa que se utiliza para cargar y guardar los usuarios.
     */
    private final Type mapType;
    private final Gson gson;
    /** Último identificador asignado para respuestas. */
    private String lastResponseId;

    private Map<String, Map<String, Response>>loadResponsesFromJson() {
        File file = new File(FILE_PATH);
        if(!file.exists()  || file.length()==0)
            return new HashMap<>();
        try(Reader reader = new FileReader(file)) {
            return gson.fromJson(reader, mapType);
        }catch(Exception e) {
            throw new RuntimeException(
                    "Error al cargar las respuestas desde el fichero: " + FILE_PATH, e);
        }
    }

    /**
     * Crea un repositorio vacío para almacenar respuestas.
     * El primer identificador asignado será "0".
     */
    public ResponseRepository() {
        this("DATA/db/responses.json");
    }

    /**
     * Crea un repositorio de respuestas con un path personalizado.
     * @param filePath ruta al archivo JSON de respuestas
     */
    public ResponseRepository(String filePath) {
        this.FILE_PATH = filePath;
        this.mapType = new TypeToken<Map<String, Map<String, Response>>>(){}.getType();
        // json con formato y adaptador para poder usar LocalDateTime que no está soportado por defecto
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Answer.class, new AnswerAdapter())
                .setPrettyPrinting().create();
        this.responses = loadResponsesFromJson();
        this.lastResponseId = "0";
    }

    /**
     * Elimina todas las respuestas del repositorio.
     */
    public void clear() {
        responses.clear();
        saveResponsesToJson();
    }

    private void saveResponsesToJson() {
        // try-with-resources -> forzar escritura inmediata (writer.flush()) y cerrar el canal de escriture (writer.close())
        // crea el fichero si no existe
        /*
        try anidado
        try{
        File file = new File(FILE_PATH);
        file.getParentFile().mkdirs(); // crea el directorio padre si no existe
         */
        try (Writer writer = new FileWriter(FILE_PATH)) {
            gson.toJson(responses, writer);
        } catch (Exception e) {
            throw new RuntimeException("Error al guardar las respuestas en el fichero: " + FILE_PATH, e);
        }
    }

    // ───────────────────────────────────────────────
    // Gestión del identificador de respuesta
    // ───────────────────────────────────────────────

    /** Obtiene el último identificador de respuesta asignado.
     *
     * @return último ID de respuesta
     */
    public String getLastResponseId() {
        return lastResponseId;
    }

    /** Actualiza el último identificador de respuesta asignado.
     *
     * @param lastResponseId nuevo último ID de respuesta
     */
    public void setLastResponseId(String lastResponseId) {
        this.lastResponseId = lastResponseId;
    }

    // ───────────────────────────────────────────────
    // Gestión de entradas de encuestas y respuestas
    // ───────────────────────────────────────────────

    /** Añade una nueva entrada para una encuesta en el repositorio.
     *
     * @param surveyId ID de la encuesta
     */
    public void addSurveyEntry(String surveyId) {
        responses.putIfAbsent(surveyId, new HashMap<>());
        saveResponsesToJson();
    }

    /** Elimina la entrada de una encuesta y todas sus respuestas.
     *
     * @param surveyId ID de la encuesta
     */
    public void deleteSurveyEntry(String surveyId) {
        responses.remove(surveyId);
        saveResponsesToJson();
    }

    /** Comprueba si existe una entrada para una encuesta.
     *
     * @param surveyId ID de la encuesta
     * @return true si existe la entrada, false en caso contrario
     */
    public boolean existsSurveyEntry(String surveyId) {
        return responses.containsKey(surveyId);
    }

    // ───────────────────────────────────────────────
    // Gestión de respuestas
    // ───────────────────────────────────────────────

    /** Añade una nueva respuesta a una encuesta.
     *
     * @param surveyId ID de la encuesta
     * @param response respuesta a añadir
     */
    public void addResponse(String surveyId, Response response) {
        addSurveyEntry(surveyId);
        responses.get(surveyId).putIfAbsent(response.getRESPONSE_ID(), response);
        setLastResponseId(response.getRESPONSE_ID());
        saveResponsesToJson();
    }

    /** Obtiene una respuesta específica de una encuesta.
     * 
     * @param surveyId ID de la encuesta
     * @param responseId ID de la respuesta
     * @return respuesta específica
     */
    public Response getResponse(String surveyId, String responseId) {
        return responses.get(surveyId).get(responseId);
    }

    /** Obtiene todas las respuestas de una encuesta.
     *
     * @param surveyId ID de la encuesta
     * @return lista de respuestas
     */
    public List<Response> getAllResponses(String surveyId) {
        return new ArrayList<>(responses.get(surveyId).values());
    }

    /** Elimina una respuesta específica de una encuesta.
     *
     * @param surveyId ID de la encuesta
     * @param responseId ID de la respuesta
     */
    public void deleteResponse(String surveyId, String responseId) {
        responses.get(surveyId).remove(responseId);
        saveResponsesToJson();
    }

    /** Actualiza una respuesta existente de una encuesta.
     *
     * @param surveyId ID de la encuesta
     * @param response respuesta actualizada
     */
    public void updateResponse(String surveyId, Response response) {
        responses.get(surveyId).put(response.getRESPONSE_ID(), response);
        saveResponsesToJson();
    }

    /** Comprueba si existe una respuesta en una encuesta.
     *
     * @param surveyId ID de la encuesta
     * @param responseId ID de la respuesta
     * @return true si existe, false en caso contrario
     */
    public boolean existsResponse(String surveyId, String responseId) {
        return responses.containsKey(surveyId) && responses.get(surveyId).containsKey(responseId);
    }

    /** Obtiene todas las respuestas de una encuesta.
     *
     * @param surveyId id de la encuesta
     * @return lista de respuestas
     */
    public List<Response> getResponsesBySurveyId(String surveyId) {
        if (responses.containsKey(surveyId)) {
            return new ArrayList<>(responses.get(surveyId).values());
        }
        return new ArrayList<>();
    }

    /** Actualiza una respuesta específica de una encuesta.
        *
        * @param surveyId ID de la encuesta
        * @param responseId ID de la respuesta
        * @param answerIndex índice de la respuesta a actualizar
        * @param answer nueva respuesta
        */
    public void updateAnswer(String surveyId, String responseId, int answerIndex, Answer answer) {
        responses.get(surveyId).get(responseId).updateAnswer(answerIndex, answer);
        saveResponsesToJson();
    }

    /** Obtiene una respuesta específica de una encuesta.
     *
     * @param surveyId ID de la encuesta
     * @param responseId ID de la respuesta
     * @param answerIndex índice de la respuesta
     * @return respuesta específica
     */
    public Answer getAnswer(String surveyId, String responseId, int answerIndex) {
        return responses.get(surveyId).get(responseId).getAnswer(answerIndex);
    }

    /** Obtiene todas las respuestas de una respuesta específica de una encuesta.
     *
     * @param surveyId ID de la encuesta
     * @param responseId ID de la respuesta
     * @return lista de respuestas
     */
    public List<Answer> getAllAnswers(String surveyId, String responseId) {
        return Arrays.asList(responses.get(surveyId).get(responseId).getANSWERS());
    }


}
