package domain.clustering;

import domain.model.*;
import domain.model.enums.TypeQuestion;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Calculadora de distancias entre respuestas y centroides.
 * Soporta múltiples métricas de distancia y tipos de datos (numéricos y textuales).
 */
public class DistanceCalculator {

    private DistanceType distanceType;
    private TextDistanceType textDistanceType;
    private Map<Integer, Double> weights;
    private static EmbeddingModel embeddingModel;

    private static EmbeddingModel getEmbeddingModel() {
        if (embeddingModel == null) {
            embeddingModel = new AllMiniLmL6V2EmbeddingModel();
        }
        return embeddingModel;
    }

    /**
     * Constructor que establece el tipo de distancia.
     *
     * @param distanceType Métrica de distancia a utilizar
     * @param textDistanceType Métrica de distancia textual a utilizar
     */
    public DistanceCalculator(DistanceType distanceType, TextDistanceType textDistanceType) {
        this.distanceType = distanceType;
        this.textDistanceType = textDistanceType;
        this.weights = new HashMap<>();
    }

    /**
     * Establece el peso de una pregunta específica en el cálculo de distancia.
     *
     * @param questionIndex Índice de la pregunta
     * @param weight Peso a aplicar (mayor peso = mayor importancia)
     */
    public void setWeight(int questionIndex, double weight) {
        this.weights.put(questionIndex, weight);
    }

    /**
     * Obtiene todos los pesos configurados.
     *
     * @return Mapa de índices de pregunta a pesos
     */
    public Map<Integer, Double> getWeights() {
        return new HashMap<>(this.weights);
    }

    /**
     * Obtiene el tipo de distancia configurado.
     *
     * @return Tipo de distancia actual
     */
    public DistanceType getDistanceType() {
        return this.distanceType;
    }

    /**
     * Obtiene el tipo de distancia textual configurado.
     *
     * @return Tipo de distancia textual actual
     */
    public TextDistanceType getTextDistanceType() {
        return this.textDistanceType;
    }

    /**
     * Calcula la distancia entre dos respuestas.
     *
     * @param rs1 Primera respuesta
     * @param rs2 Segunda respuesta
     * @param questions Lista de preguntas para interpretar las respuestas
     * @return Distancia calculada
     */
    public double calculate(Response rs1, Response rs2, List<Question> questions) {
        double totalDistanceSquared = 0.0;
        double totalDistanceManhattan = 0.0;
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (Question question : questions) {
            int qIdx = question.getQuestionIndex();
            Answer r1 = rs1.getAnswer(qIdx);
            Answer r2 = rs2.getAnswer(qIdx);
            double weight = this.weights.getOrDefault(qIdx, 1.0);

            double localDist = calculateLocal(r1, r2, question);

            if (this.distanceType == DistanceType.MANHATTAN) {
                totalDistanceManhattan += (localDist * weight);
            } else if (this.distanceType == DistanceType.COSINE) {
                double v1 = 1.0 - localDist;
                double v2 = 1.0;
                dotProduct += v1 * v2 * weight;
                norm1 += v1 * v1 * weight;
                norm2 += v2 * v2 * weight;
            } else {
                totalDistanceSquared += Math.pow(localDist * weight, 2);
            }
        }

        if (this.distanceType == DistanceType.MANHATTAN) {
            return totalDistanceManhattan;
        }
        if (this.distanceType == DistanceType.COSINE) {
            if (norm1 == 0.0 || norm2 == 0.0) return 1.0;
            double cosineSimilarity = dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
            return 1.0 - cosineSimilarity;
        }
        return Math.sqrt(totalDistanceSquared); // EUCLIDEAN by default
    }

