package domain.service;

import java.util.List;
import java.util.TreeSet;

import data.ResponseRepository;
import domain.controller.UserController;
import domain.exception.ResponseException;
import domain.exception.SurveyException;
import domain.model.Answer;
import domain.model.MultipleChoiceAnswer;
import domain.model.MultipleChoiceQuestion;
import domain.model.NumericalAnswer;
import domain.model.Question;
import domain.model.Response;
import domain.model.TextualAnswer;
import domain.model.enums.ResponseStatus;
import domain.model.enums.TypeQuestion;

/**
 * Servicio encargado de gestionar toda la lógica de negocio relacionada con las
 * respuestas de los usuarios a las encuestas del sistema.
 * <p>
 * Este servicio garantiza:
 * <ul>
 *     <li>Que el usuario esté autenticado antes de responder.</li>
 *     <li>Que la encuesta, la pregunta y la respuesta existan correctamente.</li>
 *     <li>Que las respuestas se creen, actualicen y validen siguiendo las reglas definidas.</li>
 *     <li>Coherencia entre el registro de respuestas en el repositorio y el usuario que las emite.</li>
 * </ul>
 * <p>
 * Las excepciones asociadas a errores de validación o de acceso se gestionan mediante
 * {@link ResponseException} y {@link SurveyException}.
 */
public class ResponseService {

    /**
     * Repositorio encargado de almacenar todas las respuestas del sistema.
     */
    private final ResponseRepository responseRepository;


    /**
     * Controlador de usuario para validar sesión y recuperar el usuario actual.
     */
    private final UserController userController;

    /**
     * Servicio de encuestas utilizado para validar estructura y recuperar preguntas.
     */
    public final SurveyService surveyService;

    /**
     * Construye el servicio de respuestas.
     *
     * @param responseRepository repositorio de respuestas
     * @param userController     controlador responsable del estado de sesión
     * @param surveyService      servicio de encuestas para validación de estructura
     */
    public ResponseService(ResponseRepository responseRepository,
                           UserController userController,
                           SurveyService surveyService) {
        this.responseRepository = responseRepository;
        this.userController = userController;
        this.surveyService = surveyService;
    }


    // ───────────────────────────────────────────────
    // Validación de entradas
    // ───────────────────────────────────────────────

    /**
     * Comprueba si un texto está vacío o es nulo.
     */
    private boolean isInputBlank(String text) {
        return text == null || text.trim().isEmpty();
    }

    /**
     * Comprueba si un número es inválido (tratado como texto).
     */
    private boolean isInputBlank(Double num) {
        return num.toString().trim().isEmpty();
    }

    // ───────────────────────────────────────────────
    // Gestión de selección Multiple Choice
    // ───────────────────────────────────────────────

    /**
     * Convierte un string con índices de selección ("0 2 3") en un array booleano
     * validando:
     * <ul>
     *     <li>Formato numérico</li>
     *     <li>Límites mínimo y máximo de selección</li>
     *     <li>Que las opciones existan</li>
     * </ul>
     *
     * @param input         cadena con los índices seleccionados
     * @param minSelections mínimo permitido
     * @param maxSelections máximo permitido
     * @param numOptions    número total de opciones de la pregunta
     * @return array booleano indicando qué opciones se han seleccionado
     */
    public boolean[] getOptionsSelected(String input, int minSelections, int maxSelections, int numOptions) {
        if (!input.matches("[0-9\\s]+"))
            throw new ResponseException("Las opciones tienen que ser las opciones marcadas separadas por espacios.");
        String[] optionsStr = input.trim().split("\\s+");
        TreeSet<Integer> selectedOptions = new TreeSet<Integer>();
        for (String s : optionsStr) {
            if (!s.trim().isEmpty())
                selectedOptions.add(Integer.parseInt(s));
        }
        if (selectedOptions.size() < minSelections)
            throw new ResponseException("Debes seleccionar como mínimo " + minSelections + " opciones.");
        if (selectedOptions.size() > maxSelections)
            throw new ResponseException("Debes seleccionar como máximo " + maxSelections + " opciones");
        if (selectedOptions.last() >= numOptions)
            throw new ResponseException("Has seleccionado una opción que no existe.");
        boolean[] result = new boolean[numOptions];
        for (Integer option : selectedOptions)
            result[option] = true;
        return result;
    }

