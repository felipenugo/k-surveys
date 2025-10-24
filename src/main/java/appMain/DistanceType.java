package edu.upc.prop.clusterxx;

/**
 * Enumera los tipos de cálculo de distancia global que se pueden utilizar.
 */
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
     * Distancia del Coseno: Mide el ángulo entre dos vectores.
     */
    COSINE
}