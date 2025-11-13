package presentation.drivers;

import domain.controller.QuestionController;
import domain.model.Question;
import domain.model.MultipleChoiceQuestion;
import domain.model.enums.TypeQuestion;

import java.util.Scanner;

public class EditorQuestionDriver {
    private final Scanner sc = new Scanner(System.in);
    private final EditorMultipleChoiceOptionsDriver optionsDriver;
    private final QuestionController questionController;

    public EditorQuestionDriver(QuestionController questionController) {
        this.questionController = questionController;
        this.optionsDriver = new EditorMultipleChoiceOptionsDriver(questionController);
    }

    private int selectQuestionTypeOption() {
        System.out.println("\n--- TIPO DE PREGUNTA ---");
        System.out.println("1. PREGUNTA TEXTUAL");
        System.out.println("2. PREGUNTA DE OPCIÓN MÚLTIPLE");
        System.out.println("3. PREGUNTA NUMÉRICA");
        System.out.println("4. CANCELAR");
        System.out.print("Opción: ");
        return sc.nextInt();
    }

    private int selectEditTextualQuestionMenuOption() {
        System.out.println("\n--- EDITAR PREGUNTA TEXTUAL ---");
        System.out.println("1. EDITAR TEXTO DE LA PREGUNTA");
        System.out.println("2. CAMBIAR SI ES OBLIGATORIA");
        System.out.println("3. GUARDAR Y SALIR");
        System.out.print("Opción: ");
        return sc.nextInt();
    }

    private int selectEditNumericalQuestionMenuOption() {
        System.out.println("\n--- EDITAR PREGUNTA NUMÉRICA ---");
        System.out.println("1. EDITAR TEXTO DE LA PREGUNTA");
        System.out.println("2. CAMBIAR SI ES OBLIGATORIA");
        System.out.println("3. GUARDAR Y SALIR");
        System.out.print("Opción: ");
        return sc.nextInt();
    }

    private int selectEditMultipleChoiceQuestionMenuOption() {
        System.out.println("\n--- EDITAR PREGUNTA DE OPCIÓN MÚLTIPLE ---");
        System.out.println("1. EDITAR TEXTO DE LA PREGUNTA");
        System.out.println("2. EDITAR OPCIONES");
        System.out.println("3. CAMBIAR SI ES OBLIGATORIA");
        System.out.println("4. GUARDAR Y SALIR");
        System.out.print("Opción: ");
        return sc.nextInt();
    }

    /**
     * Crea una nueva pregunta según el tipo seleccionado
     */
    public Question createQuestion(String surveyId, int questionIndex) {


        System.out.println("\n========================================");
        System.out.println("       CREAR NUEVA PREGUNTA");
        System.out.println("========================================");
        System.out.print("Introduce el texto de la pregunta: ");
        String questionText = sc.nextLine();

        if (questionText.trim().isEmpty()) {
            System.out.println("El texto de la pregunta no puede estar vacío.");
            return null;
        }

        // Preguntar si es obligatoria
        System.out.print("¿Esta pregunta es obligatoria? (S/N, por defecto S): ");
        String response = sc.nextLine().trim();
        boolean isRequired = response.isEmpty() || response.equalsIgnoreCase("S");

        int typeOption = selectQuestionTypeOption();

        Question question = null;

        try {
            switch (typeOption) {
                case 1 -> {
                    // Pregunta textual - Llamar al controller
                    question = questionController.createTextualQuestion(
                            questionIndex, surveyId, questionText, isRequired);
                    System.out.println("\nPregunta textual creada.");
                }
                case 2 -> {
                    // Pregunta de opción múltiple - Delegar al driver específico
                    question = optionsDriver.createMultipleChoiceQuestion(
                            questionIndex, surveyId, questionText, isRequired);

                    if (question != null) {
                        System.out.println("\nPregunta de opción múltiple creada.");
                    }
                }
                case 3 -> {
                    // Pregunta numérica - Llamar al controller
                    question = questionController.createNumericalQuestion(
                            questionIndex, surveyId, questionText, isRequired);
                    System.out.println("\nPregunta numérica creada.");
                    System.out.println("Los usuarios deberán responder con un número válido.");
                }
                case 4 -> System.out.println("\nCreación cancelada.");
                default -> System.out.println("\nOpción no válida.");
            }
        } catch (Exception e) {
            System.out.println("Error al crear la pregunta: " + e.getMessage());
            return null;
        }

        return question;
    }

    /**
     * Edita una pregunta existente según su tipo
     */
    public Question editQuestion(Question originalQuestion) {
        if (originalQuestion instanceof MultipleChoiceQuestion) {
            return editMultipleChoiceQuestion((MultipleChoiceQuestion) originalQuestion);
        } else if (originalQuestion.getTypeQuestion() == TypeQuestion.NUMERICAL) {
            return editNumericalQuestion(originalQuestion);
        } else {
            return editTextualQuestion(originalQuestion);
        }
    }

