package presentation.driverMain;

import java.util.Scanner;

import domain.controller.*; // import all controllers
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
    System.out.println("BIENVENIDO A K-SURVEY");
}

public static void main(String[] var0) {
    Scanner sc = new Scanner(System.in);
    /*SurveyController surveyController;
    QuestionController questionController;
    ResponseController responseController;
    AnswerController answerController;*/
    UserRepository userRepository = new UserRepository();
    UserService userService = new UserService(userRepository);
    UserController userController= new UserController(userService);

    SessionDriver sessionDriver = new SessionDriver(userController);

    displayWelcomeMessage();
    boolean exit = false;
    while (!exit) {
        displayWelcomeOptions();
        String option = sc.nextLine();
        switch (option) {
            case "1":
                sessionDriver.handleLogin();
                break;
            case "2":

                sessionDriver.handleRegister();
                break;
            case "3":
                System.out.println("---SALIENDO---");
                exit = true;
                break;
            default:
                System.out.println("Opción no válida.");

        }
    }

}
}
