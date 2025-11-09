package domain.service;

import data.QuestionRepository;
import domain.controller.UserController;

public class QuestionService {
    private final QuestionRepository questionRepository;
    private final UserController userController;

    public QuestionService(QuestionRepository questionRepository, UserController userController) {
        this.questionRepository = questionRepository;
        this.userController = userController;
    }
}
