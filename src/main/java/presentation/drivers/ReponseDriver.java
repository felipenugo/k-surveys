package presentation.drivers;

public class ReponseDriver {
    private final EditorResponseDriver editorResponseDriver;

    public ReponseDriver(EditorResponseDriver editorResponseDriver) {
        this.editorResponseDriver = editorResponseDriver;
    }

    public void responseMenu() {
        boolean exitResponseMenu = false;
        do {
            System.out.println("Estamos en obras :-(");
            exitResponseMenu = true;
        } while (!exitResponseMenu);
        System.out.println("--- SALIENDO DE RESPONDER ENCUESTAS ---");
    }
}
