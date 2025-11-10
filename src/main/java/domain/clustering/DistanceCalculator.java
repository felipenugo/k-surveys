package domain.clustering;

import domain.model.Answer;
import domain.model.MultipleChoiceAnswer;
import domain.model.MultipleChoiceQuestion;
import domain.model.Question;
import domain.model.Response;
import domain.model.TextualAnswer;
import domain.model.enums.TypeQuestion;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Calculates the distance (similarity) between two Responses or
 * between a Response and a Centroid.
 */
public class DistanceCalculator {

    private DistanceType distanceType;
    private Map<Integer, Double> weights; // Use Question Index as key

    public DistanceCalculator(DistanceType distanceType) {
        this.distanceType = distanceType;
        this.weights = new HashMap<>();
    }

    public void setWeight(int questionIndex, double weight) {
        this.weights.put(questionIndex, weight);
    }

    public Map<Integer, Double> getWeights() {
        return new HashMap<>(this.weights);
    }

    public DistanceType getDistanceType() {
        return this.distanceType;
    }

    public double calculate(Response rs1, Response rs2, List<Question> questions) {
        double totalDistanceSquared = 0.0;
        double totalDistanceManhattan = 0.0;

        for (Question question : questions) {
            int qIdx = question.getQuestionIndex();
            Answer r1 = rs1.getAnswer(qIdx);
            Answer r2 = rs2.getAnswer(qIdx);
            double weight = this.weights.getOrDefault(qIdx, 1.0);

            double localDist = calculateLocal(r1, r2, question);

            if (this.distanceType == DistanceType.MANHATTAN) {
                totalDistanceManhattan += (localDist * weight);
            } else {
                totalDistanceSquared += Math.pow(localDist * weight, 2);
            }
        }

        if (this.distanceType == DistanceType.MANHATTAN) {
            return totalDistanceManhattan;
        }
        return Math.sqrt(totalDistanceSquared); // EUCLIDEAN by default
    }

    public double calculateToCentroid(Response rs, Centroid centroid, List<Question> questions) {
        double totalDistanceSquared = 0.0;
        double totalDistanceManhattan = 0.0;

        for (Question question : questions) {
            int qIdx = question.getQuestionIndex();
            Answer r = rs.getAnswer(qIdx);
            Object cValue = centroid.getComponent(qIdx);
            double weight = this.weights.getOrDefault(qIdx, 1.0);

            double localDist = calculateLocalToCentroid(r, cValue, question);

            if (this.distanceType == DistanceType.MANHATTAN) {
                totalDistanceManhattan += (localDist * weight);
            } else {
                totalDistanceSquared += Math.pow(localDist * weight, 2);
            }
        }

        if (this.distanceType == DistanceType.MANHATTAN) {
            return totalDistanceManhattan;
        }
        return Math.sqrt(totalDistanceSquared); // EUCLIDEAN by default
    }

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
        return false;
    }

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
            }
        } catch (Exception e) {
            return 1.0; // Casting error
        }
        return 0.0; // Unsupported question type
    }

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
            }
        } catch (Exception e) {
            return 1.0; // Casting error
        }
        return 0.0;
    }

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

    public double euclideanDistance(double[] v1, double[] v2) {
        if (v1.length != v2.length) return 1.0;

        double sumSq = 0.0;
        for (int i = 0; i < v1.length; i++) {
            sumSq += Math.pow(v1[i] - v2[i], 2);
        }
        return Math.sqrt(sumSq);
    }

    public double calculateTextDistance(String text1, String text2) {
        if (text1 == null || text2 == null) {
            return (text1 == text2) ? 0.0 : 1.0;
        }
        if (text1.equals(text2)) {
            return 0.0;
        }

        int maxLen = Math.max(text1.length(), text2.length());
        if (maxLen == 0) return 0.0;

        return (double) levenshtein(text1, text2) / maxLen;
    }

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
        for (int i = 0; i < vector1.length; i++) {
            if (!(vector1[i] instanceof Number) || !(vector2[i] instanceof Number)) {
                throw new IllegalArgumentException("Los elementos del vector en posición " + i + " deben ser numéricos (Number)");
            }
            double val1 = ((Number) vector1[i]).doubleValue();
            double val2 = ((Number) vector2[i]).doubleValue();
            double diff = val1 - val2;
            if (this.distanceType == DistanceType.MANHATTAN) {
                sum += Math.abs(diff);
            } else {
                sum += diff * diff;
            }
        }
        if (this.distanceType == DistanceType.EUCLIDEAN) {
            return Math.sqrt(sum);
        }
        return sum;
    }

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
