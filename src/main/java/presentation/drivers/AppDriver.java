package presentation.drivers;

import domain.controller.*;
import presentation.driverMain.DriverMain;

import java.util.Scanner;

public class AppDriver {
    private final Scanner sc = DriverMain.getScanner();
    private final SurveyDriver surveyDriver;
    private final CreateSurveyDriver createsurveyDriver;
    private final SessionDriver sessionDriver;


    public AppDriver(SurveyDriver surveyDriver, SessionDriver sessionDriver, CreateSurveyDriver createsurveyDriver) {
        this.createsurveyDriver = createsurveyDriver;
        this.surveyDriver = surveyDriver;
        this.sessionDriver = sessionDriver;
    }


    private int selectAppMenuOption() {
        System.out.println("--- K-SURVEY ---");
        System.out.println("1. RESPONDER O ANALIZAR ENCUESTAS");
        System.out.println("2. CREAR ENCUESTA");
        System.out.println("3. CERRAR SESIÓN");
        System.out.print("Opcion: ");
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
            switch (selectAppMenuOption()) {
                case 1 -> surveyDriver.surveyMenu();
                case 2 -> createsurveyDriver.createSurveyMenu();
                case 3 -> {
                    sessionDriver.logout();
                    exitApp = true;
                }
                default -> System.out.println("Opción no válida. Seleccióna una opción del menú.");
            }
        } while (!exitApp);
    }
}

