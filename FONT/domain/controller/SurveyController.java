package domain.controller;

import java.util.List;

import domain.model.Survey;
import domain.service.SurveyService;

/**
 * Controlador responsable de gestionar las operaciones relacionadas con las encuestas.
 * 
 * Esta clase actúa como capa intermedia entre la capa de presentación y la capa de servicio
 * ({@link SurveyService}), delegando las peticiones del usuario y aplicando la lógica de control
 * necesaria antes de interactuar con los datos de la aplicación.
 * 
 * Permite crear, inicializar, consultar y validar encuestas, así como obtener sus identificadores
 * y listas filtradas.
 */

public class SurveyController {
    /** Servicio encargado de la lógica de negocio relacionada con las encuestas. */
    private final SurveyService surveyService;

    // ───────────────────────────────────────────────
    // Constructor
    // ───────────────────────────────────────────────

    /**
     * Crea una nueva instancia del controlador de encuestas.
     *
     * @param surveyService servicio de encuestas utilizado para la lógica de negocio
     */

    public SurveyController(SurveyService surveyService) {
        this.surveyService = surveyService;

    }
    /**
     * Crea una nueva encuesta en el sistema.
     *
     * <p>Este método delega la operación en el {@link SurveyService}, que se encarga
     * de validar y almacenar la encuesta. Si la encuesta no cumple los requisitos
     * (por ejemplo, título o descripción vacíos), el servicio puede lanzar una excepción
     * correspondiente.</p>
     *
     * @param survey la encuesta que se desea crear
     * @return la encuesta creada, incluyendo cualquier información adicional generada
     *         durante el proceso (como un identificador único)
     */
    public Survey createSurvey(Survey survey) {
        return surveyService.createSurvey(survey);
    }
    /**
     * Inicializa una nueva encuesta con los datos básicos proporcionados.
     * 
     * Este método prepara una instancia de {@link Survey} con título, descripción
     * y nombre del creador, dejándola lista para ser configurada antes de su publicación.
     *
     * @param title            título de la encuesta
     * @param description      descripción de la encuesta
     * @param creatorUsername  nombre del usuario que crea la encuesta
     * @return objeto {@link Survey} inicializado
     */
    public Survey initializeNewSurvey(String title, String description, String creatorUsername) {
        return surveyService.initializeNewSurvey(title, description, creatorUsername);
    }

    /**
     * Devuelve una lista de encuestas seleccionadas.
     * 
     * En versiones futuras, este método permitirá paginar los resultados
     * según número de página y tamaño.
     *
     * @return lista de objetos {@link Survey} seleccionados
     */
    public List<Survey> getSelectedSurveys() {
        return surveyService.getSelectedSurveys();
    }

    /**
     * Devuelve una lista con los identificadores de todas las encuestas existentes.
     *
     * @return lista de identificadores de encuestas
     */
    public List<String> getSurveysId() {
        return surveyService.getSurveysId();
    }

    /**
     * Devuelve una encuesta específica según su identificador.
     *
     * @param surveyId identificador único de la encuesta
     * @return objeto {@link Survey} correspondiente, o {@code null} si no existe
     */
    public Survey getSurvey(String surveyId) {
        return surveyService.getSurvey(surveyId);
    }

    /**
     * Devuelve la lista de encuestas creadas por el usuario actual.
     *
     * @return lista de objetos {@link Survey} creados por el usuario
     */
    public List<Survey> getMySurveys() {
        return surveyService.getMySurveys();
    }

    /**
     * Verifica si una encuesta con el identificador dado existe en el sistema.
     *
     * @param surveyId identificador único de la encuesta
     * @return {@code true} si la encuesta existe, {@code false} en caso contrario
     */
    public boolean existsSurvey(String surveyId) {
        return surveyService.existsSurvey(surveyId);
    }

    /**
     * Incrementa el contador de respuestas de una encuesta específica.
     *
     * @param surveyId identificador único de la encuesta
     */
    public void incrementResponseCount(String surveyId) {
        surveyService.incrementResponseCount(surveyId);
    }

    /**
     * Obtiene todas las encuestas creadas por un usuario específico.
     *
     * @param username nombre del usuario
     * @return lista de encuestas del usuario
     */
    public List<Survey> getSurveysByUser(String username) {
        return surveyService.getSurveysByUser(username);
    }

    /**
     * Actualiza una encuesta existente (solo en estado DRAFT).
     *
     * @param surveyId identificador de la encuesta
     * @param updatedSurvey encuesta con los cambios
     * @return encuesta actualizada
     */
    public Survey updateSurvey(String surveyId, Survey updatedSurvey) {
        return surveyService.updateSurvey(surveyId, updatedSurvey);
    }

    /**
     * Publica una encuesta (cambia de DRAFT a PUBLISHED).
     *
     * @param surveyId identificador de la encuesta
     * @return encuesta publicada
     */
    public Survey publishSurvey(String surveyId) {
        return surveyService.publishSurvey(surveyId);
    }

    /**
     * Cierra una encuesta (cambia de PUBLISHED a CLOSED).
     *
     * @param surveyId identificador de la encuesta
     * @return encuesta cerrada
     */
    public Survey closeSurvey(String surveyId) {
        return surveyService.closeSurvey(surveyId);
    }

    /**
     * Elimina una encuesta (solo si está en estado DRAFT).
     *
     * @param surveyId identificador de la encuesta
     */
    public void deleteSurvey(String surveyId) {
        surveyService.deleteSurvey(surveyId);
    }

    /**
     * Elimina una pregunta de una encuesta.
     *
     * @param surveyId identificador de la encuesta
     * @param questionIndex índice de la pregunta a eliminar
     */
    public void deleteQuestion(String surveyId, int questionIndex) {
        surveyService.deleteQuestion(surveyId, questionIndex);
    }

    /**
     * Reordena las preguntas de una encuesta.
     *
     * @param surveyId identificador de la encuesta
     * @param oldIndex índice actual de la pregunta
     * @param newIndex nuevo índice de la pregunta
     */
    public void reorderQuestion(String surveyId, int oldIndex, int newIndex) {
        surveyService.reorderQuestion(surveyId, oldIndex, newIndex);
    }

    /**
     * Genera un identificador único para una nueva encuesta delegando al servicio.
     * @return nuevo ID generado
     */
    public String generateUniqueSurveyId() {
        return surveyService.generateUniqueSurveyId();
    }

    /**
     * Añade una valoración a una encuesta específica.
     *
     * @param surveyId identificador único de la encuesta
     * @param rating valoración a añadir
     */
    public void addRating(String surveyId, double rating) {
        surveyService.addSurveyRating(surveyId, rating);
    }

}
