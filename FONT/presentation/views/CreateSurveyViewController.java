package presentation.views;

import domain.controller.SurveyController;
import domain.controller.UserController;
import domain.model.MultipleChoiceQuestion;
import domain.model.OptionQuestion;
import domain.model.Question;
import domain.model.enums.TypeQuestion;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class CreateSurveyViewController implements Initializable {

    private final UserController userController;
    private final SurveyController surveyController;
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
    @FXML private VBox questionsContainer; // Contenedor de preguntas

    // MODELO DE DATOS
    private final List<Question> questionList = new ArrayList<>();
    
    // Variable temporal para Drag & Drop (índice del elemento arrastrado)
    private int draggingIndex = -1;

    public CreateSurveyViewController(UserController userController, SurveyController surveyController, SceneManager sceneManager) {
        this.userController = userController;
        this.surveyController = surveyController;
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

        // Inicializar con una pregunta de texto por defecto
        addDefaultQuestion();
    }

    // --- MÉTODOS AUXILIARES SIDEBAR ---

    /**
     * Obtiene las iniciales de un nombre de usuario.
     */
    private String getInitialLetters(String username) {
        String[] parts = username.trim().split("\\s+");
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                result.append(part.substring(0, 1).toUpperCase());
            }
        }
        return result.length() > 0 ? result.toString() : "U";
    }

    /**
     * Marca el botón "Crear Encuesta" como activo en la barra lateral.
     */
    private void setViewActive() {
        createSurvey.getStyleClass().add("nav-btn-active");
    }

    // --- NAVEGACIÓN SIDEBAR ---

    @FXML
    public void goToHome(ActionEvent event) {
        sceneManager.showHome();
    }

    @FXML
    public void goToCreateSurvey(ActionEvent event) {
        // Ya estamos en esta vista, no hacer nada
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

    // --- ACCIONES PRINCIPALES ---

    @FXML
    public void addDefaultQuestion() {
        // Por defecto añadimos una Textual, pero el usuario puede cambiarla con el ComboBox
        addQuestion(TypeQuestion.TEXTUAL);
    }

    private void addQuestion(TypeQuestion type) {
        int index = questionList.size(); // Nuevo índice
        Question q;
        
        // Crear instancia según tipo (aunque inicialmente sea textual, preparamos lógica)
        if (type == TypeQuestion.MULTIPLE_CHOICE) {
            q = new MultipleChoiceQuestion(index, "TEMP_ID");
            // Añadir opciones por defecto
            ((MultipleChoiceQuestion) q).addOption(new OptionQuestion(0, "TEMP_ID"));
            ((MultipleChoiceQuestion) q).addOption(new OptionQuestion(1, "TEMP_ID"));
        } else {
            q = new Question(index, "TEMP_ID");
            q.setTypeQuestion(type);
        }
        
        questionList.add(q);
        renderQuestions(); // Refrescar vista
        
        // Scroll al final (opcional, requeriría acceso al ScrollPane)
    }

    @FXML
    public void handlePublish() {
        String title = surveyTitleField.getText();
        if (title == null || title.trim().isEmpty()) {
            System.out.println("Error: El título es obligatorio");
            // Aquí podrías poner un borde rojo al campo o mostrar alerta
            return;
        }
        
        // Aquí llamarías al controlador para guardar
        // Survey newSurvey = new Survey(title, surveyDescField.getText(), ...);
        // newSurvey.setQuestions(questionList);
        // surveyController.save(newSurvey);
        
        System.out.println("Publicando encuesta: " + title + " con " + questionList.size() + " preguntas.");
        sceneManager.showHome();
    }

    @FXML
    public void handleCancel() {
        sceneManager.showHome();
    }

    // --- RENDERIZADO DINÁMICO ---

    /**
     * Reconstruye la lista visual de preguntas basándose en el modelo 'questionList'.
     */
    private void renderQuestions() {
        questionsContainer.getChildren().clear();

        for (int i = 0; i < questionList.size(); i++) {
            Question q = questionList.get(i);
            q.setQuestionIndex(i); // Asegurar índices correctos
            
            VBox card = createQuestionCard(q, i);
            questionsContainer.getChildren().add(card);
        }
    }

    /**
     * Construye la tarjeta visual para una pregunta específica.
     */
    private VBox createQuestionCard(Question q, int index) {
        VBox card = new VBox();
        card.getStyleClass().add("question-card");
        
        // Configurar Drag & Drop para la tarjeta
        setupDragAndDrop(card, index);

        // --- 1. HEADER (Handle, Input, Combo, Delete) ---
        HBox header = new HBox(15);
        header.setAlignment(Pos.TOP_LEFT);
        header.getStyleClass().add("q-header-row");

        // Handle
        Label handle = new Label("⋮⋮");
        handle.getStyleClass().add("drag-handle");
        handle.setTooltip(new Tooltip("Arrastra para reordenar"));

        // Input Texto Pregunta
        TextField qInput = new TextField(q.getQuestionText());
        qInput.setPromptText("Escribe tu pregunta...");
        qInput.getStyleClass().add("q-text-input");
        HBox.setHgrow(qInput, Priority.ALWAYS);
        // Listener para actualizar modelo
        qInput.textProperty().addListener((obs, o, n) -> q.setQuestionText(n));

        // ComboBox Tipo
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("Texto", "Opción Múltiple", "Numérica");
        typeCombo.getStyleClass().add("q-type-combo");
        
        // Seleccionar valor actual
        if (q.getTypeQuestion() == TypeQuestion.TEXTUAL) typeCombo.setValue("Texto");
        else if (q.getTypeQuestion() == TypeQuestion.MULTIPLE_CHOICE) typeCombo.setValue("Opción Múltiple");
        else typeCombo.setValue("Numérica");

        // Listener cambio de tipo (Transformación de objeto)
        typeCombo.valueProperty().addListener((obs, oldVal, newVal) -> changeQuestionType(index, newVal));

        // Botón Borrar
        Button deleteBtn = new Button();
        deleteBtn.getStyleClass().add("icon-btn");
        SVGPath trashIcon = new SVGPath();
        trashIcon.setContent("M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z");
        trashIcon.getStyleClass().add("trash-icon");
        deleteBtn.setGraphic(trashIcon);
        deleteBtn.setOnAction(e -> {
            questionList.remove(index);
            renderQuestions();
        });

        header.getChildren().addAll(handle, qInput, typeCombo);
        card.getChildren().add(header);

        // --- 2. CONTENIDO (Según Tipo) ---
        VBox contentArea = new VBox();
        contentArea.getStyleClass().add("q-content-area");

        if (q instanceof MultipleChoiceQuestion) {
            // Renderizar lista de opciones
            renderOptions((MultipleChoiceQuestion) q, contentArea);
        } else {
            // Renderizar placeholder de texto
            Label placeholder = new Label("Texto de respuesta del usuario");
            placeholder.getStyleClass().add("text-placeholder");
            placeholder.setMaxWidth(Double.MAX_VALUE);
            contentArea.getChildren().add(placeholder);
        }
        card.getChildren().add(contentArea);

        // --- 3. FOOTER (Obligatoria, Borrar) ---
        HBox footer = new HBox(15);
        footer.getStyleClass().add("card-footer");
        footer.setAlignment(Pos.CENTER_RIGHT);

        CheckBox requiredCheck = new CheckBox("Obligatoria");
        requiredCheck.setSelected(q.isRequired());
        requiredCheck.selectedProperty().addListener((obs, o, n) -> q.setRequired(n));
        
        // Separador vertical
        Region sep = new Region();
        sep.setPrefSize(1, 20);
        sep.setStyle("-fx-background-color: #ddd;");

        footer.getChildren().addAll(requiredCheck, sep, deleteBtn); // Movemos delete aquí para que cuadre con diseño
        card.getChildren().add(footer);

        return card;
    }

    /**
     * Renderiza las opciones de una pregunta de selección múltiple.
     */
    private void renderOptions(MultipleChoiceQuestion mcq, VBox container) {
        VBox optionsList = new VBox(10);
        
        for (int i = 0; i < mcq.getOptions().size(); i++) {
            OptionQuestion opt = mcq.getOption(i);
            int optIndex = i; // Efectivamente final para lambdas

            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("option-row");

            // Icono arrastre opción (Visual por ahora)
            Label dragOpt = new Label("⋮⋮");
            dragOpt.setStyle("-fx-text-fill: #ddd; -fx-cursor: move;");

            // Radio visual (Círculo)
            Region radio = new Region();
            radio.getStyleClass().add("radio-circle");

            // Input Opción
            TextField optInput = new TextField(opt.getOptionText());
            optInput.setPromptText("Opción " + (i + 1));
            optInput.getStyleClass().add("option-input");
            HBox.setHgrow(optInput, Priority.ALWAYS);
            optInput.textProperty().addListener((obs, o, n) -> opt.setOptionText(n));

            // Borrar Opción
            Button delOpt = new Button("✕");
            delOpt.getStyleClass().add("remove-opt-btn"); // Definir en CSS: transparente, rojo hover
            delOpt.setOnAction(e -> {
                mcq.removeOption(optIndex);
                renderQuestions(); // Re-renderizar tarjeta
            });

            row.getChildren().addAll(dragOpt, radio, optInput, delOpt);
            optionsList.getChildren().add(row);
        }

        // Botón Añadir Opción
        Button addOptBtn = new Button("+ Añadir opción");
        addOptBtn.getStyleClass().add("add-option-btn");
        addOptBtn.setOnAction(e -> {
            mcq.addOption(new OptionQuestion(mcq.getOptions().size(), "TEMP"));
            renderQuestions();
        });

        HBox addRow = new HBox(addOptBtn);
        addRow.setPadding(new javafx.geometry.Insets(5, 0, 0, 42)); // Indentación

        container.getChildren().addAll(optionsList, addRow);
    }

    /**
     * Cambia el tipo de pregunta en una posición dada.
     * Esto implica reemplazar el objeto Question por uno nuevo (si cambia de clase)
     * o simplemente cambiar su enum (si la clase es la misma).
     */
    private void changeQuestionType(int index, String newTypeStr) {
        Question oldQ = questionList.get(index);
        TypeQuestion newType;
        
        if (newTypeStr.equals("Opción Múltiple")) newType = TypeQuestion.MULTIPLE_CHOICE;
        else if (newTypeStr.equals("Numérica")) newType = TypeQuestion.NUMERICAL;
        else newType = TypeQuestion.TEXTUAL;

        if (oldQ.getTypeQuestion() == newType) return; // No hay cambio

        // Crear nueva pregunta conservando datos básicos
        Question newQ;
        if (newType == TypeQuestion.MULTIPLE_CHOICE) {
            newQ = new MultipleChoiceQuestion(index, oldQ.getSURVEY_ID());
            ((MultipleChoiceQuestion) newQ).addOption(new OptionQuestion(0, "Opción 1"));
        } else {
            newQ = new Question(index, oldQ.getSURVEY_ID());
        }
        
        newQ.setTypeQuestion(newType);
        newQ.setQuestionText(oldQ.getQuestionText());
        newQ.setRequired(oldQ.isRequired());

        // Reemplazar en lista
        questionList.set(index, newQ);
        renderQuestions();
    }

    // ==========================================
    // LÓGICA DRAG & DROP (REORDENAR)
    // ==========================================

    private void setupDragAndDrop(Node node, int index) {
        // 1. Iniciar arrastre
        node.setOnDragDetected(event -> {
            draggingIndex = index;
            Dragboard db = node.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(String.valueOf(index)); // Guardamos el índice origen
            db.setContent(content);
            node.setOpacity(0.4);
            event.consume();
        });

        // 2. Sobrevolar otro nodo
        node.setOnDragOver(event -> {
            if (event.getGestureSource() != node && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        // 3. Soltar
        node.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                int sourceIdx = Integer.parseInt(db.getString());
                int targetIdx = index; // El índice del nodo donde soltamos

                // Reordenar Lista
                if (sourceIdx != targetIdx) {
                    Question item = questionList.remove(sourceIdx);
                    questionList.add(targetIdx, item);
                    success = true;
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });

        // 4. Terminar (Limpieza y Repintado)
        node.setOnDragDone(event -> {
            node.setOpacity(1.0);
            if (event.getTransferMode() == TransferMode.MOVE) {
                renderQuestions(); // Refrescar UI con nuevo orden
            }
            event.consume();
        });
    }
}

