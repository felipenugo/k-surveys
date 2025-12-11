package presentation.views;

import domain.controller.CtrlDominioClustering;
import domain.controller.SurveyController;
import domain.controller.UserController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
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

    @FXML private BarChart<String, Number> sizeBarChart;
    @FXML private ScatterChart<Number, Number> silhouetteScatterChart;
    @FXML private NumberAxis scatterXAxis;
    @FXML private NumberAxis scatterYAxis;
    @FXML private ComboBox<String> xAxisCombo;
    @FXML private ComboBox<String> yAxisCombo;

    // Colores compartidos para clusters (barras y puntos)
    private static final String[] CLUSTER_COLORS = {
        "#3498db", "#e74c3c", "#2ecc71", "#9b59b6", "#f39c12", "#1abc9c", "#e91e63", "#00bcd4"
    };

    private String currentAnalysisId;
    private List<Map.Entry<Integer, String>> numericalQuestions; // Para los selectores de dimensión
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
        mySurveys.getStyleClass().add("nav-btn-active");
        kField.setText("2"); // Valor por defecto
    }

    private void setupUserInfo() {
        String username = userController.getUsernameLoggedIn();
        usernameLabel.setText(username);
        if (username != null && !username.isEmpty()) {
            avatarLabel.setText(username.substring(0, 1).toUpperCase());
        }
    }

    private void setupCombos() {
        algorithmCombo.getItems().addAll("KMeans", "KMeansPlusPlus", "KMedoids");
        algorithmCombo.setValue("KMeans");

        distanceCombo.getItems().addAll("EUCLIDEAN", "MANHATTAN");
        distanceCombo.setValue("EUCLIDEAN");

        textDistanceCombo.getItems().addAll("LEVENSHTEIN", "EMBEDDING");
        textDistanceCombo.setValue("LEVENSHTEIN");
    }

    private void setupDimensionCombos() {
        // Obtener preguntas numéricas para los selectores de dimensión
        numericalQuestions = clusteringController.getNumericalQuestionLabels(surveyId);
        
        xAxisCombo.getItems().clear();
        yAxisCombo.getItems().clear();
        
        for (Map.Entry<Integer, String> entry : numericalQuestions) {
            xAxisCombo.getItems().add(entry.getValue());
            yAxisCombo.getItems().add(entry.getValue());
        }
        
        // Seleccionar por defecto las dos primeras si hay suficientes
        if (numericalQuestions.size() >= 2) {
            xAxisCombo.setValue(numericalQuestions.get(0).getValue());
            yAxisCombo.setValue(numericalQuestions.get(1).getValue());
        } else if (numericalQuestions.size() == 1) {
            xAxisCombo.setValue(numericalQuestions.get(0).getValue());
            yAxisCombo.setValue(numericalQuestions.get(0).getValue());
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
            String algorithm = algorithmCombo.getValue();
            String distance = distanceCombo.getValue();
            String textDistance = textDistanceCombo.getValue();
            int k = Integer.parseInt(kField.getText());
            int maxIter = Integer.parseInt(maxIterField.getText());
            double tolerance = Double.parseDouble(toleranceField.getText());

            currentAnalysisId = clusteringController.runAnalysis(algorithm, surveyId, k, maxIter, tolerance, distance, textDistance);
            
            // Visualizar resultados
            displayResults(currentAnalysisId);

        } catch (NumberFormatException e) {
            showAlert("Error", "Por favor, introduce valores numéricos válidos.");
        } catch (Exception e) {
            showAlert("Error", "Error al ejecutar el análisis: " + e.getMessage());
            e.printStackTrace();
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
                    currentClusterBox = new VBox(10);
                    currentClusterBox.getStyleClass().add("cluster-card");
                    
                    Label clusterTitle = new Label(line);
                    clusterTitle.getStyleClass().add("cluster-title");
                    currentClusterBox.getChildren().add(clusterTitle);
                    
                    clustersContainer.getChildren().add(currentClusterBox);

                    // Parse ID and Size for Charts
                    // Format: "Cluster cluster_1 (Size: 5)"
                    try {
                        int sizeIndex = line.indexOf("(Size:");
                        if (sizeIndex != -1) {
                            currentClusterId = line.substring(0, sizeIndex).trim();
                            String sizeStr = line.substring(sizeIndex + 6, line.indexOf(")")).trim();
                            int size = Integer.parseInt(sizeStr);
                            sizeSeries.getData().add(new XYChart.Data<>(currentClusterId, size));
                            
                            // Crear serie para scatter plot
                            XYChart.Series<Number, Number> series = new XYChart.Series<>();
                            series.setName(currentClusterId);
                            scatterSeriesMap.put(currentClusterId, series);
                        }
                    } catch (Exception e) {
                        System.err.println("Error parsing cluster size: " + e.getMessage());
                    }

                } else if (currentClusterBox != null) {
                    if (line.startsWith("Silhouette Score:")) {
                        Label scoreLabel = new Label(line);
                        scoreLabel.getStyleClass().add("cluster-metric");
                        currentClusterBox.getChildren().add(scoreLabel);
                    } else if (line.startsWith("Centroid:")) {
                         Label centroidLabel = new Label(line);
                         centroidLabel.getStyleClass().add("cluster-metric");
                         centroidLabel.setWrapText(true);
                         currentClusterBox.getChildren().add(centroidLabel);
                    } else if (line.startsWith("Member Response IDs:")) {
                        Label membersTitle = new Label("Miembros:");
                        membersTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 5 0 0 0;");
                        currentClusterBox.getChildren().add(membersTitle);
                    } else if (line.startsWith("-")) {
                        // Member ID
                        String responseId = line.substring(1).trim();
                        
                        // Obtener resumen de respuesta
                        String summary = clusteringController.getResponseSummary(surveyId, responseId);
                        Label memberLabel = new Label("ID: " + responseId + "\n" + summary);
                        memberLabel.getStyleClass().add("cluster-response-item");
                        memberLabel.setMaxWidth(Double.MAX_VALUE);
                        currentClusterBox.getChildren().add(memberLabel);

                        // Añadir punto al Scatter Chart
                        if (points.containsKey(responseId) && scatterSeriesMap.containsKey(currentClusterId)) {
                            double[] coords = points.get(responseId);
                            XYChart.Data<Number, Number> dataPoint = new XYChart.Data<>(coords[0], coords[1]);
                            // Añadir tooltip al punto
                            // Nota: Tooltip se instala después de añadir a la escena, pero aquí lo preparamos
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

        resultsContainer.setVisible(true);
        resultsContainer.setManaged(true);
    }

    /**
     * Actualiza las etiquetas de los ejes del scatter chart según los ComboBoxes seleccionados.
     */
    private void updateAxisLabels() {
        if (xAxisCombo.getValue() != null) {
            scatterXAxis.setLabel(xAxisCombo.getValue());
        }
        if (yAxisCombo.getValue() != null) {
            scatterYAxis.setLabel(yAxisCombo.getValue());
        }
    }

    /**
     * Actualiza el scatter chart cuando se cambian las dimensiones seleccionadas.
     */
    private void updateScatterChart() {
        if (currentAnalysisId == null || numericalQuestions == null || numericalQuestions.isEmpty()) {
            return;
        }
        
        // Obtener índices seleccionados
        int dimXIndex = -1;
        int dimYIndex = -1;
        
        String selectedX = xAxisCombo.getValue();
        String selectedY = yAxisCombo.getValue();
        
        for (Map.Entry<Integer, String> entry : numericalQuestions) {
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
                    XYChart.Series<Number, Number> series = new XYChart.Series<>();
                    series.setName(currentClusterId);
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
    }

    @FXML
    public void exportResults(ActionEvent event) {
        if (currentAnalysisId == null) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Resultados");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos de Texto", "*.txt"));
        File file = fileChooser.showSaveDialog(sceneManager.getPrimaryStage());

        if (file != null) {
            try {
                clusteringController.exportarAnalisis(currentAnalysisId, file.getAbsolutePath());
                showAlert("Éxito", "Resultados exportados correctamente.");
            } catch (IOException e) {
                showAlert("Error", "No se pudo guardar el archivo: " + e.getMessage());
            }
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
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
