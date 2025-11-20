package presentation.views;

import domain.controller.UserController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneManager {
    private final UserController userController;
    private Stage primaryStage;

    public SceneManager(UserController userController) {
        this.userController = userController;
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    public void showLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/login.fxml"));

            if (loader.getLocation() == null) {
                System.err.println("FATAL: No se pudo encontrar la ruta al fichero fxml.");
                System.err.println("SOLUCIÓN: Modifica la ruta o mueve el fichero de ruta.");
                System.exit(1);
            }

            LoginViewController controller = new LoginViewController(userController, this);
            loader.setController(controller);

            Scene scene = new Scene(loader.load());

            primaryStage.setTitle("Login App");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error FATAL al cargar el FXML: " + e.getMessage());
        }
    }

    public void showMainMenu() {
        primaryStage.setTitle("Main Menu");
    }
}