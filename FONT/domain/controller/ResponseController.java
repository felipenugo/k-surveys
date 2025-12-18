package domain.controller;

import domain.service.ResponseService;
import domain.model.*;
import domain.model.enums.*;

import java.util.List;

/**
 * Controlador responsable de gestionar las respuestas de los usuarios a las encuestas.
 * 
 * Actúa como intermediario entre la capa de presentación y la lógica de negocio
 * implementada en {@link ResponseService}, encargándose de iniciar, consultar y
 * actualizar las respuestas asociadas a cada encuesta.
 * 
 * Permite acceder a las preguntas de una encuesta, iniciar nuevas respuestas,
 * recuperar respuestas existentes y actualizar sus valores según el tipo de pregunta.
 */
public class ResponseController {
    /** Servicio encargado de la lógica de negocio relacionada con las respuestas. */
    private final ResponseService responseService;

    // ───────────────────────────────────────────────
    // Constructor
    // ───────────────────────────────────────────────

    /**
     * Crea una nueva instancia del controlador de respuestas.
     *
     * @param responseService servicio de respuestas utilizado para la lógica de negocio
     */
    public ResponseController(ResponseService responseService) {
        this.responseService = responseService;
    }

     // ───────────────────────────────────────────────
    // Métodos principales
    // ───────────────────────────────────────────────

    /**
     * Inicia una nueva respuesta asociada a una encuesta.
     * 
     * Este método crea una nueva instancia de {@link Response} y devuelve
     * el identificador único generado para dicha respuesta.
     *
     * @param surveyId identificador de la encuesta a la que pertenece la respuesta
     * @return identificador único de la nueva respuesta
     */
    public String startResponse(String surveyId) {
        return responseService.startResponse(surveyId);
    }

    /**
     * Devuelve la lista completa de preguntas asociadas a una encuesta.
     *
     * @param surveyId identificador de la encuesta
     * @return lista de objetos {@link Question} que pertenecen a la encuesta
     */
    public List<Question> getQuestions(String surveyId) {
        return responseService.getQuestions(surveyId);
    }

    /**
     * Devuelve una pregunta específica de una encuesta según su índice.
     *
     * @param surveyId identificador de la encuesta
     * @param questionIndex índice de la pregunta dentro de la encuesta
     * @return objeto {@link Question} correspondiente al índice proporcionado
     */
    public Question getQuestion(String surveyId, int questionIndex) {
        return responseService.getQuestion(surveyId, questionIndex);
    }

    /**
     * Devuelve la lista de respuestas asociadas a una respuesta específica de una encuesta.
     *
     * @param surveyId identificador de la encuesta
     * @param responseId identificador de la respuesta
     * @return lista de objetos {@link Answer} correspondientes a la respuesta proporcionada
     */
    public List<Answer> getAnswers(String surveyId, String responseId) {
        return responseService.getAnswers(surveyId, responseId);
    }

    /**
     * Inicia la respuesta a una pregunta específica dentro de una encuesta.
     *
     * @param surveyId identificador de la encuesta
     * @param responseId identificador de la respuesta
     * @param questionIndex índice de la pregunta dentro de la encuesta
     * @return objeto {@link Question} correspondiente a la pregunta iniciada
     */
    public Question startAnswer(String surveyId, String responseId, int questionIndex) {
        return responseService.startAnswer(surveyId, responseId, questionIndex);
    }

    /**
     * Actualiza la respuesta a una pregunta de tipo texto o selección múltiple.
     *
     * @param surveyId identificador de la encuesta
     * @param responseId identificador de la respuesta
     * @param questionIndex índice de la pregunta dentro de la encuesta
     * @param strAnswer respuesta en formato de cadena
     * @param typeAnswer tipo de pregunta (TEXTO o MULTIPLE_CHOICE)
     */
    public void updateAnswer(String surveyId, String responseId, int questionIndex, String strAnswer, TypeQuestion typeAnswer) {
        responseService.updateAnswer(surveyId, responseId, questionIndex, strAnswer, typeAnswer);
    }

    /**
     * Actualiza la respuesta a una pregunta de tipo numérico.
     *
     * @param surveyId identificador de la encuesta
     * @param responseId identificador de la respuesta
     * @param questionIndex índice de la pregunta dentro de la encuesta
     * @param numericalAnswer respuesta en formato numérico
     */
    public void updateAnswer(String surveyId, String responseId, int questionIndex, Double numericalAnswer) {
        responseService.updateAnswer(surveyId, responseId, questionIndex, numericalAnswer);
    }

    /**
     * Incrementa el contador de respuestas de una encuesta.
     *
     * @param surveyid identificador de la encuesta
     */
    public void  incrementResponseCount(String surveyid)
    {
        responseService.incrementResponseCount(surveyid);
    }

    /**
     * Devuelve todas las respuestas enviadas de una encuesta.
     *
     * @param surveyId identificador de la encuesta
     * @return lista de objetos {@link Response} correspondientes a la encuesta
     */
    public List<Response> getAllResponses(String surveyId) {
        return responseService.getAllResponses(surveyId);
    }
}

