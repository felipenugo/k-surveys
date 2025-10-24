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
     * @param dataMatrix La matriz de datos donde cada fila es un punto/observación
     *                   y cada columna es una característica/dimensión
     * @param k El número de clusters deseado
     * @param distance El calculador de distancias a utilizar
     * @return Los resultados del clustering incluyendo asignaciones y centroides
     * @throws IllegalArgumentException si los parámetros son inválidos
     */
    ClusterResults execute(Object[][] dataMatrix, int k, DistanceCalculator distance);
    
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
