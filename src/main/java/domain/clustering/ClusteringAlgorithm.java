package edu.upc.prop.clusterxx;

/**
 * Interfaz que define el contrato para los algoritmos de clustering.
 * 
 * Implementa el patrón Strategy permitiendo intercambiar diferentes
 * algoritmos de clustering (K-Means, K-Means++, K-Medoids, etc.)
 */
public interface ClusteringAlgorithm {
    
    /**
     * Ejecuta el algoritmo de clustering sobre una matriz de datos.
     * 
     */
    public List<Cluster> execute(List<ResponseSet> responseSets, List<Question> questions, int k, DistanceCalculator distance)
    
    /**
     * Obtiene el nombre del algoritmo.
     * 
     * @return El nombre descriptivo del algoritmo (ej. "K-Means", "K-Medoids")
     */
    String getName();
    
    /**
     * Obtiene una descripción del algoritmo.
     * 
     * @return Una descripción breve del algoritmo y sus características
     */
    String getDescription();
}
