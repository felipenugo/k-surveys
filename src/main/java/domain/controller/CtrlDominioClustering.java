package domain.controller;

import domain.clustering.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador de clustering (patrón Facade).
 * Crea algoritmos, ejecuta clustering, gestiona múltiples análisis y resultados.
 */
public class CtrlDominioClustering {
    
    // ========== ATRIBUTOS ==========
    
    private Map<String, ClusterResults> analysisResults;
    private ClusterResults currentResult;
    private String currentAnalysisId;
    private int analysisCounter;
    
    // ========== CONSTRUCTOR ==========
    
    public CtrlDominioClustering() {
        this.analysisResults = new HashMap<>();
        this.currentResult = null;
        this.currentAnalysisId = null;
        this.analysisCounter = 0;
    }
    
    // ========== MÉTODOS DE NEGOCIO ==========
    
    /** Crea algoritmo de clustering: "KMeans", "KMeansPlusPlus", "KMedoids". */
    public ClusteringAlgorithm crearAlgoritmo(String algorithmType, int maxIterations, double tolerance) {
        if (algorithmType == null || algorithmType.isEmpty()) {
            throw new IllegalArgumentException("El tipo de algoritmo no puede ser null o vacío");
        }
        
        // Usar valores por defecto si se pasan 0
        int iterations = (maxIterations <= 0) ? 100 : maxIterations;
        double tol = (tolerance <= 0.0) ? 1e-4 : tolerance;
        
        switch (algorithmType.toUpperCase()) {
            case "KMEANS":
                return new KMeans(iterations, tol);
                
            case "KMEANSPLUSPLUS":
            case "KMEANS++":
                return new KMeansPlusPlus(iterations, tol);
                
            case "KMEDOIDS":
                return new KMedoids(iterations, tol);
                
            default:
                throw new IllegalArgumentException("Tipo de algoritmo no soportado: " + algorithmType + 
                    ". Opciones válidas: KMeans, KMeansPlusPlus, KMedoids");
        }
    }
    
    /**
     * Ejecuta un análisis de clustering sobre los datos.
     * 
     * @param dataMatrix Matriz de datos [numPuntos][numFeatures]
     * @param k Número de clusters
     * @param algorithmType Tipo de algoritmo
     * @return ID del análisis creado
     * @throws IllegalArgumentException Si los parámetros son inválidos
     */
    public String ejecutarClustering(Object[][] dataMatrix, int k, String algorithmType) {
        return ejecutarClustering(dataMatrix, k, algorithmType, 0, 0.0);
    }
    
    /**
     * Ejecuta un análisis de clustering con parámetros personalizados.
     * 
     * @param dataMatrix Matriz de datos
     * @param k Número de clusters
     * @param algorithmType Tipo de algoritmo
     * @param maxIterations Máximo de iteraciones (0 para default)
     * @param tolerance Umbral de convergencia (0 para default)
     * @return ID del análisis creado
     */
    public String ejecutarClustering(Object[][] dataMatrix, int k, String algorithmType, 
                                     int maxIterations, double tolerance) {
        // Validaciones
        if (dataMatrix == null || dataMatrix.length == 0) {
            throw new IllegalArgumentException("La matriz de datos no puede ser null o vacía");
        }
        if (k <= 0 || k > dataMatrix.length) {
            throw new IllegalArgumentException("k debe estar entre 1 y " + dataMatrix.length);
        }
        
        // Crear algoritmo
        ClusteringAlgorithm algorithm = crearAlgoritmo(algorithmType, maxIterations, tolerance);
        
        // Crear calculador de distancia (Euclidiana por defecto)
        DistanceCalculator distance = new DistanceCalculator(DistanceType.EUCLIDEAN);
        
        // Ejecutar clustering
        ClusterResults results = algorithm.execute(dataMatrix, k, distance);
        
        // Generar ID único y guardar resultados
        String analysisId = generarAnalysisId(algorithmType);
        analysisResults.put(analysisId, results);
        
        // Actualizar resultado actual
        currentResult = results;
        currentAnalysisId = analysisId;
        
        return analysisId;
    }
    
    /**
     * Ejecuta un análisis de clustering con ID personalizado.
     * Útil para tests donde se necesita un ID específico.
     * 
     * @param analysisId ID personalizado para el análisis
     * @param algorithmType Tipo de algoritmo
     * @param dataMatrix Matriz de datos
     * @param k Número de clusters
     * @param maxIterations Máximo de iteraciones (0 para default)
     * @param tolerance Umbral de convergencia (0 para default)
     * @return Mensaje de confirmación
     */
    public String ejecutarClustering(String analysisId, String algorithmType, Object[][] dataMatrix, 
                                     int k, int maxIterations, double tolerance) {
        // Validaciones
        if (analysisId == null || analysisId.isEmpty()) {
            throw new IllegalArgumentException("El ID de análisis no puede ser null o vacío");
        }
        if (dataMatrix == null || dataMatrix.length == 0) {
            throw new IllegalArgumentException("La matriz de datos no puede ser null o vacía");
        }
        if (k <= 0 || k > dataMatrix.length) {
            throw new IllegalArgumentException("k debe estar entre 1 y " + dataMatrix.length);
        }
        
        // Crear algoritmo
        ClusteringAlgorithm algorithm = crearAlgoritmo(algorithmType, maxIterations, tolerance);
        
        // Crear calculador de distancia
        DistanceCalculator distance = new DistanceCalculator(DistanceType.EUCLIDEAN);
        
        // Ejecutar clustering
        ClusterResults results = algorithm.execute(dataMatrix, k, distance);
        
        // Guardar resultados con el ID especificado
        analysisResults.put(analysisId, results);
        
        // Actualizar resultado actual
        currentResult = results;
        currentAnalysisId = analysisId;
        
        return "Clustering ejecutado exitosamente";
    }
    
