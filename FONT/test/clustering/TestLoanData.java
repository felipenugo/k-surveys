package clustering;

import domain.clustering.*;
import domain.model.*;
import domain.model.enums.TypeQuestion;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TestLoanData {

    // --- PARÁMETROS CONFIGURABLES ---
    private static final int K = 2;
    private static final DistanceType DISTANCE_TYPE = DistanceType.EUCLIDEAN;
    private static final QualityMetricType QUALITY_TYPE = QualityMetricType.SILHOUETTE;
    private static final String ALGORITHM = "KMedoids"; // Opciones: "KMeans", "KMeansPlusPlus", "KMedoids"
    // --------------------------------

    private ClusteringAlgorithm clusteringAlgorithm;
    private DistanceCalculator distanceCalc;
    private List<Question> questions;
    private List<Response> data;

    @Before
    public void setUp() throws IOException {
        switch (ALGORITHM) {
            case "KMeansPlusPlus":
                clusteringAlgorithm = new KMeansPlusPlus(10, 1e-5);
                break;
            case "KMedoids":
                clusteringAlgorithm = new KMedoids(10, 1e-5);
                break;
            case "KMeans":
            default:
                clusteringAlgorithm = new KMeans(10, 1e-5);
                break;
        }
        distanceCalc = new DistanceCalculator(DISTANCE_TYPE);

        questions = new ArrayList<>();
        data = new ArrayList<>();

        // Leer el archivo CSV y crear la encuesta, preguntas y respuestas
        String csvFile = "../DATA/loan-test.csv";
        String line = "";
        String cvsSplitBy = ",";

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            // Leer el encabezado para crear las preguntas
            String[] headers = br.readLine().split(cvsSplitBy);
            for (int i = 0; i < headers.length; i++) {
                Question q = new Question(i, "loan-survey");
                q.setQuestionText(headers[i]);
                // Por simplicidad, trataremos todas las preguntas como textuales por ahora
                q.setTypeQuestion(TypeQuestion.TEXTUAL);
                questions.add(q);
            }

            // Leer el resto de las líneas para crear las respuestas
            int responseId = 0;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(cvsSplitBy);
                Response r = new Response(String.valueOf(responseId++), "loan-survey", "user" + responseId, questions);
                for (int i = 0; i < values.length; i++) {
                    TextualAnswer a = new TextualAnswer(i, r.getRESPONSE_ID());
                    a.setAnswerText(values[i]);
                    r.updateAnswer(i, a);
                }
                data.add(r);
            }
        }
    }

    @Test
    public void testClusteringWithLoanData() throws IOException {
        // Crear un nuevo ClusteringAnalysis
        ClusteringAnalysis analysis = new ClusteringAnalysis(new Survey("loan-survey", "Loan Survey", "user1"), K, clusteringAlgorithm);

        // Ejecutar clustering
        analysis.execute(data, questions, distanceCalc);

        // Aserciones básicas
        Assert.assertNotNull(analysis);
        Assert.assertEquals(K, analysis.getClusters().size());
        Assert.assertTrue(analysis.hasConverged());
        Assert.assertEquals(data.size(), analysis.getResponses().size());

        // Calcular calidad
        double quality = analysis.calculateQuality(QUALITY_TYPE);

        // Escribir resultados a archivo
        try (FileWriter writer = new FileWriter("clustering_results.txt")) {
            writer.write(analysis.exportResults());
        }
    }
}
