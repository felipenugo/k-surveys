package presentation.driverMain;

import java.util.Scanner;

import domain.controller.*; // import all controllers
import domain.model.Response;
import domain.model.Survey;
import domain.service.*; // import all services
import data.*; // import all repositories
import presentation.drivers.*; // import all drivers

public class DriverMain {

    public static void displayWelcomeOptions() {
        System.out.println("INTRODUCE UN NÚMERO PARA SELECCIONAR UNA OPCIÓN:");
        System.out.println("1. INICIAR SESIÓN");
        System.out.println("2. REGISTRARSE");
        System.out.println("3. SALIR");
    }

    public static void displayWelcomeMessage() {
        System.out.println("-----------------------------------");
        System.out.println("--- BIENVENIDO A K-SURVEY ---");
    }

    public static void main(String[] var0) {
        Scanner sc = new Scanner(System.in);
        // Initialize controllers injecting survey with repository
        UserController userController = new UserController(new UserService(new UserRepository()));
        SurveyController surveyController = new SurveyController(new SurveyService(new SurveyRepository(), userController));
        QuestionController questionController = new QuestionController(new QuestionService(new QuestionRepository(),userController));
        ResponseController responseController = new ResponseController(new ResponseService(new ResponseRepository(), userController));
        AnswerController answerController = new AnswerController(new AnswerService(new AnswerRepository(), userController));

        // Initialize drivers passing the controllers needed
        EditorResponseDriver editorResponseDriver = new EditorResponseDriver();
        ReponseDriver responseDriver = new ReponseDriver(editorResponseDriver);
        MySurveysDriver mySurveysDriver = new MySurveysDriver();
        SurveyDriver surveyDriver = new SurveyDriver( responseDriver, mySurveysDriver);
        SessionDriver sessionDriver = new SessionDriver(userController);
        AppDriver appDriver = new AppDriver(surveyDriver, sessionDriver);
        sessionDriver.setAppDriver(appDriver);

        displayWelcomeMessage();
        boolean exit = false;
        while (!exit) {
            displayWelcomeOptions();
            String option = sc.nextLine();
            switch (option) {
                case "1" -> sessionDriver.driverLogin();

                case "2" -> sessionDriver.driverRegister();

                case "3" -> {
                    System.out.println("--- SALIENDO ---");
                    exit = true;
                }
                default -> System.out.println("Opción no válida.");
            }
        }

    }
}
