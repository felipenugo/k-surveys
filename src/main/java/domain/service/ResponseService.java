package domain.service;

import com.sun.source.tree.Tree;
import domain.model.*;
import domain.controller.*;
import data.*;
import domain.exception.*;
import domain.model.enums.TypeQuestion;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class ResponseService {
    private final ResponseRepository responseRepository;
    private final UserController userController;
    public final SurveyService surveyService;

    public ResponseService(ResponseRepository responseRepository, UserController userController, SurveyService surveyService) {
        this.responseRepository = responseRepository;
        this.userController = userController;
        this.surveyService = surveyService;
    }

    private boolean isInputBlank(String text) {
        return text == null || text.trim().isEmpty();
    }

    private boolean isInputBlank(Double num) {
        return num.toString().trim().isEmpty();
    }

    public boolean[] getOptionsSelected(String input, int maxSelections, int numOptions) {
        if (!input.matches("[0-9\\s]+"))
            throw new ResponseException("Las opciones tienen que ser las opciones marcadas separadas por espacios.");
        String[] optionsStr = input.trim().split("\\s+");
        TreeSet<Integer> selectedOptions = new TreeSet<Integer>();
        for (String s : optionsStr) {
            if (!s.trim().isEmpty())
                selectedOptions.add(Integer.parseInt(s));
        }
        if (selectedOptions.size() > maxSelections)
            throw new ResponseException("El máximo número de opciones son " + maxSelections);
        if (selectedOptions.last() >= numOptions)
            throw new ResponseException("Has seleccionado una opción que no existe.");
        boolean[] result = new boolean[numOptions];
        for (Integer option : selectedOptions)
            result[option] = true;
        return result;
    }

    public void checkUserLoggedin() {
        if (!userController.isLoggedIn())
            throw new SurveyException("Debes iniciar sesión para poder responder encuestas.");
    }

    // verifies that the surveyid exists also
    public void checkResponseExists(String surveyId, String responseId) {
        if (!responseRepository.existsResponse(surveyId, responseId))
            throw new ResponseException("La respuesta con id " + responseId + " no existe.");
    }

    public String getValidResponseId() {
        String lastResponseId = responseRepository.getLastResponseId();
        Integer responseId = Integer.parseInt(lastResponseId) + 1;
        return responseId.toString();
    }

    public String startResponse(String surveyId) {
        checkUserLoggedin();
        List<Question> questions = surveyService.getQuestions(surveyId); // this method verify that the survey exists
        String responseId = getValidResponseId();
        String responderUsername = userController.getUsernameLoggedIn();
        Response response = new Response(responseId, surveyId, responderUsername, questions);
        if (!responseRepository.existsSurveyEntry(surveyId))
            responseRepository.addSurveyEntry(surveyId);
        responseRepository.addResponse(surveyId, response);
        userController.addResponseId(responderUsername, surveyId, responseId); // this keeps the coherence with the double index
        return responseId;
    }

    public List<Question> getQuestions(String surveyId) {
        return surveyService.getQuestions(surveyId);
    }

    public Question getQuestion(String surveyId, int questionIndex) {
        return surveyService.getQuestion(surveyId, questionIndex);
    }

    public boolean existsQuestionAnswered(List<Answer> answers) {
        for (Answer a : answers)
            if (a.getIsAnswered())
                return true;
        return false;

    }


    public List<Answer> getAnswers(String surveyId, String responseId) {
        checkResponseExists(surveyId, responseId);
        List<Answer> answers = responseRepository.getAllAnswers(surveyId, responseId);
        if (!existsQuestionAnswered(answers))
            throw new ResponseException("Todavía no has respondido ninguna pregunta.");
        return answers;
    }

    // verifies that the question exists
    public Question startAnswer(String surveyId, String responseId, int questionIndex) {
        checkResponseExists(surveyId, responseId);
        Question question = surveyService.getQuestion(surveyId, questionIndex); // this verifies that the question exists in the survey
        Answer answer; //
        if (question.getTypeQuestion() == TypeQuestion.MULTIPLE_CHOICE) {
            answer = new MultipleChoiceAnswer(questionIndex, responseId, ((MultipleChoiceQuestion) question).getOptionsSize());
        } else if (question.getTypeQuestion() == TypeQuestion.TEXTUAL)
            answer = new TextualAnswer(questionIndex, responseId);
        else answer = new NumericalAnswer(questionIndex, responseId);
        responseRepository.updateAnswer(surveyId, responseId, questionIndex, answer); // the answerIndex is the same as the questionIndex
        return question;
    }

    public void updateAnswer(String surveyId, String responseId, int questionIndex, String strAnswer, TypeQuestion answerType) {
        if (isInputBlank(strAnswer))
            throw new ResponseException("La respuesta no puede ser vacía.");
        checkResponseExists(surveyId, responseId);
        surveyService.checkQuestionExists(surveyId, questionIndex);
        if (answerType.equals(TypeQuestion.TEXTUAL)) {
            TextualAnswer answer = new TextualAnswer(questionIndex, responseId);
            answer.setAnswerText(strAnswer);
            responseRepository.updateAnswer(surveyId, responseId, questionIndex, answer);
        } else {
            // Multiple choice answer
            MultipleChoiceQuestion mcQuestion = (MultipleChoiceQuestion) surveyService.getQuestion(surveyId, questionIndex);
            boolean[] optionsSelected = getOptionsSelected(strAnswer, mcQuestion.getMaxSelections(), mcQuestion.getOptionsSize());
            MultipleChoiceAnswer answer = new MultipleChoiceAnswer(questionIndex, responseId, mcQuestion.getMaxSelections());
            answer.setOptions(optionsSelected);
            responseRepository.updateAnswer(surveyId, responseId, questionIndex, answer);
        }
    }

    public void updateAnswer(String surveyId, String responseId, int questionIndex, Double numAnswer) {
        if (isInputBlank(numAnswer))
            throw new ResponseException("La respuesta no puede ser vacía.");
        checkResponseExists(surveyId, responseId);
        surveyService.checkQuestionExists(surveyId, questionIndex); // verifies that the questionIndex is valid
        NumericalAnswer answer = new NumericalAnswer(questionIndex, responseId);
        answer.setAnswerNum(numAnswer);
        responseRepository.updateAnswer(surveyId, responseId, questionIndex, answer);
    }
}
