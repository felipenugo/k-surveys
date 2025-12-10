package presentation.views;

import domain.controller.SurveyController;
import domain.controller.UserController;
import domain.model.Survey;
import domain.model.enums.SurveyStatus;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controlador para la vista principal de la aplicación (Home).
 * <p>
 * Se encarga de la inicialización de la UI, la gestión de la sesión del usuario,
 * el filtrado y ordenamiento de la lista de encuestas, y la creación dinámica
 * de las 'cards' de encuesta.
 */
public class HomeViewController implements Initializable {

    private final UserController userController;
    private final SurveyController surveyController;
    private final SceneManager sceneManager;
    private final int MAX_SURVEYS_PER_PAGE;
    private int numSurveys;

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

    // Pagination
    @FXML
    private Button prevPageBtn;
    @FXML
    private Button currentPageBtn;
    @FXML
    private Button nextPageBtn;
    @FXML
    private Pagination pagination;

    // Datos
    private List<Survey> allSurveys; // Todas las encuestas (Mock)
   // private List<Survey> currentSurveys; // Encuestas filtradas actualmente

    /**
     * Constructor para inyección de dependencias.
     *
     * @param userController   Controlador de la capa de dominio para gestionar usuarios y sesión.
     * @param surveyController Controlador de la capa de dominio para gestionar encuestas.
     * @param sceneManager     Gestor para la navegación entre vistas.
     */
    public HomeViewController(UserController userController, SurveyController surveyController, SceneManager sceneManager) {
        this.userController = userController;
        this.surveyController = surveyController;
        this.sceneManager = sceneManager;
        this.MAX_SURVEYS_PER_PAGE = 1;
        this.numSurveys = 0;
    }

    /**
     * Obtiene las iniciales de un nombre de usuario, excluyendo espacios y convirtiéndolas a mayúsculas.
     * Por ejemplo: "felipe antonio" -> "FA"
     *
     * @param username El nombre completo del usuario.
     * @return Las iniciales en mayúscula.
     */
    private String getInitialLetters(String username) {
        String[] initials = username.trim().replaceAll("\\s+", "").split(" ");
        StringBuilder result = new StringBuilder();
        for (String s : initials) {
            result.append(s.substring(0, 1).toUpperCase());
        }
        return result.toString();
    }

    /**
     * Obtiene el número máximo de encuestas que se deben mostrar por página.
     *
     * @return El número máximo de encuestas por página.
     */
    private int getMAX_SURVEYS_PER_PAGE() {
        return MAX_SURVEYS_PER_PAGE;
    }

    /**
     * Obtiene el número total de encuestas disponibles para paginación.
     *
     * @return El número total de encuestas.
     */
    private int getNumSurveys() {
        return numSurveys;
    }

    /**
     * Establece el número total de encuestas disponibles para paginación.
     * Esto se usa para calcular el número total de páginas en el control Pagination.
     *
     * @param numSurveys El nuevo número total de encuestas.
     */
    private void setNumSurveys(int numSurveys) {
        this.numSurveys = numSurveys;
    }

    /**
     * Devuelve la página actual cogiendo el valor del Pagination de fxml
     * @return Número de Página actual
     */
    private int getCurrentPageIndex() {
        return pagination.getCurrentPageIndex();
    }

    /**
     * Calcula el número de páginas necesarias para mostrar todas las encuestas.
     * @return número de páginas dependiendo de todas las encuestas que encajan con los filtros
     */
    private int getNumPages() {
        return (int) Math.ceil((double) getNumSurveys() / getMAX_SURVEYS_PER_PAGE());
    }

