package domain.model;

import domain.model.enums.TypeQuestion;

/**
 * Clase abstracta que representa una respuesta genérica a una pregunta de una encuesta.
 * 
 * Esta clase define los atributos y comportamientos comunes a todas las respuestas,
 * independientemente de su tipo (por ejemplo, textual o de elección múltiple).
 * 
 * Las subclases concretas, como {@link TextualAnswer} y {@link MultipleChoiceAnswer},
 * implementan la lógica específica correspondiente al tipo de respuesta.
 */
public abstract class Answer {
    // Attributes
    /** Índice que identifica la pregunta dentro de la encuesta. */
    private final int QUESTION_INDEX;
    /** Identificador único de la respuesta dentro de la encuesta. */
    private final String RESPONSE_ID;
    /** Tipo de la respuesta, correspondiente al tipo de pregunta. */
    private final TypeQuestion typeAnswer;
    /** Indica si la respuesta ha sido contestada o no. */
    private boolean isAnswered;

    // ───────────────────────────────────────────────
    // Constructor
    // ───────────────────────────────────────────────

    /**
     * Crea una nueva respuesta genérica asociada a una pregunta concreta.
     *
     * @param QUESTION_INDEX índice de la pregunta dentro de la encuesta
     * @param RESPONSE_ID    identificador único de la respuesta dentro de la encuesta
     * @param typeAnswer     tipo de la respuesta, correspondiente al tipo de pregunta
     */
    public Answer(int QUESTION_INDEX, String RESPONSE_ID, TypeQuestion typeAnswer) {
        this.QUESTION_INDEX = QUESTION_INDEX;
        this.RESPONSE_ID = RESPONSE_ID;
        this.typeAnswer = typeAnswer; // textual by default
        this.isAnswered = false;
    }

    // ───────────────────────────────────────────────
    // Getters
    // ───────────────────────────────────────────────

    /**
     * Devuelve el índice de la pregunta a la que pertenece esta respuesta.
     *
     * @return índice de la pregunta
     */
    public int getQUESTION_INDEX() {
        return QUESTION_INDEX;
    }

    /**
     * Devuelve el identificador único de la respuesta dentro de la encuesta.
     *
     * @return identificador único de la respuesta
     */
    public String getResponseId() {
        return RESPONSE_ID;
    }

    /**
     * Devuelve el tipo de la respuesta, correspondiente al tipo de pregunta.
     *
     * @return tipo de la respuesta
     */
    public TypeQuestion getTypeAnswer() {
        return typeAnswer;
    }

    /**
     * Indica si la respuesta ha sido contestada o no.
     *
     * @return true si la respuesta ha sido contestada, false en caso contrario
     */
    public boolean getIsAnswered() {
        return isAnswered;
    }

    // ───────────────────────────────────────────────
    // Setters
    // ───────────────────────────────────────────────  
    /**
     * Establece si la respuesta ha sido contestada o no.
     *
     * @param isAnswered true si la respuesta ha sido contestada, false en caso contrario
     */

    protected void setIsAnswered(boolean isAnswered) {
        this.isAnswered = isAnswered;
    }

    /**
     * Limpia la respuesta, restableciendo su estado a no contestada.
     */
    public abstract void clearAnswer();

}