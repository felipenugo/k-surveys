package presentation.drivers;

import java.util.Scanner;

import domain.exception.LogInException;
import domain.exception.RegisterException;

import domain.controller.UserController;
import presentation.driverMain.DriverMain;

public class SessionDriver {
    private final Scanner sc = DriverMain.getScanner();
    private final UserController userController;
    private AppDriver appDriver;

    public SessionDriver(UserController userController) {
        this.userController = userController;
    }

    public void setAppDriver(AppDriver appDriver) {
        this.appDriver = appDriver;
    }

    private String displayRegisterErrorMenu() {
        System.out.println("1. Intentar registrarse de nuevo.");
        System.out.println("2. Volver al menú principal.");
        System.out.print("Opción: ");
        return sc.nextLine();
    }

    private String displayLoginErrorMenu() {
        System.out.println("1. Intentar inciar sesión de nuevo.");
        System.out.println("2. Registrarse.");
        System.out.println("3. Volver al menú principal.");
        System.out.print("Opción: ");
        return sc.nextLine();
    }

    public void clearTerminal() {
        for (int i = 0; i < 50; i++)
            System.out.println();
    }

    public void driverRegister() {
        boolean exitRegister = false;
        clearTerminal();
        do {
            System.out.println("--- REGISTRO DE USUARIO ---");
            System.out.print("Introduce tu nombre de usuario: ");
            String username = sc.nextLine();
            System.out.print("Introduce tu email: ");
            String email = sc.nextLine();
            System.out.print("Introduce tu contraseña: ");
            String password = sc.nextLine();

            try {
                System.out.println("--- registrándose ---");
                userController.registerUser(username, email, password);
                System.out.println("El usuario " + username + " se ha registrado con éxito.");
                exitRegister = true;
                driverLogin(); // login exceptions will be handled in this method
            } catch (RegisterException e) {
                System.out.println("Error: " + e.getMessage());
                boolean exitErrorMenu = false;
                while (!exitErrorMenu) {
                    switch (displayRegisterErrorMenu()) {
                        case "1" -> exitErrorMenu = true;
                        case "2" -> {
                            exitErrorMenu = true;
                            exitRegister = true;
                        }
                        default -> System.out.println("Opción no válida. Selecciona una opcion del menu.");
                    }
                }
            }
        } while (!exitRegister);
    }

    public void driverLogin() {
        boolean exitLogin = false;
        clearTerminal();
        do {
        System.out.println("--- INICIO DE SESIÓN ---");
            System.out.print("Introduce tu nombre de usuario:");
            String username = sc.nextLine();
            System.out.print("Introduce tu contraseña:");
            String password = sc.nextLine();

            try {
                System.out.println("--- iniciando sesión ---");
                userController.loginUser(username, password);
                System.out.println("Inicio de sesión correcto!");
                System.out.println("Bienvenido " + username);
                appDriver.appMenu();
                exitLogin = true;
            } catch (LogInException e) {
                System.out.println("Error: " + e.getMessage());
                boolean exitErrorMenu = false;
                while (!exitErrorMenu) {
                    switch (displayLoginErrorMenu()) {
                        case "1" -> exitErrorMenu = true;
                        case "2" -> {
                            exitErrorMenu = true;
                            exitLogin = true;
                            driverRegister();
                        }
                        case "3" -> {
                            exitErrorMenu = true;
                            exitLogin = true;
                        }
                        default -> System.out.println("Opción no válida. Selecciona una opcion del menu.");
                    }
                }
            }
        } while (!exitLogin);
    }

    public void logout() {
        System.out.println("--- CERRANDO SESIÓN ---");
        try {
            String username = userController.getUsernameLoggedIn();
            userController.logoutUser();
            System.out.println("Sesión cerrada, hasta pronto " + username + ".");
        } catch (LogInException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