    // ───────────────────────────────────────────────
    // Validación de sesión / existencia
    // ───────────────────────────────────────────────

    /**
     * Confirma que el usuario está logueado.
     *
     * @throws SurveyException si no hay usuario autenticado
     */
    public void checkUserLoggedin() {
        if (!userController.isLoggedIn())
            throw new SurveyException("Debes iniciar sesión para poder responder encuestas.");
    }

    /**
     * Verifica que una respuesta concreta existe dentro de una encuesta.
     *
     * @throws ResponseException si la respuesta no existe
     */
    public void checkResponseExists(String surveyId, String responseId) {
        if (!responseRepository.existsResponse(surveyId, responseId))
            throw new ResponseException("La respuesta con id " + responseId + " no existe.");
    }

    // ───────────────────────────────────────────────
    // Creación de respuestas
    // ───────────────────────────────────────────────


    /**
     * Inicia una nueva respuesta para una encuesta:
     * <ul>
     *     <li>Verifica que el usuario esté logueado.</li>
     *     <li>Obtiene las preguntas de la encuesta (validando su existencia).</li>
     *     <li>Genera un nuevo ID de respuesta.</li>
     *     <li>Crea una instancia de {@link Response} con tantas respuestas como preguntas.</li>
     *     <li>Registra la entrada en el repositorio.</li>
     *     <li>Mantiene la coherencia con el índice del usuario.</li>
     * </ul>
     *
     * @param surveyId identificador de la encuesta
     * @return identificador de la nueva respuesta
     */
    public Response startResponse(String surveyId) {
        checkUserLoggedin();

        String username = userController.getUsernameLoggedIn();

        // 1️⃣ Buscar draft existente
        Response draft = responseRepository.getDraftResponse(surveyId, username);
        if (draft != null) {
            return draft;
        }

        // 2️⃣ Crear nueva response DRAFT
        List<Question> questions = surveyService.getQuestions(surveyId);
        String responseId = responseRepository.getValidResponseId();

        Response response = new Response(responseId, surveyId, username, questions);

        if (!responseRepository.existsSurveyEntry(surveyId)) {
            responseRepository.addSurveyEntry(surveyId);
        }

        responseRepository.addResponse(surveyId, response);
        userController.addResponseId(username, surveyId, responseId);

        return response;
    }



    // ───────────────────────────────────────────────
    // Recuperación de preguntas y respuestas
    // ───────────────────────────────────────────────

    /**
     * Devuelve todas las preguntas de la encuesta.
     */
    public List<Question> getQuestions(String surveyId) {
        return surveyService.getQuestions(surveyId);
    }

    /**
     * Devuelve una pregunta concreta.
     */
    public Question getQuestion(String surveyId, int questionIndex) {
        return surveyService.getQuestion(surveyId, questionIndex);
    }

    /**
     * Comprueba si existe al menos una respuesta completada.
     *
     * @param answers lista de respuestas
     * @return true si alguna está respondida
     */
    public boolean existsQuestionAnswered(List<Answer> answers) {
        for (Answer a : answers)
            if (a.getIsAnswered())
                return true;
        return false;

    }

    /**
     * Devuelve todas las respuestas asociadas a un responseId validado.
     *
     * @throws ResponseException si no hay ninguna contestada
     */
    public List<Answer> getAnswers(String surveyId, String responseId) {
        checkResponseExists(surveyId, responseId);
        List<Answer> answers = responseRepository.getAllAnswers(surveyId, responseId);
        if (!existsQuestionAnswered(answers))
            throw new ResponseException("Todavía no has respondido ninguna pregunta.");
        return answers;
    }

    // ───────────────────────────────────────────────
    // Respuesta individual por pregunta
    // ───────────────────────────────────────────────

