package domain.clustering;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Calcula la distancia (disimilitud) entre dos ResponseSet o
 * entre un ResponseSet y un Centroid.
 * * Esta clase implementa el patrón Strategy para las distancias locales
 * (numérica, de elección, de texto) y las agrupa en una distancia global
 * (Euclidean, Manhattan).
 */
public class DistanceCalculator {

    private DistanceType distanceType;
    private Map<String, Double> weights;

    /**
     * Constructor.
     * 
     * @param distanceType El método global para agregar distancias locales
     *                     (ej. EUCLIDEAN, MANHATTAN).
     */
    public DistanceCalculator(DistanceType distanceType) {
        this.distanceType = distanceType;
        this.weights = new HashMap<>();
    }

    /**
     * Establece un peso para una pregunta específica.
     * Las preguntas sin peso tienen un peso por defecto de 1.0.
     * 
     * @param questionId El ID de la pregunta.
     * @param weight     El peso (ej. 2.0 para darle el doble de importancia).
     */
    public void setWeight(String questionId, double weight) {
        this.weights.put(questionId, weight);
    }

    /**
     * Obtiene el mapa de pesos.
     * 
     * @return Una copia del mapa de pesos.
     */
    public Map<String, Double> getWeights() {
        return new HashMap<>(this.weights);
    }

    /**
     * Obtiene el tipo de distancia utilizado.
     * 
     * @return El tipo de distancia (EUCLIDEAN, MANHATTAN, etc.)
     */
    public DistanceType getDistanceType() {
        return this.distanceType;
    }

    /*
     * NOTA: Los siguientes métodos están comentados temporalmente porque dependen
     * de clases que aún no han sido implementadas (ResponseSet, Question, Response, etc.).
     * Una vez que esas clases existan, estos métodos deberán ser descomentados.
     * 
     * Mientras tanto, los algoritmos de clustering usan el método
     * calculateVectorDistance(Object[], Object[]) que no tiene dependencias externas.
     */
    
