package presentation.main;

import data.UserRepository;
import domain.controller.UserController;
import domain.service.UserService;
import javafx.application.Application;
import javafx.stage.Stage;
import presentation.views.SceneManager;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        // --- DEPENDENCY INJECTION ---
        // This is where we create the core components of our application
        
        // 1. Repository: Handles data storage and retrieval.
        UserRepository userRepository = new UserRepository();
        
        // 2. Service: Contains the business logic.
        UserService userService = new UserService(userRepository);
        
        // 3. Controller: Connects the UI to the business logic.
        UserController userController = new UserController(userService);
        
        // 4. SceneManager: Manages scene transitions and passes dependencies to view controllers.
        SceneManager sceneManager = new SceneManager(userController);
        sceneManager.setPrimaryStage(primaryStage);
        
        // --- APPLICATION START ---
        // Initialize the stage and show the first scene
        sceneManager.showLogin();
        sceneManager.initStage();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
