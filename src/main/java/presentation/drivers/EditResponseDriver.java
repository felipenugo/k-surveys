package presentation.drivers;

import domain.controller.ResponseController;
import domain.exception.*;
import domain.model.*;
import domain.model.enums.TypeQuestion;

import javax.swing.text.html.Option;
import java.lang.reflect.Type;
import java.sql.SQLOutput;
import java.util.Scanner;
import java.util.List;

public class EditResponseDriver {
    private final ResponseController responseController;
    private final Scanner sc = new Scanner(System.in);

    public EditResponseDriver(ResponseController responseControler) {
        this.responseController = responseControler;
    }

    public boolean confirmExit() {
        boolean sentResponse = false, exit = false;
        do {
            System.out.println("¿Estás seguro de que quieres enviar tu respuesta? No podrás volver a modificarla.");
            System.out.println("1. ESTOY SEGURO DE QUE QUIERO ENVIAR MI RESPUESTA");
            System.out.println("2. QUIERO SEGUIR EDITANDO MI RESPUESTA");
            System.out.print("Opción: ");
            switch (sc.nextInt()) {
                case 1 -> {
                    exit = true;
                    sentResponse = true;
                }
                case 2 -> {
                    exit = true;
                    sentResponse = false;
                }
                default -> System.out.println("Opción inválida. Selecciona una opción del menú.");
            }

        } while (!exit);

        return sentResponse;
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
        try {
            List<Question> questions = responseController.getQuestions(surveyid);
            for (Question question : questions) {
                System.out.println("Índice: " + question.getQuestionIndex() + ", pregunta: " + question.getQuestionText());
                if (question.getTypeQuestion() == TypeQuestion.MULTIPLE_CHOICE) {
                    MultipleChoiceQuestion mcq = (MultipleChoiceQuestion) question;
                    List<OptionQuestion> options = mcq.getOptions();
                    for (int i = 0; i < options.size(); i++) {
                        System.out.println("---" + i + ". " + options.get(i).getOptionText());
                    }
                }
            }
        } catch (ResponseException | SurveyException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void showAnswers(String surveyId, String responseId) {
        try {
            List<Answer> answers = responseController.getAnswers(surveyId, responseId);
            for (Answer answer : answers) {
                System.out.print("Índice de la pregunta: " + answer.getQUESTION_INDEX());
                if (answer.getAnswerType() == TypeQuestion.TEXTUAL) {
                    TextualAnswer ta = (TextualAnswer) answer;
                    System.out.println(" , respuesta: " + ta.getAnswerText());
                }
                if (answer.getAnswerType() == TypeQuestion.MULTIPLE_CHOICE) {
                    MultipleChoiceAnswer mca = (MultipleChoiceAnswer) answer;
                    System.out.println(" , respuestas seleccionadas: " + mca.getSelectedOptions());
                }
            }
        } catch (ResponseException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void editResponseMenu(String surveyId, String responseid) {
        boolean exit = false;
        do {
            switch (selectEditResponseMenu()) {
                case 1 -> showQuestions(surveyId);
                case 2 -> System.out.println("responder pregunta");
                case 3 -> showAnswers(surveyId, responseid);
                case 4 -> exit = confirmExit();
                default -> System.out.println("Opción no válida. Selecciona una opción del menú");
            }
        } while (!exit);
        System.out.println("--- enviando respuesta ---");
    }
}
