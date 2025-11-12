package domain.controller;

import domain.service.ResponseService;
import domain.model.*;
import domain.model.enums.*;

import java.util.List;

public class ResponseController {
    private final ResponseService responseService;

    public ResponseController(ResponseService responseService) {
        this.responseService = responseService;
    }

    public String startResponse(String surveyId) {
        return responseService.startResponse(surveyId);
    }

    public List<Question> getQuestions(String surveyId) {
        return responseService.getQuestions(surveyId);
    }

    public Question getQuestion(String surveyId, int questionIndex) {
        return responseService.getQuestion(surveyId, questionIndex);
    }

    public List<Answer> getAnswers(String surveyId, String responseId) {
        return responseService.getAnswers(surveyId, responseId);
    }

    public Question startAnswer(String surveyId, String responseId, int questionIndex) {
        return responseService.startAnswer(surveyId, responseId, questionIndex);
    }

    public void updateAnswer(String surveyId, String responseId, int questionIndex, String strAnswer, TypeQuestion typeAnswer) {
        responseService.updateAnswer(surveyId, responseId, questionIndex, strAnswer, typeAnswer);
    }

    public void updateAnswer(String surveyId, String responseId, int questionIndex, Double numericalAnswer) {
        responseService.updateAnswer(surveyId, responseId, questionIndex, numericalAnswer);
    }

    public void  incrementResponseCount(String surveyid)
    {
        responseService.incrementResponseCount(surveyid);
    }
}

