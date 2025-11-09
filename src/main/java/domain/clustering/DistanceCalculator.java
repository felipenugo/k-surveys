package edu.upc.prop.clusterxx;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Asumo que estas clases están en sus paquetes correctos
import domain.model.Response;
import domain.model.ResponseSet;
import domain.model.Centroid;
import domain.model.Question;
import domain.model.question.ChoiceQuestion;
import domain.model.question.OpenQuestion;
import domain.model.response.MultipleChoiceResponse;
import domain.model.response.TextualResponse;


/**
 * Calcula la distancia (similitud) entre dos ResponseSet o
 * entre un ResponseSet y un Centroid.
 * Esta clase implementa el patrón Strategy para las distancias locales
 * (numérica, de elección, de texto) y las agrupa en una distancia global
 * (Euclidean, Manhattan).
 */
public class DistanceCalculator {

    private DistanceType distanceType;
    private Map<String, Double> weights;

    /**
     * Constructor.
     * * @param distanceType El método global para agregar distancias locales
     * (ej. EUCLIDEAN, MANHATTAN).
     */
    public DistanceCalculator(DistanceType distanceType) {
        this.distanceType = distanceType;
        this.weights = new HashMap<>();
    }

    /**
     * Establece un peso para una pregunta específica.
     * Las preguntas sin peso tienen un peso por defecto de 1.0.
     * * @param questionId El ID de la pregunta.
     * @param weight     El peso (ej. 2.0 para darle el doble de importancia).
     */
    public void setWeight(String questionId, double weight) {
        this.weights.put(questionId, weight);
    }

    /**
     * Obtiene el mapa de pesos.
     * * @return Una copia del mapa de pesos.
     */
    public Map<String, Double> getWeights() {
        return new HashMap<>(this.weights);
    }

    /**
     * Obtiene el tipo de distancia utilizado.
     * * @return El tipo de distancia (EUCLIDEAN, MANHATTAN, etc.)
     */
    public DistanceType getDistanceType() {
        return this.distanceType;
    }

    /**
     * Calcula la distancia global entre dos conjuntos de respuestas (dos puntos de datos reales).
     * Esta es la función principal usada por K-Medoids.
     *
     * @param rs1         El primer conjunto de respuestas (punto de datos).
     * @param rs2         El segundo conjunto de respuestas (punto de datos).
     * @param questions   La lista de preguntas, para determinar el tipo de cada respuesta.
     * @return La distancia global calculada entre rs1 y rs2.
     */
    public double calculate(ResponseSet rs1, ResponseSet rs2, List<Question> questions) {
        double totalDistanceSquared = 0.0;
        double totalDistanceManhattan = 0.0;

        for (Question question : questions) {
            String qId = question.getId();
            Response r1 = rs1.getResponse(qId);
            Response r2 = rs2.getResponse(qId);
            double weight = this.weights.getOrDefault(qId, 1.0);
            
            double localDist = calculateLocal(r1, r2, question); // Llama al método local

            if (this.distanceType == DistanceType.MANHATTAN) {
                totalDistanceManhattan += (localDist * weight);
            } else {
                totalDistanceSquared += Math.pow(localDist * weight, 2);
            }
        }

        if (this.distanceType == DistanceType.MANHATTAN) {
            return totalDistanceManhattan;
        }
        return Math.sqrt(totalDistanceSquared); // EUCLIDEAN por defecto
    }

    /**
     * Calcula la distancia desde un conjunto de respuestas (punto real) a un centroide (punto artificial).
     * Usado por K-Means y K-Means++.
     *
     * @param rs         El conjunto de respuestas (punto de datos).
     * @param centroid   El centroide (punto artificial/promedio) con el que comparar.
     * @param questions  La lista de preguntas, para determinar el tipo de cada componente.
     * @return La distancia global calculada entre el punto y el centroide.
     */
    public double calculateToCentroid(ResponseSet rs, Centroid centroid, List<Question> questions) {
        double totalDistanceSquared = 0.0;
        double totalDistanceManhattan = 0.0;
        // Asume que Centroid tiene un método para obtener sus componentes (valores) como un Map
        Map<String, Object> centroidComponents = centroid.getComponentsAsMap(); 

        for (Question question : questions) {
            String qId = question.getId();
            Response r = rs.getResponse(qId);
            Object cValue = centroidComponents.get(qId); // Valor del centroide (Double, double[] o String)
            double weight = this.weights.getOrDefault(qId, 1.0);

            double localDist = calculateLocalToCentroid(r, cValue, question); // Llama al método local

            if (this.distanceType == DistanceType.MANHATTAN) {
                totalDistanceManhattan += (localDist * weight);
            } else {
                totalDistanceSquared += Math.pow(localDist * weight, 2);
            }
        }
        
        if (this.distanceType == DistanceType.MANHATTAN) {
            return totalDistanceManhattan;
        }
        return Math.sqrt(totalDistanceSquared); // EUCLIDEAN por defecto
    }

