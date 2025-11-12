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

/**
 * Controlador de dominio para operaciones de clustering.
 * Gestiona la creación y ejecución de análisis de clustering sobre encuestas.
 */
public class CtrlDominioClustering {

    private AnalysisController analysisController;
    private final ResponseRepository responseRepository;
    private final SurveyRepository surveyRepository;

    /**
     * Constructor del controlador de clustering.
     * 
     * @param responseRepository Repositorio de respuestas
     * @param surveyRepository Repositorio de encuestas
     */
    public CtrlDominioClustering(ResponseRepository responseRepository, SurveyRepository surveyRepository) {
        this.analysisController = new AnalysisController();
        this.responseRepository = responseRepository;
        this.surveyRepository = surveyRepository;
    }

    /**
     * Ejecuta un análisis de clustering sobre una encuesta.
     * 
     * @param analisisId ID del análisis a crear (no usado, se genera automáticamente)
     * @param algoritmo Nombre del algoritmo (KMeans, KMeans++, KMedoids)
     * @param surveyId ID de la encuesta
     * @param k Número de clusters
     * @param maxIter Iteraciones máximas
     * @param tolerance Tolerancia de convergencia
     * @param distanceMetric Métrica de distancia (EUCLIDEAN, MANHATTAN)
     * @return ID real del análisis ejecutado
     */
    public String ejecutarClustering(String analisisId, String algoritmo, String surveyId, int k, int maxIter, double tolerance, String distanceMetric) {
        Survey survey = surveyRepository.getSurvey(surveyId);
        if (survey == null) {
            return null;
        }

        Map<String, Object> config = new HashMap<>();
        config.put("maxIterations", maxIter);
        config.put("tolerance", tolerance);

        ClusteringAnalysis analysis = analysisController.createAnalysis(survey, k, algoritmo, config);

        List<Response> responses = responseRepository.getResponsesBySurveyId(surveyId);
        List<Question> questions = survey.getQuestions();
        
        DistanceType distanceType = DistanceType.valueOf(distanceMetric);
        analysisController.executeAnalysis(analysis.getId(), responses, questions, new DistanceCalculator(distanceType));
        
        return analysis.getId();
    }

    /**
     * Obtiene los resultados de un análisis.
     * 
     * @param analisisId ID del análisis
     * @return Resultados del clustering
     * @throws IllegalArgumentException si el análisis no existe
     */
    public ClusterResults obtenerResultados(String analisisId) {
        ClusteringAnalysis analysis = analysisController.getAnalysis(analisisId);
        if (analysis == null) {
            throw new IllegalArgumentException("Analisis no encontrado");
        }
        // This is a temporary solution, as ClusterResults is not directly available from ClusteringAnalysis
        // We need to adapt ClusterResults or ClusteringAnalysis to provide this information
        return new ClusterResults(analysis.getClusters(), analysis.getIterations(), analysis.hasConverged());
    }

    /**
     * Obtiene un resumen textual del análisis.
     * 
     * @param analisisId ID del análisis
     * @return Resumen del análisis
     * @throws IllegalArgumentException si el análisis no existe
     */
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

    /**
     * Obtiene información detallada de un cluster específico.
     * 
     * @param analisisId ID del análisis
     * @param clusterIndex Índice del cluster
     * @return Información del cluster
     * @throws IllegalArgumentException si el análisis o cluster no existe
     */
    public String obtenerInfoCluster(String analisisId, int clusterIndex) {
        ClusteringAnalysis analysis = analysisController.getAnalysis(analisisId);
        if (analysis == null) {
            throw new IllegalArgumentException("Analisis con ID " + analisisId + " no encontrado.");
        }
        List<Cluster> clusters = analysis.getClusters();
        if (clusterIndex < 0 || clusterIndex >= clusters.size()) {
            throw new IllegalArgumentException("Indice de cluster fuera de rango: " + clusterIndex);
        }
        Cluster cluster = clusters.get(clusterIndex);
        return "Información del cluster " + clusterIndex + " del análisis " + analisisId + ":\n" +
                "  - Puntos: " + cluster.getSize() + "\n" +
                "  - Distancia media: " + cluster.getAverageDistance();
    }

    /**
     * Lista todos los IDs de análisis almacenados.
     * 
     * @return Lista de IDs de análisis
     */
    public List<String> listarAnalisis() {
        return new ArrayList<>(analysisController.getAnalyses().keySet());
    }

    /**
     * Limpia todos los análisis almacenados.
     */
    public void limpiarAnalisis() {
        analysisController.clearAnalyses();
    }
}
