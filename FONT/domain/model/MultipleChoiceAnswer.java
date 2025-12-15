package domain.model;

import domain.model.enums.TypeQuestion;

/**
 * Representa una respuesta de tipo elección múltiple dentro de una encuesta.
 * 
 * Esta clase hereda de {@link Answer} y permite almacenar el estado (seleccionado o no)
 * de cada una de las opciones disponibles en una pregunta de tipo 
 * {@link TypeQuestion#MULTIPLE_CHOICE}.
 * 
 * Cada posición del arreglo {@code selectedOptions} representa una opción de la pregunta,
 * con un valor booleano que indica si fue marcada por el usuario.
 */
public class MultipleChoiceAnswer extends Answer {
    // Attributes
    /** Número máximo de opciones disponibles en la pregunta. */
    private final int maxOptions;
    /** Array que indica qué opciones han sido seleccionadas. */
    private final boolean[] selectedOptions;

    // ───────────────────────────────────────────────
    // Constructor
    // ───────────────────────────────────────────────

    /**
     * Crea una nueva respuesta de tipo elección múltiple.
     * 
     * Inicializa un array booleano de tamaño {@code maxOptions}, 
     * donde cada elemento representa una opción posible.
     *
     * @param questionIndex índice de la pregunta a la que pertenece la respuesta
     * @param responseId    identificador único de la respuesta dentro de la encuesta
     * @param maxOptions    número máximo de opciones disponibles
     */
    public MultipleChoiceAnswer(int questionIndex, String responseId, int maxOptions) {
        super(questionIndex, responseId, TypeQuestion.MULTIPLE_CHOICE);
        this.maxOptions = maxOptions;
        this.selectedOptions = new boolean[maxOptions];
    }

    // ───────────────────────────────────────────────
    // Getters
    // ───────────────────────────────────────────────

    /**
     * Devuelve el número máximo de opciones disponibles en la pregunta.
     *
     * @return número máximo de opciones
     */
    public int getMaxOptions() {
        return maxOptions;
    }

    /**
     * Devuelve el array que indica qué opciones han sido seleccionadas.
     *
     * @return array de opciones seleccionadas
     */
    public boolean[] getSelectedOptions() {
        return selectedOptions;
    }

    // boolean[] methods, correct usage must be ensured by the caller
    
    /**
     * Comprueba si un índice dado está dentro del rango válido de opciones.
     *
     * @param index índice a comprobar
     * @return true si el índice es válido, false en caso contrario
     */
    public boolean inRange(int index) {
        return index >= 0 && index < maxOptions;
    }

    /**
     * Comprueba si una opción específica ha sido seleccionada.
     *
     * @param index índice de la opción a comprobar
     * @return true si la opción está seleccionada, false en caso contrario
     */
    public boolean isOptionSelected(int index) {
        return selectedOptions[index];
    }

    /**
     * Establece el estado de selección de una opción específica.
     *
     * @param index índice de la opción a modificar
     * @param option nuevo estado de selección (true para seleccionada, false para no seleccionada)
     */
    public void setOption(int index, boolean option) {
        selectedOptions[index] = option;
        super.setIsAnswered(true);
    }

    /**
     * Establece el estado de selección de todas las opciones.
     *
     * @param options array con los nuevos estados de selección
     */
    public void setOptions(boolean[] options) {
        for (int i = 0; i < selectedOptions.length; i++)
            setOption(i, options[i]);
    }
    /**
     * Limpia la respuesta, desmarcando todas las opciones.
     */
    @Override
    public void clearAnswer() {
        for (int i = 0; i < maxOptions; i++) {
            selectedOptions[i] = false;
        }
        super.setIsAnswered(false);
    }

    /**
     * Devuelve una representación en cadena de la respuesta.
     *
     * @return representación en cadena de las opciones seleccionadas
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (int i = 0; i < selectedOptions.length; i++) {
            if (selectedOptions[i]) {
                if (!first) sb.append(", ");
                sb.append("Opción ").append(i + 1);
                first = false;
            }
        }
        sb.append("]");
        return first ? "(ninguna opción)" : sb.toString();
    }
}
