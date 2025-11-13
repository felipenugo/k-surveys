package domain.controller;

import domain.clustering.ClusteringAnalysis;
import domain.clustering.ClusteringAlgorithm;
import domain.clustering.DistanceCalculator;
import domain.clustering.DistanceType;
import domain.clustering.KMeans;
import domain.clustering.KMeansPlusPlus;
import domain.clustering.KMedoids;
import domain.clustering.QualityMetricCalculator;
import domain.clustering.QualityMetricType;
import domain.model.Survey;
import domain.model.Response; // This is the ResponseSet in the diagram
import domain.model.Question; // This is the Question in the diagram

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalysisController {
    private Map<String, ClusteringAnalysis> analyses;
    // private AnalysisRepository repository; // Assuming a repository will be implemented later
    private QualityMetricCalculator metricCalculator;

    public AnalysisController() {
        this.analyses = new HashMap<>();
        this.metricCalculator = new QualityMetricCalculator();
    }

    public ClusteringAnalysis createAnalysis(Survey survey, Integer k, String algorithmType, Map<String, Object> config) {
        ClusteringAlgorithm algorithm = createAlgorithm(algorithmType, config);
        ClusteringAnalysis analysis = new ClusteringAnalysis(survey, k, algorithm);
        analyses.put(analysis.getId(), analysis);
        return analysis;
    }

    public ClusteringAlgorithm createAlgorithm(String type, Map<String, Object> config) {
        int maxIterations = (int) config.getOrDefault("maxIterations", 100);
        double tolerance = (double) config.getOrDefault("tolerance", 1e-4);

        switch (type) {
            case "KMeans":
                return new KMeans(maxIterations, tolerance);
            case "KMeansPlusPlus":
                return new KMeansPlusPlus(maxIterations, tolerance);
            case "KMedoids":
                return new KMedoids(maxIterations, tolerance);
            default:
                throw new IllegalArgumentException("Unknown clustering algorithm type: " + type);
        }
    }

    public ClusteringAnalysis getAnalysis(String id) {
        return analyses.get(id);
    }

    public List<ClusteringAnalysis> getAnalysesByQuestionSet(String surveyId) {
        List<ClusteringAnalysis> result = new ArrayList<>();
        for (ClusteringAnalysis analysis : analyses.values()) {
            if (analysis.getSurveyId().equals(surveyId)) {
                result.add(analysis);
            }
        }
        return result;
    }

    public void executeAnalysis(String id, List<Response> responses, List<Question> questions, DistanceCalculator distance) {
        ClusteringAnalysis analysis = analyses.get(id);
        if (analysis == null) {
            throw new IllegalArgumentException("Analysis with ID " + id + " not found.");
        }
        analysis.execute(responses, questions, distance);
    }

    public Double calculateQuality(String id, QualityMetricType metricType) {
        ClusteringAnalysis analysis = analyses.get(id);
        if (analysis == null) {
            throw new IllegalArgumentException("Analysis with ID " + id + " not found.");
        }
        return analysis.calculateQuality(metricType);
    }

    public boolean deleteAnalysis(String id) {
        return analyses.remove(id) != null;
    }

    public Integer determineOptimalK(List<Response> responses, List<Question> questions, Survey survey, Integer minK, Integer maxK) {
        if (responses == null || responses.isEmpty() || minK <= 1 || maxK <= minK || maxK > responses.size()) {
            throw new IllegalArgumentException("Invalid parameters for determining optimal K.");
        }

        double bestScore = -1.0;
        int optimalK = minK;

        // Use robust defaults for the analysis
        ClusteringAlgorithm algorithm = new KMeansPlusPlus();
        DistanceCalculator distance = new DistanceCalculator(DistanceType.EUCLIDEAN);

        for (int k = minK; k <= maxK; k++) {
            ClusteringAnalysis analysis = new ClusteringAnalysis(survey, k, algorithm);
            analysis.execute(responses, questions, distance);
            
            if (analysis.hasConverged()) {
                double score = analysis.calculateQuality(QualityMetricType.SILHOUETTE);
                if (score > bestScore) {
                    bestScore = score;
                    optimalK = k;
                }
            }
        }
        return optimalK;
    }

    public List<String> getAvailableAlgorithms() {
        List<String> available = new ArrayList<>();
        available.add("KMeans");
        available.add("KMeansPlusPlus");
        available.add("KMedoids");
        return available;
    }

    public void clearAnalyses() {
        analyses.clear();
    }

    public Map<String, ClusteringAnalysis> getAnalyses() {
        return analyses;
    }
}
