package domain.clustering;

import domain.model.Response;
import domain.model.Question;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Calculadora de métricas de calidad para análisis de clustering.
 * Proporciona cálculo de coeficiente de silueta y Davies-Bouldin.
 */
public class QualityMetricCalculator {

    /**
     * Calculates the Silhouette Coefficient for a clustering analysis.
     * The Silhouette Coefficient is a measure of how similar an object is to its own cluster (cohesion)
     * compared to other clusters (separation).
     *
     * @param analysis   The ClusteringAnalysis object containing clusters, responses, and questions.
     * @param distance   The DistanceCalculator to use for distance computations.
     * @return The average Silhouette Coefficient across all responses, or 0.0 if calculation is not possible.
     */
    public Double calculateSilhouette(ClusteringAnalysis analysis, DistanceCalculator distance) {
        List<Cluster> clusters = analysis.getClusters();
        List<Response> allResponses = analysis.getResponses();
        List<Question> questions = analysis.getQuestions();

        if (clusters.size() <= 1 || allResponses.isEmpty()) {
            return 0.0; // Silhouette is not defined for 1 or 0 clusters, or no responses
        }

        double totalSilhouette = 0.0;
        int responseCount = 0;

        // Map response IDs to their actual Response objects for quick lookup
        Map<String, Response> responseMap = allResponses.stream()
                .collect(Collectors.toMap(Response::getRESPONSE_ID, r -> r));

        // Map response IDs to their cluster for quick lookup
        Map<String, Cluster> responseToClusterMap = clusters.stream()
                .flatMap(cluster -> cluster.getMembers().stream()
                        .map(member -> Map.entry(member.getResponseId(), cluster)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));


        for (Response response : allResponses) {
            Cluster ownCluster = responseToClusterMap.get(response.getRESPONSE_ID());
            if (ownCluster == null) continue; // Should not happen if response is part of analysis

            // a(i): average distance to all other points in the same cluster
            double ai = calculateCohesion(response, ownCluster, distance, questions, responseMap);

            // b(i): minimum average distance to points in any other cluster
            double bi = calculateSeparation(response, clusters, ownCluster, distance, questions, responseMap);

            if (ai == 0.0 && bi == 0.0) {
                // This can happen if a cluster has only one member. Silhouette is 0.
                // Or if a response is isolated and cannot be compared.
                continue;
            }

            double silhouette = (bi - ai) / Math.max(ai, bi);
            totalSilhouette += silhouette;
            responseCount++;
        }

        return responseCount > 0 ? totalSilhouette / responseCount : 0.0;
    }

    /**
     * Calculates the Silhouette Coefficient for a single cluster.
     *
     * @param cluster    The cluster for which to calculate the score.
     * @param analysis   The ClusteringAnalysis object.
     * @param distance   The DistanceCalculator.
     * @return The average Silhouette score for the cluster.
     */
    public Double calculateSilhouetteForCluster(Cluster cluster, ClusteringAnalysis analysis, DistanceCalculator distance) {
        List<Cluster> allClusters = analysis.getClusters();
        List<Response> allResponses = analysis.getResponses();
        List<Question> questions = analysis.getQuestions();

        if (allClusters.size() <= 1 || cluster.getMembers().isEmpty()) {
            return 0.0;
        }

        double totalSilhouette = 0.0;
        int responseCount = 0;

        Map<String, Response> responseMap = allResponses.stream()
                .collect(Collectors.toMap(Response::getRESPONSE_ID, r -> r));

        for (ClusterMembership member : cluster.getMembers()) {
            Response response = responseMap.get(member.getResponseId());
            if (response == null) continue;

            double ai = calculateCohesion(response, cluster, distance, questions, responseMap);
            double bi = calculateSeparation(response, allClusters, cluster, distance, questions, responseMap);

            if (ai == 0.0 && bi == 0.0) {
                continue;
            }

            double silhouette = (bi - ai) / Math.max(ai, bi);
            totalSilhouette += silhouette;
            responseCount++;
        }

        return responseCount > 0 ? totalSilhouette / responseCount : 0.0;
    }

