import static org.junit.Assert.*;
import org.junit.Test;
import domain.model.Centroid;
import java.util.List;
import java.util.ArrayList;

/**
 * Test para Centroid.
 */
public class TestCentroid {
    
    @Test
    public void testCreacion() {
        List<String> questionIds = new ArrayList<>();
        questionIds.add("Q1");
        questionIds.add("Q2");
        questionIds.add("Q3");
        
        Centroid centroid = new Centroid(questionIds);
        
        assertEquals(3, centroid.getQuestionIds().size());
        assertEquals(3, centroid.getComponents().size());
    }
    
    @Test
    public void testComponentes() {
        List<String> questionIds = new ArrayList<>();
        questionIds.add("Q1");
        questionIds.add("Q2");
        
        Centroid centroid = new Centroid(questionIds);
        centroid.setComponent(0, 1.5);
        centroid.setComponent(1, 2.5);
        
        List<Object> components = centroid.getComponents();
        assertEquals(1.5, components.get(0));
        assertEquals(2.5, components.get(1));
    }
    
    @Test
    public void testClone() {
        List<String> questionIds = new ArrayList<>();
        questionIds.add("Q1");
        questionIds.add("Q2");
        
        Centroid original = new Centroid(questionIds);
        original.setComponent(0, 3.0);
        original.setComponent(1, 4.0);
        
        Centroid cloned = original.clone();
        
        assertEquals(2, cloned.getQuestionIds().size());
        assertEquals(3.0, cloned.getComponents().get(0));
        assertEquals(4.0, cloned.getComponents().get(1));
        
        cloned.setComponent(0, 5.0);
        assertNotEquals(5.0, original.getComponents().get(0));
    }
}
