package presentation.views;

import domain.controller.ResponseController;
import domain.controller.UserController;
import domain.controller.SurveyController;
import domain.model.*;
import domain.model.enums.TypeQuestion;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;

import java.util.*;
import java.util.stream.Collectors;

public class AnswerSurveyViewController {

    @FXML private Label surveyTitle;
    @FXML private VBox questionsBox;

    @FXML private Label usernameLabel;
    @FXML private Label avatarLabel;

    private Survey survey;
    private final ResponseController responseController;
    private final UserController userController;
    private final SurveyController surveyController;
    private final SceneManager sceneManager;

    // Guarda answers: questionIndex -> valor
    private final Map<Integer, Object> answersMap = new HashMap<>();

    public AnswerSurveyViewController(ResponseController responseController,
                                      SceneManager sceneManager, UserController userController, SurveyController surveyController) {
        this.responseController = responseController;
        this.sceneManager = sceneManager;
        this.userController = userController;
        this.surveyController = surveyController;
    }

    public void loadSurvey(Survey survey) {
        this.survey = survey;
        usernameLabel.setText(userController.getUsernameLoggedIn());
        avatarLabel.setText(userController.getUsernameLoggedIn().substring(0,1).toUpperCase());
        surveyTitle.setText("Encuesta: " + survey.getTitle());
        renderQuestions();
    }

    /** Renderizado dinámico de todas las preguntas */
    private void renderQuestions() {
        questionsBox.getChildren().clear();

        for (Question q : survey.getQuestions()) {

            VBox card = new VBox(8);
            card.getStyleClass().add("question-card");

            Label qText = new Label(q.getQuestionText());
            qText.getStyleClass().add("question-title");
            card.getChildren().add(qText);

            int qIndex = q.getQuestionIndex();

            switch (q.getTypeQuestion()) {

                // ---------------- TEXTUAL ----------------
                case TEXTUAL -> {
                    TextArea area = new TextArea();
                    area.setPromptText("Escribe tu respuesta...");
                    area.setWrapText(true);

                    area.textProperty().addListener((obs, old, val) ->
                            answersMap.put(qIndex, val)
                    );

                    card.getChildren().add(area);
                }

                // ---------------- NUMERIC ----------------
                case NUMERICAL -> {
                    TextField numberField = new TextField();
                    numberField.setPromptText("Introduce un número");

                    // Force the field to be numeric only
                    TextFormatter<String> formatter = new TextFormatter<>(change -> {
                        String newText = change.getControlNewText();
                        if (newText.matches("-?(\\d*|\\d+\\.\\d*)?")) {
                            return change;
                        }
                        return null;
                    });

                    numberField.setTextFormatter(formatter);

                    numberField.textProperty().addListener((obs, old, val) -> {
                        // We can be sure it's a valid number or empty
                        answersMap.put(qIndex, val);
                    });

                    card.getChildren().add(numberField);
                }

                // ------------- MULTIPLE CHOICE -------------
                case MULTIPLE_CHOICE -> {
                    MultipleChoiceQuestion mcq = (MultipleChoiceQuestion) q;

                    VBox optionsBox = new VBox(5);

                    List<Integer> selectedIndexes = new ArrayList<>();
                    answersMap.put(qIndex, selectedIndexes);

                    for (int i = 0; i < mcq.getOptionsSize(); i++) {
                        CheckBox cb = new CheckBox(mcq.getOptions().get(i).getOptionText());
                        int optionIndex = i;

                        cb.selectedProperty().addListener((obs, old, val) -> {
                            if (val) selectedIndexes.add(optionIndex);
                            else selectedIndexes.remove(Integer.valueOf(optionIndex));
                        });

                        optionsBox.getChildren().add(cb);
                    }

                    card.getChildren().add(optionsBox);
                }
            }

            questionsBox.getChildren().add(card);
        }
    }

