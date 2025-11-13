package presentation.drivers;

import domain.controller.SurveyController;
import domain.model.Survey;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class MySurveysDriver {

    private final SurveyController surveyController;
    private final ClusteringDriver clusteringDriver;

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
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                String option = reader.readLine();
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
}