    /*
    /**
     * Calcula la distancia global entre dos conjuntos de respuestas.
     * Esta es la función principal que usarás para comparar dos "individuos".
     * /
    public double calculate(ResponseSet rs1, ResponseSet rs2, List<Question> questions) {
        double totalDistance = 0.0;
        double totalDistanceSquared = 0.0;

        for (Question question : questions) {
            String qId = question.getId();
            Response r1 = rs1.getResponse(qId);
            Response r2 = rs2.getResponse(qId);

            double weight = this.weights.getOrDefault(qId, 1.0);
            double localDist;

            // Maneja respuestas no contestadas
            if (r1 == null || !r1.isAnswered() || r2 == null || !r2.isAnswered()) {
                // Penalización máxima (1.0) si uno o ambos no respondieron.
                // Asumimos que todas las distancias locales están normalizadas a [0, 1].
                localDist = 1.0;
            } else {
                // Calcula la distancia local para esta pregunta
                localDist = calculateLocal(r1, r2, question);
            }

            // Agrega a la distancia global según el tipo
            if (this.distanceType == DistanceType.MANHATTAN) {
                totalDistance += (localDist * weight);
            } else {
                // Por defecto (y para EUCLIDEAN), usamos la suma de cuadrados
                totalDistanceSquared += Math.pow(localDist * weight, 2);
            }
        }

        if (this.distanceType == DistanceType.MANHATTAN) {
            return totalDistance;
        }

        // Por defecto, devuelve EUCLIDEAN
        return Math.sqrt(totalDistanceSquared);
    }

    /**
     * Calcula la distancia desde un conjunto de respuestas a un centroide.
     * Esta es la función clave para el paso de "asignación" de k-means.
     * /
    public double calculateToCentroid(ResponseSet rs, Centroid centroid, List<Question> questions) {
        double totalDistance = 0.0;
        double totalDistanceSquared = 0.0;

        // Mapea los IDs de las preguntas del centroide a sus valores para búsqueda
        // rápida
        Map<String, Object> centroidComponents = centroid.getComponentsAsMap();

        for (Question question : questions) {
            String qId = question.getId();
            Response r = rs.getResponse(qId);
            Object cValue = centroidComponents.get(qId); // Valor del centroide para esta pregunta

            double weight = this.weights.getOrDefault(qId, 1.0);
            double localDist;

            // Maneja respuesta no contestada o componente del centroide faltante
            if (r == null || !r.isAnswered() || cValue == null) {
                localDist = 1.0; // Penalización máxima
            } else {
                // Calcula la distancia local al componente del centroide
                localDist = calculateLocalToCentroid(r, cValue, question);
            }

            if (this.distanceType == DistanceType.MANHATTAN) {
                totalDistance += (localDist * weight);
            } else {
                totalDistanceSquared += Math.pow(localDist * weight, 2);
            }
        }

        if (this.distanceType == DistanceType.MANHATTAN) {
            return totalDistance;
        }
        return Math.sqrt(totalDistanceSquared);
    }

    // --- MÉTODOS PRIVADOS DE DISTANCIA LOCAL ---

    /**
     * (PRIVADO) Despachador para calcular la distancia entre dos respuestas.
     * /
    private double calculateLocal(Response r1, Response r2, Question question) {
        try {
            if (question instanceof OpenQuestion && ((OpenQuestion) question).isNumericOnly()) {
                return calculateNumericDistance(
                        (NumericResponse) r1,
                        (NumericResponse) r2,
                        (OpenQuestion) question);
            } else if (question instanceof ChoiceQuestion) {
                return calculateChoiceDistance(
                        (ChoiceResponse) r1,
                        (ChoiceResponse) r2,
                        (ChoiceQuestion) question);
            } else if (question instanceof OpenQuestion) { // Asumimos que es texto libre
                return calculateTextDistance(
                        ((ChoiceResponse) r1).getValue(), // Asume que existe ChoiceResponse
                        ((ChoiceResponse) r2).getValue() // Asume que existe ChoiceResponse
                );
            }
        } catch (ClassCastException e) {
            // Error: el tipo de Respuesta no coincide con el tipo de Pregunta
            return 1.0; // Penalización máxima
        }
        return 0.0; // Tipo de pregunta no soportado
    }

    /**
     * (PRIVADO) Despachador para calcular la distancia de una respuesta a un
     * componente del centroide.
     * /
    private double calculateLocalToCentroid(Response r, Object cValue, Question question) {
        try {
            if (question instanceof OpenQuestion && ((OpenQuestion) question).isNumericOnly()) {
                // El componente del centroide debe ser un Double (la media)
                double centroidVal = (Double) cValue;
                double responseVal = ((NumericResponse) r).getValue();
                // Normalizamos ambos para comparar
                double normR = normalizeDistance(responseVal, ((OpenQuestion) question).getMinValue(),
                        ((OpenQuestion) question).getMaxValue());
                double normC = normalizeDistance(centroidVal, ((OpenQuestion) question).getMinValue(),
                        ((OpenQuestion) question).getMaxValue());
                return Math.abs(normR - normC);

            } else if (question instanceof ChoiceQuestion) {
                // El componente del centroide puede ser un Set<String> (medoide)
                // o Map<String, Double> (distribución de probabilidad).
                // Asumimos Set<String> por simplicidad (k-medoids).
                Set<String> centroidOptions = (Set<String>) cValue;
                Set<String> responseOptions = ((ChoiceResponse) r).getSelectedOptions();
                return calculateJaccardDistance(responseOptions, centroidOptions);

            } else if (question instanceof OpenQuestion) { // Texto libre
                // El componente del centroide debe ser un String (el medoide de texto)
                String centroidText = (String) cValue;
                String responseText = ((TextResponse) r).getValue(); // Asume TextResponse
                return calculateTextDistance(responseText, centroidText);
            }
        } catch (Exception e) {
            // Error de casting o tipo de dato inesperado en el centroide
            return 1.0; // Penalización máxima
        }
        return 0.0;
    }

    /**
     * (PRIVADO) Distancia para preguntas numéricas.
     * Devuelve la distancia absoluta normalizada a [0, 1].
     * /
    private double calculateNumericDistance(NumericResponse r1, NumericResponse r2, OpenQuestion question) {
        double val1 = r1.getValue();
        double val2 = r2.getValue();
        double min = question.getMinValue();
        double max = question.getMaxValue();

        double norm1 = normalizeDistance(val1, min, max);
        double norm2 = normalizeDistance(val2, min, max);

        return Math.abs(norm1 - norm2);
    }

    /**
     * (PRIVADO) Normaliza un valor a un rango [0, 1] dados un min y max.
     * /
    private double normalizeDistance(double value, double min, double max) {
        if (max - min == 0)
            return 0.0; // Evita división por cero si max == min
        return (value - min) / (max - min);
    }

    /**
     * (PRIVADO) Distancia para preguntas de elección múltiple.
     * Usa la Distancia Jaccard.
     * /
    private double calculateChoiceDistance(ChoiceResponse r1, ChoiceResponse r2, ChoiceQuestion question) {
        Set<String> set1 = r1.getSelectedOptions();
        Set<String> set2 = r2.getSelectedOptions();
        return calculateJaccardDistance(set1, set2);
    }

    /**
     * (PRIVADO) Calcula la Distancia Jaccard (1 - Similitud Jaccard).
     * Similitud Jaccard = |A ∩ B| / |A ∪ B|
     * /
    private double calculateJaccardDistance(Set<String> set1, Set<String> set2) {
        if (set1.isEmpty() && set2.isEmpty()) {
            return 0.0; // Dos conjuntos vacíos son idénticos
        }

        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);

        double similarity = (double) intersection.size() / union.size();
        return 1.0 - similarity;
    }

    /**
     * (PRIVADO) Distancia para preguntas de texto libre.
     * Placeholder: usa Distancia de Levenshtein normalizada.
     * /
    private double calculateTextDistance(String text1, String text2) {

        if (text1.equals(text2)) {
            return 0.0;
        }

        int charDiff = 0;
        int minLen = Math.min(text1.length(), text2.length());
        int maxLen = Math.max(text1.length(), text2.length());

        for (int i = 0; i < minLen; i++) {
            if (text1.charAt(i) != text2.charAt(i)) {
                charDiff++;
            }
        }

        // Normaliza la diferencia de caracteres a [0, 1]
        return (double) charDiff / maxLen;
    }
    */
    
