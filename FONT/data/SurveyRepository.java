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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import data.adapter.LocalDateTimeAdapter;
import data.adapter.QuestionAdapter;
import domain.model.Survey;
import domain.model.Question;
import domain.model.MultipleChoiceQuestion;
import domain.model.OptionQuestion;
import domain.exception.SurveyException;

/**
 * Repositorio encargado de almacenar y gestionar en memoria todas las encuestas del sistema.
 * Mantiene una estructura &lt;surveyId, Survey&gt; donde cada Survey contiene sus preguntas.
 * No realiza validaciones de negocio; actúa como capa de persistencia en memoria.
 */
public class SurveyRepository {
    /**
     * Estructura de almacenamiento en memoria: <surveyId, Survey>.
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

    /**
     * Carga las encuestas desde el JSON. Soporta dos formatos:
     * - Formato nuevo: { "nextSurveyId": <int>, "surveys": { ... } }
     * - Formato antiguo: { "<id>": { ... }, ... }
     */
    private Map<String, Survey> loadSurveysFromJson() {
        File file = new File(FILE_PATH);
        if (!file.exists() || file.length() == 0)
            return new HashMap<>();

        try (Reader reader = new FileReader(file)) {
            // Parsear el JSON para comprobar si contiene nextSurveyId
            JsonElement root = JsonParser.parseReader(reader);
            if (root == null || root.isJsonNull()) return new HashMap<>();

            JsonObject rootObj = root.getAsJsonObject();

            if (rootObj.has("surveys")) {
                // Formato nuevo
                JsonElement nextEl = rootObj.get("nextSurveyId");
                if (nextEl != null && !nextEl.isJsonNull()) {
                    try {
                        this.nextSurveyId = nextEl.getAsInt();
                    } catch (Exception e) {
                        // fallback en caso de formato inesperado
                        this.nextSurveyId = calculateNextSurveyIdFromMap(rootObj.getAsJsonObject("surveys"));
                    }
                } else {
                    this.nextSurveyId = calculateNextSurveyIdFromMap(rootObj.getAsJsonObject("surveys"));
                }

                // Deserializar mapa de encuestas
                Map<String, Survey> loaded = gson.fromJson(rootObj.getAsJsonObject("surveys"), mapType);

                return loaded;
            } else {
                // Formato antiguo: el root es directamente el mapa de encuestas
                Map<String, Survey> loaded = gson.fromJson(rootObj, mapType);
                // Calcular nextSurveyId a partir del contenido para ser compatible
                this.nextSurveyId = calculateNextSurveyId(loaded);
                return loaded;
            }

        } catch (Exception e) {
            throw new RuntimeException(
                    "Error al cargar las encuestas desde el fichero: " + FILE_PATH, e);
        }
    }

    // Helper usado solo durante carga en caso de formato manual
    private int calculateNextSurveyId(Map<String, Survey> map) {
        int maxId = -1;
        for (String k : map.keySet()) {
            try {
                int id = Integer.parseInt(k);
                if (id > maxId) maxId = id;
            } catch (NumberFormatException ignored) {}
        }
        return maxId + 1;
    }

    // Helper para calcular next id from JsonObject surveys when loading new format
    private int calculateNextSurveyIdFromMap(JsonObject surveysObj) {
        int maxId = -1;
        for (Map.Entry<String, JsonElement> e : surveysObj.entrySet()) {
            String key = e.getKey();
            try {
                int id = Integer.parseInt(key);
                if (id > maxId) maxId = id;
            } catch (NumberFormatException ignored) {}
        }
        return maxId + 1;
    }

