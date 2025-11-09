package domain.clustering;

import java.util.ArrayList;
import java.util.HashMap; // Importado
import java.util.HashSet;
import java.util.List;
import java.util.Map; // Importado
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

// Importaciones de dominio necesarias
import domain.model.Question;
import domain.model.Response;
import domain.model.ResponseSet;
import domain.model.Cluster;
import domain.model.Centroid;
import domain.model.question.ChoiceQuestion;
import domain.model.question.OpenQuestion;
import domain.model.response.MultipleChoiceResponse;
import domain.model.response.TextualResponse;

/**
 * Implementación del algoritmo K-Means para clustering.
 * * Esta es una implementación HÍBRIDA:
 * 1. Para datos numéricos y de elección (choice), calcula un 'promedio' (K-Means).
 * 2. Para datos de texto, encuentra el 'medoide' (el punto más central) (K-Medoids).
 * * K-Means es un algoritmo iterativo que:
 * 1. Inicializa k centroides aleatoriamente (tomando k puntos existentes).
 * 2. Asigna cada punto al centroide más cercano.
 * 3. Recalcula los centroides (como promedio/medoide) de los puntos asignados.
 * 4. Repite hasta convergencia o alcanzar el máximo de iteraciones.
 */
public class KMeans implements ClusteringAlgorithm {
    
    // ========== ATRIBUTOS ==========
    
    private int maxIterations;
    private double tolerance;
    private long randomSeed;
    private Random random;
    
    // ========== CONSTRUCTORES ==========
    
    /**
     * Constructor con parámetros personalizados.
     * * @param maxIterations Número máximo de iteraciones permitidas.
     * @param tolerance     Umbral de convergencia (cambio mínimo en centroides).
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
     * Constructor con valores por defecto (100 iteraciones, tolerancia 1e-4).
     */
    public KMeans() {
        this(100, 1e-4);
    }

    /**
     * Establece la semilla aleatoria para reproducibilidad.
     * * @param seed La semilla para el generador de números aleatorios.
     */
    public void setRandomSeed(long seed) {
        this.randomSeed = seed;
        this.random = new Random(seed);
    }

    /**
     * Obtiene el nombre del algoritmo.
     * * @return El string "K-Means".
     */
    @Override
    public String getName() {
        return "K-Means";
    }

    /**
     * Obtiene la descripción del algoritmo.
     * * @return Una descripción de lo que hace el algoritmo.
     */
    @Override
    public String getDescription() {
        return "Algoritmo K-Means (híbrido con K-Medoids para texto) que particiona los datos en k clusters.";
    }

    /**
     * Ejecuta el algoritmo de clustering K-Means.
     * * @param responseSets La lista de conjuntos de respuestas (puntos de datos).
     * @param questions    La lista de preguntas, necesaria para interpretar los datos.
     * @param k            El número de clusters a formar.
     * @param distance     El objeto DistanceCalculator para medir distancias.
     * @return Una lista de los {@link Cluster} finales, cada uno con sus miembros.
     */
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
            List<Centroid> newCentroids = updateCentroids(responseSets, questions, newAssignments, k, distance);
            
            // 4. Verificar convergencia
            converged = hasConverged(centroids, newCentroids, distance, questions);

