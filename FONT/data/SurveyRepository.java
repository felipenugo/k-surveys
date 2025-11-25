package data;

import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import domain.model.Survey;
import domain.model.Question;
import domain.exception.SurveyException;

/**
 * Repositorio encargado de almacenar y gestionar en memoria todas las encuestas del sistema.
 * Mantiene una estructura &lt;surveyId, Survey&gt; donde cada Survey contiene sus preguntas.
 * No realiza validaciones de negocio; actúa como capa de persistencia en memoria.
 */
public class SurveyRepository {
    /**
     * Estructura de almacenamiento en memoria: &lt;surveyId, Survey&gt;.
     */
    private final Map<String, Survey> surveys; // <surveyId, Survey>, Survey contiene sus preguntas

    /**
     * String que almacena la dirección del fichero donde se guardan los usuarios.
     */
    private final String FILE_PATH;
    /**
     * Tipo de mapa que se utiliza para cargar y guardar los usuarios.
     */
    private final Type mapType;
    private final Gson gson;

    /**
     * Contador interno para generar IDs autoincrementales.
     */
    private int nextSurveyId;

    private Map<String, Survey> loadSurveysFromJson() {
        File file = new File(FILE_PATH);
        if (!file.exists() || file.length() == 0)
            return new HashMap<>();
        try (Reader reader = new FileReader(file)) {
            return gson.fromJson(reader, mapType);
        } catch (Exception e) {
            System.err.println("Error al cargar las encuestas desde el fichero: " + e.getMessage());
            System.exit(1);
            return null;
        }
    }

