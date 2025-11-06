package presentation.drivers;

import java.util.Scanner;

import domain.exception.RegisterException;

import domain.controller.UserController;

public class SessionDriver {
    private final Scanner sc = new Scanner(System.in);
    private final UserController userController;

    public SessionDriver(UserController userController) {
        this.userController = userController;
    }

    private String displayRegisterErrorMenu() {
        System.out.println("1. Intentar registrarse de nuevo.");
        System.out.println("2. Volver al menú principal.");
        System.out.print("Opción: ");
        return sc.nextLine();
    }

    public void driverRegister() {
        boolean exitRegister = false;
        System.out.println("--- REGISTRO DE USUARIO ---");
        do {
            System.out.print("Introduce tu nombre de usuario: ");
            String username = sc.nextLine();
            System.out.print("Introduce tu email: ");
            String email = sc.nextLine();
            System.out.print("Introduce tu contraseña: ");
            String password = sc.nextLine();

            try {
                System.out.println("--- REGISTRÁNDOSE ---");
                userController.registerUser(username, email, password);
                System.out.println("El usuario " + username + " se ha registrado con éxito.");
                handleLogin(); // login exceptions will be handled in this method
                exitRegister = true;
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

    public void handleLogin() {
        String option;
        System.out.println("--- INICIANDO SESIÓN ---");
        do {

            System.out.println("Introduce tu nombre de usuario.");
            String username = sc.nextLine();
            System.out.println("Introduce tu contraseña");
            String password = sc.nextLine();
            switch (userController.loginUser(username, password)) {
                case "user_not_exists" -> System.out.println("El usuario " + username + " no existe.");
                case "incorrect_password" -> System.out.println("La contraseña " + password + " no es válida.");
                default -> {
                    System.out.println("Inicio de sesión de " + username + " con éxito.");
                    System.out.println("Sesión iniciada.");
                    System.out.println("Estamos en obras :-(");
                    System.out.println("--- CERRANDO SESIÓN ---");
                    return;
                }
            }
            System.out.println("1. Intentar de nuevo.");
            System.out.println("2. Registrarse.");
            System.out.println("3. Volver al menú principal.");
            option = sc.nextLine();
            if (option.equals("2")) {
                driverRegister();
                return;
            }

        } while (!option.equals("3"));
    }
}