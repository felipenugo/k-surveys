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
            System.out.println("--- MY SURVEYS ---");
            System.out.println("1. List my surveys");
            System.out.println("2. Run clustering algorithm");
            System.out.println("3. Back");
            System.out.print("Option: ");
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
                        System.out.println("Invalid option.");
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } while (!exitMySurvey);
        System.out.println("--- EXITING MY SURVEYS ---");
    }

    private void listMySurveys() {
        List<Survey> surveys = surveyController.getMySurveys();
        if (surveys.isEmpty()) {
            System.out.println("You have no surveys.");
        } else {
            for (Survey survey : surveys) {
                System.out.println(survey.getSURVEY_ID() + " - " + survey.getTitle());
            }
        }
    }

    private void runClustering() throws IOException {
        listMySurveys();
        System.out.print("Select a survey to run the clustering algorithm on: ");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String surveyId = reader.readLine();
         if(surveyController.existsSurvey(surveyId)){
            clusteringDriver.run(surveyId);
        } else {
            System.out.println("Invalid survey ID.");
        }
    }
}