    /**
     * Inicializa una respuesta vacía según el tipo de pregunta:
     * <ul>
     *     <li>MultipleChoiceAnswer</li>
     *     <li>TextualAnswer</li>
     *     <li>NumericalAnswer</li>
     * </ul>
     */
    public Question startAnswer(String surveyId, String responseId, int questionIndex) {
        checkResponseExists(surveyId, responseId);
        Question question = surveyService.getQuestion(surveyId, questionIndex); // this verifies that the question exists in the survey
        Answer answer; //
        if (question.getTypeQuestion() == TypeQuestion.MULTIPLE_CHOICE) {
            answer = new MultipleChoiceAnswer(questionIndex, responseId, ((MultipleChoiceQuestion) question).getOptionsSize());
        } else if (question.getTypeQuestion() == TypeQuestion.TEXTUAL)
            answer = new TextualAnswer(questionIndex, responseId);
        else answer = new NumericalAnswer(questionIndex, responseId);
        responseRepository.updateAnswer(surveyId, responseId, questionIndex, answer); // the answerIndex is the same as the questionIndex
        return question;
    }

    // ───────────────────────────────────────────────
    // Actualización de respuestas
    // ───────────────────────────────────────────────

    /**
     * Actualiza una respuesta textual o de opción múltiple.
     *
     * @throws ResponseException si la entrada es inválida o las selecciones no cumplen las reglas
     */
    public void updateAnswer(String surveyId, String responseId, int questionIndex, String strAnswer, TypeQuestion answerType) {
        if (isInputBlank(strAnswer))
            throw new ResponseException("La respuesta no puede ser vacía.");
        checkResponseExists(surveyId, responseId);
        surveyService.checkQuestionExists(surveyId, questionIndex);
        if (answerType.equals(TypeQuestion.TEXTUAL)) {
            TextualAnswer answer = new TextualAnswer(questionIndex, responseId);
            answer.setAnswerText(strAnswer);
            responseRepository.updateAnswer(surveyId, responseId, questionIndex, answer);
        } else {
            // Multiple choice answer
            MultipleChoiceQuestion mcQuestion = (MultipleChoiceQuestion) surveyService.getQuestion(surveyId, questionIndex);
            boolean[] optionsSelected = getOptionsSelected(strAnswer, mcQuestion.getMinSelections(), mcQuestion.getMaxSelections(), mcQuestion.getOptionsSize());
            MultipleChoiceAnswer answer = new MultipleChoiceAnswer(questionIndex, responseId, mcQuestion.getMaxSelections());
            answer.setOptions(optionsSelected);
            responseRepository.updateAnswer(surveyId, responseId, questionIndex, answer);
        }
    }

    /**
     * Actualiza una respuesta numérica.
     *
     * @throws ResponseException si la entrada es inválida
     */
    public void updateAnswer(String surveyId, String responseId, int questionIndex, Double numAnswer) {
        if (isInputBlank(numAnswer))
            throw new ResponseException("La respuesta no puede ser vacía.");
        checkResponseExists(surveyId, responseId);
        surveyService.checkQuestionExists(surveyId, questionIndex); // verifies that the questionIndex is valid
        NumericalAnswer answer = new NumericalAnswer(questionIndex, responseId);
        answer.setAnswerNum(numAnswer);
        responseRepository.updateAnswer(surveyId, responseId, questionIndex, answer);
    }

    // ───────────────────────────────────────────────
    // Estadísticas
    // ───────────────────────────────────────────────

    /**
     * Incrementa el contador de respuestas de una encuesta delegando la operación
     * en {@link SurveyService}.
     *
     * @param surveyId identificador de la encuesta
     */
    public void incrementResponseCount(String surveyId) {
        surveyService.incrementResponseCount(surveyId);
    }

    /**
     * Publica una respuesta, cambiando su estado a SUBMITTED.
     *
     * @param surveyId   identificador de la encuesta
     * @param responseId identificador de la respuesta
     */
    public void publishResponse(String surveyId, String responseId) {
        checkResponseExists(surveyId, responseId);

        Response response = responseRepository.getResponse(surveyId, responseId);
        response.setResponseStatus(ResponseStatus.SUBMITTED);

        responseRepository.updateResponse(surveyId, response);
    }

}
