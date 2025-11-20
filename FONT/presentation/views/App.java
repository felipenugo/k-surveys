package presentation.views;

import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {

    private static SceneManager globalSceneManager;

    public static void setGlobalSceneManager(SceneManager manager) {
        globalSceneManager = manager;
    }

    @Override
    public void start(Stage stage) {
        SceneManager manager = globalSceneManager;
        if (manager == null) {
            System.err.println("FATAL: La instancia de SceneManager es null.");
            System.err.println("SOLUCTIÓN: Asigan un sceneManager con el método setSceneManager antes de lanzar la app.");
            System.exit(1);
        }
        manager.setPrimaryStage(stage);
        manager.showLogin();
    }

    public static void main(String[] args) {
        launch(args);
    }
}