    /**
     * Calculates the Calinski-Harabasz Index for a clustering analysis.
     * This index is a ratio of the between-cluster dispersion to the within-cluster dispersion.
     * Higher values generally indicate better clustering.
     *
     * @param analysis   The ClusteringAnalysis object.
     * @param distance   The DistanceCalculator.
     * @return The Calinski-Harabasz Index, or 0.0 if calculation is not possible.
     */
    public Double calculateCalinskiHarabasz(ClusteringAnalysis analysis, DistanceCalculator distance) {
        List<Cluster> clusters = analysis.getClusters();
        List<Response> allResponses = analysis.getResponses();
        List<Question> questions = analysis.getQuestions();
        int k = clusters.size();
        int N = allResponses.size();

        if (k <= 1 || N <= k) {
            return 0.0; // Index not defined for 1 or 0 clusters, or N <= k
        }

        // 1. Calculate the overall centroid of all responses
        Centroid overallCentroid = calculateOverallCentroid(allResponses, questions, distance);

        // 2. Calculate SS_B (Between-cluster dispersion)
        double ssB = 0.0;
        for (Cluster cluster : clusters) {
            int clusterSize = cluster.getSize();
            if (clusterSize > 0) {
                double dist = distance.calculate(cluster.getCentroid(), overallCentroid, questions);
                ssB += clusterSize * Math.pow(dist, 2);
            }
        }

        // 3. Calculate SS_W (Within-cluster dispersion)
        double ssW = 0.0;
        for (Cluster cluster : clusters) {
            Centroid clusterCentroid = cluster.getCentroid();
            for (ClusterMembership member : cluster.getMembers()) {
                Response response = allResponses.stream()
                        .filter(r -> r.getRESPONSE_ID().equals(member.getResponseId()))
                        .findFirst().orElse(null);
                if (response != null) {
                    double dist = distance.calculateToCentroid(response, clusterCentroid, questions);
                    ssW += Math.pow(dist, 2);
                }
            }
        }

        if (ssW == 0.0) { // Avoid division by zero, implies perfect clustering (all points are at their centroids)
            return Double.MAX_VALUE;
        }

        return (ssB / (k - 1)) / (ssW / (N - k));
    }

    /**
     * Calculates the Davies-Bouldin Index for a clustering analysis.
     * This index is a ratio of within-cluster scatter to between-cluster separation.
     * Lower values generally indicate better clustering.
     *
     * @param analysis   The ClusteringAnalysis object.
     * @param distance   The DistanceCalculator.
     * @return The Davies-Bouldin Index, or 0.0 if calculation is not possible.
     */
    public Double calculateDaviesBouldin(ClusteringAnalysis analysis, DistanceCalculator distance) {
        List<Cluster> clusters = analysis.getClusters();
        List<Response> allResponses = analysis.getResponses();
        List<Question> questions = analysis.getQuestions();
        int k = clusters.size();

        if (k <= 1) {
            return 0.0; // Index not defined for 1 or 0 clusters
        }

        // 1. Calculate s_i (average distance from each point in cluster i to its centroid) for all clusters
        double[] s = new double[k];
        for (int i = 0; i < k; i++) {
            Cluster cluster = clusters.get(i);
            Centroid centroid = cluster.getCentroid();
            double sumDistances = 0.0;
            int memberCount = 0;
            for (ClusterMembership member : cluster.getMembers()) {
                Response response = allResponses.stream()
                        .filter(r -> r.getRESPONSE_ID().equals(member.getResponseId()))
                        .findFirst().orElse(null);
                if (response != null) {
                    sumDistances += distance.calculateToCentroid(response, centroid, questions);
                    memberCount++;
                }
            }
            s[i] = memberCount > 0 ? sumDistances / memberCount : 0.0;
        }

        // 2. Calculate R_ij and D_i
        double totalDi = 0.0;
        for (int i = 0; i < k; i++) {
            double maxRij = 0.0;
            for (int j = 0; j < k; j++) {
                if (i == j) continue;

                Centroid ci = clusters.get(i).getCentroid();
                Centroid cj = clusters.get(j).getCentroid();

                double distBetweenCentroids = distance.calculate(ci, cj, questions);

                if (distBetweenCentroids == 0.0) { // Avoid division by zero
                    maxRij = Double.MAX_VALUE; // This cluster pair is problematic
                    break;
                }

                double Rij = (s[i] + s[j]) / distBetweenCentroids;
                if (Rij > maxRij) {
                    maxRij = Rij;
                }
            }
            totalDi += maxRij;
        }

        return k > 0 ? totalDi / k : 0.0;
    }

