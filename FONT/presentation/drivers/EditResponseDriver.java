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

    public boolean confirmExit() { return true; }
    public boolean selectAnswerErrorMenu() { return true; }
    private int selectEditResponseMenu() { return 1; }
    public void showQuestion(Question question) {}
    public void showQuestions(String surveyid) {}
    public void showAnswers(String surveyId, String responseId) {}
    public void answerQuestion(String surveyId, String responseId, int questionIndex) {}
    public void selectAnswer(String surveyId, String responseId) {}
    public void editResponseMenu(String surveyId, String responseId) {}
}
