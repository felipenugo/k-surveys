package domain.service;

import data.QuestionRepository;
import domain.controller.UserController;
import domain.exception.SurveyException;
import domain.model.Question;
import domain.model.MultipleChoiceQuestion;
import domain.model.OptionQuestion;
import domain.model.enums.TypeQuestion;

import java.util.List;
/**
 * Servicio responsable de gestionar la lógica de negocio relacionada con la creación
 * y modificación de preguntas dentro de una encuesta.
 *
 * Este servicio permite crear preguntas de distintos tipos (textuales, numéricas
 * y de opción múltiple) y actualizar sus atributos siguiendo las reglas establecidas.
 * También se encarga de validar la corrección de los datos introducidos.
 *
 * Cualquier error de validación o regla incumplida se comunica mediante
 * {@link SurveyException}.
 *
 * El servicio no accede directamente a encuestas ni respuestas: únicamente
 * manipula objetos {@link Question} y {@link MultipleChoiceQuestion}.
 */
public class QuestionService {
    /** Repositorio para almacenar preguntas. */
    private final QuestionRepository questionRepository;
    /** Controlador del usuario para futuras validaciones de permisos. */
    private final UserController userController;

      /**
     * Crea una instancia del servicio de preguntas.
     *
     * @param questionRepository repositorio de preguntas
     * @param userController controlador de usuario (por si se requieren permisos)
     */
    public QuestionService(QuestionRepository questionRepository, UserController userController) {
        this.questionRepository = questionRepository;
        this.userController = userController;
    }

   // ───────────────────────────────────────────────
    // Creación de preguntas
    // ───────────────────────────────────────────────

    /**
     * Crea una nueva pregunta de tipo textual.
     *
     * @param questionIndex índice de la pregunta dentro de la encuesta
     * @param surveyId identificador de la encuesta
     * @param questionText texto de la pregunta
     * @param isRequired indica si la pregunta es obligatoria
     * @return la pregunta creada
     * @throws SurveyException si el texto está vacío
     */
    public Question createTextualQuestion(int questionIndex, String surveyId, String questionText, boolean isRequired) {
        if (questionText == null || questionText.trim().isEmpty()) {
            throw new SurveyException("El texto de la pregunta no puede estar vacío.");
        }

        Question question = new Question(questionIndex, surveyId);
        question.setQuestionText(questionText);
        question.setTypeQuestion(TypeQuestion.TEXTUAL);
        question.setRequired(isRequired);

        return question;
    }

    /**
     * Crea una nueva pregunta numérica.
     *
     * @param questionIndex índice de la pregunta dentro de la encuesta
     * @param surveyId identificador de la encuesta
     * @param questionText texto de la pregunta
     * @param isRequired indica si la pregunta es obligatoria
     * @return la pregunta creada
     * @throws SurveyException si el texto está vacío
     */
    public Question createNumericalQuestion(int questionIndex, String surveyId, String questionText, boolean isRequired) {
        if (questionText == null || questionText.trim().isEmpty()) {
            throw new SurveyException("El texto de la pregunta no puede estar vacío.");
        }

        Question question = new Question(questionIndex, surveyId);
        question.setQuestionText(questionText);
        question.setTypeQuestion(TypeQuestion.NUMERICAL);
        question.setRequired(isRequired);

        return question;
    }

    /**
     * Crea una nueva pregunta de tipo opción múltiple.
     *
     * Valida:
     * <ul>
     *     <li>Que el texto de la pregunta no esté vacío.</li>
     *     <li>Que existan al menos dos opciones.</li>
     *     <li>Que el texto de cada opción sea válido.</li>
     *     <li>Que los valores minSelections y maxSelections sean coherentes:</li>
     *     <ul>
     *         <li>minSelections ≥ 1</li>
     *         <li>maxSelections ≥ minSelections</li>
     *         <li>Ambos ≤ número de opciones</li>
     *     </ul>
     * </ul>
     *
     * @param questionIndex índice dentro de la encuesta
     * @param surveyId id de la encuesta
     * @param questionText texto de la pregunta
     * @param isRequired si es obligatoria
     * @param minSelections número mínimo de selecciones permitidas
     * @param maxSelections número máximo de selecciones permitidas
     * @param optionTexts lista de textos de las opciones
     * @return objeto {@link MultipleChoiceQuestion} completo
     * @throws SurveyException si algún valor es inválido
     */
    public MultipleChoiceQuestion createMultipleChoiceQuestion(
            int questionIndex,
            String surveyId,
            String questionText,
            boolean isRequired,
            int minSelections,
            int maxSelections,
            List<String> optionTexts) {

        // 1. VALIDACIONES DE DATOS DE ENTRADA (antes de crear el objeto)
        if (questionText == null || questionText.trim().isEmpty()) {
            throw new SurveyException("El texto de la pregunta no puede estar vacío.");
        }

        if (optionTexts == null || optionTexts.size() < 2) {
            throw new SurveyException("Se requieren al menos 2 opciones.");
        }

        // Validar que las opciones no estén vacías
        for (String optionText : optionTexts) {
            if (optionText == null || optionText.trim().isEmpty()) {
                throw new SurveyException("El texto de las opciones no puede estar vacío.");
            }
        }

        // 2. VALIDACIONES DE SELECCIONES (con respecto al número de opciones)
        if (minSelections < 1) {
            throw new SurveyException("El mínimo de selecciones debe ser al menos 1.");
        }

        if (maxSelections < minSelections) {
            throw new SurveyException("El máximo de selecciones no puede ser menor que el mínimo.");
        }

        if (minSelections > optionTexts.size()) {
            throw new SurveyException("El mínimo de selecciones (" + minSelections + ") no puede ser mayor que el número de opciones (" + optionTexts.size() + ").");
        }

        if (maxSelections > optionTexts.size()) {
            throw new SurveyException("El máximo de selecciones (" + maxSelections + ") no puede ser mayor que el número de opciones (" + optionTexts.size() + ").");
        }

        // 3. CREAR EL OBJETO (ahora sabemos que los datos son válidos)
        MultipleChoiceQuestion mcQuestion = new MultipleChoiceQuestion(questionIndex, surveyId);
        mcQuestion.setQuestionText(questionText);
        mcQuestion.setRequired(isRequired);

        // 4. AÑADIR OPCIONES PRIMERO
        for (String optionText : optionTexts) {
            OptionQuestion option = new OptionQuestion(questionIndex, surveyId);
            option.setOptionText(optionText);
            mcQuestion.addOption(option);
        }

        // 5. CONFIGURAR SELECCIONES (después de añadir las opciones)
        mcQuestion.setMaxSelections(maxSelections);
        mcQuestion.setMinSelections(minSelections);

        return mcQuestion;
    }

