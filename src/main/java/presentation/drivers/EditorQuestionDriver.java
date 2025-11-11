package presentation.drivers;

import domain.model.Question;
import domain.model.MultipleChoiceQuestion;
import domain.model.OptionQuestion;
import domain.model.enums.TypeQuestion;

import java.util.Scanner;

public class EditorQuestionDriver {
    private final Scanner sc = new Scanner(System.in);
    private final EditorMultipleChoiceOptionsDriver optionsDriver;

    public EditorQuestionDriver() {
        this.optionsDriver = new EditorMultipleChoiceOptionsDriver();
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
        System.out.println("2. GUARDAR Y SALIR");
        System.out.print("Opción: ");
        return sc.nextInt();
    }

    private int selectEditNumericalQuestionMenuOption() {
        System.out.println("\n--- EDITAR PREGUNTA NUMÉRICA ---");
        System.out.println("1. EDITAR TEXTO DE LA PREGUNTA");
        System.out.println("2. GUARDAR Y SALIR");
        System.out.print("Opción: ");
        return sc.nextInt();
    }

    private int selectEditMultipleChoiceQuestionMenuOption() {
        System.out.println("\n--- EDITAR PREGUNTA DE OPCIÓN MÚLTIPLE ---");
        System.out.println("1. EDITAR TEXTO DE LA PREGUNTA");
        System.out.println("2. EDITAR OPCIONES");
        System.out.println("3. GUARDAR Y SALIR");
        System.out.print("Opción: ");
        return sc.nextInt();
    }

    public Question createQuestion(String surveyId, int questionIndex) {
        sc.nextLine();

        System.out.println("\n========================================");
        System.out.println("       CREAR NUEVA PREGUNTA");
        System.out.println("========================================");
        System.out.print("Introduce el texto de la pregunta: ");
        String questionText = sc.nextLine();

        if (questionText.trim().isEmpty()) {
            System.out.println("El texto de la pregunta no puede estar vacío.");
            return null;
        }

        int typeOption = selectQuestionTypeOption();

        Question question = null;

        switch (typeOption) {
            case 1 -> {
                // Pregunta textual
                question = new Question(questionIndex, surveyId);
                question.setQuestionText(questionText);
                question.setTypeQuestion(TypeQuestion.TEXTUAL);
                System.out.println("\nPregunta textual creada.");
            }
            case 2 -> {
                // Pregunta de opción múltiple
                MultipleChoiceQuestion mcQuestion = new MultipleChoiceQuestion(questionIndex, surveyId);
                mcQuestion.setQuestionText(questionText);

                // Configurar opciones iniciales usando el driver específico
                if (optionsDriver.configureInitialOptions(mcQuestion)) {
                    question = mcQuestion;
                    System.out.println("\nPregunta de opción múltiple creada.");
                } else {
                    System.out.println("\nCreación cancelada: se requieren al menos 2 opciones.");
                }
            }
            case 3 -> {
                question = new Question(questionIndex, surveyId);
                question.setQuestionText(questionText);
                question.setTypeQuestion(TypeQuestion.NUMERICAL);
                System.out.println("\nPregunta numérica creada.");
                System.out.println("Los usuarios deberán responder con un número válido.");
            }
            case 4 -> System.out.println("\nCreación cancelada.");
            default -> System.out.println("\nOpción no válida.");
        }

        return question;
    }


    public Question editQuestion(Question originalQuestion) {
        if (originalQuestion instanceof MultipleChoiceQuestion) {
            return editMultipleChoiceQuestion((MultipleChoiceQuestion) originalQuestion);
        } else if (originalQuestion.getTypeQuestion() == TypeQuestion.NUMERICAL) {
            return editNumericalQuestion(originalQuestion);  // NUEVO
        } else {
            return editTextualQuestion(originalQuestion);
        }
    }

    private Question editTextualQuestion(Question question) {
        Question editedQuestion = cloneTextualQuestion(question);

        boolean exitEditor = false;

        do {
            displayTextualQuestionInfo(editedQuestion);

            switch (selectEditTextualQuestionMenuOption()) {
                case 1 -> editQuestionText(editedQuestion);
                case 2 -> {
                    System.out.println("\nCambios guardados.");
                    exitEditor = true;
                }
                default -> System.out.println("Opción no válida.");
            }
        } while (!exitEditor);

        return editedQuestion;
    }

