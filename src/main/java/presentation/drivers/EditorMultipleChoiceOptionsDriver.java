package presentation.drivers;

import domain.model.MultipleChoiceQuestion;
import domain.model.OptionQuestion;

import java.util.Scanner;

/**
 * Driver especializado en la gestión de opciones para preguntas de opción múltiple.
 * Este driver se encarga exclusivamente de las operaciones relacionadas con las opciones.
 */
public class EditorMultipleChoiceOptionsDriver {
    private final Scanner sc = new Scanner(System.in);

    private int selectEditOptionsMenuOption() {
        System.out.println("\n--- EDITAR OPCIONES ---");
        System.out.println("1. AÑADIR OPCIÓN");
        System.out.println("2. EDITAR OPCIÓN");
        System.out.println("3. ELIMINAR OPCIÓN");
        System.out.println("4. REORDENAR OPCIONES");
        System.out.println("5. CAMBIAR NÚMERO DE SELECCIONES MÁXIMAS");
        System.out.println("6. VER TODAS LAS OPCIONES");
        System.out.println("7. VOLVER");
        System.out.print("Opción: ");
        return sc.nextInt();
    }

    /**
     * Configura las opciones iniciales de una pregunta de opción múltiple recién creada
     * @return true si se configuraron correctamente, false si se canceló
     */
    public boolean configureInitialOptions(MultipleChoiceQuestion question) {
        System.out.println("\n--- CONFIGURACIÓN DE OPCIONES ---");

        // Configurar número de selecciones máximas
        System.out.print("¿Cuántas selecciones máximas permitirás? (por defecto 1): ");
        int maxSelections = sc.nextInt();

        if (maxSelections < 1) {
            maxSelections = 1;
            System.out.println("Valor no válido. Se establecerá 1 selección máxima.");
        }
        question.setMaxSelections(maxSelections);

        sc.nextLine(); // Limpiar buffer

        // Añadir opciones iniciales
        System.out.print("¿Cuántas opciones deseas añadir? (mínimo 2): ");
        int numOptions = sc.nextInt();
        sc.nextLine(); // Limpiar buffer

        if (numOptions < 2) {
            System.out.println("Se requieren al menos 2 opciones.");
            return false;
        }

        for (int i = 0; i < numOptions; i++) {
            System.out.print("Introduce el texto de la opción " + (i + 1) + ": ");
            String optionText = sc.nextLine();

            if (optionText.trim().isEmpty()) {
                System.out.println("El texto de la opción no puede estar vacío. Intenta de nuevo.");
                i--; // Repetir esta iteración
                continue;
            }

            OptionQuestion option = new OptionQuestion(question.getQuestionIndex(), question.getSURVEY_ID());
            option.setOptionText(optionText);
            question.addOption(option);
        }

        return true;
    }

    /**
     * Menú principal para editar opciones de una pregunta de opción múltiple
     */
    public void editOptionsMenu(MultipleChoiceQuestion question) {
        boolean exitOptionsEditor = false;

        do {
            switch (selectEditOptionsMenuOption()) {
                case 1 -> addOption(question);
                case 2 -> editOption(question);
                case 3 -> deleteOption(question);
                case 4 -> reorderOptions(question);
                case 5 -> changeMaxSelections(question);
                case 6 -> viewAllOptions(question);
                case 7 -> exitOptionsEditor = true;
                default -> System.out.println("Opción no válida.");
            }
        } while (!exitOptionsEditor);
    }

    private void addOption(MultipleChoiceQuestion question) {
        sc.nextLine(); // Limpiar buffer
        System.out.print("\nIntroduce el texto de la nueva opción: ");
        String optionText = sc.nextLine();

        if (optionText.trim().isEmpty()) {
            System.out.println("El texto de la opción no puede estar vacío.");
            return;
        }

        OptionQuestion option = new OptionQuestion(question.getQuestionIndex(), question.getSURVEY_ID());
        option.setOptionText(optionText);
        question.addOption(option);

        System.out.println("Opción añadida en posición " + (question.getOptions().size() - 1) + ".");
    }

