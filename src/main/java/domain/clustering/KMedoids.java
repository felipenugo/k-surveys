package domain.clustering;

import domain.model.Question;
import domain.model.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class KMedoids implements ClusteringAlgorithm {

    private int maxIterations;
    private double tolerance;
    private long randomSeed;
    private Random random;

    public KMedoids(int maxIterations, double tolerance) {
        if (maxIterations <= 0) {
            throw new IllegalArgumentException("maxIterations debe ser mayor que 0");
        }
        if (tolerance < 0) {
            throw new IllegalArgumentException("tolerance no puede ser negativa");
        }
        this.maxIterations = maxIterations;
        this.tolerance = tolerance;
        this.randomSeed = System.currentTimeMillis();
        this.random = new Random(randomSeed);
    }

    public KMedoids() {
        this(100, 1e-4);
    }

    public void setRandomSeed(long seed) {
        this.randomSeed = seed;
        this.random = new Random(seed);
    }

    @Override
    public String getName() {
        return "K-Medoids";
    }

    @Override
    public String getDescription() {
        return "Algoritmo K-Medoids que utiliza puntos reales del dataset como representantes de clusters.";
    }

    @Override
    public ClusterResults execute(List<Response> responses, List<Question> questions, int k,
                                 DistanceCalculator distance) {
        if (responses == null || responses.isEmpty()) {
            throw new IllegalArgumentException("Los datos (responses) no pueden ser nulos o vacíos");
        }
        if (k <= 0 || k > responses.size()) {
            throw new IllegalArgumentException("k debe estar entre 1 y el número de puntos");
        }

        // 1. Initialize medoids (indices of actual responses)
        int[] medoidIndices = initializeMedoids(responses.size(), k);

        Integer[] assignments = new Integer[responses.size()];
        boolean converged = false;
        int iteration = 0;
        double previousCost = Double.MAX_VALUE;

        // 2. Main loop
        while (iteration < maxIterations && !converged) {
            // 2.1 Assign each point to the nearest medoid
            assignments = assignToClusters(responses, questions, medoidIndices, distance);

            // 2.2 Update medoids
            int[] newMedoidIndices = updateMedoids(responses, questions, assignments, k, distance);

            // 2.3 Check for convergence
            double currentCost = calculateTotalCost(responses, questions, assignments, newMedoidIndices, distance);
            if (Math.abs(previousCost - currentCost) < tolerance) {
                converged = true;
            }
            
            previousCost = currentCost;
            medoidIndices = newMedoidIndices;
            iteration++;
        }

        // 3. Create final clusters
        List<Cluster> finalClusters = createClusters(responses, questions, assignments, medoidIndices, distance);
        return new ClusterResults(finalClusters, iteration, converged);
    }

    private int[] initializeMedoids(int numPoints, int k) {
        int[] medoidIndices = new int[k];
        Set<Integer> chosenIndices = new HashSet<>();
        for (int i = 0; i < k; i++) {
            int randomIndex;
            do {
                randomIndex = random.nextInt(numPoints);
            } while (chosenIndices.contains(randomIndex));
            chosenIndices.add(randomIndex);
            medoidIndices[i] = randomIndex;
        }
        return medoidIndices;
    }

    private Integer[] assignToClusters(List<Response> responses, List<Question> questions,
                                       int[] medoidIndices, DistanceCalculator distance) {
        Integer[] assignments = new Integer[responses.size()];
        for (int i = 0; i < responses.size(); i++) {
            double minDistance = Double.MAX_VALUE;
            int bestCluster = -1;
            for (int j = 0; j < medoidIndices.length; j++) {
                Response medoid = responses.get(medoidIndices[j]);
                double d = distance.calculate(responses.get(i), medoid, questions);
                if (d < minDistance) {
                    minDistance = d;
                    bestCluster = j;
                }
            }
            assignments[i] = bestCluster;
        }
        return assignments;
    }

    private int[] updateMedoids(List<Response> responses, List<Question> questions,
                                Integer[] assignments, int k, DistanceCalculator distance) {
        int[] newMedoidIndices = new int[k];
        for (int i = 0; i < k; i++) {
            List<Integer> clusterPointIndices = new ArrayList<>();
            for (int j = 0; j < assignments.length; j++) {
                if (assignments[j] != null && assignments[j] == i) {
                    clusterPointIndices.add(j);
                }
            }

            if (clusterPointIndices.isEmpty()) {
                // Re-initialize medoid for empty cluster
                newMedoidIndices[i] = random.nextInt(responses.size());
                continue;
            }

            double minClusterCost = Double.MAX_VALUE;
            int bestMedoidIndex = -1;

            // Find the point that minimizes the sum of distances within the cluster
            for (int candidateIndex : clusterPointIndices) {
                double currentCost = 0.0;
                for (int pointIndex : clusterPointIndices) {
                    currentCost += distance.calculate(responses.get(candidateIndex), responses.get(pointIndex), questions);
                }
                if (currentCost < minClusterCost) {
                    minClusterCost = currentCost;
                    bestMedoidIndex = candidateIndex;
                }
            }
            newMedoidIndices[i] = bestMedoidIndex;
        }
        return newMedoidIndices;
    }

    private double calculateTotalCost(List<Response> responses, List<Question> questions,
                                      Integer[] assignments, int[] medoidIndices, DistanceCalculator distance) {
        double totalCost = 0.0;
        for (int i = 0; i < responses.size(); i++) {
            if (assignments[i] != null) {
                int medoidIndex = medoidIndices[assignments[i]];
                totalCost += distance.calculate(responses.get(i), responses.get(medoidIndex), questions);
            }
        }
        return totalCost;
    }

    private List<Cluster> createClusters(List<Response> responses, List<Question> questions,
                                         Integer[] assignments, int[] medoidIndices,
                                         DistanceCalculator distance) {
        int k = medoidIndices.length;
        List<Cluster> finalClusters = new ArrayList<>();
        Map<Integer, Cluster> clusterMap = new HashMap<>();

        // Create Centroid objects from the final medoids
        List<Centroid> centroids = new ArrayList<>();
        for (int medoidIndex : medoidIndices) {
            centroids.add(KMeansPlusPlus.createCentroidFromResponse(responses.get(medoidIndex), questions));
        }

        for (int i = 0; i < k; i++) {
            Cluster cluster = new Cluster("cluster_" + (i + 1));
            cluster.setCentroid(centroids.get(i));
            finalClusters.add(cluster);
            clusterMap.put(i, cluster);
        }

        for (int i = 0; i < responses.size(); i++) {
            Integer clusterIndex = assignments[i];
            if (clusterIndex == null || clusterIndex == -1) continue;
            Response r = responses.get(i);
            Cluster cluster = clusterMap.get(clusterIndex);
            if (cluster != null) {
                double distToCentroid = distance.calculateToCentroid(r, cluster.getCentroid(), questions);
                cluster.addMember(r, distToCentroid);
            }
        }
        return finalClusters;
    }
}