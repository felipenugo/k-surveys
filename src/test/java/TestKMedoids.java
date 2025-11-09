import static org.junit.Assert.*;
import org.junit.Test;
import domain.clustering.*;

/**
 * Test para KMedoids.
 */
public class TestKMedoids {
    
    @Test
    public void testBasico() {
        Object[][] data = {
            {1.0, 1.0}, {1.5, 1.2}, {1.2, 1.5},
            {8.0, 8.0}, {8.5, 8.2}, {8.2, 8.5}
        };
        
        KMedoids km = new KMedoids(100, 1e-4);
        DistanceCalculator dist = new DistanceCalculator(DistanceType.EUCLIDEAN);
        ClusterResults results = km.execute(data, 2, dist);
        
        assertEquals(2, results.getK());
        assertTrue(results.hasConverged());
        assertEquals(6, results.getNumberOfResponses());
    }
    
    @Test
    public void testMedoidsSonPuntosReales() {
        Object[][] data = {
            {1.0, 1.0}, {1.5, 1.2},
            {8.0, 8.0}, {8.5, 8.2}
        };
        
        KMedoids km = new KMedoids(100, 1e-4);
        DistanceCalculator dist = new DistanceCalculator(DistanceType.EUCLIDEAN);
        ClusterResults results = km.execute(data, 2, dist);
        
        Object[] centroid0 = results.getCentroid(0);
        Object[] centroid1 = results.getCentroid(1);
        
        boolean centroid0EsPunto = esPuntoDelDataset(centroid0, data);
        boolean centroid1EsPunto = esPuntoDelDataset(centroid1, data);
        
        assertTrue("Los medoids deben ser puntos del dataset", centroid0EsPunto && centroid1EsPunto);
    }
    
    private boolean esPuntoDelDataset(Object[] centroid, Object[][] data) {
        for (Object[] punto : data) {
            if (punto.length == centroid.length) {
                boolean igual = true;
                for (int i = 0; i < punto.length; i++) {
                    if (!punto[i].equals(centroid[i])) {
                        igual = false;
                        break;
                    }
                }
                if (igual) return true;
            }
        }
        return false;
    }
}
