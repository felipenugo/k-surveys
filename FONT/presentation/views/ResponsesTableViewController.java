package presentation.views;

import domain.controller.ResponseController;
import domain.controller.SurveyController;
import domain.controller.UserController;
import domain.model.*;
import domain.model.enums.TypeQuestion;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Popup;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Controlador para la vista de tabla de respuestas de una encuesta.
 * Muestra todas las respuestas en formato tabla con filtros integrados en columnas y ordenación.
 */
public class ResponsesTableViewController implements Initializable {

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
    @FXML private Label filteredCountLabel;
    @FXML private Label activeFiltersLabel;

    // --- CAMPOS FXML TABLA ---
    @FXML private TableView<ResponseRow> responsesTable;
    @FXML private ScrollPane scrollPane;

    // MODELO DE DATOS
    private Survey currentSurvey;
    private List<Response> responses;
    private ObservableList<ResponseRow> responseRows;
    private FilteredList<ResponseRow> filteredData;

    // Mapa de filtros por columna
    private Map<Integer, ColumnFilter> columnFilters;

    public ResponsesTableViewController(UserController userController, SurveyController surveyController,
                                        ResponseController responseController, SceneManager sceneManager,
                                        String surveyId) {
        this.userController = userController;
        this.surveyController = surveyController;
        this.responseController = responseController;
        this.sceneManager = sceneManager;
        this.surveyId = surveyId;
        this.columnFilters = new HashMap<>();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (!userController.isLoggedIn()) {
            sceneManager.showLogin();
            return;
        }

        String username = userController.getUsernameLoggedIn();
        usernameLabel.setText(username);
        avatarLabel.setText(getInitialLetters(username));
        setViewActive();

        scrollPane.setFitToWidth(true);
        responseRows = FXCollections.observableArrayList();

        loadSurveyAndResponses();
    }

