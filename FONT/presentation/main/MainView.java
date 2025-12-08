package presentation.main;

import javafx.application.Application;
import presentation.views.App;

import domain.controller.*;
import domain.service.*;
import data.*;
import presentation.views.*;

// Esta clase es el antiguo DriverMain
public class MainView {
    public static void main(String[] args) {

        // Inicializar repositorios
        UserRepository userRepository = new UserRepository();
        SurveyRepository surveyRepository = new SurveyRepository();
        QuestionRepository questionRepository = new QuestionRepository();
        ResponseRepository responseRepository = new ResponseRepository();
        AnswerRepository answerRepository = new AnswerRepository();

        // Inicializar controladores inyectando servicio con repositorio
        UserService userService = new UserService(userRepository);
        UserController userController = new UserController(userService);

        SurveyService surveyService = new SurveyService(surveyRepository, userController, userService);
        SurveyController surveyController = new SurveyController(surveyService);

        QuestionService questionService = new QuestionService(questionRepository, userController);
        QuestionController questionController = new QuestionController(questionService);

        ResponseController responseController = new ResponseController(new ResponseService(responseRepository, userController, surveyService));

        AnswerController answerController = new AnswerController(new AnswerService(answerRepository, userController));

        CtrlDominioClustering ctrlDominioClustering = new CtrlDominioClustering(responseRepository, surveyRepository);

        SceneManager sceneManager = new SceneManager(userController, surveyController);
        App.setGlobalSceneManager(sceneManager);
        Application.launch(App.class, args);
    }
}
