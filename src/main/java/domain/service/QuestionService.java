package domain.service;

import data.QuestionRepository;
import domain.controller.UserController;
import domain.exception.SurveyException;
import domain.model.Question;
import domain.model.MultipleChoiceQuestion;
import domain.model.OptionQuestion;
import domain.model.enums.TypeQuestion;

import java.util.List;

public class QuestionService {
    private final QuestionRepository questionRepository;
    private final UserController userController;

    public QuestionService(QuestionRepository questionRepository, UserController userController) {
        this.questionRepository = questionRepository;
        this.userController = userController;
    }

    /**
     * Crea una nueva pregunta textual
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
     * Crea una nueva pregunta numérica
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
     * Crea una nueva pregunta de opción múltiple
     */
    public MultipleChoiceQuestion createMultipleChoiceQuestion(
            int questionIndex,
            String surveyId,
            String questionText,
            boolean isRequired,
            int minSelections,
            int maxSelections,
            List<String> optionTexts) {

        if (questionText == null || questionText.trim().isEmpty()) {
            throw new SurveyException("El texto de la pregunta no puede estar vacío.");
        }

        if (optionTexts == null || optionTexts.size() < 2) {
            throw new SurveyException("Se requieren al menos 2 opciones.");
        }

        if (minSelections < 1) {
            throw new SurveyException("El mínimo de selecciones debe ser al menos 1.");
        }

        if (maxSelections < minSelections) {
            throw new SurveyException("El máximo de selecciones no puede ser menor que el mínimo.");
        }

        if (maxSelections > optionTexts.size()) {
            throw new SurveyException("El máximo de selecciones no puede ser mayor que el número de opciones.");
        }

        MultipleChoiceQuestion mcQuestion = new MultipleChoiceQuestion(questionIndex, surveyId);
        mcQuestion.setQuestionText(questionText);
        mcQuestion.setRequired(isRequired);
        mcQuestion.setMinSelections(minSelections);
        mcQuestion.setMaxSelections(maxSelections);

        // Añadir opciones
        for (String optionText : optionTexts) {
            if (optionText == null || optionText.trim().isEmpty()) {
                throw new SurveyException("El texto de las opciones no puede estar vacío.");
            }

            OptionQuestion option = new OptionQuestion(questionIndex, surveyId);
            option.setOptionText(optionText);
            mcQuestion.addOption(option);
        }

        return mcQuestion;
    }

    /**
     * Actualiza el texto de una pregunta
     */
    public void updateQuestionText(Question question, String newText) {
        if (newText == null || newText.trim().isEmpty()) {
            throw new SurveyException("El texto de la pregunta no puede estar vacío.");
        }
        question.setQuestionText(newText);
    }

    /**
     * Cambia el estado de obligatoriedad de una pregunta
     */
    public void toggleRequiredStatus(Question question) {
        question.setRequired(!question.isRequired());
    }

    /**
     * Añade una opción a una pregunta de opción múltiple
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
     * Actualiza una opción existente
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
     * Elimina una opción
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
     * Actualiza las selecciones mínimas
     */
    public void updateMinSelections(MultipleChoiceQuestion question, int minSelections) {
        try {
            question.setMinSelections(minSelections);
        } catch (IllegalArgumentException e) {
            throw new SurveyException(e.getMessage());
        }
    }

    /**
     * Actualiza las selecciones máximas
     */
    public void updateMaxSelections(MultipleChoiceQuestion question, int maxSelections) {
        try {
            question.setMaxSelections(maxSelections);
        } catch (IllegalArgumentException e) {
            throw new SurveyException(e.getMessage());
        }
    }
}
