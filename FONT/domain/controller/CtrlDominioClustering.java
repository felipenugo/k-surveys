package domain.controller;

import data.ResponseRepository;
import data.SurveyRepository;
import domain.clustering.*;
import domain.clustering.TextDistanceType;
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
    public String runAnalysis(String algorithmName, String surveyId, int k, int maxIter, double tolerance, String distanceMetric, String textDistanceMetric) {
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
        TextDistanceType textDistanceType = TextDistanceType.valueOf(textDistanceMetric.toUpperCase());
        analysisController.executeAnalysis(analysis.getId(), responses, questions, new DistanceCalculator(distanceType, textDistanceType));

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
                String tempAnalysisId = runAnalysis(algorithmName, surveyId, k, maxIter, tolerance, distanceMetric, "LEVENSHTEIN");
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

    /**
     * Calcula el número óptimo de clusters (k) utilizando el método del codo (Elbow Method).
     *
     * @param surveyId ID de la encuesta.
     * @param maxK     Número máximo de clusters a evaluar.
     * @return El número óptimo de clusters.
     */
    public int calculateOptimalK(String surveyId, int maxK) {
        Survey survey = surveyRepository.getSurvey(surveyId);
        if (survey == null) {
            throw new IllegalArgumentException("Survey with ID " + surveyId + " not found.");
        }
        List<Response> responses = responseRepository.getResponsesBySurveyId(surveyId);
        if (responses.isEmpty()) return 1;

        int optimalK = 1;
        double maxSilhouette = -1.0;

        // Limitar maxK al número de respuestas si es menor
        int limitK = Math.min(maxK, responses.size() - 1);
        if (limitK < 2) return Math.min(2, responses.size()); // Al menos 2 clusters si es posible

        List<Question> questions = survey.getQuestions();
        DistanceCalculator distanceCalculator = new DistanceCalculator(DistanceType.EUCLIDEAN, TextDistanceType.LEVENSHTEIN);

        for (int k = 2; k <= limitK; k++) {
            Map<String, Object> config = new HashMap<>();
            config.put("maxIterations", 50);
            config.put("tolerance", 1e-4);

            // Usamos KMeans para la estimación rápida
            ClusteringAnalysis analysis = analysisController.createAnalysis(survey, k, "KMeans", config);
            analysisController.executeAnalysis(analysis.getId(), responses, questions, distanceCalculator);

            Double silhouette = analysisController.calculateSilhouette(analysis.getId(), distanceCalculator);

            if (silhouette > maxSilhouette) {
                maxSilhouette = silhouette;
                optimalK = k;
            }
        }
        return optimalK;
    }

    /**
     * Obtiene una representación textual de las respuestas de un usuario.
     *
     * @param surveyId ID de la encuesta.
     * @param responseId ID de la respuesta.
     * @return String con las respuestas formateadas.
     */
    public String getResponseSummary(String surveyId, String responseId) {
        Response response = responseRepository.getResponse(surveyId, responseId);
        if (response == null) return "Respuesta no encontrada";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < response.getSize(); i++) {
            domain.model.Answer answer = response.getAnswer(i);
            if (answer != null && answer.getIsAnswered()) {
                sb.append("P").append(i + 1).append(": ").append(answer.toString()).append("; ");
            }
        }
        return sb.toString();
    }

    /**
     * Obtiene las coordenadas 2D para visualizar los clusters.
     * Utiliza las dos primeras preguntas numéricas como ejes X e Y.
     * Si no hay suficientes preguntas numéricas, usa un hash simple para visualización.
     *
     * @param analysisId ID del análisis.
     * @param surveyId ID de la encuesta.
     * @return Mapa de ResponseID -> Point2D (double[2])
     */
    public Map<String, double[]> getClusterVisualizationData(String analysisId, String surveyId) {
        ClusteringAnalysis analysis = analysisController.getAnalysis(analysisId);
        if (analysis == null) return new HashMap<>();

        Map<String, double[]> points = new HashMap<>();
        
        // Obtener todas las respuestas del análisis
        List<Response> allResponses = new ArrayList<>();
        for (Cluster c : analysis.getClusters()) {
            for (ClusterMembership member : c.getMembers()) {
                Response r = responseRepository.getResponse(surveyId, member.getResponseId());
                if (r != null) {
                    allResponses.add(r);
                }
            }
        }

        if (allResponses.isEmpty()) return points;

        // Identificar índices de preguntas numéricas
        List<Integer> numericalIndices = new ArrayList<>();
        Response first = allResponses.get(0);
        for (int i = 0; i < first.getSize(); i++) {
            if (first.getAnswer(i) instanceof domain.model.NumericalAnswer) {
                numericalIndices.add(i);
            }
        }

        // Generar coordenadas
        for (Response r : allResponses) {
            double x = 0.0;
            double y = 0.0;

            if (numericalIndices.size() >= 2) {
                // Usar las dos primeras preguntas numéricas
                x = getNumericalValue(r.getAnswer(numericalIndices.get(0)));
                y = getNumericalValue(r.getAnswer(numericalIndices.get(1)));
            } else if (numericalIndices.size() == 1) {
                // Usar la única numérica como X y el índice como Y (jitter)
                x = getNumericalValue(r.getAnswer(numericalIndices.get(0)));
                y = r.getRESPONSE_ID().hashCode() % 10; 
            } else {
                // Fallback: Hash de respuestas textuales para dispersión
                x = r.getRESPONSE_ID().hashCode() % 100;
                y = (r.getRESPONSE_ID().hashCode() / 100) % 100;
            }
            
            points.put(r.getRESPONSE_ID(), new double[]{x, y});
        }

        return points;
    }

    /**
     * Obtiene las coordenadas de los centroides para visualización.
     *
     * @param analysisId ID del análisis.
     * @return Mapa de ClusterID -> Coordenadas 2D (double[2])
     */
    public Map<String, double[]> getCentroidsVisualizationData(String analysisId) {
        ClusteringAnalysis analysis = analysisController.getAnalysis(analysisId);
        if (analysis == null) return new HashMap<>();

        Map<String, double[]> centroids = new HashMap<>();
        
        for (Cluster c : analysis.getClusters()) {
            Centroid centroid = c.getCentroid();
            if (centroid != null) {
                List<Object> components = centroid.getComponents();
                double x = 0.0;
                double y = 0.0;
                
                // Buscar las primeras dos componentes numéricas
                int foundNumeric = 0;
                for (Object comp : components) {
                    if (comp instanceof Number && foundNumeric < 2) {
                        double val = ((Number) comp).doubleValue();
                        if (foundNumeric == 0) x = val;
                        else y = val;
                        foundNumeric++;
                    }
                }
                
                centroids.put(c.getId(), new double[]{x, y});
            }
        }

        return centroids;
    }

    private double getNumericalValue(domain.model.Answer answer) {
        if (answer instanceof domain.model.NumericalAnswer) {
            Double val = ((domain.model.NumericalAnswer) answer).getAnswerNum();
            return val != null ? val : 0.0;
        }
        return 0.0;
    }

    /**
     * Obtiene las etiquetas de las preguntas numéricas de una encuesta.
     *
     * @param surveyId ID de la encuesta.
     * @return Lista de pares [índice, etiqueta] de preguntas numéricas.
     */
    public List<Map.Entry<Integer, String>> getNumericalQuestionLabels(String surveyId) {
        Survey survey = surveyRepository.getSurvey(surveyId);
        if (survey == null) return new ArrayList<>();

        List<Map.Entry<Integer, String>> labels = new ArrayList<>();
        List<Question> questions = survey.getQuestions();
        
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            // Solo añadir preguntas numéricas
            if (q.getTypeQuestion() == domain.model.enums.TypeQuestion.NUMERICAL) {
                final int index = i;
                final String label = "P" + (i + 1) + ": " + q.getQuestionText();
                labels.add(new java.util.AbstractMap.SimpleEntry<>(index, label));
            }
        }
        
        return labels;
    }

    /**
     * Obtiene las coordenadas 2D para visualizar los clusters con dimensiones específicas.
     *
     * @param analysisId ID del análisis.
     * @param surveyId ID de la encuesta.
     * @param dimXIndex Índice de la pregunta para el eje X.
     * @param dimYIndex Índice de la pregunta para el eje Y.
     * @return Mapa de ResponseID -> Point2D (double[2])
     */
    public Map<String, double[]> getClusterVisualizationDataWithDims(String analysisId, String surveyId, int dimXIndex, int dimYIndex) {
        ClusteringAnalysis analysis = analysisController.getAnalysis(analysisId);
        if (analysis == null) return new HashMap<>();

        Map<String, double[]> points = new HashMap<>();
        
        // Obtener todas las respuestas del análisis
        for (Cluster c : analysis.getClusters()) {
            for (ClusterMembership member : c.getMembers()) {
                Response r = responseRepository.getResponse(surveyId, member.getResponseId());
                if (r != null) {
                    double x = getNumericalValue(r.getAnswer(dimXIndex));
                    double y = getNumericalValue(r.getAnswer(dimYIndex));
                    points.put(r.getRESPONSE_ID(), new double[]{x, y});
                }
            }
        }

        return points;
    }

    /**
     * Obtiene las coordenadas de los centroides para dimensiones específicas.
     *
     * @param analysisId ID del análisis.
     * @param dimXIndex Índice de la dimensión para el eje X.
     * @param dimYIndex Índice de la dimensión para el eje Y.
     * @return Mapa de ClusterID -> Coordenadas 2D (double[2])
     */
    public Map<String, double[]> getCentroidsVisualizationDataWithDims(String analysisId, int dimXIndex, int dimYIndex) {
        ClusteringAnalysis analysis = analysisController.getAnalysis(analysisId);
        if (analysis == null) return new HashMap<>();

        Map<String, double[]> centroids = new HashMap<>();
        
        for (Cluster c : analysis.getClusters()) {
            Centroid centroid = c.getCentroid();
            if (centroid != null) {
                List<Object> components = centroid.getComponents();
                double x = 0.0;
                double y = 0.0;
                
                // Obtener valores de las dimensiones específicas
                if (dimXIndex < components.size() && components.get(dimXIndex) instanceof Number) {
                    x = ((Number) components.get(dimXIndex)).doubleValue();
                }
                if (dimYIndex < components.size() && components.get(dimYIndex) instanceof Number) {
                    y = ((Number) components.get(dimYIndex)).doubleValue();
                }
                
                centroids.put(c.getId(), new double[]{x, y});
            }
        }

        return centroids;
    }
}