package edu.upc.prop.clusterxx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Encapsula los resultados de un análisis de clustering.
 * 
 * Almacena las asignaciones de cluster, distancias, centroides,
 * y proporciona métodos para consultar y manipular los resultados.
 */
public class ClusterResults {
    
    private Integer[] clusterAssignments;
    private Double[] distances;
    private Object[][] centroidMatrix;
    private Object[][] dataMatrix;
    private Map<Integer, String> clusterLabels;
    private int k;
    private int numResponses;
    private int numFeatures;
    
    /**
     * Constructor.
     * 
     * @param k El número de clusters
     * @param numResponses El número de respuestas/puntos
     * @param numFeatures El número de características/dimensiones
     */
    public ClusterResults(int k, int numResponses, int numFeatures) {
        this.k = k;
        this.numResponses = numResponses;
        this.numFeatures = numFeatures;
        
        this.clusterAssignments = new Integer[numResponses];
        this.distances = new Double[numResponses];
        this.centroidMatrix = new Object[k][numFeatures];
        this.dataMatrix = new Object[numResponses][numFeatures];
        this.clusterLabels = new HashMap<>();
        
        // Inicializar etiquetas por defecto
        for (int i = 0; i < k; i++) {
            clusterLabels.put(i, "Cluster " + (i + 1));
        }
    }
    
    /**
     * Establece la asignación de cluster para una respuesta.
     * 
     * @param responseIndex El índice de la respuesta
     * @param clusterIndex El índice del cluster asignado
     */
    public void setClusterAssignment(int responseIndex, int clusterIndex) {
        if (responseIndex < 0 || responseIndex >= numResponses) {
            throw new IndexOutOfBoundsException("Índice de respuesta fuera de rango");
        }
        if (clusterIndex < 0 || clusterIndex >= k) {
            throw new IndexOutOfBoundsException("Índice de cluster fuera de rango");
        }
        this.clusterAssignments[responseIndex] = clusterIndex;
    }
    
    /**
     * Obtiene el cluster asignado a una respuesta.
     * 
     * @param responseIndex El índice de la respuesta
     * @return El índice del cluster asignado
     */
    public Integer getClusterForResponse(int responseIndex) {
        if (responseIndex < 0 || responseIndex >= numResponses) {
            throw new IndexOutOfBoundsException("Índice de respuesta fuera de rango");
        }
        return this.clusterAssignments[responseIndex];
    }
    
    /**
     * Establece la distancia de una respuesta a su centroide.
     * 
     * @param responseIndex El índice de la respuesta
     * @param distance La distancia al centroide
     */
    public void setDistance(int responseIndex, double distance) {
        if (responseIndex < 0 || responseIndex >= numResponses) {
            throw new IndexOutOfBoundsException("Índice de respuesta fuera de rango");
        }
        this.distances[responseIndex] = distance;
    }
    
    /**
     * Obtiene la distancia de una respuesta a su centroide.
     * 
     * @param responseIndex El índice de la respuesta
     * @return La distancia al centroide
     */
    public Double getDistanceForResponse(int responseIndex) {
        if (responseIndex < 0 || responseIndex >= numResponses) {
            throw new IndexOutOfBoundsException("Índice de respuesta fuera de rango");
        }
        return this.distances[responseIndex];
    }
    
    /**
     * Establece el centroide de un cluster.
     * 
     * @param clusterIndex El índice del cluster
     * @param centroid El vector centroide
     */
    public void setCentroid(int clusterIndex, Object[] centroid) {
        if (clusterIndex < 0 || clusterIndex >= k) {
            throw new IndexOutOfBoundsException("Índice de cluster fuera de rango");
        }
        if (centroid.length != numFeatures) {
            throw new IllegalArgumentException("El centroide debe tener " + numFeatures + " características");
        }
        this.centroidMatrix[clusterIndex] = centroid.clone();
    }
    
    /**
     * Obtiene el centroide de un cluster.
     * 
     * @param clusterIndex El índice del cluster
     * @return El vector centroide
     */
    public Object[] getCentroid(int clusterIndex) {
        if (clusterIndex < 0 || clusterIndex >= k) {
            throw new IndexOutOfBoundsException("Índice de cluster fuera de rango");
        }
        return this.centroidMatrix[clusterIndex].clone();
    }
    
