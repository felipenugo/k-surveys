package domain.clustering;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * K-Medoids: usa puntos reales del dataset como centros (medoides).
 * Más robusto a outliers que K-Means, funciona con distancias no euclidianas.
 * Más lento (O(n²)) pero mejor para datos categóricos o con ruido.
 */
public class KMedoids implements ClusteringAlgorithm {
    
    // ========== ATRIBUTOS ==========
    
    private int maxIterations;
    private double tolerance;
    private long randomSeed;
    private Random random;
    
    // ========== CONSTRUCTORES ==========
    
    /** Constructor con parámetros personalizados. */
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
    
    /** Constructor con valores por defecto: maxIterations=100, tolerance=1e-4. */
    public KMedoids() {
        this(100, 1e-4);
    }
    
    // ========== MÉTODOS PÚBLICOS ==========
    
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
        return "Algoritmo K-Medoids que utiliza puntos reales del dataset como " +
               "representantes de clusters. Más robusto a outliers que K-Means " +
               "y funciona mejor con distancias no euclidianas y datos categóricos.";
    }
    
    @Override
    public ClusterResults execute(Object[][] dataMatrix, int k, DistanceCalculator distance) {
        // Validaciones
        if (dataMatrix == null || dataMatrix.length == 0) {
            throw new IllegalArgumentException("La matriz de datos no puede ser nula o vacía");
        }
        if (k <= 0 || k > dataMatrix.length) {
            throw new IllegalArgumentException("k debe estar entre 1 y el número de puntos");
        }
        if (distance == null) {
            throw new IllegalArgumentException("DistanceCalculator no puede ser null");
        }
        
        int numPoints = dataMatrix.length;
        int numFeatures = dataMatrix[0].length;
        
        // Inicializar resultados
        ClusterResults results = new ClusterResults(k, numPoints, numFeatures);
        
        // Copiar datos
        for (int i = 0; i < numPoints; i++) {
            results.setResponseData(i, dataMatrix[i]);
        }
        
        // 1. Inicializar medoides (índices de puntos reales)
        int[] medoidIndices = initializeMedoids(dataMatrix, k);
        
        // Variables para el bucle iterativo
        Integer[] assignments = new Integer[numPoints];
        boolean converged = false;
        int iteration = 0;
        double previousCost = Double.MAX_VALUE;
        
        // 2. Bucle principal
        while (iteration < maxIterations && !converged) {
            // 2.1 Asignar cada punto al medoide más cercano
            assignments = assignToClusters(dataMatrix, medoidIndices, distance);
            
            // 2.2 Actualizar medoides (seleccionar mejor punto de cada cluster)
            int[] newMedoidIndices = updateMedoids(dataMatrix, assignments, k, distance);
            
            // 2.3 Calcular costo total y verificar convergencia
            double currentCost = calculateTotalCost(dataMatrix, assignments, newMedoidIndices, distance);
            converged = Math.abs(previousCost - currentCost) < tolerance;
            
            previousCost = currentCost;
            medoidIndices = newMedoidIndices;
            iteration++;
        }
        
        // 3. Guardar resultados finales
        for (int i = 0; i < numPoints; i++) {
            results.setClusterAssignment(i, assignments[i]);
            double dist = distance.calculateVectorDistance(dataMatrix[i], dataMatrix[medoidIndices[assignments[i]]]);
            results.setDistance(i, dist);
        }
        
        // Los "centroides" son los medoides (puntos reales)
        for (int i = 0; i < k; i++) {
            results.setCentroid(i, dataMatrix[medoidIndices[i]]);
        }
        
        results.setIterations(iteration);
        results.setConverged(converged);
        
        return results;
    }
    
    // ========== MÉTODOS PRIVADOS ==========
    
    /** Inicializa k medoides seleccionando puntos aleatorios. */
    private int[] initializeMedoids(Object[][] dataMatrix, int k) {
        int numPoints = dataMatrix.length;
        int[] medoidIndices = new int[k];
        boolean[] selected = new boolean[numPoints];
        
        for (int i = 0; i < k; i++) {
            int randomIndex;
            do {
                randomIndex = random.nextInt(numPoints);
            } while (selected[randomIndex]);
            
            selected[randomIndex] = true;
            medoidIndices[i] = randomIndex;
        }
        
        return medoidIndices;
    }
    
    /** Asigna cada punto al medoide más cercano. */
    private Integer[] assignToClusters(Object[][] dataMatrix, int[] medoidIndices, 
                                       DistanceCalculator distance) {
        int numPoints = dataMatrix.length;
        int k = medoidIndices.length;
        Integer[] assignments = new Integer[numPoints];
        
        for (int i = 0; i < numPoints; i++) {
            double minDistance = Double.MAX_VALUE;
            int closestCluster = 0;
            
            for (int j = 0; j < k; j++) {
                double dist = distance.calculateVectorDistance(dataMatrix[i], dataMatrix[medoidIndices[j]]);
                if (dist < minDistance) {
                    minDistance = dist;
                    closestCluster = j;
                }
            }
            
            assignments[i] = closestCluster;
        }
        
        return assignments;
    }
    
    /** Actualiza medoides: para cada cluster, elige punto que minimiza suma de distancias. */
    private int[] updateMedoids(Object[][] dataMatrix, Integer[] assignments, int k, 
                                DistanceCalculator distance) {
        int numPoints = dataMatrix.length;
        int[] newMedoidIndices = new int[k];
        
        // Para cada cluster
        for (int cluster = 0; cluster < k; cluster++) {
            // Obtener índices de puntos en este cluster
            List<Integer> clusterPoints = new ArrayList<>();
            for (int i = 0; i < numPoints; i++) {
                if (assignments[i] == cluster) {
                    clusterPoints.add(i);
                }
            }
            
            // Si el cluster está vacío, mantener un medoide aleatorio
            if (clusterPoints.isEmpty()) {
                newMedoidIndices[cluster] = random.nextInt(numPoints);
                continue;
            }
            
            // Encontrar el punto que minimiza la suma de distancias
            int bestMedoid = clusterPoints.get(0);
            double minTotalDistance = Double.MAX_VALUE;
            
            for (int candidateIdx : clusterPoints) {
                double totalDistance = 0.0;
                
                // Sumar distancias a todos los puntos del cluster
                for (int pointIdx : clusterPoints) {
                    totalDistance += distance.calculateVectorDistance(
                        dataMatrix[candidateIdx], 
                        dataMatrix[pointIdx]
                    );
                }
                
                if (totalDistance < minTotalDistance) {
                    minTotalDistance = totalDistance;
                    bestMedoid = candidateIdx;
                }
            }
            
            newMedoidIndices[cluster] = bestMedoid;
        }
        
        return newMedoidIndices;
    }
    
    /** Calcula costo total: suma de distancias de cada punto a su medoide. */
    private double calculateTotalCost(Object[][] dataMatrix, Integer[] assignments, 
                                     int[] medoidIndices, DistanceCalculator distance) {
        double totalCost = 0.0;
        
        for (int i = 0; i < dataMatrix.length; i++) {
            int medoidIdx = medoidIndices[assignments[i]];
            totalCost += distance.calculateVectorDistance(dataMatrix[i], dataMatrix[medoidIdx]);
        }
        
        return totalCost;
    }
}
