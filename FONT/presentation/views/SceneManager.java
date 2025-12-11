package presentation.views;

import domain.controller.SurveyController;
import domain.controller.UserController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import presentation.util.SurveyView;

public class SceneManager {
    private final UserController userController;
    private final SurveyController surveyController;
    private Stage primaryStage;

    public SceneManager(UserController userController, SurveyController surveyController) {
        this.userController = userController;
        this.surveyController = surveyController;
    }

    public void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
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

    public void showAnswerSurvey(String surveyId)
    {
        this.primaryStage.setTitle("Responder Encuesta con id: " + surveyId + "");
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

    public void showClustering(String surveyId) {
        this.primaryStage.setTitle("Clustering de Encuestas con id: " + surveyId + "");
    }
}

