package domain.model;

import domain.model.enums.TypeQuestion;

/**
 * Representa una respuesta de tipo textual dentro del sistema de encuestas.
 * 
 * Esta clase hereda de {@link Answer} y almacena el texto escrito por el usuario
 * como respuesta a una pregunta de tipo {@link TypeQuestion#TEXTUAL}.
 * 
 * Cada respuesta textual está asociada a una pregunta concreta mediante su índice
 * y a una respuesta global identificada por su {@code responseId}.
 */
public class TextualAnswer extends Answer {
    /** Texto introducido por el usuario como respuesta. */
    private String answerText;

    /**
     * Crea una nueva respuesta textual asociada a una pregunta concreta.
     * 
     * @param questionIndex índice de la pregunta a la que pertenece esta respuesta
     * @param responseId    identificador único de la respuesta dentro de la encuesta
     */
    public TextualAnswer(int questionIndex, String responseId) {
        super(questionIndex, responseId, TypeQuestion.TEXTUAL); // Llama al constructor del padre
        this.answerText = "";
    }

    // ───────────────────────────────────────────────
    // Getters
    // ───────────────────────────────────────────────

    /**
     * Devuelve el texto introducido como respuesta.
     * 
     * @return texto de la respuesta
     */
    public String getAnswerText() {
        return answerText;
    }

    // ───────────────────────────────────────────────
    // Setters
    // ───────────────────────────────────────────────

    /**
     * Actualiza el texto de la respuesta.
     * 
     * @param answerText nuevo texto de la respuesta
     */
    public void setAnswerText(String answerText) {
        this.answerText = answerText;
        super.setIsAnswered(true);
    }

    // ───────────────────────────────────────────────
    // Métodos sobreescritos
    // ───────────────────────────────────────────────

    /**
     * Limpia el contenido de la respuesta textual.
     * 
     * Este método establece el texto de la respuesta como una cadena vacía,
     * manteniendo la instancia pero eliminando su contenido.
     */
    @Override
    public void clearAnswer() {
        this.answerText = "";
        super.setIsAnswered(false);
    }

    /**
     * Devuelve una representación en cadena de la respuesta.
     *
     * @return representación en cadena del texto de la respuesta o indicación de no respuesta
     */
    @Override
    public String toString() {
        return answerText != null && !answerText.isEmpty() ? "\"" + answerText + "\"" : "(sin respuesta)";
    }
}
