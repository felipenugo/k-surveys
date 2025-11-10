package domain.controller;

import domain.service.QuestionService;

public class QuestionController {
    private final QuestionService questionService;
    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }
}
