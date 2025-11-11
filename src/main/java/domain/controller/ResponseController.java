package domain.controller;

import domain.service.ResponseService;
import domain.model.*;

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

    public List<Answer> getAnswers(String surveyid, String responseId)
    {
        return responseService.getAnswers(surveyid, responseId);
    }
}

