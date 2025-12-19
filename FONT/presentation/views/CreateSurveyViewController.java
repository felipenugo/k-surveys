package presentation.views;

import domain.controller.ResponseController;
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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.stage.FileChooser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlador para la vista de creación de encuestas.
 * Gestiona la creación dinámica de preguntas, guardado como borrador y publicación.
 */
public class CreateSurveyViewController implements Initializable {

    private final UserController userController;
    private final SurveyController surveyController;
    private final ResponseController responseController;
    private final SceneManager sceneManager;

    // --- CAMPOS FXML SIDEBAR ---
    @FXML private Label avatarLabel;
    @FXML private Label usernameLabel;
    @FXML private Button home;
    @FXML private Button createSurvey;
    @FXML private Button mySurveys;
    @FXML private Button myDrafts;

    // --- CAMPOS FXML FORMULARIO ---
    @FXML private TextField surveyTitleField;
    @FXML private TextArea surveyDescField;
    @FXML private VBox questionsContainer;
    @FXML private ScrollPane scrollPane;

    // MODELO DE DATOS
    private final List<Question> questionList = new ArrayList<>();
    private String currentSurveyId = null; // null = nueva encuesta

    // Respuestas importadas desde CSV (se guardarán al publicar)
    private List<List<String>> importedResponses = new ArrayList<>();

    // Variable temporal para Drag & Drop
    private int draggingIndex = -1;

    public CreateSurveyViewController(UserController userController, SurveyController surveyController, 
                                       ResponseController responseController, SceneManager sceneManager) {
        this.userController = userController;
        this.surveyController = surveyController;
        this.responseController = responseController;
        this.sceneManager = sceneManager;
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

        // Inicializar con una pregunta de texto por defecto
        addDefaultQuestion();
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
        // Quitar el estilo create-btn para que se vea como activo en lugar de como botón principal
        createSurvey.getStyleClass().remove("create-btn");
        createSurvey.getStyleClass().add("nav-btn-active");
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
        // Ya estamos aquí
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
    public void addDefaultQuestion() {
        addQuestion(TypeQuestion.TEXTUAL);
    }

    private void addQuestion(TypeQuestion type) {
        int index = questionList.size();
        Question q;

        if (type == TypeQuestion.MULTIPLE_CHOICE) {
            MultipleChoiceQuestion mcq = new MultipleChoiceQuestion(index, "TEMP_ID");
            OptionQuestion opt1 = new OptionQuestion(0, "TEMP_ID");
            opt1.setOptionText("Opción 1");
            OptionQuestion opt2 = new OptionQuestion(1, "TEMP_ID");
            opt2.setOptionText("Opción 2");
            mcq.addOption(opt1);
            mcq.addOption(opt2);
            q = mcq;
        } else {
            q = new Question(index, "TEMP_ID");
            q.setTypeQuestion(type);
        }

        questionList.add(q);
        renderQuestions();

        // Scroll al final
        scrollPane.setVvalue(1.0);
    }

    @FXML
    public void handleSaveDraft() {
        String title = surveyTitleField.getText();
        if (title == null || title.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Título Requerido",
                    "Debes ingresar un título para guardar el borrador.");
            surveyTitleField.getStyleClass().add("error-field");
            return;
        }
        surveyTitleField.getStyleClass().remove("error-field");

        // Validar preguntas
        if (!validateQuestions()) {
            return;
        }

        try {
            Survey survey = buildSurvey();
            survey.setSurveyStatus(SurveyStatus.DRAFT);

            if (currentSurveyId == null) {
                // Nueva encuesta
                Survey created = surveyController.createSurvey(survey);
                currentSurveyId = created.getSURVEY_ID();
                showAlert(Alert.AlertType.INFORMATION, "Borrador Guardado",
                        "El borrador ha sido guardado exitosamente.");
            } else {
                // Actualizar existente
                surveyController.updateSurvey(currentSurveyId, survey);
                showAlert(Alert.AlertType.INFORMATION, "Borrador Actualizado",
                        "El borrador ha sido actualizado exitosamente.");
            }

            sceneManager.showHome();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "No se pudo guardar el borrador: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handlePublish() {
        String title = surveyTitleField.getText();
        if (title == null || title.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Título Requerido",
                    "Debes ingresar un título para publicar la encuesta.");
            surveyTitleField.getStyleClass().add("error-field");
            return;
        }
        surveyTitleField.getStyleClass().remove("error-field");

        if (questionList.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Sin Preguntas",
                    "Debes añadir al menos una pregunta.");
            return;
        }

