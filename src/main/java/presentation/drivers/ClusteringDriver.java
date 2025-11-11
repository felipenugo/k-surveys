package presentation.drivers;

import domain.controller.CtrlDominioClustering;
import domain.controller.SurveyController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class ClusteringDriver {

    private final CtrlDominioClustering ctrlDominioClustering;
    private final SurveyController surveyController;

    public ClusteringDriver(CtrlDominioClustering ctrlDominioClustering, SurveyController surveyController) {
        this.ctrlDominioClustering = ctrlDominioClustering;
        this.surveyController = surveyController;
    }

    public void run(String surveyId) {
        try {
            System.out.println("--- Clustering Menu ---");
            System.out.println("Select an algorithm:");
            System.out.println("1. KMeans");
            System.out.println("2. KMeans++");
            System.out.println("3. KMedoids");
            System.out.print("Option: ");
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String algorithmOption = reader.readLine();
            String algorithm = "";
            switch (algorithmOption) {
                case "1":
                    algorithm = "KMeans";
                    break;
                case "2":
                    algorithm = "KMeans++";
                    break;
                case "3":
                    algorithm = "KMedoids";
                    break;
                default:
                    System.out.println("Invalid option.");
                    return;
            }

            System.out.print("Enter the number of clusters (k): ");
            int k = Integer.parseInt(reader.readLine());

            System.out.println("Select a distance metric:");
            System.out.println("1. Euclidean");
            System.out.println("2. Manhattan");
            System.out.print("Option: ");
            String distanceOption = reader.readLine();
            String distanceMetric = "";
            switch (distanceOption) {
                case "1":
                    distanceMetric = "EUCLIDEAN";
                    break;
                case "2":
                    distanceMetric = "MANHATTAN";
                    break;
                default:
                    System.out.println("Invalid option.");
                    return;
            }

            String analysisId = surveyId + "_" + algorithm + "_k" + k + "_" + System.currentTimeMillis();
            String result = ctrlDominioClustering.ejecutarClustering(analysisId, algorithm, surveyId, k, 100, 1e-4, distanceMetric);
            System.out.println(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
