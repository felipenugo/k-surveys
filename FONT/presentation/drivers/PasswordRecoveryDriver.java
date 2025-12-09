package presentation.drivers;

import java.util.Scanner;

import domain.controller.UserController;
import domain.exception.LogInException;
import domain.exception.RegisterException;

public class PasswordRecoveryDriver {

    private final UserController userController;
    private final Scanner sc = new Scanner(System.in);

    public PasswordRecoveryDriver(UserController userController) {
        this.userController = userController;
    }

    public void recoveryMenu() {

        System.out.println("\n--- RECUPERAR CONTRASEÑA ---");

        try {
            // 1. Pedir username
            System.out.print("Introduce tu nombre de usuario: ");
            String username = sc.nextLine().trim();

            // 2. Verificar que el usuario exista y obtener pregunta secreta
            String question = userController.startPasswordRecovery(username);

            System.out.println("\nPregunta secreta: " + question);

            // 3. Pedir respuesta secreta
            System.out.print("Introduce tu respuesta secreta: ");
            String answer = sc.nextLine().trim();

            boolean correct = userController.verifySecurityAnswer(username, answer);

            if (!correct) {
                System.out.println("Respuesta incorrecta. No se puede recuperar la contraseña.");
                return;
            }

            // 4. Pedir nueva contraseña
            System.out.print("Introduce tu nueva contraseña: ");
            String newPassword = sc.nextLine().trim();

            userController.resetPassword(username, newPassword);

            System.out.println("Contraseña actualizada correctamente.");
            System.out.println("Volviendo al menú principal...");

        } catch (LogInException | RegisterException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }
}
