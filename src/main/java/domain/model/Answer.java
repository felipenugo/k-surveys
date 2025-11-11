package domain.model;

import domain.model.enums.TypeQuestion;

public abstract class Answer {
    // Attributes
    private final int QUESTION_INDEX;
    private final String RESPONSE_ID;
    private final TypeQuestion typeAnswer;
    private boolean isAnswered;

    // Constructor
    public Answer(int QUESTION_INDEX, String RESPONSE_ID, TypeQuestion typeAnswer) {
        this.QUESTION_INDEX = QUESTION_INDEX;
        this.RESPONSE_ID = RESPONSE_ID;
        this.typeAnswer = typeAnswer; // textual by default
        this.isAnswered = false;
    }

    // Getters
    public int getQUESTION_INDEX() {
        return QUESTION_INDEX;
    }

    public String getResponseId() {
        return RESPONSE_ID;
    }

    public TypeQuestion getTypeAnswer() {
        return typeAnswer;
    }

    public boolean getIsAnswered() {
        return isAnswered;
    }

    // Setters

    public void setIsAnswered(boolean isAnswered) {
        this.isAnswered = isAnswered;
    }

    public abstract void clearAnswer();

}