    // ========== MÉTODOS PARA CLUSTERING DIRECTO ==========
    
    /**
     * Calcula la distancia entre dos vectores de características (Object[]).
     * Este método es usado por los algoritmos de clustering (KMeans, KMedoids, etc.)
     * cuando trabajan con matrices de datos numéricas.
     * 
     * <p>Soporta distancias Euclidiana y Manhattan según el tipo configurado.</p>
     * 
     * @param vector1 Primer vector de características (debe contener Number)
     * @param vector2 Segundo vector de características (debe contener Number)
     * @return La distancia entre los dos vectores
     * @throws IllegalArgumentException Si los vectores tienen longitudes diferentes
     *                                  o contienen valores no numéricos
     */
    public double calculateVectorDistance(Object[] vector1, Object[] vector2) {
        if (vector1 == null || vector2 == null) {
            throw new IllegalArgumentException("Los vectores no pueden ser null");
        }
        
        if (vector1.length != vector2.length) {
            throw new IllegalArgumentException(
                "Los vectores deben tener la misma longitud: " + 
                vector1.length + " vs " + vector2.length
            );
        }
        
        if (vector1.length == 0) {
            return 0.0;
        }
        
        double sum = 0.0;
        
        for (int i = 0; i < vector1.length; i++) {
            if (!(vector1[i] instanceof Number) || !(vector2[i] instanceof Number)) {
                throw new IllegalArgumentException(
                    "Los elementos del vector en posición " + i + 
                    " deben ser numéricos (Number)"
                );
            }
            
            double val1 = ((Number) vector1[i]).doubleValue();
            double val2 = ((Number) vector2[i]).doubleValue();
            double diff = val1 - val2;
            
            if (this.distanceType == DistanceType.MANHATTAN) {
                sum += Math.abs(diff);
            } else {
                // Por defecto EUCLIDEAN
                sum += diff * diff;
            }
        }
        
        // Para Euclidiana, devolver la raíz cuadrada
        if (this.distanceType == DistanceType.EUCLIDEAN) {
            return Math.sqrt(sum);
        }
        
        // Para Manhattan, devolver la suma directamente
        return sum;
    }
    
    /**
     * Calcula la distancia entre dos vectores usando el tipo de distancia especificado.
     * Método de conveniencia que permite sobrescribir el tipo de distancia configurado.
     * 
     * @param vector1 Primer vector
     * @param vector2 Segundo vector
     * @param type Tipo de distancia a usar (EUCLIDEAN, MANHATTAN, COSINE)
     * @return La distancia entre los vectores
     */
    public double calculateVectorDistance(Object[] vector1, Object[] vector2, DistanceType type) {
        DistanceType originalType = this.distanceType;
        this.distanceType = type;
        
        try {
            return calculateVectorDistance(vector1, vector2);
        } finally {
            this.distanceType = originalType;
        }
    }
}