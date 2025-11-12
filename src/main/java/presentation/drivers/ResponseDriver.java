
package presentation.drivers;

import java.util.List;
import java.util.Scanner;

import domain.controller.ResponseController;
import domain.exception.ResponseException;
import domain.exception.SurveyException;

import domain.controller.SurveyController;
import domain.model.Survey;

public class ResponseDriver {
    private final SurveyController surveyController;
    private final ResponseController responseController;
    private final EditResponseDriver editResponseDriver;
    private final Scanner sc = new Scanner(System.in);

    public ResponseDriver(SurveyController surveyController, ResponseController responseController, EditResponseDriver editResponseDriver) {
        this.surveyController = surveyController;
        this.responseController = responseController;
        this.editResponseDriver = editResponseDriver;
    }

    public int selectResponseMenuOption() {
        System.out.println("--- RESPONDER ENCUESTAS ---");
        System.out.println("1. SELECCIONAR UNA ENCUESTA PARA RESPONDER");
        System.out.println("2. VOLVER ATRÁS");
        System.out.print("Opción: ");
        return sc.nextInt();
    }

    private int selectErrorMenuOption() {
        System.out.println("1. Intentar de nuevo");
        System.out.println("2. Volver atrás");
        System.out.print("Opción: ");
        return sc.nextInt();
    }

    public void showSurveys(List<Survey> surveys) {
        for (Survey survey : surveys) {
            System.out.println("id: " + survey.getSURVEY_ID() + ", título: " + survey.getTitle() + ", autor: " + survey.getCREATOR_USERNAME() + ", número de respuestas: " + survey.getViews());
            System.out.println("descripción: " + survey.getDescription() + "\n");
        }
        String cleanBuffer = sc.nextLine();
    }

    public void selectSurvey() {
        boolean exit = false;
        do {
            try {
                var surveys = surveyController.getSelectedSurveys();
                showSurveys(surveys);
                System.out.print("Selecciona el id de una encuesta: ");
                String surveyIdSelected = sc.nextLine();
                String responseId = responseController.startResponse(surveyIdSelected);
                editResponseDriver.editResponseMenu(surveyIdSelected, responseId);
                exit = true;
            } catch ( SurveyException | ResponseException e) {
                System.out.println("Error: " + e.getMessage());
                boolean exitErrorMenu = false;
                do {
                    switch (selectErrorMenuOption()) {
                        case 1 -> exitErrorMenu = true;
                        case 2 -> {
                            exit = true;
                            exitErrorMenu = true;
                        }
                        default -> System.out.println("Opción no válida. Selecciona una opción del menú");
                    }
                } while (!exitErrorMenu);
            }
        } while (!exit);
    }


    public void responseMenu() {
        boolean exitResponseMenu = false;
        do {
            switch (selectResponseMenuOption()) {
                case 1 -> selectSurvey();
                case 2 -> exitResponseMenu = true;
                default -> System.out.println("Opción no válida. Selecciona una opción del menú");
            }
        } while (!exitResponseMenu);
        System.out.println("--- saliendo de responder encuesta ---");
    }
}