    /** Manejar envío de encuesta */
    @FXML
    private void handleSubmit(ActionEvent event) {

        try {
            // 1 — VALIDAR PRIMERO todas las preguntas obligatorias ANTES de crear la respuesta
            for (Question q : survey.getQuestions()) {
                if (!q.isRequired()) continue; // Solo validar obligatorias

                int qIndex = q.getQuestionIndex();
                Object raw = answersMap.get(qIndex);

                boolean isEmpty = isAnswerEmpty(raw);

                if (isEmpty) {
                    throw new RuntimeException("Debes responder la pregunta obligatoria: " + q.getQuestionText());
                }
            }

            // 2 — Crear nueva respuesta (solo después de validar)
            String responseId = responseController.startResponse(survey.getSURVEY_ID());

            // 3 — Procesar y guardar cada pregunta
            for (Question q : survey.getQuestions()) {

                int qIndex = q.getQuestionIndex();
                Object raw = answersMap.get(qIndex);

                // Si la respuesta está vacía, saltar (ya validamos las obligatorias arriba)
                if (isAnswerEmpty(raw)) {
                    continue;
                }

                // Enviar según tipo
                switch (q.getTypeQuestion()) {

                    // -------- TEXTUAL --------
                    case TEXTUAL -> {
                        String text = raw.toString();
                        responseController.updateAnswer(
                                survey.getSURVEY_ID(),
                                responseId,
                                qIndex,
                                text,
                                TypeQuestion.TEXTUAL
                        );
                    }

                    // -------- NUMERIC --------
                    case NUMERICAL -> {
                        String numberStr = raw.toString();
                        double num = Double.parseDouble(numberStr);

                        responseController.updateAnswer(
                                survey.getSURVEY_ID(),
                                responseId,
                                qIndex,
                                num
                        );
                    }

                    // -------- MULTIPLE CHOICE --------
                    case MULTIPLE_CHOICE -> {
                        @SuppressWarnings("unchecked")
                        List<Integer> selected = (List<Integer>) raw;

                        // Convertimos [0,2,3] → "0 2 3"
                        String joined = selected.stream()
                                .map(String::valueOf)
                                .collect(Collectors.joining(" "));

                        responseController.updateAnswer(
                                survey.getSURVEY_ID(),
                                responseId,
                                qIndex,
                                joined,
                                TypeQuestion.MULTIPLE_CHOICE
                        );
                    }
                }
            }

            // 4 — Marcar respuesta como enviada (establece fecha y estado)
            responseController.submitResponse(survey.getSURVEY_ID(), responseId);

            // 5 — Incrementar contador
            responseController.incrementResponseCount(survey.getSURVEY_ID());

             //  6 — MOSTRAR POPUP DE RATING
            sceneManager.showRatingPopup(rating -> {

                // Guardar rating en la encuesta
                surveyController.addRating(survey.getSURVEY_ID(), rating);

                // Volver al home
                sceneManager.showHome();
            });

        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    /**
     * Determina si una respuesta está vacía según su tipo.
     */
    private boolean isAnswerEmpty(Object raw) {
        if (raw == null) {
            return true;
        }
        if (raw instanceof String) {
            return ((String) raw).isBlank();
        }
        if (raw instanceof List) {
            return ((List<?>) raw).isEmpty();
        }
        return false;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error al enviar");
        alert.setHeaderText("No se pudo enviar la encuesta");
        alert.setContentText(message);
        alert.showAndWait();
    }

   @FXML
    public void goToHome(ActionEvent e) {
        sceneManager.showConfirmLeave(() -> sceneManager.showHome());
    }

    @FXML
    private void goToMySurveys(ActionEvent e) { sceneManager.showConfirmLeave(() -> sceneManager.showMySurveys()); }

    @FXML
    private void goToMyDrafts(ActionEvent e) { sceneManager.showConfirmLeave(() -> sceneManager.showMySurveysDrafts()); }

    @FXML
    private void goToCreateSurvey(ActionEvent e) { sceneManager.showConfirmLeave(() -> sceneManager.showCreateSurvey()); }

    @FXML
    public void handleLogout(ActionEvent e) {
        sceneManager.showConfirmLeave(() -> {
            userController.logoutUser();
            sceneManager.showLogin();
        });
    }
}
