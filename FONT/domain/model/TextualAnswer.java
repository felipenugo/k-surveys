package domain.model;

import domain.model.enums.TypeQuestion;

public class TextualAnswer extends Answer {
    private String answerText;

    public TextualAnswer(int questionIndex, String responseId) {
        super(questionIndex, responseId, TypeQuestion.TEXTUAL);
        this.answerText = "";
    }

    public String getAnswerText() {
        return answerText;
    }

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
