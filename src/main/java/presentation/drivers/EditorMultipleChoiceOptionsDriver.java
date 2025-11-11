package presentation.drivers;

import domain.model.MultipleChoiceQuestion;
import domain.model.OptionQuestion;

import java.util.Scanner;

public class EditorMultipleChoiceOptionsDriver {
    private final Scanner sc = new Scanner(System.in);

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

    public boolean configureInitialOptions(MultipleChoiceQuestion question) {
        System.out.println("\n--- CONFIGURACIÓN DE OPCIONES ---");

        // Añadir opciones iniciales primero
        System.out.print("¿Cuántas opciones deseas añadir? (mínimo 2): ");
        int numOptions = sc.nextInt();
        sc.nextLine();

        if (numOptions < 2) {
            System.out.println("Se requieren al menos 2 opciones.");
            return false;
        }

        for (int i = 0; i < numOptions; i++) {
            System.out.print("Introduce el texto de la opción " + (i + 1) + ": ");
            String optionText = sc.nextLine();

            if (optionText.trim().isEmpty()) {
                System.out.println("El texto de la opción no puede estar vacío. Intenta de nuevo.");
                i--;
                continue;
            }

            OptionQuestion option = new OptionQuestion(question.getQuestionIndex(), question.getSURVEY_ID());
            option.setOptionText(optionText);
            question.addOption(option);
        }


        System.out.print("\n¿Cuántas selecciones mínimas se requieren? (por defecto 1): ");
        int minSelections = sc.nextInt();

        if (minSelections < 1) {
            minSelections = 1;
            System.out.println("Valor no válido. Se establecerá 1 selección mínima.");
        }
        if (minSelections > numOptions) {
            minSelections = 1;
            System.out.println("El mínimo no puede ser mayor que el número de opciones. Se establecerá 1.");
        }

        try {
            question.setMinSelections(minSelections);
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
            question.setMinSelections(1);
        }

        // Configurar máximo de selecciones
        System.out.print("¿Cuántas selecciones máximas permitirás? (por defecto 1): ");
        int maxSelections = sc.nextInt();

        if (maxSelections < minSelections) {
            maxSelections = minSelections;
            System.out.println("El máximo no puede ser menor que el mínimo. Se establecerá " + minSelections + ".");
        }
        if (maxSelections > numOptions) {
            maxSelections = numOptions;
            System.out.println("El máximo no puede ser mayor que el número de opciones. Se establecerá " + numOptions + ".");
        }

        try {
            question.setMaxSelections(maxSelections);
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
            question.setMaxSelections(minSelections);
        }

        return true;
    }


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

        // ACTUALIZADO: Verificar que no quedarán menos opciones que minSelections
        if (question.getOptions().size() <= question.getMinSelections()) {
            System.out.println("\nNo puedes eliminar más opciones.");
            System.out.println("El número de opciones no puede ser menor que las selecciones mínimas requeridas (" + question.getMinSelections() + ").");
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
        System.out.println("Opción movida de posición " + oldIndex + " a posición " + newIndex + ".");
    }

    private void changeMinSelections(MultipleChoiceQuestion question) {  // NUEVO
        System.out.println("\nSelecciones mínimas actuales: " + question.getMinSelections());
        System.out.print("Introduce el nuevo número de selecciones mínimas (1-" + question.getMaxSelections() + "): ");
        int minSelections = sc.nextInt();

        try {
            question.setMinSelections(minSelections);
            System.out.println("Selecciones mínimas actualizadas a " + minSelections + ".");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void changeMaxSelections(MultipleChoiceQuestion question) {
        System.out.println("\nSelecciones máximas actuales: " + question.getMaxSelections());
        System.out.print("Introduce el nuevo número de selecciones máximas (" + question.getMinSelections() + "-" + question.getOptions().size() + "): ");
        int maxSelections = sc.nextInt();

        try {
            question.setMaxSelections(maxSelections);
            System.out.println("Selecciones máximas actualizadas a " + maxSelections + ".");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void viewAllOptions(MultipleChoiceQuestion question) {
        System.out.println("\n========================================");
        System.out.println("       OPCIONES DE LA PREGUNTA");
        System.out.println("========================================");
        System.out.println("Selecciones requeridas: " + question.getMinSelections() + "-" + question.getMaxSelections());  // ACTUALIZADO
        System.out.println("----------------------------------------");
        for (int i = 0; i < question.getOptions().size(); i++) {
            System.out.println("[" + i + "] " + question.getOptions().get(i).getOptionText());
        }
        System.out.println("========================================");
    }
}