    private void saveSurveysToJson() {
        try (Writer writer = new FileWriter(FILE_PATH)) {
            // Crear objeto raíz con nextSurveyId y el mapa de encuestas
            JsonObject root = new JsonObject();
            root.addProperty("nextSurveyId", this.nextSurveyId);

            // Construir surveys a partir del modelo (no depender de toJsonTree) para garantizar formato deseado
            JsonObject surveysObj = new JsonObject();
            for (Map.Entry<String, Survey> entry : surveys.entrySet()) {
                String sid = entry.getKey();
                Survey s = entry.getValue();

                JsonObject sObj = new JsonObject();
                sObj.addProperty("SURVEY_ID", s.getSURVEY_ID());
                sObj.addProperty("title", s.getTitle());
                sObj.addProperty("description", s.getDescription());
                sObj.addProperty("CREATOR_USERNAME", s.getCREATOR_USERNAME());
                // LocalDateTime serializado por Gson para mantener formato consistente
                sObj.add("CREATED_AT", gson.toJsonTree(s.getCREATED_AT()));
                if (s.getPUBLISHED_AT() != null) sObj.add("PUBLISHED_AT", gson.toJsonTree(s.getPUBLISHED_AT()));
                sObj.addProperty("surveyStatus", s.getSurveyStatus().name());
                sObj.addProperty("avgRating", s.getAvgRating());
                sObj.addProperty("views", s.getViews());

                // Preguntas
                JsonArray qArr = new JsonArray();
                List<Question> questions = s.getQuestions();
                if (questions != null) {
                    for (Question q : questions) {
                        JsonObject qObj = new JsonObject();
                        qObj.addProperty("questionIndex", q.getQuestionIndex());
                        qObj.addProperty("SURVEY_ID", q.getSURVEY_ID());
                        qObj.addProperty("typeQuestion", q.getTypeQuestion() != null ? q.getTypeQuestion().name() : "TEXTUAL");
                        qObj.addProperty("questionText", q.getQuestionText());
                        qObj.addProperty("isRequired", q.isRequired());

                        if (q instanceof MultipleChoiceQuestion) {
                            MultipleChoiceQuestion mcq = (MultipleChoiceQuestion) q;
                            // Forzar minChoices / maxChoices
                            qObj.addProperty("minChoices", mcq.getMinSelections());
                            qObj.addProperty("maxChoices", mcq.getMaxSelections());
                            // Forzar options como array de strings
                            JsonArray opts = new JsonArray();
                            if (mcq.getOptions() != null) {
                                for (OptionQuestion opt : mcq.getOptions()) {
                                    String text = opt.getOptionText();
                                    if (text == null) text = "";
                                    opts.add(new JsonPrimitive(text));
                                }
                            }
                            qObj.add("options", opts);
                        }

                        qArr.add(qObj);
                    }
                }

                sObj.add("questions", qArr);
                surveysObj.add(sid, sObj);
            }

            root.add("surveys", surveysObj);
            gson.toJson(root, writer);
        }  catch (Exception e) {
            throw new RuntimeException("Error al guardar las encuestas en el fichero: " + FILE_PATH, e);
        }
    }

    // ───────────────────────────────────────────────
    // Gestión general de encuestas
    // ───────────────────────────────────────────────

    public SurveyRepository() {
        this("DATA/db/surveys.json");
    }

    /**
     * Crea un nuevo repositorio de encuestas con un path personalizado.
     * @param filePath ruta al archivo JSON de encuestas
     */
    public SurveyRepository(String filePath) {
        this.FILE_PATH = filePath;
        this.mapType = new TypeToken<Map<String, Survey>>() {
        }.getType();
        // json con formato y adaptador para poder usar LocalDateTime que no está soportado por defecto
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Question.class, new QuestionAdapter())
                .setPrettyPrinting().create();

        Map<String, Survey> loaded = loadSurveysFromJson();
        this.surveys = (loaded != null) ? loaded : new HashMap<>();

        // Si nextSurveyId no fue inicializado durante la carga (archivo vacío), calcularlo
        if (this.nextSurveyId == 0 && !this.surveys.isEmpty()) {
            this.nextSurveyId = calculateNextSurveyId(this.surveys);
        }

        // Garantía: si sigue vacío (no encuestas), iniciar en 0
        if (this.nextSurveyId < 0) this.nextSurveyId = 0;
    }

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
        // Guardar el cambio en el fichero para persistir el contador inmediatamente
        saveSurveysToJson();
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
