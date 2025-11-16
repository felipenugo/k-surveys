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

/**
 * Controlador responsable de gestionar los análisis de clustering realizados sobre
 * las respuestas de una encuesta.
 *
 * Esta clase actúa como intermediaria entre la lógica de negocio del clustering
 * (algoritmos, distancias, métricas de calidad…) y la capa de presentación.
 * 
 * Permite crear análisis, ejecutar algoritmos, obtener resultados, exportarlos
 * y consultarlos según distintos criterios.
 *
 * El controlador mantiene un repositorio interno en memoria (un {@code Map})
 * donde se almacenan los análisis creados durante la ejecución del programa.
 *
 * En versiones futuras, este repositorio podrá ser reemplazado por un 
 * {@code AnalysisRepository} persistente.
 */
public class AnalysisController {
    /** Repositorio en memoria para almacenar los análisis de clustering creados. */
    private Map<String, ClusteringAnalysis> analyses;
    // private AnalysisRepository repository; // Assuming a repository will be implemented later
    private QualityMetricCalculator metricCalculator;

    /**
     * Crea una nueva instancia del controlador de análisis.
     */
    public AnalysisController() {
        this.analyses = new HashMap<>();
        this.metricCalculator = new QualityMetricCalculator();
    }

    /**
     * Crea un nuevo análisis de clustering.
     *
     * @param survey        encuesta sobre la que se realiza el análisis
     * @param k             número de clusters
     * @param algorithmType tipo de algoritmo de clustering a utilizar
     * @param config        configuración específica para el algoritmo
     * @return análisis de clustering creado
     */
    public ClusteringAnalysis createAnalysis(Survey survey, Integer k, String algorithmType, Map<String, Object> config) {
        ClusteringAlgorithm algorithm = createAlgorithm(algorithmType, config);
        ClusteringAnalysis analysis = new ClusteringAnalysis(survey, k, algorithm);
        analyses.put(analysis.getId(), analysis);
        return analysis;
    }

    /**
     * Crea un algoritmo de clustering según el tipo y configuración especificados.
     *
     * @param type   tipo de algoritmo (por ejemplo, "KMeans", "KMeansPlusPlus", "KMedoids")
     * @param config configuración específica para el algoritmo
     * @return instancia del algoritmo de clustering creado
     */
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

    /**
     * Obtiene un análisis de clustering por su identificador.
     *
     * @param id identificador del análisis
     * @return análisis de clustering correspondiente, o {@code null} si no existe
     */
    public ClusteringAnalysis getAnalysis(String id) {
        return analyses.get(id);
    }

    /**
     * Obtiene una lista de análisis de clustering asociados a una encuesta específica.
     *
     * @param surveyId identificador de la encuesta
     * @return lista de análisis de clustering correspondientes
     */
    public List<ClusteringAnalysis> getAnalysesByQuestionSet(String surveyId) {
        List<ClusteringAnalysis> result = new ArrayList<>();
        for (ClusteringAnalysis analysis : analyses.values()) {
            if (analysis.getSurveyId().equals(surveyId)) {
                result.add(analysis);
            }
        }
        return result;
    }

    /**
     * Ejecuta un análisis de clustering específico.
     *
     * @param id        identificador del análisis
     * @param responses respuestas a analizar
     * @param questions preguntas asociadas a las respuestas
     * @param distance  calculadora de distancia a utilizar
     */
    public void executeAnalysis(String id, List<Response> responses, List<Question> questions, DistanceCalculator distance) {
        ClusteringAnalysis analysis = analyses.get(id);
        if (analysis == null) {
            throw new IllegalArgumentException("Analysis with ID " + id + " not found.");
        }
        analysis.execute(responses, questions, distance);
    }

    /**
     * Calcula la métrica de calidad de un análisis de clustering específico.
     *
     * @param id         identificador del análisis
     * @param metricType tipo de métrica de calidad a calcular
     * @return valor de la métrica de calidad calculada
     */
    public Double calculateQuality(String id, QualityMetricType metricType) {
        ClusteringAnalysis analysis = analyses.get(id);
        if (analysis == null) {
            throw new IllegalArgumentException("Analysis with ID " + id + " not found.");
        }
        return analysis.calculateQuality(metricType);
    }

    /**
     * Elimina un análisis de clustering por su identificador.
     *
     * @param id identificador del análisis a eliminar
     * @return {@code true} si el análisis fue eliminado, {@code false} si no existía
     */
    public boolean deleteAnalysis(String id) {
        return analyses.remove(id) != null;
    }

    /**
     * Determina el número óptimo de clusters (K) para un análisis de clustering dado.
     *
     * @param responses respuestas a analizar
     * @param questions preguntas asociadas a las respuestas
     * @param survey   encuesta sobre la que se realiza el análisis
     * @param minK     número mínimo de clusters a considerar
     * @param maxK     número máximo de clusters a considerar
     * @return número óptimo de clusters determinado
     */
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
    
    /**
     * Obtiene una lista de algoritmos de clustering disponibles.
     *
     * @return lista de nombres de algoritmos disponibles
     */
    public List<String> getAvailableAlgorithms() {
        List<String> available = new ArrayList<>();
        available.add("KMeans");
        available.add("KMeansPlusPlus");
        available.add("KMedoids");
        return available;
    }

    /**
     * Elimina todos los análisis almacenados en el controlador.
     */
    public void clearAnalyses() {
        analyses.clear();
    }

    /**
     * Obtiene todos los análisis almacenados en el controlador.
     *
     * @return mapa de análisis de clustering
     */
    public Map<String, ClusteringAnalysis> getAnalyses() {
        return analyses;
    }
}
