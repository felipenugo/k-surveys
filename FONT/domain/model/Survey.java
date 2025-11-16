package domain.model;

import domain.model.enums.SurveyStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Survey {
    private final String SURVEY_ID;
    private String title;
    private String description;
    private final String CREATOR_USERNAME;
    private final LocalDateTime CREATED_AT;
    private LocalDateTime PUBLISHED_AT;
    private SurveyStatus surveyStatus;
    private double avgRating;
    private int views;
    private List<Question> questions;

    public Survey(String SURVEY_ID, String title, String description, String CREATOR_USERNAME) {
        this.SURVEY_ID = SURVEY_ID;
        this.title = title;
        this.description = description;
        this.CREATOR_USERNAME = CREATOR_USERNAME;
        this.CREATED_AT = LocalDateTime.now();
        this.PUBLISHED_AT = null;
        this.surveyStatus = SurveyStatus.DRAFT;
        this.avgRating = 0.0;
        this.views = 0;
        this.questions = new ArrayList<>();
    }

    public Survey(String title, String description, String CREATOR_USERNAME) {
        this.SURVEY_ID = null;
        this.title = title;
        this.description = description;
        this.CREATOR_USERNAME = CREATOR_USERNAME;
        this.CREATED_AT = LocalDateTime.now();
        this.PUBLISHED_AT = null;
        this.surveyStatus = SurveyStatus.DRAFT;
        this.avgRating = 0.0;
        this.views = 0;
        this.questions = new ArrayList<>();
    }

    public String getSURVEY_ID() { return SURVEY_ID; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCREATOR_USERNAME() { return CREATOR_USERNAME; }
    public LocalDateTime getCREATED_AT() { return CREATED_AT; }
    public LocalDateTime getPUBLISHED_AT() { return PUBLISHED_AT; }
    public SurveyStatus getSurveyStatus() { return surveyStatus; }
    public double getAvgRating() { return avgRating; }
    public int getViews() { return views; }
    public List<Question> getQuestions() { return questions; }

    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setPUBLISHED_AT() { this.PUBLISHED_AT = LocalDateTime.now(); }
    public void setSurveyStatus(SurveyStatus surveyStatus) { this.surveyStatus = surveyStatus; }
    public void setAvgRating(double avgRating) { this.avgRating = avgRating; }
    public void setViews(int views) { this.views = views; }

    public int getSize() { return questions.size(); }
    public void addQuestion(Question question) { questions.add(question); }
    public void removeQuestion(int index) { questions.remove(index); }
    public void reorderQuestion(int oldIndex, int newIndex) { Question question = questions.get(oldIndex); questions.remove(oldIndex); questions.add(newIndex, question); }
    public void updateQuestion(int index, Question question) { questions.set(index, question); }
    public Question getQuestion(int index) { return questions.get(index); }
    public void clearQuestions() { questions.clear(); }
}
