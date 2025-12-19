package presentation.main;

import data.ResponseRepository;
import data.SurveyRepository;
import data.UserRepository;
import domain.controller.SurveyController;
import domain.controller.UserController;
import domain.controller.CtrlDominioClustering;
import domain.controller.ResponseController;
import domain.service.SurveyService;
import domain.service.UserService;
import javafx.application.Application;
import javafx.stage.Stage;
import presentation.views.SceneManager;

import java.io.IOException;

public class Main extends Application {

    private void iniStage(Stage primaryStage)
    {

    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        // --- DEPENDENCY INJECTION ---
        // This is where we create the core components of our application
        
        // 1. Repository: Handles data storage and retrieval.
        UserRepository userRepository = new UserRepository();
        SurveyRepository surveyRepository = new SurveyRepository();
        ResponseRepository responseRepository = new ResponseRepository(); // Instancia única
        
        // 2. Service: Contains the business logic.
        UserService userService = new UserService(userRepository, surveyRepository, responseRepository);
        
        // 3. Controller: Connects the UI to the business logic.
        UserController userController = new UserController(userService);
        SurveyService surveyService = new SurveyService(surveyRepository, userController, userService);
        SurveyController surveyController = new SurveyController(surveyService);
        
        // Clustering Controller
        CtrlDominioClustering clusteringController = new CtrlDominioClustering(responseRepository, surveyRepository);
        
        // Response Controller
        ResponseController responseController = new ResponseController(
                new domain.service.ResponseService(responseRepository, userController, surveyService)
        );
        
        // 4. SceneManager: Manages scene transitions and passes dependencies to view controllers.
        //SceneManager.initStage();
        SceneManager sceneManager = new SceneManager(userController, surveyController, clusteringController, responseController);
        sceneManager.setPrimaryStage(primaryStage);
        
        // --- APPLICATION START ---
        // Initialize the stage and show the first scene
        sceneManager.initStage();
        sceneManager.showLogin();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
