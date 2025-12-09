package presentation.views;

import domain.controller.UserController;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

public class ResetPasswordStep2Controller {

    @FXML private Label questionLabel;
    @FXML private TextField answerField;
    @FXML private HBox errorCard;
    @FXML private Label errorLabel;

    private final UserController userController;
    private final SceneManager sceneManager;
    private final String username;
    private final String question;

    public ResetPasswordStep2Controller(UserController uc, SceneManager sm, String username, String question) {
        this.userController = uc;
        this.sceneManager = sm;
        this.username = username;
        this.question = question; // ✔ guardar la pregunta
    }

    @FXML
    public void initialize() {
        questionLabel.setText(question); // ✔ el FXML recibe el texto real
    }

    @FXML
    public void handleValidate() {
        try {
            String answer = answerField.getText().trim();
            boolean ok = userController.verifySecurityAnswer(username, answer);

            if (ok) {
                sceneManager.showPasswordRecoveryStep3(username);
            } else {
                throw new Exception("La respuesta no es correcta.");
            }

        } catch (Exception e) {
            errorLabel.setText(e.getMessage());
            errorCard.setVisible(true);
            errorCard.setManaged(true);
        }
    }

    @FXML
    public void goBack() {
        sceneManager.showPasswordRecovery();
    }
}
