package domain.clustering;

import domain.model.Cluster;
import domain.model.Centroid;
import java.util.ArrayList;
import java.util.List;

/**
 * Almacena resultados de clustering con objetos Cluster.
 * Mantiene compatibilidad con arrays para algoritmos existentes.
 */
public class ClusterResults {
    
    // ========== ATRIBUTOS ==========
    
    private final int numClusters;
    private final int numResponses;
    private final int numFeatures;
    
    // Nueva estructura según diagrama
    private List<Cluster> clusters;
    
    // Estructuras legacy para compatibilidad
    private Integer[] clusterAssignments; // clusterAssignments[i] = cluster del punto i
    private Double[] distances; // distances[i] = distancia del punto i a su centroide
    private Object[][] centroidMatrix; // [numClusters][numFeatures]
    private Object[][] dataMatrix; // [numResponses][numFeatures]
    
    private int iterations;
    private boolean converged;
    
    // ========== CONSTRUCTOR ==========
    
    /** Crea contenedor de resultados. */
    public ClusterResults(int k, int numResponses, int numFeatures) {
        if (k <= 0) {
            throw new IllegalArgumentException("k debe ser mayor que 0");
        }
        if (numResponses <= 0) {
            throw new IllegalArgumentException("numResponses debe ser mayor que 0");
        }
        if (numFeatures <= 0) {
            throw new IllegalArgumentException("numFeatures debe ser mayor que 0");
        }
        
        this.numClusters = k;
        this.numResponses = numResponses;
        this.numFeatures = numFeatures;
        
        // Inicializar clusters según diagrama
        this.clusters = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            this.clusters.add(new Cluster(String.valueOf(i)));
        }
        
        // Mantener estructuras legacy para compatibilidad
        this.clusterAssignments = new Integer[numResponses];
        this.distances = new Double[numResponses];
        this.centroidMatrix = new Object[k][numFeatures];
        this.dataMatrix = new Object[numResponses][numFeatures];
        
