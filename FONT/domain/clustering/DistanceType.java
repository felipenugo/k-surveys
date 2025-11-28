package domain.clustering;

/** Tipos de distancia: EUCLIDEAN, MANHATTAN, COSINE. */
public enum DistanceType {
    /**
     * Distancia Euclídea: sqrt(sum( (d_i)^2 ))
     */
    EUCLIDEAN,

    /**
     * Distancia de Manhattan: sum( |d_i| )
     */
    MANHATTAN,

    /**
     * Distancia del Coseno: 1 - (A·B / (||A|| * ||B||))
     */
    COSINE
}