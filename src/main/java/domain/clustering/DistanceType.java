package domain.clustering;

/** Tipos de distancia: EUCLIDEAN, MANHATTAN, COSINE. */
public enum DistanceType {
    EUCLIDEAN,   // sqrt(sum(d_i²))
    MANHATTAN,   // sum(|d_i|)
    COSINE       // Ángulo entre vectores
}