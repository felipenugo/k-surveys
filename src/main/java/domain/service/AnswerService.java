package domain.service;

import data.AnswerRepository;
import domain.controller.UserController;
import domain.model.User;

public class AnswerService {
    private final AnswerRepository answerRepository;
    private final UserController userController;
    public AnswerService(AnswerRepository answerRepository, UserController userController) {
        this.answerRepository = answerRepository;
        this.userController = userController;
    }
}
