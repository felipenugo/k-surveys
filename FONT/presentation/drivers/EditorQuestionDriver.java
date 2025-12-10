package presentation.drivers;

import domain.controller.QuestionController;
import domain.model.Question;
import domain.model.MultipleChoiceQuestion;
import domain.model.enums.TypeQuestion;
import presentation.driverMain.DriverMain;

import java.util.Scanner;

public class EditorQuestionDriver {
    private final Scanner sc = DriverMain.getScanner();
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
        int option = sc.nextInt();
        sc.nextLine(); // Consumir el salto de línea
        return option;
    }

    private int selectEditTextualQuestionMenuOption() {
        System.out.println("\n--- EDITAR PREGUNTA TEXTUAL ---");
        System.out.println("1. EDITAR TEXTO DE LA PREGUNTA");
        System.out.println("2. CAMBIAR SI ES OBLIGATORIA");
        System.out.println("3. GUARDAR Y SALIR");
        System.out.print("Opción: ");
        int option = sc.nextInt();
        sc.nextLine(); // Consumir el salto de línea
        return option;
    }

    private int selectEditNumericalQuestionMenuOption() {
        System.out.println("\n--- EDITAR PREGUNTA NUMÉRICA ---");
        System.out.println("1. EDITAR TEXTO DE LA PREGUNTA");
        System.out.println("2. CAMBIAR SI ES OBLIGATORIA");
        System.out.println("3. GUARDAR Y SALIR");
        System.out.print("Opción: ");
        int option = sc.nextInt();
        sc.nextLine(); // Consumir el salto de línea
        return option;
    }

    private int selectEditMultipleChoiceQuestionMenuOption() {
        System.out.println("\n--- EDITAR PREGUNTA DE OPCIÓN MÚLTIPLE ---");
        System.out.println("1. EDITAR TEXTO DE LA PREGUNTA");
        System.out.println("2. EDITAR OPCIONES");
        System.out.println("3. CAMBIAR SI ES OBLIGATORIA");
        System.out.println("4. GUARDAR Y SALIR");
        System.out.print("Opción: ");
        int option = sc.nextInt();
        sc.nextLine(); // Consumir el salto de línea
        return option;
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

    /**
     * Método main para pruebas independientes del EditorQuestionDriver.
     * Permite probar la creación y edición de preguntas.
     */
    public static void main(String[] args) {
        // Inicializar repositorios
        data.UserRepository userRepository = new data.UserRepository();
        data.QuestionRepository questionRepository = new data.QuestionRepository();
        data.SurveyRepository surveyRepository = new data.SurveyRepository();
        
        // Inicializar servicios
        domain.service.UserService userService = new domain.service.UserService(userRepository, surveyRepository, new data.ResponseRepository());
        domain.controller.UserController userController = new domain.controller.UserController(userService);
        domain.service.QuestionService questionService = new domain.service.QuestionService(questionRepository, userController);
        domain.service.SurveyService surveyService = new domain.service.SurveyService(surveyRepository, userController, userService);

        // Inicializar controladores
        domain.controller.QuestionController questionController = new domain.controller.QuestionController(questionService);
        domain.controller.SurveyController surveyController = new domain.controller.SurveyController(surveyService);
        
        // Crear driver
        EditorQuestionDriver editorQuestionDriver = new EditorQuestionDriver(questionController);
        
        System.out.println("=== PRUEBA EDITORQUESTIONDRIVER ===");
        System.out.println("Este driver permite crear y editar preguntas para encuestas.");
        System.out.println("Nota: Para usar esta funcionalidad necesitas tener un usuario logueado y una encuesta creada.\n");
        
        // Simular login
        Scanner scanner = new Scanner(System.in);
        System.out.print("Introduce tu nombre de usuario: ");
        String username = scanner.nextLine();
        System.out.print("Introduce tu contraseña: ");
        String password = scanner.nextLine();
        
        try {
            userController.loginUser(username, password);
            System.out.println("Login exitoso.\n");
            
            // Solicitar ID de encuesta para crear preguntas
            System.out.print("Introduce el ID de la encuesta donde crear preguntas: ");
            String surveyId = scanner.nextLine();
            
            // Verificar que la encuesta existe
            if (surveyController.getSurvey(surveyId) != null) {
                boolean continuar = true;
                int questionIndex = 0;
                
                while (continuar) {
                    System.out.println("\n--- MENÚ DE PRUEBA ---");
                    System.out.println("1. Crear nueva pregunta");
                    System.out.println("2. Editar pregunta existente");
                    System.out.println("0. Salir");
                    System.out.print("Opción: ");
                    
                    String opcion = scanner.nextLine();
                    
                    switch (opcion) {
                        case "1" -> {
                            Question newQuestion = editorQuestionDriver.createQuestion(surveyId, questionIndex);
                            if (newQuestion != null) {
                                System.out.println("Pregunta creada exitosamente.");
                                questionIndex++;
                            }
                        }
                        case "2" -> {
                            System.out.println("Funcionalidad de edición disponible dentro del flujo de creación de encuestas.");
                            System.out.println("Esta es una prueba simplificada del driver.");
                        }
                        case "0" -> continuar = false;
                        default -> System.out.println("Opción no válida.");
                    }
                }
            } else {
                System.out.println("La encuesta con ID '" + surveyId + "' no existe.");
            }
            
        } catch (domain.exception.LogInException e) {
            System.out.println("Error al iniciar sesión: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        
        scanner.close();
    }
}
