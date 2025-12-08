package presentation.drivers;

import domain.controller.SurveyController;
import domain.controller.UserController;
import domain.model.Survey;
import domain.model.enums.SurveyStatus;
import domain.exception.SurveyException;
import presentation.driverMain.DriverMain;

import java.util.List;
import java.util.Scanner;
import domain.model.Question;
import domain.model.MultipleChoiceQuestion;
import domain.model.enums.TypeQuestion;

/**
 * Driver para editar encuestas en estado DRAFT.
 * Permite:
 * 1. Listar todas las encuestas del usuario con su estado
 * 2. Seleccionar una encuesta DRAFT para editarla
 * 3. Editor completo similar a CreateSurveyDriver
 */
public class EditSurveysDriver {
    private final SurveyController surveyController;
    private final UserController userController;
    private final EditorQuestionDriver editorQuestionDriver;
    private final Scanner sc = DriverMain.getScanner();
    private Survey currentSurvey;

    public EditSurveysDriver(
            SurveyController surveyController,
            UserController userController,
            EditorQuestionDriver editorQuestionDriver) {
        this.surveyController = surveyController;
        this.userController = userController;
        this.editorQuestionDriver = editorQuestionDriver;
    }

    private int selectEditSurveysMenuOption() {
        System.out.println("\n--- EDITAR ENCUESTAS ---");
        System.out.println("1. LISTAR MIS ENCUESTAS");
        System.out.println("2. EDITAR ENCUESTA (BORRADOR)");
        System.out.println("3. SALIR");
        System.out.print("Opción: ");
        int option = sc.nextInt();
        sc.nextLine(); // Consumir el salto de línea
        return option;
    }

