package domain.clustering;

import java.util.ArrayList;
import java.util.List;

public class ClusterResults {
    private final List<Cluster> clusters;
    private int iterations;
    private boolean converged;

    public ClusterResults(List<Cluster> clusters, int iterations, boolean converged) {
        this.clusters = clusters != null ? new ArrayList<>(clusters) : new ArrayList<>();
        this.iterations = iterations;
        this.converged = converged;
    }

    public List<Cluster> getClusters() { return clusters; }
    public Cluster getCluster(int clusterIndex) {
        if (clusterIndex < 0 || clusterIndex >= clusters.size()) throw new IndexOutOfBoundsException("Índice de cluster fuera de rango: " + clusterIndex);
        return clusters.get(clusterIndex);
    }
    public int getNumberOfClusters() { return clusters.size(); }
    public int getK() { return clusters.size(); }
    public int getNumberOfResponses() { return clusters.stream().mapToInt(Cluster::getSize).sum(); }
    public int getIterations() { return iterations; }
    public boolean hasConverged() { return converged; }
    public Integer[] getClusterAssignments() { List<Integer> assignments = new ArrayList<>(); for (int i = 0; i < clusters.size(); i++) { Cluster cluster = clusters.get(i); for (int j = 0; j < cluster.getSize(); j++) assignments.add(i); } return assignments.toArray(new Integer[0]); }
}
