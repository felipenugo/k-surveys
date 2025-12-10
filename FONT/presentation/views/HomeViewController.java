package presentation.views;

import domain.controller.SurveyController;
import domain.controller.UserController;
import domain.model.Survey;
import domain.model.enums.SurveyStatus;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import presentation.util.CreateSurveyCard;

import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

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
     *
     * @return Número de Página actual
     */
    private int getCurrentPageIndex() {
        return pagination.getCurrentPageIndex();
    }

    /**
     * Calcula el número de páginas necesarias para mostrar todas las encuestas.
     *
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
     * Si no hay encuestas entonces ponemos valor 1 que es el mínimo aceptable
     */
    private void updatePageCount() {
        if (getNumPages() == 0)
            pagination.setPageCount(1);
        else
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
        updatePageCount();
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
                surveysContainer.getChildren().add(CreateSurveyCard.getSurveyCard(sceneManager, s));
        }
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

    @FXML
    public void handleDeleteAccount(ActionEvent event) {
        try {
            sceneManager.showDeleteAccountConfirm(); 
        } catch (Exception e) {
            System.err.println("Error al navegar a eliminar cuenta: " + e.getMessage());
        }
    }

}