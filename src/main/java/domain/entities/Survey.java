package domain.entities;
import domain.entities.Question;
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
    private LocalDateTime creationDate;
     */

    // Constructor without id for new surveys
    public Survey (String title, String description, String CREATOR_USERNAME) {
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

    // Getters y Setters
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
    public int getSize()
    {
        return questions.size();
    }
}