    private Question editNumericalQuestion(Question question) {
        Question editedQuestion = cloneTextualQuestion(question);
        editedQuestion.setTypeQuestion(TypeQuestion.NUMERICAL);

        boolean exitEditor = false;

        do {
            displayNumericalQuestionInfo(editedQuestion);

            switch (selectEditNumericalQuestionMenuOption()) {
                case 1 -> editQuestionText(editedQuestion);
                case 2 -> {
                    System.out.println("\nCambios guardados.");
                    exitEditor = true;
                }
                default -> System.out.println("Opción no válida.");
            }
        } while (!exitEditor);

        return editedQuestion;
    }

    private Question editMultipleChoiceQuestion(MultipleChoiceQuestion question) {
        MultipleChoiceQuestion editedQuestion = cloneMultipleChoiceQuestion(question);

        boolean exitEditor = false;

        do {
            displayMultipleChoiceQuestionInfo(editedQuestion);

            switch (selectEditMultipleChoiceQuestionMenuOption()) {
                case 1 -> editQuestionText(editedQuestion);
                case 2 -> {
                    // Delegar la edición de opciones al driver específico
                    optionsDriver.editOptionsMenu(editedQuestion);
                }
                case 3 -> {
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

    private void displayTextualQuestionInfo(Question question) {
        System.out.println("\n========================================");
        System.out.println("Pregunta: " + question.getQuestionText());
        System.out.println("Tipo: TEXTUAL");
        System.out.println("========================================");
    }

    private void displayNumericalQuestionInfo(Question question) {
        System.out.println("\n========================================");
        System.out.println("Pregunta: " + question.getQuestionText());
        System.out.println("Tipo: NUMÉRICA");
        System.out.println("(Los usuarios deberán responder con un número)");
        System.out.println("========================================");
    }

    private void displayMultipleChoiceQuestionInfo(MultipleChoiceQuestion question) {
        System.out.println("\n========================================");
        System.out.println("Pregunta: " + question.getQuestionText());
        System.out.println("Tipo: OPCIÓN MÚLTIPLE");
        System.out.println("Selecciones mínimas: " + question.getMinSelections());
        System.out.println("Selecciones máximas: " + question.getMaxSelections());
        System.out.println("Número de opciones: " + question.getOptions().size());
        System.out.println("========================================");
    }

    private void editQuestionText(Question question) {
        sc.nextLine(); // Limpiar buffer
        System.out.println("\nTexto actual: " + question.getQuestionText());
        System.out.print("Introduce el nuevo texto de la pregunta: ");
        String newText = sc.nextLine();

        if (!newText.trim().isEmpty()) {
            question.setQuestionText(newText);
            System.out.println("✓ Texto actualizado.");
        } else {
            System.out.println("El texto no puede estar vacío.");
        }
    }

    private Question cloneTextualQuestion(Question original) {
        Question clone = new Question(original.getQuestionIndex(), original.getSURVEY_ID());
        clone.setQuestionText(original.getQuestionText());
        clone.setTypeQuestion(original.getTypeQuestion());
        return clone;
    }

    private MultipleChoiceQuestion cloneMultipleChoiceQuestion(MultipleChoiceQuestion original) {
        MultipleChoiceQuestion clone = new MultipleChoiceQuestion(
                original.getQuestionIndex(),
                original.getSURVEY_ID()
        );
        clone.setQuestionText(original.getQuestionText());
        clone.setMinSelections(original.getMinSelections());
        clone.setMaxSelections(original.getMaxSelections());

        // Copiar opciones
        for (OptionQuestion option : original.getOptions()) {
            OptionQuestion clonedOption = new OptionQuestion(
                    option.getQuestionIndex(),
                    option.getSurveyId()
            );
            clonedOption.setOptionText(option.getOptionText());
            clone.addOption(clonedOption);
        }

        return clone;
    }
}
