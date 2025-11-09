import static org.junit.Assert.*;
import org.junit.Test;
import domain.clustering.*;

/**
 * Test para KMeans.
 */
public class TestKMeans {
    
    @Test
    public void testBasico() {
        Object[][] data = {
            {1.0, 1.0}, {1.5, 1.2}, {1.2, 1.5},
            {8.0, 8.0}, {8.5, 8.2}, {8.2, 8.5}
        };
        
        KMeans kmeans = new KMeans(100, 1e-4);
        DistanceCalculator dist = new DistanceCalculator(DistanceType.EUCLIDEAN);
        ClusterResults results = kmeans.execute(data, 2, dist);
        
        assertEquals(2, results.getK());
        assertEquals(6, results.getNumberOfResponses());
        assertTrue(results.hasConverged());
        
        Integer[] assignments = results.getClusterAssignments();
        boolean cluster1OK = assignments[0].equals(assignments[1]) && assignments[1].equals(assignments[2]);
        boolean cluster2OK = assignments[3].equals(assignments[4]) && assignments[4].equals(assignments[5]);
        boolean separated = !assignments[0].equals(assignments[3]);
        
        assertTrue("Clusters mal formados", cluster1OK && cluster2OK && separated);
    }
    
    @Test
    public void testConvergencia() {
        Object[][] data = {{1.0, 1.0}, {1.1, 1.0}, {9.0, 9.0}, {9.1, 9.0}};
        
        KMeans kmeans = new KMeans(100, 1e-4);
        DistanceCalculator dist = new DistanceCalculator(DistanceType.EUCLIDEAN);
        ClusterResults results = kmeans.execute(data, 2, dist);
        
        assertTrue("Demasiadas iteraciones", results.getIterations() <= 10);
    }
    
    @Test
    public void testDistintoK() {
        Object[][] data = {
            {1.0, 1.0}, {1.2, 1.1},
            {5.0, 5.0}, {5.2, 5.1},
            {9.0, 9.0}, {9.2, 9.1}
        };
        
        KMeans kmeans = new KMeans(100, 1e-4);
        DistanceCalculator dist = new DistanceCalculator(DistanceType.EUCLIDEAN);
        ClusterResults results = kmeans.execute(data, 3, dist);
        
        assertEquals(3, results.getK());
        assertEquals(6, results.getNumberOfResponses());
    }
}
