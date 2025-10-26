package presentation.driverMain;

import java.util.Scanner;

import presentation.drivers.SessionDriver;

public class DriverMain {
    public static void displayWelcomeOptions() {
        System.out.println("INTRODUCE UN NÚMERO PARA SELECCIONAR UNA OPCIÓN:");
        System.out.println("1. INICIAR SESIÓN");
        System.out.println("2. REGISTRARSE");
        System.out.println("3. SALIR");
    }

    public static void displayWelcomeMessage() {
        System.out.println("-----------------------------------");
        System.out.println("BIENVENIDO A K-SURVEY");
    }

    public static void main(String[] var0) {
        Scanner sc = new Scanner(System.in);
        SessionDriver sessionDriver = new SessionDriver();
        displayWelcomeMessage();
        boolean exit = false;
        while (!exit) {
            displayWelcomeOptions();
            String option = sc.nextLine();
            switch (option) {
                case "1":
                    sessionDriver.handleLogin();
                    break;
                case "2":

                    sessionDriver.handleRegister();
                    break;
                case "3":
                    System.out.println("---SALIENDO---");
                    exit = true;
                    break;
                default:
                    System.out.println("Opción no válida.");

            }
        }

    }
}
