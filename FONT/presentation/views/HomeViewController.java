package presentation.views;

import domain.controller.SurveyController;
import domain.controller.UserController;
import domain.model.Survey;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class HomeViewController implements Initializable {

    private final UserController userController;
    private final SurveyController surveyController;
    private final SceneManager sceneManager;

    // --- VARIABLES FXML ---
    @FXML
    private Button home; // Botón activo en el sidebar
    @FXML
    private Label usernameLabel;
    @FXML
    private Label avatarLabel;

    // Header
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> filterCombo;
    @FXML
    private ComboBox<String> sortCombo;

    // Contenedores
    @FXML
    private VBox surveysContainer;
    @FXML
    private VBox emptyStateBox;

    // Datos
    private List<Survey> allSurveys; // Todas las encuestas (Mock)
    private List<Survey> currentSurveys; // Encuestas filtradas actualmente

    // Inyección de Dependencias
    public HomeViewController(UserController userController, SurveyController surveyController, SceneManager sceneManager) {
        this.userController = userController;
        this.surveyController = surveyController;
        this.sceneManager = sceneManager;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // 1. Configurar estilo activo del Sidebar
        if (home != null) {
            home.getStyleClass().add("nav-btn-active");
        }

        // 2. Cargar datos del usuario (Si hay sesión iniciada)
        // if (userController.getCurrentUser() != null) {
        //     usernameLabel.setText(userController.getCurrentUser().getUsername());
        //     avatarLabel.setText(userController.getCurrentUser().getUsername().substring(0, 2).toUpperCase());
        // }


        // 4. Configurar ComboBoxes
        setupFilters();
        allSurveys = surveyController.getSelectedSurveys();
        // 5. Renderizar lista inicial
        renderSurveyList(allSurveys);
    }

    /**
     * Configura los listeners para los filtros y el buscador.
     */
    private void setupFilters() {
        filterCombo.getItems().addAll("Todos", "Rating > 4.0", "Más Vistas (>500)");
        filterCombo.setValue("Todos"); // Valor por defecto

        sortCombo.getItems().addAll("Más Recientes", "Más Antiguas", "Mejor Valoradas");
        sortCombo.setValue("Más Recientes");

        // Listener para Filtros
        filterCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        sortCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        // Listener para Buscador (al escribir)
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    /**
     * Aplica los filtros seleccionados a la lista 'allSurveys' y actualiza la vista.
     * (Lógica simplificada para demostración).
     */
    private void applyFilters() {
        String filter = filterCombo.getValue();
        String searchText = searchField.getText().toLowerCase();

        // 1. Filtrar
        currentSurveys = allSurveys.stream()
                .filter(s -> s.getTitle().toLowerCase().contains(searchText)) // Buscador
                .filter(s -> { // Combo Filtro
                    if ("Rating > 4.0".equals(filter)) return s.getAvgRating() > 4.0;
                    if ("Más Vistas (>500)".equals(filter)) return s.getViews() > 500;
                    return true; // "Todos"
                })
                .collect(Collectors.toList());

        // 2. Ordenar (Mock básico, aquí usarías Comparators reales)
        // if ("Mejor Valoradas".equals(sortCombo.getValue())) { ... }

        // 3. Renderizar
        renderSurveyList(currentSurveys);
    }


    // --- RENDERIZADO DINÁMICO ---

    private void renderSurveyList(List<Survey> surveys) {
        // Limpiar todas las cards de encuestas del contenedor
        surveysContainer.getChildren().clear();

        // Si no hay encuestas, mostrar un mensaje informando al usuario
        if (surveys.isEmpty()) {
            surveysContainer.setVisible(false);
            surveysContainer.setManaged(false);
            emptyStateBox.setVisible(true);
            emptyStateBox.setManaged(true);
        } else {
            surveysContainer.setVisible(true);
            surveysContainer.setManaged(true);
            emptyStateBox.setVisible(false);
            emptyStateBox.setManaged(false);
            for (Survey s : surveys) {
                // Crear las cards de las encuestas con el contenido y el estilo css
                HBox surveyCard = createSurveyCard(s);
                // Añadir la card al contenedor
                surveysContainer.getChildren().add(surveyCard);
            }
        }
    }

    /**
     * Construye programáticamente el HBox de una fila de encuesta.
     * Estructura: [ Main Info ] | [ Views ] | [ Rating ] | [ Date ]
     */
    private HBox createSurveyCard(Survey s) {
        // Contenedor horizontal de las cards de encuestas
        HBox card = new HBox();
        card.getStyleClass().add("survey-card");
        // Evento al hacer clic en la fila (ej. ir a detalles)
        card.setOnMouseClicked(e -> sceneManager.showAnswerSurvey(s.getSURVEY_ID()));

        // 1. COLUMNA PRINCIPAL (Título y Autor)
        VBox colMain = new VBox();
        colMain.getStyleClass().add("col-main");
        HBox.setHgrow(colMain, Priority.ALWAYS); // Ocupa espacio sobrante

        Label titleLbl = new Label(s.getTitle());
        titleLbl.getStyleClass().add("row-title");

        Label authorLbl = new Label(s.getCREATOR_USERNAME());
        authorLbl.getStyleClass().add("row-author");

        colMain.getChildren().addAll(titleLbl, authorLbl);

        // 2. COLUMNA VISTAS
        // SVG path de un ojo
        String eyeSvg = "M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z";
        HBox colViews = createDataCell(String.valueOf(s.getViews()), eyeSvg);
        colViews.setMinWidth(100); // Coincide con header

        // 3. COLUMNA RATING
        HBox colRating = createRatingCell(s.getAvgRating());
        colRating.setMinWidth(140); // Coincide con header

        // 4. COLUMNA FECHA
        String dateStr = s.getPUBLISHED_AT() != null ?
                s.getPUBLISHED_AT().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "Borrador";
        // SVG path de un calendario
        String calSvg = "M19 3h-1V1h-2v2H8V1H6v2H5c-1.11 0-1.99.9-1.99 2L3 19c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H5V8h14v11zM7 10h5v5H7z";
        HBox colDate = createDataCell(dateStr, calSvg);
        colDate.setMinWidth(120); // Coincide con header

        // Añadir columnas y divisores
        card.getChildren().addAll(
                colMain,
                createVerticalDivider(),
                colViews,
                createVerticalDivider(),
                colRating,
                createVerticalDivider(),
                colDate
        );

        return card;
    }

    // --- MÉTODOS AUXILIARES UI ---

    private Region createVerticalDivider() {
        Region r = new Region();
        r.getStyleClass().add("v-divider");
        return r;
    }

    private HBox createDataCell(String text, String svgPath) {
        HBox cell = new HBox(5);
        cell.setAlignment(Pos.CENTER);

        SVGPath icon = new SVGPath();
        icon.setContent(svgPath);
        icon.getStyleClass().add("icon-small"); // Definido en CSS (.icon-small { -fx-fill: #aaa; ... })

        Label lbl = new Label(text);
        lbl.setStyle("-fx-text-fill: #666; -fx-font-size: 13px;");

        cell.getChildren().addAll(icon, lbl);
        return cell;
    }

    private HBox createRatingCell(double rating) {
        HBox cell = new HBox(5);
        cell.setAlignment(Pos.CENTER);

        HBox starsBox = new HBox(1);
        String starSvg = "M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z";

        for (int i = 1; i <= 5; i++) {
            SVGPath star = new SVGPath();
            star.setContent(starSvg);
            star.getStyleClass().add("star"); // Estilo base (gris)

            if (rating >= i) {
                star.getStyleClass().add("full"); // Clase CSS para amarillo
            } else if (rating >= i - 0.5) {
                star.getStyleClass().add("half"); // Clase CSS para amarillo suave
            }
            starsBox.getChildren().add(star);
        }

        Label ratingNum = new Label(String.valueOf(rating));
        ratingNum.setStyle("-fx-font-weight: bold; -fx-text-fill: #555;");

        cell.getChildren().addAll(starsBox, ratingNum);
        return cell;
    }

    // --- NAVEGACIÓN SIDEBAR ---

    @FXML
    public void goToHome(ActionEvent event) {
        // Ya estamos en Home, podrías refrescar la lista si quieres
        System.out.println("Refrescando Home...");
    }

    @FXML
    public void goToCreateSurvey(ActionEvent event) {
        // sceneManager.showCreateSurvey();
        System.out.println("Navegando a Crear Encuesta...");
    }

    @FXML
    public void goToMySurveys(ActionEvent event) {
        // sceneManager.showMySurveys();
        System.out.println("Navegando a Mis Encuestas...");
    }

    @FXML
    public void goToMyDrafts(ActionEvent event) {
        // sceneManager.showMyDrafts();
        System.out.println("Navegando a Borradores...");
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        try {
            userController.logoutUser();
            sceneManager.showLogin();
        } catch (Exception e) {
            System.err.println("Error al cerrar sesión: " + e.getMessage());
        }
    }

    @FXML
    public void handleDeleteAccount(ActionEvent event) {
        try {
            sceneManager.showDeleteAccountConfirm(); 
        } catch (Exception e) {
            System.err.println("Error al navegar a eliminar cuenta: " + e.getMessage());
        }
    }

}