    /**
     * Provides a textual interpretation of a given quality score based on its type.
     *
     * @param metricType The type of quality metric.
     * @param score      The calculated score.
     * @return A string interpretation of the score.
     */
    public String interpretScore(QualityMetricType metricType, Double score) {
        if (score == null || score.isNaN()) {
            return "Score is undefined.";
        }

        switch (metricType) {
            case SILHOUETTE:
                if (score > 0.7) return "Strong clustering structure.";
                if (score > 0.5) return "Reasonable clustering structure.";
                if (score > 0.2) return "Weak clustering structure, could be artificial.";
                if (score > 0.0) return "No substantial structure, or overlapping clusters.";
                return "Objects are probably assigned to the wrong clusters.";
            case CALINSKI_HARABASZ:
                return "Higher Calinski-Harabasz score indicates better defined clusters.";
            case DAVIES_BOULDIN:
                if (score < 1.0) return "Good clustering, clusters are compact and well-separated.";
                return "Higher Davies-Bouldin score indicates worse clustering.";
            default:
                return "Score: " + score;
        }
    }

    /**
     * Calculates the cohesion (a(i)) for a single response within its assigned cluster.
     * This is the average distance from the response to all other responses in the same cluster.
     *
     * @param response      The response for which to calculate cohesion.
     * @param ownCluster    The cluster to which the response belongs.
     * @param distance      The DistanceCalculator.
     * @param questions     The list of questions.
     * @param responseMap   A map of all responses by ID for efficient lookup.
     * @return The cohesion value (a(i)), or 0.0 if the cluster has only one member.
     */
    private Double calculateCohesion(Response response, Cluster ownCluster, DistanceCalculator distance, List<Question> questions, Map<String, Response> responseMap) {
        List<ClusterMembership> members = ownCluster.getMembers();
        if (members.size() <= 1) {
            return 0.0; // If only one member, cohesion is 0
        }

        double sumDistances = 0.0;
        int count = 0;
        for (ClusterMembership member : members) {
            if (!member.getResponseId().equals(response.getRESPONSE_ID())) {
                Response otherResponse = responseMap.get(member.getResponseId());
                if (otherResponse != null) {
                    sumDistances += distance.calculate(response, otherResponse, questions);
                    count++;
                }
            }
        }
        return count > 0 ? sumDistances / count : 0.0;
    }

