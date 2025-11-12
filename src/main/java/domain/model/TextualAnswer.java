package domain.model;

import domain.model.enums.TypeQuestion;

public class TextualAnswer extends Answer {
    // Attributes
    private String answerText;

    public TextualAnswer(int questionIndex, String responseId) {
        super(questionIndex, responseId, TypeQuestion.TEXTUAL); // Llama al constructor del padre
        this.answerText = "";
    }

    // Getters

    public String getAnswerText() {
        return answerText;
    }

    // Setters
    public void setAnswerText(String answerText) {
        this.answerText = answerText;
        super.setIsAnswered(true);
    }

    @Override
    public void clearAnswer() {
        this.answerText = "";
        super.setIsAnswered(false);
    }
}
