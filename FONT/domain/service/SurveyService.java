package domain.service;

import data.SurveyRepository;
import domain.controller.UserController;
import domain.exception.SurveyException;
import domain.model.*;
import domain.model.enums.TypeQuestion;

import java.util.List;
import java.util.ArrayList;

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
    /** Servicio de usuario para registrar encuestas creadas. */
    private final UserService userService;

    /**
     * Crea una nueva instancia del servicio de encuestas.
     *
     * @param surveyRepository repositorio de encuestas
     * @param userController controlador de usuario utilizado para validar permisos
     * @param userService servicio de usuario para registrar encuestas creadas
     */
    public SurveyService(SurveyRepository surveyRepository, UserController userController, UserService userService) {
        this.surveyRepository = surveyRepository;
        this.userController = userController;
        this.userService = userService;
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
        // Nota: permitir crear encuestas sin preguntas (se validará al publicar)
        /*if (survey.getSize() == 0) {
            throw new SurveyException("La encuesta debe tener al menos una pregunta.");
        }*/

        // Verificar que el creador sea el usuario logueado
        if (!survey.getCREATOR_USERNAME().equals(userController.getLoggedUser().getUsername())) {
            throw new SurveyException("Solo puedes crear encuestas a tu nombre.");
        }

        // Generar el ID autoincremental si no tiene uno
        String surveyId = survey.getSURVEY_ID();

        // Siempre construiremos una nueva instancia de Survey con el ID definitivo y copiaremos
        // todas las preguntas al objeto que realmente se va a persistir. Esto evita inconsistencias
        // si el objeto pasado proviene de la capa de presentación con referencias incompletas.
        if (surveyId == null || surveyId.trim().isEmpty()) {
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
            // Si se proporcionó un ID existente, asegurarnos de que no exista ya en el repositorio
            if (surveyRepository.existsSurvey(surveyId)) {
                throw new SurveyException("Ya existe una encuesta con el ID: " + surveyId);
            }
        }

        // Crear una nueva encuesta con el ID generado o proporcionado (porque SURVEY_ID es final)
        Survey surveyWithId = new Survey(surveyId, survey.getTitle(), survey.getDescription(), survey.getCREATOR_USERNAME());
        surveyWithId.setSurveyStatus(survey.getSurveyStatus());
        if (survey.getPUBLISHED_AT() != null) {
            surveyWithId.setPUBLISHED_AT();
        }

        // Copiar todas las preguntas creando nuevas instancias con el SURVEY_ID correcto
        for (int i = 0; i < survey.getSize(); i++) {
            Question originalQ = survey.getQuestion(i);

            if (originalQ.getTypeQuestion() == domain.model.enums.TypeQuestion.MULTIPLE_CHOICE) {
                domain.model.MultipleChoiceQuestion origMc = (domain.model.MultipleChoiceQuestion) originalQ;
                domain.model.MultipleChoiceQuestion mcCopy = new domain.model.MultipleChoiceQuestion(origMc.getQuestionIndex(), surveyId);
                mcCopy.setQuestionText(origMc.getQuestionText());
                mcCopy.setRequired(origMc.isRequired());

                // Copiar opciones primero
                if (origMc.getOptions() != null) {
                    for (int k = 0; k < origMc.getOptions().size(); k++) {
                        domain.model.OptionQuestion opt = origMc.getOption(k);
                        domain.model.OptionQuestion optCopy = new domain.model.OptionQuestion(opt.getQuestionIndex(), surveyId);
                        optCopy.setOptionText(opt.getOptionText());
                        mcCopy.addOption(optCopy);
                    }
                }

                // Copiar min/max seleccion después de añadir opciones (usar setters para validar)
                try {
                    mcCopy.setMaxSelections(origMc.getMaxSelections());
                    mcCopy.setMinSelections(origMc.getMinSelections());
                } catch (IllegalArgumentException e) {
                    // Si las validaciones fallan, ajustar a valores seguros
                    int optionsCount = mcCopy.getOptions().size();
                    if (optionsCount >= 1) {
                        mcCopy.setMinSelections(1);
                        mcCopy.setMaxSelections(Math.max(1, optionsCount));
                    } else {
                        mcCopy.clearOptions(); // dejarlo vacío si hay inconsistencia
                    }
                }

                surveyWithId.addQuestion(mcCopy);
            } else {
                // Pregunta textual o numérica: crear nueva instancia con SURVEY_ID correcto
                Question qCopy = new Question(originalQ.getQuestionIndex(), surveyId);
                qCopy.setQuestionText(originalQ.getQuestionText());
                qCopy.setTypeQuestion(originalQ.getTypeQuestion());
                qCopy.setRequired(originalQ.isRequired());
                surveyWithId.addQuestion(qCopy);
            }
        }

        // Guardar en el repositorio
        surveyRepository.addSurvey(surveyWithId);

        // Registrar la encuesta creada en el usuario
        try {
            userService.addSurveyCreated(surveyWithId.getCREATOR_USERNAME(), surveyWithId.getSURVEY_ID());
        } catch (Exception e) {
            System.err.println("[WARNING] Error al registrar la encuesta " + surveyWithId.getSURVEY_ID() +
                             " en el usuario " + surveyWithId.getCREATOR_USERNAME() + ": " + e.getMessage());
            // La encuesta se crea igual, solo se registra el warning en logs
        }

        return surveyWithId;
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
        return surveyRepository.getAllSurveys();
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
    // CRUD Completo: UPDATE, DELETE, GET BY USER, PUBLISH
    // ───────────────────────────────────────────────

    /**
     * Actualiza una encuesta existente (solo si es borrador).
     *
     * Valida que:
     * - La encuesta exista
     * - El usuario actual sea el propietario
     * - La encuesta esté en estado DRAFT (solo se pueden editar borradores)
     * - Los cambios se sincronicen con BD y JSON
     *
     * @param surveyId identificador de la encuesta a actualizar
     * @param updatedSurvey encuesta con los cambios
     * @return encuesta actualizada
     * @throws SurveyException si la encuesta no existe, no es del usuario, o no está en DRAFT
     */
    public Survey updateSurvey(String surveyId, Survey updatedSurvey) {
        checkUserLoggedin();
        checkSurveyExists(surveyId);

        Survey existingSurvey = surveyRepository.getSurvey(surveyId);

        // Validar que el usuario es el propietario
        if (!existingSurvey.getCREATOR_USERNAME().equals(userController.getUsernameLoggedIn())) {
            throw new SurveyException("No tienes permisos para editar esta encuesta.");
        }

        // Validar que la encuesta está en DRAFT
        if (!existingSurvey.getSurveyStatus().equals(domain.model.enums.SurveyStatus.DRAFT)) {
            throw new SurveyException("Solo se pueden editar encuestas en estado borrador.");
        }

        // Validar cambios
        if (updatedSurvey.getTitle() == null || updatedSurvey.getTitle().trim().isEmpty()) {
            throw new SurveyException("El título no puede estar vacío.");
        }
        if (updatedSurvey.getDescription() == null || updatedSurvey.getDescription().trim().isEmpty()) {
            throw new SurveyException("La descripción no puede estar vacía.");
        }

        // Actualizar los campos básicos
        existingSurvey.setTitle(updatedSurvey.getTitle());
        existingSurvey.setDescription(updatedSurvey.getDescription());

        // --- NUEVO: sincronizar la lista completa de preguntas desde updatedSurvey ---
        // Reemplazar las preguntas de la encuesta existente por las de updatedSurvey (copia profunda)
        // Hacer primero una copia profunda de las preguntas recibidas PARA EVITAR problemas
        // en el caso en que updatedSurvey sea la misma instancia que existingSurvey.
        List<Question> updatedQuestions = new ArrayList<>();
        if (updatedSurvey.getQuestions() != null) {
            for (Question q : updatedSurvey.getQuestions()) {
                // Crear copia superficial que luego se convertirá en instancia válida
                if (q.getTypeQuestion() == domain.model.enums.TypeQuestion.MULTIPLE_CHOICE) {
                    domain.model.MultipleChoiceQuestion origMc = (domain.model.MultipleChoiceQuestion) q;
                    domain.model.MultipleChoiceQuestion mcCopy = new domain.model.MultipleChoiceQuestion(origMc.getQuestionIndex(), surveyId);
                    mcCopy.setQuestionText(origMc.getQuestionText());
                    mcCopy.setRequired(origMc.isRequired());

                    if (origMc.getOptions() != null) {
                        for (int k = 0; k < origMc.getOptions().size(); k++) {
                            domain.model.OptionQuestion opt = origMc.getOption(k);
                            domain.model.OptionQuestion optCopy = new domain.model.OptionQuestion(opt.getQuestionIndex(), surveyId);
                            optCopy.setOptionText(opt.getOptionText());
                            mcCopy.addOption(optCopy);
                        }
                    }

                    try {
                        mcCopy.setMaxSelections(origMc.getMaxSelections());
                        mcCopy.setMinSelections(origMc.getMinSelections());
                    } catch (IllegalArgumentException e) {
                        int optionsCount = mcCopy.getOptions().size();
                        if (optionsCount >= 1) {
                            mcCopy.setMinSelections(1);
                            mcCopy.setMaxSelections(Math.max(1, optionsCount));
                        } else {
                            mcCopy.clearOptions();
                        }
                    }

                    updatedQuestions.add(mcCopy);
                } else {
                    Question qCopy = new Question(q.getQuestionIndex(), surveyId);
                    qCopy.setQuestionText(q.getQuestionText());
                    qCopy.setTypeQuestion(q.getTypeQuestion());
                    qCopy.setRequired(q.isRequired());
                    updatedQuestions.add(qCopy);
                }
            }
        }

        // Ahora reemplazar las preguntas de existingSurvey por las copias construidas
        existingSurvey.clearQuestions();
        for (Question nq : updatedQuestions) existingSurvey.addQuestion(nq);

         // Guardar cambios (persistir encuesta completa con preguntas y opciones)
         surveyRepository.addSurvey(existingSurvey);

         System.out.println("[LOG] Encuesta " + surveyId + " actualizada por usuario " + userController.getUsernameLoggedIn());

         return existingSurvey;
     }

    /**
     * Elimina una encuesta (solo si es borrador).
     *
     * Valida que:
     * - La encuesta exista
     * - El usuario actual sea el propietario
     * - La encuesta esté en estado DRAFT (solo se pueden eliminar borradores)
     * - Se elimine de BD y del set de encuestas del usuario
     * - Se sincronice el JSON del usuario
     *
     * @param surveyId identificador de la encuesta a eliminar
     * @throws SurveyException si la encuesta no existe, no es del usuario, o no está en DRAFT
     */
    public void deleteSurvey(String surveyId) {
        checkUserLoggedin();
        checkSurveyExists(surveyId);

        Survey survey = surveyRepository.getSurvey(surveyId);

        // Validar que el usuario es el propietario
        if (!survey.getCREATOR_USERNAME().equals(userController.getUsernameLoggedIn())) {
            throw new SurveyException("No tienes permisos para eliminar esta encuesta.");
        }

        // Validar que la encuesta está en DRAFT
        if (!survey.getSurveyStatus().equals(domain.model.enums.SurveyStatus.DRAFT)) {
            throw new SurveyException("Solo se pueden eliminar encuestas en estado borrador.");
        }

        // Eliminar de BD
        surveyRepository.deleteSurvey(surveyId);

        // Eliminar del set de encuestas del usuario
        try {
            userService.removeSurveyCreated(survey.getCREATOR_USERNAME(), surveyId);
        } catch (Exception e) {
            System.err.println("[WARNING] Error al eliminar encuesta del usuario: " + e.getMessage());
        }

        System.out.println("[LOG] Encuesta " + surveyId + " eliminada por usuario " + userController.getUsernameLoggedIn());
    }

    /**
     * Obtiene todas las encuestas creadas por un usuario específico.
     *
     * Retorna solo las encuestas del usuario autenticado actualmente.
     * Las encuestas se ordenan por más reciente primero.
     *
     * @param username nombre de usuario
     * @return lista de encuestas del usuario (ordenadas por fecha descendente)
     * @throws SurveyException si el usuario no tiene encuestas
     */
    public List<Survey> getSurveysByUser(String username) {
        checkUserLoggedin();

        // Validar que el usuario solicitado es el actualmente logueado
        if (!username.equals(userController.getUsernameLoggedIn())) {
            throw new SurveyException("No tienes permisos para ver las encuestas de otro usuario.");
        }

        List<Survey> userSurveys = new ArrayList<>();
        List<String> surveyIds = surveyRepository.getAllSurveysId();

        for (String surveyId : surveyIds) {
            Survey survey = surveyRepository.getSurvey(surveyId);
            if (survey.getCREATOR_USERNAME().equals(username)) {
                userSurveys.add(survey);
            }
        }

        if (userSurveys.isEmpty()) {
            throw new SurveyException("No tienes encuestas creadas.");
        }

        // Ordenar por fecha de creación (más reciente primero)
        userSurveys.sort((s1, s2) -> s2.getCREATED_AT().compareTo(s1.getCREATED_AT()));

        return userSurveys;
    }

    /**
     * Publica una encuesta (cambia de DRAFT a PUBLISHED).
     *
     * Valida que:
     * - La encuesta exista
     * - El usuario actual sea el propietario
     * - La encuesta esté en estado DRAFT
     * - La encuesta tenga al menos una pregunta
     *
     * @param surveyId identificador de la encuesta a publicar
     * @return encuesta publicada
     * @throws SurveyException si la encuesta no puede publicarse
     */
    public Survey publishSurvey(String surveyId) {
        checkUserLoggedin();
        checkSurveyExists(surveyId);

        Survey survey = surveyRepository.getSurvey(surveyId);

        // Validar que el usuario es el propietario
        if (!survey.getCREATOR_USERNAME().equals(userController.getUsernameLoggedIn())) {
            throw new SurveyException("No tienes permisos para publicar esta encuesta.");
        }

        // Validar que está en DRAFT
        if (!survey.getSurveyStatus().equals(domain.model.enums.SurveyStatus.DRAFT)) {
            throw new SurveyException("La encuesta ya ha sido publicada.");
        }

        // Validar que tiene al menos una pregunta
        if (survey.getSize() == 0) {
            throw new SurveyException("No se puede publicar una encuesta sin preguntas.");
        }

        // Cambiar estado a PUBLISHED
        survey.setSurveyStatus(domain.model.enums.SurveyStatus.PUBLISHED);
        survey.setPUBLISHED_AT();

        // Guardar cambios
        surveyRepository.addSurvey(survey);

        System.out.println("[LOG] Encuesta " + surveyId + " publicada por usuario " + userController.getUsernameLoggedIn());

        return survey;
    }

    /**
     * Cierra una encuesta publicada (cambia de PUBLISHED a CLOSED).
     *
     * @param surveyId identificador de la encuesta a cerrar
     * @return encuesta cerrada
     * @throws SurveyException si la encuesta no puede cerrarse
     */
    public Survey closeSurvey(String surveyId) {
        checkUserLoggedin();
        checkSurveyExists(surveyId);

        Survey survey = surveyRepository.getSurvey(surveyId);

        // Validar que el usuario es el propietario
        if (!survey.getCREATOR_USERNAME().equals(userController.getUsernameLoggedIn())) {
            throw new SurveyException("No tienes permisos para cerrar esta encuesta.");
        }

        // Validar que está en PUBLISHED
        if (survey.getSurveyStatus().equals(domain.model.enums.SurveyStatus.CLOSED)) {
            throw new SurveyException("La encuesta ya está cerrada.");
        }

        if (survey.getSurveyStatus().equals(domain.model.enums.SurveyStatus.DRAFT)) {
            throw new SurveyException("No se puede cerrar una encuesta en borrador. Primero debes publicarla.");
        }

        // Cambiar estado a CLOSED
        survey.setSurveyStatus(domain.model.enums.SurveyStatus.CLOSED);

        // Guardar cambios
        surveyRepository.addSurvey(survey);

        System.out.println("[LOG] Encuesta " + surveyId + " cerrada por usuario " + userController.getUsernameLoggedIn());

        return survey;
    }

    // ───────────────────────────────────────────────
    // Manipulación de preguntas
    // ───────────────────────────────────────────────

    /**
     * Elimina una pregunta de una encuesta.
     *
     * @param surveyId identificador de la encuesta
     * @param questionIndex índice de la pregunta a eliminar
     * @throws SurveyException si la encuesta no existe o el índice es inválido
     */
    public void deleteQuestion(String surveyId, int questionIndex) {
        checkSurveyExists(surveyId);
        checkQuestionExists(surveyId, questionIndex);
        surveyRepository.deleteQuestion(surveyId, questionIndex);
    }

    /**
     * Reordena una pregunta en una encuesta.
     *
     * @param surveyId identificador de la encuesta
     * @param oldIndex índice actual de la pregunta
     * @param newIndex nuevo índice para la pregunta
     * @throws SurveyException si la encuesta no existe o los índices son inválidos
     */
    public void reorderQuestion(String surveyId, int oldIndex, int newIndex) {
        checkSurveyExists(surveyId);
        checkQuestionExists(surveyId, oldIndex);
        checkQuestionExists(surveyId, newIndex);
        surveyRepository.swapQuestions(surveyId, oldIndex, newIndex);
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

    /**
     * Añade una valoración a una encuesta.
     * 
     * Este método depende de que la clase {@link Survey} implemente el método
     * {@code addRating(double)}.
     * 
     * @param surveyId identificador de la encuesta
     * @param rating valoración a añadir
     */
    public void addSurveyRating(String surveyId, double rating) {
        Survey survey = getSurvey(surveyId);
        if (survey == null) throw new SurveyException("La encuesta no existe.");

        survey.addRating(rating);
        surveyRepository.updateSurvey(surveyId, survey); // si usas repo persistente
    }
}
