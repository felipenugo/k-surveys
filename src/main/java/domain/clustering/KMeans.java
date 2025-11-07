package edu.upc.prop.clusterxx;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import domain.model.Question;

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
     * @param tolerance     Umbral de convergencia (cambio mínimo en centroides)
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

    // Implementando la interfaz del diagrama (List<Cluster>)
    // y añadiendo List<Question> que es esencial para el 'distance'
    @Override
    public List<Cluster> execute(List<ResponseSet> responseSets, List<Question> questions, int k,
            DistanceCalculator distance) {
        if (responseSets == null || responseSets.isEmpty()) {
            throw new IllegalArgumentException("Los datos (responseSets) no pueden ser nulos o vacíos");
        }
        if (k <= 0 || k > responseSets.size()) {
            throw new IllegalArgumentException("k debe estar entre 1 y el número de puntos");
        }

        int numPoints = responseSets.size();

        // 1. Inicializar centroides
        List<Centroid> centroids = initializeCentroids(responseSets, questions, k);

        // Variables para el bucle
        Integer[] assignments = new Integer[numPoints];
        boolean converged = false;
        int iteration = 0;

        while (iteration < maxIterations && !converged) {

            // 2. Asignar cada punto al centroide más cercano
            Integer[] newAssignments = assignToClusters(responseSets, questions, centroids, distance);

            // 3. Recalcular centroides
            List<Centroid> newCentroids = updateCentroids(responseSets, questions, newAssignments, k);

            // 4. Verificar convergencia
            converged = hasConverged(centroids, newCentroids, distance, questions); // Necesitas un 'hasConverged' que
                                                                                    // compare Centroids

            // Actualizar
            assignments = newAssignments;
            centroids = newCentroids;
            iteration++;
        }

        // 5. Generar los resultados finales (List<Cluster>)
        return createClusters(responseSets, questions, assignments, centroids, distance);
    }

    /**
     * Inicializa k centroides seleccionando aleatoriamente k puntos de los datos.
     * 
     * @param dataMatrix La matriz de datos
     * @param k          El número de clusters
     * @return Matriz de centroides iniciales
     */
    private List<Centroid> initializeCentroids(List<ResponseSet> responseSets, List<Question> questions, int k) {
        List<Centroid> centroids = new ArrayList<>();
        // Obtiene los IDs de las preguntas en orden
        List<String> questionIds = questions.stream()
                .map(Question::getId)
                .collect(Collectors.toList());

        // Elige k índices únicos aleatoriamente
        Set<Integer> indices = new HashSet<>();
        while (indices.size() < k) {
            indices.add(random.nextInt(responseSets.size()));
        }

        // Convierte los ResponseSet elegidos en Centroids
        for (Integer index : indices) {
            ResponseSet rs = responseSets.get(index);
            Centroid c = new Centroid(questionIds); // Crea un centroide

            // Rellena los componentes del centroide con los valores de la respuesta
            for (int i = 0; i < questionIds.size(); i++) {
                String qId = questionIds.get(i);
                Response r = rs.getResponse(qId);

                // Asume que la Respuesta no es nula/vacía (simplificación)
                // Extrae el valor real (Double, Set<String>, etc.)
                Object value = getResponseValue(r, questions.get(i));
                c.setComponent(i, value);
            }
            centroids.add(c);
        }
        return centroids;
    }

    private Object getResponseValue(Response r, Question q) {
        if (r == null || !r.isAnswered())
            return null;

        if (q instanceof OpenQuestion && ((OpenQuestion) q).isNumericOnly()) {
            return ((NumericResponse) r).getValue();
        }
        if (q instanceof ChoiceQuestion) {
            return ((ChoiceResponse) r).getSelectedOptions();
        }
        // ... otros tipos
        return null;
    }

    /**
     * Asigna cada punto al cluster del centroide más cercano.
     * 
     * @param dataMatrix La matriz de datos
     * @param centroids  Los centroides actuales
     * @param distance   El calculador de distancias
     * @return Array con la asignación de cluster para cada punto
     */
    private Integer[] assignToClusters(List<ResponseSet> responseSets, List<Question> questions,
            List<Centroid> centroids, DistanceCalculator distance) {
        Integer[] assignments = new Integer[responseSets.size()];

        for (int i = 0; i < responseSets.size(); i++) {
            ResponseSet rs = responseSets.get(i);
            double minDistance = Double.MAX_VALUE;
            int bestCluster = -1;

            for (int j = 0; j < centroids.size(); j++) {
                // ¡Esta es la llamada clave!
                double d = distance.calculateToCentroid(rs, centroids.get(j), questions);

                if (d < minDistance) {
                    minDistance = d;
                    bestCluster = j;
                }
            }
            assignments[i] = bestCluster;
        }
        return assignments;
    }

    /**
     * Recalcula los centroides como el promedio de los puntos asignados a cada
     * cluster.
     * 
     * @param dataMatrix  La matriz de datos
     * @param assignments Las asignaciones actuales de cluster
     * @param k           El número de clusters
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
     * Verifica si el algoritmo ha convergido comparando los centroides antiguos y
     * nuevos.
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
     * @param point1   Primer vector
     * @param point2   Segundo vector
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
