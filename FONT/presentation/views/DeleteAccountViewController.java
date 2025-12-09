package presentation.views;

import javafx.event.ActionEvent;
import domain.controller.UserController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class DeleteAccountViewController {

    private final UserController userController;
    private final SceneManager sceneManager;

    public DeleteAccountViewController(UserController userController, SceneManager sceneManager) {
        this.userController = userController;
        this.sceneManager = sceneManager;
    }

    @FXML
    private void confirmDelete(ActionEvent event) {
        boolean ok = userController.deleteCurrentUser();
        if (ok) {
            ((Stage)((Button)event.getSource()).getScene().getWindow()).close();
            sceneManager.showLogin();
        }
    }

    @FXML
    public void handleCancel(ActionEvent event) {
        ((Stage)((Button)event.getSource()).getScene().getWindow()).close();
        sceneManager.showHome();
    }
}
