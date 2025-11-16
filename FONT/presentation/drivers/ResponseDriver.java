
package presentation.drivers;

import java.util.List;
import java.util.Scanner;

import domain.controller.ResponseController;
import domain.exception.ResponseException;
import domain.exception.SurveyException;

import domain.controller.SurveyController;
import domain.model.Survey;
import presentation.driverMain.DriverMain;

public class ResponseDriver {
    private final SurveyController surveyController;
    private final ResponseController responseController;
    private final EditResponseDriver editResponseDriver;
    private final Scanner sc = DriverMain.getScanner();

    public ResponseDriver(SurveyController surveyController, ResponseController responseController, EditResponseDriver editResponseDriver) {
        this.surveyController = surveyController;
        this.responseController = responseController;
        this.editResponseDriver = editResponseDriver;
    }

    public int selectResponseMenuOption() {
        System.out.println("--- RESPONDER ENCUESTAS ---");
        System.out.println("1. SELECCIONAR UNA ENCUESTA PARA RESPONDER");
        System.out.println("2. VOLVER ATRÁS");
        System.out.print("Opción: ");
        int option = sc.nextInt();
        sc.nextLine(); // Consumir el salto de línea
        return option;
    }

    private int selectErrorMenuOption() {
        System.out.println("1. Intentar de nuevo");
        System.out.println("2. Volver atrás");
        System.out.print("Opción: ");
        int option = sc.nextInt();
        sc.nextLine(); // Consumir el salto de línea
        return option;
    }

    public void showSurveys(List<Survey> surveys) {
        for (Survey survey : surveys) {
            System.out.println("id: " + survey.getSURVEY_ID() + ", título: " + survey.getTitle() + ", autor: " + survey.getCREATOR_USERNAME() + ", número de preguntas: " + survey.getSize());
            System.out.println("descripción: " + survey.getDescription() + ", número de respuestas: " + survey.getViews()+ "\n");
        }
    }

    public void selectSurvey() {
        boolean exit = false;
        do {
            try {
                var surveys = surveyController.getSelectedSurveys();
                showSurveys(surveys);
                System.out.print("Selecciona el id de una encuesta: ");
                String surveyIdSelected = sc.nextLine();
                String responseId = responseController.startResponse(surveyIdSelected);
                editResponseDriver.editResponseMenu(surveyIdSelected, responseId);
                exit = true;
            } catch ( SurveyException | ResponseException e) {
                System.out.println("Error: " + e.getMessage());
                boolean exitErrorMenu = false;
                do {
                    switch (selectErrorMenuOption()) {
                        case 1 -> exitErrorMenu = true;
                        case 2 -> {
                            exit = true;
                            exitErrorMenu = true;
                        }
                        default -> System.out.println("Opción no válida. Selecciona una opción del menú");
                    }
                } while (!exitErrorMenu);
            }
        } while (!exit);
    }


    public void responseMenu() {
        boolean exitResponseMenu = false;
        do {
            switch (selectResponseMenuOption()) {
                case 1 -> selectSurvey();
                case 2 -> exitResponseMenu = true;
                default -> System.out.println("Opción no válida. Selecciona una opción del menú");
            }
        } while (!exitResponseMenu);
        System.out.println("--- saliendo de responder encuesta ---");
    }

    /**
     * Método main para pruebas independientes del ResponseDriver.
     * Permite probar la funcionalidad de responder encuestas.
     */
    public static void main(String[] args) {
        // Inicializar repositorios
        data.UserRepository userRepository = new data.UserRepository();
        data.SurveyRepository surveyRepository = new data.SurveyRepository();
        data.ResponseRepository responseRepository = new data.ResponseRepository();
        data.QuestionRepository questionRepository = new data.QuestionRepository();
        
        // Inicializar servicios
        domain.service.UserService userService = new domain.service.UserService(userRepository);
        domain.controller.UserController userController = new domain.controller.UserController(userService);
        domain.service.SurveyService surveyService = new domain.service.SurveyService(surveyRepository, userController);
        domain.service.QuestionService questionService = new domain.service.QuestionService(questionRepository, userController);
        domain.service.ResponseService responseService = new domain.service.ResponseService(responseRepository, userController, surveyService);
        
        // Inicializar controladores
        domain.controller.SurveyController surveyController = new domain.controller.SurveyController(surveyService);
        domain.controller.ResponseController responseController = new domain.controller.ResponseController(responseService);
        
        // Inicializar drivers
        EditResponseDriver editResponseDriver = new EditResponseDriver(responseController);
        ResponseDriver responseDriver = new ResponseDriver(surveyController, responseController, editResponseDriver);
        
        System.out.println("=== PRUEBA RESPONSEDRIVER ===");
        System.out.println("Nota: Para responder encuestas necesitas tener un usuario logueado.");
        System.out.println("Por favor, inicia sesión primero.\n");
        
        // Simular login
        Scanner scanner = new Scanner(System.in);
        System.out.print("Introduce tu nombre de usuario: ");
        String username = scanner.nextLine();
        System.out.print("Introduce tu contraseña: ");
        String password = scanner.nextLine();
        
        try {
            userController.loginUser(username, password);
            System.out.println("Login exitoso. Accediendo al menú de respuestas...\n");
            responseDriver.responseMenu();
        } catch (domain.exception.LogInException e) {
            System.out.println("Error al iniciar sesión: " + e.getMessage());
            System.out.println("Debes registrarte primero o verificar tus credenciales.");
        }
        
        scanner.close();
    }
}
