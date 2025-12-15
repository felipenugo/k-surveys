package domain.model;

import domain.model.enums.TypeQuestion;

/**
 * Representa una respuesta numérica asociada a una pregunta de tipo NUMERICAL.
 *
 * <p>Extiende a {@link Answer} y mantiene un valor numérico de tipo {@code Double}.
 * La respuesta puede quedar inicialmente vacía, en cuyo caso se considera no respondida.</p>
 *
 * <p>La clase gestiona internamente el estado de respuesta mediante el atributo
 * {@code isAnswered}, que se actualiza automáticamente cuando se asigna un valor válido
 * o cuando la respuesta se limpia.</p>
 */
public class NumericalAnswer extends Answer {
    /** Valor numérico introducido por el usuario. Puede ser null si no ha sido respondida. */
    private Double answerNum;

    /**
     * Crea una nueva respuesta numérica asociada a una pregunta concreta.
     *
     * @param questionIndex índice de la pregunta correspondiente dentro de la encuesta
     * @param responseId identificador de la respuesta completa a la encuesta
     */
    public NumericalAnswer(int questionIndex, String responseId) {
        super(questionIndex, responseId, TypeQuestion.NUMERICAL);
        this.answerNum = null;
    }

    /**
     * Devuelve el valor numérico introducido por el usuario.
     *
     * @return el valor numérico (puede ser null si no se ha respondido)
     */
    public Double getAnswerNum() {
        return answerNum;
    }
    /**
     * Asigna un valor numérico como respuesta.
     *
     * <p>Si el valor es no nulo, la respuesta se marca como respondida.</p>
     *
     * @param answerNum el valor numérico a asignar (puede ser null para indicar no respondida)
     */
    public void setAnswerNum(Double answerNum) {
        this.answerNum = answerNum;
        super.setIsAnswered(answerNum != null);
    }

    /**
     * Limpia la respuesta, dejándola sin valor y marcándola como no respondida.
     */
    @Override
    public void clearAnswer() {
        this.answerNum = null;
        super.setIsAnswered(false);
    }

    /**
     * Devuelve una representación en cadena de la respuesta.
     *
     * @return representación en cadena del valor numérico o indicación de no respuesta
     */
    @Override
    public String toString() {
        return answerNum != null ? String.valueOf(answerNum) : "(sin respuesta)";
    }
}