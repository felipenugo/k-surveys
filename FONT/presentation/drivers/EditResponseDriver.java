package presentation.drivers;

import domain.controller.ResponseController;
import domain.exception.*;
import domain.model.*;
import domain.model.enums.TypeQuestion;
import presentation.driverMain.DriverMain;

import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.List;

public class EditResponseDriver {
    private final ResponseController responseController;
    private final Scanner sc = DriverMain.getScanner();

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
            int option = sc.nextInt();
            sc.nextLine(); // Consumir el salto de línea
            switch (option) {
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
            int option = sc.nextInt();
            sc.nextLine(); // Consumir el salto de línea
            switch (option) {
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
        } while (!exit);
        return exitAnswer;
    }

    private int selectEditResponseMenu() {
        System.out.println("--- EDITOR DE RESPUESTAS ---");
        System.out.println("1. VER PREGUNTAS.");
        System.out.println("2. RESPONDER PREGUNTA");
        System.out.println("3. VER MIS RESPUESTAS");
        System.out.println("4. ENVIAR RESPUESTA");
        System.out.print("Opción: ");
        int option = sc.nextInt();
        sc.nextLine(); // Consumir el salto de línea
        return option;
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
            for (Question question : questions) {
                System.out.println(); // salto de línea
                showQuestion(question);
            }
        } catch (ResponseException | SurveyException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void showAnswers(String surveyId, String responseId) {
        try {
            List<Answer> answers = responseController.getAnswers(surveyId, responseId
            );
            System.out.println();
            for (Answer answer : answers) {
                System.out.print("Índice de la pregunta: " + answer.getQUESTION_INDEX());
                if (answer.getTypeAnswer().equals(TypeQuestion.MULTIPLE_CHOICE)) {
                    MultipleChoiceAnswer mca = (MultipleChoiceAnswer) answer;
                    System.out.println(" , respuestas seleccionadas: " + Arrays.toString(mca.getSelectedOptions()));
                }
                else if(!answer.getIsAnswered()){
                    System.out.println(", no respondida");
                }
                else if (answer.getTypeAnswer().equals(TypeQuestion.TEXTUAL)) {
                    TextualAnswer ta = (TextualAnswer) answer;
                    System.out.println(" , respuesta: " + ta.getAnswerText());
                }
                else {
                    // Respuesta Numérica
                    NumericalAnswer na = (NumericalAnswer) answer;
                    System.out.println(", respuesta: " + na.getAnswerNum());
                }
            }
        } catch (ResponseException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void answerQuestion(String surveyId, String responseId, int questionIndex) {
        Question question = responseController.startAnswer(surveyId, responseId, questionIndex);
        TypeQuestion answerType = question.getTypeQuestion();
        System.out.println("-Pregunta a responder-");
        showQuestion(question);
        if (answerType.equals(TypeQuestion.TEXTUAL)) {
            System.out.print("Introduce el texto de tu respuesta: ");
            String textAnswer = sc.nextLine();
            responseController.updateAnswer(surveyId, responseId, questionIndex, textAnswer, answerType);

        } else if (answerType.equals(TypeQuestion.MULTIPLE_CHOICE)) {
            int minOptions = ((MultipleChoiceQuestion) question).getMinSelections();
            int maxOptions = ((MultipleChoiceQuestion) question).getMaxSelections();
            System.out.println("Introduce el número de tus opciones separadas por espacios, mínimo " + minOptions + " máximo " + maxOptions + ".");
            System.out.print("Opciones seleccionadas: ");
            String multipleChoiceAnswer = sc.nextLine();
            responseController.updateAnswer(surveyId, responseId, questionIndex, multipleChoiceAnswer, answerType);
        } else {
            // Respuesta Numérica
            System.out.print("Introduce el número de tu respuesta:");
            if(!sc.hasNextDouble())
            {
                throw new ResponseException("El formato tiene que ser númerico. Utiliza el punto (.) para decimales.");
            }
            Double numericalAnswer = sc.nextDouble();
            sc.nextLine(); // Consumir el salto de línea
            responseController.updateAnswer(surveyId, responseId, questionIndex, numericalAnswer);

        }
    }

    public void selectAnswer(String surveyId, String responseId) {
        boolean exit = false;
        do {
            try {

                System.out.print("Introduce el índice de la pregunta que quieras responder:");
                int questionIndex = sc.nextInt();
                sc.nextLine(); // Consumir el salto de línea
                answerQuestion(surveyId, responseId, questionIndex);
                System.out.println("---guardando tu respuesta a la pregunta " + questionIndex + "---");
                exit = true;
            } catch (ResponseException | SurveyException e) {
                System.out.println("Error: " + e.getMessage());
                exit = selectAnswerErrorMenu();
            }catch(InputMismatchException e)
            {
                System.out.println("El formato tiene que ser númerico. Utiliza el punto (.) para decimales.");;
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
        responseController.incrementResponseCount(surveyId);
        System.out.println("--- enviando respuesta ---");
    }
}
