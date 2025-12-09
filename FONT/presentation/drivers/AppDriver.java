package presentation.drivers;

import domain.controller.*;
import presentation.driverMain.DriverMain;

import java.util.Scanner;

public class AppDriver {
    private final Scanner sc = DriverMain.getScanner();
    private final SurveyDriver surveyDriver;
    private final CreateSurveyDriver createsurveyDriver;
    private final EditSurveysDriver editSurveysDriver;
    private final SessionDriver sessionDriver;


    public AppDriver(SurveyDriver surveyDriver, SessionDriver sessionDriver, CreateSurveyDriver createsurveyDriver, EditSurveysDriver editSurveysDriver) {
        this.createsurveyDriver = createsurveyDriver;
        this.editSurveysDriver = editSurveysDriver;
        this.surveyDriver = surveyDriver;
        this.sessionDriver = sessionDriver;
    }


    private int selectAppMenuOption() {
        System.out.println("--- K-SURVEY ---");
        System.out.println("1. RESPONDER O ANALIZAR ENCUESTAS");
        System.out.println("2. CREAR ENCUESTA");
        System.out.println("3. EDITAR ENCUESTAS BORRADOR");
        System.out.println("4. CERRAR SESIÓN");
        System.out.println("5. ELIMINAR CUENTA");
        System.out.print("Opción: ");
        int option = sc.nextInt();
        sc.nextLine(); // Consumir el salto de línea
        return option;
    }

    public void clearTerminal()
    {
        for(int i =0;i< 50; i++)
            System.out.println();
    }

    public void appMenu() {
        boolean exitApp = false;
        do {
            try {
                switch (selectAppMenuOption()) {
                    case 1 -> surveyDriver.surveyMenu();
                    case 2 -> createsurveyDriver.createSurveyMenu();
                    case 3 -> editSurveysDriver.editSurveysMenu();
                    case 4 -> {
                        sessionDriver.logout();
                        exitApp = true;
                    }
                    case 5 -> {
                        sessionDriver.driverDeleteAccount();
                        exitApp = true;
                    }
                    default -> System.out.println("Opción no válida. Seleccióna una opción del menú.");
                }
            } catch (java.util.NoSuchElementException e) {
                exitApp = true;
            }
        } while (!exitApp);
    }

    /**
     * Método main para pruebas independientes del AppDriver.
     * Permite probar el menú principal de la aplicación.
     */
    public static void main(String[] args) {
        // Inicializar repositorios
        data.UserRepository userRepository = new data.UserRepository();
        data.SurveyRepository surveyRepository = new data.SurveyRepository();
        data.ResponseRepository responseRepository = new data.ResponseRepository();
        data.QuestionRepository questionRepository = new data.QuestionRepository();
        
        // Inicializar controladores básicos
        domain.service.UserService userService = new domain.service.UserService(userRepository, surveyRepository, responseRepository);
        domain.controller.UserController userController = new domain.controller.UserController(userService);
        
        // Inicializar servicios (necesitan UserController)
        domain.service.SurveyService surveyService = new domain.service.SurveyService(surveyRepository, userController, userService);
        domain.service.QuestionService questionService = new domain.service.QuestionService(questionRepository, userController);
        domain.service.ResponseService responseService = new domain.service.ResponseService(responseRepository, userController, surveyService);
        
        // Inicializar controladores de dominio
        domain.controller.SurveyController surveyController = new domain.controller.SurveyController(surveyService);
        domain.controller.ResponseController responseController = new domain.controller.ResponseController(responseService);
        domain.controller.QuestionController questionController = new domain.controller.QuestionController(questionService);
        domain.controller.CtrlDominioClustering clusteringController = new domain.controller.CtrlDominioClustering(responseRepository, surveyRepository);
        
        // Inicializar drivers auxiliares
        EditorQuestionDriver editorQuestionDriver = new EditorQuestionDriver(questionController);
        EditResponseDriver editResponseDriver = new EditResponseDriver(responseController);
        ClusteringDriver clusteringDriver = new ClusteringDriver(clusteringController);
        MySurveysDriver mySurveysDriver = new MySurveysDriver(surveyController, clusteringDriver);
        ResponseDriver responseDriver = new ResponseDriver(surveyController, responseController, editResponseDriver);
        SurveyDriver surveyDriver = new SurveyDriver(responseDriver, mySurveysDriver);
        CreateSurveyDriver createSurveyDriver = new CreateSurveyDriver(surveyController, userController, editorQuestionDriver);
        PasswordRecoveryDriver passwordRecoveryDriver = new PasswordRecoveryDriver(userController);
        EditSurveysDriver editSurveysDriver = new EditSurveysDriver(surveyController, userController, editorQuestionDriver);
        SessionDriver sessionDriver = new SessionDriver(userController, passwordRecoveryDriver);
        AppDriver appDriver = new AppDriver(surveyDriver, sessionDriver, createSurveyDriver, editSurveysDriver);

        // Conectar drivers
        sessionDriver.setAppDriver(appDriver);
        
        System.out.println("=== PRUEBA APPDRIVER ===");
        System.out.println("Para probar este driver, primero debes iniciar sesión.");
        
        // Simular login para poder probar el menú
        sessionDriver.driverLogin();
    }
}

