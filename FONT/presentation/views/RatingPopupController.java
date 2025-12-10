package presentation.views;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.stage.Stage;
import presentation.views.RatingPopupController.RatingListener;

public class RatingPopupController {

    @FXML private Slider ratingSlider;
    @FXML private Label ratingValueLabel;

    private RatingListener listener;

    public interface RatingListener {
        void onRatingSelected(double rating);
    }

    public void setListener(RatingListener listener) {
        this.listener = listener;
    }

    @FXML
    public void initialize() {
        ratingSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            ratingValueLabel.setText(String.format("%.0f", newVal.doubleValue()));
        });
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    @FXML
    private void handleConfirm() {
        if (listener != null) {
            listener.onRatingSelected(ratingSlider.getValue());
        }
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) ratingSlider.getScene().getWindow();
        stage.close();
    }
}
