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

    private void checkLoaderAddress(FXMLLoader loader)
    {
        if (loader.getLocation() == null) {
            System.err.println("FATAL: No se pudo encontrar la ruta al fichero fxml.");
            System.err.println("SOLUCIÓN: Modifica la ruta o mueve el fichero de ruta.");
            System.exit(1);
        }
    }

    private void finalizeScene(FXMLLoader loader, String title) throws Exception
    {
        Scene scene = new Scene(loader.load());

        this.primaryStage.setTitle(title);
        this.primaryStage.setScene(scene);
        this.primaryStage.show();
    }
    public void initStage()
    {
        if(this.primaryStage== null)
        {
            System.err.println("ERROR FATAL: El stage es null");
            System.err.println("SOULUCIÓN: llamar primero a setPrimaryStage()");
            System.exit(1);
        }

        this.primaryStage.setResizable(true);
        this.primaryStage.setMinHeight(700);
        this.primaryStage.setMinWidth(1000);
        double centerX = this.primaryStage.getWidth()/2;
        double centerY = this.primaryStage.getHeight()/2;
        this.primaryStage.setX(centerX);
        this.primaryStage.setY(centerY);
        this.primaryStage.setMaximized(false);

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

    public void showRegister(){
        try{
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/register.fxml")); // carga el fichero fxml
            checkLoaderAddress(loader); // verifica que la carga se ha hecho correctamente

            // Inicializa el controlador de la vista
            RegisterViewController registerViewController = new RegisterViewController(userController, this);
            loader.setController(registerViewController); // asigna el controlador de la vista al fichero fxml

            finalizeScene(loader, "REGISTRO");
        }catch (Exception e)
        {
            System.err.println("ERROR FATAL: " + e.getMessage());
        }
    }

    public void showMainMenu() {
        this.primaryStage.setTitle("Main Menu");
    }
}