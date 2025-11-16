package domain.controller;

import data.ResponseRepository;
import data.SurveyRepository;
import domain.clustering.*;
import domain.model.Question;
import domain.model.Response;
import domain.model.Survey;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controlador de dominio para operaciones de clustering.
 * Gestiona la creación y ejecución de análisis de clustering sobre encuestas.
 */
public class CtrlDominioClustering {

    private final AnalysisController analysisController;
    private final ResponseRepository responseRepository;
    private final SurveyRepository surveyRepository;

    /**
     * Constructor del controlador de clustering.
     *
     * @param responseRepository Repositorio de respuestas
     * @param surveyRepository   Repositorio de encuestas
     */
    public CtrlDominioClustering(ResponseRepository responseRepository, SurveyRepository surveyRepository) {
        this.analysisController = new AnalysisController();
        this.responseRepository = responseRepository;
        this.surveyRepository = surveyRepository;
    }

    /**
     * Ejecuta un análisis de clustering sobre una encuesta.
     *
     * @param algorithmName  Nombre del algoritmo (KMeans, KMeans++, KMedoids)
     * @param surveyId       ID de la encuesta
     * @param k              Número de clusters
     * @param maxIter        Iteraciones máximas
     * @param tolerance      Tolerancia de convergencia
     * @param distanceMetric Métrica de distancia (EUCLIDEAN, MANHATTAN)
     * @return ID del análisis ejecutado
     */
    public String runAnalysis(String algorithmName, String surveyId, int k, int maxIter, double tolerance, String distanceMetric) {
        Survey survey = surveyRepository.getSurvey(surveyId);
        if (survey == null) {
            throw new IllegalArgumentException("Survey with ID " + surveyId + " not found.");
        }

        List<Response> responses = responseRepository.getResponsesBySurveyId(surveyId);
        if (k <= 0 || k > responses.size()) {
            throw new IllegalArgumentException("Invalid number of clusters (k). It must be greater than 0 and not greater than the number of responses.");
        }

        Map<String, Object> config = new HashMap<>();
        config.put("maxIterations", maxIter);
        config.put("tolerance", tolerance);

        ClusteringAnalysis analysis = analysisController.createAnalysis(survey, k, algorithmName, config);

        List<Question> questions = survey.getQuestions();

        DistanceType distanceType = DistanceType.valueOf(distanceMetric.toUpperCase());
        analysisController.executeAnalysis(analysis.getId(), responses, questions, new DistanceCalculator(distanceType));

        return analysis.getId();
    }

    /**
     * Exporta los resultados de un análisis a un archivo.
     *
     * @param analysisId ID del análisis a exportar.
     * @return Ruta del archivo guardado.
     * @throws IOException si ocurre un error de E/S.
     */
    public String exportarAnalisis(String analysisId, String filePath) throws IOException {
        ClusteringAnalysis analysis = analysisController.getAnalysis(analysisId);
        if (analysis == null) {
            throw new IllegalArgumentException("Analysis with ID " + analysisId + " not found.");
        }

        String results = analysis.exportResults();
        File file = new File(filePath);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(results);
        }

