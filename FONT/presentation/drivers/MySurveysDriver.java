package presentation.drivers;

import domain.controller.SurveyController;
import domain.model.Survey;
import presentation.driverMain.DriverMain;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class MySurveysDriver {

    private final SurveyController surveyController;
    private final ClusteringDriver clusteringDriver;
    private final Scanner sc = DriverMain.getScanner();

    public MySurveysDriver(SurveyController surveyController, ClusteringDriver clusteringDriver) {
        this.surveyController = surveyController;
        this.clusteringDriver = clusteringDriver;
    }

    public void mySurveysMenu() {
        boolean exitMySurvey = false;
        do {
            System.out.println("--- MIS ENCUESTAS ---");
            System.out.println("1. Listar mis encuestas");
            System.out.println("2. Ejecutar algoritmo de clustering");
            System.out.println("3. Atrás");
            System.out.print("Opción: ");
            try {
                String option = sc.nextLine();
                switch (option) {
                    case "1":
                        listMySurveys();
                        break;
                    case "2":
                        runClustering();
                        break;
                    case "3":
                        exitMySurvey = true;
                        break;
                    default:
                        System.out.println("Opción no válida.");
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NoSuchElementException e) {
                exitMySurvey = true;
            }
        } while (!exitMySurvey);
        System.out.println("--- SALIENDO DE MIS ENCUESTAS ---");
    }

    private void listMySurveys() {
        List<Survey> surveys = surveyController.getMySurveys();
        if (surveys.isEmpty()) {
            System.out.println("No tienes encuestas.");
        } else {
            for (Survey survey : surveys) {
                System.out.println(survey.getSURVEY_ID() + " - " + survey.getTitle());
            }
        }
    }

    private void runClustering() throws IOException {
        clusteringDriver.run();
    }

    /**
     * Método main para pruebas independientes del MySurveysDriver.
     * Permite probar la visualización de encuestas propias y clustering.
     */
    public static void main(String[] args) {
        // Inicializar repositorios
        data.UserRepository userRepository = new data.UserRepository();
        data.SurveyRepository surveyRepository = new data.SurveyRepository();
        data.ResponseRepository responseRepository = new data.ResponseRepository();
        
        // Inicializar servicios
        domain.service.UserService userService = new domain.service.UserService(userRepository);
        domain.controller.UserController userController = new domain.controller.UserController(userService);
        domain.service.SurveyService surveyService = new domain.service.SurveyService(surveyRepository, userController, userService);

        // Inicializar controladores
        domain.controller.SurveyController surveyController = new domain.controller.SurveyController(surveyService);
        domain.controller.CtrlDominioClustering clusteringController = new domain.controller.CtrlDominioClustering(responseRepository, surveyRepository);
        
        // Inicializar drivers
        ClusteringDriver clusteringDriver = new ClusteringDriver(clusteringController);
        MySurveysDriver mySurveysDriver = new MySurveysDriver(surveyController, clusteringDriver);
        
        System.out.println("=== PRUEBA MYSURVEYSDRIVER ===");
        System.out.println("Nota: Para usar esta funcionalidad necesitas tener un usuario logueado.");
        System.out.println("Por favor, inicia sesión primero.\n");
        
        // Simular login
        java.util.Scanner scanner = new java.util.Scanner(System.in);
        System.out.print("Introduce tu nombre de usuario: ");
        String username = scanner.nextLine();
        System.out.print("Introduce tu contraseña: ");
        String password = scanner.nextLine();
        
        try {
            userController.loginUser(username, password);
            System.out.println("Login exitoso. Accediendo a tus encuestas...\n");
            mySurveysDriver.mySurveysMenu();
        } catch (domain.exception.LogInException e) {
            System.out.println("Error al iniciar sesión: " + e.getMessage());
            System.out.println("Debes registrarte primero o verificar tus credenciales.");
        }
        
        scanner.close();
    }
}
