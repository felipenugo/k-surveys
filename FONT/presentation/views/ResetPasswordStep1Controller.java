package presentation.views;

import domain.controller.UserController;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

public class ResetPasswordStep1Controller {

    @FXML private TextField usernameField;
    @FXML private HBox errorCard;
    @FXML private Label errorLabel;

    private final UserController userController;
    private final SceneManager sceneManager;

    public ResetPasswordStep1Controller(UserController userController, SceneManager sceneManager) {
        this.userController = userController;
        this.sceneManager = sceneManager;
    }

    @FXML
    public void handleContinue() {
        try {
            String username = usernameField.getText().trim();
            String question = userController.startPasswordRecovery(username);
            sceneManager.showPasswordRecoveryStep2(username, question);

        } catch (Exception e) {
            errorLabel.setText(e.getMessage());
            errorCard.setVisible(true);
            errorCard.setManaged(true);
        }
    }

    @FXML
    public void goBack() {
        sceneManager.showLogin();
    }
}
