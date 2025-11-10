package presentation.drivers;

import java.util.ArrayList;
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
    private final EditorResponseDriver editorResponseDriver;
    private final Scanner sc = new Scanner(System.in);

    public ResponseDriver(SurveyController surveyController, ResponseController responseController, EditorResponseDriver editorResponseDriver) {
        this.surveyController = surveyController;
        this.responseController = responseController;
        this.editorResponseDriver = editorResponseDriver;
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
        int index = 0;
        for (Survey survey : surveys) {
            System.out.print(index + ". ");
            System.out.println("id: " + survey.getSURVEY_ID());
            System.out.println("título: " + survey.getTitle());
            System.out.println("descripción: " + survey.getDescription());
            System.out.println("autor: " + survey.getCREATOR_USERNAME() + "\n");
        }
    }

    public void selectSurvey() {
        boolean exit = false;
        do {
            try {
                var surveys = surveyController.getSelectedSurveys();
                showSurveys(surveys);
                System.out.print("Selecciona una encuesta: ");
                String surveyIdSelected = sc.nextLine();
                System.out.print("Introduce un id para tu respuesta: ");
                String responseId = sc.nextLine();
                responseController.startResponse(surveyIdSelected, responseId);
                editorResponseDriver.editorResponseMenu(surveyIdSelected, responseId);
                exit = true;
            } catch (SurveyException | ResponseException e) {
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

    public void showSurveysMenu() {
        boolean exitSelectSurveyMenu = false;
        do {
            System.out.println("1. RESPONDER ENCUESTA");
            System.out.println("2. VOLVER ATRÁS");
            System.out.print("Opción: ");
            switch (sc.nextInt()) {
                case 1 -> selectSurvey();
                case 2 -> exitSelectSurveyMenu = true;
                default -> System.out.println("Opción no válida. Selecciona una opción del menú.");
            }
        } while (!exitSelectSurveyMenu);
    }

    public void responseMenu() {
        boolean exitResponseMenu = false;
        do {
            switch (selectResponseMenuOption()) {
                case 1 -> showSurveysMenu();
                case 2 -> exitResponseMenu = true;
                default -> System.out.println("Opción no válida. Selecciona una opción del menú");
            }
        } while (!exitResponseMenu);
        System.out.println("--- saliendo de responder encuesta---");
    }
}
