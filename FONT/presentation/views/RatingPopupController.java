package presentation.views;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class RatingPopupController {

    @FXML private HBox starsContainer;
    @FXML private Label ratingValueLabel;

    private RatingListener listener;
    private double currentRating = 0.0;
    private static final int NUM_STARS = 5;
    private static final double STAR_SIZE = 40.0;
    
    // SVG path para una estrella
    private static final String STAR_PATH = "M12 .587l3.668 7.431 8.2 1.192-5.934 5.787 1.402 8.173L12 18.897l-7.336 3.854 1.402-8.173L.132 9.21l8.2-1.192z";

    public interface RatingListener {
        void onRatingSelected(double rating);
    }

    public void setListener(RatingListener listener) {
        this.listener = listener;
    }

    @FXML
    public void initialize() {
        createStars();
        updateStarsDisplay();
    }

    private void createStars() {
        starsContainer.getChildren().clear();
        
        for (int i = 0; i < NUM_STARS; i++) {
            final int starIndex = i;
            StackPane starPane = createStarPane(starIndex);
            starsContainer.getChildren().add(starPane);
        }
    }

    private StackPane createStarPane(int starIndex) {
        StackPane starPane = new StackPane();
        starPane.setPrefSize(STAR_SIZE, STAR_SIZE);
        
        // Estrella de fondo (vacía - gris)
        SVGPath emptyStar = createStarSVG();
        emptyStar.setFill(Color.LIGHTGRAY);
        emptyStar.setScaleX(STAR_SIZE / 24.0);
        emptyStar.setScaleY(STAR_SIZE / 24.0);
        
        // Estrella llena (amarilla)
        SVGPath fullStar = createStarSVG();
        fullStar.setFill(Color.GOLD);
        fullStar.setScaleX(STAR_SIZE / 24.0);
        fullStar.setScaleY(STAR_SIZE / 24.0);
        
        // Clip para media estrella (mitad izquierda)
        Rectangle halfClip = new Rectangle(0, 0, STAR_SIZE / 2, STAR_SIZE);
        halfClip.setTranslateX(-STAR_SIZE / 4);
        
        // Estrella media (amarilla, solo mitad izquierda visible)
        SVGPath halfStar = createStarSVG();
        halfStar.setFill(Color.GOLD);
        halfStar.setScaleX(STAR_SIZE / 24.0);
        halfStar.setScaleY(STAR_SIZE / 24.0);
        halfStar.setClip(halfClip);
        
        // Inicialmente ocultas
        fullStar.setVisible(false);
        halfStar.setVisible(false);
        
        starPane.getChildren().addAll(emptyStar, halfStar, fullStar);
        
        // Manejar clicks para medias estrellas
        starPane.setOnMouseClicked(event -> {
            double clickX = event.getX();
            double halfWidth = starPane.getWidth() / 2;
            
            if (clickX <= halfWidth) {
                // Click en la mitad izquierda: media estrella
                currentRating = starIndex + 0.5;
            } else {
                // Click en la mitad derecha: estrella completa
                currentRating = starIndex + 1.0;
            }
            
            updateStarsDisplay();
        });
        
        // Cambiar cursor al pasar por encima
        starPane.setStyle("-fx-cursor: hand;");
        
        return starPane;
    }

    private SVGPath createStarSVG() {
        SVGPath star = new SVGPath();
        star.setContent(STAR_PATH);
        return star;
    }

    private void updateStarsDisplay() {
        for (int i = 0; i < NUM_STARS; i++) {
            StackPane starPane = (StackPane) starsContainer.getChildren().get(i);
            SVGPath halfStar = (SVGPath) starPane.getChildren().get(1);
            SVGPath fullStar = (SVGPath) starPane.getChildren().get(2);
            
            double starValue = i + 1;
            
            if (currentRating >= starValue) {
                // Estrella completa
                fullStar.setVisible(true);
                halfStar.setVisible(false);
            } else if (currentRating >= starValue - 0.5) {
                // Media estrella
                fullStar.setVisible(false);
                halfStar.setVisible(true);
            } else {
                // Estrella vacía
                fullStar.setVisible(false);
                halfStar.setVisible(false);
            }
        }
        
        ratingValueLabel.setText(String.format("%.1f", currentRating));
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    @FXML
    private void handleConfirm() {
        if (listener != null) {
            listener.onRatingSelected(currentRating);
        }
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) starsContainer.getScene().getWindow();
        stage.close();
    }
}
