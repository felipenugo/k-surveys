package domain.model;

import domain.model.enums.TypeQuestion;

public abstract class Answer {
    // Attributes
    private final int QUESTION_INDEX;
    private final String RESPONSE_ID;

    // Constructor
    public Answer(int QUESTION_INDEX, String RESPONSE_ID) {
        this.QUESTION_INDEX = QUESTION_INDEX;
        this.RESPONSE_ID = RESPONSE_ID;
    }

    // Getters
    public int getQUESTION_INDEX() {
        return QUESTION_INDEX;
    }

    public String getResponseId() {
        return RESPONSE_ID;
    }

    public abstract TypeQuestion getAnswerType();

    public abstract void clearAnswer();

}