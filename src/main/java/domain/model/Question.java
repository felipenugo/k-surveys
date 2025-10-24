package domain.model;

import java.lang.reflect.Type;

public class Question {
    private int questionId; // identifier for question
    private int surveyId; // identifier to the survey to which it belongs
    private TypeQuestion typeQuestion; // can be MULTIPLE_CHOICE or TEXTUAL
    private String questionText;

    // Constructor without id for new questions
    public Question( int surveyId) {
        this.questionId = -1; // id will be set when checked from database
        this.surveyId = surveyId;
        this.typeQuestion = TypeQuestion.TEXTUAL; // textual by default, can be changed later
        this.questionText = "";
    }

    // Constructor with id checked from database
    public Question(int questionId, int surveyId) {
        this.questionId = questionId;
        this.surveyId = surveyId;
        this.typeQuestion = TypeQuestion.TEXTUAL; // textual by default, can be changed later

        this.questionText = "";
    }

    // Getters
    public int getQuestionId() {
        return questionId;
    }

    public int getSurveyId() {
        return surveyId;
    }

    public TypeQuestion getTypeQuestion() {
        return typeQuestion;
    }

    public String getQuestionText() {
        return questionText;
    }

    // Setters
    public void setTypeQuestion(TypeQuestion typeQuestion) {
        this.typeQuestion = typeQuestion;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }
}
