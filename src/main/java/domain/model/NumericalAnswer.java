package domain.model;

import domain.model.enums.TypeQuestion;

public class NumericalAnswer extends Answer {
    // Attributes
    private double answerNum;

    public NumericalAnswer(int questionIndex, String responseId) {
        super(questionIndex, responseId, TypeQuestion.NUMERICAL);
        this.answerNum = -1;
    }

    // Getters

    public double getAnswerNum() {
        return answerNum;
    }

    //Setters
    public void setAnswerNum(double answerNum) {
        this.answerNum = answerNum;
        super.setIsAnswered(true);
    }

    @Override
    public void clearAnswer() {
        super.setIsAnswered(false);
    }
}
