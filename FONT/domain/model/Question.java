package domain.model;

import domain.model.enums.TypeQuestion;

/**
 * Representa una pregunta individual dentro de una encuesta.
 * 
 * Cada pregunta pertenece a una encuesta identificada por su {@code SURVEY_ID}
 * y puede ser de distintos tipos definidos en {@link TypeQuestion} 
 * (por ejemplo, texto libre o elección múltiple).
 * 
 * Esta clase permite definir el texto de la pregunta, su tipo y su posición
 * dentro de la encuesta correspondiente.
 */

public class Question {
    // Attributes
    /** Índice que identifica la posición de la pregunta dentro de una encuesta. */
    private int questionIndex; // identifier with surveyId
    /** Identificador de la encuesta a la que pertenece esta pregunta. */
    private final String SURVEY_ID; // identifier to the survey to which it belongs
     /** Tipo de pregunta. */
    private TypeQuestion typeQuestion; // can be MULTIPLE_CHOICE or TEXTUAL
    /** Texto o enunciado de la pregunta. */
    private String questionText;
    /** Indica si la pregunta es obligatoria. */
    private boolean isRequired;

    // ───────────────────────────────────────────────
    // Constructor
    // ───────────────────────────────────────────────

    /**
     * Crea una nueva pregunta asociada a una encuesta.
     * 
     * Por defecto, el tipo de pregunta se establece como {@link TypeQuestion#TEXTUAL}.
     *
     * @param questionIndex índice o posición de la pregunta dentro de la encuesta
     * @param SURVEY_ID     identificador de la encuesta a la que pertenece
     */
    public Question(int questionIndex, String SURVEY_ID) {
        this.questionIndex = questionIndex;
        this.SURVEY_ID = SURVEY_ID;
        this.typeQuestion = TypeQuestion.TEXTUAL; // textual by default, can be changed later
        this.questionText = "";
        this.isRequired = true;

    }

    // ───────────────────────────────────────────────
    // Getters
    // ───────────────────────────────────────────────

    /**
     * Devuelve el índice o posición de la pregunta dentro de la encuesta.
     *
     * @return índice de la pregunta
     */
    public int getQuestionIndex() {
        return questionIndex;
    }

    /**
     * Devuelve el identificador de la encuesta a la que pertenece esta pregunta.
     *
     * @return identificador de la encuesta
     */
    public String getSURVEY_ID() {
        return SURVEY_ID;
    }

    /**
     * Devuelve el tipo de pregunta.
     *
     * @return tipo de pregunta
     */
    public TypeQuestion getTypeQuestion() {
        return typeQuestion;
    }

    /**
     * Devuelve el texto o enunciado de la pregunta.
     *
     * @return texto de la pregunta
     */
    public String getQuestionText() {
        return questionText;
    }

    /**
     * Indica si la pregunta es obligatoria.
     *
     * @return true si la pregunta es obligatoria, false en caso contrario
     */
    public boolean isRequired() { return isRequired;}

    // ───────────────────────────────────────────────
    // Setters
    // ───────────────────────────────────────────────

    /**
     * Establece un nuevo índice para la pregunta dentro de la encuesta.
     *
     * @param questionIndex nuevo índice de la pregunta
     */

    public void setQuestionIndex(int questionIndex) {
        this.questionIndex = questionIndex;
    }

    /**
     * Establece un nuevo tipo para la pregunta.
     *
     * @param typeQuestion nuevo tipo de la pregunta
     */
    public void setTypeQuestion(TypeQuestion typeQuestion) {
        this.typeQuestion = typeQuestion;
    }

    /**
     * Establece un nuevo texto o enunciado para la pregunta.
     *
     * @param questionText nuevo texto de la pregunta
     */
    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    /**
     * Establece si la pregunta es obligatoria.
     *
     * @param required nuevo valor para indicar si la pregunta es obligatoria
     */
    public void setRequired(boolean required) { this.isRequired = required;}

    //Clonar
    public Question copy() {
        Question copy = new Question(this.questionIndex, this.SURVEY_ID);
        copy.setQuestionText(this.questionText);
        copy.setTypeQuestion(this.typeQuestion);
        copy.setRequired(this.isRequired);
        return copy;
    }
}