    public void editSurveysMenu() {
        try {
            boolean exitMenu = false;
            do {
                switch (selectEditSurveysMenuOption()) {
                    case 1 -> listMySurveys();
                    case 2 -> editSurvey();
                    case 3 -> exitMenu = true;
                    default -> System.out.println("Opción no válida. Selecciona una opción del menú.");
                }
            } while (!exitMenu);
        } catch (SurveyException e) {
            System.out.println("\nERROR: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("\nError inesperado: " + e.getMessage());
        }
    }

    /**
     * Muestra todas las encuestas del usuario con su estado
     */
    private void listMySurveys() {
        try {
            String username = userController.getUsernameLoggedIn();
            List<Survey> mySurveys = surveyController.getSurveysByUser(username);

            System.out.println("\n========================================");
            System.out.println("       MIS ENCUESTAS (BORRADORES)");
            System.out.println("========================================");

            boolean hasDraftSurveys = false;
            for (Survey survey : mySurveys) {
                // Filtrar solo encuestas en estado DRAFT
                if (survey.getSurveyStatus().equals(SurveyStatus.DRAFT)) {
                    hasDraftSurveys = true;
                    System.out.println("ID: " + survey.getSURVEY_ID());
                    System.out.println("Título: " + survey.getTitle());
                    System.out.println("Estado: " + survey.getSurveyStatus());
                    System.out.println("Preguntas: " + survey.getSize());
                    System.out.println("----------------------------------------");
                }
            }

            if (!hasDraftSurveys) {
                System.out.println("No tienes encuestas en estado borrador.");
            }
        } catch (SurveyException e) {
            System.out.println("\nERROR: " + e.getMessage());
        }
    }

    /**
     * Edita una encuesta seleccionando su ID
     */
    private void editSurvey() {
        try {
            System.out.print("\nIntroduce el ID de la encuesta que deseas editar: ");
            String surveyId = sc.nextLine();

            // Validar que la encuesta existe
            if (!surveyController.existsSurvey(surveyId)) {
                System.out.println("ERROR: La encuesta con ID " + surveyId + " no existe.");
                return;
            }

            // Obtener la encuesta
            Survey survey = surveyController.getSurvey(surveyId);

            // Validar que es del usuario actual
            String username = userController.getUsernameLoggedIn();
            if (!survey.getCREATOR_USERNAME().equals(username)) {
                System.out.println("ERROR: No tienes permisos para editar esta encuesta.");
                return;
            }

            // Validar que está en estado DRAFT
            if (!survey.getSurveyStatus().equals(SurveyStatus.DRAFT)) {
                System.out.println("ERROR: Solo puedes editar encuestas en estado BORRADOR.");
                System.out.println("Estado actual: " + survey.getSurveyStatus());
                return;
            }

            // Cargar la encuesta para editar
            currentSurvey = surveyController.getSurvey(surveyId); // cargar desde BD
            editSurveyMenu();

        } catch (SurveyException e) {
            System.out.println("\nERROR: " + e.getMessage());
        }
    }

    /**
     * Menú para editar una encuesta específica
     */
    private int selectEditSurveyMenuOption() {
        System.out.println("\n--- EDITAR ENCUESTA (ID: " + currentSurvey.getSURVEY_ID() + ") ---");
        System.out.println("1. VER INFO ENCUESTA");
        System.out.println("2. EDITAR TÍTULO");
        System.out.println("3. EDITAR DESCRIPCIÓN");
        System.out.println("4. AÑADIR PREGUNTA");
        System.out.println("5. EDITAR PREGUNTA");
        System.out.println("6. ELIMINAR PREGUNTA");
        System.out.println("7. REORDENAR PREGUNTAS");
        System.out.println("8. VER TODAS LAS PREGUNTAS");
        System.out.println("9. GUARDAR Y SALIR");
        System.out.println("10. PUBLICAR Y SALIR");
        System.out.println("11. ELIMINAR ENCUESTA");
        System.out.println("12. DESCARTAR CAMBIOS Y SALIR");
        System.out.print("Opción: ");
        int option = sc.nextInt();
        sc.nextLine(); // Consumir el salto de línea
        return option;
    }

    private void editSurveyMenu() {
        boolean exitMenu = false;
        do {
            try {
                switch (selectEditSurveyMenuOption()) {
                    case 1 -> viewSurveyInfo();
                    case 2 -> editTitle();
                    case 3 -> editDescription();
                    case 4 -> addQuestion();
                    case 5 -> editQuestion();
                    case 6 -> deleteQuestion();
                    case 7 -> reorderQuestions();
                    case 8 -> viewAllQuestions();
                    case 9 -> {
                        if (saveSurvey()) {
                            exitMenu = true;
                        }
                    }
                    case 10 -> {
                        if (publishSurvey()) {
                            exitMenu = true;
                        }
                    }
                    case 11 -> {
                        if (deleteSurvey()) {
                            exitMenu = true;
                        }
                    }
                    case 12 -> exitMenu = true;
                    default -> System.out.println("Opción no válida. Selecciona una opción del menú.");
                }
            } catch (Exception e) {
                System.out.println("\nError: " + e.getMessage());
            }
        } while (!exitMenu);
    }

    private void viewSurveyInfo() {
        System.out.println("\n========================================");
        System.out.println("       INFO DE LA ENCUESTA");
        System.out.println("========================================");
        System.out.println("ID: " + currentSurvey.getSURVEY_ID());
        System.out.println("Título: " + currentSurvey.getTitle());
        System.out.println("Descripción: " + currentSurvey.getDescription());
        System.out.println("Creador: " + currentSurvey.getCREATOR_USERNAME());
        System.out.println("Estado: " + currentSurvey.getSurveyStatus());
        System.out.println("Número de preguntas: " + currentSurvey.getSize());
        System.out.println("========================================");
    }

    private void editTitle() {
        System.out.print("\nTítulo actual: " + currentSurvey.getTitle());
        System.out.print("\nIntroduce el nuevo título: ");
        String newTitle = sc.nextLine();

        if (!newTitle.trim().isEmpty()) {
            currentSurvey.setTitle(newTitle);
            try {
                // Persistir el cambio inmediatamente
                surveyController.updateSurvey(currentSurvey.getSURVEY_ID(), currentSurvey);
                // Recargar desde la BD para asegurar consistencia
                currentSurvey = surveyController.getSurvey(currentSurvey.getSURVEY_ID());
                System.out.println("Título actualizado y guardado.");
            } catch (SurveyException e) {
                System.out.println("ERROR al guardar el título: " + e.getMessage());
            }
        } else {
            System.out.println("El título no puede estar vacío.");
        }
    }

    private void editDescription() {
        System.out.print("\nDescripción actual: " + currentSurvey.getDescription());
        System.out.print("\nIntroduce la nueva descripción: ");
        String newDescription = sc.nextLine();

        if (!newDescription.trim().isEmpty()) {
            currentSurvey.setDescription(newDescription);
            try {
                // Persistir el cambio inmediatamente
                surveyController.updateSurvey(currentSurvey.getSURVEY_ID(), currentSurvey);
                // Recargar desde la BD
                currentSurvey = surveyController.getSurvey(currentSurvey.getSURVEY_ID());
                System.out.println("Descripción actualizada y guardada.");
            } catch (SurveyException e) {
                System.out.println("ERROR al guardar la descripción: " + e.getMessage());
            }
        } else {
            System.out.println("La descripción no puede estar vacía.");
        }
    }

    private void addQuestion() {
        try {
            Question newQuestion = editorQuestionDriver.createQuestion(currentSurvey.getSURVEY_ID(), currentSurvey.getSize());

            if (newQuestion == null) {
                System.out.println("No se creó la pregunta.");
                return;
            }

            currentSurvey.addQuestion(newQuestion);
            surveyController.updateSurvey(currentSurvey.getSURVEY_ID(), currentSurvey);

            currentSurvey = surveyController.getSurvey(currentSurvey.getSURVEY_ID());
            System.out.println("Pregunta añadida correctamente en posición " + newQuestion.getQuestionIndex() + ".");
        } catch (Exception e) {
            System.out.println("ERROR al añadir la pregunta: " + e.getMessage());
        }
    }

    private void editQuestion() {
        if (currentSurvey.getSize() == 0) {
            System.out.println("No hay preguntas para editar.");
            return;
        }

        viewAllQuestions();

        System.out.print("Introduce el índice de la pregunta a editar (0 - " + (currentSurvey.getSize() - 1) + "): ");
        try {
            int index = sc.nextInt();
            sc.nextLine();

            if (index < 0 || index >= currentSurvey.getSize()) {
                System.out.println("Índice inválido.");
                return;
            }

            Question original = currentSurvey.getQuestion(index);
            // Obtener la pregunta editada del editor (no modificar original hasta confirmar)
            Question editedQuestion = editorQuestionDriver.editQuestion(original);

            if (editedQuestion != null) {
                // Actualizar en el objeto actual y persistir
                currentSurvey.updateQuestion(index, editedQuestion);
                surveyController.updateSurvey(currentSurvey.getSURVEY_ID(), currentSurvey);
                // Recargar para asegurar que el repositorio refleja los cambios
                currentSurvey = surveyController.getSurvey(currentSurvey.getSURVEY_ID());
                System.out.println("Pregunta actualizada correctamente.");
            } else {
                System.out.println("Edición cancelada o sin cambios.");
            }
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            sc.nextLine();
        }
    }

    private void deleteQuestion() {
        if (currentSurvey.getSize() == 0) {
            System.out.println("No hay preguntas para eliminar.");
            return;
        }

        System.out.print("Introduce el índice de la pregunta a eliminar (0 - " + (currentSurvey.getSize() - 1) + "): ");
        try {
            int index = sc.nextInt();
            sc.nextLine();

            if (index < 0 || index >= currentSurvey.getSize()) {
                System.out.println("Índice inválido.");
                return;
            }

            surveyController.deleteQuestion(currentSurvey.getSURVEY_ID(), index);
            currentSurvey = surveyController.getSurvey(currentSurvey.getSURVEY_ID());
            System.out.println("Pregunta eliminada correctamente.");
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    private void reorderQuestions() {
        if (currentSurvey.getSize() < 2) {
            System.out.println("Necesitas al menos 2 preguntas para reordenar.");
            return;
        }

        System.out.print("Introduce el índice actual (0 - " + (currentSurvey.getSize() - 1) + "): ");
        try {
            int oldIndex = sc.nextInt();
            System.out.print("Introduce el nuevo índice (0 - " + (currentSurvey.getSize() - 1) + "): ");
            int newIndex = sc.nextInt();
            sc.nextLine();

            if (oldIndex < 0 || oldIndex >= currentSurvey.getSize() || newIndex < 0 || newIndex >= currentSurvey.getSize()) {
                System.out.println("Índices inválidos.");
                return;
            }

            surveyController.reorderQuestion(currentSurvey.getSURVEY_ID(), oldIndex, newIndex);
            currentSurvey = surveyController.getSurvey(currentSurvey.getSURVEY_ID());
            System.out.println("Preguntas reordenadas correctamente.");
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    private void viewAllQuestions() {
        if (currentSurvey.getSize() == 0) {
            System.out.println("No hay preguntas en esta encuesta.");
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

                // Obtener la lista de opciones de forma segura
                java.util.List<?> options = mcq.getOptions();
                if (options == null || options.isEmpty()) {
                    System.out.println("      (No hay opciones definidas)");
                } else {
                    // Imprimir opciones de forma robusta: pueden ser String o un objeto con getOptionText()
                    for (int j = 0; j < options.size(); j++) {
                        Object opt = options.get(j);
                        String optionText = "";
                        if (opt == null) {
                            optionText = "(null)";
                        } else if (opt instanceof String) {
                            optionText = (String) opt;
                        } else {
                            // Intentar llamar a getOptionText() por reflexión o usar toString() como fallback
                            try {
                                java.lang.reflect.Method m = opt.getClass().getMethod("getOptionText");
                                Object res = m.invoke(opt);
                                optionText = (res != null) ? res.toString() : opt.toString();
                            } catch (Exception e) {
                                optionText = opt.toString();
                            }
                        }
                        System.out.println("      " + (j + 1) + ". " + optionText);
                    }
                }
            } else if (q.getTypeQuestion() == TypeQuestion.NUMERICAL) {
                System.out.println("    (Respuesta numérica requerida)");
            }
        }
        System.out.println("========================================");
    }

    private boolean saveSurvey() {
        try {
            System.out.println("\n========================================");
            System.out.println("       GUARDAR ENCUESTA");
            System.out.println("========================================");

            if (currentSurvey.getSize() == 0) {
                System.out.println("La encuesta debe tener al menos una pregunta para guardarse.");
                return false;
            }

            System.out.println("Título: " + currentSurvey.getTitle());
            System.out.println("Descripción: " + currentSurvey.getDescription());
            System.out.println("Preguntas: " + currentSurvey.getSize());
            System.out.println("Estado: BORRADOR");

            System.out.print("\n¿Confirmas guardar los cambios? (S/N): ");
            String confirmation = sc.nextLine();

            if (confirmation.equalsIgnoreCase("S")) {
                surveyController.updateSurvey(currentSurvey.getSURVEY_ID(), currentSurvey);
                // Recargar la encuesta para mantener consistencia con la BD
                currentSurvey = surveyController.getSurvey(currentSurvey.getSURVEY_ID());
                System.out.println("\nEncuesta guardada correctamente.");
                return true;
            }

            return false;
        } catch (SurveyException e) {
            System.out.println("\nERROR: " + e.getMessage());
            return false;
        }
    }

    private boolean publishSurvey() {
        try {
            System.out.println("\n========================================");
            System.out.println("       PUBLICAR ENCUESTA");
            System.out.println("========================================");

            if (currentSurvey.getSize() == 0) {
                System.out.println("No puedes publicar una encuesta sin preguntas.");
                return false;
            }

            System.out.println("Título: " + currentSurvey.getTitle());
            System.out.println("Descripción: " + currentSurvey.getDescription());
            System.out.println("Preguntas: " + currentSurvey.getSize());

            System.out.print("\n¿Estás seguro? Una vez publicada, no podrá editarse. (S/N): ");
            String confirmation = sc.nextLine();

            if (confirmation.equalsIgnoreCase("S")) {
                // Asegurar que los últimos cambios estén guardados
                surveyController.updateSurvey(currentSurvey.getSURVEY_ID(), currentSurvey);
                surveyController.publishSurvey(currentSurvey.getSURVEY_ID());
                // Recargar para mostrar estado y fecha de publicación
                currentSurvey = surveyController.getSurvey(currentSurvey.getSURVEY_ID());
                System.out.println("\nEncuesta publicada correctamente.");
                return true;
            }

            return false;
        } catch (SurveyException e) {
            System.out.println("\nERROR: " + e.getMessage());
            return false;
        }
    }

    private boolean deleteSurvey() {
        try {
            System.out.println("\n========================================");
            System.out.println("       ELIMINAR ENCUESTA");
            System.out.println("========================================");

            System.out.println("ID: " + currentSurvey.getSURVEY_ID());
            System.out.println("Título: " + currentSurvey.getTitle());

            System.out.print("\n¿Confirmas eliminar esta encuesta? Esta acción no se puede deshacer. (S/N): ");
            String confirmation = sc.nextLine();

            if (confirmation.equalsIgnoreCase("S")) {
                surveyController.deleteSurvey(currentSurvey.getSURVEY_ID());
                System.out.println("\nEncuesta eliminada correctamente.");
                return true;
            }

            return false;
        } catch (SurveyException e) {
            System.out.println("\nERROR: " + e.getMessage());
            return false;
        }
    }

}
