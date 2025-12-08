package presentation.views;

import domain.controller.SurveyController;
import domain.controller.UserController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;

import java.net.URL;
import java.util.ResourceBundle;

public class HomeViewController implements Initializable {
    private final UserController userController;
    private final SurveyController surveyController;
    private final SceneManager sceneManager;
    @FXML
    private Button home;

    public HomeViewController(UserController userController, SurveyController surveyController, SceneManager sceneManager) {
        this.userController = userController;
        this.surveyController = surveyController;
        this.sceneManager = sceneManager;
    }

    /**
     * -----------------------------
     * --- FUNCIONES DEL SIDEBAR ---
     * -----------------------------
     */

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
        sceneManager.showMyDrafts();
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        try {
            userController.logoutUser();
            sceneManager.showLogin();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        home.getStyleClass().add("nav-btn-active");
    }

}
