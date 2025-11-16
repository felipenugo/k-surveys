package domain.model;

import domain.model.enums.TypeQuestion;

public class MultipleChoiceAnswer extends Answer {
    private final int maxOptions;
    private final boolean[] selectedOptions;

    public MultipleChoiceAnswer(int questionIndex, String responseId, int maxOptions) {
        super(questionIndex, responseId, TypeQuestion.MULTIPLE_CHOICE);
        this.maxOptions = maxOptions;
        this.selectedOptions = new boolean[maxOptions];
    }

    public int getMaxOptions() {
        return maxOptions;
    }

    public boolean[] getSelectedOptions() {
        return selectedOptions;
    }

    public boolean inRange(int index) {
        return index >= 0 && index < maxOptions;
    }

    public boolean isOptionSelected(int index) {
        return selectedOptions[index];
    }

    public void setOption(int index, boolean option) {
        selectedOptions[index] = option;
        super.setIsAnswered(true);
    }

    public void setOptions(boolean[] options) {
        for (int i = 0; i < selectedOptions.length; i++)
            setOption(i, options[i]);
    }

    @Override
    public void clearAnswer() {
        for (int i = 0; i < maxOptions; i++) {
            selectedOptions[i] = false;
        }
        super.setIsAnswered(false);
    }
}
