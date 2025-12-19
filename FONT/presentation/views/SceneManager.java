package presentation.views;

import javafx.scene.Parent;
import domain.model.Survey;
import domain.controller.ResponseController;
import domain.controller.SurveyController;
import domain.controller.UserController;
import domain.controller.CtrlDominioClustering;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import presentation.util.SurveyView;

public class SceneManager {
    private final UserController userController;
    private final SurveyController surveyController;
    private final CtrlDominioClustering clusteringController;
    private final ResponseController responseController;
    private Stage primaryStage;

    public SceneManager(UserController userController, SurveyController surveyController, CtrlDominioClustering clusteringController, ResponseController responseController) {
        this.userController = userController;
        this.surveyController = surveyController;
        this.clusteringController = clusteringController;
        this.responseController = responseController;
    }

    public void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public CtrlDominioClustering getClusteringController() {
        return clusteringController;
    }

    private void checkLoaderAddress(FXMLLoader loader) {
        if (loader.getLocation() == null) {
            System.err.println("FATAL: No se pudo encontrar la ruta al fichero fxml.");
            System.err.println("SOLUCIÓN: Modifica la ruta o mueve el fichero de ruta.");
            System.exit(1);
        }
    }

    private void finalizeScene(FXMLLoader loader, String title) throws Exception {
        Scene currentScene = primaryStage.getScene();

        if (currentScene == null) {
            currentScene = new Scene(loader.load());
            primaryStage.setScene(currentScene);
        } else {
            // Reemplaza el Root con el nuevo contenido
            currentScene.setRoot(loader.load());
        }
        primaryStage.setTitle(title);
    }


    public void initStage() {
        if (primaryStage == null) {
            System.err.println("ERROR FATAL: El stage es null");
            System.err.println("SOLUCIÓN: llamar primero a setPrimaryStage()");
            System.exit(1);
        }

        primaryStage.centerOnScreen();
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(800);
       // primaryStage.getIcons().add(../img/logo.png);
        primaryStage.show();
    }

    public void showLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/login.fxml"));
            checkLoaderAddress(loader);

            LoginViewController loginViewController = new LoginViewController(userController, this);
            loader.setController(loginViewController);

