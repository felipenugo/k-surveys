package domain.service;

import data.SurveyRepository;
import domain.controller.UserController;
import domain.exception.SurveyException;
import domain.model.*;
import domain.model.enums.TypeQuestion;

import java.util.List;

/**
 * Servicio encargado de gestionar toda la lógica de negocio relacionada con las encuestas.
 * 
 * Este servicio valida permisos, existencia de encuestas y preguntas, y garantiza que las
 * operaciones sobre las encuestas cumplan las reglas del sistema. También delega en 
 * {@link SurveyRepository} la persistencia de los datos.
 * 
 * Las operaciones de este servicio requieren que el usuario esté autenticado, por lo que
 * la mayoría de los métodos invocan internamente a {@link UserController#isLoggedIn()}.
 * 
 * Las excepciones relacionadas con errores de validación o permisos se encapsulan en
 * {@link SurveyException}.
 */

public class SurveyService {
    /** Repositorio encargado de almacenar y gestionar las encuestas. */
    private final SurveyRepository surveyRepository;
    /** Controlador de usuario para validar permisos y obtener al usuario activo. */
    private final UserController userController;

    /**
     * Crea una nueva instancia del servicio de encuestas.
     *
     * @param surveyRepository repositorio de encuestas
     * @param userController controlador de usuario utilizado para validar permisos
     */
    public SurveyService(SurveyRepository surveyRepository, UserController userController) {
        this.surveyRepository = surveyRepository;
        this.userController = userController;
    }

    // ───────────────────────────────────────────────
    // Validaciones principales
    // ───────────────────────────────────────────────

    /**
     * Verifica que el usuario actual ha iniciado sesión.
     *
     * @throws SurveyException si ningún usuario ha iniciado sesión
     */
    public void checkUserLoggedin() {
        if (!userController.isLoggedIn())
            throw new SurveyException("Debes iniciar sesión para poder responder encuestas.");

    }

    /**
     * Comprueba que una encuesta exista en el repositorio.
     *
     * @param surveyId identificador de la encuesta
     * @throws SurveyException si la encuesta no existe
     */
    public void checkSurveyExists(String surveyId) {
        if (!surveyRepository.existsSurvey(surveyId))
            throw new SurveyException("La encuesta con id " + surveyId + " no existe.");
    }

     // ───────────────────────────────────────────────
    // Creación de encuestas
    // ───────────────────────────────────────────────

    /**
     * Genera un nuevo identificador único para una encuesta.
     *
     * @return identificador autogenerado
     */
    public String generateUniqueSurveyId() {
        return surveyRepository.generateNextSurveyId();
    }

    /**
     * Crea una encuesta después de validar permisos, titularidad y contenido.
     * 
     * Este método:
     * <ul>
     *   <li>Comprueba que el usuario esté logueado.</li>
     *   <li>Valida título, descripción y número de preguntas.</li>
     *   <li>Verifica que el creador de la encuesta sea el usuario logueado.</li>
     *   <li>Asigna un ID único si la encuesta aún no lo tiene (el campo es final).</li>
     *   <li>Impide sobrescribir encuestas existentes con el mismo ID.</li>
     *   <li>Guarda la encuesta en el repositorio.</li>
     * </ul>
     *
     * @param survey encuesta a crear
     * @throws SurveyException si la encuesta es inválida o el usuario no tiene permisos
     */
    public Survey createSurvey(Survey survey) {
        checkUserLoggedin();

        // Validar que la encuesta tenga título
        if (survey.getTitle() == null || survey.getTitle().trim().isEmpty()) {
            throw new SurveyException("El título de la encuesta no puede estar vacío.");
        }

        // Validar que la encuesta tenga descripción
        if (survey.getDescription() == null || survey.getDescription().trim().isEmpty()) {
            throw new SurveyException("La descripción de la encuesta no puede estar vacía.");
        }

        // Validar que la encuesta tenga al menos una pregunta
        if (survey.getSize() == 0) {
            throw new SurveyException("La encuesta debe tener al menos una pregunta.");
        }

        // Verificar que el creador sea el usuario logueado
        if (!survey.getCREATOR_USERNAME().equals(userController.getLoggedUser().getUsername())) {
            throw new SurveyException("Solo puedes crear encuestas a tu nombre.");
        }

        // Generar el ID autoincremental si no tiene uno
        String surveyId = survey.getSURVEY_ID();

        if (surveyId == null || surveyId.trim().isEmpty()) {
            // Generar el siguiente ID autoincremental
            surveyId = generateUniqueSurveyId();

            // Crear una nueva encuesta con el ID generado (porque SURVEY_ID es final)
            Survey surveyWithId = new Survey(surveyId, survey.getTitle(), survey.getDescription(), survey.getCREATOR_USERNAME());
            surveyWithId.setSurveyStatus(survey.getSurveyStatus());
            if (survey.getPUBLISHED_AT() != null) {
                surveyWithId.setPUBLISHED_AT();
            }

            // Copiar todas las preguntas
            for (int i = 0; i < survey.getSize(); i++) {
                Question q = survey.getQuestion(i);
                q.setQuestionIndex(i);
                q.setSURVEY_ID(surveyId);
                if(q.getTypeQuestion().equals(TypeQuestion.MULTIPLE_CHOICE))
                {
                    q = (MultipleChoiceQuestion)q.copy();
                }
                surveyWithId.addQuestion(q);
            }

            survey = surveyWithId;
        } else {
            // Si ya tiene ID, verificar que no exista
            if (surveyRepository.existsSurvey(surveyId)) {
                throw new SurveyException("Ya existe una encuesta con el ID: " + surveyId);
            }
        }

        // Guardar en el repositorio
        surveyRepository.addSurvey(survey);
        return survey;
    }

