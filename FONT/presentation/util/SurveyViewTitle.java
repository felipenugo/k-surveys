package presentation.util;

public enum SurveyViewTitle {
    HOME("RESPONDE A LAS ENCUESTAS"),
    DRAFTS("EDITA TUS ENCUESTAS"),
    MY_SURVEYS("ANALIZA TUS ENCUESTAS");

    private final String title;

    SurveyViewTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
