package domain.model;

import domain.model.enums.TypeQuestion;

public class Question {
    private int questionIndex;
    private final String SURVEY_ID;
    private TypeQuestion typeQuestion;
    private String questionText;
    private boolean isRequired;

    public Question(int questionIndex, String SURVEY_ID) {
        this.questionIndex = questionIndex;
        this.SURVEY_ID = SURVEY_ID;
        this.typeQuestion = TypeQuestion.TEXTUAL;
        this.questionText = "";
        this.isRequired = true;
    }

    public int getQuestionIndex() { return questionIndex; }
    public String getSURVEY_ID() { return SURVEY_ID; }
    public TypeQuestion getTypeQuestion() { return typeQuestion; }
    public String getQuestionText() { return questionText; }
    public boolean isRequired() { return isRequired; }

    public void setQuestionIndex(int questionIndex) { this.questionIndex = questionIndex; }
    public void setTypeQuestion(TypeQuestion typeQuestion) { this.typeQuestion = typeQuestion; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }
    public void setRequired(boolean required) { this.isRequired = required; }

    public Question copy() {
        Question copy = new Question(this.questionIndex, this.SURVEY_ID);
        copy.setQuestionText(this.questionText);
        copy.setTypeQuestion(this.typeQuestion);
        copy.setRequired(this.isRequired);
        return copy;
    }
}