    /**
     * Edita una pregunta textual
     */
    private Question editTextualQuestion(Question question) {
        Question editedQuestion = question.copy();

        boolean exitEditor = false;

        do {
            displayTextualQuestionInfo(editedQuestion);

            switch (selectEditTextualQuestionMenuOption()) {
                case 1 -> editQuestionText(editedQuestion);
                case 2 -> toggleRequiredStatus(editedQuestion);
                case 3 -> {
                    System.out.println("\nCambios guardados.");
                    exitEditor = true;
                }
                default -> System.out.println("Opción no válida.");
            }
        } while (!exitEditor);

        return editedQuestion;
    }

    /**
     * Edita una pregunta numérica
     */
    private Question editNumericalQuestion(Question question) {
        Question editedQuestion = question.copy();

        boolean exitEditor = false;

        do {
            displayNumericalQuestionInfo(editedQuestion);

            switch (selectEditNumericalQuestionMenuOption()) {
                case 1 -> editQuestionText(editedQuestion);
                case 2 -> toggleRequiredStatus(editedQuestion);
                case 3 -> {
                    System.out.println("\nCambios guardados.");
                    exitEditor = true;
                }
                default -> System.out.println("Opción no válida.");
            }
        } while (!exitEditor);

        return editedQuestion;
    }

    /**
     * Edita una pregunta de opción múltiple
     */
    private Question editMultipleChoiceQuestion(MultipleChoiceQuestion question) {
        MultipleChoiceQuestion editedQuestion = (MultipleChoiceQuestion) question.copy();

        boolean exitEditor = false;

        do {
            displayMultipleChoiceQuestionInfo(editedQuestion);

            switch (selectEditMultipleChoiceQuestionMenuOption()) {
                case 1 -> editQuestionText(editedQuestion);
                case 2 -> {
                    // Delegar la edición de opciones al driver específico
                    optionsDriver.editOptionsMenu(editedQuestion);
                }
                case 3 -> toggleRequiredStatus(editedQuestion);
                case 4 -> {
                    // Validar que tenga al menos 2 opciones antes de guardar
                    if (editedQuestion.getOptions().size() >= 2) {
                        System.out.println("\nCambios guardados.");
                        exitEditor = true;
                    } else {
                        System.out.println("\nNo puedes guardar: se requieren al menos 2 opciones.");
                    }
                }
                default -> System.out.println("Opción no válida.");
            }
        } while (!exitEditor);

        return editedQuestion;
    }

    /**
     * Cambia el estado de obligatoriedad de una pregunta
     */
    private void toggleRequiredStatus(Question question) {
        String currentStatus = question.isRequired() ? "OBLIGATORIA" : "OPCIONAL";
        System.out.println("\nEstado actual: " + currentStatus);
        System.out.print("¿Cambiar a " + (question.isRequired() ? "OPCIONAL" : "OBLIGATORIA") + "? (S/N): ");


        String response = sc.nextLine().trim();

        if (response.equalsIgnoreCase("S")) {
            try {
                questionController.toggleRequiredStatus(question);
                String newStatus = question.isRequired() ? "OBLIGATORIA" : "OPCIONAL";
                System.out.println("Pregunta marcada como " + newStatus);
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        } else {
            System.out.println("No se realizaron cambios.");
        }
    }

    private void displayTextualQuestionInfo(Question question) {
        System.out.println("\n========================================");
        System.out.println("Pregunta: " + question.getQuestionText());
        System.out.println("Tipo: TEXTUAL");
        System.out.println("Obligatoria: " + (question.isRequired() ? "SÍ" : "NO"));
        System.out.println("========================================");
    }

    private void displayNumericalQuestionInfo(Question question) {
        System.out.println("\n========================================");
        System.out.println("Pregunta: " + question.getQuestionText());
        System.out.println("Tipo: NUMÉRICA");
        System.out.println("Obligatoria: " + (question.isRequired() ? "SÍ" : "NO"));
        System.out.println("(Los usuarios deberán responder con un número)");
        System.out.println("========================================");
    }

    private void displayMultipleChoiceQuestionInfo(MultipleChoiceQuestion question) {
        System.out.println("\n========================================");
        System.out.println("Pregunta: " + question.getQuestionText());
        System.out.println("Tipo: OPCIÓN MÚLTIPLE");
        System.out.println("Obligatoria: " + (question.isRequired() ? "SÍ" : "NO"));
        System.out.println("Selecciones mínimas: " + question.getMinSelections());
        System.out.println("Selecciones máximas: " + question.getMaxSelections());
        System.out.println("Número de opciones: " + question.getOptions().size());
        System.out.println("========================================");
    }

    private void editQuestionText(Question question) {

        System.out.println("\nTexto actual: " + question.getQuestionText());
        System.out.print("Introduce el nuevo texto de la pregunta: ");
        String newText = sc.nextLine();

        try {
            questionController.updateQuestionText(question, newText);
            System.out.println("Texto actualizado.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
