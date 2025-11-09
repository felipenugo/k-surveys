import static org.junit.Assert.*;
import org.junit.Test;
import domain.clustering.*;

/**
 * Test para DistanceCalculator.
 */
public class TestDistanceCalculator {
    
    @Test
    public void testEuclidean() {
        DistanceCalculator calc = new DistanceCalculator(DistanceType.EUCLIDEAN);
        
        Object[] p1 = {0.0, 0.0};
        Object[] p2 = {3.0, 4.0};
        
        double dist = calc.calculateVectorDistance(p1, p2);
        assertEquals(5.0, dist, 1e-10);
    }
    
    @Test
    public void testManhattan() {
        DistanceCalculator calc = new DistanceCalculator(DistanceType.MANHATTAN);
        
        Object[] p1 = {0.0, 0.0};
        Object[] p2 = {3.0, 4.0};
        
        double dist = calc.calculateVectorDistance(p1, p2);
        assertEquals(7.0, dist, 1e-10);
    }
    
    @Test
    public void testCosine() {
        DistanceCalculator calc = new DistanceCalculator(DistanceType.COSINE);
        
        Object[] p1 = {1.0, 0.0};
        Object[] p2 = {1.0, 0.0};
        
        double dist = calc.calculateVectorDistance(p1, p2);
        assertTrue("Vectores iguales deberían tener distancia 0", dist < 1e-10);
    }
}
