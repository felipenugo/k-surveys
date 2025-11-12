package presentation.drivers;

import domain.controller.ResponseController;
import domain.exception.*;
import domain.model.*;
import domain.model.enums.TypeQuestion;

import java.util.Arrays;
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
            System.out.println("¿ESTÁS SEGURO DE QUE QUIERES ENVIAR TU RESPUESTA? NO PODRÁS VOLVER A MODIFICARLA.");
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

    public boolean selectAnswerErrorMenu() {
        boolean exit = false;
        boolean exitAnswer = false;
        do {
            System.out.println("1. Intentar responder de nuevo.");
            System.out.println("2. Responder otra pregunta.");
            System.out.print("Opción: ");
            switch (sc.nextInt()) {
                case 1 -> {
                    exit = true;
                    exitAnswer = false;
                }
                case 2 -> {
                    exit = true;
                    exitAnswer = true;
                }
                default -> System.out.println("Opción incorrecta, selecciona una opción del menú.");
            }
        } while (exit = false);
        return exitAnswer;
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

    public void showQuestion(Question question) {
        System.out.println("Índice: " + question.getQuestionIndex() + ", pregunta: " + question.getQuestionText());
        if (question.getTypeQuestion() == TypeQuestion.MULTIPLE_CHOICE) {
            MultipleChoiceQuestion mcq = (MultipleChoiceQuestion) question;
            List<OptionQuestion> options = mcq.getOptions();
            for (int i = 0; i < options.size(); i++) {
                System.out.println("--Opción " + i + ". " + options.get(i).getOptionText());
            }
        }
    }

    public void showQuestions(String surveyid) {
        try {
            List<Question> questions = responseController.getQuestions(surveyid);
            for (Question question : questions)
                showQuestion(question);
        } catch (ResponseException | SurveyException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void showAnswers(String surveyId, String responseId) {
        try {
            List<Answer> answers = responseController.getAnswers(surveyId, responseId);
            for (Answer answer : answers) {
                System.out.print("Índice de la pregunta: " + answer.getQUESTION_INDEX());
                if (answer.getTypeAnswer().equals(TypeQuestion.TEXTUAL)) {
                    TextualAnswer ta = (TextualAnswer) answer;
                    System.out.println(" , respuesta: " + ta.getAnswerText());
                }
                if (answer.getTypeAnswer().equals(TypeQuestion.MULTIPLE_CHOICE)) {
                    MultipleChoiceAnswer mca = (MultipleChoiceAnswer) answer;
                    System.out.println(" , respuestas seleccionadas: " + Arrays.toString(mca.getSelectedOptions()));
                }
            }
        } catch (ResponseException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void answerQuestion(String surveyId, String responseId, int questionIndex) {
        Question question = responseController.startAnswer(surveyId, responseId, questionIndex);
        TypeQuestion answerType = question.getTypeQuestion();
        if (answerType.equals(TypeQuestion.TEXTUAL)) {
            System.out.print("Introduce el texto de tu respuesta: ");
            String textAnswer = sc.nextLine();
            responseController.updateAnswer(surveyId, responseId, questionIndex, textAnswer, answerType);

        } else if (answerType.equals(TypeQuestion.MULTIPLE_CHOICE)) {
            System.out.println("Introduce el número de como máximo " + ((MultipleChoiceQuestion) question).getMaxSelections() + " opciones  separadas por espacios:");
            String multipleChoiceAnswer = sc.nextLine();
            responseController.updateAnswer(surveyId, responseId, questionIndex, multipleChoiceAnswer, answerType);
        } else {
            // NumericalAnswer
            System.out.println("Introduce el número de tu respuesta");
            Double numericalAnswer = sc.nextDouble();
            responseController.updateAnswer(surveyId, responseId, questionIndex, numericalAnswer);

        }
    }

    public void selectAnswer(String surveyId, String responseId) {
        boolean exit = false;
        do {
            try {

                System.out.print("Introduce el índice de la pregunta que quieras responder:");
                int questionIndex = sc.nextInt();
                answerQuestion(surveyId, responseId, questionIndex);
                exit = true;
            } catch (ResponseException | SurveyException e) {
                System.out.println("Error: " + e.getMessage());
                exit = selectAnswerErrorMenu();
            }
        } while (!exit);
    }

    public void editResponseMenu(String surveyId, String responseId) {
        boolean exit = false;
        do {
            switch (selectEditResponseMenu()) {
                case 1 -> showQuestions(surveyId);
                case 2 -> selectAnswer(surveyId, responseId);
                case 3 -> showAnswers(surveyId, responseId);
                case 4 -> exit = confirmExit();
                default -> System.out.println("Opción no válida. Selecciona una opción del menú");
            }
        } while (!exit);
        System.out.println("--- enviando respuesta ---");
    }
}
