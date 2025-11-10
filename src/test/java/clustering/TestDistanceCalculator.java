package clustering;

import domain.clustering.DistanceCalculator;
import domain.clustering.DistanceType;
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

public class TestDistanceCalculator {

    private DistanceCalculator euclideanCalculator;
    private DistanceCalculator manhattanCalculator;
    private List<Question> questions;

    @Before
    public void setUp() {
        euclideanCalculator = new DistanceCalculator(DistanceType.EUCLIDEAN);
        manhattanCalculator = new DistanceCalculator(DistanceType.MANHATTAN);

        questions = new ArrayList<>();
        MultipleChoiceQuestion q1 = new MultipleChoiceQuestion(0, "s1");
        q1.addOption(new domain.model.OptionQuestion(0, "s1"));
        q1.addOption(new domain.model.OptionQuestion(1, "s1"));
        questions.add(q1);

        Question q2 = new Question(1, "s1");
        q2.setTypeQuestion(TypeQuestion.TEXTUAL);
        questions.add(q2);
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
    public void testCalculateEuclidean() {
        Response r1 = createResponse("u1", new boolean[]{true, false}, "apple");
        Response r2 = createResponse("u2", new boolean[]{false, true}, "apply");
        double dist = euclideanCalculator.calculate(r1, r2, questions);
        Assert.assertTrue(dist > 0);
    }

    @Test
    public void testCalculateManhattan() {
        Response r1 = createResponse("u1", new boolean[]{true, false}, "apple");
        Response r2 = createResponse("u2", new boolean[]{false, true}, "apply");
        double dist = manhattanCalculator.calculate(r1, r2, questions);
        Assert.assertTrue(dist > 0);
    }
}
