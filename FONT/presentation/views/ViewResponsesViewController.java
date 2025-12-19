package presentation.views;

import domain.controller.ResponseController;
import domain.controller.SurveyController;
import domain.controller.UserController;
import domain.model.*;
import domain.model.enums.TypeQuestion;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlador para la vista de visualización de respuestas de una encuesta.
 * Muestra todas las respuestas agrupadas por pregunta con el usuario que respondió.
 */
public class ViewResponsesViewController implements Initializable {

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
    @FXML private Label responsesCountLabel;

    // --- CAMPOS FXML CONTENIDO ---
    @FXML private VBox responsesContainer;
    @FXML private ScrollPane scrollPane;

    // MODELO DE DATOS
    private Survey currentSurvey;
    private List<Response> responses;

    public ViewResponsesViewController(UserController userController, SurveyController surveyController,
                                       ResponseController responseController, SceneManager sceneManager,
                                       String surveyId) {
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

        // Cargar datos
        loadSurveyAndResponses();
    }

    /**
     * Carga la encuesta y sus respuestas desde la base de datos.
     */
    private void loadSurveyAndResponses() {
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
                        "No tienes permisos para ver las respuestas de esta encuesta.");
                sceneManager.showMySurveys();
                return;
            }

            // Llenar campos del header
            surveyTitleLabel.setText(currentSurvey.getTitle());

            // Cargar respuestas
            responses = responseController.getAllResponses(surveyId);
            responsesCountLabel.setText(String.valueOf(responses.size()));

            // Renderizar respuestas
            renderResponses();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Error al cargar las respuestas: " + e.getMessage());
            e.printStackTrace();
            sceneManager.showMySurveys();
        }
    }

    // =========================================
    // RENDERIZADO DE RESPUESTAS
    // =========================================

    private void renderResponses() {
        responsesContainer.getChildren().clear();

        if (responses == null || responses.isEmpty()) {
            Label empty = new Label("Esta encuesta no tiene respuestas todavía.");
            empty.setStyle("-fx-text-fill: #888; -fx-font-style: italic; -fx-font-size: 16px;");
            responsesContainer.getChildren().add(empty);
            return;
        }

        List<Question> questions = currentSurvey.getQuestions();

        // Para cada pregunta, mostrar todas las respuestas
        for (int qIndex = 0; qIndex < questions.size(); qIndex++) {
            Question question = questions.get(qIndex);
            VBox questionSection = createQuestionSection(question, qIndex);
            responsesContainer.getChildren().add(questionSection);
        }
    }

    private VBox createQuestionSection(Question question, int questionIndex) {
        VBox section = new VBox();
        section.getStyleClass().add("response-question-section");
        section.setSpacing(15);

        // --- HEADER DE LA PREGUNTA ---
        VBox questionHeader = new VBox(5);
        questionHeader.getStyleClass().add("response-question-header");

        HBox headerRow = new HBox(15);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Label numberLabel = new Label("PREGUNTA " + (questionIndex + 1));
        numberLabel.getStyleClass().add("question-number");

        Label typeLabel = new Label(getTypeText(question.getTypeQuestion()));
        typeLabel.getStyleClass().add("question-type-badge");

        headerRow.getChildren().addAll(numberLabel, typeLabel);

        Label questionText = new Label(question.getQuestionText());
        questionText.getStyleClass().add("question-text");
        questionText.setWrapText(true);

        questionHeader.getChildren().addAll(headerRow, questionText);
        section.getChildren().add(questionHeader);

        // --- RESPUESTAS DE LOS USUARIOS EN DESPLEGABLE ---
        VBox answersBox = new VBox(10);
        answersBox.getStyleClass().add("answers-container");
        answersBox.setPadding(new Insets(10, 15, 10, 15));

        for (Response response : responses) {
            Answer[] answers = response.getANSWERS();
            if (questionIndex < answers.length) {
                Answer answer = answers[questionIndex];
                HBox answerRow = createAnswerRow(response.getResponderUsername(), answer, question);
                answersBox.getChildren().add(answerRow);
            }
        }

        if (answersBox.getChildren().isEmpty()) {
            Label noAnswers = new Label("Sin respuestas para esta pregunta.");
            noAnswers.setStyle("-fx-text-fill: #999; -fx-font-style: italic;");
            answersBox.getChildren().add(noAnswers);
        }

        // Crear TitledPane (desplegable) para las respuestas
        int responseCount = (int) responses.stream()
                .filter(r -> questionIndex < r.getANSWERS().length)
                .count();
        TitledPane titledPane = new TitledPane("Respuestas (" + responseCount + ")", answersBox);
        titledPane.setExpanded(false);
        titledPane.getStyleClass().add("responses-dropdown");
        titledPane.setAnimated(true);

        section.getChildren().add(titledPane);

        return section;
    }

    private HBox createAnswerRow(String username, Answer answer, Question question) {
        HBox row = new HBox(15);
        row.getStyleClass().add("answer-row");
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 15, 10, 15));

        // Avatar/inicial del usuario
        Label avatar = new Label(getInitialLetters(username));
        avatar.getStyleClass().add("answer-avatar");

        // Contenedor de info
        VBox infoBox = new VBox(3);
        infoBox.setAlignment(Pos.CENTER_LEFT);

        Label usernameLabel = new Label(username);
        usernameLabel.getStyleClass().add("answer-username");

        Label answerLabel = new Label(formatAnswer(answer, question));
        answerLabel.getStyleClass().add("answer-text");
        answerLabel.setWrapText(true);

        infoBox.getChildren().addAll(usernameLabel, answerLabel);

        row.getChildren().addAll(avatar, infoBox);

        return row;
    }

    private String formatAnswer(Answer answer, Question question) {
        if (!answer.getIsAnswered()) {
            return "(sin respuesta)";
        }

        TypeQuestion type = answer.getTypeAnswer();

        if (type == TypeQuestion.TEXTUAL) {
            TextualAnswer ta = (TextualAnswer) answer;
            String text = ta.getAnswerText();
            return text != null && !text.isEmpty() ? text : "(sin respuesta)";
        } else if (type == TypeQuestion.NUMERICAL) {
            NumericalAnswer na = (NumericalAnswer) answer;
            Double num = na.getAnswerNum();
            return num != null ? String.valueOf(num) : "(sin respuesta)";
        } else if (type == TypeQuestion.MULTIPLE_CHOICE) {
            MultipleChoiceAnswer mca = (MultipleChoiceAnswer) answer;
            boolean[] selected = mca.getSelectedOptions();
            MultipleChoiceQuestion mcq = (MultipleChoiceQuestion) question;
            List<OptionQuestion> options = mcq.getOptions();

            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (int i = 0; i < selected.length && i < options.size(); i++) {
                if (selected[i]) {
                    if (!first) sb.append(", ");
                    sb.append(options.get(i).getOptionText());
                    first = false;
                }
            }
            return sb.length() > 0 ? sb.toString() : "(ninguna opción seleccionada)";
        }

        return "(tipo desconocido)";
    }

    private String getTypeText(TypeQuestion type) {
        switch (type) {
            case TEXTUAL: return "TEXTO";
            case NUMERICAL: return "NUMÉRICA";
            case MULTIPLE_CHOICE: return "OPCIÓN MÚLTIPLE";
            default: return "OTRO";
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
        sceneManager.showViewSurvey(surveyId);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

