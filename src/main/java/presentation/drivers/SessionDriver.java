package presentation.drivers;

import java.util.Scanner;

import domain.controller.UserController;

public class SessionDriver {
    private final Scanner sc = new Scanner(System.in);
    private final UserController userController;

    public SessionDriver(UserController userController) {
        this.userController = userController;
    }

    public void handleRegister() {
        String option;
        System.out.println("---REGISTRÁNDOSE---");
        do {
            System.out.println("Introduce tu nombre de usuario.");
            String username = sc.nextLine();
            System.out.println("Introduce tu email.");
            String email = sc.nextLine();
            System.out.println("Introduce tu contraseña.");
            String password = sc.nextLine();
            if (userController.registerUser(username, email, password)) {
                System.out.println("El usuario " + username + " se ha registrado con éxito.");
                handleLogin();
                return;
            } else {
                System.out.println("El usuario " + username + " ya existe.");
                System.out.println("1. Intentar de nuevo.");
                System.out.println("2. Volver al menú principal.");
                option = sc.nextLine();
            }
        } while (!option.equals("2"));
    }

    public void handleLogin() {
        String option;
        System.out.println("---INICIANDO SESIÓN---");
        do {

            System.out.println("Introduce tu nombre de usuario.");
            String username = sc.nextLine();
            System.out.println("Introduce tu contraseña");
            String password = sc.nextLine();
            boolean success = false;
            switch (userController.loginUser(username, password)) {
                case "user_not_exists" -> System.out.println("El usuario " + username + " no existe.");
                case "incorrect_password" -> System.out.println("La contraseña " + password + " no es válida.");
                default -> {
                    System.out.println("Login de " + username + " con éxito.");
                    System.out.println("Sesión iniciada.");
                    System.out.println("Estamos en obras :-(");
                    System.out.println("---CERRANDO SESIÓN---");
                    return;
                }
            }
            System.out.println("1. Intentar de nuevo.");
            System.out.println("2. Registrarse.");
            System.out.println("3. Volver al menú principal.");
            option = sc.nextLine();
            if (option.equals("2")) {
                handleRegister();
                return;
            }

        } while (!option.equals("3"));
    }
}