    // ───────────────────────────────────────────────
    // Modificaciones de preguntas
    // ───────────────────────────────────────────────

    /**
     * Actualiza el texto de una pregunta.
     *
     * @param question pregunta a modificar
     * @param newText nuevo texto
     * @throws SurveyException si el texto es inválido
     */
    public void updateQuestionText(Question question, String newText) {
        if (newText == null || newText.trim().isEmpty()) {
            throw new SurveyException("El texto de la pregunta no puede estar vacío.");
        }
        question.setQuestionText(newText);
    }

     /**
     * Alterna el estado de obligatoriedad de una pregunta.
     *
     * @param question pregunta a modificar
     */
    public void toggleRequiredStatus(Question question) {
        question.setRequired(!question.isRequired());
    }

    /**
     * Añade una opción a una pregunta de opción múltiple/**
     * Añade una nueva opción a una pregunta de tipo Multiple Choice.
     *
     * @param question pregunta de opción múltiple
     * @param optionText texto de la nueva opción
     * @throws SurveyException si el texto es inválido
     */
    public void addOption(MultipleChoiceQuestion question, String optionText) {
        if (optionText == null || optionText.trim().isEmpty()) {
            throw new SurveyException("El texto de la opción no puede estar vacío.");
        }

        OptionQuestion option = new OptionQuestion(question.getQuestionIndex(), question.getSURVEY_ID());
        option.setOptionText(optionText);
        question.addOption(option);
    }

    /**
     * Actualiza el texto de una opción existente.
     *
     * @param question pregunta a modificar
     * @param index índice de opción
     * @param newText nuevo texto
     * @throws SurveyException si el índice es inválido o el texto vacío
     */
    public void updateOption(MultipleChoiceQuestion question, int index, String newText) {
        if (!question.inRange(index)) {
            throw new SurveyException("Índice de opción no válido.");
        }

        if (newText == null || newText.trim().isEmpty()) {
            throw new SurveyException("El texto de la opción no puede estar vacío.");
        }

        OptionQuestion option = question.getOption(index);
        option.setOptionText(newText);
        question.updateOption(index, option);
    }

    /**
     * Elimina una opción de una pregunta de opción múltiple.
     *
     * Validaciones:
     * <ul>
     *     <li>No se pueden tener menos de 2 opciones.</li>
     *     <li>No se pueden tener menos opciones que minSelections.</li>
     *     <li>El índice debe ser válido.</li>
     * </ul>
     *
     * @param question pregunta a modificar
     * @param index índice de opción a eliminar
     */
    public void removeOption(MultipleChoiceQuestion question, int index) {
        if (question.getOptions().size() <= 2) {
            throw new SurveyException("No puedes eliminar más opciones. Se requieren al menos 2.");
        }

        if (question.getOptions().size() <= question.getMinSelections()) {
            throw new SurveyException("El número de opciones no puede ser menor que las selecciones mínimas requeridas.");
        }

        if (!question.inRange(index)) {
            throw new SurveyException("Índice de opción no válido.");
        }

        question.removeOption(index);
    }

    /**
     * Actualiza el mínimo de selecciones permitidas.
     *
     * @throws SurveyException si el valor es inválido
     */
    public void updateMinSelections(MultipleChoiceQuestion question, int minSelections) {
        try {
            question.setMinSelections(minSelections);
        } catch (IllegalArgumentException e) {
            throw new SurveyException(e.getMessage());
        }
    }

    /**
     * Actualiza el máximo de selecciones permitidas.
     *
     * @throws SurveyException si el valor es inválido
     */
    public void updateMaxSelections(MultipleChoiceQuestion question, int maxSelections) {
        try {
            question.setMaxSelections(maxSelections);
        } catch (IllegalArgumentException e) {
            throw new SurveyException(e.getMessage());
        }
    }
}
