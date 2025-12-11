package presentation.views;

import domain.controller.SurveyController;
import domain.controller.UserController;
import domain.model.Survey;
import domain.model.enums.SurveyStatus;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import presentation.util.SurveyView;
import presentation.util.SurveyViewTitle;

import java.util.function.Predicate;

public class MySurveysViewController extends SurveysViewController {
    @FXML
    private Button mySurveys;
    @FXML
    private Label centerContentTitle;
    private final SurveyViewTitle TITLE;
    private String currentUsername;

    /**
     * Constructor para inyección de dependencias.
     *
     * @param userController   Controlador de la capa de dominio para gestionar usuarios y sesión.
     * @param surveyController Controlador de la capa de dominio para gestionar encuestas.
     * @param sceneManager     Gestor para la navegación entre vistas.
     */
    public MySurveysViewController(UserController userController, SurveyController surveyController, SurveyView surveyView, SceneManager sceneManager) {
        super(userController, surveyController, surveyView, sceneManager);
        currentUsername = userController.getUsernameLoggedIn();
        TITLE = SurveyViewTitle.MY_SURVEYS;
    }

    @Override
    protected void setViewActive() {
        mySurveys.getStyleClass().add("nav-btn-active");
    }

    @Override
    protected void setContentTitle() {
        centerContentTitle.setText(TITLE.getTitle());
    }

    @Override
    protected Predicate<Survey> getFilter(String filter, String searchText) {
        return s ->
                s.getCREATOR_USERNAME().equals(currentUsername) &&
                        !s.getSurveyStatus().equals(SurveyStatus.DRAFT) &&
                        s.getTitle().toLowerCase().contains(searchText) &&
                        switch (filter) {
                            case "Rating > 3.5" -> s.getAvgRating() > 3.5;
                            case "Rating > 4.5" -> s.getAvgRating() > 4.0;
                            case "Views > 50" -> s.getViews() > 50;
                            default -> true;
                        };
    }
}