    /**
     * Método invocado después de que un controlador ha sido cargado en su totalidad.
     * Se encarga de configurar el estado inicial de la vista.
     *
     * @param url            La ubicación utilizada para resolver las rutas relativas para el objeto raíz, o null si la ubicación no se conoce.
     * @param resourceBundle Los recursos utilizados para localizar el objeto raíz, o null si el objeto raíz no fue localizado.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // 1. Configurar estilo activo del Sidebar
        if (home != null) {
            home.getStyleClass().add("nav-btn-active");
        }
        if (!userController.isLoggedIn()) {
            sceneManager.showLogin();
            return;
        }
        // Asignar valor al avatar y nombre del usuario
        String username = userController.getUsernameLoggedIn();
        usernameLabel.setText(username);
        avatarLabel.setText(getInitialLetters(username));

        setFilters(); // definir eventos para buscar, filtrar y ordenar
        allSurveys = surveyController.getSelectedSurveys(); // todas las encuestas

        // aplicar los filtros y renderizar las encuestas
        applyFilters();

        // pagination
        pagination.currentPageIndexProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    /**
     * Actualiza el número de páginas y si cambia pone el currentPageIndex a 1
     */
    private void updatePagination() {
        pagination.setPageCount(getNumPages());
    }

    /**
     * Configura los ComboBoxes de filtro y ordenamiento y establece los listeners de eventos
     * para el buscador y los combos.
     */
    private void setFilters() {
        filterCombo.getItems().addAll("Todos", "Rating > 3.5", "Rating > 4.5", "Views > 50");
        filterCombo.setValue("Todos"); // valor por defecto

        sortCombo.getItems().addAll("Más Recientes", "Más Antiguas", "Más populares", "Mejor Valoradas");
        sortCombo.setValue("Más Recientes"); // valor por defecto

        // Listener para Filtros
        filterCombo.valueProperty().addListener((obs, oldVal, newVal) ->
                applyFilters());
        sortCombo.valueProperty().addListener((obs, oldVal, newVal) ->
                applyFilters());

        // Listener para Buscador
        searchField.textProperty().addListener((obs, oldVal, newVal) ->
                applyFilters());
    }

    /**
     * Aplica el filtro de texto, el filtro de selección (Combo Filtro) y el ordenamiento (Combo Sort)
     * a la lista de encuestas {@code allSurveys} y actualiza la vista.
     */
    private void applyFilters() {
        String filter = filterCombo.getValue(); // valor actual del filtro
        String sort = sortCombo.getValue(); // valor actual del sort
        String searchText = searchField.getText().toLowerCase(); // texto introducido en el buscador

        // Comparador para Ordenamiento
        Comparator<Survey> surveyComparator = switch (sort) {
            case "Más Antiguas" -> Comparator.comparing(Survey::getPUBLISHED_AT);
            case "Más populares" -> Comparator.comparing(Survey::getViews).reversed();
            case "Mejor Valoradas" -> Comparator.comparing(Survey::getAvgRating).reversed();
            default -> Comparator.comparing(Survey::getPUBLISHED_AT).reversed(); // encuestas más recientes
        };

        // Filtrar y ordenar encuestas
        List<Survey> currentSurveys = allSurveys.stream()
                .filter(s -> !s.getSurveyStatus().equals(SurveyStatus.DRAFT))
                .filter(s -> s.getTitle().toLowerCase().contains(searchText)) // Buscador
                .filter(s -> switch (filter) { // Combo Filtro con expresión switch
                    case "Rating > 3.5" -> s.getAvgRating() > 3.5;
                    case "Rating > 4.5" -> s.getAvgRating() > 4.0;
                    case "Views > 50" -> s.getViews() > 50;
                    default -> true; // todas las encuestas
                })
                .sorted(surveyComparator)
                .toList();
        // Actualizar encuestas y número de encuestas
        this.setNumSurveys(currentSurveys.size());
        updatePagination();
        int startIndex = getCurrentPageIndex() * getMAX_SURVEYS_PER_PAGE();
        int endIndex = Math.min(startIndex + getMAX_SURVEYS_PER_PAGE(), currentSurveys.size());
        if (startIndex < 0 || startIndex >= currentSurveys.size())
            currentSurveys = new ArrayList<>();
        currentSurveys = currentSurveys.subList(startIndex, endIndex);
        renderSurveyList(currentSurveys);
    }


    // Crear y renderizar cards de las encuestas