    private void loadSurveyAndResponses() {
        try {
            currentSurvey = surveyController.getSurvey(surveyId);

            if (currentSurvey == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "No se encontró la encuesta.");
                sceneManager.showMySurveys();
                return;
            }

            String username = userController.getUsernameLoggedIn();
            if (!currentSurvey.getCREATOR_USERNAME().equals(username)) {
                showAlert(Alert.AlertType.ERROR, "Sin Permisos",
                        "No tienes permisos para ver las respuestas de esta encuesta.");
                sceneManager.showMySurveys();
                return;
            }

            surveyTitleLabel.setText(currentSurvey.getTitle());
            responses = responseController.getAllResponses(surveyId);
            responsesCountLabel.setText(String.valueOf(responses.size()));

            setupTable();
            loadResponsesIntoTable();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Error al cargar las respuestas: " + e.getMessage());
            e.printStackTrace();
            sceneManager.showMySurveys();
        }
    }

    private void setupTable() {
        responsesTable.getColumns().clear();
        columnFilters.clear();

        // Columna de Usuario (índice 0) - Filtro de texto
        TableColumn<ResponseRow, String> userCol = createColumnWithFilter(
                "Usuario", 0, FilterType.TEXT, null);
        userCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getUsername()));
        userCol.setMinWidth(130);
        responsesTable.getColumns().add(userCol);

        // Columna de Fecha (índice 1) - Filtro de texto
        TableColumn<ResponseRow, String> dateCol = createColumnWithFilter(
                "Fecha", 1, FilterType.TEXT, null);
        dateCol.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getSubmittedDate()));
        dateCol.setMinWidth(150);
        responsesTable.getColumns().add(dateCol);

        // Columnas para cada pregunta
        List<Question> questions = currentSurvey.getQuestions();
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            final int qIndex = i;
            
            FilterType filterType;
            if (q.getTypeQuestion() == TypeQuestion.NUMERICAL) {
                filterType = FilterType.NUMERIC_RANGE;
            } else if (q.getTypeQuestion() == TypeQuestion.MULTIPLE_CHOICE) {
                filterType = FilterType.MULTIPLE_CHOICE;
            } else {
                filterType = FilterType.TEXT;
            }

            String headerText = "P" + (i + 1) + ": " + q.getQuestionText();
            TableColumn<ResponseRow, String> qCol = createColumnWithFilter(
                    headerText, i + 2, filterType, q);

            qCol.setCellValueFactory(cellData -> {
                String answer = cellData.getValue().getAnswer(qIndex);
                return new SimpleStringProperty(answer);
            });

            // Calcular ancho mínimo basado en la longitud del título (aprox 7px por carácter + 50px para el icono de filtro)
            int minWidth = Math.max(200, headerText.length() * 7 + 50);
            qCol.setMinWidth(minWidth);
            qCol.setPrefWidth(minWidth);
            qCol.setCellFactory(column -> createTableCell());

            responsesTable.getColumns().add(qCol);
        }

        responsesTable.setPlaceholder(new Label("No hay respuestas para mostrar."));
    }

    /**
     * Crea una columna con filtro integrado en el encabezado.
     */
    private TableColumn<ResponseRow, String> createColumnWithFilter(String title, int colIndex, 
                                                                     FilterType filterType, Question question) {
        TableColumn<ResponseRow, String> column = new TableColumn<>();
        
        // Crear encabezado personalizado con icono de filtro
        HBox header = new HBox(5);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 5, 0, 5));
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px;");
        titleLabel.setWrapText(true);
        
        Button filterBtn = new Button("🔍");
        filterBtn.getStyleClass().add("filter-icon-btn");
        filterBtn.setOnAction(e -> showFilterPopup(filterBtn, colIndex, filterType, question, title));
        
        header.getChildren().addAll(titleLabel, filterBtn);
        column.setGraphic(header);
        column.setText("");
        column.setSortable(true);
        
        // Crear y guardar el filtro
        ColumnFilter filter = new ColumnFilter(colIndex, filterType, question);
        columnFilters.put(colIndex, filter);
        
        return column;
    }

    /**
     * Muestra el popup de filtro para una columna.
     */
    private void showFilterPopup(Button anchor, int colIndex, FilterType filterType, 
                                  Question question, String columnTitle) {
        ContextMenu popup = new ContextMenu();
        popup.getStyleClass().add("filter-popup");
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(15));
        content.setStyle("-fx-background-color: white;");
        content.setMinWidth(250);
        
        Label titleLabel = new Label("Filtrar: " + truncateText(columnTitle, 30));
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1e3c72;");
        content.getChildren().add(titleLabel);
        
        ColumnFilter filter = columnFilters.get(colIndex);
        
        switch (filterType) {
            case TEXT:
                createTextFilter(content, filter);
                break;
            case NUMERIC_RANGE:
                createNumericRangeFilter(content, filter);
                break;
            case MULTIPLE_CHOICE:
                createMultipleChoiceFilter(content, filter, question);
                break;
        }
        
        // Botones de acción
        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        buttons.setPadding(new Insets(10, 0, 0, 0));
        
        Button applyBtn = new Button("Aplicar");
        applyBtn.getStyleClass().add("filter-apply-btn");
        applyBtn.setOnAction(e -> {
            applyFilters();
            popup.hide();
        });
        
        Button clearBtn = new Button("Limpiar");
        clearBtn.getStyleClass().add("filter-clear-btn");
        clearBtn.setOnAction(e -> {
            filter.clear();
            applyFilters();
            popup.hide();
        });
        
        buttons.getChildren().addAll(clearBtn, applyBtn);
        content.getChildren().add(buttons);
        
        CustomMenuItem menuItem = new CustomMenuItem(content);
        menuItem.setHideOnClick(false);
        popup.getItems().add(menuItem);
        
        popup.show(anchor, Side.BOTTOM, 0, 0);
    }

    private void createTextFilter(VBox content, ColumnFilter filter) {
        TextField textField = new TextField();
        textField.setPromptText("Buscar texto...");
        textField.setText(filter.textValue != null ? filter.textValue : "");
        textField.getStyleClass().add("filter-text-field");
        
        textField.textProperty().addListener((obs, old, newVal) -> filter.textValue = newVal);
        
        content.getChildren().add(textField);
    }

    private void createNumericRangeFilter(VBox content, ColumnFilter filter) {
        Label infoLabel = new Label("Especifica un rango de valores:");
        infoLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
        
        HBox rangeBox = new HBox(10);
        rangeBox.setAlignment(Pos.CENTER_LEFT);
        
        TextField minField = new TextField();
        minField.setPromptText("Mín");
        minField.setPrefWidth(80);
        minField.getStyleClass().add("filter-text-field");
        if (filter.minValue != null) minField.setText(String.valueOf(filter.minValue));
        
        Label toLabel = new Label("a");
        toLabel.setStyle("-fx-text-fill: #666;");
        
        TextField maxField = new TextField();
        maxField.setPromptText("Máx");
        maxField.setPrefWidth(80);
        maxField.getStyleClass().add("filter-text-field");
        if (filter.maxValue != null) maxField.setText(String.valueOf(filter.maxValue));
        
        minField.textProperty().addListener((obs, old, newVal) -> {
            try {
                filter.minValue = newVal.isEmpty() ? null : Double.parseDouble(newVal);
            } catch (NumberFormatException e) {
                filter.minValue = null;
            }
        });
        
        maxField.textProperty().addListener((obs, old, newVal) -> {
            try {
                filter.maxValue = newVal.isEmpty() ? null : Double.parseDouble(newVal);
            } catch (NumberFormatException e) {
                filter.maxValue = null;
            }
        });
        
        rangeBox.getChildren().addAll(minField, toLabel, maxField);
        content.getChildren().addAll(infoLabel, rangeBox);
    }

    private void createMultipleChoiceFilter(VBox content, ColumnFilter filter, Question question) {
        if (!(question instanceof MultipleChoiceQuestion mcq)) return;
        
        Label infoLabel = new Label("Selecciona las opciones a mostrar:");
        infoLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
        content.getChildren().add(infoLabel);
        
        List<OptionQuestion> options = mcq.getOptions();
        if (filter.selectedOptions == null) {
            filter.selectedOptions = new HashSet<>();
        }
        
        VBox optionsBox = new VBox(5);
        optionsBox.setPadding(new Insets(5, 0, 0, 0));
        
        for (OptionQuestion opt : options) {
            CheckBox cb = new CheckBox(opt.getOptionText());
            cb.setSelected(filter.selectedOptions.contains(opt.getOptionText()));
            cb.getStyleClass().add("filter-checkbox");
            
            cb.selectedProperty().addListener((obs, old, selected) -> {
                if (selected) {
                    filter.selectedOptions.add(opt.getOptionText());
                } else {
                    filter.selectedOptions.remove(opt.getOptionText());
                }
            });
            
            optionsBox.getChildren().add(cb);
        }
        
        // Opción para mostrar sin respuesta
        CheckBox noAnswerCb = new CheckBox("(sin respuesta)");
        noAnswerCb.setSelected(filter.selectedOptions.contains("(sin respuesta)"));
        noAnswerCb.getStyleClass().add("filter-checkbox");
        noAnswerCb.selectedProperty().addListener((obs, old, selected) -> {
            if (selected) {
                filter.selectedOptions.add("(sin respuesta)");
            } else {
                filter.selectedOptions.remove("(sin respuesta)");
            }
        });
        optionsBox.getChildren().add(noAnswerCb);
        
        ScrollPane scroll = new ScrollPane(optionsBox);
        scroll.setMaxHeight(150);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        
        content.getChildren().add(scroll);
    }

    private TableCell<ResponseRow, String> createTableCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setWrapText(true);
                    
                    if (item.equals("(sin respuesta)") || item.equals("(ninguna opción)")) {
                        setStyle("-fx-text-fill: #999; -fx-font-style: italic;");
                    } else {
                        setStyle("");
                    }
                }
            }
        };
    }

    private void loadResponsesIntoTable() {
        responseRows.clear();

        List<Question> questions = currentSurvey.getQuestions();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (Response response : responses) {
            ResponseRow row = new ResponseRow(response, questions, formatter);
            responseRows.add(row);
        }

        filteredData = new FilteredList<>(responseRows, p -> true);
        SortedList<ResponseRow> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(responsesTable.comparatorProperty());

        responsesTable.setItems(sortedData);
        updateFilteredCount();
    }

    private void applyFilters() {
        if (filteredData == null) return;

        filteredData.setPredicate(row -> {
            for (Map.Entry<Integer, ColumnFilter> entry : columnFilters.entrySet()) {
                int colIndex = entry.getKey();
                ColumnFilter filter = entry.getValue();
                
                if (!filter.isActive()) continue;
                
                String cellValue = getCellValue(row, colIndex);
                
                if (!filter.matches(cellValue)) {
                    return false;
                }
            }
            return true;
        });

        updateFilteredCount();
        updateActiveFiltersLabel();
    }

    private String getCellValue(ResponseRow row, int colIndex) {
        if (colIndex == 0) return row.getUsername();
        if (colIndex == 1) return row.getSubmittedDate();
        return row.getAnswer(colIndex - 2);
    }

    private void updateFilteredCount() {
        if (filteredData != null) {
            filteredCountLabel.setText(String.valueOf(filteredData.size()));
        }
    }

    private void updateActiveFiltersLabel() {
        long activeCount = columnFilters.values().stream().filter(ColumnFilter::isActive).count();
        if (activeCount > 0) {
            activeFiltersLabel.setText("🔍 " + activeCount + " filtro(s) activo(s)");
            activeFiltersLabel.setVisible(true);
        } else {
            activeFiltersLabel.setText("");
            activeFiltersLabel.setVisible(false);
        }
    }

    @FXML
    public void handleClearAllFilters(ActionEvent event) {
        for (ColumnFilter filter : columnFilters.values()) {
            filter.clear();
        }
        applyFilters();
    }

    private String truncateText(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }

    // =========================================
    // CLASES INTERNAS
    // =========================================

    private enum FilterType {
        TEXT, NUMERIC_RANGE, MULTIPLE_CHOICE
    }

    private static class ColumnFilter {
        int columnIndex;
        FilterType type;
        Question question;
        
        // Valores del filtro
        String textValue;
        Double minValue;
        Double maxValue;
        Set<String> selectedOptions;
        
        ColumnFilter(int columnIndex, FilterType type, Question question) {
            this.columnIndex = columnIndex;
            this.type = type;
            this.question = question;
        }
        
        boolean isActive() {
            switch (type) {
                case TEXT:
                    return textValue != null && !textValue.isEmpty();
                case NUMERIC_RANGE:
                    return minValue != null || maxValue != null;
                case MULTIPLE_CHOICE:
                    return selectedOptions != null && !selectedOptions.isEmpty();
                default:
                    return false;
            }
        }
        
        boolean matches(String cellValue) {
            if (cellValue == null) cellValue = "";
            
            switch (type) {
                case TEXT:
                    return cellValue.toLowerCase().contains(textValue.toLowerCase());
                    
                case NUMERIC_RANGE:
                    if (cellValue.equals("(sin respuesta)")) {
                        return false;
                    }
                    try {
                        double value = Double.parseDouble(cellValue);
                        if (minValue != null && value < minValue) return false;
                        if (maxValue != null && value > maxValue) return false;
                        return true;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                    
                case MULTIPLE_CHOICE:
                    if (selectedOptions == null || selectedOptions.isEmpty()) return true;
                    // Verificar si alguna de las opciones seleccionadas está en la respuesta
                    for (String option : selectedOptions) {
                        if (option.equals("(sin respuesta)") && 
                            (cellValue.equals("(sin respuesta)") || cellValue.equals("(ninguna opción)"))) {
                            return true;
                        }
                        if (cellValue.contains(option)) {
                            return true;
                        }
                    }
                    return false;
                    
                default:
                    return true;
            }
        }
        
        void clear() {
            textValue = null;
            minValue = null;
            maxValue = null;
            if (selectedOptions != null) selectedOptions.clear();
        }
    }

    public static class ResponseRow {
        private final String username;
        private final String submittedDate;
        private final List<String> answers;
        private final Response response;

        public ResponseRow(Response response, List<Question> questions, DateTimeFormatter formatter) {
            this.response = response;
            this.username = response.getResponderUsername();
            
            LocalDateTime submittedAt = response.getSUBMITTED_AT();
            this.submittedDate = submittedAt != null ? submittedAt.format(formatter) : "--";
            
            this.answers = new ArrayList<>();
            Answer[] answerArray = response.getANSWERS();
            
            for (int i = 0; i < questions.size(); i++) {
                if (i < answerArray.length) {
                    answers.add(formatAnswer(answerArray[i], questions.get(i)));
                } else {
                    answers.add("(sin respuesta)");
                }
            }
        }

        public String getUsername() { return username; }
        public String getSubmittedDate() { return submittedDate; }
        
        public String getAnswer(int index) {
            if (index >= 0 && index < answers.size()) {
                return answers.get(index);
            }
            return "(sin respuesta)";
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
                return sb.length() > 0 ? sb.toString() : "(ninguna opción)";
            }

            return "(tipo desconocido)";
        }

        public Response getResponse() { return response; }
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
        // La clase nav-btn-active ya está definida en el FXML
    }

    // =========================================
    // NAVEGACIÓN SIDEBAR
    // =========================================

    @FXML public void goToHome(ActionEvent event) { sceneManager.showHome(); }
    @FXML public void goToCreateSurvey(ActionEvent event) { sceneManager.showCreateSurvey(); }
    @FXML public void goToMySurveys(ActionEvent event) { sceneManager.showMySurveys(); }
    @FXML public void goToMyDrafts(ActionEvent event) { sceneManager.showMySurveysDrafts(); }
    @FXML public void handleLogout(ActionEvent event) { userController.logoutUser(); sceneManager.showLogin(); }

    // =========================================
    // ACCIONES PRINCIPALES
    // =========================================

    @FXML public void handleBack(ActionEvent event) { sceneManager.showViewSurvey(surveyId); }
    @FXML public void handleViewByQuestions(ActionEvent event) { sceneManager.showViewResponses(surveyId); }

    @FXML
    public void handleExportCSV(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exportar Respuestas a CSV");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivos CSV", "*.csv"));

        String title = currentSurvey.getTitle().replaceAll("[^a-zA-Z0-9áéíóúÁÉÍÓÚñÑ\\s]", "").trim().replaceAll("\\s+", "_");
        fileChooser.setInitialFileName(title + "_respuestas.csv");

        File file = fileChooser.showSaveDialog(responsesTable.getScene().getWindow());
        if (file == null) return;

        try {
            exportToCSV(file);
            showAlert(Alert.AlertType.INFORMATION, "Exportación Exitosa",
                    "Las respuestas se han exportado correctamente a:\n" + file.getAbsolutePath());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error de Exportación",
                    "No se pudo exportar el CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void exportToCSV(File file) throws IOException {
        List<Question> questions = currentSurvey.getQuestions();

        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            StringBuilder header = new StringBuilder("Usuario,Fecha");
            for (int i = 0; i < questions.size(); i++) {
                header.append(",").append(escapeCSV("P" + (i + 1) + ": " + questions.get(i).getQuestionText()));
            }
            writer.println(header);

            for (ResponseRow row : filteredData) {
                StringBuilder line = new StringBuilder();
                line.append(escapeCSV(row.getUsername()));
                line.append(",").append(escapeCSV(row.getSubmittedDate()));
                for (int i = 0; i < questions.size(); i++) {
                    line.append(",").append(escapeCSV(row.getAnswer(i)));
                }
                writer.println(line);
            }
        }
    }

    private String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
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
}
