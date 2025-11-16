package domain.model;

import domain.model.enums.TypeQuestion;
import java.util.ArrayList;
import java.util.List;

public class MultipleChoiceQuestion extends Question {
    private int minSelections;
    private int maxSelections;
    private List<OptionQuestion> optionQuestions;

    public MultipleChoiceQuestion(int questionIndex, String surveyId) {
        super(questionIndex, surveyId);
        this.setTypeQuestion(TypeQuestion.MULTIPLE_CHOICE);
        this.maxSelections = 1;
        this.minSelections = 1;
        this.optionQuestions = new ArrayList<>();
    }

    public int getMinSelections() { return minSelections; }
    public int getMaxSelections() { return maxSelections; }
    public List<OptionQuestion> getOptions() { return optionQuestions; }
    public int getOptionsSize() { return optionQuestions.size(); }

    public void setMinSelections(int minSelections) {
        if (minSelections < 1) throw new IllegalArgumentException("El mínimo de selecciones debe ser al menos 1.");
        if (minSelections > this.maxSelections) throw new IllegalArgumentException("El mínimo de selecciones no puede ser mayor que el máximo.");
        if (minSelections > this.optionQuestions.size()) throw new IllegalArgumentException("El mínimo de selecciones no puede ser mayor que el número de opciones disponibles.");
        this.minSelections = minSelections;
    }

    public void setMaxSelections(int maxSelections) {
        if (maxSelections < this.minSelections) throw new IllegalArgumentException("El máximo de selecciones no puede ser menor que el mínimo.");
        if (maxSelections > this.optionQuestions.size() && !this.optionQuestions.isEmpty()) throw new IllegalArgumentException("El máximo de selecciones no puede ser mayor que el número de opciones disponibles.");
        this.maxSelections = maxSelections;
    }

    private int getSize() { return optionQuestions.size(); }
    public boolean inRange(int index) { return (index >= 0 && index < getSize()); }
    public void addOption(OptionQuestion optionQuestion) { optionQuestions.add(optionQuestion); }
    public void removeOption(int index) { optionQuestions.remove(index); }
    public void reorderOption(int oldIndex, int newIndex) { OptionQuestion optionQuestion = optionQuestions.get(oldIndex); optionQuestions.remove(oldIndex); optionQuestions.add(newIndex, optionQuestion); }
    public void updateOption(int index, OptionQuestion optionQuestion) { optionQuestions.set(index, optionQuestion); }
    public OptionQuestion getOption(int index) { return optionQuestions.get(index); }
    public void clearOptions() { optionQuestions.clear(); }

    @Override
    public MultipleChoiceQuestion copy() {
        MultipleChoiceQuestion copy = new MultipleChoiceQuestion(this.getQuestionIndex(), this.getSURVEY_ID());
        copy.setQuestionText(this.getQuestionText());
        copy.setRequired(this.isRequired());
        copy.setMaxSelections(this.maxSelections);
        for (OptionQuestion option : this.optionQuestions) {
            OptionQuestion optionCopy = new OptionQuestion(option.getQuestionIndex(), option.getSurveyId());
            optionCopy.setOptionText(option.getOptionText());
            copy.addOption(optionCopy);
        }
        copy.setMinSelections(this.minSelections);
        return copy;
    }
}
