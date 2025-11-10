package domain.controller;

import domain.service.ResponseService;

public class ResponseController {
    private final ResponseService responseService;
    public ResponseController(ResponseService responseService) {
        this.responseService = responseService;
    }

    public void startResponse(String surveyId, String responseId)
    {
        responseService.startResponse(surveyId, responseId);
    }
}
