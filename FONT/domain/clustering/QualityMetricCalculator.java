package domain.clustering;

import domain.model.Response;
import domain.model.Question;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class QualityMetricCalculator {
        public Double calculateSilhouette(ClusteringAnalysis analysis, DistanceCalculator distance) {
            List<Cluster> clusters = analysis.getClusters();
            List<Response> allResponses = analysis.getResponses();
            List<Question> questions = analysis.getQuestions();

            if (clusters.size() <= 1 || allResponses.isEmpty()) return 0.0;
            double totalSilhouette = 0.0; int responseCount = 0;
            Map<String, Response> responseMap = allResponses.stream().collect(Collectors.toMap(Response::getRESPONSE_ID, r -> r));
            Map<String, Cluster> responseToClusterMap = clusters.stream().flatMap(cluster -> cluster.getMembers().stream().map(member -> Map.entry(member.getResponseId(), cluster))).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            for (Response response : allResponses) {
                Cluster ownCluster = responseToClusterMap.get(response.getRESPONSE_ID()); if (ownCluster == null) continue;
                double ai = calculateCohesion(response, ownCluster, distance, questions, responseMap);
                double bi = calculateSeparation(response, clusters, ownCluster, distance, questions, responseMap);
                if (ai == 0.0 && bi == 0.0) continue;
                double silhouette = (bi - ai) / Math.max(ai, bi);
                totalSilhouette += silhouette; responseCount++;
            }
            return responseCount > 0 ? totalSilhouette / responseCount : 0.0;
        }

        public Double calculateSilhouetteForCluster(Cluster cluster, ClusteringAnalysis analysis, DistanceCalculator distance) {
            List<Cluster> allClusters = analysis.getClusters(); List<Response> allResponses = analysis.getResponses(); List<Question> questions = analysis.getQuestions();
            if (allClusters.size() <= 1 || cluster.getMembers().isEmpty()) return 0.0;
            double totalSilhouette = 0.0; int responseCount = 0;
            Map<String, Response> responseMap = allResponses.stream().collect(Collectors.toMap(Response::getRESPONSE_ID, r -> r));
            for (ClusterMembership member : cluster.getMembers()) {
                Response response = responseMap.get(member.getResponseId()); if (response == null) continue;
                double ai = calculateCohesion(response, cluster, distance, questions, responseMap);
                double bi = calculateSeparation(response, allClusters, cluster, distance, questions, responseMap);
                if (ai == 0.0 && bi == 0.0) continue; double silhouette = (bi - ai) / Math.max(ai, bi); totalSilhouette += silhouette; responseCount++; }
            return responseCount > 0 ? totalSilhouette / responseCount : 0.0;
        }

        public Double calculateCalinskiHarabasz(ClusteringAnalysis analysis, DistanceCalculator distance) {
            List<Cluster> clusters = analysis.getClusters(); List<Response> allResponses = analysis.getResponses(); List<Question> questions = analysis.getQuestions(); int k = clusters.size(); int N = allResponses.size(); if (k <= 1 || N <= k) return 0.0; Centroid overallCentroid = calculateOverallCentroid(allResponses, questions, distance); double ssB = 0.0; for (Cluster cluster : clusters) { int clusterSize = cluster.getSize(); if (clusterSize > 0) { double dist = distance.calculate(cluster.getCentroid(), overallCentroid, questions); ssB += clusterSize * Math.pow(dist, 2); } } double ssW = 0.0; for (Cluster cluster : clusters) { Centroid clusterCentroid = cluster.getCentroid(); for (ClusterMembership member : cluster.getMembers()) { Response response = allResponses.stream().filter(r -> r.getRESPONSE_ID().equals(member.getResponseId())).findFirst().orElse(null); if (response != null) { double dist = distance.calculateToCentroid(response, clusterCentroid, questions); ssW += Math.pow(dist, 2); } } } if (ssW == 0.0) return Double.MAX_VALUE; return (ssB / (k - 1)) / (ssW / (N - k)); }

        public Double calculateDaviesBouldin(ClusteringAnalysis analysis, DistanceCalculator distance) {
            List<Cluster> clusters = analysis.getClusters(); List<Response> allResponses = analysis.getResponses(); List<Question> questions = analysis.getQuestions(); int k = clusters.size(); if (k <= 1) return 0.0; double[] s = new double[k]; for (int i = 0; i < k; i++) { Cluster cluster = clusters.get(i); Centroid centroid = cluster.getCentroid(); double sumDistances = 0.0; int memberCount = 0; for (ClusterMembership member : cluster.getMembers()) { Response response = allResponses.stream().filter(r -> r.getRESPONSE_ID().equals(member.getResponseId())).findFirst().orElse(null); if (response != null) { sumDistances += distance.calculateToCentroid(response, centroid, questions); memberCount++; } } s[i] = memberCount > 0 ? sumDistances / memberCount : 0.0; } double totalDi = 0.0; for (int i = 0; i < k; i++) { double maxRij = 0.0; for (int j = 0; j < k; j++) { if (i == j) continue; Centroid ci = clusters.get(i).getCentroid(); Centroid cj = clusters.get(j).getCentroid(); double distBetweenCentroids = distance.calculate(ci, cj, questions); if (distBetweenCentroids == 0.0) { maxRij = Double.MAX_VALUE; break; } double Rij = (s[i] + s[j]) / distBetweenCentroids; if (Rij > maxRij) maxRij = Rij; } totalDi += maxRij; } return k > 0 ? totalDi / k : 0.0; }

        public String interpretScore(QualityMetricType metricType, Double score) {
            if (score == null || score.isNaN()) return "Score is undefined.";
            switch (metricType) { case SILHOUETTE: if (score > 0.7) return "Strong clustering structure."; if (score > 0.5) return "Reasonable clustering structure."; if (score > 0.2) return "Weak clustering structure, could be artificial."; if (score > 0.0) return "No substantial structure, or overlapping clusters."; return "Objects are probably assigned to the wrong clusters."; case CALINSKI_HARABASZ: return "Higher Calinski-Harabasz score indicates better defined clusters."; case DAVIES_BOULDIN: if (score < 1.0) return "Good clustering, clusters are compact and well-separated."; return "Higher Davies-Bouldin score indicates worse clustering."; default: return "Score: " + score; }
        }

        private Double calculateCohesion(Response response, Cluster ownCluster, DistanceCalculator distance, List<Question> questions, Map<String, Response> responseMap) {
            List<ClusterMembership> members = ownCluster.getMembers(); if (members.size() <= 1) return 0.0; double sumDistances = 0.0; int count = 0; for (ClusterMembership member : members) { if (!member.getResponseId().equals(response.getRESPONSE_ID())) { Response otherResponse = responseMap.get(member.getResponseId()); if (otherResponse != null) { sumDistances += distance.calculate(response, otherResponse, questions); count++; } } } return count > 0 ? sumDistances / count : 0.0; }

        private Double calculateSeparation(Response response, List<Cluster> allClusters, Cluster responseCluster, DistanceCalculator distance, List<Question> questions, Map<String, Response> responseMap) { double minAvgDistance = Double.MAX_VALUE; boolean foundOtherCluster = false; for (Cluster otherCluster : allClusters) { if (!otherCluster.getId().equals(responseCluster.getId())) { foundOtherCluster = true; double sumDistances = 0.0; int count = 0; for (ClusterMembership member : otherCluster.getMembers()) { Response otherResponse = responseMap.get(member.getResponseId()); if (otherResponse != null) { sumDistances += distance.calculate(response, otherResponse, questions); count++; } } if (count > 0) minAvgDistance = Math.min(minAvgDistance, sumDistances / count); } } return foundOtherCluster ? minAvgDistance : 0.0; }

        private Centroid calculateOverallCentroid(List<Response> allResponses, List<Question> questions, DistanceCalculator distance) { int numQuestions = questions.size(); Centroid overallCentroid = new Centroid(numQuestions); java.util.List<java.util.List<Double>> choiceSums = new ArrayList<>(numQuestions); java.util.List<java.util.List<String>> textValues = new ArrayList<>(numQuestions); int[] responseCountsPerQuestion = new int[numQuestions]; for (int i = 0; i < numQuestions; i++) { choiceSums.add(new ArrayList<>()); textValues.add(new ArrayList<>()); } for (Response response : allResponses) { for (int qIdx = 0; qIdx < numQuestions; qIdx++) { Question q = questions.get(qIdx); domain.model.Answer a = response.getAnswer(q.getQuestionIndex()); if (KMeansPlusPlus.isAnswered(a)) { Object value = KMeansPlusPlus.getAnswerValue(a, q); if (value instanceof double[]) { double[] val = (double[]) value; if (choiceSums.get(qIdx).isEmpty()) { for (double v : val) choiceSums.get(qIdx).add(v); } else { for (int optIdx = 0; optIdx < val.length; optIdx++) choiceSums.get(qIdx).set(optIdx, choiceSums.get(qIdx).get(optIdx) + val[optIdx]); } responseCountsPerQuestion[qIdx]++; } else if (value instanceof String) { textValues.get(qIdx).add((String) value); responseCountsPerQuestion[qIdx]++; } } } } for (int qIdx = 0; qIdx < numQuestions; qIdx++) { Question q = questions.get(qIdx); if (q instanceof domain.model.MultipleChoiceQuestion) { if (responseCountsPerQuestion[qIdx] > 0) { double[] avgOptions = new double[choiceSums.get(qIdx).size()]; for (int optIdx = 0; optIdx < choiceSums.get(qIdx).size(); optIdx++) avgOptions[optIdx] = choiceSums.get(qIdx).get(optIdx) / responseCountsPerQuestion[qIdx]; overallCentroid.setComponent(qIdx, avgOptions); } } else if (q.getTypeQuestion() == domain.model.enums.TypeQuestion.TEXTUAL) { if (!textValues.get(qIdx).isEmpty()) { String medoidText = findTextMedoid(textValues.get(qIdx), distance); overallCentroid.setComponent(qIdx, medoidText); } } } return overallCentroid; }

        private String findTextMedoid(List<String> texts, DistanceCalculator distance) { if (texts == null || texts.isEmpty()) return null; double minTotalDistance = Double.MAX_VALUE; String bestMedoid = texts.get(0); for (String candidate : texts) { double currentTotalDistance = 0.0; for (String other : texts) currentTotalDistance += distance.calculateTextDistance(candidate, other); if (currentTotalDistance < minTotalDistance) { minTotalDistance = currentTotalDistance; bestMedoid = candidate; } } return bestMedoid; }
    }