        this.iterations = 0;
        this.converged = false;
    }
    
    // ========== SETTERS ==========
    
    /** Establece asignación de cluster para un punto. */
    public void setClusterAssignment(int responseIndex, int clusterIndex) {
        validateResponseIndex(responseIndex);
        validateClusterIndex(clusterIndex);
        this.clusterAssignments[responseIndex] = clusterIndex;
        
        // Actualizar estructura de Cluster (usando responseIndex como ID temporal)
        // Primero eliminar de cualquier cluster anterior
        for (Cluster cluster : clusters) {
            cluster.removeMember(String.valueOf(responseIndex));
        }
        // Añadir al nuevo cluster
        double distance = (distances[responseIndex] != null) ? distances[responseIndex] : 0.0;
        clusters.get(clusterIndex).addMember(String.valueOf(responseIndex), distance);
    }
    
    /** Establece distancia de un punto a su centroide. */
    public void setDistance(int responseIndex, double distance) {
        validateResponseIndex(responseIndex);
        this.distances[responseIndex] = distance;
        
        // Actualizar distancia en el Cluster si ya está asignado
        if (clusterAssignments[responseIndex] != null) {
            int clusterIndex = clusterAssignments[responseIndex];
            // Actualizar membership (eliminar y re-añadir con nueva distancia)
            Cluster cluster = clusters.get(clusterIndex);
            cluster.removeMember(String.valueOf(responseIndex));
            cluster.addMember(String.valueOf(responseIndex), distance);
        }
    }
    
    /** Establece centroide de un cluster. */
    public void setCentroid(int clusterIndex, Object[] centroid) {
        validateClusterIndex(clusterIndex);
        if (centroid == null || centroid.length != numFeatures) {
            throw new IllegalArgumentException("Centroid debe tener " + numFeatures + " features");
        }
        this.centroidMatrix[clusterIndex] = centroid.clone();
        
        // Actualizar Centroid del Cluster
        List<String> questionIds = new ArrayList<>();
        for (int i = 0; i < numFeatures; i++) {
            questionIds.add("Q" + i);
        }
        domain.model.Centroid centroidObj = new domain.model.Centroid(questionIds);
        for (int i = 0; i < centroid.length; i++) {
            centroidObj.setComponent(i, centroid[i]);
        }
        clusters.get(clusterIndex).setCentroid(centroidObj);
    }
    
    /** Establece datos de un punto. */
    public void setResponseData(int responseIndex, Object[] data) {
        validateResponseIndex(responseIndex);
        if (data == null || data.length != numFeatures) {
            throw new IllegalArgumentException("Data debe tener " + numFeatures + " features");
        }
        this.dataMatrix[responseIndex] = data.clone();
    }
    
    /** Establece número de iteraciones ejecutadas. */
    public void setIterations(int iterations) {
        this.iterations = iterations;
    }
    
    /** Establece si el algoritmo convergió. */
    public void setConverged(boolean converged) {
        this.converged = converged;
    }
    
    // ========== GETTERS ==========
    
    /** Obtiene cluster asignado a un punto. */
    public int getClusterForResponse(int responseIndex) {
        validateResponseIndex(responseIndex);
        return clusterAssignments[responseIndex];
    }
    
    /** Obtiene distancia de un punto a su centroide. */
    public double getDistanceForResponse(int responseIndex) {
        validateResponseIndex(responseIndex);
        return distances[responseIndex];
    }
    
    /** Obtiene centroide de un cluster (copia). */
    public Object[] getCentroid(int clusterIndex) {
        validateClusterIndex(clusterIndex);
        return centroidMatrix[clusterIndex].clone();
    }
    
    /** Obtiene datos de un punto (copia). */
    public Object[] getResponseData(int responseIndex) {
        validateResponseIndex(responseIndex);
        return dataMatrix[responseIndex].clone();
    }
    
    /** Obtiene índices de puntos que pertenecen a un cluster. */
    public List<Integer> getResponsesInCluster(int clusterIndex) {
        validateClusterIndex(clusterIndex);
        List<Integer> members = new ArrayList<>();
        
        for (int i = 0; i < numResponses; i++) {
            if (clusterAssignments[i] != null && clusterAssignments[i] == clusterIndex) {
                members.add(i);
            }
        }
        
        return members;
    }
    
    public int getNumberOfClusters() { return numClusters; }
    public int getK() { return numClusters; }
    public int getNumberOfResponses() { return numResponses; }
    public int getNumberOfFeatures() { return numFeatures; }
    public int getIterations() { return iterations; }
    public boolean hasConverged() { return converged; }
    public boolean isConverged() { return converged; }
    public Integer[] getClusterAssignments() { return clusterAssignments; }
    
    // ========== MÉTODOS NUEVOS SEGÚN DIAGRAMA ==========
    
    /** Obtiene lista de clusters (según diagrama). */
    public List<Cluster> getClusters() {
        return new ArrayList<>(clusters);
    }
    
    /** Obtiene un cluster específico por índice. */
    public Cluster getCluster(int clusterIndex) {
        validateClusterIndex(clusterIndex);
        return clusters.get(clusterIndex);
    }
    
    /** Obtiene matriz completa de centroides (copia). */
    public Object[][] getCentroidMatrix() {
        Object[][] copy = new Object[numClusters][];
        for (int i = 0; i < numClusters; i++) {
            copy[i] = centroidMatrix[i].clone();
        }
        return copy;
    }
    
    /** Obtiene matriz completa de datos (copia). */
    public Object[][] getDataMatrix() {
        Object[][] copy = new Object[numResponses][];
        for (int i = 0; i < numResponses; i++) {
            copy[i] = dataMatrix[i].clone();
        }
        return copy;
    }
    
    // ========== MÉTODOS PRIVADOS ==========
    
    private void validateResponseIndex(int index) {
        if (index < 0 || index >= numResponses) {
            throw new IndexOutOfBoundsException("Response index fuera de rango: " + index);
        }
    }
    
    private void validateClusterIndex(int index) {
        if (index < 0 || index >= numClusters) {
            throw new IndexOutOfBoundsException("Cluster index fuera de rango: " + index);
        }
    }
    
    // ========== UTILIDADES ==========
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ClusterResults[");
        sb.append("k=").append(numClusters);
        sb.append(", points=").append(numResponses);
        sb.append(", features=").append(numFeatures);
        sb.append(", iterations=").append(iterations);
        sb.append(", converged=").append(converged);
        sb.append("]");
        return sb.toString();
    }
}
