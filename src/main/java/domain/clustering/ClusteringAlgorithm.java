package domain.clustering;

/**
 * Interfaz para algoritmos de clustering (patrón Strategy).
 * Agrupa datos en k clusters, calcula centroides y asigna puntos.
 */
public interface ClusteringAlgorithm {
    
    /**
     * Ejecuta el algoritmo de clustering sobre una matriz de datos.
     * 
     */
    public List<Cluster> execute(List<ResponseSet> responseSets, List<Question> questions, int k, DistanceCalculator distance)
    
    /** Nombre del algoritmo (ej: "K-Means"). */
    String getName();
    
    /** Descripción del algoritmo, ventajas y limitaciones. */
    String getDescription();
}
