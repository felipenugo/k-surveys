package domain.model;

public class OptionQuestion {
    private int questionIndex;
    private final String surveyId;
    private String optionText;

    public OptionQuestion(int questionIndex, String surveyId) {
        this.questionIndex = questionIndex;
        this.surveyId = surveyId;
        this.optionText = "";
    }

    public int getQuestionIndex() { return questionIndex; }
    public String getSurveyId() { return surveyId; }
    public String getOptionText() { return optionText; }
    public void setQuestionIndex(int questionIndex) { this.questionIndex = questionIndex; }
    public void setOptionText(String optionText) { this.optionText = optionText; }
}
