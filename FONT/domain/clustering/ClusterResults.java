package domain.clustering;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsula los resultados de un análisis de clustering.
 * Almacena la lista de clusters y metadatos sobre la ejecución del algoritmo.
 */
public class ClusterResults {

    private final List<Cluster> clusters;
    private int iterations;
    private boolean converged;

    /**
     * Constructor.
     * @param clusters La lista de clusters resultado del algoritmo.
     * @param iterations El número de iteraciones que tomó el algoritmo.
     * @param converged Si el algoritmo convergió o no.
     */
    public ClusterResults(List<Cluster> clusters, int iterations, boolean converged) {
        this.clusters = clusters != null ? new ArrayList<>(clusters) : new ArrayList<>();
        this.iterations = iterations;
        this.converged = converged;
    }

    /**
     * Obtiene la lista de clusters.
     * @return Una lista de objetos Cluster.
     */
    public List<Cluster> getClusters() {
        return clusters;
    }

    /**
     * Obtiene un cluster específico por su índice en la lista.
     * @param clusterIndex El índice del cluster.
     * @return El objeto Cluster.
     */
    public Cluster getCluster(int clusterIndex) {
        if (clusterIndex < 0 || clusterIndex >= clusters.size()) {
            throw new IndexOutOfBoundsException("Índice de cluster fuera de rango: " + clusterIndex);
        }
        return clusters.get(clusterIndex);
    }
    
    /**
     * Obtiene el número de clusters.
     * @return El número de clusters.
     */
    public int getNumberOfClusters() {
        return clusters.size();
    }

    /**
     * Obtiene el número de clusters.
     * @return El número de clusters.
     */
    public int getK() {
        return clusters.size();
    }

    /**
     * Obtiene el número total de puntos (respuestas) en todos los clusters.
     * @return El número total de respuestas.
     */
    public int getNumberOfResponses() {
        return clusters.stream().mapToInt(Cluster::getSize).sum();
    }

    /**
     * Obtiene el número de iteraciones que realizó el algoritmo.
     * @return El número de iteraciones.
     */
    public int getIterations() {
        return iterations;
    }

    /**
     * Devuelve si el algoritmo alcanzó la convergencia.
     * @return true si convergió, false en caso contrario.
     */
    public boolean hasConverged() {
        return converged;
    }

    public Integer[] getClusterAssignments() {
        List<Integer> assignments = new ArrayList<>();
        for (int i = 0; i < clusters.size(); i++) {
            Cluster cluster = clusters.get(i);
            for (int j = 0; j < cluster.getSize(); j++) {
                assignments.add(i);
            }
        }
        return assignments.toArray(new Integer[0]);
    }
}