    /**
     * Calculates the separation (b(i)) for a single response from other clusters.
     * This is the minimum average distance from the response to all responses in any other cluster.
     *
     * @param response          The response for which to calculate separation.
     * @param allClusters       All clusters in the analysis.
     * @param responseCluster   The cluster to which the response belongs.
     * @param distance          The DistanceCalculator.
     * @param questions         The list of questions.
     * @param responseMap       A map of all responses by ID for efficient lookup.
     * @return The separation value (b(i)), or 0.0 if no other clusters exist.
     */
    private Double calculateSeparation(Response response, List<Cluster> allClusters, Cluster responseCluster, DistanceCalculator distance, List<Question> questions, Map<String, Response> responseMap) {
        double minAvgDistance = Double.MAX_VALUE;
        boolean foundOtherCluster = false;

        for (Cluster otherCluster : allClusters) {
            if (!otherCluster.getId().equals(responseCluster.getId())) {
                foundOtherCluster = true;
                double sumDistances = 0.0;
                int count = 0;
                for (ClusterMembership member : otherCluster.getMembers()) {
                    Response otherResponse = responseMap.get(member.getResponseId());
                    if (otherResponse != null) {
                        sumDistances += distance.calculate(response, otherResponse, questions);
                        count++;
                    }
                }
                if (count > 0) {
                    minAvgDistance = Math.min(minAvgDistance, sumDistances / count);
                }
            }
        }
        return foundOtherCluster ? minAvgDistance : 0.0;
    }

    /**
     * Calculates the overall centroid of all responses in the analysis.
     * This is used for metrics like Calinski-Harabasz.
     *
     * @param allResponses The list of all responses.
     * @param questions    The list of questions.
     * @param distance     The DistanceCalculator (needed for textual medoid calculation).
     * @return A Centroid representing the average of all responses.
     */
    private Centroid calculateOverallCentroid(List<Response> allResponses, List<Question> questions, DistanceCalculator distance) {
        int numQuestions = questions.size();
        Centroid overallCentroid = new Centroid(numQuestions);

        List<List<Double>> choiceSums = new ArrayList<>(numQuestions);
        List<List<String>> textValues = new ArrayList<>(numQuestions);
        int[] responseCountsPerQuestion = new int[numQuestions];

        for (int i = 0; i < numQuestions; i++) {
            choiceSums.add(new ArrayList<>());
            textValues.add(new ArrayList<>());
        }

        for (Response response : allResponses) {
            for (int qIdx = 0; qIdx < numQuestions; qIdx++) {
                Question q = questions.get(qIdx);
                domain.model.Answer a = response.getAnswer(q.getQuestionIndex());
                if (KMeansPlusPlus.isAnswered(a)) {
                    Object value = KMeansPlusPlus.getAnswerValue(a, q);
                    if (value instanceof double[]) {
                        double[] val = (double[]) value;
                        if (choiceSums.get(qIdx).isEmpty()) {
                            for (double v : val) choiceSums.get(qIdx).add(v);
                        } else {
                            for (int optIdx = 0; optIdx < val.length; optIdx++) {
                                choiceSums.get(qIdx).set(optIdx, choiceSums.get(qIdx).get(optIdx) + val[optIdx]);
                            }
                        }
                        responseCountsPerQuestion[qIdx]++;
                    } else if (value instanceof String) {
                        textValues.get(qIdx).add((String) value);
                        responseCountsPerQuestion[qIdx]++;
                    }
                }
            }
        }

        for (int qIdx = 0; qIdx < numQuestions; qIdx++) {
            Question q = questions.get(qIdx);
            if (q instanceof domain.model.MultipleChoiceQuestion) {
                if (responseCountsPerQuestion[qIdx] > 0) {
                    double[] avgOptions = new double[choiceSums.get(qIdx).size()];
                    for (int optIdx = 0; optIdx < choiceSums.get(qIdx).size(); optIdx++) {
                        avgOptions[optIdx] = choiceSums.get(qIdx).get(optIdx) / responseCountsPerQuestion[qIdx];
                    }
                    overallCentroid.setComponent(qIdx, avgOptions);
                }
            } else if (q.getTypeQuestion() == domain.model.enums.TypeQuestion.TEXTUAL) {
                if (!textValues.get(qIdx).isEmpty()) {
                    String medoidText = findTextMedoid(textValues.get(qIdx), distance);
                    overallCentroid.setComponent(qIdx, medoidText);
                }
            }
        }
        return overallCentroid;
    }

    // Helper method to find text medoid (copied from KMeans)
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
}