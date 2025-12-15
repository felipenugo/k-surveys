package domain.model;

import domain.model.enums.SurveyStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa una encuesta dentro del sistema.
 * 
 * Cada encuesta tiene un identificador único, un creador, un estado
 * (borrador, publicada, cerrada, etc.) y una lista de preguntas asociadas.
 * 
 * Esta clase actúa como entidad principal en el dominio de las encuestas
 * y permite gestionar sus preguntas, estado y metadatos básicos.
 */

public class Survey {
    // Attributes
    /** Identificador único de la encuesta (asignado por la base de datos). */
    private final String SURVEY_ID;
    /** Título descriptivo de la encuesta. */
    private String title;
    /** Descripción detallada de la encuesta. */
    private String description;
    /** Nombre de usuario del creador de la encuesta. */
    private final String CREATOR_USERNAME; // identifier for survey creator
    /** Fecha y hora de creación de la encuesta. */
    private final LocalDateTime CREATED_AT;
    /** Fecha y hora de publicación de la encuesta. */
    private LocalDateTime PUBLISHED_AT;
    /** Estado actual de la encuesta (borrador etc.). */
    private SurveyStatus surveyStatus;
    /** Calificación promedio de la encuesta basada en las respuestas recibidas. */
    private double avgRating;
    /** Suma total de las valoraciones recibidas. */
    private double totalRating = 0.0;
    /** Número de valoraciones recibidas. */
    private int ratingCount = 0;
    /** Número de veces que la encuesta ha sido vista. */
    private int views;
    /** Lista de preguntas asociadas a la encuesta. */
    private List<Question> questions;
    /*
    extra features to consider later (search filters, sorting, etc.):
    private responseCount;
     */

    // ───────────────────────────────────────────────
    // Constructores
    // ───────────────────────────────────────────────

    /**
     * Crea una nueva encuesta con un identificador ya asignado (por ejemplo, desde la base de datos).
     *
     * @param SURVEY_ID        identificador único de la encuesta
     * @param title            título de la encuesta
     * @param description      descripción de la encuesta
     * @param CREATOR_USERNAME nombre de usuario del creador
     */
    public Survey(String SURVEY_ID, String title, String description, String CREATOR_USERNAME) {
        this.SURVEY_ID = SURVEY_ID;
        this.title = title;
        this.description = description;
        this.CREATOR_USERNAME = CREATOR_USERNAME;
        this.CREATED_AT = LocalDateTime.now();
        this.PUBLISHED_AT = null; // to be set when published
        this.surveyStatus = SurveyStatus.DRAFT;
        this.avgRating = 0.0;
        this.views = 0;
        this.questions = new ArrayList<>();
    }

     /**
     * Crea una nueva encuesta sin identificador (para encuestas recién creadas que
     * aún no se han guardado en la base de datos).
     *
     * @param title            título de la encuesta
     * @param description      descripción de la encuesta
     * @param CREATOR_USERNAME nombre de usuario del creador
     */
    public Survey(String title, String description, String CREATOR_USERNAME) {
        this.SURVEY_ID = null; // to be set when stored in database
        this.title = title;
        this.description = description;
        this.CREATOR_USERNAME = CREATOR_USERNAME;
        this.CREATED_AT = LocalDateTime.now();
        this.PUBLISHED_AT = null; // to be set when published
        this.surveyStatus = SurveyStatus.DRAFT;
        this.avgRating = 0.0;
        this.views = 0;
        this.questions = new ArrayList<>();
    }

    // ───────────────────────────────────────────────
    // Getters
        // ───────────────────────────────────────────────
    /**
     * Devuelve el identificador único de la encuesta.
     * 
     * @return identificador único de la encuesta
     */
    public String getSURVEY_ID() {
        return SURVEY_ID;
    }

    /**
     * Devuelve el título de la encuesta.
     * 
     * @return título de la encuesta
     */
    public String getTitle() {
        return title;
    }

    /**
     * Devuelve la descripción de la encuesta.
     * 
     * @return descripción de la encuesta
     */
    public String getDescription() {
        return description;
    }

    /**
     * Devuelve el nombre de usuario del creador de la encuesta.
     * 
     * @return nombre de usuario del creador
     */
    public String getCREATOR_USERNAME() {
        return CREATOR_USERNAME;
    }

    /**
     * Devuelve la fecha y hora de creación de la encuesta.
     * 
     * @return fecha y hora de creación
     */
    public LocalDateTime getCREATED_AT() {
        return CREATED_AT;
    }

    /**
     * Devuelve la fecha y hora de publicación de la encuesta.
     * 
     * @return fecha y hora de publicación
     */
    public LocalDateTime getPUBLISHED_AT() {
        return PUBLISHED_AT;
    }

