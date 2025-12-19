package presentation.views;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

public class App extends Application {

    private static SceneManager globalSceneManager;

    public static void setGlobalSceneManager(SceneManager manager) {
        globalSceneManager = manager;
    }

    @Override
    public void start(Stage stage) {
        // Set up global exception handler
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            throwable.printStackTrace(); // Keep logging the error
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Error Inesperado");
                alert.setHeaderText("Ha habido un ERROR FATAL, ¿deseas salir de la aplicación?");
                alert.setContentText("Se ha producido un error no controlado. Se recomienda salir.");

                alert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        Platform.exit();
                    }
                });
            });
        });

        SceneManager manager = globalSceneManager;
        if (manager == null) {
            System.err.println("FATAL: La instancia de SceneManager es null.");
            System.err.println("SOLUCTIÓN: Asigan un sceneManager con el método setSceneManager antes de lanzar la app.");
            System.exit(1);
        }
        manager.setPrimaryStage(stage); // pasar el stage
        manager.initStage(); // inicializar los parámetros generales del stage que usaran todas las vistas
        manager.showRegister(); // cargar primera vista
    }

    public static void main(String[] args) {
        launch(args);
    }
}