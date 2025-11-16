package domain.model;

import domain.model.enums.TypeQuestion;

import java.util.ArrayList;
import java.util.List;
/**
 * Representa una pregunta de tipo elección múltiple dentro de una encuesta.
 * 
 * Cada pregunta de este tipo contiene un conjunto de opciones posibles 
 * ({@link OptionQuestion}) y puede limitar el número máximo de selecciones
 * permitidas por el usuario.
 * 
 * Hereda de la clase base {@link Question} e inicializa su tipo como
 * {@link TypeQuestion#MULTIPLE_CHOICE}.
 */
public class MultipleChoiceQuestion extends Question {
    /** Número mínimo de opciones que el usuario debe seleccionar. */
    private int minSelections;
    /** Número máximo de opciones que el usuario puede seleccionar. */
    private int maxSelections;
    /** Lista de opciones asociadas a la pregunta. */
    private List<OptionQuestion> optionQuestions;

    // ───────────────────────────────────────────────
    // Constructor
    // ───────────────────────────────────────────────

    /**
     * Crea una nueva pregunta de tipo elección múltiple.
     * 
     * Por defecto, el número máximo de selecciones permitidas es 1.
     *
     * @param questionIndex índice de la pregunta dentro de la encuesta
     * @param surveyId      identificador de la encuesta a la que pertenece
     */
    public MultipleChoiceQuestion(int questionIndex, String surveyId) {
        super(questionIndex, surveyId);
        this.setTypeQuestion(TypeQuestion.MULTIPLE_CHOICE);
        this.maxSelections = 1; // default value
        this.minSelections = 1; // default value
        this.optionQuestions = new ArrayList<>();
    }

    // ───────────────────────────────────────────────
    // Getters
    // ───────────────────────────────────────────────

    /**
     * Devuelve el número mínimpo de opciones que el usuario puede seleccionar.
     *
     * @return número mínimo de selecciones
     */

    public int getMinSelections() { return minSelections; }
    /**
     * Devuelve el número máximo de opciones que el usuario puede seleccionar.
     *
     * @return número máximo de selecciones
     */
    public int getMaxSelections() {
        return maxSelections;
    }

    /**
     * Devuelve la lista de opciones asociadas a la pregunta.
     *
     * @return lista de opciones
     */
    public List<OptionQuestion> getOptions() {
        return optionQuestions;
    }

    /**
     * Devuelve el número de opciones asociadas a la pregunta.
     *
     * @return número de opciones
     */
    public int getOptionsSize(){return optionQuestions.size();}

    // ───────────────────────────────────────────────
    // Setters
    // ───────────────────────────────────────────────

    /**
     * Establece el número mínimo de opciones que el usuario puede seleccionar.
     *
     * @param minSelections nuevo número mínimo de selecciones
     */
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

    /**
     * Establece el número máximo de opciones que el usuario puede seleccionar.
     *
     * @param maxSelections nuevo número máximo de selecciones
     */
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

    // ───────────────────────────────────────────────
    // Métodos auxiliares de gestión de opciones
    // ───────────────────────────────────────────────

    /**
     * Devuelve el número total de opciones asociadas a esta pregunta.
     *
     * @return número de opciones disponibles
     */
    private int getSize() {
        return optionQuestions.size();
    }

    /**
     * Comprueba si un índice dado está dentro del rango válido de opciones.
     *
     * @param index índice a comprobar
     * @return true si el índice es válido, false en caso contrario
     */
    public boolean inRange(int index) {
        return (index >= 0 && index < getSize());
    }

    /**
     * Añade una nueva opción a la lista de opciones de la pregunta.
     *
     * @param optionQuestion opción a añadir
     */
    public void addOption(OptionQuestion optionQuestion) {
        optionQuestions.add(optionQuestion);
    }

    /**
     * Elimina una opción de la lista de opciones de la pregunta en la posición especificada.
     *
     * @param index índice de la opción a eliminar
     */
    public void removeOption(int index) {
        optionQuestions.remove(index);
    }

    /**
     * Reordena una opción dentro de la lista de opciones de la pregunta.
     *
     * @param oldIndex índice actual de la opción
     * @param newIndex nuevo índice para la opción
     */
    public void reorderOption(int oldIndex, int newIndex) {
        OptionQuestion optionQuestion = optionQuestions.get(oldIndex);
        optionQuestions.remove(oldIndex);
        optionQuestions.add(newIndex, optionQuestion);
    }

    /**
     * Actualiza una opción en la lista de opciones de la pregunta en la posición especificada.
     *
     * @param index índice de la opción a actualizar
     * @param optionQuestion nueva opción para reemplazar la existente
     */
    public void updateOption(int index, OptionQuestion optionQuestion) {
        optionQuestions.set(index, optionQuestion);
    }

    /**
     * Devuelve una opción de la lista de opciones de la pregunta en la posición especificada.
     *
     * @param index índice de la opción a obtener
     * @return opción en la posición especificada
     */
    public OptionQuestion getOption(int index) {
        return optionQuestions.get(index);
    }

    /**
     * Elimina todas las opciones de la lista de opciones de la pregunta.
     */
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
