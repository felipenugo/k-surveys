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

    // --- CONFIGURABLE PARAMETERS ---
    private static final int K = 3;
    private static final DistanceType DISTANCE_TYPE = DistanceType.EUCLIDEAN;
    private static final QualityMetricType QUALITY_TYPE = QualityMetricType.SILHOUETTE;
    private static final String ALGORITHM = "KMeans"; // Options: "KMeans", "KMeansPlusPlus", "KMedoids"
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

        // Read the CSV file and create the survey, questions, and responses
        String csvFile = "loan-test.csv";
        String line = "";
        String cvsSplitBy = ",";

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            // Read the header to create the questions
            String[] headers = br.readLine().split(cvsSplitBy);
            for (int i = 0; i < headers.length; i++) {
                Question q = new Question(i, "loan-survey");
                q.setQuestionText(headers[i]);
                // For simplicity, we'll treat all questions as textual for now
                q.setTypeQuestion(TypeQuestion.TEXTUAL);
                questions.add(q);
            }

            // Read the rest of the lines to create the responses
            int responseId = 0;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(cvsSplitBy);
                Response r = new Response(String.valueOf(responseId++), "loan-survey", "user" + responseId, questions.size());
                for (int i = 0; i < values.length; i++) {
                    TextualAnswer a = new TextualAnswer(i, r.getRESPONSE_ID(), values[i]);
                    r.updateAnswer(i, a);
                }
                data.add(r);
            }
        }
    }

    @Test
    public void testClusteringWithLoanData() throws IOException {
        // Create a new ClusteringAnalysis
        ClusteringAnalysis analysis = new ClusteringAnalysis(new Survey("loan-survey", "Loan Survey", "user1"), K, clusteringAlgorithm);

        // Execute clustering
        analysis.execute(data, questions, distanceCalc);

        // Basic assertions
        Assert.assertNotNull(analysis);
        Assert.assertEquals(K, analysis.getClusters().size());
        Assert.assertTrue(analysis.hasConverged());
        Assert.assertEquals(data.size(), analysis.getResponses().size());

        // Calculate quality
        double quality = analysis.calculateQuality(QUALITY_TYPE, distanceCalc);

        // Write results to file
        try (FileWriter writer = new FileWriter("clustering_results.txt")) {
            writer.write("Clustering Results\n");
            writer.write("==================\n\n");
            writer.write("Configuration:\n");
            writer.write("- Algorithm: " + ALGORITHM + "\n");
            writer.write("- K: " + K + "\n");
            writer.write("- Distance Type: " + DISTANCE_TYPE + "\n");
            writer.write("- Quality Metric: " + QUALITY_TYPE + "\n\n");

            writer.write("Clustering Quality:\n");
            writer.write("- " + QUALITY_TYPE + ": " + quality + "\n\n");

            for (Cluster cluster : analysis.getClusters()) {
                writer.write("Cluster " + cluster.getId() + "\n");
                writer.write("----------\n");
                writer.write("Centroid: " + cluster.getCentroid().toString() + "\n");
                writer.write("Responses:\n");
                for (ClusterMembership member : cluster.getMembers()) {
                    Optional<Response> response = data.stream().filter(r -> r.getRESPONSE_ID().equals(member.getResponseSetId())).findFirst();
                    response.ifPresent(r -> {
                        try {
                            writer.write("- " + r.getRESPONSE_ID() + "\n");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }
                writer.write("\n");
            }
        }
    }
}
