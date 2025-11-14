package domain.controller;

import domain.model.Question;
import domain.model.MultipleChoiceQuestion;
import domain.service.QuestionService;

import java.util.List;

/**
 * Controlador responsable de gestionar las preguntas asociadas a las encuestas.
 * 
 * Actúa como intermediario entre la capa de presentación y la lógica de negocio
 * implementada en {@link QuestionService}, permitiendo crear, actualizar y eliminar
 * preguntas de distintos tipos (textuales, numéricas o de elección múltiple).
 * 
 * También proporciona métodos para modificar opciones y restricciones en las preguntas
 * de tipo múltiple.
 */

public class QuestionController {
    /** Servicio encargado de la lógica de negocio relacionada con las preguntas. */
    private final QuestionService questionService;

    // ───────────────────────────────────────────────
    // Constructor
    // ───────────────────────────────────────────────

    /**
     * Crea una nueva instancia del controlador de preguntas.
     *
     * @param questionService servicio de preguntas utilizado para la lógica de negocio
     */
    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    // ───────────────────────────────────────────────
    // Creación de preguntas
    // ───────────────────────────────────────────────

    /**
     * Crea una nueva pregunta de tipo textual.
     *
     * @param questionIndex índice de la pregunta dentro de la encuesta
     * @param surveyId      identificador de la encuesta a la que pertenece
     * @param questionText  texto de la pregunta
     * @param isRequired    indica si la pregunta es obligatoria
     * @return objeto {@link Question} de tipo textual
     */
    public Question createTextualQuestion(int questionIndex, String surveyId, String questionText, boolean isRequired) {
        return questionService.createTextualQuestion(questionIndex, surveyId, questionText, isRequired);
    }

    /**
     * Crea una nueva pregunta de tipo numérico.
     *
     * @param questionIndex índice de la pregunta dentro de la encuesta
     * @param surveyId      identificador de la encuesta a la que pertenece
     * @param questionText  texto de la pregunta
     * @param isRequired    indica si la pregunta es obligatoria
     * @return objeto {@link Question} de tipo numérico
     */
    public Question createNumericalQuestion(int questionIndex, String surveyId, String questionText, boolean isRequired) {
        return questionService.createNumericalQuestion(questionIndex, surveyId, questionText, isRequired);
    }
    /**
     * Crea una nueva pregunta de tipo opción múltiple.
     *
     * @param questionIndex índice de la pregunta dentro de la encuesta
     * @param surveyId      identificador de la encuesta a la que pertenece
     * @param questionText  texto de la pregunta
     * @param isRequired    indica si la pregunta es obligatoria
     * @param minSelections número mínimo de selecciones permitidas
     * @param maxSelections número máximo de selecciones permitidas
     * @param optionTexts   lista de textos para las opciones disponibles
     * @return objeto {@link MultipleChoiceQuestion} creado
     */ 
    public MultipleChoiceQuestion createMultipleChoiceQuestion(
            int questionIndex,
            String surveyId,
            String questionText,
            boolean isRequired,
            int minSelections,
            int maxSelections,
            List<String> optionTexts) {
        return questionService.createMultipleChoiceQuestion(
                questionIndex, surveyId, questionText, isRequired, minSelections, maxSelections, optionTexts);
    }
    // ───────────────────────────────────────────────
    // Modificación de preguntas    
    // ───────────────────────────────────────────────

    /**
     * Actualiza el texto de una pregunta.
     *
     * @param question pregunta a modificar
     * @param newText  nuevo texto de la pregunta
     */
    public void updateQuestionText(Question question, String newText) {
        questionService.updateQuestionText(question, newText);
    }

    /**
     * Cambia el estado de obligatoriedad de una pregunta.
     *
     * @param question pregunta a modificar
     */ 
    public void toggleRequiredStatus(Question question) {
        questionService.toggleRequiredStatus(question);
    }

    /**
     * Añade una nueva opción a una pregunta de opción múltiple.
     *
     * @param question   pregunta de opción múltiple a modificar
     * @param optionText texto de la nueva opción
     */
    public void addOption(MultipleChoiceQuestion question, String optionText) {
        questionService.addOption(question, optionText);
    }

    /**
     * Actualiza el texto de una opción en una pregunta de opción múltiple.
     *
     * @param question pregunta de opción múltiple a modificar
     * @param index    índice de la opción a actualizar
     * @param newText  nuevo texto para la opción
     */
    public void updateOption(MultipleChoiceQuestion question, int index, String newText) {
        questionService.updateOption(question, index, newText);
    }

    /**
     * Elimina una opción de una pregunta de opción múltiple.
     *
     * @param question pregunta de opción múltiple a modificar
     * @param index    índice de la opción a eliminar
     */
    public void removeOption(MultipleChoiceQuestion question, int index) {
        questionService.removeOption(question, index);
    }

    /**
     * Actualiza el número mínimo de selecciones permitidas en una pregunta de opción múltiple.
     *
     * @param question      pregunta de opción múltiple a modificar
     * @param minSelections nuevo número mínimo de selecciones permitidas
     */
    public void updateMinSelections(MultipleChoiceQuestion question, int minSelections) {
        questionService.updateMinSelections(question, minSelections);
    }

    /**
     * Actualiza el número máximo de selecciones permitidas en una pregunta de opción múltiple.
     *
     * @param question      pregunta de opción múltiple a modificar
     * @param maxSelections nuevo número máximo de selecciones permitidas
     */
    public void updateMaxSelections(MultipleChoiceQuestion question, int maxSelections) {
        questionService.updateMaxSelections(question, maxSelections);
    }
}
