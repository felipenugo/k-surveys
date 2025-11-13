package domain.controller;

import domain.clustering.ClusteringAnalysis;
import domain.model.Survey;
import org.junit.Before;
import org.junit.Test;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.*;

public class AnalysisControllerTest {

    private AnalysisController analysisController;
    private Survey survey;

    @Before
    public void setUp() {
        analysisController = new AnalysisController();
        survey = new Survey("Test Survey", "A survey for testing purposes", "tester");
    }

    @Test
    public void testCreateAnalysis() {
        Map<String, Object> config = new HashMap<>();
        ClusteringAnalysis analysis = analysisController.createAnalysis(survey, 3, "KMeans", config);
        assertNotNull(analysis);
        assertEquals(survey.getSURVEY_ID(), analysis.getSurveyId());
        assertEquals((Integer) 3, analysis.getK());
        assertNotNull(analysisController.getAnalysis(analysis.getId()));
    }

    @Test
    public void testDeleteAnalysis() {
        Map<String, Object> config = new HashMap<>();
        ClusteringAnalysis analysis = analysisController.createAnalysis(survey, 3, "KMeans", config);
        String analysisId = analysis.getId();
        assertTrue(analysisController.deleteAnalysis(analysisId));
        assertNull(analysisController.getAnalysis(analysisId));
    }

    @Test
    public void testGetAvailableAlgorithms() {
        assertFalse(analysisController.getAvailableAlgorithms().isEmpty());
        assertTrue(analysisController.getAvailableAlgorithms().contains("KMeans"));
    }
}
