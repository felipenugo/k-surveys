package domain.clustering;

import domain.model.*;
import domain.model.enums.TypeQuestion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Implementación del algoritmo K-Means con enfoque híbrido.
 * Utiliza centroides numéricos para preguntas de opción múltiple y medoides para preguntas textuales.
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
     * @param tolerance Umbral de convergencia
     * @throws IllegalArgumentException si los parámetros no son válidos
     */
    public KMeans(int maxIterations, double tolerance) {
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

    /**
     * Constructor por defecto con valores estándar.
     * Configura 100 iteraciones máximas y tolerancia de 1e-4.
     */
    public KMeans() {
        this(100, 1e-4);
    }

    /**
     * Establece la semilla aleatoria para reproducibilidad.
     *
     * @param seed Semilla para el generador de números aleatorios
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
        return "K-Means (híbrido con K-Medoids para texto) que particiona los datos en k clusters.";
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

        int numPoints = responses.size();

        List<Centroid> centroids = initializeCentroids(responses, questions, k);

        Integer[] assignments = new Integer[numPoints];
        boolean converged = false;
        int iteration = 0;

        while (iteration < maxIterations && !converged) {
            Integer[] newAssignments = assignToClusters(responses, questions, centroids, distance);
            List<Centroid> newCentroids = updateCentroids(responses, questions, newAssignments, k, distance);
            converged = hasConverged(centroids, newCentroids, questions, distance);
            assignments = newAssignments;
            centroids = newCentroids;
            iteration++;
        }

        List<Cluster> finalClusters = createClusters(responses, questions, assignments, centroids, distance);
        return new ClusterResults(finalClusters, iteration, converged);
    }

    /**
     * Inicializa centroides seleccionando k respuestas aleatorias.
     *
     * @param responses Lista de respuestas
     * @param questions Lista de preguntas
     * @param k Número de clusters
     * @return Lista de centroides inicializados
     */
    private List<Centroid> initializeCentroids(List<Response> responses, List<Question> questions, int k) {
        List<Centroid> centroids = new ArrayList<>();
        int numQuestions = questions.size();

        Set<Integer> indices = new HashSet<>();
        while (indices.size() < k) {
            indices.add(random.nextInt(responses.size()));
        }

        for (Integer index : indices) {
            Response r = responses.get(index);
            Centroid c = new Centroid(numQuestions);
            for (int i = 0; i < numQuestions; i++) {
                Question q = questions.get(i);
                Answer a = r.getAnswer(q.getQuestionIndex());
                Object value = getAnswerValue(a, q);
                c.setComponent(i, value);
            }
            centroids.add(c);
        }
        return centroids;
    }

    /**
     * Verifica si una respuesta ha sido contestada.
     *
     * @param a Respuesta a verificar
     * @return true si la respuesta contiene datos válidos
     */
    private boolean isAnswered(Answer a) {
        if (a == null) return false;
        if (a instanceof MultipleChoiceAnswer) {
            for (boolean b : ((MultipleChoiceAnswer) a).getSelectedOptions()) {
                if (b) return true;
            }
            return false;
        }
        if (a instanceof TextualAnswer) {
            String text = ((TextualAnswer) a).getAnswerText();
            return text != null && !text.isEmpty();
        }
        if (a instanceof NumericalAnswer) {
            return ((NumericalAnswer) a).getAnswerNum() != null;
        }
        return false;
    }

    /**
     * Extrae el valor numérico o textual de una respuesta.
     *
     * @param a Respuesta a procesar
     * @param q Pregunta asociada
     * @return Valor de la respuesta (double[] o String)
     */
    private Object getAnswerValue(Answer a, Question q) {
        if (!isAnswered(a)) return null;

        try {
            if (q instanceof MultipleChoiceQuestion) {
                boolean[] val = ((MultipleChoiceAnswer) a).getSelectedOptions();
                double[] valAsDouble = new double[val.length];
                for (int i = 0; i < val.length; ++i) valAsDouble[i] = val[i] ? 1.0 : 0.0;
                return valAsDouble;
            } else if (q.getTypeQuestion() == TypeQuestion.TEXTUAL) {
                return ((TextualAnswer) a).getAnswerText();
            } else if (q.getTypeQuestion() == TypeQuestion.NUMERICAL) {
                return ((NumericalAnswer) a).getAnswerNum();
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    /**
     * Asigna cada respuesta al cluster más cercano.
     *
     * @param responses Lista de respuestas
     * @param questions Lista de preguntas
     * @param centroids Lista de centroides
     * @param distance Calculadora de distancia
     * @return Array de asignaciones de cluster
     */
    private Integer[] assignToClusters(List<Response> responses, List<Question> questions,
                                       List<Centroid> centroids, DistanceCalculator distance) {
        Integer[] assignments = new Integer[responses.size()];
        for (int i = 0; i < responses.size(); i++) {
            Response r = responses.get(i);
            double minDistance = Double.MAX_VALUE;
            int bestCluster = -1;
            for (int j = 0; j < centroids.size(); j++) {
                double d = distance.calculateToCentroid(r, centroids.get(j), questions);
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
     * Recalcula los centroides basándose en las asignaciones actuales.
     *
     * @param responses Lista de respuestas
     * @param questions Lista de preguntas
     * @param assignments Asignaciones de cluster
     * @param k Número de clusters
     * @param distance Calculadora de distancia
     * @return Lista de centroides actualizados
     */
    private List<Centroid> updateCentroids(List<Response> responses, List<Question> questions,
                                           Integer[] assignments, int k, DistanceCalculator distance) {
        int numQuestions = questions.size();
        List<Centroid> newCentroids = new ArrayList<>();

        Map<Integer, double[][]> choiceSums = new HashMap<>();
        Map<Integer, Map<Integer, List<String>>> textValues = new HashMap<>();
        Map<Integer, Map<Integer, List<Double>>> numericalValues = new HashMap<>();
        int[] clusterSizes = new int[k];

        for (int i = 0; i < responses.size(); i++) {
            int clusterIdx = assignments[i];
            if (clusterIdx == -1) continue;
            clusterSizes[clusterIdx]++;
            Response r = responses.get(i);

            for (int qIdx = 0; qIdx < numQuestions; qIdx++) {
                Question q = questions.get(qIdx);
                Answer a = r.getAnswer(q.getQuestionIndex());
                if (!isAnswered(a)) continue;

                try {
                    if (q instanceof MultipleChoiceQuestion) {
                        choiceSums.putIfAbsent(clusterIdx, new double[numQuestions][]);
                        boolean[] val = ((MultipleChoiceAnswer) a).getSelectedOptions();
                        int numOptions = val.length;
                        if (choiceSums.get(clusterIdx)[qIdx] == null) {
                            choiceSums.get(clusterIdx)[qIdx] = new double[numOptions];
                        }
                        for (int optIdx = 0; optIdx < numOptions; optIdx++) {
                            if (val[optIdx]) {
                                choiceSums.get(clusterIdx)[qIdx][optIdx] += 1.0;
                            }
                        }
                    } else if (q.getTypeQuestion() == TypeQuestion.TEXTUAL) {
                        textValues.putIfAbsent(clusterIdx, new HashMap<>());
                        textValues.get(clusterIdx).putIfAbsent(qIdx, new ArrayList<>());
                        textValues.get(clusterIdx).get(qIdx).add(((TextualAnswer) a).getAnswerText());
                    } else if (q.getTypeQuestion() == TypeQuestion.NUMERICAL) {
                        numericalValues.putIfAbsent(clusterIdx, new HashMap<>());
                        numericalValues.get(clusterIdx).putIfAbsent(qIdx, new ArrayList<>());
                        numericalValues.get(clusterIdx).get(qIdx).add(((NumericalAnswer) a).getAnswerNum());
                    }
                } catch (Exception e) { /* Ignorar este dato si hay error */ }
            }
        }

        for (int cIdx = 0; cIdx < k; cIdx++) {
            Centroid c = new Centroid(numQuestions);
            int size = clusterSizes[cIdx];
            if (size > 0) {
                for (int qIdx = 0; qIdx < numQuestions; qIdx++) {
                    Question q = questions.get(qIdx);
                    if (q instanceof MultipleChoiceQuestion) {
                        double[][] cSums = choiceSums.get(cIdx);
                        if (cSums != null && cSums[qIdx] != null) {
                            int numOptions = cSums[qIdx].length;
                            double[] avgOptions = new double[numOptions];
                            for (int optIdx = 0; optIdx < numOptions; optIdx++) {
                                avgOptions[optIdx] = cSums[qIdx][optIdx] / size;
                            }
                            c.setComponent(qIdx, avgOptions);
                        }
                    } else if (q.getTypeQuestion() == TypeQuestion.TEXTUAL) {
                        Map<Integer, List<String>> clusterTexts = textValues.get(cIdx);
                        if (clusterTexts != null && clusterTexts.get(qIdx) != null) {
                            List<String> texts = clusterTexts.get(qIdx);
                            String medoidText = findTextMedoid(texts, distance);
                            c.setComponent(qIdx, medoidText);
                        }
                    } else if (q.getTypeQuestion() == TypeQuestion.NUMERICAL) {
                        Map<Integer, List<Double>> clusterNumericals = numericalValues.get(cIdx);
                        if (clusterNumericals != null && clusterNumericals.get(qIdx) != null) {
                            List<Double> numbers = clusterNumericals.get(qIdx);
                            double sum = numbers.stream().mapToDouble(Double::doubleValue).sum();
                            c.setComponent(qIdx, sum / numbers.size());
                        }
                    }
                }
            }
            newCentroids.add(c);
        }
        return newCentroids;
    }

    /**
     * Verifica si el algoritmo ha convergido.
     *
     * @param oldCentroids Centroides de la iteración anterior
     * @param newCentroids Centroides de la iteración actual
     * @param questions Lista de preguntas
     * @param distance Calculadora de distancia
     * @return true si la diferencia es menor que la tolerancia
     */
    private boolean hasConverged(List<Centroid> oldCentroids, List<Centroid> newCentroids, List<Question> questions, DistanceCalculator distance) {
        double totalMovement = 0.0;
        for (int i = 0; i < oldCentroids.size(); i++) {
            Centroid oldC = oldCentroids.get(i);
            Centroid newC = newCentroids.get(i);
            double centroidDiff = 0.0;
            for (int qIdx = 0; qIdx < questions.size(); qIdx++) {
                Object oldComp = oldC.getComponent(qIdx);
                Object newComp = newC.getComponent(qIdx);

                if (oldComp == null || newComp == null) {
                    if (oldComp != newComp) centroidDiff += 1.0;
                    continue;
                }
                try {
                    if (oldComp instanceof double[]) { // Choice
                        double[] oldV = (double[]) oldComp;
                        double[] newV = (double[]) newComp;
                        centroidDiff += distance.euclideanDistance(oldV, newV);
                    } else if (oldComp instanceof String) { // Text
                        centroidDiff += distance.calculateTextDistance((String) oldComp, (String) newComp);
                    } else if (oldComp instanceof Double) { // Numerical
                        centroidDiff += Math.abs((Double) oldComp - (Double) newComp);
                    }
                } catch (Exception e) {
                    centroidDiff += 1.0;
                }
            }
            totalMovement += centroidDiff;
        }
        return totalMovement < tolerance;
    }

    /**
     * Encuentra el medoide (texto más representativo) en un conjunto de textos.
     *
     * @param texts Lista de textos
     * @param distance Calculadora de distancia
     * @return El texto medoide
     */
    private String findTextMedoid(List<String> texts, DistanceCalculator distance) {
        if (texts == null || texts.isEmpty()) return null;
        double minTotalDistance = Double.MAX_VALUE;
        String bestMedoid = texts.get(0);
        for (String candidate : texts) {
            double currentTotalDistance = 0.0;
            for (String other : texts) {
                currentTotalDistance += distance.calculateTextDistance(candidate, other);
            }
            if (currentTotalDistance < minTotalDistance) {
                minTotalDistance = currentTotalDistance;
                bestMedoid = candidate;
            }
        }
        return bestMedoid;
    }

    /**
     * Crea la estructura final de clusters con sus miembros.
     *
     * @param responses Lista de respuestas
     * @param questions Lista de preguntas
     * @param assignments Asignaciones de cluster
     * @param centroids Lista de centroides
     * @param distance Calculadora de distancia
     * @return Lista de objetos Cluster
     */
    private List<Cluster> createClusters(List<Response> responses, List<Question> questions,
                                         Integer[] assignments, List<Centroid> centroids,
                                         DistanceCalculator distance) {
        int k = centroids.size();
        List<Cluster> finalClusters = new ArrayList<>();
        Map<Integer, Cluster> clusterMap = new HashMap<>();

        for (int i = 0; i < k; i++) {
            Cluster cluster = new Cluster("cluster_" + (i + 1));
            cluster.setCentroid(centroids.get(i));
            finalClusters.add(cluster);
            clusterMap.put(i, cluster);
        }

        for (int i = 0; i < responses.size(); i++) {
            Integer clusterIndex = assignments[i];
            if (clusterIndex == -1) continue;
            Response r = responses.get(i);
            Cluster cluster = clusterMap.get(clusterIndex);
            double distToCentroid = distance.calculateToCentroid(r, cluster.getCentroid(), questions);
            cluster.addMember(r, distToCentroid);
        }
        return finalClusters;
    }
}
