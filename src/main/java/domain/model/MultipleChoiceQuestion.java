package domain.model;

import domain.model.enums.TypeQuestion;

import java.util.ArrayList;
import java.util.List;

public class MultipleChoiceQuestion extends Question {
    private int minSelections;
    private int maxSelections;
    private List<OptionQuestion> optionQuestions;

    // we will use the constructor of the parent class Question and add the maxSelections attribute
    public MultipleChoiceQuestion(int questionIndex, String surveyId) {
        super(questionIndex, surveyId);
        this.setTypeQuestion(TypeQuestion.MULTIPLE_CHOICE);
        this.maxSelections = 1; // default value
        this.minSelections = 1; // default value
        this.optionQuestions = new ArrayList<>();
    }

    // Getters

    public int getMinSelections() { return minSelections; }

    public int getMaxSelections() {
        return maxSelections;
    }

    public List<OptionQuestion> getOptions() {
        return optionQuestions;
    }

    public int getOptionsSize(){return optionQuestions.size();}

    // Setters
    public void setMinSelections(int minSelections) {
        if (minSelections < 1) {
            throw new IllegalArgumentException("El mínimo de selecciones debe ser al menos 1.");
        }
        if (minSelections > this.maxSelections) {
            throw new IllegalArgumentException("El mínimo de selecciones no puede ser mayor que el máximo.");
        }
        if (minSelections > this.optionQuestions.size()) {
            throw new IllegalArgumentException("El mínimo de selecciones no puede ser mayor que el número de opciones disponibles.");
        }
        this.minSelections = minSelections;
    }

    public void setMaxSelections(int maxSelections) {
        if (maxSelections < this.minSelections) {
            throw new IllegalArgumentException("El máximo de selecciones no puede ser menor que el mínimo.");
        }
        if (maxSelections > this.optionQuestions.size() && !this.optionQuestions.isEmpty()) {
            throw new IllegalArgumentException("El máximo de selecciones no puede ser mayor que el número de opciones disponibles.");
        }
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


    /**
     * Crea una copia profunda de esta pregunta de opción múltiple
     * Override del método copy() de la clase padre
     * @return Una nueva instancia de MultipleChoiceQuestion con los mismos valores
     */
    @Override
    public MultipleChoiceQuestion copy() {
        MultipleChoiceQuestion copy = new MultipleChoiceQuestion(
                this.getQuestionIndex(),
                this.getSURVEY_ID()
        );

        // Copiar atributos de la clase padre
        copy.setQuestionText(this.getQuestionText());
        copy.setRequired(this.isRequired());

        // Copiar atributos propios
        copy.setMaxSelections(this.maxSelections);

        // Copiar opciones (copia profunda)
        for (OptionQuestion option : this.optionQuestions) {
            OptionQuestion optionCopy = new OptionQuestion(
                    option.getQuestionIndex(),
                    option.getSurveyId()
            );
            optionCopy.setOptionText(option.getOptionText());
            copy.addOption(optionCopy);
        }
        copy.setMinSelections(this.minSelections);

        return copy;
    }
}
