package domain.model;
/**
 * Representa una opción dentro de una pregunta de tipo elección múltiple.
 * 
 * Cada opción pertenece a una pregunta concreta identificada por su índice
 * y a una encuesta específica a través de su {@code surveyId}.
 * 
 * Esta clase se utiliza para almacenar el texto de una opción y su
 * relación con la pregunta a la que pertenece.
 */
public class OptionQuestion {
    // Attributes
    /** Índice que identifica la posición de la pregunta dentro de la encuesta. */
    private int questionIndex;
    /** Identificador de la encuesta a la que pertenece esta opción. */
    private final String surveyId;
    /** Texto de la opción. */
    private String optionText;

    // ───────────────────────────────────────────────
    // Constructor
    // ───────────────────────────────────────────────

    /**
     * Crea una nueva opción asociada a una pregunta de una encuesta.
     * 
     * Por defecto, el texto de la opción se inicializa como una cadena vacía.
     *
     * @param questionIndex índice de la pregunta a la que pertenece la opción
     * @param surveyId      identificador de la encuesta
     */
    public OptionQuestion(int questionIndex, String surveyId) {
        this.questionIndex = questionIndex;
        this.surveyId = surveyId;
        this.optionText = "";
    }

    // ───────────────────────────────────────────────
    // Getters
    // ───────────────────────────────────────────────
    
    /**
     * Devuelve el índice de la pregunta a la que pertenece la opción.
     *
     * @return índice de la pregunta
     */
    public int getQuestionIndex() {
        return questionIndex;
    }

    /**
     * Devuelve el identificador de la encuesta a la que pertenece esta opción.
     *
     * @return identificador de la encuesta
     */
    public String getSurveyId() {
        return surveyId;
    }

    /**
     * Devuelve el texto de la opción.
     *
     * @return texto de la opción
     */
    public String getOptionText() {
        return optionText;
    }

    // ───────────────────────────────────────────────
    // Setters
    // ───────────────────────────────────────────────
    
    /**
     * Establece un nuevo índice para la pregunta a la que pertenece la opción.
     *
     * @param questionIndex nuevo índice de la pregunta
     */
    public void setQuestionIndex(int questionIndex) {
        this.questionIndex = questionIndex;
    }

    /**
     * Establece un nuevo texto para la opción.
     *
     * @param optionText nuevo texto de la opción
     */
    public void setOptionText(String optionText) {
        this.optionText = optionText;
    }
}