    /**
     * Calcula la distancia entre una respuesta y un centroide.
     *
     * @param rs Respuesta
     * @param centroid Centroide
     * @param questions Lista de preguntas
     * @return Distancia calculada
     */
    public double calculateToCentroid(Response rs, Centroid centroid, List<Question> questions) {
        double totalDistanceSquared = 0.0;
        double totalDistanceManhattan = 0.0;
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (Question question : questions) {
            int qIdx = question.getQuestionIndex();
            Answer r = rs.getAnswer(qIdx);
            Object cValue = centroid.getComponent(qIdx);
            double weight = this.weights.getOrDefault(qIdx, 1.0);

            double localDist = calculateLocalToCentroid(r, cValue, question);

            if (this.distanceType == DistanceType.MANHATTAN) {
                totalDistanceManhattan += (localDist * weight);
            } else if (this.distanceType == DistanceType.COSINE) {
                double v1 = 1.0 - localDist;
                double v2 = 1.0;
                dotProduct += v1 * v2 * weight;
                norm1 += v1 * v1 * weight;
                norm2 += v2 * v2 * weight;
            } else {
                totalDistanceSquared += Math.pow(localDist * weight, 2);
            }
        }

        if (this.distanceType == DistanceType.MANHATTAN) {
            return totalDistanceManhattan;
        }
        if (this.distanceType == DistanceType.COSINE) {
            if (norm1 == 0.0 || norm2 == 0.0) return 1.0;
            double cosineSimilarity = dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
            return 1.0 - cosineSimilarity;
        }
        return Math.sqrt(totalDistanceSquared); // EUCLIDEAN by default
    }

    /**
     * Verifica si una respuesta ha sido contestada.
     *
     * @param r Respuesta a verificar
     * @return true si la respuesta contiene datos válidos
     */
    private boolean isAnswered(Answer r) {
        if (r == null) return false;
        if (r instanceof MultipleChoiceAnswer) {
            for (boolean b : ((MultipleChoiceAnswer) r).getSelectedOptions()) {
                if (b) return true;
            }
            return false;
        }
        if (r instanceof TextualAnswer) {
            String text = ((TextualAnswer) r).getAnswerText();
            return text != null && !text.isEmpty();
        }
        if (r instanceof NumericalAnswer) {
            return ((NumericalAnswer) r).getAnswerNum() != null;
        }
        return false;
    }

    /**
     * Calcula la distancia local entre dos respuestas para una pregunta.
     *
     * @param r1 Primera respuesta
     * @param r2 Segunda respuesta
     * @param question Pregunta asociada
     * @return Distancia local
     */
    private double calculateLocal(Answer r1, Answer r2, Question question) {
        if (!isAnswered(r1) || !isAnswered(r2)) {
            return 1.0;
        }

        try {
            if (question instanceof MultipleChoiceQuestion) {
                boolean[] val1 = ((MultipleChoiceAnswer) r1).getSelectedOptions();
                boolean[] val2 = ((MultipleChoiceAnswer) r2).getSelectedOptions();
                return calculateJaccardDistance(val1, val2);

            } else if (question.getTypeQuestion() == TypeQuestion.TEXTUAL) {
                String s1 = ((TextualAnswer) r1).getAnswerText();
                String s2 = ((TextualAnswer) r2).getAnswerText();
                return calculateTextDistance(s1, s2);
            } else if (question.getTypeQuestion() == TypeQuestion.NUMERICAL) {
                Double v1 = ((NumericalAnswer) r1).getAnswerNum();
                Double v2 = ((NumericalAnswer) r2).getAnswerNum();
                // TODO: This distance is not normalized. For better results, normalize this value
                // to the 0-1 range based on the min/max values of all answers for this question.
                return Math.abs(v1 - v2);
            }
        } catch (Exception e) {
            return 1.0; // Casting error
        }
        return 0.0; // Unsupported question type
    }

    /**
     * Calcula la distancia local entre una respuesta y un componente de centroide.
     *
     * @param r Respuesta
     * @param cValue Valor del componente del centroide
     * @param question Pregunta asociada
     * @return Distancia local
     */
    private double calculateLocalToCentroid(Answer r, Object cValue, Question question) {
        if (!isAnswered(r) || cValue == null) {
            return 1.0;
        }

        try {
            if (question instanceof MultipleChoiceQuestion) {
                boolean[] pointVal = ((MultipleChoiceAnswer) r).getSelectedOptions();
                double[] centroidVal = (double[]) cValue;

                double[] pointValAsDouble = new double[pointVal.length];
                for (int i = 0; i < pointVal.length; i++) {
                    pointValAsDouble[i] = pointVal[i] ? 1.0 : 0.0;
                }

                return euclideanDistance(pointValAsDouble, centroidVal);

            } else if (question.getTypeQuestion() == TypeQuestion.TEXTUAL) {
                String s_point = ((TextualAnswer) r).getAnswerText();
                String s_centroid_medoid = (String) cValue;
                return calculateTextDistance(s_point, s_centroid_medoid);
            } else if (question.getTypeQuestion() == TypeQuestion.NUMERICAL) {
                double pointVal = ((NumericalAnswer) r).getAnswerNum();
                double centroidVal = (Double) cValue;
                return Math.abs(pointVal - centroidVal); // Simple absolute difference
            }
        } catch (Exception e) {
            return 1.0; // Casting error
        }
        return 0.0;
    }

