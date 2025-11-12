package domain.model;

import domain.model.enums.TypeQuestion;

import java.util.ArrayList;
import java.util.List;

public class MultipleChoiceQuestion extends Question {

    private int maxSelections;
    private List<OptionQuestion> optionQuestions;

    // we will use the constructor of the parent class Question and add the maxSelections attribute
    public MultipleChoiceQuestion(int questionIndex, String surveyId) {
        super(questionIndex, surveyId);
        this.setTypeQuestion(TypeQuestion.MULTIPLE_CHOICE);
        this.maxSelections = 1; // default value
        this.optionQuestions = new ArrayList<>();
    }

    // Getters
    public int getMaxSelections() {
        return maxSelections;
    }

    public List<OptionQuestion> getOptions() {
        return optionQuestions;
    }

    public int getOptionsSize(){return optionQuestions.size();}

    // Setters
    public void setMaxSelections(int maxSelections) {
        this.maxSelections = maxSelections;
    }

    // List<Options> methods, correct usage must be ensured by the caller

    // private getter
    private int getSize() {
        return optionQuestions.size();
    }

    public boolean inRange(int index) {
        return (index >= 0 && index < getSize());
    }

    public void addOption(OptionQuestion optionQuestion) {
        optionQuestions.add(optionQuestion);
    }

    public void removeOption(int index) {
        optionQuestions.remove(index);
    }

    public void reorderOption(int oldIndex, int newIndex) {
        OptionQuestion optionQuestion = optionQuestions.get(oldIndex);
        optionQuestions.remove(oldIndex);
        optionQuestions.add(newIndex, optionQuestion);
    }

    public void updateOption(int index, OptionQuestion optionQuestion) {
        optionQuestions.set(index, optionQuestion);
    }

    public OptionQuestion getOption(int index) {
        return optionQuestions.get(index);
    }

    public void clearOptions() {
        optionQuestions.clear();
    }
}
