package domain.model;

import domain.model.enums.ResponseStatus;

import java.time.LocalDateTime;

public class Response {
    // Attributes
    private final String RESPONSE_ID; // response identifier
    private final String SURVEY_ID; // identifies the survey which belongs
    private final String RESPONDER_USERNAME; // identifies the user that response
    private ResponseStatus responseStatus;
    private LocalDateTime SUBMITTED_AT;
    private final Answer[] ANSWERS;

    // Constructor with id checked from database
    public Response(String RESPONSE_ID, String SURVEY_ID, String RESPONDER_USERNAME, ResponseStatus responseStatus, int numQuestions) {
        this.RESPONSE_ID = RESPONSE_ID;
        this.SURVEY_ID = SURVEY_ID;
        this.RESPONDER_USERNAME = RESPONDER_USERNAME;
        this.responseStatus = ResponseStatus.DRAFT;
        SUBMITTED_AT = null;
        ANSWERS = new Answer[numQuestions];
    }

    // Constructor without id for new responses
    public Response(String SURVEY_ID, String RESPONDER_USERNAME, int numQuestions) {
        this.RESPONSE_ID = null;
        this.SURVEY_ID = SURVEY_ID;
        this.RESPONDER_USERNAME = RESPONDER_USERNAME;
        this.responseStatus = ResponseStatus.DRAFT;
        SUBMITTED_AT = null;
        ANSWERS = new Answer[numQuestions];
    }

    // Getters
    public String getRESPONSE_ID() {
        return RESPONSE_ID;
    }

    public String getSurveyId() {
        return SURVEY_ID;
    }

    public String getResponderUsername() {
        return RESPONDER_USERNAME;
    }

    public ResponseStatus getResponseStatus() {
        return responseStatus;
    }

    public LocalDateTime getSUBMITTED_AT() {
        return SUBMITTED_AT;
    }

    public Answer[] getANSWERS() {
        return ANSWERS;
    }

    // Setters
    public void setResponseStatus(ResponseStatus responseStatus) {
        this.responseStatus = responseStatus;
    }

    // this method must be used only when the response is submitted
    public void setSUBMITTED_AT(LocalDateTime SUBMITTED_AT) {
        this.SUBMITTED_AT = SUBMITTED_AT;
    }

    // Answer[] methods, correct usage must be ensured by the caller

    // private getter
    private int getSize() {
        return ANSWERS.length;
    }

    public boolean inRange(int index) {
        return (index >= 0 && index < getSize());
    }

    // can not add or remove answers since the number of questions is fixed
    public void updateAnswer(int index, Answer answer) {
        ANSWERS[index] = answer;
    }

    public void clearAnswer(){};

    public Answer getAnswer(int index) {
        return ANSWERS[index];
    }

}
