package domain.controller;

import domain.clustering.ClusterResults;
import domain.clustering.ClusteringAlgorithm;
import domain.clustering.DistanceCalculator;
import domain.clustering.DistanceType;
import domain.clustering.KMeans;
import domain.clustering.KMeansPlusPlus;
import domain.clustering.KMedoids;
import domain.model.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CtrlDominioClustering {

    private Map<String, ClusterResults> analysis = new HashMap<>();

    public String ejecutarClustering(String analisisId, String algoritmo, Object[][] data, int k, int maxIter, double tolerance) {
        List<Response> responses = new ArrayList<>();
        for (Object[] datum : data) {
            Response r = new Response(analisisId, analisisId, k);
            responses.add(r);
        }

        ClusteringAlgorithm algorithm;
        switch (algoritmo) {
            case "KMeans":
                algorithm = new KMeans(maxIter, tolerance);
                break;
            case "KMeansPlusPlus":
                algorithm = new KMeansPlusPlus(maxIter, tolerance);
                break;
            case "KMedoids":
                algorithm = new KMedoids(maxIter, tolerance);
                break;
            default:
                throw new IllegalArgumentException("Algoritmo no válido");
        }

        DistanceCalculator distance = new DistanceCalculator(DistanceType.EUCLIDEAN);
        ClusterResults results = algorithm.execute(responses, new ArrayList<>(), k, distance);
        analysis.put(analisisId, results);
        return "Análisis " + analisisId + " ejecutado con éxito";
    }

    public ClusterResults obtenerResultados(String analisisId) {
        if (!analysis.containsKey(analisisId)) {
            throw new IllegalArgumentException("Análisis no encontrado");
        }
        return analysis.get(analisisId);
    }

    public String obtenerResumen(String analisisId) {
        ClusterResults results = obtenerResultados(analisisId);
        return "Resumen del análisis " + analisisId + ":\n" +
                "  - Clusters: " + results.getNumberOfClusters() + "\n" +
                "  - Iteraciones: " + results.getIterations() + "\n" +
                "  - Convergencia: " + (results.hasConverged() ? "Sí" : "No");
    }

    public String obtenerInfoCluster(String analisisId, int clusterId) {
        ClusterResults results = obtenerResultados(analisisId);
        if (clusterId < 0 || clusterId >= results.getNumberOfClusters()) {
            throw new IllegalArgumentException("ID de cluster fuera de rango");
        }
        return "Información del cluster " + clusterId + " del análisis " + analisisId + ":\n" +
                "  - Puntos: " + results.getCluster(clusterId).getSize() + "\n" +
                "  - Distancia media: " + results.getCluster(clusterId).getAverageDistance();
    }

    public List<String> listarAnalisis() {
        return new ArrayList<>(analysis.keySet());
    }

    public void limpiarAnalisis() {
        analysis.clear();
    }
}
