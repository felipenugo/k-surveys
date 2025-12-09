package presentation.driverMain;

import java.util.Scanner;

import domain.controller.*;
import domain.service.*;
import data.*;
import presentation.drivers.*;

public class DriverMain {

    // Scanner estático compartido por toda la aplicación
    private static final Scanner sc = new Scanner(System.in);

    public static Scanner getScanner() {
        return sc;
    }

    public static int selectWelcomeMenuOption() {
        System.out.println("-----------------------------------");
        System.out.println("--- BIENVENIDO A K-SURVEY ---");
        System.out.println("1. INICIAR SESIÓN");
        System.out.println("2. REGISTRARSE");
        System.out.println("3. RECUPERAR CONTRASEÑA");
        System.out.println("4. SALIR");
        System.out.print("Opción: ");
        int option = sc.nextInt();
        sc.nextLine(); // Consumir el salto de línea
        return option;
    }

    public static void main(String[] var0) {
        // Inicializar repositorios
        UserRepository userRepository = new UserRepository();
        SurveyRepository surveyRepository = new SurveyRepository();
        QuestionRepository questionRepository = new QuestionRepository();
        ResponseRepository responseRepository = new ResponseRepository();
        AnswerRepository answerRepository = new AnswerRepository();

        // Inicializar servicios y controladores inyectando servicio con repositorio
        UserService userService = new UserService(userRepository, surveyRepository, responseRepository);
        UserController userController = new UserController(userService);

        SurveyService surveyService = new SurveyService(surveyRepository, userController, userService);
        SurveyController surveyController = new SurveyController(surveyService);

        QuestionService questionService = new QuestionService(questionRepository, userController);
        QuestionController questionController = new QuestionController(questionService);

        ResponseController responseController = new ResponseController(
                new ResponseService(responseRepository, userController, surveyService)
        );

        AnswerController answerController = new AnswerController(
                new AnswerService(answerRepository, userController)
        );

        CtrlDominioClustering ctrlDominioClustering = new CtrlDominioClustering(responseRepository, surveyRepository);

        // Inicializar drivers
        EditorQuestionDriver editorQuestionDriver = new EditorQuestionDriver(questionController);

        CreateSurveyDriver createSurveyDriver = new CreateSurveyDriver(
                surveyController,
                userController,
                editorQuestionDriver
        );

        EditSurveysDriver editSurveysDriver = new EditSurveysDriver(
                surveyController,
                userController,
                editorQuestionDriver
        );

        PasswordRecoveryDriver passwordRecoveryDriver = new PasswordRecoveryDriver(userController);
        EditResponseDriver editResponseDriver = new EditResponseDriver(responseController);
        ResponseDriver responseDriver = new ResponseDriver(surveyController, responseController, editResponseDriver);
        ClusteringDriver clusteringDriver = new ClusteringDriver(ctrlDominioClustering);
        MySurveysDriver mySurveysDriver = new MySurveysDriver(surveyController, clusteringDriver);
        SurveyDriver surveyDriver = new SurveyDriver(responseDriver, mySurveysDriver);
        SessionDriver sessionDriver = new SessionDriver(userController, passwordRecoveryDriver);


        AppDriver appDriver = new AppDriver(surveyDriver, sessionDriver, createSurveyDriver, editSurveysDriver);
        sessionDriver.setAppDriver(appDriver);

        boolean exit = false;
        while (!exit) {
            try {
                switch (selectWelcomeMenuOption()) {
                    case 1 -> sessionDriver.driverLogin();
                    case 2 -> sessionDriver.driverRegister();
                    case 3 -> passwordRecoveryDriver.recoveryMenu();
                    case 4 -> exit = true;
                    default -> System.out.println("Opción no válida.");
                }
            } catch (java.util.NoSuchElementException e) {
                exit = true;
            }
        }
        System.out.println("--- SALIENDO DE K-SURVEY ---");
        System.out.println("-----------------------------------");
    }
}
