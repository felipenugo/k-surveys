package presentation.views;

import domain.controller.ResponseController;
import domain.controller.SurveyController;
import domain.controller.UserController;
import domain.model.*;
import domain.model.enums.SurveyStatus;
import domain.model.enums.TypeQuestion;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlador para la vista de visualización de encuestas publicadas.
 * Muestra los detalles de la encuesta, estadísticas y lista de preguntas en modo solo lectura.
 */
public class ViewSurveyViewController implements Initializable {

    private final UserController userController;
    private final SurveyController surveyController;
    private final ResponseController responseController;
    private final SceneManager sceneManager;
    private final String surveyId;

    // --- CAMPOS FXML SIDEBAR ---
    @FXML private Label avatarLabel;
    @FXML private Label usernameLabel;
    @FXML private Button home;
    @FXML private Button createSurvey;
    @FXML private Button mySurveys;
    @FXML private Button myDrafts;

    // --- CAMPOS FXML HEADER ---
    @FXML private Label surveyTitleLabel;
    @FXML private Label surveyDescLabel;
    @FXML private Label statusBadge;
    @FXML private Label viewsCountLabel;
    @FXML private Label responsesCountLabel;
    @FXML private Label ratingLabel;
    @FXML private Label createdDateLabel;
    @FXML private Label publishedDateLabel;

    // --- CAMPOS FXML CONTENIDO ---
    @FXML private VBox questionsContainer;
    @FXML private ScrollPane scrollPane;

    // MODELO DE DATOS
    private Survey currentSurvey;

    public ViewSurveyViewController(UserController userController, SurveyController surveyController,
                                     ResponseController responseController, SceneManager sceneManager, String surveyId) {
        this.userController = userController;
        this.surveyController = surveyController;
        this.responseController = responseController;
        this.sceneManager = sceneManager;
        this.surveyId = surveyId;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Verificar login
        if (!userController.isLoggedIn()) {
            sceneManager.showLogin();
            return;
        }

        // Configurar sidebar
        String username = userController.getUsernameLoggedIn();
        usernameLabel.setText(username);
        avatarLabel.setText(getInitialLetters(username));
        setViewActive();

        // Configurar ScrollPane
        scrollPane.setFitToWidth(true);

        // Cargar la encuesta
        loadSurvey();
    }

