package domain.model;

import domain.model.enums.ResponseStatus;
import domain.model.enums.TypeQuestion;
import org.w3c.dom.Text;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Representa una respuesta completa a una encuesta realizada por un usuario.
 * 
 * Cada objeto {@code Response} contiene la información del usuario que respondió,
 * el identificador de la encuesta a la que pertenece, el estado de la respuesta
 * (borrador o enviada) y un conjunto de objetos {@link Answer} que representan
 * las respuestas individuales a cada pregunta.
 * 
 * Esta clase no permite añadir ni eliminar respuestas, ya que el número de
 * preguntas de una encuesta es fijo en el momento de crear la respuesta.
 */
public class Response {
    // Attributes
    /** Identificador único de la respuesta (asignado desde la base de datos). */
    private final String RESPONSE_ID; // response identifier
    /** Identificador de la encuesta a la que pertenece la respuesta. */
    private final String SURVEY_ID; // identifies the survey which belongs
    /** Nombre de usuario del que responde. */
    private final String RESPONDER_USERNAME; // identifies the user that response
    /** Estado actual de la respuesta (borrador o enviada). */
    private ResponseStatus responseStatus;
    /** Fecha y hora en que se envió la respuesta (puede ser null si no se ha enviado). */
    private LocalDateTime SUBMITTED_AT;
    /** Conjunto de respuestas individuales, una por cada pregunta de la encuesta. */
    private final Answer[] ANSWERS;

    // Constructor with id checked from database
    // ───────────────────────────────────────────────
    // Constructores
    // ───────────────────────────────────────────────

    /**
     * Crea una nueva respuesta con un identificador ya asignado (por ejemplo, desde la base de datos).
     *
     * @param RESPONSE_ID        identificador único de la respuesta
     * @param SURVEY_ID          identificador de la encuesta asociada
     * @param RESPONDER_USERNAME nombre del usuario que responde
     * @param questions          lista de preguntas de la encuesta
     */
    public Response(String RESPONSE_ID, String SURVEY_ID, String RESPONDER_USERNAME, List<Question> questions) {
        this.RESPONSE_ID = RESPONSE_ID;
        this.SURVEY_ID = SURVEY_ID;
        this.RESPONDER_USERNAME = RESPONDER_USERNAME;
        this.responseStatus = ResponseStatus.DRAFT;
        SUBMITTED_AT = null;
        ANSWERS = new Answer[questions.size()];
        for (int i = 0; i < questions.size(); i++) {
            TypeQuestion answerType = questions.get(i).getTypeQuestion();
            if (answerType.equals(TypeQuestion.TEXTUAL)) {
                TextualAnswer answer = new TextualAnswer(i, RESPONSE_ID);
                ANSWERS[i] = answer;
            } else if (answerType.equals(TypeQuestion.MULTIPLE_CHOICE)) {
                MultipleChoiceAnswer answer = new MultipleChoiceAnswer(i, RESPONSE_ID, ((MultipleChoiceQuestion) questions.get(i)).getOptionsSize());
                ANSWERS[i] = answer;
            } else if (answerType.equals(TypeQuestion.NUMERICAL)) {
                NumericalAnswer answer = new NumericalAnswer(i, RESPONSE_ID);
                ANSWERS[i] = answer;
            } else {
                TextualAnswer answer = new TextualAnswer(i, RESPONSE_ID);
                ANSWERS[i] = answer;
            }
        }
    }

    // Constructor without id for new responses
    /**
     * Crea una nueva respuesta sin identificador (para respuestas recién creadas).
     *
     * @param SURVEY_ID          identificador de la encuesta asociada
     * @param RESPONDER_USERNAME nombre del usuario que responde
     * @param questions          lista de preguntas de la encuesta
     */
    public Response(String SURVEY_ID, String RESPONDER_USERNAME, List<Question> questions) {
        this.RESPONSE_ID = null;
        this.SURVEY_ID = SURVEY_ID;
        this.RESPONDER_USERNAME = RESPONDER_USERNAME;
        this.responseStatus = ResponseStatus.DRAFT;
        SUBMITTED_AT = null;
        ANSWERS = new Answer[questions.size()];
        for (int i = 0; i < questions.size(); i++) {
            TypeQuestion answerType = questions.get(i).getTypeQuestion();
            if (answerType.equals(TypeQuestion.TEXTUAL)) {
                TextualAnswer answer = new TextualAnswer(i, RESPONSE_ID);
                ANSWERS[i] = answer;
            } else if (answerType.equals(TypeQuestion.MULTIPLE_CHOICE)) {
                MultipleChoiceAnswer answer = new MultipleChoiceAnswer(i, RESPONSE_ID, ((MultipleChoiceQuestion) questions.get(i)).getOptionsSize());
                ANSWERS[i] = answer;
            } else if (answerType.equals(TypeQuestion.NUMERICAL)) {
                NumericalAnswer answer = new NumericalAnswer(i, RESPONSE_ID);
                ANSWERS[i] = answer;
            } else {
                TextualAnswer answer = new TextualAnswer(i, RESPONSE_ID);
                ANSWERS[i] = answer;
            }
        }
    }


