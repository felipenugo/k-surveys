package domain.clustering;

import domain.model.Survey;
import domain.model.Response; // This is the ResponseSet in the diagram
import domain.model.Question; // This is the Question in the diagram
import domain.clustering.QualityMetricType;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

public class ClusteringAnalysis {
    private String id;
    private String surveyId; // Corresponds to questionSetId
    private Integer k;
    private String algorithmType;
    private Date executionDate;
    private Boolean converged;
    private Integer iterations;
    private Long executionTime;
    private List<Cluster> clusters;
    private ClusteringAlgorithm algorithm;
    private List<Response> responses; // Added field
    private List<Question> questions; // Added field

    public ClusteringAnalysis(Survey survey, Integer k, ClusteringAlgorithm algorithm) {
        this.id = java.util.UUID.randomUUID().toString(); // Generate a unique ID
        this.surveyId = survey.getSURVEY_ID();
        this.k = k;
        this.algorithm = algorithm;
        this.algorithmType = algorithm.getName();
        this.executionDate = new Date();
        this.converged = false;
        this.iterations = 0;
        this.executionTime = 0L;
        this.clusters = new ArrayList<>();
        this.responses = new ArrayList<>(); // Initialize
        this.questions = new ArrayList<>(); // Initialize
    }

    public String getId() {
        return id;
    }

    public String getSurveyId() {
        return surveyId;
    }

    public Integer getK() {
        return k;
    }

    // The execute method now takes a List<Response> (which is ResponseSet in the diagram)
    // and a List<Question> (which is Question in the diagram)
    public void execute(List<Response> responses, List<Question> questions, DistanceCalculator distance) {
        long startTime = System.currentTimeMillis();
        this.responses = new ArrayList<>(responses); // Store responses
        this.questions = new ArrayList<>(questions); // Store questions
        // The algorithm.execute method needs to be adapted to take List<Response> and List<Question>
        ClusterResults results = algorithm.execute(responses, questions, k, distance);
        long endTime = System.currentTimeMillis();

        this.clusters = results.getClusters();
        this.converged = results.hasConverged();
        this.iterations = results.getIterations();
        this.executionTime = endTime - startTime;
    }

    public List<Cluster> getClusters() {
        return clusters;
    }

    public Cluster getCluster(String clusterId) {
        for (Cluster cluster : clusters) {
            if (cluster.getId().equals(clusterId)) {
                return cluster;
            }
        }
        return null; // Or throw an exception
    }

    public Boolean hasConverged() {
        return converged;
    }

    public Integer getIterations() {
        return iterations;
    }

    public Long getExecutionTime() {
        return executionTime;
    }

    public List<Response> getResponses() { // Added getter
        return responses;
    }

    public List<Question> getQuestions() { // Added getter
        return questions;
    }

    public Double calculateQuality(QualityMetricType metricType, DistanceCalculator distance) {
        QualityMetricCalculator calculator = new QualityMetricCalculator();
        switch (metricType) {
            case SILHOUETTE:
                return calculator.calculateSilhouette(this, distance);
            case CALINSKI_HARABASZ:
                return calculator.calculateCalinskiHarabasz(this, distance);
            case DAVIES_BOULDIN:
                return calculator.calculateDaviesBouldin(this, distance);
            default:
                return 0.0; // Should not happen
        }
    }

    // Placeholder for exportResults
    public void exportResults(String filePath) {
        // Implementation for exporting results
    }
}