    // --- MÉTODOS PRIVADOS DE DISTANCIA LOCAL ---

    /**
     * (PRIVADO) Despachador para calcular la distancia entre dos respuestas reales.
     *
     * @param r1         La primera respuesta (para una pregunta específica).
     * @param r2         La segunda respuesta (para la misma pregunta).
     * @param question   La pregunta correspondiente, para saber cómo comparar (choice, numeric, text).
     * @return La distancia local (normalizada a [0, 1]) entre las dos respuestas.
     */
    private double calculateLocal(Response r1, Response r2, Question question) {
        // Penalización máxima si alguno no respondió
        if (r1 == null || !r1.isAnswered() || r2 == null || !r2.isAnswered()) {
            return 1.0; 
        }

        try {
            if (question instanceof ChoiceQuestion) {
                // Compara boolean[] vs boolean[]
                boolean[] val1 = ((MultipleChoiceResponse) r1).getValue();
                boolean[] val2 = ((MultipleChoiceResponse) r2).getValue();
                return calculateJaccardDistance(val1, val2);

            } else if (question instanceof OpenQuestion) {
                // Compara String vs String
                String s1 = ((TextualResponse) r1).getValue();
                String s2 = ((TextualResponse) r2).getValue();
                
                if (((OpenQuestion) question).isNumericOnly()) {
                    // Es numérico, parsear y normalizar
                    return calculateNumericDistance(s1, s2, (OpenQuestion) question);
                } else {
                    // Es texto, usar tu métrica de distancia
                    return calculateTextDistance(s1, s2);
                }
            }
        } catch (Exception e) {
            return 1.0; // Error de casting o parseo
        }
        return 0.0; // Tipo de pregunta no soportado
    }

    /**
     * (PRIVADO) Despachador para calcular la distancia de una respuesta real a un componente de centroide.
     *
     * @param r          La respuesta real del punto de datos.
     * @param cValue     El componente del centroide (puede ser `Double`, `double[]` o `String`).
     * @param question   La pregunta correspondiente, para saber cómo comparar.
     * @return La distancia local (normalizada a [0, 1]) entre la respuesta y el componente del centroide.
     */
    private double calculateLocalToCentroid(Response r, Object cValue, Question question) {
        // Penalización máxima si no hay respuesta o el centroide no tiene ese componente
        if (r == null || !r.isAnswered() || cValue == null) {
            return 1.0; 
        }

        try {
            if (question instanceof ChoiceQuestion) {
                // Compara boolean[] (punto) vs double[] (centroide)
                boolean[] pointVal = ((MultipleChoiceResponse) r).getValue();
                double[] centroidVal = (double[]) cValue; // El centroide K-Means guarda un promedio

                double[] pointValAsDouble = new double[pointVal.length];
                for(int i = 0; i < pointVal.length; i++) {
                    pointValAsDouble[i] = pointVal[i] ? 1.0 : 0.0;
                }
                
                return euclideanDistance(pointValAsDouble, centroidVal);

            } else if (question instanceof OpenQuestion) {
                
                if (((OpenQuestion) question).isNumericOnly()) {
                    // Compara String (punto) vs Double (centroide)
                    String s_point = ((TextualResponse) r).getValue();
                    double d_point = Double.parseDouble(s_point);
                    double d_centroid = (Double) cValue; // El centroide K-Means guarda un promedio

                    OpenQuestion oq = (OpenQuestion) question;
                    double normPoint = normalizeValue(d_point, oq.getMinValue(), oq.getMaxValue());
                    double normCentroid = normalizeValue(d_centroid, oq.getMinValue(), oq.getMaxValue());
                    return Math.abs(normPoint - normCentroid);

                } else {
                    // Compara String (punto) vs String (centroide/medoid de texto)
                    String s_point = ((TextualResponse) r).getValue();
                    String s_centroid_medoid = (String) cValue; 
                    
                    return calculateTextDistance(s_point, s_centroid_medoid);
                }
            }
        } catch (Exception e) {
            return 1.0; // Error de casting o parseo
        }
        return 0.0;
    }

