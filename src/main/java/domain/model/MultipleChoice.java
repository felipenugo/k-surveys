package domain.model;

public class MultipleChoice extends Question {

    private int maxChoices;

    // we will use the contructor of the parent class Question and add the maxChoices attribute
    public MultipleChoice(int surveyId) {
        super(surveyId);
        this.maxChoices = 1; // default value
    }

    // Getters
    public int getMaxChoices() {
        return maxChoices;
    }

    // Setters
    public void setMaxChoices(int maxChoices) {
        this.maxChoices = maxChoices; // if maxChoices is 1 this method won't be called
    }
}