    /**
     * Carga la encuesta desde la base de datos y llena los campos.
     */
    private void loadSurvey() {
        try {
            currentSurvey = surveyController.getSurvey(surveyId);

            if (currentSurvey == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "No se encontró la encuesta.");
                sceneManager.showMySurveys();
                return;
            }

            // Verificar que es del usuario actual
            String username = userController.getUsernameLoggedIn();
            if (!currentSurvey.getCREATOR_USERNAME().equals(username)) {
                showAlert(Alert.AlertType.ERROR, "Sin Permisos",
                        "No tienes permisos para ver esta encuesta.");
                sceneManager.showMySurveys();
                return;
            }

            // Llenar campos del header
            surveyTitleLabel.setText(currentSurvey.getTitle());
            surveyDescLabel.setText(currentSurvey.getDescription() != null && !currentSurvey.getDescription().isEmpty()
                    ? currentSurvey.getDescription()
                    : "Sin descripción");

            // Estado
            updateStatusBadge();

            // Estadísticas
            viewsCountLabel.setText(String.valueOf(currentSurvey.getViews()));
            responsesCountLabel.setText(String.valueOf(currentSurvey.getRatingCount()));
            ratingLabel.setText(String.format("%.1f", currentSurvey.getAvgRating()));

            // Fechas
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            if (currentSurvey.getCREATED_AT() != null) {
                createdDateLabel.setText(currentSurvey.getCREATED_AT().format(formatter));
            }
            if (currentSurvey.getPUBLISHED_AT() != null) {
                publishedDateLabel.setText(currentSurvey.getPUBLISHED_AT().format(formatter));
            }

            // Renderizar preguntas
            renderQuestions();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Error al cargar la encuesta: " + e.getMessage());
            e.printStackTrace();
            sceneManager.showMySurveys();
        }
    }

    private void updateStatusBadge() {
        if (currentSurvey.getSurveyStatus() == SurveyStatus.PUBLISHED) {
            statusBadge.setText("PUBLICADA");
            statusBadge.getStyleClass().clear();
            statusBadge.getStyleClass().add("status-badge-published");
        } else if (currentSurvey.getSurveyStatus() == SurveyStatus.CLOSED) {
            statusBadge.setText("CERRADA");
            statusBadge.getStyleClass().clear();
            statusBadge.getStyleClass().add("status-badge-closed");
        }
    }

    // =========================================
    // MÉTODOS AUXILIARES SIDEBAR
    // =========================================

    private String getInitialLetters(String username) {
        String[] parts = username.trim().split("\\s+");
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                result.append(part.substring(0, 1).toUpperCase());
            }
        }
        return !result.isEmpty() ? result.toString() : "U";
    }

    private void setViewActive() {
        mySurveys.getStyleClass().add("nav-btn-active");
    }

    // =========================================
    // NAVEGACIÓN SIDEBAR
    // =========================================

    @FXML
    public void goToHome(ActionEvent event) {
        sceneManager.showHome();
    }

    @FXML
    public void goToCreateSurvey(ActionEvent event) {
        sceneManager.showCreateSurvey();
    }

    @FXML
    public void goToMySurveys(ActionEvent event) {
        sceneManager.showMySurveys();
    }

    @FXML
    public void goToMyDrafts(ActionEvent event) {
        sceneManager.showMySurveysDrafts();
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        userController.logoutUser();
        sceneManager.showLogin();
    }

    // =========================================
    // ACCIONES PRINCIPALES
    // =========================================

    @FXML
    public void handleBack(ActionEvent event) {
        sceneManager.showMySurveys();
    }

    @FXML
    public void handleViewResponses(ActionEvent event) {
        sceneManager.showViewResponses(surveyId);
    }

    @FXML
    public void handleViewResponsesTable(ActionEvent event) {
        sceneManager.showResponsesTable(surveyId);
    }

    @FXML
    public void handleRunClustering(ActionEvent event) {
        int responseCount = sceneManager.getClusteringController().getResponseCount(surveyId);
        if (responseCount == 0) {
            showAlert(Alert.AlertType.WARNING, "Sin Respuestas", 
                "Esta encuesta no tiene respuestas.\nNecesita al menos 1 respuesta para ejecutar el análisis de clustering.");
            return;
        }
        sceneManager.showClustering(surveyId);
    }

    @FXML
    public void handleCloseSurvey(ActionEvent event) {
        if (currentSurvey.getSurveyStatus() == SurveyStatus.CLOSED) {
            showAlert(Alert.AlertType.WARNING, "Ya Cerrada", "Esta encuesta ya está cerrada.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar Cierre");
        confirm.setHeaderText("¿Cerrar esta encuesta?");
        confirm.setContentText("Una vez cerrada, la encuesta no aceptará más respuestas.\nEsta acción no se puede deshacer.");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try {
            // Usar el nuevo método closeSurvey
            currentSurvey = surveyController.closeSurvey(surveyId);

            updateStatusBadge();
            showAlert(Alert.AlertType.INFORMATION, "Encuesta Cerrada",
                    "La encuesta ha sido cerrada exitosamente.");

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "No se pudo cerrar la encuesta: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleExportCSV(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exportar Encuesta a CSV");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivos CSV", "*.csv"));
        
        // Nombre de archivo: Titulo-Descripcion.csv
        String title = currentSurvey.getTitle().replaceAll("[^a-zA-Z0-9áéíóúÁÉÍÓÚñÑ\\s]", "").trim().replaceAll("\\s+", "_");
        String description = currentSurvey.getDescription() != null 
                ? currentSurvey.getDescription().replaceAll("[^a-zA-Z0-9áéíóúÁÉÍÓÚñÑ\\s]", "").trim().replaceAll("\\s+", "_")
                : "";
        String fileName = title + (description.isEmpty() ? "" : "-" + description) + ".csv";
        fileChooser.setInitialFileName(fileName);
        
        File file = fileChooser.showSaveDialog(questionsContainer.getScene().getWindow());
        if (file == null) return;

        try {
            int responsesExported = exportSurveyToCSV(file);
            showAlert(Alert.AlertType.INFORMATION, "Exportación Exitosa",
                    "La encuesta se ha exportado correctamente a:\n" + file.getAbsolutePath() +
                    "\n\nRespuestas exportadas: " + responsesExported);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error de Exportación",
                    "No se pudo exportar el CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Exporta la encuesta a un archivo CSV con el formato:
     * Fila 1 (Question): Textos de las preguntas
     * Fila 2 (Type): Tipos de pregunta (TEXTUAL, NUMERICAL, MULTIPLE_CHOICE[op1|op2|...](min=X;max=Y))
     * Fila 3 (Required): Si cada pregunta es obligatoria (true/false)
     * Filas 4+: Respuestas existentes - primera columna es username/ID del respondedor
     * 
     * @return número de respuestas exportadas
     */
    private int exportSurveyToCSV(File file) throws IOException {
        List<Question> questions = currentSurvey.getQuestions();
        int responsesExported = 0;
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            // Fila 1: Question (textos de las preguntas)
            StringBuilder questionRow = new StringBuilder("Question");
            for (Question q : questions) {
                questionRow.append(",").append(escapeCSV(q.getQuestionText()));
            }
            writer.println(questionRow);

            // Fila 2: Type (tipos de pregunta con min/max para multiple choice)
            StringBuilder typeRow = new StringBuilder("Type");
            for (Question q : questions) {
                typeRow.append(",").append(getTypeString(q));
            }
            writer.println(typeRow);

            // Fila 3: Required (si cada pregunta es obligatoria)
            StringBuilder requiredRow = new StringBuilder("Required");
            for (Question q : questions) {
                requiredRow.append(",").append(q.isRequired() ? "true" : "false");
            }
            writer.println(requiredRow);

            // Filas 4+: Respuestas (si las hay)
            // Obtener todas las respuestas de la encuesta usando el ID de currentSurvey
            String currentSurveyId = currentSurvey.getSURVEY_ID();
            List<Response> responses = sceneManager.getClusteringController().getResponses(currentSurveyId);
            
            if (responses != null && !responses.isEmpty()) {
                for (Response response : responses) {
                    // Primera columna: username o ID del respondedor
                    String responderInfo = response.getResponderUsername();
                    if (responderInfo == null || responderInfo.isEmpty()) {
                        responderInfo = response.getRESPONSE_ID();
                    }
                    StringBuilder responseRow = new StringBuilder(escapeCSV(responderInfo));
                    
                    Answer[] answers = response.getANSWERS();
                    for (int i = 0; i < questions.size(); i++) {
                        String answerValue = "";
                        if (i < answers.length && answers[i] != null) {
                            answerValue = getAnswerValue(answers[i], questions.get(i));
                        }
                        responseRow.append(",").append(escapeCSV(answerValue));
                    }
                    writer.println(responseRow);
                    responsesExported++;
                }
            }
        }
        return responsesExported;
    }

    /**
     * Convierte el tipo de pregunta a string para CSV.
     * Formato: MULTIPLE_CHOICE[op1|op2|op3](min=1;max=2)
     */
    private String getTypeString(Question q) {
        if (q instanceof MultipleChoiceQuestion mcq) {
            StringBuilder sb = new StringBuilder("MULTIPLE_CHOICE[");
            List<OptionQuestion> options = mcq.getOptions();
            for (int i = 0; i < options.size(); i++) {
                if (i > 0) sb.append("|");
                sb.append(options.get(i).getOptionText());
            }
            sb.append("](min=").append(mcq.getMinSelections())
              .append(";max=").append(mcq.getMaxSelections()).append(")");
            return sb.toString();
        } else if (q.getTypeQuestion() == TypeQuestion.NUMERICAL) {
            return "NUMERICAL";
        } else {
            return "TEXTUAL";
        }
    }

    /**
     * Obtiene el valor de una respuesta como string.
     */
    private String getAnswerValue(Answer answer, Question question) {
        if (answer instanceof TextualAnswer ta) {
            return ta.getAnswerText() != null ? ta.getAnswerText() : "";
        } else if (answer instanceof NumericalAnswer na) {
            return na.getAnswerNum() != null ? String.valueOf(na.getAnswerNum()) : "";
        } else if (answer instanceof MultipleChoiceAnswer mca) {
            // Para opción múltiple, devolver los índices seleccionados separados por |
            boolean[] selections = mca.getSelectedOptions();
            if (selections == null) return "";
            
            StringBuilder sb = new StringBuilder();
            if (question instanceof MultipleChoiceQuestion mcq) {
                List<OptionQuestion> options = mcq.getOptions();
                for (int i = 0; i < selections.length && i < options.size(); i++) {
                    if (selections[i]) {
                        if (sb.length() > 0) sb.append("|");
                        sb.append(options.get(i).getOptionText());
                    }
                }
            }
            return sb.toString();
        }
        return "";
    }

    /**
     * Escapa un valor para CSV (maneja comas y comillas).
     */
    private String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("[") || value.contains("]")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // =========================================
    // RENDERIZADO DE PREGUNTAS (SOLO LECTURA)
    // =========================================

    private void renderQuestions() {
        questionsContainer.getChildren().clear();

        if (currentSurvey.getQuestions() == null || currentSurvey.getQuestions().isEmpty()) {
            Label empty = new Label("Esta encuesta no tiene preguntas.");
            empty.setStyle("-fx-text-fill: #888; -fx-font-style: italic;");
            questionsContainer.getChildren().add(empty);
            return;
        }

        for (int i = 0; i < currentSurvey.getQuestions().size(); i++) {
            Question q = currentSurvey.getQuestions().get(i);
            VBox card = createQuestionCard(q, i);
            questionsContainer.getChildren().add(card);
        }
    }

    private VBox createQuestionCard(Question q, int index) {
        VBox card = new VBox();
        card.getStyleClass().add("question-view-card");
        card.setSpacing(10);

        // --- HEADER ---
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        // Número de pregunta
        Label numberLabel = new Label("PREGUNTA " + (index + 1));
        numberLabel.getStyleClass().add("question-number");

        // Tipo de pregunta
        Label typeLabel = new Label(getTypeText(q.getTypeQuestion()));
        typeLabel.getStyleClass().add("question-type-badge");

        // Badge obligatoria
        if (q.isRequired()) {
            Label requiredLabel = new Label("OBLIGATORIA");
            requiredLabel.getStyleClass().add("question-required-badge");
            header.getChildren().addAll(numberLabel, typeLabel, requiredLabel);
        } else {
            header.getChildren().addAll(numberLabel, typeLabel);
        }

        card.getChildren().add(header);

        // --- TEXTO DE LA PREGUNTA ---
        Label questionText = new Label(q.getQuestionText());
        questionText.getStyleClass().add("question-text");
        questionText.setWrapText(true);
        card.getChildren().add(questionText);

        // --- CONTENIDO SEGÚN TIPO ---
        if (q instanceof MultipleChoiceQuestion mcq) {
            VBox optionsBox = new VBox(5);
            optionsBox.setStyle("-fx-padding: 10 0 0 0;");

            for (OptionQuestion opt : mcq.getOptions()) {
                HBox optRow = new HBox(10);
                optRow.getStyleClass().add("option-view-row");
                optRow.setAlignment(Pos.CENTER_LEFT);

                Region bullet = new Region();
                bullet.getStyleClass().add("option-bullet");

                Label optText = new Label(opt.getOptionText());
                optText.getStyleClass().add("option-text");

                optRow.getChildren().addAll(bullet, optText);
                optionsBox.getChildren().add(optRow);
            }

            // Info de selecciones
            Label selectionInfo = new Label("Selecciones permitidas: " +
                    mcq.getMinSelections() + " - " + mcq.getMaxSelections());
            selectionInfo.getStyleClass().add("selection-info");
            optionsBox.getChildren().add(selectionInfo);

            card.getChildren().add(optionsBox);

        } else if (q.getTypeQuestion() == TypeQuestion.NUMERICAL) {
            Label placeholder = new Label("Respuesta numérica");
            placeholder.setStyle("-fx-text-fill: #888; -fx-font-style: italic; -fx-padding: 10 0 0 20;");
            card.getChildren().add(placeholder);

        } else {
            Label placeholder = new Label("Respuesta de texto libre");
            placeholder.setStyle("-fx-text-fill: #888; -fx-font-style: italic; -fx-padding: 10 0 0 20;");
            card.getChildren().add(placeholder);
        }

        return card;
    }

    private String getTypeText(TypeQuestion type) {
        return switch (type) {
            case TEXTUAL -> "TEXTO";
            case MULTIPLE_CHOICE -> "OPCIÓN MÚLTIPLE";
            case NUMERICAL -> "NUMÉRICA";
        };
    }
}

