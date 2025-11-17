package presentation.drivers;

import domain.controller.SurveyController;
import domain.controller.UserController;
import domain.model.Survey;
import domain.model.Question;
import domain.model.MultipleChoiceQuestion;
import domain.model.enums.SurveyStatus;
import domain.model.enums.TypeQuestion;
import domain.exception.SurveyException;
import presentation.driverMain.DriverMain;

import java.util.NoSuchElementException;
import java.util.Scanner;

public class CreateSurveyDriver {
    private final SurveyController surveyController;
    private final UserController userController;
    private final EditorQuestionDriver editorQuestionDriver;
    private final Scanner sc = DriverMain.getScanner();
    private Survey currentSurvey;

    public CreateSurveyDriver(
            SurveyController surveyController,
            UserController userController,
            EditorQuestionDriver editorQuestionDriver) {
        this.surveyController = surveyController;
        this.userController = userController;
        this.editorQuestionDriver = editorQuestionDriver;
    }

    private int selectCreateSurveyMenuOption() {
        System.out.println("\n--- CREAR ENCUESTA ---");
        System.out.println("1. VER INFO ENCUESTA CREADA");
        System.out.println("2. EDITAR TÍTULO");
        System.out.println("3. EDITAR DESCRIPCIÓN");
        System.out.println("4. AÑADIR PREGUNTA");
        System.out.println("5. EDITAR PREGUNTA");
        System.out.println("6. ELIMINAR PREGUNTA");
        System.out.println("7. REORDENAR PREGUNTAS");
        System.out.println("8. VER TODAS LAS PREGUNTAS");
        System.out.println("9. GUARDAR, PUBLICAR Y SALIR");
        System.out.print("Opción: ");
        int option = sc.nextInt();
        sc.nextLine(); // Consumir el salto de línea
        return option;
    }

    public void createSurveyMenu() {
        try {
            // El usuario ya está logueado cuando llega aquí
            initializeNewSurvey();

            if (currentSurvey == null) {
                return; // Error al inicializar
            }

            boolean exitMenu = false;
            do {
                switch (selectCreateSurveyMenuOption()) {
                    case 1 -> viewSurveyId();
                    case 2 -> editTitle();
                    case 3 -> editDescription();
                    case 4 -> addQuestion();
                    case 5 -> editQuestion();
                    case 6 -> deleteQuestion();
                    case 7 -> reorderQuestions();
                    case 8 -> viewAllQuestions();
                    case 9 -> {
                        if (saveAndPublishSurvey()) {
                            exitMenu = true;
                        }
                    }
                    default -> System.out.println("Opción no válida. Selecciona una opción del menú.");
                }
            } while (!exitMenu);

        } catch (SurveyException e) {
            System.out.println("\nERROR: " + e.getMessage());
        } catch (NoSuchElementException e) {
            System.out.println("\nSe ha llegado al final de la entrada de datos.");
        } catch (Exception e) {
            System.out.println("\nError inesperado: " + e.getMessage());
        }
    }

    private void initializeNewSurvey() {


        System.out.println("\n========================================");
        System.out.println("       CREANDO NUEVA ENCUESTA");
        System.out.println("========================================");

        System.out.print("Introduce el título de la encuesta: ");
        String title = sc.nextLine();

        System.out.print("Introduce la descripción de la encuesta: ");
        String description = sc.nextLine();

        String creatorUsername = userController.getUsernameLoggedIn();

        try {
            currentSurvey = surveyController.initializeNewSurvey(title, description, creatorUsername);
            System.out.println("\nEncuesta inicializada correctamente.");
            System.out.println("(El ID será asignado al publicar)");
        } catch (Exception e) {
            System.out.println("Error al inicializar encuesta: " + e.getMessage());
            currentSurvey = null;
        }
    }

    private void viewSurveyId() {
        System.out.println("\n========================================");

        if (currentSurvey.getSURVEY_ID() != null) {
            System.out.println("ID de la encuesta: " + currentSurvey.getSURVEY_ID());
        } else {
            System.out.println("ID: (Se asignará automáticamente al publicar)");
        }

        System.out.println("Título: " + currentSurvey.getTitle());
        System.out.println("Descripción: " + currentSurvey.getDescription());
        System.out.println("Creador: " + currentSurvey.getCREATOR_USERNAME());
        System.out.println("Estado: " + currentSurvey.getSurveyStatus());
        System.out.println("Número de preguntas: " + currentSurvey.getSize());
        System.out.println("Fecha de creación: " + currentSurvey.getCREATED_AT());
        System.out.println("========================================");
    }

    private void editTitle() {

        System.out.println("\nTítulo actual: " + currentSurvey.getTitle());
        System.out.print("Introduce el nuevo título: ");
        String newTitle = sc.nextLine();

        if (!newTitle.trim().isEmpty()) {
            currentSurvey.setTitle(newTitle);
            System.out.println("Título actualizado.");
        } else {
            System.out.println("El título no puede estar vacío.");
        }
    }

