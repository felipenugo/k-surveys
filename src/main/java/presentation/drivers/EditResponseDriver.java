package presentation.drivers;

import domain.controller.ResponseController;
import domain.model.*;

import java.util.Scanner;
import java.util.List;

public class EditResponseDriver {
    private final ResponseController responseController;
    private final Scanner sc = new Scanner(System.in);

    public EditResponseDriver(ResponseController responseControler)
    {
        this.responseController = responseControler;
    }

    private int selectEditResponseMenu() {
        System.out.println("--- EDITOR DE RESPUESTAS ---");
        System.out.println("1. VER PREGUNTAS.");
        System.out.println("2. RESPONDER PREGUNTA");
        System.out.println("3. VER MIS RESPUESTAS");
        System.out.println("4. ENVIAR RESPUESTA");
        System.out.print("Opción: ");
        return sc.nextInt();
    }

    public void showQuestions(String surveyid) {

        List<Question> questions = responseController.getQuestions(surveyid);

        System.out.println("PULSA CUALQUIER TECLA PARA SALIR.");
        String tmp = sc.nextLine();
    }

    public void editResponseMenu(String surveyId, String responseid) {
        boolean exit = false;
        do {
            switch (selectEditResponseMenu()) {
                case 1 -> System.out.println("ver preguntas");
                case 2 -> System.out.println("responder pregunta");
                case 3 -> System.out.println("ver mis respuestas");
                case 4 -> exit = true;
                default -> System.out.println("Opción no válida. Selecciona una opción del menú");
            }
        } while (!exit);
        System.out.println("--- enviando respuesta ---");
    }
}
