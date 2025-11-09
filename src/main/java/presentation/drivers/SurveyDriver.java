package presentation.drivers;

import domain.controller.SurveyController;

import java.util.Scanner;

public class SurveyDriver {
    private final Scanner sc = new Scanner(System.in);
    private final ReponseDriver reponseDriver;
    private final MySurveysDriver mySurveysDriver;

    public SurveyDriver(ReponseDriver responseDriver, MySurveysDriver mySurveysDriver) {
        this.reponseDriver = responseDriver;
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
                case 1 -> reponseDriver.responseMenu();
                case 2 -> mySurveysDriver.mySurveysMenu();
                case 3 -> exitSurveyMenu = true;
                default -> System.out.println("Opción no válida. Selecciona una opción del menú.");
            }
        } while (!exitSurveyMenu);
        System.out.println("--- VOLVIENDO AL MENÚ PRINCIPAL ---");
    }
}
