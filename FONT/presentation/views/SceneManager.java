package presentation.views;

import javafx.scene.Parent;
import domain.model.Survey;
import domain.controller.ResponseController;
import domain.controller.SurveyController;
import domain.controller.UserController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.stage.StageStyle;

import java.io.IOException;

public class SceneManager {
    private final UserController userController;
    private final SurveyController surveyController;
    private Stage primaryStage;
    private final ResponseController responseController;

    public SceneManager(UserController userController, SurveyController surveyController, ResponseController responseController) {
        this.userController = userController;
        this.surveyController = surveyController;
        this.responseController = responseController;
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
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/register.fxml")); // carga el fichero fxml
            checkLoaderAddress(loader); // verifica que la carga se ha hecho correctamente

            // Inicializa el controlador de la vista
            RegisterViewController registerViewController = new RegisterViewController(userController, this);
            loader.setController(registerViewController); // asigna el controlador de la vista al fichero fxml

            finalizeScene(loader, "REGISTRO");
        } catch (Exception e) {
            System.err.println("ERROR FATAL: " + e.getMessage());
        }
    }

    public void showHome() {
        try{
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/home.fxml"));
            checkLoaderAddress(loader);

            HomeViewController homeViewController = new HomeViewController(userController, surveyController, this);
            loader.setController(homeViewController);

            finalizeScene(loader, "INICIO");
        }catch(Exception e){
            System.err.println("ERROR FATAL HOME: " + e.getMessage());
            e.printStackTrace();
        }

        this.primaryStage.setTitle("Main Menu");

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
        this.primaryStage.setTitle("Crear Encuesta");
    }

    public void showMySurveys() {
        this.primaryStage.setTitle("Mis Encuestas");
    }

    public void showMyDrafts() {
        this.primaryStage.setTitle("Mis Borradores");
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
}