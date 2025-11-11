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

    public Question getQuestion(String surveyId, int questionIndex)
    {
        return responseService.getQuestion(surveyId, questionIndex);
    }

    public List<Answer> getAnswers(String surveyId, String responseId)
    {
        return responseService.getAnswers(surveyId, responseId);
    }

    public Question startAnswer(String surveyId, String responseId, int questionIndex )
    {
        return responseService.startAnswer(surveyId, responseId, questionIndex);
    }
}

