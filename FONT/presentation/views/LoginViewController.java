package presentation.views;

import domain.controller.UserController;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.scene.layout.HBox;

public class LoginViewController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField passwordText;
    @FXML
    private Button loginButton;
    @FXML
    private HBox errorCard;
    @FXML
    private Label errorLabel;

    private final UserController userController;
    private final SceneManager sceneManager;

    public LoginViewController(UserController userController, SceneManager sceneManager) {
        this.userController = userController;
        this.sceneManager = sceneManager;
    }

    @FXML
    public void handleLogin(ActionEvent event) {
        try {
            String username = usernameField.getText();
            String password = passwordField.getText();
            userController.loginUser(username, password);
            sceneManager.showHome();

        } catch (Exception e) {
            errorLabel.setText(e.getMessage());
            errorCard.setVisible(true);
            errorCard.setManaged(true);
        }
    }

    @FXML
    public void togglePassword() {
        if (passwordField.isVisible())
        {
            passwordText.setText(passwordField.getText());

            passwordField.setVisible(false);
            passwordField.setManaged(false);

            passwordText.setVisible(true);
            passwordText.setManaged(true);
        }
        else
        {
            passwordField.setText(passwordText.getText());

            passwordText.setVisible(false);
            passwordText.setManaged(false);

            passwordField.setVisible(true);
            passwordField.setManaged(true);
        }
    }

    @FXML
    public void goToRegister(ActionEvent event) {
        sceneManager.showRegister();
    }

    @FXML
    public void goToResetPassword(ActionEvent e)
    {
        sceneManager.showRegister(); // poner la vista de reset password
    }
}