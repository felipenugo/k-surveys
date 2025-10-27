package domain.model;

public class OptionQuestion {
    // Attributes
    private int questionIndex;
    private final String surveyId;
    private String optionText;

    // Constructor
    public OptionQuestion(int questionIndex, String surveyId) {
        this.questionIndex = questionIndex;
        this.surveyId = surveyId;
        this.optionText = "";
    }

    // Getters
    public int getQuestionIndex() {
        return questionIndex;
    }

    public String getSurveyId() {
        return surveyId;
    }

    public String getOptionText() {
        return optionText;
    }

    // Setters
    public void setQuestionIndex(int questionIndex) {
        this.questionIndex = questionIndex;
    }

    public void setOptionText(String optionText) {
        this.optionText = optionText;
    }
}
