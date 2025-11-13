package domain.clustering;

import domain.model.Survey;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class ClusteringAnalysisTest {

    private Survey survey;
    private ClusteringAlgorithm algorithm;

    @Before
    public void setUp() {
        survey = new Survey("Test Survey", "A survey for testing purposes", "tester");
        algorithm = new KMeans(100, 1e-4);
    }

    @Test
    public void testClusteringAnalysisCreation() {
        ClusteringAnalysis analysis = new ClusteringAnalysis(survey, 3, algorithm);
        assertNotNull(analysis.getId());
        assertEquals(survey.getSURVEY_ID(), analysis.getSurveyId());
        assertEquals((Integer) 3, analysis.getK());
        assertEquals("K-Means", algorithm.getName());
        assertNotNull(analysis.getAnalysisDate());
    }
}
