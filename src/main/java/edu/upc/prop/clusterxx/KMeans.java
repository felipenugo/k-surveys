package edu.upc.prop.clusterxx;

import java.util.Random;

/**
 * Implementación del algoritmo K-Means para clustering.
 * 
 * K-Means es un algoritmo iterativo que:
 * 1. Inicializa k centroides aleatoriamente
 * 2. Asigna cada punto al centroide más cercano
 * 3. Recalcula los centroides como el promedio de los puntos asignados
 * 4. Repite hasta convergencia o alcanzar el máximo de iteraciones
 */
public class KMeans implements ClusteringAlgorithm {

    private int maxIterations;
    private double tolerance;
    private long randomSeed;
    private Random random;

    /**
     * Constructor con parámetros personalizados.
     * 
     * @param maxIterations Número máximo de iteraciones permitidas
     * @param tolerance Umbral de convergencia (cambio mínimo en centroides)
     */
    public KMeans(int maxIterations, double tolerance) {
        this.maxIterations = maxIterations;
        this.tolerance = tolerance;
        this.randomSeed = System.currentTimeMillis();
        this.random = new Random(randomSeed);
    }

    /**
     * Constructor con valores por defecto.
     */
    public KMeans() {
        this(100, 1e-4);
    }

    /**
     * Establece la semilla aleatoria para reproducibilidad.
     * 
     * @param seed La semilla para el generador de números aleatorios
     */
    public void setRandomSeed(long seed) {
        this.randomSeed = seed;
        this.random = new Random(seed);
    }

    @Override
    public String getName() {
        return "K-Means";
    }

    @Override
    public String getDescription() {
        return "Algoritmo K-Means clásico que particiona los datos en k clusters " +
               "minimizando la suma de las distancias cuadradas dentro de cada cluster.";
    }

    @Override
    public ClusterResults execute(Object[][] dataMatrix, int k, DistanceCalculator distance) {
        if (dataMatrix == null || dataMatrix.length == 0) {
            throw new IllegalArgumentException("La matriz de datos no puede ser nula o vacía");
        }
        if (k <= 0 || k > dataMatrix.length) {
            throw new IllegalArgumentException("k debe estar entre 1 y el número de puntos");
        }

        int numPoints = dataMatrix.length;
        int numFeatures = dataMatrix[0].length;

        // Inicializar resultados
        ClusterResults results = new ClusterResults(k, numPoints, numFeatures);

        // Copiar datos a la matriz de resultados
        for (int i = 0; i < numPoints; i++) {
            results.setResponseData(i, dataMatrix[i]);
        }

        // 1. Inicializar centroides aleatoriamente
        Object[][] centroids = initializeCentroids(dataMatrix, k);

        // Variables para el bucle iterativo
        Integer[] assignments = new Integer[numPoints];
        boolean converged = false;
        int iteration = 0;

        // 2. Bucle principal del algoritmo
        while (iteration < maxIterations && !converged) {
            // 2.1 Asignar cada punto al centroide más cercano
            Integer[] newAssignments = assignToClusters(dataMatrix, centroids, distance);

            // 2.2 Recalcular centroides
            Object[][] newCentroids = updateCentroids(dataMatrix, newAssignments, k);

            // 2.3 Verificar convergencia
            converged = hasConverged(centroids, newCentroids);

            // Actualizar para la siguiente iteración
            assignments = newAssignments;
            centroids = newCentroids;
            iteration++;
        }

        // 3. Almacenar resultados finales
        for (int i = 0; i < numPoints; i++) {
            results.setClusterAssignment(i, assignments[i]);
            
            // Calcular distancia del punto a su centroide asignado
            double dist = calculateDistance(dataMatrix[i], centroids[assignments[i]], distance);
            results.setDistance(i, dist);
        }

        // Almacenar centroides finales
        for (int j = 0; j < k; j++) {
            results.setCentroid(j, centroids[j]);
        }

        return results;
    }

    /**
     * Inicializa k centroides seleccionando aleatoriamente k puntos de los datos.
     * 
     * @param dataMatrix La matriz de datos
     * @param k El número de clusters
     * @return Matriz de centroides iniciales
     */
    private Object[][] initializeCentroids(Object[][] dataMatrix, int k) {
        int numPoints = dataMatrix.length;
        int numFeatures = dataMatrix[0].length;
        Object[][] centroids = new Object[k][numFeatures];

        // Seleccionar k índices únicos aleatoriamente
        boolean[] selected = new boolean[numPoints];
        int count = 0;

        while (count < k) {
            int index = random.nextInt(numPoints);
            if (!selected[index]) {
                selected[index] = true;
                // Copiar el punto como centroide inicial
                for (int j = 0; j < numFeatures; j++) {
                    centroids[count][j] = dataMatrix[index][j];
                }
                count++;
            }
        }

        return centroids;
    }

