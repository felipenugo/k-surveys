package domain.controller;

import domain.model.Question;
import domain.model.MultipleChoiceQuestion;
import domain.service.QuestionService;
import java.util.List;

public class QuestionController {
    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    public Question createTextualQuestion(int questionIndex, String surveyId, String questionText, boolean isRequired) {
        return questionService.createTextualQuestion(questionIndex, surveyId, questionText, isRequired);
    }

    public Question createNumericalQuestion(int questionIndex, String surveyId, String questionText, boolean isRequired) {
        return questionService.createNumericalQuestion(questionIndex, surveyId, questionText, isRequired);
    }

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

    public void updateQuestionText(Question question, String newText) { questionService.updateQuestionText(question, newText); }
    public void toggleRequiredStatus(Question question) { questionService.toggleRequiredStatus(question); }
    public void addOption(MultipleChoiceQuestion question, String optionText) { questionService.addOption(question, optionText); }
    public void updateOption(MultipleChoiceQuestion question, int index, String newText) { questionService.updateOption(question, index, newText); }
    public void removeOption(MultipleChoiceQuestion question, int index) { questionService.removeOption(question, index); }
    public void updateMinSelections(MultipleChoiceQuestion question, int minSelections) { questionService.updateMinSelections(question, minSelections); }
    public void updateMaxSelections(MultipleChoiceQuestion question, int maxSelections) { questionService.updateMaxSelections(question, maxSelections); }
}