    // ───────────────────────────────────────────────
    // Getters
    // ───────────────────────────────────────────────

    /**
     * Devuelve el identificador único de la respuesta.
     *
     * @return identificador de la respuesta
     */
    public String getRESPONSE_ID() {
        return RESPONSE_ID;
    }

    /**
     * Devuelve el identificador de la encuesta asociada a la respuesta.
     *
     * @return identificador de la encuesta
     */
    public String getSurveyId() {
        return SURVEY_ID;
    }

    /**
     * Devuelve el nombre de usuario del que responde.
     *
     * @return nombre de usuario del que responde
     */
    public String getResponderUsername() {
        return RESPONDER_USERNAME;
    }

    /**
     * Devuelve el estado actual de la respuesta.
     *
     * @return estado de la respuesta
     */
    public ResponseStatus getResponseStatus() {
        return responseStatus;
    }

    /**
     * Devuelve la fecha y hora en que se envió la respuesta.
     *
     * @return fecha y hora de envío
     */
    public LocalDateTime getSUBMITTED_AT() {
        return SUBMITTED_AT;
    }

    /**
     * Devuelve el conjunto de respuestas individuales.
     *
     * @return array de respuestas
     */
    public Answer[] getANSWERS() {
        return ANSWERS;
    }

    // ───────────────────────────────────────────────
    // Setters
    // ───────────────────────────────────────────────

    /**
     * Cambia el estado actual de la respuesta.
     *
     * @param responseStatus nuevo estado de la respuesta
     */
    public void setResponseStatus(ResponseStatus responseStatus) {
        this.responseStatus = responseStatus;
    }

    // this method must be used only when the response is submitted
    
    /**
     * Establece la fecha y hora en que se envió la respuesta.
     *
     * @param SUBMITTED_AT fecha y hora de envío
     */
    public void setSUBMITTED_AT(LocalDateTime SUBMITTED_AT) {
        this.SUBMITTED_AT = SUBMITTED_AT;
    }

    // Answer[] methods, correct usage must be ensured by the caller

    // ───────────────────────────────────────────────
    // Métodos relacionados con las respuestas individuales
    // ───────────────────────────────────────────────

    /**
     * Devuelve el tamaño del array de respuestas (número de preguntas de la encuesta).
     *
     * @return número de respuestas
     */
    private int getSize() {
        return ANSWERS.length;
    }

    /**
     * Verifica si un índice está dentro del rango válido del array de respuestas.
     *
     * @param index índice a verificar
     * @return true si el índice está dentro del rango, false en caso contrario
     */
    public boolean inRange(int index) {
        return (index >= 0 && index < getSize());
    }

    // can not add or remove answers since the number of questions is fixed
    /**
     * Actualiza la respuesta en la posición especificada del array de respuestas.
     *
     * @param index  índice de la respuesta a actualizar
     * @param answer nueva respuesta a establecer
     */
    public void updateAnswer(int index, Answer answer) {
        ANSWERS[index] = answer;
    }

     /**
     * Elimina el contenido de todas las respuestas.
     * 
     * @param index índice de la respuesta a borrar
     */
    public void clearAnswer(int index) {
        ANSWERS[index].clearAnswer();
    }

    ;

    /**
     * Devuelve la respuesta en la posición especificada del array de respuestas.
     *
     * @param index índice de la respuesta a obtener
     * @return respuesta en la posición especificada
     */
    public Answer getAnswer(int index) {
        return ANSWERS[index];
    }

}