    /**
     * Establece los datos de una respuesta.
     * 
     * @param responseIndex El índice de la respuesta
     * @param data El vector de datos
     */
    public void setResponseData(int responseIndex, Object[] data) {
        if (responseIndex < 0 || responseIndex >= numResponses) {
            throw new IndexOutOfBoundsException("Índice de respuesta fuera de rango");
        }
        if (data.length != numFeatures) {
            throw new IllegalArgumentException("Los datos deben tener " + numFeatures + " características");
        }
        this.dataMatrix[responseIndex] = data.clone();
    }
    
    /**
     * Obtiene los datos de una respuesta.
     * 
     * @param responseIndex El índice de la respuesta
     * @return El vector de datos
     */
    public Object[] getResponseData(int responseIndex) {
        if (responseIndex < 0 || responseIndex >= numResponses) {
            throw new IndexOutOfBoundsException("Índice de respuesta fuera de rango");
        }
        return this.dataMatrix[responseIndex].clone();
    }
    
    /**
     * Obtiene los índices de todas las respuestas en un cluster.
     * 
     * @param clusterIndex El índice del cluster
     * @return Lista de índices de respuestas en el cluster
     */
    public List<Integer> getResponsesInCluster(int clusterIndex) {
        if (clusterIndex < 0 || clusterIndex >= k) {
            throw new IndexOutOfBoundsException("Índice de cluster fuera de rango");
        }
        
        List<Integer> responses = new ArrayList<>();
        for (int i = 0; i < numResponses; i++) {
            if (clusterAssignments[i] != null && clusterAssignments[i] == clusterIndex) {
                responses.add(i);
            }
        }
        return responses;
    }
    
    /**
     * Establece la etiqueta de un cluster.
     * 
     * @param clusterIndex El índice del cluster
     * @param label La etiqueta descriptiva
     */
    public void setClusterLabel(int clusterIndex, String label) {
        if (clusterIndex < 0 || clusterIndex >= k) {
            throw new IndexOutOfBoundsException("Índice de cluster fuera de rango");
        }
        this.clusterLabels.put(clusterIndex, label);
    }
    
    /**
     * Obtiene la etiqueta de un cluster.
     * 
     * @param clusterIndex El índice del cluster
     * @return La etiqueta del cluster
     */
    public String getClusterLabel(int clusterIndex) {
        if (clusterIndex < 0 || clusterIndex >= k) {
            throw new IndexOutOfBoundsException("Índice de cluster fuera de rango");
        }
        return this.clusterLabels.getOrDefault(clusterIndex, "Cluster " + (clusterIndex + 1));
    }
    
    /**
     * Obtiene el número de clusters.
     * 
     * @return El número de clusters
     */
    public int getNumberOfClusters() {
        return this.k;
    }
    
    /**
     * Obtiene el número de respuestas.
     * 
     * @return El número de respuestas
     */
    public int getNumberOfResponses() {
        return this.numResponses;
    }
    
    /**
     * Obtiene el número de características.
     * 
     * @return El número de características
     */
    public int getNumberOfFeatures() {
        return this.numFeatures;
    }
    
    /**
     * Convierte los resultados a una matriz para análisis posterior.
     * 
     * @return Matriz con los datos completos
     */
    public Object[][] toMatrix() {
        return this.dataMatrix.clone();
    }
    
    /**
     * Obtiene la matriz de centroides.
     * 
     * @return Matriz de centroides
     */
    public Object[][] getCentroidMatrix() {
        return this.centroidMatrix.clone();
    }
    
    /**
     * Obtiene el array de asignaciones de cluster.
     * 
     * @return Array de asignaciones
     */
    public Integer[] getClusterAssignments() {
        return this.clusterAssignments.clone();
    }
    
    /**
     * Obtiene el array de distancias.
     * 
     * @return Array de distancias
     */
    public Double[] getDistances() {
        return this.distances.clone();
    }
}