    /**
     * Asigna cada punto al cluster del centroide más cercano.
     * 
     * @param dataMatrix La matriz de datos
     * @param centroids Los centroides actuales
     * @param distance El calculador de distancias
     * @return Array con la asignación de cluster para cada punto
     */
    private Integer[] assignToClusters(Object[][] dataMatrix, Object[][] centroids, 
                                       DistanceCalculator distance) {
        int numPoints = dataMatrix.length;
        Integer[] assignments = new Integer[numPoints];

        for (int i = 0; i < numPoints; i++) {
            double minDistance = Double.MAX_VALUE;
            int closestCluster = 0;

            // Encontrar el centroide más cercano
            for (int j = 0; j < centroids.length; j++) {
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

    /**
     * Recalcula los centroides como el promedio de los puntos asignados a cada cluster.
     * 
     * @param dataMatrix La matriz de datos
     * @param assignments Las asignaciones actuales de cluster
     * @param k El número de clusters
     * @return Nueva matriz de centroides
     */
    private Object[][] updateCentroids(Object[][] dataMatrix, Integer[] assignments, int k) {
        int numFeatures = dataMatrix[0].length;
        Object[][] newCentroids = new Object[k][numFeatures];
        int[] clusterSizes = new int[k];

        // Inicializar acumuladores
        double[][] sums = new double[k][numFeatures];

        // Acumular valores por cluster
        for (int i = 0; i < dataMatrix.length; i++) {
            int cluster = assignments[i];
            clusterSizes[cluster]++;

            for (int j = 0; j < numFeatures; j++) {
                double value = convertToDouble(dataMatrix[i][j]);
                sums[cluster][j] += value;
            }
        }

        // Calcular promedios
        for (int cluster = 0; cluster < k; cluster++) {
            if (clusterSizes[cluster] > 0) {
                for (int j = 0; j < numFeatures; j++) {
                    newCentroids[cluster][j] = sums[cluster][j] / clusterSizes[cluster];
                }
            } else {
                // Si un cluster está vacío, reinicializarlo con un punto aleatorio
                int randomIndex = random.nextInt(dataMatrix.length);
                for (int j = 0; j < numFeatures; j++) {
                    newCentroids[cluster][j] = dataMatrix[randomIndex][j];
                }
            }
        }

        return newCentroids;
    }

    /**
     * Verifica si el algoritmo ha convergido comparando los centroides antiguos y nuevos.
     * 
     * @param oldCentroids Centroides de la iteración anterior
     * @param newCentroids Centroides de la iteración actual
     * @return true si la diferencia está por debajo del umbral de tolerancia
     */
    private boolean hasConverged(Object[][] oldCentroids, Object[][] newCentroids) {
        double maxChange = 0.0;

        for (int i = 0; i < oldCentroids.length; i++) {
            double change = 0.0;
            for (int j = 0; j < oldCentroids[i].length; j++) {
                double oldVal = convertToDouble(oldCentroids[i][j]);
                double newVal = convertToDouble(newCentroids[i][j]);
                change += Math.abs(oldVal - newVal);
            }
            maxChange = Math.max(maxChange, change);
        }

        return maxChange < tolerance;
    }

    /**
     * Calcula la distancia entre dos vectores usando el calculador de distancias.
     * 
     * @param point1 Primer vector
     * @param point2 Segundo vector
     * @param distance El calculador de distancias
     * @return La distancia entre los dos vectores
     */
    private double calculateDistance(Object[] point1, Object[] point2, DistanceCalculator distance) {
        // Convertir a valores numéricos y calcular distancia
        double sum = 0.0;
        
        for (int i = 0; i < point1.length; i++) {
            double val1 = convertToDouble(point1[i]);
            double val2 = convertToDouble(point2[i]);
            double diff = val1 - val2;
            
            // Usar el tipo de distancia apropiado
            if (distance.getDistanceType() == DistanceType.EUCLIDEAN) {
                sum += diff * diff;
            } else if (distance.getDistanceType() == DistanceType.MANHATTAN) {
                sum += Math.abs(diff);
            }
        }
        
        // Para distancia Euclidiana, tomar la raíz cuadrada
        if (distance.getDistanceType() == DistanceType.EUCLIDEAN) {
            return Math.sqrt(sum);
        }
        
        return sum;
    }

    /**
     * Convierte un Object a double para cálculos numéricos.
     * 
     * @param obj El objeto a convertir
     * @return El valor como double
     */
    private double convertToDouble(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).doubleValue();
        } else if (obj instanceof String) {
            try {
                return Double.parseDouble((String) obj);
            } catch (NumberFormatException e) {
                // Para valores de texto, usar hash code normalizado
                return (double) obj.hashCode();
            }
        }
        return 0.0;
    }

    // Getters
    public int getMaxIterations() {
        return maxIterations;
    }

    public double getTolerance() {
        return tolerance;
    }

    public long getRandomSeed() {
        return randomSeed;
    }
}
