package domain.model;

public class Answer {
    private int id;
    private final int RESPONSE_ID;
    private final int QUESTION_ID;
    private String answerText;

    // Constructor without id for new answers
    public Answer(int responseId, int questionId, String answerText) {
        this.id = -1; // id will be set when checked from database
        this.RESPONSE_ID = responseId;
        this.QUESTION_ID = questionId;
        this.answerText = answerText;
    }

    // Constructor with id checked from database
    public Answer(int id, int responseId, int questionId, String answerText) {
        this.id = id;
        this.RESPONSE_ID = responseId;
        this.QUESTION_ID = questionId;
        this.answerText = answerText;
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getResponseId() {
        return RESPONSE_ID;
    }

    public int getQuestionId() {
        return QUESTION_ID;
    }

    public String getAnswerText() {
        return answerText;
    }
}