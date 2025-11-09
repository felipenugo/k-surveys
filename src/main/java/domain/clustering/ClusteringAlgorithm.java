package domain.clustering;

/**
 * Interfaz para algoritmos de clustering (patrón Strategy).
 * Agrupa datos en k clusters, calcula centroides y asigna puntos.
 */
public interface ClusteringAlgorithm {
    
    /**
     * Ejecuta clustering sobre dataMatrix[numPuntos][numFeatures].
     * @throws IllegalArgumentException Si parámetros inválidos
     */
    ClusterResults execute(Object[][] dataMatrix, int k, DistanceCalculator distance);
    
    /** Nombre del algoritmo (ej: "K-Means"). */
    String getName();
    
    /** Descripción del algoritmo, ventajas y limitaciones. */
    String getDescription();
}
