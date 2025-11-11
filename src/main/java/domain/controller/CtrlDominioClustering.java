package domain.controller;

import domain.clustering.ClusterResults;
import domain.clustering.ClusteringAlgorithm;
import domain.clustering.DistanceCalculator;
import domain.clustering.DistanceType;
import domain.clustering.KMeans;
import domain.clustering.KMeansPlusPlus;
import domain.clustering.KMedoids;
import domain.model.Response;
import domain.model.Survey;
import domain.model.Question;
import domain.clustering.ClusteringAnalysis;
import domain.clustering.Cluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CtrlDominioClustering {

    private AnalysisController analysisController;

    public CtrlDominioClustering() {
        this.analysisController = new AnalysisController();
    }

    public String ejecutarClustering(String analisisId, String algoritmo, Object[][] data, int k, int maxIter, double tolerance) {
        // Dummy Survey for now, as we don't have a QuestionSetController
        Survey dummySurvey = new Survey("dummySurveyId", "Dummy Survey", "Description", "dummyUser");

        Map<String, Object> config = new HashMap<>();
        config.put("maxIterations", maxIter);
        config.put("tolerance", tolerance);

        ClusteringAnalysis analysis = analysisController.createAnalysis(dummySurvey, k, algoritmo, config);

        List<Response> responses = new ArrayList<>();
        for (Object[] datum : data) {
            Response r = new Response(analisisId, analisisId, k); // This constructor is still problematic
            responses.add(r);
        }
        
        // Need to get actual questions from the survey, but for now, use an empty list
        analysisController.executeAnalysis(analysis.getId(), responses, new ArrayList<>(), new DistanceCalculator(DistanceType.EUCLIDEAN));
        
        return "Análisis " + analisisId + " ejecutado con éxito";
    }

    public ClusterResults obtenerResultados(String analisisId) {
        ClusteringAnalysis analysis = analysisController.getAnalysis(analisisId);
        if (analysis == null) {
            throw new IllegalArgumentException("Análisis no encontrado");
        }
        // This is a temporary solution, as ClusterResults is not directly available from ClusteringAnalysis
        // We need to adapt ClusterResults or ClusteringAnalysis to provide this information
        return new ClusterResults(analysis.getClusters(), analysis.getIterations(), analysis.hasConverged());
    }

    public String obtenerResumen(String analisisId) {
        ClusteringAnalysis analysis = analysisController.getAnalysis(analisisId);
        if (analysis == null) {
            throw new IllegalArgumentException("Análisis no encontrado");
        }
        return "Resumen del análisis " + analisisId + ":\n" +
                "  - Clusters: " + analysis.getClusters().size() + "\n" +
                "  - Iteraciones: " + analysis.getIterations() + "\n" +
                "  - Convergencia: " + (analysis.hasConverged() ? "Sí" : "No");
    }

    public String obtenerInfoCluster(String analisisId, int clusterIndex) {
        ClusteringAnalysis analysis = analysisController.getAnalysis(analisisId);
        if (analysis == null) {
            throw new IllegalArgumentException("Análisis con ID " + analisisId + " no encontrado.");
        }
        List<Cluster> clusters = analysis.getClusters();
        if (clusterIndex < 0 || clusterIndex >= clusters.size()) {
            throw new IllegalArgumentException("Índice de cluster fuera de rango: " + clusterIndex);
        }
        Cluster cluster = clusters.get(clusterIndex);
        return "Información del cluster " + clusterIndex + " del análisis " + analisisId + ":\n" +
                "  - Puntos: " + cluster.getSize() + "\n" +
                "  - Distancia media: " + cluster.getAverageDistance();
    }

    public List<String> listarAnalisis() {
        return new ArrayList<>(analysisController.getAnalyses().keySet());
    }

    public void limpiarAnalisis() {
        analysisController.clearAnalyses();
    }
}
