package presentation.drivers;

import domain.controller.SurveyController;
import presentation.driverMain.DriverMain;

import java.util.Scanner;

public class SurveyDriver {
    private final Scanner sc = DriverMain.getScanner();
    private final ResponseDriver responseDriver;
    private final MySurveysDriver mySurveysDriver;

    public SurveyDriver(ResponseDriver responseDriver, MySurveysDriver mySurveysDriver) {
        this.responseDriver = responseDriver;
        this.mySurveysDriver = mySurveysDriver;
    }

    public int selectSurveyMenuOption() {
        System.out.println("--- ENCUESTAS ---");
        System.out.println("1. RESPONDER ENCUESTAS");
        System.out.println("2. ANALIZAR MIS ENCUESTAS");
        System.out.println("3. VOLVER AL MENÚ PRINCIPAL");
        System.out.print("Opción: ");
        int option = sc.nextInt();
        sc.nextLine(); // Consumir el salto de línea
        return option;
    }

    public void surveyMenu() {
        boolean exitSurveyMenu = false;
        do {
            switch (selectSurveyMenuOption()) {
                case 1 -> responseDriver.responseMenu();
                case 2 -> mySurveysDriver.mySurveysMenu();
                case 3 -> exitSurveyMenu = true;
                default -> System.out.println("Opción no válida. Selecciona una opción del menú.");
            }
        } while (!exitSurveyMenu);
        System.out.println("--- volviendo al menú principal ---");
    }

    /**
     * Método main para pruebas independientes del SurveyDriver.
     * Permite probar el menú de encuestas (responder y analizar).
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
        domain.controller.CtrlDominioClustering clusteringController = new domain.controller.CtrlDominioClustering(responseRepository, surveyRepository);
        
        // Inicializar drivers
        EditResponseDriver editResponseDriver = new EditResponseDriver(responseController);
        ClusteringDriver clusteringDriver = new ClusteringDriver(clusteringController);
        MySurveysDriver mySurveysDriver = new MySurveysDriver(surveyController, clusteringDriver);
        ResponseDriver responseDriver = new ResponseDriver(surveyController, responseController, editResponseDriver);
        SurveyDriver surveyDriver = new SurveyDriver(responseDriver, mySurveysDriver);
        
        System.out.println("=== PRUEBA SURVEYDRIVER ===");
        System.out.println("Nota: Para usar esta funcionalidad necesitas tener un usuario logueado.");
        System.out.println("Ejecutando menú de encuestas...\n");
        
        surveyDriver.surveyMenu();
    }
}
