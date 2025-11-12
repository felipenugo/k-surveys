package domain.clustering;

import domain.model.Survey;
import domain.model.Response; // This is the ResponseSet in the diagram
import domain.model.Question; // This is the Question in the diagram
import domain.clustering.QualityMetricType;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Representa un análisis de clustering completo sobre una encuesta.
 * Coordina la ejecución del algoritmo y almacena los resultados.
 */
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
    private DistanceCalculator distance; // Added field

    /**
     * Constructor que inicializa un análisis de clustering.
     *
     * @param survey Encuesta sobre la que realizar el clustering
     * @param k Número de clusters a generar
     * @param algorithm Algoritmo de clustering a utilizar
     */
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
     * Ejecuta el análisis de clustering con los datos proporcionados.
     *
     * @param responses Lista de respuestas a agrupar
     * @param questions Lista de preguntas de la encuesta
     * @param distance Calculadora de distancia a utilizar
     */
    public void execute(List<Response> responses, List<Question> questions, DistanceCalculator distance) {
        long startTime = System.currentTimeMillis();
        this.responses = new ArrayList<>(responses); // Store responses
        this.questions = new ArrayList<>(questions); // Store questions
        this.distance = distance; // Store distance calculator
        // The algorithm.execute method needs to be adapted to take List<Response> and List<Question>
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
    public List<Response> getResponses() { // Added getter
        return responses;
    }

    /**
     * Obtiene las preguntas de la encuesta.
     *
     * @return Lista de preguntas
     */
    public List<Question> getQuestions() { // Added getter
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
     * Exporta los resultados a un archivo.
     *
     * @param filePath Ruta del archivo de destino
     */
    public void exportResults(String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write("Clustering Analysis Results\n");
            writer.write("=============================\n\n");
            writer.write("Configuration:\n");
            writer.write("- Analysis ID: " + id + "\n");
            writer.write("- Survey ID: " + surveyId + "\n");
            writer.write("- Algorithm: " + algorithmType + "\n");
            writer.write("- K: " + k + "\n");
            if (distance != null) {
                writer.write("- Distance Type: " + distance.getDistanceType() + "\n");
            }
            writer.write("\n");

            writer.write("Execution Stats:\n");
            writer.write("- Converged: " + (converged ? "Yes" : "No") + "\n");
            writer.write("- Iterations: " + iterations + "\n");
            writer.write("- Execution Time: " + executionTime + " ms\n\n");

            writer.write("Clusters (" + clusters.size() + "):\n");
            writer.write("-----------------------------\n");
            for (Cluster cluster : clusters) {
                writer.write("\nCluster " + cluster.getId() + " (Size: " + cluster.getSize() + ")\n");
                writer.write("----------\n");
                writer.write("Centroid: " + cluster.getCentroid().toString() + "\n");
                writer.write("Member Response IDs:\n");
                for (ClusterMembership member : cluster.getMembers()) {
                    writer.write("- " + member.getResponseId() + "\n");
                }
            }
        } catch (IOException e) {
            // In a real application, you might want to throw a custom exception
            e.printStackTrace();
        }
    }
}
