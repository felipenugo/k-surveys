package domain.clustering;

import domain.model.Survey;
import domain.model.Response;
import domain.model.Question;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

public class ClusteringAnalysis {
    private final String id;
    private final String surveyId;
    private final Integer k;
    private final String algorithmType;
    private final LocalDateTime analysisDate;
    private Boolean converged;
    private Integer iterations;
    private Long executionTime;
    private List<Cluster> clusters;
    private final ClusteringAlgorithm algorithm;
    private List<Response> responses;
    private List<Question> questions;
    private DistanceCalculator distance;

    public ClusteringAnalysis(Survey survey, Integer k, ClusteringAlgorithm algorithm) {
        this.id = java.util.UUID.randomUUID().toString();
        this.surveyId = survey.getSURVEY_ID();
        this.k = k;
        this.algorithmType = algorithm.getName();
        this.analysisDate = LocalDateTime.now();
        this.converged = false;
        this.iterations = 0;
        this.executionTime = 0L;
        this.clusters = new ArrayList<>();
        this.algorithm = algorithm;
        this.responses = new ArrayList<>();
        this.questions = new ArrayList<>();
    }

    public String getId() { return id; }
    public String getSurveyId() { return surveyId; }
    public Integer getK() { return k; }
    public java.time.LocalDateTime getAnalysisDate() { return analysisDate; }
    public void execute(List<Response> responses, List<Question> questions, DistanceCalculator distance) { this.responses = new ArrayList<>(responses); this.questions = new ArrayList<>(questions); this.distance = distance; ClusterResults results = algorithm.execute(responses, questions, k, distance); this.clusters = results.getClusters(); this.converged = results.hasConverged(); this.iterations = results.getIterations(); }
    public List<Cluster> getClusters() { return clusters; }
    public Cluster getCluster(String clusterId) { for (Cluster cluster : clusters) if (cluster.getId().equals(clusterId)) return cluster; return null; }
    public Boolean hasConverged() { return converged; }
    public Integer getIterations() { return iterations; }
    public Long getExecutionTime() { return executionTime; }
    public List<Response> getResponses() { return responses; }
    public List<Question> getQuestions() { return questions; }
    public Double calculateQuality(QualityMetricType metricType) { QualityMetricCalculator calc = new QualityMetricCalculator(); switch (metricType) { case SILHOUETTE: return calc.calculateSilhouette(this, distance); case CALINSKI_HARABASZ: return calc.calculateCalinskiHarabasz(this, distance); case DAVIES_BOULDIN: return calc.calculateDaviesBouldin(this, distance); default: return 0.0; } }
    public Double getClusterQuality(Cluster cluster, QualityMetricType metricType) { QualityMetricCalculator calculator = new QualityMetricCalculator(); switch (metricType) { case SILHOUETTE: return calculator.calculateSilhouetteForCluster(cluster, this, this.distance); default: return 0.0; } }
    public String exportResults() { 
        StringBuilder sb = new StringBuilder(); 
        sb.append("Clustering Analysis Results\n"); 
        sb.append("=============================\n\n"); 
        sb.append("Configuration:\n"); 
        sb.append("- Analysis ID: ").append(id).append("\n"); 
        sb.append("- Survey ID: ").append(surveyId).append("\n"); 
        sb.append("- Algorithm: ").append(algorithmType).append("\n"); 
        sb.append("- K: ").append(k).append("\n"); 
        if (distance != null) { 
            sb.append("- Distance Type: ").append(distance.getDistanceType()).append("\n"); 
        } 
        sb.append("\n\n"); 
        sb.append("Execution Stats:\n"); 
        sb.append("- Converged: ").append(converged ? "Yes" : "No").append("\n"); 
        sb.append("- Iterations: ").append(iterations).append("\n"); 
        sb.append("- Execution Time: ").append(executionTime).append(" ms\n\n"); 
        sb.append("Overall Quality Metrics:\n"); 
        try { 
            sb.append(String.format("- Silhouette Score: %.4f\n", calculateQuality(QualityMetricType.SILHOUETTE))); 
        } catch (IllegalStateException e) { 
            sb.append("- Silhouette Score: Not available (" + e.getMessage() + ")\n"); 
        } 
        try { 
            sb.append(String.format("- Calinski-Harabasz Score: %.4f\n", calculateQuality(QualityMetricType.CALINSKI_HARABASZ))); 
        } catch (IllegalStateException e) { 
            sb.append("- Calinski-Harabasz Score: Not available (" + e.getMessage() + ")\n"); 
        } 
        try { 
            sb.append(String.format("- Davies-Bouldin Score: %.4f\n", calculateQuality(QualityMetricType.DAVIES_BOULDIN))); 
        } catch (IllegalStateException e) { 
            sb.append("- Davies-Bouldin Score: Not available (" + e.getMessage() + ")\n"); 
        } 
        sb.append("\n"); 
        sb.append("Clusters (" + clusters.size() + "):\n"); 
        sb.append("-----------------------------\n"); 
        for (Cluster cluster : clusters) { 
            sb.append("\nCluster " + cluster.getId() + " (Size: " + cluster.getSize() + ")\n"); 
            sb.append("----------\n"); 
            try { 
                double silhouette = getClusterQuality(cluster, QualityMetricType.SILHOUETTE); 
                sb.append(String.format("Silhouette Score: %.4f\n", silhouette)); 
            } catch (Exception e) { 
                sb.append("Silhouette Score: Not available\n"); 
            } 
            sb.append("Centroid: " + cluster.getCentroid().toString() + "\n"); 
            sb.append("Member Response IDs:\n"); 
            for (ClusterMembership member : cluster.getMembers()) { 
                sb.append("- " + member.getResponseId() + "\n"); 
            } 
        } 
        return sb.toString(); 
    }
}
