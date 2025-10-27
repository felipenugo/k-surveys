package domain.model;

import domain.model.enums.TypeQuestion;

public class MultipleChoiceAnswer extends Answer {
    // Attributes
    private final int maxOptions;
    private final boolean[] selectedOptions;

    // Constructor
    public MultipleChoiceAnswer(int questionIndex, String responseId, int maxOptions) {
        super(questionIndex, responseId);
        this.maxOptions = maxOptions;
        this.selectedOptions = new boolean[maxOptions];
    }

    // Getters
    @Override
    public TypeQuestion getAnswerType() {
        return TypeQuestion.MULTIPLE_CHOICE;
    }

    public int getMaxOptions() {
        return maxOptions;
    }

    public boolean[] getSelectedOptions() {
        return selectedOptions;
    }

    // boolean[] methods, correct usage must be ensured by the caller

    public boolean inRange(int index) {
        return index >= 0 && index < maxOptions;
    }

    public boolean isOptionSelected(int index) {
        return selectedOptions[index];
    }

    public void setOption(int index) {
        selectedOptions[index] = true;
    }

    public void unsetOption(int index) {
        selectedOptions[index] = false;
    }

    @Override
    public void clearAnswer() {
        for (int i = 0; i < maxOptions; i++) {
            selectedOptions[i] = false;
        }
    }
}
