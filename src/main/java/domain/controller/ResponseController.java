package domain.controller;

import domain.service.ResponseService;
import domain.model.*;

import java.util.List;

public class ResponseController {
    private final ResponseService responseService;

    public ResponseController(ResponseService responseService) {
        this.responseService = responseService;
    }

    public void startResponse(String surveyId, String responseId) {
        responseService.startResponse(surveyId, responseId);
    }

    public List<Question> getQuestions(String surveyId) {
        return responseService.getQuestions(surveyId);
    }
}
