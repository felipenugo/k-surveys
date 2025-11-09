package domain.model;

import domain.clustering.ClusteringAlgorithm;
import domain.clustering.ClusterResults;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Representa un análisis de clustering sobre un conjunto de preguntas.
 */
public class ClusteringAnalysis {
    
    private String id;
    private String questionSetId;
    private Integer k;
    private String algorithmType;
    private Date executionDate;
    private Boolean converged;
    private Integer iterations;
    private Long executionTime;
    private ClusterResults results;
    private ClusteringAlgorithm algorithm;
    
    /**
     * Constructor.
     */
    public ClusteringAnalysis(String questionSetId, Integer k, ClusteringAlgorithm algorithm) {
        this.id = generateId();
        this.questionSetId = questionSetId;
        this.k = k;
        this.algorithm = algorithm;
        this.algorithmType = algorithm.getName();
        this.executionDate = null;
        this.converged = false;
        this.iterations = 0;
        this.executionTime = 0L;
        this.results = null;
    }
    
    public String getId() {
        return id;
    }
    
    public Integer getK() {
        return k;
    }
    
    /**
     * Ejecuta el clustering sobre la matriz de datos.
     */
    public void execute(Object[][] dataMatrix) {
        long startTime = System.currentTimeMillis();
        
        // Ejecutar algoritmo (falta DistanceCalculator, se usará null temporalmente)
        this.results = algorithm.execute(dataMatrix, k, null);
        
        // Actualizar metadatos
        this.executionDate = new Date();
        this.converged = results.hasConverged();
        this.iterations = results.getIterations();
        this.executionTime = System.currentTimeMillis() - startTime;
    }
    
    public ClusterResults getResults() {
        return results;
    }
    
    public List<Cluster> getClusters() {
        if (results == null) {
            return new ArrayList<>();
        }
        return results.getClusters();
    }
    
    public Cluster getCluster(String clusterId) {
        if (results == null) {
            throw new IllegalStateException("Analysis not yet executed");
        }
        try {
            int clusterIndex = Integer.parseInt(clusterId);
            return results.getCluster(clusterIndex);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid cluster ID: " + clusterId);
        }
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
    
    /**
     * Calcula la calidad del clustering.
     * TODO: Implementar cuando exista QualityMetricType.
     */
    public Double calculateQuality(String metricType) {
        throw new UnsupportedOperationException("Quality metrics not yet implemented");
    }
    
    /**
     * Exporta resultados a archivo.
     */
    public void exportResults(String filePath) {
        throw new UnsupportedOperationException("Export functionality not yet implemented");
    }
    
    private String generateId() {
        return "ANALYSIS_" + System.currentTimeMillis();
    }
}