    private void editDescription() {

        System.out.println("\nDescripción actual: " + currentSurvey.getDescription());
        System.out.print("Introduce la nueva descripción: ");
        String newDescription = sc.nextLine();

        if (!newDescription.trim().isEmpty()) {
            currentSurvey.setDescription(newDescription);
            System.out.println("Descripción actualizada.");
        } else {
            System.out.println("La descripción no puede estar vacía.");
        }
    }

    private void addQuestion() {
        try {
            int questionIndex = currentSurvey.getSize();

            // Usar ID temporal si aún no tiene uno
            String surveyId = (currentSurvey.getSURVEY_ID() != null)
                    ? currentSurvey.getSURVEY_ID()
                    : "temp_" + System.currentTimeMillis();

            Question newQuestion = editorQuestionDriver.createQuestion(surveyId, questionIndex);

            if (newQuestion != null) {
                currentSurvey.addQuestion(newQuestion);
                System.out.println("Pregunta añadida correctamente en posición " + questionIndex + ".");
            } else {
                System.out.println("No se añadió ninguna pregunta.");
            }
        } catch (Exception e) {
            System.out.println("Error al añadir pregunta: " + e.getMessage());
        }
    }

    private void editQuestion() {
        if (currentSurvey.getSize() == 0) {
            System.out.println("\nNo hay preguntas para editar.");
            return;
        }

        viewAllQuestions();
        System.out.print("\nIntroduce el índice de la pregunta a editar (0-" + (currentSurvey.getSize() - 1) + "): ");
        int index = sc.nextInt();
        sc.nextLine(); // Consumir el salto de línea

        if (index < 0 || index >= currentSurvey.getSize()) {
            System.out.println("Índice no válido.");
            return;
        }

        Question question = currentSurvey.getQuestion(index);
        Question editedQuestion = editorQuestionDriver.editQuestion(question);

        if (editedQuestion != null) {
            currentSurvey.updateQuestion(index, editedQuestion);
            System.out.println("Pregunta actualizada.");
        }
    }

    private void deleteQuestion() {
        if (currentSurvey.getSize() == 0) {
            System.out.println("\nNo hay preguntas para eliminar.");
            return;
        }

        viewAllQuestions();
        System.out.print("\nIntroduce el índice de la pregunta a eliminar (0-" + (currentSurvey.getSize() - 1) + "): ");
        int index = sc.nextInt();
        sc.nextLine(); // Consumir el salto de línea

        if (index < 0 || index >= currentSurvey.getSize()) {
            System.out.println("Índice no válido.");
            return;
        }


        System.out.print("¿Estás seguro de que deseas eliminar esta pregunta? (S/N): ");
        String confirmation = sc.nextLine();

        if (confirmation.equalsIgnoreCase("S")) {
            currentSurvey.removeQuestion(index);

            // Actualizar índices de las preguntas restantes
            for (int i = index; i < currentSurvey.getSize(); i++) {
                currentSurvey.getQuestion(i).setQuestionIndex(i);
            }

            System.out.println("Pregunta eliminada.");
        } else {
            System.out.println("Operación cancelada.");
        }
    }

    private void reorderQuestions() {
        if (currentSurvey.getSize() < 2) {
            System.out.println("\nSe necesitan al menos 2 preguntas para reordenar.");
            return;
        }

        viewAllQuestions();
        System.out.print("\nIntroduce el índice de la pregunta a mover: ");
        int oldIndex = sc.nextInt();
        sc.nextLine(); // Consumir el salto de línea
        System.out.print("Introduce la nueva posición: ");
        int newIndex = sc.nextInt();
        sc.nextLine(); // Consumir el salto de línea

        if (oldIndex < 0 || oldIndex >= currentSurvey.getSize() ||
                newIndex < 0 || newIndex >= currentSurvey.getSize()) {
            System.out.println("Índices no válidos.");
            return;
        }

        if (oldIndex == newIndex) {
            System.out.println("La pregunta ya está en esa posición.");
            return;
        }

        currentSurvey.reorderQuestion(oldIndex, newIndex);

        // Actualizar índices
        int start = Math.min(oldIndex, newIndex);
        int end = Math.max(oldIndex, newIndex);
        for (int i = start; i <= end; i++) {
            currentSurvey.getQuestion(i).setQuestionIndex(i);
        }

        System.out.println("Pregunta movida de posición " + oldIndex + " a posición " + newIndex + ".");
    }

