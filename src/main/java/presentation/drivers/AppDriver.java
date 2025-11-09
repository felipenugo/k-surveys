package presentation.drivers;

import domain.controller.*;

import java.util.Scanner;

public class AppDriver {
    private final Scanner sc = new Scanner(System.in);
    private final SurveyDriver surveyDriver;
    private final SessionDriver sessionDriver;

    public AppDriver(SurveyDriver surveyDriver, SessionDriver sessionDriver) {
        this.surveyDriver = surveyDriver;
        this.sessionDriver = sessionDriver;
    }


    private int selectAppMenu() {
        System.out.println("--- K-SURVEY ---");
        System.out.println("1. RESPONDER O ANALIZAR ENCUESTAS");
        System.out.println("2. CREAR ENCUESTA");
        System.out.println("3. CERRAR SESIÓN");
        System.out.print("Opcion: ");
        return sc.nextInt();
    }

    public void appMenu() {
        boolean exitApp = false;
        do {
            switch (selectAppMenu()) {
                case 1 -> surveyDriver.surveyMenu();
                case 2 -> System.out.println("createsurveyDriver.createSurveyMenu()");
                case 3 -> {
                    sessionDriver.logout();
                    exitApp = true;
                }
                default -> System.out.println("Opción no válida. Seleccióna una opción del menú.");
            }
        } while (!exitApp);
    }
}