    /**
     * (PRIVADO) Calcula la Distancia Jaccard (1 - Similitud Jaccard) para vectores booleanos.
     *
     * @param v1 El primer vector booleano (opciones seleccionadas).
     * @param v2 El segundo vector booleano (opciones seleccionadas).
     * @return La distancia Jaccard (1 - similitud) en el rango [0, 1].
     */
    private double calculateJaccardDistance(boolean[] v1, boolean[] v2) {
        if (v1.length != v2.length) return 1.0; // No deberían tener longitudes distintas

        int intersection = 0;
        int union = 0;

        for (int i = 0; i < v1.length; i++) {
            if (v1[i] && v2[i]) {
                intersection++;
            }
            if (v1[i] || v2[i]) {
                union++;
            }
        }

        if (union == 0) {
            return 0.0; // Ambos vectores son [0,0,0], son idénticos.
        }

        double similarity = (double) intersection / union;
        return 1.0 - similarity;
    }

    /**
     * (PRIVADO) Distancia Numérica (normalizada) desde dos Strings.
     *
     * @param s1         El primer valor textual (que se parseará a Double).
     * @param s2         El segundo valor textual (que se parseará a Double).
     * @param question   La pregunta, usada para obtener los valores `min` y `max` para la normalización.
     * @return La distancia numérica normalizada en [0, 1].
     */
    private double calculateNumericDistance(String s1, String s2, OpenQuestion question) {
        try {
            double v1 = Double.parseDouble(s1);
            double v2 = Double.parseDouble(s2);
            double min = question.getMinValue();
            double max = question.getMaxValue();

            double norm1 = normalizeValue(v1, min, max);
            double norm2 = normalizeValue(v2, min, max);
            
            return Math.abs(norm1 - norm2);

        } catch (NumberFormatException e) {
            return 1.0; // Error de parseo, máxima penalización
        }
    }

    /**
     * (PRIVADO) Normaliza un valor a un rango [0, 1] dados un min y max.
     *
     * @param value El valor a normalizar.
     * @param min   El valor mínimo del rango.
     * @param max   El valor máximo del rango.
     * @return El valor normalizado y "clamped" (asegurado) en el rango [0, 1].
     */
    private double normalizeValue(double value, double min, double max) {
        if (max - min == 0) return 0.0; // Evita división por cero
        // Asegura que el valor esté dentro de [min, max] antes de normalizar
        double clampedVal = Math.max(min, Math.min(value, max)); 
        return (clampedVal - min) / (max - min);
    }

    /**
     * (PRIVADO) Calcula la distancia euclidiana simple entre dos vectores de double.
     *
     * @param v1 El primer vector de doubles.
     * @param v2 El segundo vector de doubles.
     * @return La distancia euclidiana entre los dos vectores.
     */
    private double euclideanDistance(double[] v1, double[] v2) {
        if (v1.length != v2.length) return 1.0; // Debería normalizarse, pero por seguridad
        
        double sumSq = 0.0;
        for (int i = 0; i < v1.length; i++) {
            sumSq += Math.pow(v1[i] - v2[i], 2);
        }
        return Math.sqrt(sumSq);
    }

    /**
     * (PRIVADO) Distancia para preguntas de texto libre (tu implementación).
     *
     * @param text1 El primer string.
     * @param text2 El segundo string.
     * @return La distancia de texto normalizada en [0, 1].
     */
    private double calculateTextDistance(String text1, String text2) {
        if (text1 == null || text2 == null) {
             // Si uno es nulo y el otro no, la distancia es máxima (1.0)
             return (text1 == text2) ? 0.0 : 1.0;
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
        
        // Añadir la diferencia de longitud
        charDiff += (maxLen - minLen);

        // Normaliza la diferencia de caracteres a [0, 1]
        return (double) charDiff / maxLen;
    }
}