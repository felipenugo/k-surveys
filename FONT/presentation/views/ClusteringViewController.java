package presentation.views;

import domain.controller.CtrlDominioClustering;
import domain.controller.SurveyController;
import domain.controller.UserController;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class ClusteringViewController implements Initializable {

    private final UserController userController;
    private final SurveyController surveyController;
    private final CtrlDominioClustering clusteringController;
    private final SceneManager sceneManager;
    private final String surveyId;

    @FXML private Label avatarLabel;
    @FXML private Label usernameLabel;
    @FXML private Button mySurveys;
    @FXML private Button runAnalysisBtn;
    
    @FXML private ComboBox<String> algorithmCombo;
    @FXML private TextField kField;
    @FXML private ComboBox<String> distanceCombo;
    @FXML private ComboBox<String> textDistanceCombo;
    @FXML private TextField maxIterField;
    @FXML private TextField toleranceField;
    
    @FXML private VBox resultsContainer;
    @FXML private VBox clustersContainer;
    @FXML private Label silhouetteLabel;
    @FXML private Label executionTimeLabel;
    @FXML private Label calinskiLabel;
    @FXML private Label daviesLabel;

    @FXML private StackPane chartsStackPane;
    @FXML private HBox chartsContainer;
    @FXML private BarChart<String, Number> sizeBarChart;
    @FXML private NumberAxis barYAxis;
    @FXML private ScatterChart<Number, Number> silhouetteScatterChart;
    @FXML private NumberAxis scatterXAxis;
    @FXML private NumberAxis scatterYAxis;
    @FXML private ComboBox<String> xAxisCombo;
    @FXML private ComboBox<String> yAxisCombo;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private VBox loadingOverlay;

    // Colores compartidos para clusters (barras y puntos)
    private static final String[] CLUSTER_COLORS = {
        "#3498db", "#ce3a2aff", "#2ecc71", "#9b59b6", "#f39c12", "#1abc9c", "#e91e63", "#00bcd4"
    };

    private String currentAnalysisId;
    private List<Map.Entry<Integer, String>> allQuestions; // Para los selectores de dimensión (todas las preguntas)
    private java.util.Map<String, XYChart.Series<Number, Number>> currentScatterSeriesMap; // Series actuales

    public ClusteringViewController(UserController userController, SurveyController surveyController, 
                                  CtrlDominioClustering clusteringController, SceneManager sceneManager, String surveyId) {
        this.userController = userController;
        this.surveyController = surveyController;
        this.clusteringController = clusteringController;
        this.sceneManager = sceneManager;
        this.surveyId = surveyId;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupUserInfo();
        setupCombos();
        setupDimensionCombos();
        setupChartAxes();
        mySurveys.getStyleClass().add("nav-btn-active");
        kField.setText("2"); // Valor por defecto
    }

    /**
     * Configura los ejes de las gráficas para mostrar enteros.
     */
    private void setupChartAxes() {
        // Configurar eje Y del BarChart para enteros
        if (barYAxis != null) {
            barYAxis.setTickUnit(1);
            barYAxis.setMinorTickVisible(false);
            barYAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(barYAxis) {
                @Override
                public String toString(Number object) {
                    return String.valueOf(object.intValue());
                }
            });
        }
        
        // Configurar ejes del ScatterChart para enteros
        if (scatterXAxis != null) {
            scatterXAxis.setTickUnit(1);
            scatterXAxis.setMinorTickVisible(false);
            scatterXAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(scatterXAxis) {
                @Override
                public String toString(Number object) {
                    return String.valueOf(object.intValue());
                }
            });
        }
        
        if (scatterYAxis != null) {
            scatterYAxis.setTickUnit(1);
            scatterYAxis.setMinorTickVisible(false);
            scatterYAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(scatterYAxis) {
                @Override
                public String toString(Number object) {
                    return String.valueOf(object.intValue());
                }
            });
        }
    }

    private void setupUserInfo() {
        String username = userController.getUsernameLoggedIn();
        usernameLabel.setText(username);
        if (username != null && !username.isEmpty()) {
            avatarLabel.setText(username.substring(0, 1).toUpperCase());
        }
    }

    // Mapeos de nombres de display a valores internos
    private static final java.util.Map<String, String> ALGORITHM_MAP = java.util.Map.of(
        "K-Means", "KMeans",
        "K-Means++", "KMeansPlusPlus",
        "K-Medoids", "KMedoids"
    );
    
    private static final java.util.Map<String, String> DISTANCE_MAP = java.util.Map.of(
        "Euclidean", "EUCLIDEAN",
        "Manhattan", "MANHATTAN",
        "Cosinus", "COSINE"
    );
    
    private static final java.util.Map<String, String> TEXT_DISTANCE_MAP = java.util.Map.of(
        "Levenshtein", "LEVENSHTEIN",
        "Embedding", "EMBEDDING"
    );

    private void setupCombos() {
        algorithmCombo.getItems().addAll("K-Means", "K-Means++", "K-Medoids");
        algorithmCombo.setValue("K-Means");

        distanceCombo.getItems().addAll("Euclidean", "Manhattan", "Cosinus");
        distanceCombo.setValue("Euclidean");

        textDistanceCombo.getItems().addAll("Levenshtein", "Embedding");
        textDistanceCombo.setValue("Levenshtein");
    }

    private void setupDimensionCombos() {
        // Obtener TODAS las preguntas para los selectores de dimensión
        allQuestions = clusteringController.getAllQuestionLabels(surveyId);
        
        xAxisCombo.getItems().clear();
        yAxisCombo.getItems().clear();
        
        for (Map.Entry<Integer, String> entry : allQuestions) {
            xAxisCombo.getItems().add(entry.getValue());
            yAxisCombo.getItems().add(entry.getValue());
        }
        
        // Seleccionar por defecto las dos primeras si hay suficientes
        if (allQuestions.size() >= 2) {
            xAxisCombo.setValue(allQuestions.get(0).getValue());
            yAxisCombo.setValue(allQuestions.get(1).getValue());
        } else if (allQuestions.size() == 1) {
            xAxisCombo.setValue(allQuestions.get(0).getValue());
            yAxisCombo.setValue(allQuestions.get(0).getValue());
        }
        
        // Listeners para actualizar el scatter chart cuando se cambie la dimensión
        xAxisCombo.setOnAction(e -> updateScatterChart());
        yAxisCombo.setOnAction(e -> updateScatterChart());
    }

    @FXML
    public void calculateOptimalK(ActionEvent event) {
        try {
            int maxK = 10; // Límite razonable
            int optimalK = clusteringController.calculateOptimalK(surveyId, maxK);
            kField.setText(String.valueOf(optimalK));
            showAlert("Cálculo Completado", "El número óptimo de clusters estimado es: " + optimalK);
        } catch (Exception e) {
            showAlert("Error", "Error al calcular K óptimo: " + e.getMessage());
        }
    }

    @FXML
    public void runAnalysis(ActionEvent event) {
        try {
            String algorithm = ALGORITHM_MAP.get(algorithmCombo.getValue());
            String distance = DISTANCE_MAP.get(distanceCombo.getValue());
            String textDistance = TEXT_DISTANCE_MAP.get(textDistanceCombo.getValue());
            int k = Integer.parseInt(kField.getText());
            int maxIter = Integer.parseInt(maxIterField.getText());
            double tolerance = Double.parseDouble(toleranceField.getText());

            // Ocultar resultados anteriores y mostrar indicador de carga
            resultsContainer.setVisible(false);
            resultsContainer.setManaged(false);
            showLoadingOverlay(true);
            
            // Desactivar botón durante el procesamiento
            if (runAnalysisBtn != null) {
                runAnalysisBtn.setDisable(true);
            }

            Task<String> analysisTask = new Task<>() {
                @Override
                protected String call() throws Exception {
                    return clusteringController.runAnalysis(algorithm, surveyId, k, maxIter, tolerance, distance, textDistance);
                }
            };

            analysisTask.setOnSucceeded(e -> {
                currentAnalysisId = analysisTask.getValue();
                try {
                    displayResults(currentAnalysisId);
                } catch (IOException ex) {
                    showAlert("Error", "Error al mostrar resultados: " + ex.getMessage());
                } finally {
                    showLoadingOverlay(false);
                    if (runAnalysisBtn != null) {
                        runAnalysisBtn.setDisable(false);
                    }
                }
            });

            analysisTask.setOnFailed(e -> {
                showLoadingOverlay(false);
                if (runAnalysisBtn != null) {
                    runAnalysisBtn.setDisable(false);
                }
                Throwable ex = analysisTask.getException();
                showAlert("Error", "Error al ejecutar el análisis: " + (ex != null ? ex.getMessage() : "Desconocido"));
                if (ex != null) ex.printStackTrace();
            });

            new Thread(analysisTask).start();

        } catch (NumberFormatException e) {
            showAlert("Error", "Por favor, introduce valores numéricos válidos.");
        }
    }

    private void showLoadingOverlay(boolean show) {
        if (loadingOverlay != null) {
            loadingOverlay.setVisible(show);
            loadingOverlay.setManaged(show);
        }
    }

    private void displayResults(String analysisId) throws IOException {
        // Usamos el archivo temporal para obtener el texto completo y parsearlo "a mano" para la UI
        File tempFile = File.createTempFile("clustering_results_", ".txt");
        clusteringController.exportarAnalisis(analysisId, tempFile.getAbsolutePath());
        String results = java.nio.file.Files.readString(tempFile.toPath());
        tempFile.delete();

        clustersContainer.getChildren().clear();
        sizeBarChart.getData().clear();
        silhouetteScatterChart.getData().clear();

        XYChart.Series<String, Number> sizeSeries = new XYChart.Series<>();
        sizeSeries.setName("Tamaño de Clusters");
        
        // Preparar series para el Scatter Chart (una por cluster para colores distintos)
        // Map<ClusterID, Series>
        java.util.Map<String, XYChart.Series<Number, Number>> scatterSeriesMap = new java.util.HashMap<>();

        // Obtener coordenadas para visualización
        Map<String, double[]> points = clusteringController.getClusterVisualizationData(analysisId, surveyId);

        String[] lines = results.split("\n");
        VBox currentClusterBox = null;
        boolean inClustersSection = false;
        String currentClusterId = "";
        
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("----------")) continue;

            if (line.startsWith("Clusters (")) {
                inClustersSection = true;
                continue;
            }

            if (!inClustersSection) {
                // Global Metrics
                if (line.startsWith("- Execution Time:")) {
                    executionTimeLabel.setText(line.replace("- Execution Time:", "").trim());
                } else if (line.startsWith("- Silhouette Score:")) {
                    silhouetteLabel.setText(line.replace("- Silhouette Score:", "").trim());
                } else if (line.startsWith("- Calinski-Harabasz Score:")) {
                    calinskiLabel.setText(line.replace("- Calinski-Harabasz Score:", "").trim());
                } else if (line.startsWith("- Davies-Bouldin Score:")) {
                    daviesLabel.setText(line.replace("- Davies-Bouldin Score:", "").trim());
                }
            } else {
                // Cluster Parsing
                if (line.startsWith("Cluster")) {
                    currentClusterBox = new VBox(12);
                    currentClusterBox.getStyleClass().add("cluster-card");
                    
                    // Parse ID and Size for Charts
                    // Format: "Cluster cluster_1 (Size: 5)" -> Display as "Cluster 1"
                    int currentSize = 0;
                    String displayLabel = "";
                    try {
                        int sizeIndex = line.indexOf("(Size:");
                        if (sizeIndex != -1) {
                            currentClusterId = line.substring(0, sizeIndex).trim();
                            String sizeStr = line.substring(sizeIndex + 6, line.indexOf(")")).trim();
                            currentSize = Integer.parseInt(sizeStr);
                            // Simplificar etiqueta: "Cluster cluster_1" -> "Cluster 1"
                            displayLabel = currentClusterId.replaceAll("cluster_", "");
                            sizeSeries.getData().add(new XYChart.Data<>(displayLabel, currentSize));
                            
                            // Crear serie para scatter plot con etiqueta simplificada
                            XYChart.Series<Number, Number> series = new XYChart.Series<>();
                            series.setName(displayLabel);
                            scatterSeriesMap.put(currentClusterId, series);
                        }
                    } catch (Exception e) {
                        System.err.println("Error parsing cluster size: " + e.getMessage());
                    }
                    
                    // Título simplificado
                    Label clusterTitle = new Label(displayLabel.isEmpty() ? "Cluster" : displayLabel);
                    clusterTitle.getStyleClass().add("cluster-title");
                    currentClusterBox.getChildren().add(clusterTitle);
                    
                    // Mostrar número de elementos
                    Label elementsLabel = new Label("Elementos: " + currentSize);
                    elementsLabel.getStyleClass().add("cluster-elements");
                    currentClusterBox.getChildren().add(elementsLabel);
                    
                    clustersContainer.getChildren().add(currentClusterBox);

                } else if (currentClusterBox != null) {
                    if (line.startsWith("Silhouette Score:")) {
                        // Crear contenedor de métricas si no existe
                        HBox metricsBox = new HBox(15);
                        metricsBox.getStyleClass().add("cluster-metrics-box");
                        
                        String silhouetteValue = line.replace("Silhouette Score:", "").trim();
                        VBox silhouetteBox = createMetricBox("Silhouette", silhouetteValue);
                        metricsBox.getChildren().add(silhouetteBox);
                        
                        // Guardar referencia para añadir más métricas
                        currentClusterBox.getChildren().add(metricsBox);
                    } else if (line.startsWith("Centroid:")) {
                        // Formatear el centroide mostrando cada componente
                        String centroidStr = line.replace("Centroid:", "").trim();
                        String formattedCentroid = formatCentroid(centroidStr);
                        
                        Label centroidTitle = new Label("Centroide:");
                        centroidTitle.getStyleClass().add("cluster-centroid-title");
                        currentClusterBox.getChildren().add(centroidTitle);
                        
                        Label centroidLabel = new Label(formattedCentroid);
                        centroidLabel.getStyleClass().add("cluster-centroid");
                        centroidLabel.setWrapText(true);
                        currentClusterBox.getChildren().add(centroidLabel);
                    } else if (line.startsWith("Member Response IDs:")) {
                        Label membersTitle = new Label("Respuestas:");
                        membersTitle.getStyleClass().add("cluster-centroid-title");
                        currentClusterBox.getChildren().add(membersTitle);
                    } else if (line.startsWith("-")) {
                        // Member ID
                        String responseId = line.substring(1).trim();
                        
                        // Obtener resumen de respuesta
                        String summary = clusteringController.getResponseSummary(surveyId, responseId);
                        
                        // Crear contenedor para la respuesta
                        VBox responseBox = new VBox(5);
                        responseBox.getStyleClass().add("cluster-response-item");
                        
                        Label idLabel = new Label("ID: " + responseId);
                        idLabel.getStyleClass().add("response-id");
                        responseBox.getChildren().add(idLabel);
                        
                        Label summaryLabel = new Label(summary);
                        summaryLabel.getStyleClass().add("response-summary");
                        summaryLabel.setWrapText(true);
                        responseBox.getChildren().add(summaryLabel);
                        
                        currentClusterBox.getChildren().add(responseBox);

                        // Añadir punto al Scatter Chart
                        if (points.containsKey(responseId) && scatterSeriesMap.containsKey(currentClusterId)) {
                            double[] coords = points.get(responseId);
                            XYChart.Data<Number, Number> dataPoint = new XYChart.Data<>(coords[0], coords[1]);
                            scatterSeriesMap.get(currentClusterId).getData().add(dataPoint);
                        }
                    }
                }
            }
        }

        sizeBarChart.getData().add(sizeSeries);
        
        // Los colores de las barras se manejan automáticamente via CSS (default-color0, default-color1, etc.)
        // Pero como todas las barras están en una sola serie, necesitamos asignar colores manualmente
        Platform.runLater(() -> {
            for (int i = 0; i < sizeSeries.getData().size(); i++) {
                XYChart.Data<String, Number> data = sizeSeries.getData().get(i);
                final String color = CLUSTER_COLORS[i % CLUSTER_COLORS.length];
                if (data.getNode() != null) {
                    data.getNode().setStyle("-fx-bar-fill: " + color + ";");
                }
            }
        });
        
        // Guardar las series para poder actualizarlas cuando cambie la dimensión
        currentScatterSeriesMap = scatterSeriesMap;
        
        // Añadir series al scatter chart - los colores se asignan via CSS automáticamente
        // (default-color0, default-color1, etc. definidos en clustering.css)
        for (XYChart.Series<Number, Number> s : scatterSeriesMap.values()) {
            silhouetteScatterChart.getData().add(s);
        }

        // Añadir centroides al scatter chart
        Map<String, double[]> centroids = clusteringController.getCentroidsVisualizationData(analysisId);
        XYChart.Series<Number, Number> centroidSeries = new XYChart.Series<>();
        centroidSeries.setName("Centroides");
        for (Map.Entry<String, double[]> entry : centroids.entrySet()) {
            double[] coords = entry.getValue();
            centroidSeries.getData().add(new XYChart.Data<>(coords[0], coords[1]));
        }
        
        silhouetteScatterChart.getData().add(centroidSeries);
        
        // Estilizar centroides con clase CSS especial
        Platform.runLater(() -> {
            for (XYChart.Data<Number, Number> d : centroidSeries.getData()) {
                if (d.getNode() != null) {
                    d.getNode().getStyleClass().add("centroid-symbol");
                }
            }
        });
        
        // Actualizar etiquetas de los ejes
        updateAxisLabels();
        
        // Configurar aspect ratio 1:1 para los ejes del scatter chart
        configureScatterAxisRanges();

        resultsContainer.setVisible(true);
        resultsContainer.setManaged(true);
    }

    /**
     * Actualiza las etiquetas de los ejes del scatter chart según los ComboBoxes seleccionados.
     * No se muestran etiquetas en los ejes para mantener el aspect ratio 1:1.
     */
    private void updateAxisLabels() {
        // Las etiquetas se muestran en los ComboBoxes, no en los ejes
        // para evitar asimetría en el área de ploteo
        scatterXAxis.setLabel(null);
        scatterYAxis.setLabel(null);
    }

    /**
     * Configura los rangos de los ejes del scatter chart para mantener aspect ratio 1:1.
     */
    private void configureScatterAxisRanges() {
        if (silhouetteScatterChart.getData().isEmpty()) return;
        
        // Calcular min/max de todos los puntos
        double minX = Double.MAX_VALUE, maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE, maxY = Double.MIN_VALUE;
        
        for (XYChart.Series<Number, Number> series : silhouetteScatterChart.getData()) {
            for (XYChart.Data<Number, Number> data : series.getData()) {
                double x = data.getXValue().doubleValue();
                double y = data.getYValue().doubleValue();
                minX = Math.min(minX, x);
                maxX = Math.max(maxX, x);
                minY = Math.min(minY, y);
                maxY = Math.max(maxY, y);
            }
        }
        
        // Si no hay datos válidos, usar valores por defecto
        if (minX == Double.MAX_VALUE || maxX == Double.MIN_VALUE) {
            minX = 0; maxX = 10;
        }
        if (minY == Double.MAX_VALUE || maxY == Double.MIN_VALUE) {
            minY = 0; maxY = 10;
        }
        
        // Calcular el rango máximo y aplicarlo a ambos ejes
        double rangeX = maxX - minX;
        double rangeY = maxY - minY;
        double maxRange = Math.max(rangeX, rangeY);
        
        // Añadir padding del 10%
        double padding = maxRange * 0.1;
        if (padding < 0.5) padding = 0.5;
        
        // Centrar los datos
        double centerX = (minX + maxX) / 2;
        double centerY = (minY + maxY) / 2;
        
        // Calcular límites con mismo rango para ambos ejes
        double halfRange = (maxRange / 2) + padding;
        double newMin = Math.floor(Math.min(centerX - halfRange, centerY - halfRange));
        double newMax = Math.ceil(Math.max(centerX + halfRange, centerY + halfRange));
        
        // Asegurar valores enteros redondeados
        newMin = Math.floor(newMin);
        newMax = Math.ceil(newMax);
        
        // Aplicar a ambos ejes
        scatterXAxis.setAutoRanging(false);
        scatterYAxis.setAutoRanging(false);
        scatterXAxis.setLowerBound(newMin);
        scatterXAxis.setUpperBound(newMax);
        scatterYAxis.setLowerBound(newMin);
        scatterYAxis.setUpperBound(newMax);
        
        // Calcular tick unit apropiado
        double range = newMax - newMin;
        double tickUnit = Math.max(1, Math.ceil(range / 10));
        scatterXAxis.setTickUnit(tickUnit);
        scatterYAxis.setTickUnit(tickUnit);
    }

    /**
     * Actualiza el scatter chart cuando se cambian las dimensiones seleccionadas.
     */
    private void updateScatterChart() {
        if (currentAnalysisId == null || allQuestions == null || allQuestions.isEmpty()) {
            return;
        }
        
        // Obtener índices seleccionados
        int dimXIndex = -1;
        int dimYIndex = -1;
        
        String selectedX = xAxisCombo.getValue();
        String selectedY = yAxisCombo.getValue();
        
        for (Map.Entry<Integer, String> entry : allQuestions) {
            if (entry.getValue().equals(selectedX)) {
                dimXIndex = entry.getKey();
            }
            if (entry.getValue().equals(selectedY)) {
                dimYIndex = entry.getKey();
            }
        }
        
        if (dimXIndex == -1 || dimYIndex == -1) {
            return;
        }
        
        // Obtener nuevas coordenadas con las dimensiones seleccionadas
        Map<String, double[]> points = clusteringController.getClusterVisualizationDataWithDims(
            currentAnalysisId, surveyId, dimXIndex, dimYIndex);
        Map<String, double[]> centroids = clusteringController.getCentroidsVisualizationDataWithDims(
            currentAnalysisId, dimXIndex, dimYIndex);
        
        // Limpiar y reconstruir scatter chart
        silhouetteScatterChart.getData().clear();
        silhouetteScatterChart.getData().clear();
        
        // Obtener información de los clusters para reconstruir las series
        java.util.Map<String, XYChart.Series<Number, Number>> newScatterSeriesMap = new java.util.LinkedHashMap<>();
        
        try {
            File tempFile = File.createTempFile("clustering_temp_", ".txt");
            clusteringController.exportarAnalisis(currentAnalysisId, tempFile.getAbsolutePath());
            String results = java.nio.file.Files.readString(tempFile.toPath());
            tempFile.delete();
            
            String[] lines = results.split("\n");
            String currentClusterId = "";
            
            for (String line : lines) {
                line = line.trim();
                if (line.startsWith("Cluster ") && line.contains("(Size:")) {
                    int sizeIndex = line.indexOf("(Size:");
                    currentClusterId = line.substring(0, sizeIndex).trim();
                    // Simplificar etiqueta: "Cluster cluster_1" -> "Cluster 1"
                    String displayLabel = currentClusterId.replaceAll("cluster_", "");
                    XYChart.Series<Number, Number> series = new XYChart.Series<>();
                    series.setName(displayLabel);
                    newScatterSeriesMap.put(currentClusterId, series);
                } else if (line.startsWith("-") && !currentClusterId.isEmpty()) {
                    String responseId = line.substring(1).trim();
                    if (points.containsKey(responseId) && newScatterSeriesMap.containsKey(currentClusterId)) {
                        double[] coords = points.get(responseId);
                        newScatterSeriesMap.get(currentClusterId).getData().add(new XYChart.Data<>(coords[0], coords[1]));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        
        // Añadir series al chart - colores asignados automáticamente via CSS
        for (XYChart.Series<Number, Number> s : newScatterSeriesMap.values()) {
            silhouetteScatterChart.getData().add(s);
        }
        
        // Añadir centroides
        XYChart.Series<Number, Number> centroidSeries = new XYChart.Series<>();
        centroidSeries.setName("Centroides");
        for (Map.Entry<String, double[]> entry : centroids.entrySet()) {
            double[] coords = entry.getValue();
            centroidSeries.getData().add(new XYChart.Data<>(coords[0], coords[1]));
        }
        silhouetteScatterChart.getData().add(centroidSeries);
        
        // Estilizar centroides con clase CSS
        Platform.runLater(() -> {
            for (XYChart.Data<Number, Number> d : centroidSeries.getData()) {
                if (d.getNode() != null) {
                    d.getNode().getStyleClass().add("centroid-symbol");
                }
            }
        });
        
        updateAxisLabels();
        configureScatterAxisRanges();
    }

    @FXML
    public void exportCSV(ActionEvent event) {
        if (currentAnalysisId == null) {
            showAlert("Aviso", "No hay resultados para exportar. Ejecuta un análisis primero.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar CSV de Resultados");
        fileChooser.setInitialFileName("clustering_results_" + currentAnalysisId + ".csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        File file = fileChooser.showSaveDialog(sceneManager.getPrimaryStage());

        if (file != null) {
            try {
                exportToCSV(file);
                showAlert("Éxito", "CSV exportado correctamente.");
            } catch (IOException e) {
                showAlert("Error", "Error al exportar: " + e.getMessage());
            }
        }
    }

    private void exportToCSV(File csvFile) throws IOException {
        // Obtener datos del análisis en formato para CSV
        @SuppressWarnings("unchecked")
        Map<String, Object> csvData = clusteringController.getCSVExportData(currentAnalysisId, surveyId);
        
        @SuppressWarnings("unchecked")
        List<String> headers = (List<String>) csvData.get("headers");
        @SuppressWarnings("unchecked")
        List<List<String>> rows = (List<List<String>>) csvData.get("rows");

        try (FileWriter writer = new FileWriter(csvFile)) {
            // Escribir encabezado CSV (envolver cada campo en comillas)
            writer.write(headers.stream()
                .map(h -> "\"" + h.replace("\"", "\"\"") + "\"")
                .collect(java.util.stream.Collectors.joining(",")) + "\n");

            // Escribir filas de datos (envolver cada campo en comillas)
            for (List<String> row : rows) {
                writer.write(row.stream()
                    .map(field -> "\"" + field.replace("\"", "\"\"") + "\"")
                    .collect(java.util.stream.Collectors.joining(",")) + "\n");
            }
        }
    }

    @FXML
    public void exportBarChartPNG(ActionEvent event) {
        if (currentAnalysisId == null || sizeBarChart == null) {
            showAlert("Aviso", "No hay gráfica para exportar.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Gráfica de Tamaños");
        fileChooser.setInitialFileName("cluster_sizes_" + currentAnalysisId + ".png");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG", "*.png"));
        File file = fileChooser.showSaveDialog(sceneManager.getPrimaryStage());

        if (file != null) {
            try {
                WritableImage image = sizeBarChart.snapshot(new SnapshotParameters(), null);
                saveWritableImageAsPNG(image, file);
                showAlert("Éxito", "Gráfica exportada correctamente.");
            } catch (IOException e) {
                showAlert("Error", "Error al exportar: " + e.getMessage());
            }
        }
    }

    @FXML
    public void exportScatterChartPNG(ActionEvent event) {
        if (currentAnalysisId == null || silhouetteScatterChart == null) {
            showAlert("Aviso", "No hay gráfica para exportar.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Gráfica de Distribución");
        fileChooser.setInitialFileName("cluster_distribution_" + currentAnalysisId + ".png");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG", "*.png"));
        File file = fileChooser.showSaveDialog(sceneManager.getPrimaryStage());

        if (file != null) {
            try {
                WritableImage image = silhouetteScatterChart.snapshot(new SnapshotParameters(), null);
                saveWritableImageAsPNG(image, file);
                showAlert("Éxito", "Gráfica exportada correctamente.");
            } catch (IOException e) {
                showAlert("Error", "Error al exportar: " + e.getMessage());
            }
        }
    }

    private void saveWritableImageAsPNG(WritableImage image, File file) throws IOException {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        
        javafx.scene.image.PixelReader pixelReader = image.getPixelReader();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                bufferedImage.setRGB(x, y, pixelReader.getArgb(x, y));
            }
        }
        
        ImageIO.write(bufferedImage, "png", file);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Crea un pequeño contenedor con una métrica formateada.
     */
    private VBox createMetricBox(String label, String value) {
        VBox box = new VBox(2);
        box.getStyleClass().add("cluster-metric-item");
        
        Label valueLabel = new Label(formatMetricValue(value));
        valueLabel.getStyleClass().add("metric-value-small");
        
        Label nameLabel = new Label(label);
        nameLabel.getStyleClass().add("metric-name-small");
        
        box.getChildren().addAll(valueLabel, nameLabel);
        return box;
    }

    /**
     * Formatea un valor numérico a 4 decimales.
     */
    private String formatMetricValue(String value) {
        try {
            double d = Double.parseDouble(value.replace(",", "."));
            return String.format("%.4f", d);
        } catch (NumberFormatException e) {
            return value;
        }
    }

    /**
     * Formatea la representación del centroide mostrando cada componente en una línea separada.
     * Formato de entrada: "Centroid(components=[valor1, valor2, [array], texto])"
     */
    private String formatCentroid(String centroidStr) {
        try {
            // Buscar el contenido entre corchetes principales
            int start = centroidStr.indexOf("[");
            int end = centroidStr.lastIndexOf("]");
            if (start == -1 || end == -1) return centroidStr;
            
            String content = centroidStr.substring(start + 1, end);
            
            // Parsear los componentes manualmente (pueden contener arrays anidados)
            java.util.List<String> components = parseComponents(content);
            
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < components.size(); i++) {
                String comp = components.get(i).trim();
                result.append("Pregunta ").append(i + 1).append(": ");
                result.append(formatComponent(comp));
                if (i < components.size() - 1) {
                    result.append("\n");
                }
            }
            return result.toString();
        } catch (Exception e) {
            return centroidStr;
        }
    }
    
    /**
     * Parsea los componentes del centroide, manejando arrays anidados.
     */
    private java.util.List<String> parseComponents(String content) {
        java.util.List<String> components = new java.util.ArrayList<>();
        int depth = 0;
        StringBuilder current = new StringBuilder();
        
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '[') {
                depth++;
                current.append(c);
            } else if (c == ']') {
                depth--;
                current.append(c);
            } else if (c == ',' && depth == 0) {
                components.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        if (current.length() > 0) {
            components.add(current.toString().trim());
        }
        return components;
    }
    
    /**
     * Formatea un componente individual del centroide.
     */
    private String formatComponent(String comp) {
        // Si es un array
        if (comp.startsWith("[") && comp.endsWith("]")) {
            String inner = comp.substring(1, comp.length() - 1);
            String[] parts = inner.split(",");
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < parts.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append(formatNumber(parts[i].trim()));
            }
            sb.append("]");
            return sb.toString();
        }
        
        // Intentar como número
        try {
            double d = Double.parseDouble(comp);
            return String.format("%.4f", d);
        } catch (NumberFormatException e) {
            // Es texto
            return "\"" + comp + "\"";
        }
    }
    
    /**
     * Formatea un número a 4 decimales.
     */
    private String formatNumber(String numStr) {
        try {
            double d = Double.parseDouble(numStr);
            return String.format("%.4f", d);
        } catch (NumberFormatException e) {
            return numStr;
        }
    }

    // Navegación Sidebar
    @FXML public void goToHome(ActionEvent event) { sceneManager.showHome(); }
    @FXML public void goToMySurveys(ActionEvent event) { sceneManager.showMySurveys(); }
    @FXML public void goToMyDrafts(ActionEvent event) { sceneManager.showMySurveysDrafts(); }
    @FXML public void goToCreateSurvey(ActionEvent event) { sceneManager.showCreateSurvey(); }
    @FXML public void handleLogout(ActionEvent event) { 
        userController.logoutUser();
        sceneManager.showLogin(); 
    }
    @FXML public void handleDeleteAccount(ActionEvent event) { sceneManager.showDeleteAccountConfirm(); }
}
