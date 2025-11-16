package domain.clustering;

/**
 * Tipos de métricas de calidad para evaluar resultados de clustering.
 */
public enum QualityMetricType {
    /** Coeficiente de Silhouette: mide cohesión y separación (-1 a 1, mayor es mejor) */
    SILHOUETTE,
    
    /** Índice de Calinski-Harabasz: ratio de dispersión entre/dentro clusters (mayor es mejor) */
    CALINSKI_HARABASZ,
    
    /** Índice de Davies-Bouldin: ratio de similitud intra/inter clusters (menor es mejor) */
    DAVIES_BOULDIN
}
