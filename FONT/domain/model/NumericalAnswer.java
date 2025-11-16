package domain.model;

import domain.model.enums.TypeQuestion;

public class NumericalAnswer extends Answer {
    private Double answerNum;

    public NumericalAnswer(int questionIndex, String responseId) {
        super(questionIndex, responseId, TypeQuestion.NUMERICAL);
        this.answerNum = null;
    }

    public Double getAnswerNum() {
        return answerNum;
    }

    public void setAnswerNum(Double answerNum) {
        this.answerNum = answerNum;
        super.setIsAnswered(answerNum != null);
    }

    @Override
    public void clearAnswer() {
        this.answerNum = null;
        super.setIsAnswered(false);
    }
}