        // Validar preguntas
        if (!validateQuestions()) {
            return;
        }

        // Confirmar publicación
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar Publicación");
        confirm.setHeaderText("¿Publicar encuesta?");
        confirm.setContentText("Una vez publicada, la encuesta estará disponible para recibir respuestas.");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try {
            Survey survey = buildSurvey();
            String publishedSurveyId;

            if (currentSurveyId == null) {
                // Crear y publicar nueva encuesta
                Survey created = surveyController.createSurvey(survey);
                surveyController.publishSurvey(created.getSURVEY_ID());
                publishedSurveyId = created.getSURVEY_ID();
            } else {
                // Actualizar y publicar existente
                surveyController.updateSurvey(currentSurveyId, survey);
                surveyController.publishSurvey(currentSurveyId);
                publishedSurveyId = currentSurveyId;
            }

            // Guardar respuestas importadas desde CSV
            if (!importedResponses.isEmpty()) {
                saveImportedResponses(publishedSurveyId);
            }

            showAlert(Alert.AlertType.INFORMATION, "Encuesta Publicada",
                    "La encuesta ha sido publicada exitosamente." +
                    (importedResponses.isEmpty() ? "" : "\nSe guardaron " + importedResponses.size() + " respuestas importadas."));
            sceneManager.showHome();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "No se pudo publicar la encuesta: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleCancel() {
        if (!questionList.isEmpty() ||
            (surveyTitleField.getText() != null && !surveyTitleField.getText().trim().isEmpty())) {

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmar Cancelación");
            confirm.setHeaderText("¿Descartar cambios?");
            confirm.setContentText("Perderás todos los cambios no guardados.");

            if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                return;
            }
        }
        sceneManager.showHome();
    }

    @FXML
    public void handleImportCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Importar Encuesta desde CSV");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivos CSV", "*.csv"));
        
        File file = fileChooser.showOpenDialog(questionsContainer.getScene().getWindow());
        if (file == null) return;

        try {
            // Extraer título y descripción del nombre del archivo (formato: Titulo-Descripcion.csv)
            extractTitleAndDescriptionFromFileName(file.getName());
            
            importSurveyFromCSV(file);
            showAlert(Alert.AlertType.INFORMATION, "Importación Exitosa",
                    "La encuesta se ha importado correctamente desde el CSV." +
                    (importedResponses.isEmpty() ? "" : "\nSe importaron " + importedResponses.size() + " respuestas que se guardarán al publicar."));
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error de Importación",
                    "No se pudo importar el CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Extrae el título y descripción del nombre del archivo CSV.
     * Formato esperado: Titulo-Descripcion.csv o Titulo.csv
     * Los guiones bajos se convierten a espacios.
     */
    private void extractTitleAndDescriptionFromFileName(String fileName) {
        // Remover la extensión .csv
        String nameWithoutExtension = fileName;
        if (fileName.toLowerCase().endsWith(".csv")) {
            nameWithoutExtension = fileName.substring(0, fileName.length() - 4);
        }
        
        // Buscar el primer guion '-' como separador entre título y descripción
        int separatorIndex = nameWithoutExtension.indexOf('-');
        
        String title;
        String description;
        
        if (separatorIndex > 0) {
            // Hay separador: título antes del guion, descripción después
            title = nameWithoutExtension.substring(0, separatorIndex);
            description = nameWithoutExtension.substring(separatorIndex + 1);
        } else {
            // No hay separador: todo es el título
            title = nameWithoutExtension;
            description = "";
        }
        
        // Convertir guiones bajos a espacios
        title = title.replace('_', ' ').trim();
        description = description.replace('_', ' ').trim();
        
        // Actualizar los campos si no están vacíos
        if (!title.isEmpty()) {
            surveyTitleField.setText(title);
        }
        if (!description.isEmpty()) {
            surveyDescField.setText(description);
        }
    }

    // Lista para almacenar los usernames de las respuestas importadas
    private List<String> importedResponseUsernames = new ArrayList<>();

    /**
     * Importa una encuesta desde un archivo CSV con el formato:
     * Fila 1 (Question): Textos de las preguntas
     * Fila 2 (Type): Tipos de pregunta (TEXTUAL, NUMERICAL, MULTIPLE_CHOICE[op1|op2|...](min=X;max=Y))
     * Fila 3 (Required): Si cada pregunta es obligatoria (true/false)
     * Filas 4+: Respuestas (opcionales) - primera columna es username/ID del respondedor
     */
    private void importSurveyFromCSV(File file) throws IOException {
        List<String[]> rows = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Parsear CSV respetando comas dentro de corchetes y paréntesis
                rows.add(parseCSVLine(line));
            }
        }

        if (rows.size() < 3) {
            throw new IOException("El archivo CSV debe tener al menos 3 filas (Question, Type y Required).");
        }

        String[] questionRow = rows.get(0);
        String[] typeRow = rows.get(1);
        String[] requiredRow = rows.get(2);

        // Validar que las filas comiencen correctamente
        if (questionRow.length == 0 || !questionRow[0].equalsIgnoreCase("Question")) {
            throw new IOException("La primera fila debe comenzar con 'Question'.");
        }
        if (typeRow.length == 0 || !typeRow[0].equalsIgnoreCase("Type")) {
            throw new IOException("La segunda fila debe comenzar con 'Type'.");
        }
        if (requiredRow.length == 0 || !requiredRow[0].equalsIgnoreCase("Required")) {
            throw new IOException("La tercera fila debe comenzar con 'Required'.");
        }

        // Limpiar preguntas existentes
        questionList.clear();
        importedResponses.clear();
        importedResponseUsernames.clear();

        // Procesar cada columna (desde la columna 1, la 0 es el identificador de fila)
        int numQuestions = Math.min(questionRow.length, Math.min(typeRow.length, requiredRow.length)) - 1;
        
        for (int i = 1; i <= numQuestions; i++) {
            String questionText = i < questionRow.length ? questionRow[i].trim() : "";
            String typeStr = i < typeRow.length ? typeRow[i].trim() : "TEXTUAL";
            String requiredStr = i < requiredRow.length ? requiredRow[i].trim() : "true";

            Question question = createQuestionFromType(questionList.size(), typeStr);
            question.setQuestionText(questionText);
            question.setRequired(requiredStr.equalsIgnoreCase("true") || requiredStr.equals("1"));
            questionList.add(question);
        }

        // Procesar respuestas (filas 4 en adelante)
        for (int rowIdx = 3; rowIdx < rows.size(); rowIdx++) {
            String[] responseRow = rows.get(rowIdx);
            if (responseRow.length == 0) continue;
            
            // Primera columna es el username/ID del respondedor
            String responderUsername = responseRow[0].trim();
            if (responderUsername.isEmpty()) {
                responderUsername = "imported_user_" + (rowIdx - 2);
            }
            
            List<String> responseAnswers = new ArrayList<>();
            for (int i = 1; i <= numQuestions && i < responseRow.length; i++) {
                responseAnswers.add(responseRow[i].trim());
            }
            
            // Rellenar respuestas faltantes con cadenas vacías
            while (responseAnswers.size() < numQuestions) {
                responseAnswers.add("");
            }
            
            if (!responseAnswers.stream().allMatch(String::isEmpty)) {
                importedResponses.add(responseAnswers);
                importedResponseUsernames.add(responderUsername);
            }
        }

        renderQuestions();
    }

    /**
     * Parsea una línea CSV respetando comas dentro de corchetes, paréntesis y comillas.
     */
    private String[] parseCSVLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inBrackets = false;
        boolean inParens = false;
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"' && !inBrackets && !inParens) {
                inQuotes = !inQuotes;
            } else if (c == '[' && !inQuotes) {
                inBrackets = true;
                current.append(c);
            } else if (c == ']' && !inQuotes) {
                inBrackets = false;
                current.append(c);
            } else if (c == '(' && !inQuotes) {
                inParens = true;
                current.append(c);
            } else if (c == ')' && !inQuotes) {
                inParens = false;
                current.append(c);
            } else if (c == ',' && !inBrackets && !inParens && !inQuotes) {
                result.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        result.add(current.toString());

        return result.toArray(new String[0]);
    }

    /**
     * Crea una pregunta a partir de la cadena de tipo del CSV.
     * Formato: MULTIPLE_CHOICE[op1|op2|op3](min=1;max=2)
     */
    private Question createQuestionFromType(int index, String typeStr) {
        String originalTypeStr = typeStr;
        typeStr = typeStr.trim().toUpperCase();

        if (typeStr.startsWith("MULTIPLE_CHOICE")) {
            MultipleChoiceQuestion mcq = new MultipleChoiceQuestion(index, "TEMP_ID");
            
            // Extraer opciones si existen: MULTIPLE_CHOICE[op1|op2|op3]
            int bracketStart = originalTypeStr.indexOf('[');
            int bracketEnd = originalTypeStr.indexOf(']');
            
            if (bracketStart != -1 && bracketEnd != -1 && bracketEnd > bracketStart) {
                String optionsStr = originalTypeStr.substring(bracketStart + 1, bracketEnd);
                String[] options = optionsStr.split("\\|");
                
                // Limpiar opciones por defecto
                while (mcq.getOptions().size() > 0) {
                    mcq.removeOption(0);
                }
                
                for (int i = 0; i < options.length; i++) {
                    OptionQuestion opt = new OptionQuestion(i, "TEMP_ID");
                    opt.setOptionText(options[i].trim());
                    mcq.addOption(opt);
                }
            } else {
                // Sin opciones especificadas, crear 2 por defecto
                OptionQuestion opt1 = new OptionQuestion(0, "TEMP_ID");
                opt1.setOptionText("Opción 1");
                OptionQuestion opt2 = new OptionQuestion(1, "TEMP_ID");
                opt2.setOptionText("Opción 2");
                mcq.addOption(opt1);
                mcq.addOption(opt2);
            }
            
            // Extraer min/max si existen: (min=1;max=2)
            int parenStart = originalTypeStr.indexOf('(');
            int parenEnd = originalTypeStr.indexOf(')');
            if (parenStart != -1 && parenEnd != -1 && parenEnd > parenStart) {
                String configStr = originalTypeStr.substring(parenStart + 1, parenEnd).toLowerCase();
                // Parsear min=X;max=Y
                for (String part : configStr.split(";")) {
                    part = part.trim();
                    if (part.startsWith("min=")) {
                        try {
                            int minVal = Integer.parseInt(part.substring(4).trim());
                            mcq.setMinSelections(Math.max(1, Math.min(minVal, mcq.getOptions().size())));
                        } catch (NumberFormatException ignored) {}
                    } else if (part.startsWith("max=")) {
                        try {
                            int maxVal = Integer.parseInt(part.substring(4).trim());
                            mcq.setMaxSelections(Math.max(1, Math.min(maxVal, mcq.getOptions().size())));
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }
            
            return mcq;
        } else if (typeStr.equals("NUMERICAL") || typeStr.equals("NUMERICA") || typeStr.equals("NUMÉRICA")) {
            Question q = new Question(index, "TEMP_ID");
            q.setTypeQuestion(TypeQuestion.NUMERICAL);
            return q;
        } else {
            // Por defecto: TEXTUAL
            Question q = new Question(index, "TEMP_ID");
            q.setTypeQuestion(TypeQuestion.TEXTUAL);
            return q;
        }
    }

    /**
     * Guarda las respuestas importadas como respuestas reales de la encuesta.
     */
    private void saveImportedResponses(String surveyId) {
        if (importedResponses.isEmpty()) return;

        for (int respIdx = 0; respIdx < importedResponses.size(); respIdx++) {
            List<String> responseAnswers = importedResponses.get(respIdx);
            String username = respIdx < importedResponseUsernames.size() 
                    ? importedResponseUsernames.get(respIdx) 
                    : "imported_user_" + respIdx;
            
            try {
                String responseId = responseController.startResponse(surveyId, username);
                
                for (int qIdx = 0; qIdx < responseAnswers.size() && qIdx < questionList.size(); qIdx++) {
                    String answerValue = responseAnswers.get(qIdx);
                    if (answerValue == null || answerValue.isEmpty()) continue;

                    Question q = questionList.get(qIdx);
                    TypeQuestion type = q.getTypeQuestion();

                    if (type == TypeQuestion.NUMERICAL) {
                        try {
                            Double numValue = Double.parseDouble(answerValue);
                            responseController.updateAnswer(surveyId, responseId, qIdx, numValue);
                        } catch (NumberFormatException e) {
                            // Ignorar respuestas numéricas inválidas
                        }
                    } else if (type == TypeQuestion.MULTIPLE_CHOICE) {
                        // Convertir nombres de opciones a índices numéricos
                        String indices = convertOptionsToIndices(answerValue, q);
                        responseController.updateAnswer(surveyId, responseId, qIdx, indices, type);
                    } else {
                        // TEXTUAL
                        responseController.updateAnswer(surveyId, responseId, qIdx, answerValue, type);
                    }
                }
                
                responseController.incrementResponseCount(surveyId);
            } catch (Exception e) {
                System.err.println("Error al guardar respuesta importada: " + e.getMessage());
            }
        }
    }
    
    /**
     * Convierte los nombres de opciones (separados por |) a índices numéricos (separados por espacios).
     * Ejemplo: "Azul|Verde" -> "1 2" (si Azul es índice 1 y Verde es índice 2)
     */
    private String convertOptionsToIndices(String optionNames, Question question) {
        if (!(question instanceof MultipleChoiceQuestion mcq)) {
            return optionNames;
        }
        
        // Si ya son índices numéricos separados por espacios, devolverlos directamente
        if (optionNames.matches("[0-9\\s]+")) {
            return optionNames;
        }
        
        List<OptionQuestion> options = mcq.getOptions();
        String[] selectedNames = optionNames.split("\\|");
        StringBuilder indices = new StringBuilder();
        
        for (String name : selectedNames) {
            String trimmedName = name.trim();
            for (int i = 0; i < options.size(); i++) {
                if (options.get(i).getOptionText().equalsIgnoreCase(trimmedName)) {
                    if (indices.length() > 0) indices.append(" ");
                    indices.append(i);
                    break;
                }
            }
        }
        
        return indices.toString();
    }

    // =========================================
    // CONSTRUCCIÓN Y VALIDACIÓN DE ENCUESTA
    // =========================================

    private Survey buildSurvey() {
        String title = surveyTitleField.getText().trim();
        String description = surveyDescField.getText() != null ? surveyDescField.getText().trim() : "";
        String username = userController.getUsernameLoggedIn();

        Survey survey;
        if (currentSurveyId != null) {
            survey = new Survey(currentSurveyId, title, description, username);
        } else {
            String newId = surveyController.generateUniqueSurveyId();
            survey = new Survey(newId, title, description, username);
        }

        // Añadir preguntas al survey
        for (int i = 0; i < questionList.size(); i++) {
            Question q = questionList.get(i);
            q.setQuestionIndex(i);
            survey.getQuestions().add(q);
        }

        return survey;
    }

    private boolean validateQuestions() {
        for (int i = 0; i < questionList.size(); i++) {
            Question q = questionList.get(i);

            // Validar que el texto no esté vacío si es obligatoria
            if (q.getQuestionText() == null || q.getQuestionText().trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Pregunta Incompleta",
                        "La pregunta #" + (i + 1) + " no tiene texto.");
                return false;
            }

            // Validar opciones múltiples
            if (q instanceof MultipleChoiceQuestion mcq) {
                if (mcq.getOptions().size() < 2) {
                    showAlert(Alert.AlertType.WARNING, "Opciones Insuficientes",
                            "La pregunta #" + (i + 1) + " debe tener al menos 2 opciones.");
                    return false;
                }

                // Validar que las opciones tengan texto
                for (int j = 0; j < mcq.getOptions().size(); j++) {
                    OptionQuestion opt = mcq.getOptions().get(j);
                    if (opt.getOptionText() == null || opt.getOptionText().trim().isEmpty()) {
                        showAlert(Alert.AlertType.WARNING, "Opción Vacía",
                                "La opción #" + (j + 1) + " de la pregunta #" + (i + 1) + " está vacía.");
                        return false;
                    }
                }

                // Validar min/max selecciones
                if (mcq.getMinSelections() > mcq.getMaxSelections()) {
                    showAlert(Alert.AlertType.WARNING, "Configuración Inválida",
                            "En la pregunta #" + (i + 1) +
                            ", el mínimo de selecciones no puede ser mayor al máximo.");
                    return false;
                }

                if (mcq.getMaxSelections() > mcq.getOptions().size()) {
                    showAlert(Alert.AlertType.WARNING, "Configuración Inválida",
                            "En la pregunta #" + (i + 1) +
                            ", el máximo de selecciones no puede ser mayor al número de opciones.");
                    return false;
                }
            }
        }
        return true;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // =========================================
    // RENDERIZADO DINÁMICO
    // =========================================

    private void renderQuestions() {
        questionsContainer.getChildren().clear();

        for (int i = 0; i < questionList.size(); i++) {
            Question q = questionList.get(i);
            q.setQuestionIndex(i);

            VBox card = createQuestionCard(q, i);
            questionsContainer.getChildren().add(card);
        }
    }

    private VBox createQuestionCard(Question q, int index) {
        VBox card = new VBox();
        card.getStyleClass().add("question-card");
        card.setSpacing(10);

        // Configurar Drag & Drop
        setupDragAndDrop(card, index);

        // --- 1. HEADER ---
        HBox header = new HBox(15);
        header.setAlignment(Pos.TOP_LEFT);
        header.getStyleClass().add("q-header-row");

        // Handle para arrastrar
        Label handle = new Label("⋮⋮");
        handle.getStyleClass().add("drag-handle");
        handle.setTooltip(new Tooltip("Arrastra para reordenar"));

        // Input texto pregunta
        TextField qInput = new TextField(q.getQuestionText());
        qInput.setPromptText("Escribe tu pregunta...");
        qInput.getStyleClass().add("q-text-input");
        HBox.setHgrow(qInput, Priority.ALWAYS);
        qInput.textProperty().addListener((obs, o, n) -> q.setQuestionText(n));

        // Focus listener para estilo activo
        qInput.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                card.getStyleClass().add("question-card-active");
            } else {
                card.getStyleClass().remove("question-card-active");
            }
        });

        // ComboBox tipo de pregunta
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("Texto", "Opción Múltiple", "Numérica");
        typeCombo.getStyleClass().add("q-type-combo");

        // Seleccionar valor actual
        switch (q.getTypeQuestion()) {
            case TEXTUAL -> typeCombo.setValue("Texto");
            case MULTIPLE_CHOICE -> typeCombo.setValue("Opción Múltiple");
            case NUMERICAL -> typeCombo.setValue("Numérica");
        }

        typeCombo.valueProperty().addListener((obs, oldVal, newVal) -> changeQuestionType(index, newVal));

        header.getChildren().addAll(handle, qInput, typeCombo);
        card.getChildren().add(header);

        // --- 2. CONTENIDO ---
        VBox contentArea = new VBox();
        contentArea.getStyleClass().add("q-content-area");
        contentArea.setSpacing(10);

        if (q instanceof MultipleChoiceQuestion mcq) {
            renderMultipleChoiceContent(mcq, contentArea);
        } else if (q.getTypeQuestion() == TypeQuestion.NUMERICAL) {
            Label placeholder = new Label("Respuesta numérica del usuario");
            placeholder.getStyleClass().add("text-placeholder");
            placeholder.setMaxWidth(Double.MAX_VALUE);
            contentArea.getChildren().add(placeholder);
        }
        // Para TEXTUAL no mostramos nada (como en el HTML de ejemplo)

        card.getChildren().add(contentArea);

        // --- 3. FOOTER ---
        HBox footer = new HBox(20);
        footer.getStyleClass().add("card-footer");
        footer.setAlignment(Pos.CENTER_RIGHT);

        // Switch obligatoria
        HBox switchContainer = new HBox(8);
        switchContainer.setAlignment(Pos.CENTER_LEFT);
        switchContainer.getStyleClass().add("switch-container");

        CheckBox requiredCheck = new CheckBox("Obligatoria");
        requiredCheck.setSelected(q.isRequired());
        requiredCheck.getStyleClass().add("switch-label");
        requiredCheck.selectedProperty().addListener((obs, o, n) -> q.setRequired(n));

        switchContainer.getChildren().add(requiredCheck);

        // Separador
        Region sep = new Region();
        sep.getStyleClass().add("vertical-separator");

        // Botón eliminar
        Button deleteBtn = new Button();
        deleteBtn.getStyleClass().addAll("icon-btn", "delete");
        SVGPath trashIcon = new SVGPath();
        trashIcon.setContent("M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z");
        trashIcon.getStyleClass().add("trash-icon");
        deleteBtn.setGraphic(trashIcon);
        deleteBtn.setTooltip(new Tooltip("Eliminar pregunta"));
        deleteBtn.setOnAction(e -> {
            if (questionList.size() > 1) {
                questionList.remove(index);
                renderQuestions();
            } else {
                showAlert(Alert.AlertType.WARNING, "No Permitido",
                        "La encuesta debe tener al menos una pregunta.");
            }
        });

        footer.getChildren().addAll(switchContainer, sep, deleteBtn);
        card.getChildren().add(footer);

        return card;
    }

    private void renderMultipleChoiceContent(MultipleChoiceQuestion mcq, VBox container) {
        VBox optionsList = new VBox(10);
        optionsList.getStyleClass().add("options-list");

        for (int i = 0; i < mcq.getOptions().size(); i++) {
            OptionQuestion opt = mcq.getOptions().get(i);
            int optIndex = i;

            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("option-row");

            // Icono drag
            Label dragOpt = new Label("⋮⋮");
            dragOpt.getStyleClass().add("option-drag");

            // Radio visual
            Region radio = new Region();
            radio.getStyleClass().add("radio-circle");

            // Input opción
            TextField optInput = new TextField(opt.getOptionText());
            optInput.setPromptText("Opción " + (i + 1));
            optInput.getStyleClass().add("option-input");
            HBox.setHgrow(optInput, Priority.ALWAYS);
            optInput.textProperty().addListener((obs, o, n) -> opt.setOptionText(n));

            // Botón eliminar opción
            Button delOpt = new Button("✕");
            delOpt.getStyleClass().add("remove-opt-btn");
            delOpt.setOnAction(e -> {
                if (mcq.getOptions().size() > 2) {
                    mcq.removeOption(optIndex);
                    // Ajustar max si es necesario
                    if (mcq.getMaxSelections() > mcq.getOptions().size()) {
                        mcq.setMaxSelections(mcq.getOptions().size());
                    }
                    if (mcq.getMinSelections() > mcq.getOptions().size()) {
                        mcq.setMinSelections(mcq.getOptions().size());
                    }
                    renderQuestions();
                } else {
                    showAlert(Alert.AlertType.WARNING, "Mínimo de Opciones",
                            "Debe haber al menos 2 opciones.");
                }
            });

            row.getChildren().addAll(dragOpt, radio, optInput, delOpt);
            optionsList.getChildren().add(row);
        }

        // Botón añadir opción
        Button addOptBtn = new Button("+ Añadir opción");
        addOptBtn.getStyleClass().add("add-option-btn");
        addOptBtn.setOnAction(e -> {
            int newIndex = mcq.getOptions().size();
            OptionQuestion newOpt = new OptionQuestion(newIndex, "TEMP_ID");
            newOpt.setOptionText("Opción " + (newIndex + 1));
            mcq.addOption(newOpt);
            renderQuestions();
        });

        HBox addRow = new HBox(addOptBtn);
        addRow.setPadding(new Insets(5, 0, 0, 42));

        // Configuración min/max selecciones
        HBox selectionConfig = new HBox(20);
        selectionConfig.getStyleClass().add("selection-config");
        selectionConfig.setAlignment(Pos.CENTER_LEFT);
        selectionConfig.setPadding(new Insets(15, 0, 5, 0));

        // Mínimo de selecciones
        Label minLabel = new Label("Mín. selecciones:");
        minLabel.getStyleClass().add("selection-label");

        Spinner<Integer> minSpinner = new Spinner<>(1, mcq.getOptions().size(), mcq.getMinSelections());
        minSpinner.getStyleClass().add("selection-spinner");
        minSpinner.setEditable(true);

        // Filter to allow only integer input
        minSpinner.getEditor().setTextFormatter(new TextFormatter<>(change -> {
            if (change.getControlNewText().matches("\\d*")) {
                return change;
            }
            return null;
        }));

        minSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            try {
                if (newVal <= mcq.getMaxSelections() && newVal <= mcq.getOptions().size()) {
                    mcq.setMinSelections(newVal);
                } else {
                    minSpinner.getValueFactory().setValue(oldVal);
                }
            } catch (IllegalArgumentException e) {
                minSpinner.getValueFactory().setValue(oldVal);
            }
        });

        // Máximo de selecciones
        Label maxLabel = new Label("Máx. selecciones:");
        maxLabel.getStyleClass().add("selection-label");

        Spinner<Integer> maxSpinner = new Spinner<>(1, mcq.getOptions().size(), mcq.getMaxSelections());
        maxSpinner.getStyleClass().add("selection-spinner");
        maxSpinner.setEditable(true);

        // Filter to allow only integer input
        maxSpinner.getEditor().setTextFormatter(new TextFormatter<>(change -> {
            if (change.getControlNewText().matches("\\d*")) {
                return change;
            }
            return null;
        }));

        maxSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            try {
                if (newVal >= mcq.getMinSelections() && newVal <= mcq.getOptions().size()) {
                    mcq.setMaxSelections(newVal);
                } else {
                    maxSpinner.getValueFactory().setValue(oldVal);
                }
            } catch (IllegalArgumentException e) {
                maxSpinner.getValueFactory().setValue(oldVal);
            }
        });

        selectionConfig.getChildren().addAll(minLabel, minSpinner, maxLabel, maxSpinner);

        container.getChildren().addAll(optionsList, addRow, selectionConfig);
    }

    private void changeQuestionType(int index, String newTypeStr) {
        Question oldQ = questionList.get(index);
        TypeQuestion newType;

        switch (newTypeStr) {
            case "Opción Múltiple" -> newType = TypeQuestion.MULTIPLE_CHOICE;
            case "Numérica" -> newType = TypeQuestion.NUMERICAL;
            default -> newType = TypeQuestion.TEXTUAL;
        }

        if (oldQ.getTypeQuestion() == newType) return;

        Question newQ;
        if (newType == TypeQuestion.MULTIPLE_CHOICE) {
            MultipleChoiceQuestion mcq = new MultipleChoiceQuestion(index, "TEMP_ID");
            OptionQuestion opt1 = new OptionQuestion(0, "TEMP_ID");
            opt1.setOptionText("Opción 1");
            OptionQuestion opt2 = new OptionQuestion(1, "TEMP_ID");
            opt2.setOptionText("Opción 2");
            mcq.addOption(opt1);
            mcq.addOption(opt2);
            newQ = mcq;
        } else {
            newQ = new Question(index, "TEMP_ID");
            newQ.setTypeQuestion(newType);
        }

        newQ.setQuestionText(oldQ.getQuestionText());
        newQ.setRequired(oldQ.isRequired());

        questionList.set(index, newQ);
        renderQuestions();
    }

    // =========================================
    // DRAG & DROP (REORDENAR)
    // =========================================

    private void setupDragAndDrop(Node node, int index) {
        node.setOnDragDetected(event -> {
            draggingIndex = index;
            Dragboard db = node.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(String.valueOf(index));
            db.setContent(content);
            node.setOpacity(0.4);
            node.getStyleClass().add("dragging");
            event.consume();
        });

        node.setOnDragOver(event -> {
            if (event.getGestureSource() != node && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        node.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                int sourceIdx = Integer.parseInt(db.getString());
                int targetIdx = index;

                if (sourceIdx != targetIdx) {
                    Question item = questionList.remove(sourceIdx);
                    questionList.add(targetIdx, item);
                    success = true;
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });

        node.setOnDragDone(event -> {
            node.setOpacity(1.0);
            node.getStyleClass().remove("dragging");
            if (event.getTransferMode() == TransferMode.MOVE) {
                renderQuestions();
            }
            event.consume();
        });
    }
}

