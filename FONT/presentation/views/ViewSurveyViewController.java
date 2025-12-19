package presentation.views;

import domain.controller.SurveyController;
import domain.controller.UserController;
import domain.model.MultipleChoiceQuestion;
import domain.model.OptionQuestion;
import domain.model.Question;
import domain.model.Survey;
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

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * Controlador para la vista de visualización de encuestas publicadas.
 * Muestra los detalles de la encuesta, estadísticas y lista de preguntas en modo solo lectura.
 */
public class ViewSurveyViewController implements Initializable {

    private final UserController userController;
    private final SurveyController surveyController;
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
                                     SceneManager sceneManager, String surveyId) {
        this.userController = userController;
        this.surveyController = surveyController;
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