        return file.getAbsolutePath();
    }

    /**
     * Obtiene las métricas de calidad de un análisis en un formato legible.
     *
     * @param analysisId ID del análisis.
     * @return Un String formateado con las métricas de calidad.
     */
    public String obtenerMetricasCalidad(String analysisId) {
        ClusteringAnalysis analysis = analysisController.getAnalysis(analysisId);
        if (analysis == null) {
            throw new IllegalArgumentException("Analysis with ID " + analysisId + " not found.");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Métricas de Calidad para el Análisis: ").append(analysisId).append("\n");
        sb.append("--------------------------------------------------\n");

        try {
            // Overall metrics
            sb.append("Métricas Generales:\n");
            Double silhouette = analysis.calculateQuality(QualityMetricType.SILHOUETTE);
            sb.append(String.format("- Coeficiente de Silhouette: %.4f\n", silhouette));

            Double calinski = analysis.calculateQuality(QualityMetricType.CALINSKI_HARABASZ);
            sb.append(String.format("- Índice de Calinski-Harabasz: %.4f\n", calinski));

            Double davies = analysis.calculateQuality(QualityMetricType.DAVIES_BOULDIN);
            sb.append(String.format("- Índice de Davies-Bouldin: %.4f\n", davies));

            // Per-cluster metrics
            sb.append("\nMétricas por Cluster:\n");
            for (Cluster cluster : analysis.getClusters()) {
                Double clusterSilhouette = analysis.getClusterQuality(cluster, QualityMetricType.SILHOUETTE);
                sb.append(String.format("- Cluster %s (Tamaño: %d): Coeficiente de Silhouette = %.4f\n",
                        cluster.getId(), cluster.getSize(), clusterSilhouette));
            }

        } catch (IllegalStateException e) {
            return "Error: El análisis no ha sido ejecutado o no contiene datos suficientes.";
        }
        return sb.toString();
    }

    /**
     * Encuentra el valor óptimo de 'k' para un algoritmo y encuesta dados.
     *
     * @param surveyId       ID de la encuesta.
     * @param algorithmName  Nombre del algoritmo.
     * @param maxIter        Iteraciones máximas.
     * @param tolerance      Tolerancia.
     * @param distanceMetric Métrica de distancia.
     * @return Un informe con los resultados para cada 'k' y una recomendación.
     */
    public String encontrarKOptima(String surveyId, String algorithmName, int maxIter, double tolerance, String distanceMetric) {
        StringBuilder report = new StringBuilder();
        report.append("--- Búsqueda de K Óptima ---\n");
        report.append("Algoritmo: ").append(algorithmName).append(", Distancia: ").append(distanceMetric).append("\n\n");

        double bestScore = -2.0; // Silhouette score is between -1 and 1
        int optimalK = -1;

        for (int k = 2; k <= 10; ++k) {
            try {
                String tempAnalysisId = runAnalysis(algorithmName, surveyId, k, maxIter, tolerance, distanceMetric);
                ClusteringAnalysis tempAnalysis = analysisController.getAnalysis(tempAnalysisId);

                if (tempAnalysis != null) {
                    Double silhouette = tempAnalysis.calculateQuality(QualityMetricType.SILHOUETTE);
                    report.append(String.format("k = %d -> Coeficiente de Silhouette: %.4f\n", k, silhouette));

                    if (silhouette > bestScore) {
                        bestScore = silhouette;
                        optimalK = k;
                    }
                    // Clean up the temporary analysis
                    deleteAnalysis(tempAnalysisId);
                }
            } catch (Exception e) {
                report.append(String.format("k = %d -> Error al ejecutar el análisis: %s\n", k, e.getMessage()));
            }
        }

        report.append("\n--- Recomendación ---\n");
        if (optimalK != -1) {
            report.append(String.format("La 'k' óptima recomendada es %d (Silhouette más alto: %.4f).\n", optimalK, bestScore));
        } else {
            report.append("No se pudo determinar una 'k' óptima debido a errores en los análisis.\n");
        }

        return report.toString();
    }


    /**
     * Lista todos los IDs de análisis almacenados, ordenados del más reciente al más antiguo.
     *
     * @return Lista de IDs de análisis ordenados.
     */
    public List<String> listarAnalisis() {
        return analysisController.getAnalyses().values().stream()
                .sorted(Comparator.comparing(ClusteringAnalysis::getAnalysisDate).reversed())
                .map(ClusteringAnalysis::getId)
                .collect(Collectors.toList());
    }

    /**
     * Elimina un análisis específico.
     *
     * @param analysisId ID del análisis a eliminar.
     */
    public void deleteAnalysis(String analysisId) {
        analysisController.deleteAnalysis(analysisId);
    }

    /**
     * Obtiene los resultados completos de un análisis en formato de texto.
     *
     * @param analysisId ID del análisis.
     * @return Un String con los resultados detallados del análisis.
     */
    public String obtenerResultadosAnalisis(String analysisId) {
        ClusteringAnalysis analysis = analysisController.getAnalysis(analysisId);
        if (analysis == null) {
            throw new IllegalArgumentException("Analysis with ID " + analysisId + " not found.");
        }
        return analysis.exportResults();
    }

    /**
     * Limpia todos los análisis almacenados.
     */
    public void limpiarAnalisis() {
        analysisController.clearAnalyses();
    }
}
