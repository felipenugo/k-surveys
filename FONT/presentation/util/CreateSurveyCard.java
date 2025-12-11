package presentation.util;

import domain.model.Survey;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import presentation.views.SceneManager;

import java.time.format.DateTimeFormatter;

/**
 * Clase de utilidad responsable de la construcción de una Card de una Encuesta con JavaFX
 */
public class CreateSurveyCard {

    /**
     * Construye y devuelve el componente principal (HBox) que representa
     * una Card de Encuesta completa, lista para ser añadida a un contenedor.
     * * Estructura generada: [ Título/Autor ] | [ Views ] | [ Rating ] | [ Date ]
     *
     * @param sceneManager El gestor de escenas, necesario para enlazar el evento
     *                     de clic de la Card con la navegación a la vista de respuesta.
     * @param s            La encuesta (Survey) cuyos datos serán mostrados en la Card.
     * @return Un HBox configurado que representa la Card de la encuesta.
     */
    public static HBox getSurveyCard(SceneManager sceneManager, Survey s, SurveyView surveyView) {
        HBox card = new HBox();
        card.getStyleClass().add("survey-card");
        switch (surveyView) {
            case HOME -> card.setOnMouseClicked(e -> sceneManager.showAnswerSurvey(s.getSURVEY_ID()));
            case MY_SURVEYS -> card.setOnMouseClicked(e -> sceneManager.showClustering(s.getSURVEY_ID()));
            case DRAFTS -> card.setOnMouseClicked(e -> sceneManager.showEditSurvey(s.getSURVEY_ID()));
        }
        // Crear columnas
        VBox colMain = createMainColumn(s);
        HBox colViews = createViewsColumn(s);
        HBox colRating = createRatingColumn(s);
        HBox colDate = createDateColumn(s);

        // Añadir columnas con divisores
        addColumnsWithDividers(card, colMain, colViews, colRating, colDate);

        // Ajustar tamaño para que sea responsive
        HBox.setHgrow(colMain, Priority.ALWAYS);
        HBox.setHgrow(colViews, Priority.ALWAYS);
        HBox.setHgrow(colRating, Priority.ALWAYS);
        HBox.setHgrow(colDate, Priority.ALWAYS);

        return card;
    }

    // --- CREACIÓN DE COLUMNAS ---

    /**
     * Crea la columna principal de la card que contiene el título y el creador.
     *
     * @param s La encuesta.
     * @return Un VBox con el título y el creador.
     */
    private static VBox createMainColumn(Survey s) {
        VBox colMain = new VBox();
        colMain.getStyleClass().add("col");
        colMain.setMinWidth(250);

        Label title = new Label(s.getTitle());
        title.getStyleClass().add("survey-title");

        Label creator = new Label(s.getCREATOR_USERNAME());
        creator.getStyleClass().add("survey-creator");

        colMain.getChildren().addAll(title, creator);

        return colMain;
    }

    /**
     * Crea la columna que muestra el número de vistas de la encuesta.
     *
     * @param s La encuesta.
     * @return Un HBox con el ícono de ojo y el número de vistas.
     */
    private static HBox createViewsColumn(Survey s) {
        String eyeSvg = "M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5M12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5m0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3";
        HBox colViews = createDataCell(String.valueOf(s.getViews()), eyeSvg);
        colViews.getStyleClass().add("col");
        colViews.setMinWidth(70);
        return colViews;
    }

    /**
     * Crea la columna que muestra la valoración promedio de la encuesta.
     *
     * @param s La encuesta.
     * @return Un HBox con las estrellas de valoración.
     */
    private static HBox createRatingColumn(Survey s) {
        HBox colRating = createRatingCell(s.getAvgRating());
        colRating.setMinWidth(150);
        return colRating;
    }

    /**
     * Crea la columna que muestra la fecha de publicación de la encuesta.
     *
     * @param s La encuesta.
     * @return Un HBox con el ícono de calendario y la fecha.
     */
    private static HBox createDateColumn(Survey s) {
        String dateStr = s.getPUBLISHED_AT().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String calSvg = "M19 3h-1V1h-2v2H8V1H6v2H5c-1.11 0-1.99.9-1.99 2L3 19c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H5V8h14v11zM7 10h5v5H7z";
        HBox colDate = createDataCell(dateStr, calSvg);
        colDate.setMinWidth(120);
        return colDate;
    }

    /**
     * Añade la columna principal seguida de las demás columnas, separadas por divisores verticales.
     *
     * @param card    El HBox contenedor de la card.
     * @param colMain La columna principal (ya añadida).
     * @param columns Las columnas secundarias a añadir.
     */
    private static void addColumnsWithDividers(HBox card, VBox colMain, HBox... columns) {
        card.getChildren().add(colMain); // primera columna
        for (HBox col : columns) {
            card.getChildren().add(createVerticalDivider());
            card.getChildren().add(col);
        }
    }

    /**
     * Crea un separador vertical estilizado para dividir las columnas.
     *
     * @return Un objeto Region estilizado.
     */
    private static Region createVerticalDivider() {
        Region r = new Region();
        r.getStyleClass().add("v-divider");
        return r;
    }

    /**
     * Crea una celda HBox genérica para mostrar un ícono SVG y un texto.
     *
     * @param text    El texto a mostrar.
     * @param svgPath La cadena SVG que define el ícono.
     * @return Un HBox que actúa como celda de datos.
     */
    private static HBox createDataCell(String text, String svgPath) {
        HBox cell = new HBox(5);
        cell.setAlignment(Pos.CENTER);

        SVGPath icon = new SVGPath();
        icon.setContent(svgPath);
        icon.getStyleClass().add("col-icon");

        Label lbl = new Label(text);
        lbl.getStyleClass().add("col-text");
        cell.getChildren().addAll(icon, lbl);
        return cell;
    }

    /**
     * Crea una celda HBox para mostrar la valoración promedio y el número.
     *
     * @param rating La valoración promedio (double).
     * @return Un HBox con las estrellas y el número de valoración.
     */
    private static HBox createRatingCell(double rating) {
        HBox cell = new HBox(5);
        cell.setAlignment(Pos.CENTER);

        HBox starsBox = createStarsBox(rating);

        Label ratingNum = new Label(String.valueOf(rating));
        ratingNum.getStyleClass().add("col-text");

        cell.getChildren().addAll(starsBox, ratingNum);
        return cell;
    }

    /**
     * Crea un HBox que contiene 5 íconos de estrellas SVG, marcando las estrellas llenas y medias
     * según la valoración proporcionada.
     *
     * @param rating La valoración (ej. 4.5).
     * @return Un HBox con las estrellas renderizadas.
     */
    private static HBox createStarsBox(double rating) {
        HBox starsBox = new HBox();
        String starSvg = "M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z";

        for (int i = 1; i <= 5; i++) {
            SVGPath star = new SVGPath();
            star.setContent(starSvg);
            star.getStyleClass().add("star");

            if (rating >= i) {
                star.getStyleClass().add("full");
            } else if (rating >= i - 0.5) {
                star.getStyleClass().add("half");
            }
            starsBox.getChildren().add(star);
        }

        return starsBox;
    }
}
