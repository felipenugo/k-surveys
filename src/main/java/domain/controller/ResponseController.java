package domain.controller;

import domain.service.ResponseService;

public class ResponseController {
    private final ResponseService responseService;
    public ResponseController(ResponseService responseService) {
        this.responseService = responseService;
    }
}
