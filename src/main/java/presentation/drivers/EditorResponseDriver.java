package presentation.drivers;

import java.util.Scanner;

public class EditorResponseDriver {
    private final Scanner sc = new Scanner(System.in);

    private int selectEditorResponseMenu() {
        System.out.println("--- EDITOR DE RESPUESTAS ---");
        System.out.println("1. VER PREGUNTAS.");
        System.out.println("2. RESPONDER PREGUNTA");
        System.out.println("3. VER MIS RESPUESTAS");
        System.out.println("4. ENVIAR RESPUESTA");
        System.out.print("Opción: ");
        return sc.nextInt();
    }

    public void editorResponseMenu() {
        boolean exit = false;
        do {
            switch (selectEditorResponseMenu()) {
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
