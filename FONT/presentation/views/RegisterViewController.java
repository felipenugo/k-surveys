package presentation.views;

import domain.controller.UserController;
import domain.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.scene.layout.HBox;

public class RegisterViewController {

    @FXML
    private TextField usernameField;
    @FXML
    private TextField emailField;
    // Contraseña 1
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordText;

    // Contraseña 2 (Confirmación)
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField confirmPasswordText;

    @FXML private TextField securityQuestionField;
    @FXML private TextField securityAnswerField;

    // Checkbox y Error
    @FXML private CheckBox showPasswordCheck;
    @FXML private HBox errorCard;
    @FXML private Label errorLabel;

    private final UserController userController;
    private final SceneManager sceneManager;

    public RegisterViewController(UserController userController, SceneManager sceneManager) {
        this.userController = userController;
        this.sceneManager = sceneManager;
    }

    @FXML
    public void handleRegister(ActionEvent event) {
        try {
            String username = usernameField.getText();
            String email = emailField.getText();
            String password = passwordField.isVisible() ? passwordField.getText() : passwordText.getText();
            String confirmPassword = confirmPasswordField.isVisible() ? confirmPasswordField.getText() : confirmPasswordText.getText();
            if(!password.equals(confirmPassword))
                throw new Exception("Las contraseñas no coinciden.");
            userController.validatePasswordStrength(password);
            String question = securityQuestionField.getText();
            String answer = securityAnswerField.getText();
            userController.registerUser(username, email, password, question, answer);
            sceneManager.showLogin();
        } catch (Exception e) {
            errorLabel.setText( e.getMessage());
            errorCard.setVisible(true);
            errorCard.setManaged(true);
        }
    }

    @FXML
    public void toggleRegisterPasswords() {
        boolean show = showPasswordCheck.isSelected();

        if (show) {
            // MOSTRAR CONTRASEÑAS (Copiar texto y cambiar visibilidad)
            passwordText.setText(passwordField.getText());
            confirmPasswordText.setText(confirmPasswordField.getText());

            // Ocultar PasswordFields
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            confirmPasswordField.setVisible(false);
            confirmPasswordField.setManaged(false);

            // Mostrar TextFields
            passwordText.setVisible(true);
            passwordText.setManaged(true);
            confirmPasswordText.setVisible(true);
            confirmPasswordText.setManaged(true);

        } else {
            // OCULTAR CONTRASEÑAS
            passwordField.setText(passwordText.getText());
            confirmPasswordField.setText(confirmPasswordText.getText());

            // Ocultar TextFields
            passwordText.setVisible(false);
            passwordText.setManaged(false);
            confirmPasswordText.setVisible(false);
            confirmPasswordText.setManaged(false);

            // Mostrar PasswordFields
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            confirmPasswordField.setVisible(true);
            confirmPasswordField.setManaged(true);
        }
    }

    @FXML
    public void goToLogin(ActionEvent event) {
        sceneManager.showLogin();
    }
}