    private void viewAllQuestions() {
        if (currentSurvey.getSize() == 0) {
            System.out.println("\nNo hay preguntas en esta encuesta.");
            return;
        }

        System.out.println("\n========================================");
        System.out.println("    PREGUNTAS DE LA ENCUESTA");
        System.out.println("========================================");

        for (int i = 0; i < currentSurvey.getSize(); i++) {
            Question q = currentSurvey.getQuestion(i);
            System.out.println("\n[" + i + "] " + q.getQuestionText());
            System.out.println("    Tipo: " + q.getTypeQuestion());
            System.out.println("    Obligatoria: " + (q.isRequired() ? "SÍ" : "NO"));

            if (q instanceof MultipleChoiceQuestion) {
                MultipleChoiceQuestion mcq = (MultipleChoiceQuestion) q;
                System.out.println("    Selecciones requeridas: " + mcq.getMinSelections() + "-" + mcq.getMaxSelections());
                System.out.println("    Opciones:");
                for (int j = 0; j < mcq.getOptions().size(); j++) {
                    System.out.println("      " + (j + 1) + ". " + mcq.getOptions().get(j).getOptionText());
                }
            } else if (q.getTypeQuestion() == TypeQuestion.NUMERICAL) {
                System.out.println("    (Respuesta numérica requerida)");
            }
        }
        System.out.println("========================================");
    }

    private boolean saveAndPublishSurvey() {
        if (currentSurvey.getSize() == 0) {
            System.out.println("\nNo puedes publicar una encuesta sin preguntas.");
            System.out.println("Añade al menos una pregunta antes de publicar.");
            return false;
        }


        System.out.println("\n========================================");
        System.out.println("       RESUMEN DE LA ENCUESTA");
        System.out.println("========================================");
        System.out.println("Título: " + currentSurvey.getTitle());
        System.out.println("Descripción: " + currentSurvey.getDescription());
        System.out.println("Creador: " + currentSurvey.getCREATOR_USERNAME());
        System.out.println("Número de preguntas: " + currentSurvey.getSize());
        System.out.println("========================================");

        System.out.print("\n¿Estás seguro de que deseas GUARDAR y PUBLICAR esta encuesta? (S/N): ");
        String confirmation = sc.nextLine();

        if (confirmation.equalsIgnoreCase("S")) {
            try {
                // Cambiar estado a PUBLICADO
                currentSurvey.setSurveyStatus(SurveyStatus.PUBLISHED);
                currentSurvey.setPUBLISHED_AT();

                // Guardar en el sistema
                surveyController.createSurvey(currentSurvey);

                System.out.println("\nEncuesta guardada y publicada correctamente");

                if (currentSurvey.getSURVEY_ID() != null) {
                    System.out.println("ID asignado: " + currentSurvey.getSURVEY_ID());
                }

                System.out.println("Fecha de publicación: " + currentSurvey.getPUBLISHED_AT());
                return true;

            } catch (SurveyException e) {
                System.out.println("\nERROR: " + e.getMessage());

                if (e.getMessage().contains("límite máximo")) {
                    System.out.println("El sistema ha alcanzado su capacidad máxima de encuestas.");
                    System.out.println("Por favor, contacta con el administrador del sistema.");
                }

                return false;

            } catch (Exception e) {
                System.out.println("\nError inesperado al publicar la encuesta: " + e.getMessage());
                return false;
            }
        } else {
            System.out.println("Publicación cancelada. Puedes seguir editando.");
            return false;
        }
    }

    /**
     * Método main para pruebas independientes del CreateSurveyDriver.
     * Permite probar la creación de encuestas.
     */
    public static void main(String[] args) {
        // Inicializar repositorios
        data.UserRepository userRepository = new data.UserRepository();
        data.SurveyRepository surveyRepository = new data.SurveyRepository();
        data.QuestionRepository questionRepository = new data.QuestionRepository();
        
        // Inicializar servicios
        domain.service.UserService userService = new domain.service.UserService(userRepository);
        domain.controller.UserController userController = new domain.controller.UserController(userService);
        domain.service.SurveyService surveyService = new domain.service.SurveyService(surveyRepository, userController);
        domain.service.QuestionService questionService = new domain.service.QuestionService(questionRepository, userController);
        
        // Inicializar controladores
        domain.controller.SurveyController surveyController = new domain.controller.SurveyController(surveyService);
        domain.controller.QuestionController questionController = new domain.controller.QuestionController(questionService);
        
        // Inicializar drivers
        EditorQuestionDriver editorQuestionDriver = new EditorQuestionDriver(questionController);
        CreateSurveyDriver createSurveyDriver = new CreateSurveyDriver(surveyController, userController, editorQuestionDriver);
        
        System.out.println("=== PRUEBA CREATESURVEYDRIVER ===");
        System.out.println("Nota: Para crear encuestas necesitas tener un usuario logueado.");
        System.out.println("Por favor, inicia sesión primero.\n");
        
        // Simular login
        Scanner scanner = new Scanner(System.in);
        System.out.print("Introduce tu nombre de usuario: ");
        String username = scanner.nextLine();
        System.out.print("Introduce tu contraseña: ");
        String password = scanner.nextLine();
        
        try {
            userController.loginUser(username, password);
            System.out.println("Login exitoso. Accediendo al menú de creación de encuestas...\n");
            createSurveyDriver.createSurveyMenu();
        } catch (domain.exception.LogInException e) {
            System.out.println("Error al iniciar sesión: " + e.getMessage());
            System.out.println("Debes registrarte primero o verificar tus credenciales.");
        }
        
        scanner.close();
    }
}
