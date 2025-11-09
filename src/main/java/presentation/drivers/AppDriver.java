package presentation.drivers;

import domain.controller.*;

import java.util.Scanner;

public class AppDriver {
    private final Scanner sc = new Scanner(System.in);
    private final SurveyDriver surveyDriver;

    public AppDriver(SurveyDriver surveyDriver) {
        this.surveyDriver = surveyDriver;
    }

    public boolean isValidOption(int firstOption, int lastOption, int option) {
        if (option < firstOption || option > lastOption) {
            System.out.println("Opción no válida. Selecciona una opción del menu");
            return false;
        } else {
            return true;
        }
    }

    private int selectAppMenu() {
        boolean exit = false;
        int option;
        System.out.println("--- K-SURVEY ---");
        do {
            System.out.println("1. RESPONDER ENCUESTAS");
            System.out.println("2. ANALIZAR MIS ENCUESTAS");
            System.out.println("3. CREAR ENCUESTA");
            System.out.println("4. CERRAR SESIÓN");
            System.out.print("Opcion: ");
            option = sc.nextInt();
            exit = isValidOption(1, 4, option);
        } while (!exit);
        return option;
    }

    public void appMenu() {
        boolean exitApp = false;
        do {
            switch (selectAppMenu()) {
                case 1 -> surveyDriver.surveyMenu();
                case 2 -> surveyDriver.surveyMenu();
                case 3 -> System.out.println("createsurveyDriver.createSurveyMenu()");
                case 4 -> {
                    System.out.println("logout");
                    System.out.println("Volver al login");
                    exitApp = true;
                }
            }
        } while (!exitApp);
    }
}