     /**
     * Inicializa una nueva encuesta sin asignarle un ID todavía.
     * Se utiliza antes de la creación definitiva.
     *
     * @param title título de la encuesta
     * @param description descripción de la encuesta
     * @param creatorUsername usuario creador
     * @return encuesta creada pero no almacenada aún
     * @throws SurveyException si título o descripción están vacíos
     */
    public Survey initializeNewSurvey(String title, String description, String creatorUsername) {
        checkUserLoggedin();

        if (title == null || title.trim().isEmpty()) {
            throw new SurveyException("El título no puede estar vacío.");
        }

        if (description == null || description.trim().isEmpty()) {
            throw new SurveyException("La descripción no puede estar vacía.");
        }

        // Crear encuesta sin ID (se asignará al publicar)
        return new Survey(title, description, creatorUsername);
    }


    /**
     * Comprueba que una pregunta concreta existe dentro de una encuesta.
     *
     * @param surveyId identificador de la encuesta
     * @param questionIndex índice de la pregunta
     * @throws SurveyException si la pregunta no existe
     */
    public void checkQuestionExists(String surveyId, int questionIndex) {

        if (!existsQuestion(surveyId, questionIndex))
            throw new SurveyException("La pregunta con índice " + questionIndex + " no existe en esta encuesta.");
    }

    // ───────────────────────────────────────────────
    // Recuperación de encuestas y preguntas
    // ───────────────────────────────────────────────

    /**
     * Devuelve todas las encuestas disponibles en el sistema.
     *
     * @return lista de encuestas
     * @throws SurveyException si no existen encuestas
     */
    public List<Survey> getSelectedSurveys() {
        checkUserLoggedin();
        List<Survey> surveys = surveyRepository.getAllSurveys();
        if (surveys.isEmpty())
            throw new SurveyException("No hay encuestas creadas todavía.");
        return surveys;
    }

    /**
     * Devuelve todos los identificadores de encuestas.
     *
     * @return lista de IDs
     */
    public List<String> getSurveysId() {
        checkUserLoggedin();
        return surveyRepository.getAllSurveysId();
    }

    /**
     * Devuelve el número de preguntas de una encuesta concreta.
     *
     * @param surveyId identificador de la encuesta
     * @return número de preguntas
     */
    public int getNumQuestions(String surveyId) {
        checkSurveyExists(surveyId);
        return surveyRepository.getNumQuestions(surveyId);
    }

    /**
     * Devuelve todas las preguntas de una encuesta concreta.
     *
     * @param surveyId identificador de la encuesta
     * @return lista de preguntas
     * @throws SurveyException si la encuesta no tiene preguntas
     */
    public List<Question> getQuestions(String surveyId) {
        checkSurveyExists(surveyId);
        List<Question> questions = surveyRepository.getAllQuestions(surveyId);
        if (questions.isEmpty())
            throw new SurveyException("La encuesta con id " + surveyId + " no tiene preguntas.");
        return questions;
    }

    /**
     * Devuelve una pregunta concreta dado su índice.
     *
     * @param surveyId identificador de la encuesta
     * @param questionIndex índice de la pregunta
     * @return pregunta correspondiente
     */
    public Question getQuestion(String surveyId, int questionIndex) {
        checkQuestionExists(surveyId, questionIndex);
        return surveyRepository.getQuestion(surveyId, questionIndex);
    }

    /**
     * Devuelve una encuesta según su ID.
     *
     * @param surveyId identificador de la encuesta
     * @return encuesta correspondiente
     */
    public Survey getSurvey(String surveyId) {
        checkSurveyExists(surveyId);
        return surveyRepository.getSurvey(surveyId);
    }

    /**
     * Devuelve todas las encuestas creadas por el usuario actual.
     *
     * @return lista de encuestas creadas por el usuario logueado
     */
    public List<Survey> getMySurveys() {
        checkUserLoggedin();
        return surveyRepository.getSurveysByUsername(userController.getLoggedUser().getUsername());
    }

    // ───────────────────────────────────────────────
    // Validación de existencia
    // ───────────────────────────────────────────────

    /**
     * Comprueba si una pregunta existe en una encuesta concreta.
     *
     * @param surveyId identificador de la encuesta
     * @param questionIndex índice de la pregunta
     * @return {@code true} si existe, {@code false} en caso contrario
     */
    public boolean existsQuestion(String surveyId, int questionIndex) {
        checkSurveyExists(surveyId);
        int numQuestions = surveyRepository.getNumQuestions(surveyId);
        return questionIndex >= 0 && questionIndex < numQuestions;
    }

     /**
     * Comprueba si existe una encuesta en el sistema.
     *
     * @param surveyId identificador de la encuesta
     * @return {@code true} si existe, {@code false} en caso contrario
     */
    public boolean existsSurvey(String surveyId) {
        return surveyRepository.existsSurvey(surveyId);
    }

    // ───────────────────────────────────────────────
    // Contador de respuestas
    // ───────────────────────────────────────────────

    /**
     * Incrementa el contador de visualizaciones o respuestas de una encuesta.
     * 
     * Este método depende de que la clase {@link Survey} implemente los métodos
     * {@code getViews()} y {@code setViews(int)}.
     * 
     * @param surveyId identificador de la encuesta
     */
    public void incrementResponseCount(String surveyId) {
        checkSurveyExists(surveyId);
        Survey survey = surveyRepository.getSurvey(surveyId);
        int responseCount = survey.getViews() + 1;
        survey.setViews(responseCount);
        surveyRepository.addSurvey(survey);
    }
}
