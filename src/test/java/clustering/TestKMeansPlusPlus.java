package clustering;

import domain.clustering.ClusterResults;
import domain.clustering.DistanceCalculator;
import domain.clustering.DistanceType;
import domain.clustering.KMeansPlusPlus;
import domain.model.MultipleChoiceAnswer;
import domain.model.MultipleChoiceQuestion;
import domain.model.Question;
import domain.model.Response;
import domain.model.TextualAnswer;
import domain.model.enums.TypeQuestion;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TestKMeansPlusPlus {

    private KMeansPlusPlus kmeansPlusPlus;
    private DistanceCalculator distanceCalc;
    private List<Question> questions;
    private List<Response> data;

    @Before
    public void setUp() {
        kmeansPlusPlus = new KMeansPlusPlus(10, 1e-5);
        distanceCalc = new DistanceCalculator(DistanceType.EUCLIDEAN);

        questions = new ArrayList<>();
        MultipleChoiceQuestion q1 = new MultipleChoiceQuestion(0, "s1");
        q1.addOption(new domain.model.OptionQuestion(0, "s1"));
        q1.addOption(new domain.model.OptionQuestion(1, "s1"));
        questions.add(q1);

        Question q2 = new Question(1, "s1");
        q2.setTypeQuestion(TypeQuestion.TEXTUAL);
        questions.add(q2);

        data = new ArrayList<>();
        data.add(createResponse("u1", new boolean[]{true, false}, "apple"));
        data.add(createResponse("u2", new boolean[]{true, false}, "apple"));
        data.add(createResponse("u3", new boolean[]{false, true}, "banana"));
        data.add(createResponse("u4", new boolean[]{false, true}, "banana"));
    }

    private Response createResponse(String username, boolean[] choice, String text) {
        Response r = new Response("s1", username, questions.size());
        MultipleChoiceAnswer a1 = new MultipleChoiceAnswer(0, r.getRESPONSE_ID(), 2);
        if (choice[0]) a1.setOption(0);
        if (choice[1]) a1.setOption(1);
        r.updateAnswer(0, a1);

        TextualAnswer a2 = new TextualAnswer(1, r.getRESPONSE_ID(), text);
        r.updateAnswer(1, a2);
        return r;
    }

    @Test
    public void testKMeansPlusPlusExecute() {
        ClusterResults results = kmeansPlusPlus.execute(data, questions, 2, distanceCalc);
        Assert.assertNotNull(results);
        Assert.assertEquals(2, results.getClusters().size());
        Assert.assertTrue(results.hasConverged());
        Assert.assertEquals(4, results.getNumberOfResponses());
    }
}
