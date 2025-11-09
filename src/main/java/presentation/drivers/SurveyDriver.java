package presentation.drivers;

import domain.controller.SurveyController;

import java.util.Scanner;

public class SurveyDriver {
    private final Scanner sc = new Scanner(System.in);
    private final SurveyController surveyController;
    private final ReponseDriver reponseDriver;

    public SurveyDriver(SurveyController surveyController, ReponseDriver responseDriver) {
        this.surveyController = surveyController;
        this.reponseDriver = responseDriver;
    }

    public boolean isValidOption(int firstOption, int lastOption, int option) {
        if (option < firstOption || option > lastOption) {
            System.out.println("Opción no válida. Selecciona una opción del menu");
            return false;
        } else {
            return true;
        }
    }

    public int selectSurveyMenuOption() {
        boolean exit = false;
        int option;
        System.out.println("--- ENCUESTAS ---");
        do {
            System.out.println("1. RESPONDER ENCUESTA");
            System.out.println("2. ANALIZAR MIS ENCUESTAS");
            System.out.println("3. VOLVER AL MENÚ PRINCIPAL");
            option = sc.nextInt();
            exit = isValidOption(1, 3, option);
        } while (!exit);
        return option;
    }

    public void surveyMenu() {
        boolean exitSurveyMenu = false;
        do {
            switch (selectSurveyMenuOption()) {
                case 1 -> reponseDriver.responseMenu();

            }
        } while (!exitSurveyMenu);
    }

    public void responseSurvey() {

    }

    public void analyzeSurvey() {

    }
}
