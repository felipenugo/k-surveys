package clustering;

import domain.clustering.Centroid;
import domain.clustering.Cluster;
import domain.clustering.ClusterResults;
import domain.clustering.DistanceCalculator;
import domain.clustering.DistanceType;
import domain.clustering.KMeans;
import domain.model.Answer;
import domain.model.MultipleChoiceAnswer;
import domain.model.MultipleChoiceQuestion;
import domain.model.Question;
import domain.model.Response;
import domain.model.TextualAnswer;
import domain.model.enums.TypeQuestion;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class KMeansRefactorTest {

    private KMeans kmeans;
    private DistanceCalculator distanceCalc;
    private List<Question> questions;
    private List<Response> data;

    @Before
    public void setUp() {
        kmeans = new KMeans(10, 1e-5);
        distanceCalc = new DistanceCalculator(DistanceType.EUCLIDEAN);

        // Create questions
        questions = new ArrayList<>();
        MultipleChoiceQuestion q1 = new MultipleChoiceQuestion(0, "survey1");
        q1.setQuestionText("Choice Question");
        q1.addOption(new domain.model.OptionQuestion(0, "survey1"));
        q1.addOption(new domain.model.OptionQuestion(1, "survey1"));
        questions.add(q1);

        Question q2 = new Question(1, "survey1");
        q2.setTypeQuestion(TypeQuestion.TEXTUAL);
        q2.setQuestionText("Text Question");
        questions.add(q2);

        // Create responses
        data = new ArrayList<>();
        data.add(createResponse("user1", new boolean[]{true, false}, "apple"));
        data.add(createResponse("user2", new boolean[]{true, false}, "apple"));
        data.add(createResponse("user3", new boolean[]{false, true}, "banana"));
        data.add(createResponse("user4", new boolean[]{false, true}, "banana"));
    }

    private Response createResponse(String username, boolean[] choice, String text) {
        Response r = new Response("survey1", username, questions.size());
        
        MultipleChoiceAnswer a1 = new MultipleChoiceAnswer(0, r.getRESPONSE_ID(), 2);
        if (choice[0]) a1.setOption(0, true);
        if (choice[1]) a1.setOption(1, true);
        r.updateAnswer(0, a1);

        TextualAnswer a2 = new TextualAnswer(1, r.getRESPONSE_ID(), text);
        r.updateAnswer(1, a2);

        return r;
    }

    @Test
    public void testKMeansExecute() {
        // Execute clustering
        ClusterResults results = kmeans.execute(data, questions, 2, distanceCalc);

        // Basic assertions
        Assert.assertNotNull(results);
        Assert.assertEquals(2, results.getClusters().size());
        Assert.assertTrue(results.hasConverged());

        // Check total number of responses
        int totalResponses = 0;
        for (Cluster c : results.getClusters()) {
            totalResponses += c.getSize();
        }
        Assert.assertEquals(data.size(), totalResponses);
    }
}