    /**
     * Limpia el contenedor de encuestas y lo repuebla con las cards de las encuestas proporcionadas.
     * Muestra el estado vacío (empty state) si la lista está vacía.
     *
     * @param surveys Lista de encuestas a renderizar.
     */
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
            // para cada encuesta crea una card y la añade al contenedor
            for (Survey s : surveys)
                surveysContainer.getChildren().add(createSurveyCard(s));
        }
    }

    /**
     * Construye programáticamente el HBox de una fila de encuesta (card).
     * Estructura: [ Main Info ] | [ Views ] | [ Rating ] | [ Date ]
     *
     * @param s La encuesta para la cual crear la card.
     * @return Un HBox que representa la fila de la encuesta.
     */
    private HBox createSurveyCard(Survey s) {
        HBox card = new HBox();
        card.getStyleClass().add("survey-card");
        card.setOnMouseClicked(e -> sceneManager.showAnswerSurvey(s.getSURVEY_ID()));

        // Crear columnas
        VBox colMain = createMainColumn(s);
        HBox colViews = createViewsColumn(s);
        HBox colRating = createRatingColumn(s);
        HBox colDate = createDateColumn(s);

        // Añadir columnas con divisores
        addColumnsWithDividers(card, colMain, colViews, colRating, colDate);

        // Ajustar tamaño para que sea responsive
        HBox.setHgrow(colMain, Priority.ALWAYS);
        HBox.setHgrow(colViews, Priority.ALWAYS);
        HBox.setHgrow(colRating, Priority.ALWAYS);
        HBox.setHgrow(colDate, Priority.ALWAYS);

        return card;
    }

