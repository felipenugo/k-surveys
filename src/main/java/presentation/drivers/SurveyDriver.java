package presentation.drivers;

import domain.controller.SurveyController;
import presentation.driverMain.DriverMain;

import java.util.Scanner;

public class SurveyDriver {
    private final Scanner sc = DriverMain.getScanner();
    private final ResponseDriver responseDriver;
    private final MySurveysDriver mySurveysDriver;

    public SurveyDriver(ResponseDriver responseDriver, MySurveysDriver mySurveysDriver) {
        this.responseDriver = responseDriver;
        this.mySurveysDriver = mySurveysDriver;
    }

    public int selectSurveyMenuOption() {
        System.out.println("--- ENCUESTAS ---");
        System.out.println("1. RESPONDER ENCUESTAS");
        System.out.println("2. ANALIZAR MIS ENCUESTAS");
        System.out.println("3. VOLVER AL MENÚ PRINCIPAL");
        System.out.print("Opción: ");
        return sc.nextInt();
    }

    public void surveyMenu() {
        boolean exitSurveyMenu = false;
        do {
            switch (selectSurveyMenuOption()) {
                case 1 -> responseDriver.responseMenu();
                case 2 -> mySurveysDriver.mySurveysMenu();
                case 3 -> exitSurveyMenu = true;
                default -> System.out.println("Opción no válida. Selecciona una opción del menú.");
            }
        } while (!exitSurveyMenu);
        System.out.println("--- volviendo al menú principal ---");
    }
}
