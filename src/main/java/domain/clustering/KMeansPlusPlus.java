package domain.clustering;

import java.util.Random;

/**
 * K-Means++ con inicialización inteligente de centroides.
 * Elige centroides lejanos entre sí (probabilidad proporcional a distancia²).
 * Mejor calidad y convergencia más rápida que K-Means estándar.
 */
public class KMeansPlusPlus implements ClusteringAlgorithm {
    
    // ========== ATRIBUTOS ==========
    
    private int maxIterations;
    private double tolerance;
    private long randomSeed;
    private Random random;
    
    // ========== CONSTRUCTORES ==========
    
    /** Constructor con parámetros personalizados. */
    public KMeansPlusPlus(int maxIterations, double tolerance) {
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
    public KMeansPlusPlus() {
        this(100, 1e-4);
    }
    
    // ========== MÉTODOS PÚBLICOS ==========
    
    public void setRandomSeed(long seed) {
        this.randomSeed = seed;
        this.random = new Random(seed);
    }
    
    @Override
    public String getName() {
        return "K-Means++";
    }
    
    @Override
    public String getDescription() {
        return "Variante mejorada de K-Means con inicialización inteligente de centroides. " +
               "Selecciona centroides que están lejanos entre sí, mejorando la calidad " +
               "de los clusters y reduciendo la sensibilidad a la inicialización aleatoria.";
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
        
        // 1. Inicializar centroides con K-Means++
        Object[][] centroids = initializeCentroidsPlusPlus(dataMatrix, k, distance);
        
        // Variables para el bucle iterativo
        Integer[] assignments = new Integer[numPoints];
        boolean converged = false;
        int iteration = 0;
        
        // 2. Bucle principal (igual que K-Means estándar)
        while (iteration < maxIterations && !converged) {
            Integer[] newAssignments = assignToClusters(dataMatrix, centroids, distance);
            Object[][] newCentroids = updateCentroids(dataMatrix, newAssignments, k);
            converged = hasConverged(centroids, newCentroids);
            
            assignments = newAssignments;
            centroids = newCentroids;
            iteration++;
        }
        
        // 3. Guardar resultados
        for (int i = 0; i < numPoints; i++) {
            results.setClusterAssignment(i, assignments[i]);
            double dist = calculateDistance(dataMatrix[i], centroids[assignments[i]], distance);
            results.setDistance(i, dist);
        }
        
        for (int i = 0; i < k; i++) {
            results.setCentroid(i, centroids[i]);
        }
        
        results.setIterations(iteration);
        results.setConverged(converged);
        
        return results;
    }
    
    // ========== MÉTODOS PRIVADOS ==========
    
    /** Inicialización K-Means++: selecciona centroides lejanos entre sí. */
    private Object[][] initializeCentroidsPlusPlus(Object[][] dataMatrix, int k, DistanceCalculator distance) {
        int numPoints = dataMatrix.length;
        int numFeatures = dataMatrix[0].length;
        Object[][] centroids = new Object[k][numFeatures];
        
        // 1. Elegir primer centroide aleatoriamente
        int firstIndex = random.nextInt(numPoints);
        for (int j = 0; j < numFeatures; j++) {
            centroids[0][j] = dataMatrix[firstIndex][j];
        }
        
        // Array para almacenar distancias mínimas de cada punto
        double[] minDistances = new double[numPoints];
        
        // 2. Para cada centroide restante
        for (int i = 1; i < k; i++) {
            double totalDistance = 0.0;
            
            // Calcular distancia² de cada punto al centroide más cercano
            for (int p = 0; p < numPoints; p++) {
                double minDist = Double.MAX_VALUE;
                
                for (int c = 0; c < i; c++) {
                    double dist = calculateDistance(dataMatrix[p], centroids[c], distance);
                    if (dist < minDist) {
                        minDist = dist;
                    }
                }
                
                minDistances[p] = minDist * minDist; // Distancia²
                totalDistance += minDistances[p];
            }
            
            // Elegir nuevo centroide con probabilidad proporcional a distancia²
            double randomValue = random.nextDouble() * totalDistance;
            double cumulative = 0.0;
            int selectedIndex = 0;
            
            for (int p = 0; p < numPoints; p++) {
                cumulative += minDistances[p];
                if (cumulative >= randomValue) {
                    selectedIndex = p;
                    break;
                }
            }
            
            // Copiar punto seleccionado como nuevo centroide
            for (int j = 0; j < numFeatures; j++) {
                centroids[i][j] = dataMatrix[selectedIndex][j];
            }
        }
        
        return centroids;
    }
    
    /** Asigna cada punto al cluster más cercano. */
    private Integer[] assignToClusters(Object[][] dataMatrix, Object[][] centroids, 
                                       DistanceCalculator distance) {
        int numPoints = dataMatrix.length;
        int k = centroids.length;
        Integer[] assignments = new Integer[numPoints];
        
        for (int i = 0; i < numPoints; i++) {
            double minDistance = Double.MAX_VALUE;
            int closestCluster = 0;
            
            for (int j = 0; j < k; j++) {
                double dist = calculateDistance(dataMatrix[i], centroids[j], distance);
                if (dist < minDistance) {
                    minDistance = dist;
                    closestCluster = j;
                }
            }
            
            assignments[i] = closestCluster;
        }
        
        return assignments;
    }
    
    /** Recalcula centroides como promedio de puntos asignados. */
    private Object[][] updateCentroids(Object[][] dataMatrix, Integer[] assignments, int k) {
        int numPoints = dataMatrix.length;
        int numFeatures = dataMatrix[0].length;
        Object[][] newCentroids = new Object[k][numFeatures];
        
        double[][] sums = new double[k][numFeatures];
        int[] counts = new int[k];
        
        for (int i = 0; i < numPoints; i++) {
            int cluster = assignments[i];
            counts[cluster]++;
            
            for (int j = 0; j < numFeatures; j++) {
                if (dataMatrix[i][j] instanceof Number) {
                    sums[cluster][j] += ((Number) dataMatrix[i][j]).doubleValue();
                }
            }
        }
        
        for (int i = 0; i < k; i++) {
            if (counts[i] > 0) {
                for (int j = 0; j < numFeatures; j++) {
                    newCentroids[i][j] = sums[i][j] / counts[i];
                }
            } else {
                for (int j = 0; j < numFeatures; j++) {
                    newCentroids[i][j] = 0.0;
                }
            }
        }
        
        return newCentroids;
    }
    
    /** Verifica convergencia comparando cambio en centroides contra tolerancia. */
    private boolean hasConverged(Object[][] oldCentroids, Object[][] newCentroids) {
        int k = oldCentroids.length;
        int numFeatures = oldCentroids[0].length;
        double totalChange = 0.0;
        
        for (int i = 0; i < k; i++) {
            for (int j = 0; j < numFeatures; j++) {
                if (oldCentroids[i][j] instanceof Number && newCentroids[i][j] instanceof Number) {
                    double oldVal = ((Number) oldCentroids[i][j]).doubleValue();
                    double newVal = ((Number) newCentroids[i][j]).doubleValue();
                    totalChange += Math.abs(newVal - oldVal);
                }
            }
        }
        
        return totalChange < tolerance;
    }
    
    /** Calcula distancia entre dos puntos usando DistanceCalculator. */
    private double calculateDistance(Object[] point1, Object[] point2, DistanceCalculator distance) {
        return distance.calculateVectorDistance(point1, point2);
    }
}
