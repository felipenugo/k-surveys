package presentation.drivers;

import domain.controller.QuestionController;
import domain.model.MultipleChoiceQuestion;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class EditorMultipleChoiceOptionsDriver {
    private final Scanner sc = new Scanner(System.in);
    private final QuestionController questionController;

    public EditorMultipleChoiceOptionsDriver(QuestionController questionController) {
        this.questionController = questionController;
    }

    private int selectEditOptionsMenuOption() {
        System.out.println("\n--- EDITAR OPCIONES ---");
        System.out.println("1. AÑADIR OPCIÓN");
        System.out.println("2. EDITAR OPCIÓN");
        System.out.println("3. ELIMINAR OPCIÓN");
        System.out.println("4. REORDENAR OPCIONES");
        System.out.println("5. CAMBIAR SELECCIONES MÍNIMAS");
        System.out.println("6. CAMBIAR SELECCIONES MÁXIMAS");
        System.out.println("7. VER TODAS LAS OPCIONES");
        System.out.println("8. VOLVER");
        System.out.print("Opción: ");
        return sc.nextInt();
    }

    /**
     * Crea una pregunta de opción múltiple con opciones iniciales
     */
    public MultipleChoiceQuestion createMultipleChoiceQuestion(
            int questionIndex,
            String surveyId,
            String questionText,
            boolean isRequired) {

        System.out.println("\n--- CONFIGURACIÓN DE OPCIONES ---");

        // Recoger opciones
        System.out.print("¿Cuántas opciones deseas añadir? (mínimo 2): ");
        int numOptions = sc.nextInt();


        if (numOptions < 2) {
            System.out.println("Se requieren al menos 2 opciones.");
            return null;
        }

        List<String> optionTexts = new ArrayList<>();
        for (int i = 0; i < numOptions; i++) {
            System.out.print("Introduce el texto de la opción " + (i + 1) + ": ");
            String optionText = sc.nextLine();

            if (optionText.trim().isEmpty()) {
                System.out.println("El texto de la opción no puede estar vacío. Intenta de nuevo.");
                i--;
                continue;
            }

            optionTexts.add(optionText);
        }

        // Configurar selecciones
        System.out.print("\n¿Cuántas selecciones mínimas se requieren? (por defecto 1): ");
        int minSelections = sc.nextInt();
        if (minSelections < 1) {
            minSelections = 1;
        }

        int maxSelections = numOptions;
        System.out.print("¿Cuántas selecciones máximas permitirás? (por defecto " + (maxSelections) + "): ");
        maxSelections = sc.nextInt();
        if (maxSelections < minSelections) {
            maxSelections = minSelections;
        }

        try {
            // Llamar al controller para crear la pregunta
            return questionController.createMultipleChoiceQuestion(
                    questionIndex, surveyId, questionText, isRequired,
                    minSelections, maxSelections, optionTexts);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }
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
                case 5 -> changeMinSelections(question);
                case 6 -> changeMaxSelections(question);
                case 7 -> viewAllOptions(question);
                case 8 -> exitOptionsEditor = true;
                default -> System.out.println("Opción no válida.");
            }
        } while (!exitOptionsEditor);
    }

    private void addOption(MultipleChoiceQuestion question) {

        System.out.print("\nIntroduce el texto de la nueva opción: ");
        String optionText = sc.nextLine();

        try {
            questionController.addOption(question, optionText);
            System.out.println("Opción añadida en posición " + (question.getOptions().size() - 1) + ".");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void editOption(MultipleChoiceQuestion question) {
        if (question.getOptions().isEmpty()) {
            System.out.println("\nNo hay opciones para editar.");
            return;
        }

        viewAllOptions(question);
        System.out.print("\nIntroduce el índice de la opción a editar (0-" + (question.getOptions().size() - 1) + "): ");
        int index = sc.nextInt();


        if (!question.inRange(index)) {
            System.out.println("Índice no válido.");
            return;
        }

        System.out.println("Texto actual: " + question.getOption(index).getOptionText());
        System.out.print("Introduce el nuevo texto: ");
        String newText = sc.nextLine();

        try {
            questionController.updateOption(question, index, newText);
            System.out.println("Opción actualizada.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void deleteOption(MultipleChoiceQuestion question) {
        if (question.getOptions().size() <= 2) {
            System.out.println("\nNo puedes eliminar más opciones. Se requieren al menos 2.");
            return;
        }

        viewAllOptions(question);
        System.out.print("\nIntroduce el índice de la opción a eliminar (0-" + (question.getOptions().size() - 1) + "): ");
        int index = sc.nextInt();


        if (!question.inRange(index)) {
            System.out.println("Índice no válido.");
            return;
        }

        System.out.print("¿Estás seguro de que deseas eliminar esta opción? (S/N): ");
        String confirmation = sc.nextLine();

        if (confirmation.equalsIgnoreCase("S")) {
            try {
                questionController.removeOption(question, index);
                System.out.println("Opción eliminada.");
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
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
        System.out.println("Opción movida de posición " + oldIndex + " a posición " + newIndex + ".");
    }

    private void changeMinSelections(MultipleChoiceQuestion question) {
        System.out.println("\nSelecciones mínimas actuales: " + question.getMinSelections());
        System.out.print("Introduce el nuevo número de selecciones mínimas (1-" + question.getMaxSelections() + "): ");
        int minSelections = sc.nextInt();

        try {
            questionController.updateMinSelections(question, minSelections);
            System.out.println("Selecciones mínimas actualizadas a " + minSelections + ".");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void changeMaxSelections(MultipleChoiceQuestion question) {
        System.out.println("\nSelecciones máximas actuales: " + question.getMaxSelections());
        System.out.print("Introduce el nuevo número de selecciones máximas (" + question.getMinSelections() + "-" + question.getOptions().size() + "): ");
        int maxSelections = sc.nextInt();

        try {
            questionController.updateMaxSelections(question, maxSelections);
            System.out.println("Selecciones máximas actualizadas a " + maxSelections + ".");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void viewAllOptions(MultipleChoiceQuestion question) {
        System.out.println("\n========================================");
        System.out.println("       OPCIONES DE LA PREGUNTA");
        System.out.println("========================================");
        System.out.println("Selecciones requeridas: " + question.getMinSelections() + "-" + question.getMaxSelections());
        System.out.println("----------------------------------------");
        for (int i = 0; i < question.getOptions().size(); i++) {
            System.out.println("[" + i + "] " + question.getOptions().get(i).getOptionText());
        }
        System.out.println("========================================");
    }
}
