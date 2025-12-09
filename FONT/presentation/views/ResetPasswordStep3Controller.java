package presentation.views;

import domain.controller.UserController;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

public class ResetPasswordStep3Controller {

    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;

    @FXML private HBox errorCard;
    @FXML private Label errorLabel;

    private final UserController userController;
    private final SceneManager sceneManager;
    private final String username;

    public ResetPasswordStep3Controller(UserController uc, SceneManager sm, String username) {
        this.userController = uc;
        this.sceneManager = sm;
        this.username = username;
    }

    @FXML
    public void handleSave() {
        try {
            String pass1 = passwordField.getText().trim();
            String pass2 = confirmPasswordField.getText().trim();

            if (!pass1.equals(pass2))
                throw new Exception("Las contraseñas no coinciden.");

            userController.validatePasswordStrength(pass1);
            userController.resetPassword(username, pass1);

            sceneManager.showLogin();

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