    private void editOption(MultipleChoiceQuestion question) {
        if (question.getOptions().isEmpty()) {
            System.out.println("\nNo hay opciones para editar.");
            return;
        }

        viewAllOptions(question);
        System.out.print("\nIntroduce el índice de la opción a editar (0-" + (question.getOptions().size() - 1) + "): ");
        int index = sc.nextInt();
        sc.nextLine(); // Limpiar buffer

        if (!question.inRange(index)) {
            System.out.println("Índice no válido.");
            return;
        }

        OptionQuestion option = question.getOption(index);
        System.out.println("Texto actual: " + option.getOptionText());
        System.out.print("Introduce el nuevo texto: ");
        String newText = sc.nextLine();

        if (newText.trim().isEmpty()) {
            System.out.println("El texto no puede estar vacío.");
            return;
        }

        option.setOptionText(newText);
        question.updateOption(index, option);

        System.out.println("Opción actualizada.");
    }

    private void deleteOption(MultipleChoiceQuestion question) {
        if (question.getOptions().size() <= 2) {
            System.out.println("\nNo puedes eliminar más opciones. Se requieren al menos 2.");
            return;
        }

        viewAllOptions(question);
        System.out.print("\nIntroduce el índice de la opción a eliminar (0-" + (question.getOptions().size() - 1) + "): ");
        int index = sc.nextInt();
        sc.nextLine(); // Limpiar buffer

        if (!question.inRange(index)) {
            System.out.println("Índice no válido.");
            return;
        }

        System.out.print("¿Estás seguro de que deseas eliminar esta opción? (S/N): ");
        String confirmation = sc.nextLine();

        if (confirmation.equalsIgnoreCase("S")) {
            question.removeOption(index);
            System.out.println("Opción eliminada.");
        } else {
            System.out.println("Operación cancelada.");
        }
    }

    private void reorderOptions(MultipleChoiceQuestion question) {
        if (question.getOptions().size() < 2) {
            System.out.println("\nSe necesitan al menos 2 opciones para reordenar.");
            return;
        }

        viewAllOptions(question);
        System.out.print("\nIntroduce el índice de la opción a mover: ");
        int oldIndex = sc.nextInt();
        System.out.print("Introduce la nueva posición: ");
        int newIndex = sc.nextInt();

        if (!question.inRange(oldIndex) || !question.inRange(newIndex)) {
            System.out.println("Índices no válidos.");
            return;
        }

        if (oldIndex == newIndex) {
            System.out.println("La opción ya está en esa posición.");
            return;
        }

        question.reorderOption(oldIndex, newIndex);
        System.out.println("pción movida de posición " + oldIndex + " a posición " + newIndex + ".");
    }

    private void changeMaxSelections(MultipleChoiceQuestion question) {
        System.out.println("\nSelecciones máximas actuales: " + question.getMaxSelections());
        System.out.print("Introduce el nuevo número de selecciones máximas (1-" + question.getOptions().size() + "): ");
        int maxSelections = sc.nextInt();

        if (maxSelections < 1 || maxSelections > question.getOptions().size()) {
            System.out.println("Valor no válido. Debe estar entre 1 y " + question.getOptions().size() + ".");
            return;
        }

        question.setMaxSelections(maxSelections);
        System.out.println("✓ Selecciones máximas actualizadas a " + maxSelections + ".");
    }

    private void viewAllOptions(MultipleChoiceQuestion question) {
        System.out.println("\n========================================");
        System.out.println("       OPCIONES DE LA PREGUNTA");
        System.out.println("========================================");
        for (int i = 0; i < question.getOptions().size(); i++) {
            System.out.println("[" + i + "] " + question.getOptions().get(i).getOptionText());
        }
        System.out.println("========================================");
    }
}
