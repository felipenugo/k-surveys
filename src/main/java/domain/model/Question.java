package domain.model;

import domain.model.enums.TypeQuestion;

public class Question {
    // Attributes
    private int questionIndex; // identifier with surveyId
    private final String SURVEY_ID; // identifier to the survey to which it belongs
    private TypeQuestion typeQuestion; // can be MULTIPLE_CHOICE or TEXTUAL
    private String questionText;
    private boolean isRequired;

    // Constructor
    public Question(int questionIndex, String SURVEY_ID) {
        this.questionIndex = questionIndex;
        this.SURVEY_ID = SURVEY_ID;
        this.typeQuestion = TypeQuestion.TEXTUAL; // textual by default, can be changed later
        this.questionText = "";
        this.isRequired = true;

    }

    // Getters
    public int getQuestionIndex() {
        return questionIndex;
    }

    public String getSURVEY_ID() {
        return SURVEY_ID;
    }

    public TypeQuestion getTypeQuestion() {
        return typeQuestion;
    }

    public String getQuestionText() {
        return questionText;
    }

    public boolean isRequired() { return isRequired;}

    // Setters

    public void setQuestionIndex(int questionIndex) {
        this.questionIndex = questionIndex;
    }

    public void setTypeQuestion(TypeQuestion typeQuestion) {
        this.typeQuestion = typeQuestion;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public void setRequired(boolean required) { this.isRequired = required;}

    //Clonar
    public Question copy() {
        Question copy = new Question(this.questionIndex, this.SURVEY_ID);
        copy.setQuestionText(this.questionText);
        copy.setTypeQuestion(this.typeQuestion);
        copy.setRequired(this.isRequired);
        return copy;
    }
}
