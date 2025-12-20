package presentation.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import domain.controller.ResponseController;
import domain.controller.SurveyController;
import domain.controller.UserController;
import domain.model.Answer;
import domain.model.MultipleChoiceAnswer;
import domain.model.MultipleChoiceQuestion;
import domain.model.NumericalAnswer;
import domain.model.Question;
import domain.model.Response;
import domain.model.Survey;
import domain.model.TextualAnswer;
import domain.model.enums.TypeQuestion;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class AnswerSurveyViewController {

    @FXML private Label surveyTitle;
    @FXML private VBox questionsBox;

    @FXML private Label usernameLabel;
    @FXML private Label avatarLabel;

    private Survey survey;
    private Response currentResponse;
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
        
        // Cargar o crear DRAFT de esta encuesta
        this.currentResponse = responseController.startResponse(survey.getSURVEY_ID());
        
        renderQuestions();
    }

    private void renderQuestions() {
        questionsBox.getChildren().clear();

        for (Question q : survey.getQuestions()) {

            VBox card = new VBox(8);
            card.getStyleClass().add("question-card");

            Label qText = new Label(q.getQuestionText());
            qText.getStyleClass().add("question-title");
            card.getChildren().add(qText);

            int qIndex = q.getQuestionIndex();

            // Recuperamos la respuesta si existe (draft)
            Answer existingAnswer = currentResponse.getAnswer(qIndex);

            switch (q.getTypeQuestion()) {

                // ---------------- TEXTUAL ----------------
                case TEXTUAL -> {
                    TextArea area = new TextArea();
                    area.setPromptText("Escribe tu respuesta...");
                    area.setWrapText(true);

                    // 🟢 Cargar draft
                    if (existingAnswer instanceof TextualAnswer ta && ta.getIsAnswered()) {
                        area.setText(ta.getAnswerText());
                        answersMap.put(qIndex, ta.getAnswerText());
                    }

                    area.textProperty().addListener((obs, old, val) ->
                            answersMap.put(qIndex, val)
                    );

                    card.getChildren().add(area);
                }

                // ---------------- NUMERIC ----------------
                case NUMERICAL -> {
                    TextField numberField = new TextField();
                    numberField.setPromptText("Introduce un número");

                    // 🟢 Cargar draft
                    if (existingAnswer instanceof NumericalAnswer na && na.getIsAnswered()) {
                        numberField.setText(String.valueOf(na.getAnswerNum()));
                        answersMap.put(qIndex, na.getAnswerNum());
                    }

                    numberField.textProperty().addListener((obs, old, val) -> {
                        if (val.matches("-?\\d*(\\.\\d+)?")) {
                            answersMap.put(qIndex, val);
                        }
                    });

                    card.getChildren().add(numberField);
                }

                // ------------- MULTIPLE CHOICE -------------
                case MULTIPLE_CHOICE -> {
                    MultipleChoiceQuestion mcq = (MultipleChoiceQuestion) q;
                    VBox optionsBox = new VBox(5);

                    List<Integer> selectedIndexes = new ArrayList<>();
                    answersMap.put(qIndex, selectedIndexes);

                    boolean[] selectedFromDraft = null;

                    // 🟢 Cargar draft
                    if (existingAnswer instanceof MultipleChoiceAnswer ma && ma.getIsAnswered()) {
                        selectedFromDraft = ma.getSelectedOptions();
                    }

                    for (int i = 0; i < mcq.getOptionsSize(); i++) {
                        CheckBox cb = new CheckBox(mcq.getOptions().get(i).getOptionText());
                        int optionIndex = i;

                        // Marcar seleccionadas del draft
                        // Verificar que el índice exista en el array del draft (por compatibilidad con cambios en opciones)
                        if (selectedFromDraft != null && i < selectedFromDraft.length && selectedFromDraft[i]) {
                            cb.setSelected(true);
                            selectedIndexes.add(optionIndex);
                        }

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
            // 1 — Usar la respuesta DRAFT cargada en loadSurvey
            String responseId = currentResponse.getRESPONSE_ID();

            // 2 — Procesar preguntas
            for (Question q : survey.getQuestions()) {

                int qIndex = q.getQuestionIndex();
                Object raw = answersMap.get(qIndex);

                // Validación REQUERIDA
                if (raw == null || raw.toString().isBlank()) {
                    if (q.isRequired())
                        throw new RuntimeException("Debes responder la pregunta: " + q.getQuestionText());
                    else
                        continue; // no requerida → saltamos
                }

                // 3 — Enviar según tipo
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
                        List<Integer> selected = (List<Integer>) raw;

                        if (selected.isEmpty() && q.isRequired())
                            throw new RuntimeException("Debes seleccionar alguna opción en: " + q.getQuestionText());

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

            // 4 — Incrementar contador

             //  5 — MOSTRAR POPUP DE RATING
            sceneManager.showRatingPopup(rating -> {

                // Guardar rating en la encuesta
                surveyController.addRating(survey.getSURVEY_ID(), rating);

                responseController.incrementResponseCount(survey.getSURVEY_ID());
                responseController.publishResponse(survey.getSURVEY_ID(), responseId);

                // Volver al home
                sceneManager.showHome();
            });

        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    /**
     * Guarda las respuestas actuales en el DRAFT y vuelve al home.
     * No requiere validación ni rating.
     */
    @FXML
    private void handleSave(ActionEvent event) {
        try {
            // 1 — Usar la respuesta DRAFT cargada en loadSurvey
            String responseId = currentResponse.getRESPONSE_ID();

            // 2 — Procesar y guardar preguntas
            for (Question q : survey.getQuestions()) {
                int qIndex = q.getQuestionIndex();
                Object raw = answersMap.get(qIndex);

                // Solo guardar si hay algo respondido
                if (raw == null) {
                    continue;
                }

                // 3 — Enviar según tipo
                switch (q.getTypeQuestion()) {
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

                    case MULTIPLE_CHOICE -> {
                        List<Integer> selected = (List<Integer>) raw;
                        if (!selected.isEmpty()) {
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
            }

            // Las respuestas se han guardado en el DRAFT
            // Volver al home sin necesidad de rating
            sceneManager.showHome();

        } catch (Exception e) {
            showError(e.getMessage());
        }
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
