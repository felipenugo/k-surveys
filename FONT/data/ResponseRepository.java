package data;

import java.util.*;

import domain.model.MultipleChoiceAnswer;
import domain.model.Response;
import domain.model.Answer;

/**
 * Repositorio encargado de almacenar en memoria todas las respuestas del sistema.
 * Utiliza una estructura Map&lt;surveyId, Map&lt;responseId, Response&gt;&gt; para indexación eficiente.
 * No realiza validaciones de negocio; actúa como capa de persistencia en memoria.
 */
public class ResponseRepository {
    /** Estructura &lt;surveyId, &lt;responseId, Response&gt;&gt; */
    private final Map<String, Map<String, Response>> responses; // <surveyId, <responseId, Response>>
    /** Último identificador asignado para respuestas. */
    private String lastResponseId;

    /**
     * Crea un repositorio vacío para almacenar respuestas.
     * El primer identificador asignado será "0".
     */
    public ResponseRepository() {
        responses = new HashMap<>();
        lastResponseId = "0";
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
    }

    /** Elimina la entrada de una encuesta y todas sus respuestas.
     *
     * @param surveyId ID de la encuesta
     */
    public void deleteSurveyEntry(String surveyId) {
        responses.remove(surveyId);
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
    }

    /** Actualiza una respuesta existente de una encuesta.
     *
     * @param surveyId ID de la encuesta
     * @param response respuesta actualizada
     */
    public void updateResponse(String surveyId, Response response) {
        responses.get(surveyId).put(response.getRESPONSE_ID(), response);
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
