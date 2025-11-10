package presentation.driverMain;

import java.util.Scanner;

import domain.controller.*; // import all controllers
import domain.model.Response;
import domain.model.Survey;
import domain.service.*; // import all services
import data.*; // import all repositories
import presentation.drivers.*; // import all drivers

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
        QuestionController questionController = new QuestionController(new QuestionService(new QuestionRepository(), userController));
        ResponseController responseController = new ResponseController(new ResponseService(new ResponseRepository(), userController, surveyService));
        AnswerController answerController = new AnswerController(new AnswerService(new AnswerRepository(), userController));

        // Initialize drivers passing the controllers needed
        EditorResponseDriver editorResponseDriver = new EditorResponseDriver();
        ResponseDriver responseDriver = new ResponseDriver(surveyController, responseController, editorResponseDriver);
        MySurveysDriver mySurveysDriver = new MySurveysDriver();
        SurveyDriver surveyDriver = new SurveyDriver(responseDriver, mySurveysDriver);
        SessionDriver sessionDriver = new SessionDriver(userController);
        AppDriver appDriver = new AppDriver(surveyDriver, sessionDriver);
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
