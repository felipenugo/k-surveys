package domain.clustering;

import domain.model.Survey;
import domain.model.Response;
import domain.model.Question;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

/**
 * Representa un análisis de clustering completo sobre una encuesta.
 * Coordina la ejecución del algoritmo y almacena los resultados.
 */
public class ClusteringAnalysis {
    private final String id;
    private final String surveyId;
    private final Integer k;
    private final String algorithmType;
    private final LocalDateTime analysisDate; // Changed from Date to LocalDateTime
    private Boolean converged;
    private Integer iterations;
    private Long executionTime;
    private List<Cluster> clusters;
    private final ClusteringAlgorithm algorithm;
    private List<Response> responses;
    private List<Question> questions;
    private DistanceCalculator distance;

    /**
     * Constructor que inicializa un análisis de clustering.
     *
     * @param survey    Encuesta sobre la que realizar el clustering
     * @param k         Número de clusters a generar
     * @param algorithm Algoritmo de clustering a utilizar
     */
    public ClusteringAnalysis(Survey survey, Integer k, ClusteringAlgorithm algorithm) {
        this.id = java.util.UUID.randomUUID().toString(); // Generate a unique ID
        this.surveyId = survey.getSURVEY_ID();
        this.k = k;
        this.algorithm = algorithm;
        this.algorithmType = algorithm.getName();
        this.analysisDate = LocalDateTime.now(); // Use LocalDateTime
        this.converged = false;
        this.iterations = 0;
        this.executionTime = 0L;
        this.clusters = new ArrayList<>();
        this.responses = new ArrayList<>();
        this.questions = new ArrayList<>();
    }

    /**
     * Obtiene el ID único del análisis.
     *
     * @return ID del análisis
     */
    public String getId() {
        return id;
    }

    /**
     * Obtiene el ID de la encuesta analizada.
     *
     * @return ID de la encuesta
     */
    public String getSurveyId() {
        return surveyId;
    }

    /**
     * Obtiene el número de clusters configurado.
     *
     * @return Número de clusters (k)
     */
    public Integer getK() {
        return k;
    }

    /**
     * Obtiene la fecha y hora de creación del análisis.
     *
     * @return La fecha y hora del análisis.
     */
    public LocalDateTime getAnalysisDate() {
        return analysisDate;
    }

    /**
     * Ejecuta el análisis de clustering con los datos proporcionados.
     *
     * @param responses Lista de respuestas a agrupar
     * @param questions Lista de preguntas de la encuesta
     * @param distance  Calculadora de distancia a utilizar
     */
    public void execute(List<Response> responses, List<Question> questions, DistanceCalculator distance) {
        long startTime = System.currentTimeMillis();
        this.responses = new ArrayList<>(responses);
        this.questions = new ArrayList<>(questions);
        this.distance = distance;
        ClusterResults results = algorithm.execute(responses, questions, k, distance);
        long endTime = System.currentTimeMillis();

        this.clusters = results.getClusters();
        this.converged = results.hasConverged();
        this.iterations = results.getIterations();
        this.executionTime = endTime - startTime;
    }

    /**
     * Obtiene la lista de clusters resultantes.
     *
     * @return Lista de clusters
     */
    public List<Cluster> getClusters() {
        return clusters;
    }

    /**
     * Obtiene un cluster específico por su ID.
     *
     * @param clusterId ID del cluster a buscar
     * @return Cluster encontrado o null
     */
    public Cluster getCluster(String clusterId) {
        for (Cluster cluster : clusters) {
            if (cluster.getId().equals(clusterId)) {
                return cluster;
            }
        }
        return null; // Or throw an exception
    }

    /**
     * Indica si el algoritmo alcanzó convergencia.
     *
     * @return true si convergió
     */
    public Boolean hasConverged() {
        return converged;
    }

    /**
     * Obtiene el número de iteraciones ejecutadas.
     *
     * @return Número de iteraciones
     */
    public Integer getIterations() {
        return iterations;
    }

    /**
     * Obtiene el tiempo de ejecución en milisegundos.
     *
     * @return Tiempo de ejecución
     */
    public Long getExecutionTime() {
        return executionTime;
    }

    /**
     * Obtiene las respuestas analizadas.
     *
     * @return Lista de respuestas
     */
    public List<Response> getResponses() {
        return responses;
    }

    /**
     * Obtiene las preguntas de la encuesta.
     *
     * @return Lista de preguntas
     */
    public List<Question> getQuestions() {
        return questions;
    }

    /**
     * Calcula una métrica de calidad del clustering.
     *
     * @param metricType Tipo de métrica a calcular
     * @return Valor de la métrica
     */
    public Double calculateQuality(QualityMetricType metricType) {
        if (this.distance == null) {
            throw new IllegalStateException("Analysis has not been executed yet.");
        }
        QualityMetricCalculator calculator = new QualityMetricCalculator();
        switch (metricType) {
            case SILHOUETTE:
                return calculator.calculateSilhouette(this, this.distance);
            case CALINSKI_HARABASZ:
                return calculator.calculateCalinskiHarabasz(this, this.distance);
            case DAVIES_BOULDIN:
                return calculator.calculateDaviesBouldin(this, this.distance);
            default:
                return 0.0; // Should not happen
        }
    }

    /**
     * Calculates a quality metric for a specific cluster.
     *
     * @param cluster    The cluster to evaluate.
     * @param metricType The type of metric to calculate.
     * @return The value of the metric for the cluster.
     */
    public Double getClusterQuality(Cluster cluster, QualityMetricType metricType) {
        if (this.distance == null) {
            throw new IllegalStateException("Analysis has not been executed yet.");
        }
        QualityMetricCalculator calculator = new QualityMetricCalculator();
        switch (metricType) {
            case SILHOUETTE:
                return calculator.calculateSilhouetteForCluster(cluster, this, this.distance);
            default:
                // Other metrics might not be applicable on a per-cluster basis
                return 0.0;
        }
    }

    /**
     * Exporta los resultados a un archivo de texto.
     *
     * @return Un String con el contenido del reporte.
     */
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
        sb.append("\n");

        sb.append("Execution Stats:\n");
        sb.append("- Converged: ").append(converged ? "Yes" : "No").append("\n");
        sb.append("- Iterations: ").append(iterations).append("\n");
        sb.append("- Execution Time: ").append(executionTime).append(" ms\n\n");

        sb.append("Clusters (").append(clusters.size()).append("):\n");
        sb.append("-----------------------------\n");
        for (Cluster cluster : clusters) {
            sb.append("\nCluster ").append(cluster.getId()).append(" (Size: ").append(cluster.getSize()).append(")\n");
            sb.append("----------\n");
            // Calculate and display Silhouette score for the cluster
            try {
                double silhouette = getClusterQuality(cluster, QualityMetricType.SILHOUETTE);
                sb.append(String.format("Silhouette Score: %.4f\n", silhouette));
            } catch (Exception e) {
                sb.append("Silhouette Score: Not available\n");
            }
            sb.append("Centroid: ").append(cluster.getCentroid().toString()).append("\n");
            sb.append("Member Response IDs:\n");
            for (ClusterMembership member : cluster.getMembers()) {
                sb.append("- ").append(member.getResponseId()).append("\n");
            }
        }
        return sb.toString();
    }
}