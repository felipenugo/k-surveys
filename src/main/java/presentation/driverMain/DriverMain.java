package presentation.driverMain;

import java.util.Scanner;

import domain.controller.*;
import domain.service.*;
import data.*;
import presentation.drivers.*;

public class DriverMain {

    public static int selectWelcomeMenuOption() {
        Scanner sc = new Scanner(System.in);
        System.out.println("-----------------------------------");
        System.out.println("--- BIENVENIDO A K-SURVEY ---");
        System.out.println("1. INICIAR SESIÓN");
        System.out.println("2. REGISTRARSE");
        System.out.println("3. SALIR");
        System.out.print("Opción: ");
        return sc.nextInt();
    }

    public static void main(String[] var0) {
        Scanner sc = new Scanner(System.in);
        // Initialize controllers injecting survey with repository
        UserController userController = new UserController(new UserService(new UserRepository()));

        SurveyService surveyService = new SurveyService(new SurveyRepository(), userController);
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

        // Initialize drivers
        EditorQuestionDriver editorQuestionDriver = new EditorQuestionDriver(questionController);

        CreateSurveyDriver createSurveyDriver = new CreateSurveyDriver(
                surveyController,
                userController,
                editorQuestionDriver
        );

        EditResponseDriver editResponseDriver = new EditResponseDriver(responseController);
        ResponseDriver responseDriver = new ResponseDriver(surveyController, responseController, editResponseDriver);
        ClusteringDriver clusteringDriver = new ClusteringDriver(ctrlDominioClustering, surveyController);
        MySurveysDriver mySurveysDriver = new MySurveysDriver(surveyController, clusteringDriver);
        SurveyDriver surveyDriver = new SurveyDriver(responseDriver, mySurveysDriver);

        SessionDriver sessionDriver = new SessionDriver(userController);
        AppDriver appDriver = new AppDriver(surveyDriver, sessionDriver, createSurveyDriver);
        sessionDriver.setAppDriver(appDriver);

        boolean exit = false;
        while (!exit) {
            switch (selectWelcomeMenuOption()) {
                case 1 -> sessionDriver.driverLogin();
                case 2 -> sessionDriver.driverRegister();
                case 3 -> exit = true;
                default -> System.out.println("Opción no válida.");
            }
        }
        System.out.println("--- SALIENDO DE K-SURVEY ---");
        System.out.println("-----------------------------------");
    }
}