            finalizeScene(loader, "INICIO DE SESIÓN");
        } catch (Exception e) {
            System.err.println("ERROR FATAL: " + e.getMessage());
        }
    }

    public void showRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/register.fxml"));
            checkLoaderAddress(loader);

            RegisterViewController registerViewController = new RegisterViewController(userController, this);
            loader.setController(registerViewController);

            finalizeScene(loader, "REGISTRO");
        } catch (Exception e) {
            System.err.println("ERROR FATAL: " + e.getMessage());
        }
    }

    public void showHome() {
        try{
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/surveys.fxml"));
            checkLoaderAddress(loader);

            HomeViewController homeViewController = new HomeViewController(userController, surveyController, SurveyView.HOME, this);
            loader.setController(homeViewController);

            finalizeScene(loader, "INICIO");
        }catch(Exception e){
            System.err.println("ERROR FATAL VIEW INICIO: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void showMySurveys()
    {
        try{
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/surveys.fxml"));
            checkLoaderAddress(loader);

            MySurveysViewController mySurveysViewController = new MySurveysViewController(userController, surveyController, SurveyView.MY_SURVEYS, this);
            loader.setController(mySurveysViewController);

            finalizeScene(loader, "MIS ENCUESTAS");
        }catch(Exception e){
            System.err.println("ERROR FATAL VIEW MIS ENCUESTAS: " + e.getMessage());
            e.printStackTrace();
        }

    }

    public void showMySurveysDrafts()
    {
        try{
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/surveys.fxml"));
            checkLoaderAddress(loader);
            MySurveyDraftsViewController mySurveyDraftsViewController = new MySurveyDraftsViewController(userController, surveyController, SurveyView.DRAFTS, this);
            loader.setController(mySurveyDraftsViewController);

            finalizeScene(loader, "MIS BORRADORES");
        }catch(Exception e){
            System.err.println("ERROR FATAL HOME: " + e.getMessage());
            e.printStackTrace();
        }

    }

    public void showPasswordRecovery() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/reset_password_step1.fxml"));
            checkLoaderAddress(loader);

            ResetPasswordStep1Controller controller =
                    new ResetPasswordStep1Controller(userController, this);
            loader.setController(controller);

            finalizeScene(loader, "RECUPERAR CONTRASEÑA — PASO 1");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showPasswordRecoveryStep2(String username, String question) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/reset_password_step2.fxml"));
            checkLoaderAddress(loader);

            ResetPasswordStep2Controller controller =
                    new ResetPasswordStep2Controller(userController, this, username, question);
            loader.setController(controller);

            finalizeScene(loader, "RECUPERAR CONTRASEÑA — PASO 2");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showPasswordRecoveryStep3(String username) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/reset_password_step3.fxml"));
            checkLoaderAddress(loader);

            ResetPasswordStep3Controller controller =
                    new ResetPasswordStep3Controller(userController, this, username);
            loader.setController(controller);

            finalizeScene(loader, "RECUPERAR CONTRASEÑA — PASO 3");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showCreateSurvey() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/create-survey.fxml"));
            checkLoaderAddress(loader);

            CreateSurveyViewController controller = new CreateSurveyViewController(userController, surveyController, this);
            loader.setController(controller);

            finalizeScene(loader, "CREAR ENCUESTA");
        } catch (Exception e) {
            System.err.println("ERROR FATAL VIEW CREAR ENCUESTA: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void showDeleteAccountConfirm() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/delete_account.fxml"));

            DeleteAccountViewController controller =
                    new DeleteAccountViewController(userController, this);
            loader.setController(controller);

            // Cargar como una nueva ventana aparte
            Scene scene = new Scene(loader.load());
            Stage popup = new Stage();
            popup.initStyle(StageStyle.TRANSPARENT);
            popup.setScene(scene);
            popup.show();

        } catch (Exception e) {
            System.err.println("ERROR al cargar delete_account.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void showAnswerSurvey(String surveyId) {
        try {
            Survey survey = surveyController.getSurvey(surveyId);

            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/answer_survey.fxml"));
            AnswerSurveyViewController controller =
                    new AnswerSurveyViewController(responseController, this, userController, surveyController);
            loader.setController(controller);

            Parent root = loader.load();
            controller.loadSurvey(survey);

            primaryStage.getScene().setRoot(root);

        } catch (Exception e) {
            System.err.println("ERROR cargando vista de responder encuesta: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void showConfirmLeave(Runnable onConfirm) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/confirm_leave.fxml"));
            Parent root = loader.load();

            ConfirmLeaveController controller = loader.getController();
            controller.setOnConfirm(onConfirm);

            Stage popup = new Stage();
            popup.initStyle(StageStyle.UNDECORATED);
            popup.initOwner(primaryStage);
            popup.setScene(new Scene(root));
            popup.show();

        } catch (Exception e) {
            System.err.println("ERROR cargando confirm_leave.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void showRatingPopup(RatingPopupController.RatingListener listener) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/rating_popup.fxml"));
            Parent root = loader.load();

            RatingPopupController controller = loader.getController();
            controller.setListener(listener);

            Stage popup = new Stage();
            popup.initModality(Modality.APPLICATION_MODAL);
            popup.setTitle("Valoración");
            popup.setScene(new Scene(root));
            popup.setResizable(false);
            popup.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error cargando popup de valoración: " + e.getMessage());
        }
    }

    public void showEditSurvey(String surveyId) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/edit-survey.fxml"));
            checkLoaderAddress(loader);

            EditSurveyViewController controller = new EditSurveyViewController(userController, surveyController, this, surveyId);
            loader.setController(controller);

            finalizeScene(loader, "EDITAR ENCUESTA");
        } catch (Exception e) {
            System.err.println("ERROR FATAL VIEW EDITAR ENCUESTA: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void showViewSurvey(String surveyId) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/view-survey.fxml"));
            checkLoaderAddress(loader);

            ViewSurveyViewController controller = new ViewSurveyViewController(userController, surveyController, this, surveyId);
            loader.setController(controller);

            finalizeScene(loader, "VER ENCUESTA");
        } catch (Exception e) {
            System.err.println("ERROR FATAL VIEW VER ENCUESTA: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void showClustering(String surveyId) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/clustering.fxml"));
            checkLoaderAddress(loader);

            ClusteringViewController controller = new ClusteringViewController(
                    userController, surveyController, clusteringController, this, surveyId
            );
            loader.setController(controller);

            finalizeScene(loader, "CLUSTERING - Encuesta " + surveyId);
        } catch (Exception e) {
            System.err.println("ERROR FATAL CLUSTERING: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void showViewResponses(String surveyId) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/view-responses.fxml"));
            checkLoaderAddress(loader);

            ViewResponsesViewController controller = new ViewResponsesViewController(
                    userController, surveyController, responseController, this, surveyId
            );
            loader.setController(controller);

            finalizeScene(loader, "RESPUESTAS - Encuesta");
        } catch (Exception e) {
            System.err.println("ERROR FATAL VIEW RESPONSES: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
