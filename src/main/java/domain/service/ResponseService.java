package domain.service;

import data.ResponseRepository;
import domain.controller.UserController;

public class ResponseService {
    private final ResponseRepository responseRepository;
    private final UserController userController;
    public ResponseService(ResponseRepository responseRepository, UserController userController) {
        this.responseRepository = responseRepository;
        this.userController = userController;
    }
}