            // Actualizar
            assignments = newAssignments;
            centroids = newCentroids;
            iteration++;
        }

        // 5. Generar los resultados finales (List<Cluster>)
        return createClusters(responseSets, questions, assignments, centroids, distance);
    }

    /**
     * (PRIVADO) Inicializa k centroides seleccionando aleatoriamente k puntos de los datos.
     * * @param responseSets La lista de todos los puntos de datos.
     * @param questions    La lista de preguntas.
     * @param k            El número de clusters.
     * @return Una lista de 'k' centroides iniciales.
     */
    private List<Centroid> initializeCentroids(List<ResponseSet> responseSets, List<Question> questions, int k) {
        List<Centroid> centroids = new ArrayList<>();
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
            Centroid c = new Centroid(questionIds); // Asume constructor que toma IDs

            // Rellena los componentes del centroide con los valores de la respuesta
            for (int i = 0; i < questionIds.size(); i++) {
                String qId = questionIds.get(i);
                Response r = rs.getResponse(qId);
                
                // Extrae el valor de la respuesta (Double, double[], o String)
                Object value = getResponseValue(r, questions.get(i));
                c.setComponent(i, value); // Asume setComponent por índice
            }
            centroids.add(c);
        }
        return centroids;
    }

    /**
     * (PRIVADO) Extrae el valor crudo de una respuesta para inicializar un centroide.
     * * @param r La respuesta (Response).
     * @param q La pregunta (Question) correspondiente.
     * @return El valor (Double, double[] o String) o null si no es válida.
     */
    private Object getResponseValue(Response r, Question q) {
        if (r == null || !r.isAnswered())
            return null;

        try {
            if (q instanceof ChoiceQuestion) {
                // Convierte boolean[] a double[] (1.0/0.0) para el centroide
                boolean[] val = ((MultipleChoiceResponse) r).getValue();
                double[] valAsDouble = new double[val.length];
                for(int i=0; i<val.length; ++i) valAsDouble[i] = val[i] ? 1.0 : 0.0;
                return valAsDouble;

            } else if (q instanceof OpenQuestion && ((OpenQuestion) q).isNumericOnly()) {
                // K-Means necesita un Double, no un String.
                String sVal = ((TextualResponse) r).getValue();
                return Double.parseDouble(sVal); 

            } else if (q instanceof OpenQuestion) {
                // K-Medoids para texto: devuelve el String
                return ((TextualResponse) r).getValue(); 
            }
        } catch (Exception e) {
            return null; // Error de casting o parseo
        }
        return null;
    }

    /**
     * (PRIVADO) Asigna cada punto de datos al centroide más cercano.
     * * @param responseSets La lista de todos los puntos de datos.
     * @param questions    La lista de preguntas.
     * @param centroids    La lista de centroides actuales.
     * @param distance     El calculador de distancias.
     * @return Un array de Enteros donde `assignments[i]` es el índice (0 a k-1) del cluster al que pertenece `responseSets.get(i)`.
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
     * (PRIVADO) Recalcula los centroides basándose en los puntos asignados a cada cluster.
     * * @param responseSets La lista de todos los puntos de datos.
     * @param questions    La lista de preguntas.
     * @param assignments  El array de asignaciones (índice de cluster para cada punto).
     * @param k            El número de clusters.
     * @param distance     El calculador de distancias (necesario para encontrar el medoid de texto).
     * @return Una nueva lista de centroides actualizados.
     */
    private List<Centroid> updateCentroids(List<ResponseSet> responseSets, List<Question> questions, 
                                        Integer[] assignments, int k, DistanceCalculator distance) {
        
        List<String> questionIds = questions.stream().map(Question::getId).collect(Collectors.toList());
        List<Centroid> newCentroids = new ArrayList<>();
        
        // Acumuladores
        Map<Integer, double[]> numericSums = new HashMap<>(); // clusterIdx -> double[questionIdx]
        Map<Integer, double[][]> choiceSums = new HashMap<>(); // clusterIdx -> double[questionIdx][optionIdx]
        Map<Integer, Map<Integer, List<String>>> textValues = new HashMap<>(); // clusterIdx -> qIdx -> List<String>
        
        int[] clusterSizes = new int[k];

        // 1. Acumular valores
        for (int i = 0; i < responseSets.size(); i++) {
            ResponseSet rs = responseSets.get(i);
            int clusterIdx = assignments[i];
            if (clusterIdx == -1) continue; 
            
            clusterSizes[clusterIdx]++;

            for (int qIdx = 0; qIdx < questions.size(); qIdx++) {
                Question q = questions.get(qIdx);
                Response r = rs.getResponse(q.getId());
                if (r == null || !r.isAnswered()) continue; 

                try {
                    if (q instanceof ChoiceQuestion) {
                        // Acumular para promedio de Choice
                        choiceSums.putIfAbsent(clusterIdx, new double[questions.size()][]);
                        boolean[] val = ((MultipleChoiceResponse) r).getValue();
                        int numOptions = val.length;
                        if (choiceSums.get(clusterIdx)[qIdx] == null) {
                            choiceSums.get(clusterIdx)[qIdx] = new double[numOptions];
                        }
                        for (int optIdx = 0; optIdx < numOptions; optIdx++) {
                            if (val[optIdx]) {
                                choiceSums.get(clusterIdx)[qIdx][optIdx] += 1.0;
                            }
                        }

                    } else if (q instanceof OpenQuestion && ((OpenQuestion) q).isNumericOnly()) {
                        // Acumular para promedio Numérico
                        numericSums.putIfAbsent(clusterIdx, new double[questions.size()]);
                        String sVal = ((TextualResponse) r).getValue();
                        double dVal = Double.parseDouble(sVal);
                        numericSums.get(clusterIdx)[qIdx] += dVal;
                        
                    } else if (q instanceof OpenQuestion) {
                        // Acumular Textos para encontrar el medoid
                        textValues.putIfAbsent(clusterIdx, new HashMap<>());
                        textValues.get(clusterIdx).putIfAbsent(qIdx, new ArrayList<>());
                        String sVal = ((TextualResponse) r).getValue();
                        textValues.get(clusterIdx).get(qIdx).add(sVal);
                    }
                    
                } catch (Exception e) { /* Ignorar este dato si hay error */ }
            }
        }

        // 2. Calcular promedios Y MEDOIDS
        for (int cIdx = 0; cIdx < k; cIdx++) {
            Centroid c = new Centroid(questionIds);
            int size = clusterSizes[cIdx];

            if (size > 0) {
                for (int qIdx = 0; qIdx < questions.size(); qIdx++) {
                    Question q = questions.get(qIdx);

                    if (q instanceof ChoiceQuestion) {
                        // Calcular promedio de Choice (probabilidades)
                        double[][] cSums = choiceSums.get(cIdx);
                        if (cSums != null && cSums[qIdx] != null) {
                            int numOptions = cSums[qIdx].length;
                            double[] avgOptions = new double[numOptions];
                            for (int optIdx = 0; optIdx < numOptions; optIdx++) {
                                avgOptions[optIdx] = cSums[qIdx][optIdx] / size;
                            }
                            c.setComponent(qIdx, avgOptions); // Centroide guarda double[]
                        }

                    } else if (q instanceof OpenQuestion && ((OpenQuestion) q).isNumericOnly()) {
                        // Calcular promedio Numérico
                        double[] nSums = numericSums.get(cIdx);
                        if (nSums != null) {
                            double avgNumeric = nSums[qIdx] / size;
                            c.setComponent(qIdx, avgNumeric); // Centroide guarda Double
                        }
                    } else if (q instanceof OpenQuestion) {
                        // Encontrar el Medoide de texto
                        Map<Integer, List<String>> clusterTexts = textValues.get(cIdx);
                        if (clusterTexts != null && clusterTexts.get(qIdx) != null) {
                            List<String> texts = clusterTexts.get(qIdx);
                            String medoidText = findTextMedoid(texts); // K-Medoid step
                            c.setComponent(qIdx, medoidText); // Centroide guarda String
                        }
                    }
                }
            } else {
                // Cluster vacío. Se podría reinicializar aleatoriamente,
                // pero por ahora se queda vacío y no atraerá puntos.
            }
            newCentroids.add(c);
        }
        
        return newCentroids;
    }

    /**
     * (PRIVADO) Verifica si el algoritmo ha convergido comparando la distancia
     * entre los centroides antiguos y nuevos.
     * * @param oldCentroids Centroides de la iteración anterior.
     * @param newCentroids Centroides de la iteración actual.
     * @param distance     El calculador de distancias.
     * @param questions    La lista de preguntas.
     * @return true si el movimiento total de centroides es menor que la `tolerance`, false en caso contrario.
     */
    private boolean hasConverged(List<Centroid> oldCentroids, List<Centroid> newCentroids, 
                             DistanceCalculator distance, List<Question> questions) {
        
        double totalMovement = 0.0;

        for (int i = 0; i < oldCentroids.size(); i++) {
            Centroid oldC = oldCentroids.get(i);
            Centroid newC = newCentroids.get(i);

            double centroidDiff = 0.0;
            for (int qIdx = 0; qIdx < questions.size(); qIdx++) {
                Object oldComp = oldC.getComponent(qIdx); // Asume getComponent(i)
                Object newComp = newC.getComponent(qIdx);

                if (oldComp == null || newComp == null) {
                    if (oldComp != null || newComp != null) centroidDiff += 1.0; // Uno es nulo y el otro no
                    continue; 
                }

                try {
                    if (oldComp instanceof double[]) { // ChoiceQuestion
                        double[] oldV = (double[]) oldComp;
                        double[] newV = (double[]) newComp;
                        double diff = 0.0;
                        for (int j = 0; j < oldV.length; j++) {
                            diff += Math.pow(oldV[j] - newV[j], 2);
                        }
                        centroidDiff += Math.sqrt(diff); // Distancia euclidiana

                    } else if (oldComp instanceof Double) { // Numeric OpenQuestion
                        double oldV = (Double) oldComp;
                        double newV = (Double) newComp;
                        OpenQuestion oq = (OpenQuestion) questions.get(qIdx);
                        double normOld = normalizeValue(oldV, oq.getMinValue(), oq.getMaxValue());
                        double normNew = normalizeValue(newV, oq.getMinValue(), oq.getMaxValue());
                        centroidDiff += Math.abs(normOld - normNew); // Distancia normalizada
                        
                    } else if (oldComp instanceof String) { // Text OpenQuestion
                        String oldV = (String) oldComp;
                        String newV = (String) newV;
                        centroidDiff += calculateTextDistance(oldV, newV); // Distancia de texto
                    }
                } catch (Exception e) {
                    centroidDiff += 1.0; // Error de casting
                }
            }
            totalMovement += centroidDiff;
        }
        
        return totalMovement < tolerance;
    }

    /**
     * Obtiene el número máximo de iteraciones.
     * @return El número máximo de iteraciones.
     */
    public int getMaxIterations() {
        return maxIterations;
    }

    /**
     * Obtiene el umbral de tolerancia para la convergencia.
     * @return El valor de tolerancia.
     */
    public double getTolerance() {
        return tolerance;
    }

    /**
     * Obtiene la semilla aleatoria usada.
     * @return La semilla aleatoria (long).
     */
    public long getRandomSeed() {
        return randomSeed;
    }

    /**
     * (PRIVADO) Normaliza un valor numérico a un rango [0, 1].
     * * @param value El valor a normalizar.
     * @param min   El valor mínimo del rango.
     * @param max   El valor máximo del rango.
     * @return El valor normalizado.
     */
    private double normalizeValue(double value, double min, double max) {
        if (max - min == 0) return 0.0;
        // No "clampea" (restringe) el valor, ya que el centroide puede estar fuera del rango min/max
        return (value - min) / (max - min);
    }

    /**
     * (PRIVADO) Encuentra el "medoide" de texto para un clúster.
     * El medoide es el string que tiene la menor distancia total a todos
     * los otros strings en la lista.
     * * @param texts La lista de todos los valores de texto en el cluster para una pregunta.
     * @return El string (medoide) más central de la lista.
     */
    private String findTextMedoid(List<String> texts) {
        if (texts == null || texts.isEmpty()) return null;

        double minTotalDistance = Double.MAX_VALUE;
        String bestMedoid = texts.get(0);

        // Comprobar cada texto como un candidato a medoid
        for (String candidate : texts) {
            double currentTotalDistance = 0.0;
            // Calcular su distancia total a todos los demás textos
            for (String other : texts) {
                currentTotalDistance += calculateTextDistance(candidate, other); 
            }

            if (currentTotalDistance < minTotalDistance) {
                minTotalDistance = currentTotalDistance;
                bestMedoid = candidate;
            }
        }
        return bestMedoid;
    }

    /**
     * (PRIVADO) Calcula la distancia entre dos textos.
     * * @param text1 El primer string.
     * @param text2 El segundo string.
     * @return La distancia normalizada en [0, 1].
     */
    private double calculateTextDistance(String text1, String text2) {
        if (text1 == null || text2 == null) {
             return (text1 == text2) ? 0.0 : 1.0; // Penalización si uno es nulo
        }
        if (text1.equals(text2)) {
            return 0.0;
        }

        int charDiff = 0;
        int minLen = Math.min(text1.length(), text2.length());
        int maxLen = Math.max(text1.length(), text2.length());
        
        if (maxLen == 0) return 0.0; // Ambos strings vacíos

        for (int i = 0; i < minLen; i++) {
            if (text1.charAt(i) != text2.charAt(i)) {
                charDiff++;
            }
        }
        
        // Añadir diferencia de longitud
        charDiff += (maxLen - minLen);

        // Normaliza la diferencia de caracteres a [0, 1]
        return (double) charDiff / maxLen;
    }

    /**
     * (PRIVADO) Genera la lista final de objetos Cluster a partir de los resultados.
     * * @param responseSets La lista de todos los puntos de datos.
     * @param questions    La lista de preguntas.
     * @param assignments  El array final de asignaciones de cluster.
     * @param centroids    La lista final de centroides.
     * @param distance     El calculador de distancias.
     * @return Una lista de {@link Cluster}, cada uno con sus miembros asignados.
     */
    private List<Cluster> createClusters(List<ResponseSet> responseSets, List<Question> questions, 
                                         Integer[] assignments, List<Centroid> centroids, 
                                         DistanceCalculator distance) {
        
        int k = centroids.size();
        List<Cluster> finalClusters = new ArrayList<>();
        Map<Integer, Cluster> clusterMap = new HashMap<>();

        // Inicializar los clusters vacíos con sus centroides
        for (int i = 0; i < k; i++) {
            Cluster cluster = new Cluster("cluster_" + (i + 1)); // Asume ID/label
            cluster.setCentroid(centroids.get(i)); // Asume setCentroid
            finalClusters.add(cluster);
            clusterMap.put(i, cluster);
        }

        // Asignar los miembros (ResponseSet) a cada cluster
        for (int i = 0; i < responseSets.size(); i++) {
            Integer clusterIndex = assignments[i];
            if (clusterIndex == -1) {
                continue; // No fue asignado (raro, pero posible si k=0 o hay error)
            }

            ResponseSet rs = responseSets.get(i);
            Cluster cluster = clusterMap.get(clusterIndex);
            
            // Recalcular la distancia final al centroide de su cluster
            double distToCentroid = distance.calculateToCentroid(rs, cluster.getCentroid(), questions);
            
            // Asume que Cluster tiene un método addMember
            cluster.addMember(rs, distToCentroid); 
        }

        return finalClusters;
    }
}