    /**
     * Devuelve el estado de la encuesta.
     * 
     * @return estado de la encuesta
     */
    public SurveyStatus getSurveyStatus() {
        return surveyStatus;
    }

    /**
     * Devuelve la calificación promedio de la encuesta.
     * 
     * @return calificación promedio
     */
    public double getAvgRating() {
        return avgRating;
    }

    /**
     * Devuelve la suma total de las valoraciones recibidas.
     * 
     * @return suma total de valoraciones
     */
    public double getTotalRating() {
        return totalRating;
    }

    /**
     * Devuelve el número de valoraciones recibidas.
     * 
     * @return número de valoraciones
     */
    public int getRatingCount() {
        return ratingCount;
    }
    
    /**
     * Calcula y devuelve la calificación promedio basada en las valoraciones recibidas.
     * 
     * @return calificación promedio calculada
     */
    public double getAverageRating() {
        if (ratingCount == 0) return 0.0;
        return totalRating / ratingCount;
    }

    /**
     * Añade una nueva valoración a la encuesta y actualiza la calificación promedio.
     * 
     * @param rating nueva valoración a añadir
     */
    public void addRating(double rating) {
        this.totalRating += rating;
        this.ratingCount += 1;
        recomputeAvgRating();
    }

    /**
     * Recalcula la calificación promedio de la encuesta.
     */
    public void recomputeAvgRating() {
        if (ratingCount == 0) {
            avgRating = 0.0;
        } else {
            double raw = totalRating / ratingCount;
            avgRating = Math.round(raw * 10.0) / 10.0; // ← 1 decimal
        }
    }

    /**
     * Devuelve el número de vistas de la encuesta.
     * 
     * @return número de vistas
     */
    public int getViews() { return views; }

    /**
     * Devuelve la lista de preguntas asociadas a la encuesta.
     *
     * @return lista de preguntas
     */
    public List<Question> getQuestions() {
        return questions;
    }


    // ───────────────────────────────────────────────
    // Setters
    // ───────────────────────────────────────────────
    /**
     * Establece el título de la encuesta.
     *
     * @param title nuevo título de la encuesta
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Establece la descripción de la encuesta.
     *
     * @param description nueva descripción de la encuesta
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**     * Establece la fecha y hora de publicación de la encuesta.
     */
    public void setPUBLISHED_AT() {
        this.PUBLISHED_AT = LocalDateTime.now();
    }

    /**
     * Establece el estado de la encuesta.
     *
     * @param surveyStatus nuevo estado de la encuesta
     */
    public void setSurveyStatus(SurveyStatus surveyStatus) {
        this.surveyStatus = surveyStatus;
    }

    /**
     * Establece la calificación promedio de la encuesta.
     *
     * @param avgRating nueva calificación promedio
     */
    public void setAvgRating(double avgRating) {
        this.avgRating = avgRating;
    }

    /**
     * Establece el número de vistas de la encuesta.
     *
     * @param views nuevo número de vistas
     */
    public void setViews (int views) { this.views = views;}

    public void setTotalRating(double totalRating) {
        this.totalRating = totalRating;
        recomputeAvgRating();
    }

    public void setRatingCount(int ratingCount) {
        this.ratingCount = ratingCount;
        recomputeAvgRating();
    }

    // List<Question> methods, correct usage must be ensured by the caller
    // Multiple Choice Questions methods will be implemented in the future
    
    /**
     * Devuelve el número de preguntas en la encuesta.
     *
     * @return número de preguntas
     */
    public int getSize() {
        return questions.size();
    }

    /**
     * Añade una nueva pregunta a la encuesta.
     *
     * @param question pregunta a añadir
     */
    public void addQuestion(Question question) {
        questions.add(question);
    }

    /**
     * Elimina una pregunta de la encuesta por su índice.
     *
     * @param index índice de la pregunta a eliminar
     */
    public void removeQuestion(int index) {
        questions.remove(index);
    }

    /**
     * Reordena una pregunta en la encuesta.
     *
     * @param oldIndex índice original de la pregunta
     * @param newIndex nuevo índice de la pregunta
     */
    public void reorderQuestion(int oldIndex, int newIndex) {
        Question question = questions.get(oldIndex);
        questions.remove(oldIndex);
        questions.add(newIndex, question);
    }

    /**
     * Actualiza una pregunta en la encuesta.
     *
     * @param index índice de la pregunta a actualizar
     * @param question nueva pregunta
     */
    public void updateQuestion(int index, Question question) {
        questions.set(index, question);
    }

    /**
     * Devuelve una pregunta de la encuesta por su índice.
     *
     * @param index índice de la pregunta
     * @return pregunta en el índice especificado
     */
    public Question getQuestion(int index) {
        return questions.get(index);
    }

    /**
     * Elimina todas las preguntas de la encuesta.
     */
    public void clearQuestions() {
        questions.clear();
    }


}

