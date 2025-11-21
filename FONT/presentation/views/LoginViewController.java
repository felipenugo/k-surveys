package presentation.views;

import domain.controller.UserController;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;

public class LoginViewController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;
    @FXML
    private Label statusLabel;

    private final UserController userController;
    private final SceneManager sceneManager;

    public LoginViewController(UserController userController, SceneManager sceneManager) {
        this.userController = userController;
        this.sceneManager = sceneManager;
    }

    @FXML
    public void handleLoginClick(ActionEvent event) {
        try {
            String username = usernameField.getText();
            String password = passwordField.getText();
            userController.loginUser(username, password);
            statusLabel.setText("--- iniciando sesión ---");
            // sceneManager.showMainMenu();

        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    public void handleRegisterClick(ActionEvent event) {
        sceneManager.showRegister();
    }
}