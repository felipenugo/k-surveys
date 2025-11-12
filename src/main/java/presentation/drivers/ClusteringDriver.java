package presentation.drivers;

import domain.controller.CtrlDominioClustering;
import domain.controller.SurveyController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Driver para ejecutar análisis de clustering desde la consola.
 */
public class ClusteringDriver {

    private final CtrlDominioClustering ctrlDominioClustering;
    private final SurveyController surveyController;

    /**
     * Constructor del driver de clustering.
     * 
     * @param ctrlDominioClustering Controlador de dominio de clustering
     * @param surveyController Controlador de encuestas
     */
    public ClusteringDriver(CtrlDominioClustering ctrlDominioClustering, SurveyController surveyController) {
        this.ctrlDominioClustering = ctrlDominioClustering;
        this.surveyController = surveyController;
    }

    /**
     * Ejecuta el menú interactivo de clustering para una encuesta.
     * 
     * @param surveyId ID de la encuesta a analizar
     */
    public void run(String surveyId) {
        try {
            System.out.println("--- Menu de Clustering ---");
            System.out.println("Selecciona un algoritmo:");
            System.out.println("1. KMeans");
            System.out.println("2. KMeans++");
            System.out.println("3. KMedoids");
            System.out.print("Opcion: ");
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
                    System.out.println("Opcion invalida.");
                    return;
            }

            System.out.print("Introduce el numero de clusters (k): ");
            int k = Integer.parseInt(reader.readLine());

            System.out.println("Selecciona una metrica de distancia:");
            System.out.println("1. Euclidea");
            System.out.println("2. Manhattan");
            System.out.print("Opcion: ");
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
                    System.out.println("Opcion invalida.");
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