    /**
     * Obtiene los resultados de un análisis específico.
     * 
     * @param analysisId ID del análisis
     * @return Resultados del clustering
     * @throws IllegalArgumentException Si el ID no existe
     */
    public ClusterResults obtenerResultados(String analysisId) {
        if (analysisId == null || !analysisResults.containsKey(analysisId)) {
            throw new IllegalArgumentException("Análisis no encontrado: " + analysisId);
        }
        return analysisResults.get(analysisId);
    }
    
    /**
     * Obtiene los resultados del último análisis ejecutado.
     * 
     * @return Resultados del último clustering, o null si no hay ninguno
     */
    public ClusterResults obtenerResultadoActual() {
        return currentResult;
    }
    
    /**
     * Obtiene el ID del último análisis ejecutado.
     * 
     * @return ID del análisis actual, o null si no hay ninguno
     */
    public String obtenerAnalysisIdActual() {
        return currentAnalysisId;
    }
    
    /**
     * Obtiene información resumida de un cluster específico.
     * 
     * @param analysisId ID del análisis
     * @param clusterIndex Índice del cluster (0-based)
     * @return String con información del cluster
     */
    public String obtenerInfoCluster(String analysisId, int clusterIndex) {
        ClusterResults results = obtenerResultados(analysisId);
        
        if (clusterIndex < 0 || clusterIndex >= results.getNumberOfClusters()) {
            throw new IllegalArgumentException("Índice de cluster inválido: " + clusterIndex);
        }
        
        java.util.List<Integer> members = results.getResponsesInCluster(clusterIndex);
        Object[] centroid = results.getCentroid(clusterIndex);
        
        // Calcular distancia promedio
        double avgDistance = 0.0;
        for (int memberIdx : members) {
            avgDistance += results.getDistanceForResponse(memberIdx);
        }
        if (!members.isEmpty()) {
            avgDistance /= members.size();
        }
        
        StringBuilder info = new StringBuilder();
        info.append("Cluster ").append(clusterIndex).append(":\n");
        info.append("  Tamaño: ").append(members.size()).append(" puntos\n");
        info.append("  Distancia promedio al centroide: ").append(String.format("%.4f", avgDistance)).append("\n");
        info.append("  Centroide: [");
        for (int i = 0; i < centroid.length; i++) {
            if (i > 0) info.append(", ");
            info.append(String.format("%.2f", centroid[i]));
        }
        info.append("]\n");
        
        return info.toString();
    }
    
    /**
     * Obtiene un resumen completo de los resultados.
     * 
     * @param analysisId ID del análisis
     * @return String con resumen del clustering
     */
    public String obtenerResumen(String analysisId) {
        ClusterResults results = obtenerResultados(analysisId);
        
        StringBuilder resumen = new StringBuilder();
        resumen.append("=== RESUMEN DEL CLUSTERING ===\n");
        resumen.append("Análisis ID: ").append(analysisId).append("\n");
        resumen.append("Número de clusters (k): ").append(results.getNumberOfClusters()).append("\n");
        resumen.append("Número de puntos: ").append(results.getNumberOfResponses()).append("\n");
        resumen.append("Número de features: ").append(results.getNumberOfFeatures()).append("\n");
        resumen.append("Iteraciones: ").append(results.getIterations()).append("\n");
        resumen.append("Convergió: ").append(results.hasConverged() ? "Sí" : "No").append("\n");
        resumen.append("\n=== DISTRIBUCIÓN DE CLUSTERS ===\n");
        
        for (int i = 0; i < results.getNumberOfClusters(); i++) {
            java.util.List<Integer> members = results.getResponsesInCluster(i);
            double percentage = (members.size() * 100.0) / results.getNumberOfResponses();
            resumen.append(String.format("Cluster %d: %d puntos (%.1f%%)%n", 
                i, members.size(), percentage));
        }
        
        return resumen.toString();
    }
    
    /**
     * Limpia todos los análisis almacenados.
     */
    public void limpiarAnalisis() {
        analysisResults.clear();
        currentResult = null;
        currentAnalysisId = null;
    }
    
    /**
     * Lista los IDs de todos los análisis guardados.
     * 
     * @return Lista con los IDs de los análisis
     */
    public java.util.List<String> listarAnalisis() {
        return new java.util.ArrayList<>(analysisResults.keySet());
    }
    
    /**
     * Obtiene el número de análisis almacenados.
     * 
     * @return Número de análisis
     */
    public int getNumeroAnalisis() {
        return analysisResults.size();
    }
    
    // ========== MÉTODOS PRIVADOS ==========
    
    /**
     * Genera un ID único para un análisis.
     */
    private String generarAnalysisId(String algorithmType) {
        analysisCounter++;
        return algorithmType + "_" + analysisCounter + "_" + System.currentTimeMillis();
    }
}
