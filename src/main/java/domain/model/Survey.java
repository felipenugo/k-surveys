package domain.model;

import java.util.ArrayList;

public class Survey {
    private int id;
    private String title;
    private String description;
    private final String CREATOR_USERNAME; // identifier for survey creator
    private ArrayList<Question> questions;
    /*
    extra features to consider later (search filters, sorting, etc.):
    private double rating;
    private views;
    private responseCount;
    private LocalDateTime creationDate;
     */

    // Constructor without id for new surveys
    public Survey(String title, String description, String CREATOR_USERNAME) {
        this.title = title;
        this.description = description;
        this.CREATOR_USERNAME = CREATOR_USERNAME;
    }

    // Constructor with id checked from database
    public Survey(int id, String title, String description, String CREATOR_USERNAME) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.CREATOR_USERNAME = CREATOR_USERNAME;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCREATOR_USERNAME() {
        return CREATOR_USERNAME;
    }


    public int getSize() {
        return questions.size();
    }

    // Setters
    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void addQuestion(Question question) {
        questions.add(question);
    }

    public Question getQuestion(int index) {
        if(index <0 || index >= questions.size())
            System.out.println("Error índice inválido"); // error will be recieved by SurveyService
        return questions.get(index);
    }
}
