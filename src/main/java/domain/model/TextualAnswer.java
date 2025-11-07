package domain.model;

import domain.model.enums.TypeQuestion;

public class TextualAnswer extends Answer {
    // Attributes
    private String answerText;

    public TextualAnswer(int questionIndex, String responseId, String answerText) {
        super(questionIndex, responseId); // Llama al constructor del padre
        this.answerText = answerText;
    }

    // Getters
    @Override
    public TypeQuestion getAnswerType() {
        return TypeQuestion.TEXTUAL;
    }

    public String getAnswerText() {
        return answerText;
    }

    // Setters
    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }

    @Override
    public void clearAnswer() {
        this.answerText = "";
    }
}