// --- CREACIÓN DE COLUMNAS ---

    /**
     * Crea la columna principal de la card que contiene el título y el creador.
     *
     * @param s La encuesta.
     * @return Un VBox con el título y el creador.
     */
    private VBox createMainColumn(Survey s) {
        VBox colMain = new VBox();
        colMain.getStyleClass().add("col");
        colMain.setMinWidth(250);

        Label title = new Label(s.getTitle());
        title.getStyleClass().add("survey-title");

        Label creator = new Label(s.getCREATOR_USERNAME());
        creator.getStyleClass().add("survey-creator");

        colMain.getChildren().addAll(title, creator);

        return colMain;
    }

    /**
     * Crea la columna que muestra el número de vistas de la encuesta.
     *
     * @param s La encuesta.
     * @return Un HBox con el ícono de ojo y el número de vistas.
     */
    private HBox createViewsColumn(Survey s) {
        String eyeSvg = "M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5M12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5m0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3";
        HBox colViews = createDataCell(String.valueOf(s.getViews()), eyeSvg);
        colViews.getStyleClass().add("col");
        colViews.setMinWidth(70);
        return colViews;
    }

    /**
     * Crea la columna que muestra la valoración promedio de la encuesta.
     *
     * @param s La encuesta.
     * @return Un HBox con las estrellas de valoración.
     */
    private HBox createRatingColumn(Survey s) {
        HBox colRating = createRatingCell(s.getAvgRating());
        colRating.setMinWidth(150);
        return colRating;
    }

    /**
     * Crea la columna que muestra la fecha de publicación de la encuesta.
     *
     * @param s La encuesta.
     * @return Un HBox con el ícono de calendario y la fecha.
     */
    private HBox createDateColumn(Survey s) {
        String dateStr = s.getPUBLISHED_AT().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String calSvg = "M19 3h-1V1h-2v2H8V1H6v2H5c-1.11 0-1.99.9-1.99 2L3 19c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H5V8h14v11zM7 10h5v5H7z";
        HBox colDate = createDataCell(dateStr, calSvg);
        colDate.setMinWidth(120);
        return colDate;
    }

    /**
     * Añade la columna principal seguida de las demás columnas, separadas por divisores verticales.
     *
     * @param card    El HBox contenedor de la card.
     * @param colMain La columna principal (ya añadida).
     * @param columns Las columnas secundarias a añadir.
     */
    private void addColumnsWithDividers(HBox card, VBox colMain, HBox... columns) {
        card.getChildren().add(colMain); // primera columna
        for (HBox col : columns) {
            card.getChildren().add(createVerticalDivider());
            card.getChildren().add(col);
        }
    }

    /**
     * Crea un separador vertical estilizado para dividir las columnas.
     *
     * @return Un objeto Region estilizado.
     */
    private Region createVerticalDivider() {
        Region r = new Region();
        r.getStyleClass().add("v-divider");
        return r;
    }

    /**
     * Crea una celda HBox genérica para mostrar un ícono SVG y un texto.
     *
     * @param text    El texto a mostrar.
     * @param svgPath La cadena SVG que define el ícono.
     * @return Un HBox que actúa como celda de datos.
     */
    private HBox createDataCell(String text, String svgPath) {
        HBox cell = new HBox(5);
        cell.setAlignment(Pos.CENTER);

        SVGPath icon = new SVGPath();
        icon.setContent(svgPath);
        icon.getStyleClass().add("col-icon");

        Label lbl = new Label(text);
        lbl.getStyleClass().add("col-text");
        cell.getChildren().addAll(icon, lbl);
        return cell;
    }

    /**
     * Crea una celda HBox para mostrar la valoración promedio y el número.
     *
     * @param rating La valoración promedio (double).
     * @return Un HBox con las estrellas y el número de valoración.
     */
    private HBox createRatingCell(double rating) {
        HBox cell = new HBox(5);
        cell.setAlignment(Pos.CENTER);

        HBox starsBox = createStarsBox(rating);

        Label ratingNum = new Label(String.valueOf(rating));
        ratingNum.getStyleClass().add("col-text");

        cell.getChildren().addAll(starsBox, ratingNum);
        return cell;
    }

    /**
     * Crea un HBox que contiene 5 íconos de estrellas SVG, marcando las estrellas llenas y medias
     * según la valoración proporcionada.
     *
     * @param rating La valoración (ej. 4.5).
     * @return Un HBox con las estrellas renderizadas.
     */
    private HBox createStarsBox(double rating) {
        HBox starsBox = new HBox();
        String starSvg = "M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z";

        for (int i = 1; i <= 5; i++) {
            SVGPath star = new SVGPath();
            star.setContent(starSvg);
            star.getStyleClass().add("star");

            if (rating >= i) {
                star.getStyleClass().add("full");
            } else if (rating >= i - 0.5) {
                star.getStyleClass().add("half");
            }
            starsBox.getChildren().add(star);
        }

        return starsBox;
    }

    // --- NAVEGACIÓN SIDEBAR ---

    /**
     * Navega a la vista principal (Home) al hacer clic en el botón.
     *
     * @param event El evento de la acción.
     */
    @FXML
    public void goToHome(ActionEvent event) {
        sceneManager.showHome();
        System.out.println("Refrescando Home...");
    }

    /**
     * Navega a la vista de Creación de Encuesta (funcionalidad no implementada en este fragmento).
     *
     * @param event El evento de la acción.
     */
    @FXML
    public void goToCreateSurvey(ActionEvent event) {
        // sceneManager.showCreateSurvey();
        System.out.println("Navegando a Crear Encuesta...");
    }

    /**
     * Navega a la vista de Mis Encuestas (funcionalidad no implementada en este fragmento).
     *
     * @param event El evento de la acción.
     */
    @FXML
    public void goToMySurveys(ActionEvent event) {
        // sceneManager.showMySurveys();
        System.out.println("Navegando a Mis Encuestas...");
    }

    /**
     * Navega a la vista de Borradores (funcionalidad no implementada en este fragmento).
     *
     * @param event El evento de la acción.
     */
    @FXML
    public void goToMyDrafts(ActionEvent event) {
        // sceneManager.showMyDrafts();
        System.out.println("Navegando a Borradores...");
    }

    /**
     * Cierra la sesión del usuario actual y navega a la vista de Login.
     *
     * @param event El evento de la acción.
     */
    @FXML
    public void handleLogout(ActionEvent event) {
        try {
            userController.logoutUser();
            sceneManager.showLogin();
        } catch (Exception e) {
            System.err.println("Error al cerrar sesión: " + e.getMessage());
        }
    }
}