    /**
     * Calcula la distancia de Jaccard entre dos vectores booleanos.
     *
     * @param v1 Primer vector
     * @param v2 Segundo vector
     * @return Distancia de Jaccard (1 - similitud)
     */
    private double calculateJaccardDistance(boolean[] v1, boolean[] v2) {
        if (v1.length != v2.length) return 1.0;

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
            return 0.0;
        }

        double similarity = (double) intersection / union;
        return 1.0 - similarity;
    }

    /**
     * Calcula la distancia euclídea entre dos vectores numéricos.
     *
     * @param v1 Primer vector
     * @param v2 Segundo vector
     * @return Distancia euclídea
     */
    public double euclideanDistance(double[] v1, double[] v2) {
        if (v1.length != v2.length) return 1.0;

        double sumSq = 0.0;
        for (int i = 0; i < v1.length; i++) {
            sumSq += Math.pow(v1[i] - v2[i], 2);
        }
        return Math.sqrt(sumSq);
    }

    /**
     * Calcula la distancia entre dos textos usando Levenshtein normalizado.
     *
     * @param text1 Primer texto
     * @param text2 Segundo texto
     * @return Distancia textual normalizada (0 = idénticos, 1 = completamente diferentes)
     */
    public double calculateTextDistance(String text1, String text2) {
        if (text1 == null || text2 == null) {
            return (text1 == text2) ? 0.0 : 1.0;
        }
        if (text1.equals(text2)) {
            return 0.0;
        }

        if (this.textDistanceType == TextDistanceType.LEVENSHTEIN) {
            int maxLen = Math.max(text1.length(), text2.length());
            if (maxLen == 0) return 0.0;
            return (double) levenshtein(text1, text2) / maxLen;
        } else if (this.textDistanceType == TextDistanceType.EMBEDDING) {
            Embedding embedding1 = getEmbeddingModel().embed(text1).content();
            Embedding embedding2 = getEmbeddingModel().embed(text2).content();

            float[] v1 = embedding1.vector();
            float[] v2 = embedding2.vector();

            double dotProduct = 0.0;
            double normA = 0.0;
            double normB = 0.0;
            for (int i = 0; i < v1.length; i++) {
                dotProduct += v1[i] * v2[i];
                normA += v1[i] * v1[i];
                normB += v2[i] * v2[i];
            }

            if (normA == 0 || normB == 0) {
                return 1.0; // Cannot compute similarity if one vector is all zeros
            }

            double cosineSimilarity = dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
            
            // Convert cosine similarity to a distance measure (0.0 - 1.0)
            return (1.0 - cosineSimilarity) / 2.0;
        } else {
            throw new UnsupportedOperationException("Tipo de distancia textual no soportado: " + this.textDistanceType);
        }
    }

    /**
     * Calcula la distancia de edición de Levenshtein entre dos cadenas.
     *
     * @param s1 Primera cadena
     * @param s2 Segunda cadena
     * @return Número mínimo de ediciones necesarias
     */
    private int levenshtein(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = Math.min(dp[i - 1][j - 1] + (s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1),
                                    Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1));
                }
            }
        }
        return dp[s1.length()][s2.length()];
    }

    /**
     * Calcula la distancia entre dos vectores de objetos numéricos.
     *
     * @param vector1 Primer vector
     * @param vector2 Segundo vector
     * @return Distancia calculada según el tipo configurado
     * @throws IllegalArgumentException si los vectores no son válidos
     */
    public double calculateVectorDistance(Object[] vector1, Object[] vector2) {
        if (vector1 == null || vector2 == null) {
            throw new IllegalArgumentException("Los vectores no pueden ser null");
        }
        if (vector1.length != vector2.length) {
            throw new IllegalArgumentException("Los vectores deben tener la misma longitud: " + vector1.length + " vs " + vector2.length);
        }
        if (vector1.length == 0) {
            return 0.0;
        }
        double sum = 0.0;
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        for (int i = 0; i < vector1.length; i++) {
            if (!(vector1[i] instanceof Number) || !(vector2[i] instanceof Number)) {
                throw new IllegalArgumentException("Los elementos del vector en posición " + i + " deben ser numéricos (Number)");
            }
            double val1 = ((Number) vector1[i]).doubleValue();
            double val2 = ((Number) vector2[i]).doubleValue();
            double diff = val1 - val2;
            if (this.distanceType == DistanceType.MANHATTAN) {
                sum += Math.abs(diff);
            } else if (this.distanceType == DistanceType.COSINE) {
                dotProduct += val1 * val2;
                norm1 += val1 * val1;
                norm2 += val2 * val2;
            } else {
                sum += diff * diff;
            }
        }
        if (this.distanceType == DistanceType.MANHATTAN) {
            return sum;
        }
        if (this.distanceType == DistanceType.COSINE) {
            if (norm1 == 0.0 || norm2 == 0.0) return 1.0;
            double cosineSimilarity = dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
            return 1.0 - cosineSimilarity;
        }
        return Math.sqrt(sum); // EUCLIDEAN by default
    }

    /**
     * Calcula la distancia entre dos vectores usando un tipo de distancia específico.
     *
     * @param vector1 Primer vector
     * @param vector2 Segundo vector
     * @param type Tipo de distancia a utilizar
     * @return Distancia calculada
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

    /**
     * Calculates the distance between two Centroid objects.
     *
     * @param c1        The first Centroid.
     * @param c2        The second Centroid.
     * @param questions The list of questions, used to interpret centroid components.
     * @return The calculated distance.
     */
    public double calculate(Centroid c1, Centroid c2, List<Question> questions) {
        double totalDistanceSquared = 0.0;
        double totalDistanceManhattan = 0.0;
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < questions.size(); i++) {
            Question question = questions.get(i);
            Object comp1 = c1.getComponent(i);
            Object comp2 = c2.getComponent(i);
            double weight = this.weights.getOrDefault(question.getQuestionIndex(), 1.0); // Use question index for weight

            double localDist = 0.0;

            // Handle null components (missing data)
            if (comp1 == null || comp2 == null) {
                if (comp1 != comp2) { // One is null, the other is not
                    localDist = 1.0; // Max distance for incomparable components
                } else { // Both are null
                    localDist = 0.0;
                }
            } else {
                // Calculate local distance based on component type
                if (comp1 instanceof double[] && comp2 instanceof double[]) { // MultipleChoiceQuestion
                    localDist = euclideanDistance((double[]) comp1, (double[]) comp2);
                } else if (comp1 instanceof String && comp2 instanceof String) { // TextualQuestion
                    localDist = calculateTextDistance((String) comp1, (String) comp2);
                } else if (comp1 instanceof Double && comp2 instanceof Double) { // NumericalQuestion
                    localDist = Math.abs((Double) comp1 - (Double) comp2);
                } else {
                    // Fallback for unexpected types or mixed types, treat as max distance
                    localDist = 1.0;
                }
            }

            if (this.distanceType == DistanceType.MANHATTAN) {
                totalDistanceManhattan += (localDist * weight);
            } else if (this.distanceType == DistanceType.COSINE) {
                double v1 = 1.0 - localDist;
                double v2 = 1.0;
                dotProduct += v1 * v2 * weight;
                norm1 += v1 * v1 * weight;
                norm2 += v2 * v2 * weight;
            } else {
                totalDistanceSquared += Math.pow(localDist * weight, 2);
            }
        }

        if (this.distanceType == DistanceType.MANHATTAN) {
            return totalDistanceManhattan;
        }
        if (this.distanceType == DistanceType.COSINE) {
            if (norm1 == 0.0 || norm2 == 0.0) return 1.0;
            double cosineSimilarity = dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
            return 1.0 - cosineSimilarity;
        }
        return Math.sqrt(totalDistanceSquared); // EUCLIDEAN by default
    }
}
