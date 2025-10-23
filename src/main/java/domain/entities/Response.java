package domain.entities;

import domain.entities.Answer;

import java.util.ArrayList;

public class Response {
    private int id;
    private final int SURVEY_ID;
    private final String RESPONDER_USERNAME;
    //private final int surveySize;
    private ArrayList<Answer> answers;

    // Constructor without id for new responses
    public Response(int surveyId, String responderUsername) {
        this.id = -1; // id will be set when checked from database
        this.SURVEY_ID = surveyId;
        this.RESPONDER_USERNAME = responderUsername;
        //answers = new ArrayList<Answer>(surveySize);
    }

    // Constructor with id checked from database
    public Response(int id, int surveyId, String responderUsername) {
        this.id = id;
        this.SURVEY_ID = surveyId;
        this.RESPONDER_USERNAME = responderUsername;
        //answers = new ArrayList<Answer>(surveySize);
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getSurveyId() {
        return SURVEY_ID;
    }

    public String getResponderUsername() {
        return RESPONDER_USERNAME;
    }

    public Answer getAnswer(int index) {
        return answers.get(index);
    }

    public void addAnswer(Answer answer) {
        answers.add(answer);
    }

    public void modifyAnswer(int index, Answer answer) {
        answers.set(index, answer);
    }
}
