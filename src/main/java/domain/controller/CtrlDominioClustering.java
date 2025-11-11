package domain.controller;

import data.ResponseRepository;
import data.SurveyRepository;
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
    private final ResponseRepository responseRepository;
    private final SurveyRepository surveyRepository;


    public CtrlDominioClustering() {
        this.analysisController = new AnalysisController();
        this.responseRepository = new ResponseRepository();
        this.surveyRepository = new SurveyRepository();
    }

    public String ejecutarClustering(String analisisId, String algoritmo, String surveyId, int k, int maxIter, double tolerance, String distanceMetric) {
        Survey survey = surveyRepository.getSurvey(surveyId);
        if (survey == null) {
            return "Error: Survey not found.";
        }

        Map<String, Object> config = new HashMap<>();
        config.put("maxIterations", maxIter);
        config.put("tolerance", tolerance);

        ClusteringAnalysis analysis = analysisController.createAnalysis(survey, k, algoritmo, config);

        List<Response> responses = responseRepository.getResponsesBySurveyId(surveyId);
        List<Question> questions = survey.getQuestions();
        
        DistanceType distanceType = DistanceType.valueOf(distanceMetric);
        analysisController.executeAnalysis(analysis.getId(), responses, questions, new DistanceCalculator(distanceType));
        
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
