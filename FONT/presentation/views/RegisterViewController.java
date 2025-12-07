package presentation.views;

import domain.controller.UserController;
import domain.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.awt.*;

public class RegisterViewController {

    @FXML
    private TextField usernameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField securityQuestionField;
    @FXML
    private TextField securityAnswerField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button registerButton;
    @FXML
    private Label statusLabel;

    private final UserController userController;
    private final SceneManager sceneManager;

    public RegisterViewController(UserController userController, SceneManager sceneManager)
    {
        this.userController = userController;
        this.sceneManager = sceneManager;
    }

    @FXML
    public void handleRegisterClick(ActionEvent event)
    {
        try{
            String username = usernameField.getText();
            String email = emailField.getText();
            String password = passwordField.getText();
            String securityQuestion = securityQuestionField.getText();
            String securityAnswer = securityAnswerField.getText();
            userController.registerUser(username, email, password, securityQuestion, securityAnswer);
            statusLabel.setText("--- registrando usuario ---");
            sceneManager.showLogin();
        }catch (Exception e)
        {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }
}