    /**
     * Crea un nuevo repositorio de encuestas en memoria.
     */
    public SurveyRepository() {
        this.FILE_PATH = "resources/db/surveys.json";
        this.mapType = new TypeToken<Map<String, Survey>>() {
        }.getType();
        // json con formato y adaptador para poder usar LocalDateTime que no está soportado por defecto
        this.gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).setPrettyPrinting().create();
        this.surveys = loadSurveysFromJson();
        this.nextSurveyId = 0;
    }

    private void saveSurveysToJson() {
        // try-with-resources -> forzar escritura inmediata (writer.flush()) y cerrar el canal de escriture (writer.close())
        // crea el fichero si no existe
        /*
        try anidado
        try{
        File file = new File(FILE_PATH);
        file.getParentFile().mkdirs(); // crea el directorio padre si no existe
         */
        try (Writer writer = new FileWriter(FILE_PATH)) {
            gson.toJson(surveys, writer);
        } catch (Exception e) {
            System.err.println("Error al guardar los usuarios en el fichero: " + e.getMessage());
            System.exit(1);
        }
    }

    // ───────────────────────────────────────────────
    // Gestión general de encuestas
    // ───────────────────────────────────────────────

    /**
     * Añade una nueva encuesta al repositorio.
     *
     * @param survey encuesta que se desea almacenar
     */
    public void addSurvey(Survey survey) {
        surveys.put(survey.getSURVEY_ID(), survey);
        saveSurveysToJson();
    }

    /**
     * Elimina una encuesta del repositorio.
     *
     * @param surveyId identificador de la encuesta a eliminar
     */
    public void deleteSurvey(String surveyId) {
        surveys.remove(surveyId);
        saveSurveysToJson();
    }

    /**
     * Recupera una encuesta por su ID.
     *
     * @param surveyId identificador de la encuesta
     * @return encuesta correspondiente al ID, o null si no existe
     */
    public Survey getSurvey(String surveyId) {
        return surveys.get(surveyId);
    }

    /**
     * Recupera todas las encuestas almacenadas.
     *
     * @return lista de todas las encuestas
     */
    public List<Survey> getAllSurveys() {
        return new ArrayList<>(surveys.values());
    }

    /**
     * Recupera todos los IDs de las encuestas almacenadas.
     *
     * @return lista de IDs de todas las encuestas
     */
    public List<String> getAllSurveysId() {
        return new ArrayList<>(surveys.keySet());
    }

    /**
     * Actualiza una encuesta existente.
     *
     * @param surveyId      ID de la encuesta a actualizar
     * @param updatedSurvey objeto Survey con los datos actualizados
     */
    public void updateSurvey(String surveyId, Survey updatedSurvey) {
        surveys.put(surveyId, updatedSurvey);
        saveSurveysToJson();

    }

    /**
     * Verifica si una encuesta existe en el repositorio.
     *
     * @param surveyId ID de la encuesta a verificar
     * @return true si la encuesta existe, false en caso contrario
     */
    public boolean existsSurvey(String surveyId) {
        return surveys.containsKey(surveyId);
    }

    /**
     * Devuelve todas las encuestas creadas por un usuario específico.
     *
     * @param username nombre de usuario del creador
     * @return lista de encuestas creadas por el usuario
     */
    public List<Survey> getSurveysByUsername(String username) {
        List<Survey> userSurveys = new ArrayList<>();
        for (Survey survey : surveys.values()) {
            if (survey.getCREATOR_USERNAME().equals(username)) {
                userSurveys.add(survey);
            }
        }
        return userSurveys;
    }

    /**
     * Genera y devuelve el siguiente ID de encuesta disponible.
     *
     * @return nuevo ID de encuesta como String
     * @throws SurveyException si se ha alcanzado el límite máximo de encuestas
     */

    public String generateNextSurveyId() {
        // Verificar si hemos alcanzado el límite máximo
        if (nextSurveyId >= Integer.MAX_VALUE) {
            throw new SurveyException("Se ha alcanzado el límite máximo de encuestas. No se pueden crear más.");
        }

        String surveyId = String.valueOf(nextSurveyId);
        nextSurveyId++; // Incrementar para la próxima encuesta
        return surveyId;
    }

    /**
     * Obtiene el valor numérico del siguiente ID de encuesta.
     *
     * @return valor numérico del siguiente ID de encuesta
     */
    public int getNextSurveyIdValue() {
        return nextSurveyId;
    }

    /**
     * Comprueba si es posible crear más encuestas sin exceder el límite.
     *
     * @return true si se pueden crear más encuestas, false si se ha alcanzado el límite
     */
    public boolean canCreateMoreSurveys() {
        return nextSurveyId < Integer.MAX_VALUE;
    }
    // ───────────────────────────────────────────────
    // Gestión de preguntas dentro de una encuesta
    // ───────────────────────────────────────────────

    /**
     * Añade una pregunta a una encuesta específica.
     *
     * @param surveyId ID de la encuesta
     * @param question pregunta a añadir
     */
    public void addQuestion(String surveyId, Question question) {
        surveys.get(surveyId).addQuestion(question);
        saveSurveysToJson();
    }

    /**
     * Elimina una pregunta de una encuesta específica.
     *
     * @param surveyId      ID de la encuesta
     * @param questionIndex índice de la pregunta a eliminar
     */
    public void deleteQuestion(String surveyId, int questionIndex) {
        surveys.get(surveyId).removeQuestion(questionIndex);
        saveSurveysToJson();
    }

    /**
     * Actualiza una pregunta en una encuesta específica.
     *
     * @param surveyId      ID de la encuesta
     * @param questionIndex índice de la pregunta a actualizar
     * @param question      nueva pregunta para reemplazar la existente
     */
    public void updateQuestion(String surveyId, int questionIndex, Question question) {
        surveys.get(surveyId).updateQuestion(questionIndex, question);
        saveSurveysToJson();
    }

    /**
     * Obtiene una pregunta específica de una encuesta.
     *
     * @param surveyId      ID de la encuesta
     * @param questionIndex índice de la pregunta
     * @return la pregunta solicitada
     */
    public Question getQuestion(String surveyId, int questionIndex) {
        return surveys.get(surveyId).getQuestion(questionIndex);
    }

    /**
     * Obtiene todas las preguntas de una encuesta específica.
     *
     * @param surveyId ID de la encuesta
     * @return lista de preguntas
     */
    public List<Question> getAllQuestions(String surveyId) {
        return surveys.get(surveyId).getQuestions();
    }

    /**
     * Obtiene el número de preguntas en una encuesta específica.
     *
     * @param surveyId ID de la encuesta
     * @return número de preguntas
     */
    public int getNumQuestions(String surveyId) {
        return surveys.get(surveyId).getSize();
    }

    /**
     * Intercambia la posición de dos preguntas en una encuesta específica.
     *
     * @param surveyId         ID de la encuesta
     * @param oldQuestionIndex índice de la primera pregunta
     * @param newQuestionIndex índice de la segunda pregunta
     */
    public void swapQuestions(String surveyId, int oldQuestionIndex, int newQuestionIndex) {
        surveys.get(surveyId).reorderQuestion(oldQuestionIndex, newQuestionIndex);
        saveSurveysToJson();
    }

    /**
     * Elimina todas las preguntas de una encuesta específica.
     *
     * @param surveyId ID de la encuesta
     */
    public void deleteAllQuestions(String surveyId) {
        surveys.get(surveyId).clearQuestions();
        saveSurveysToJson();
    }
}
