package presentation.views;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.stage.Stage;

public class ConfirmLeaveController {

    private Runnable onConfirm; // acción que se ejecuta si el usuario acepta

    public void setOnConfirm(Runnable onConfirm) {
        this.onConfirm = onConfirm;
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        ((Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow()).close();
    }

    @FXML
    private void handleLeave(ActionEvent event) {
        if (onConfirm != null) onConfirm.run();
        ((Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow()).close();
    }
}
