import static org.junit.Assert.*;
import org.junit.Test;
import domain.clustering.*;
import domain.model.*;
import java.util.List;

/**
 * Test para ClusterResults.
 */
public class TestClusterResults {
    
    @Test
    public void testCreacion() {
        ClusterResults results = new ClusterResults(3, 10, 5);
        
        assertEquals(3, results.getK());
        assertEquals(10, results.getNumberOfResponses());
        assertEquals(5, results.getNumberOfFeatures());
        assertEquals(3, results.getClusters().size());
    }
    
    @Test
    public void testAsignaciones() {
        ClusterResults results = new ClusterResults(2, 4, 2);
        
        results.setClusterAssignment(0, 0);
        results.setClusterAssignment(1, 0);
        results.setClusterAssignment(2, 1);
        results.setClusterAssignment(3, 1);
        
        results.setDistance(0, 0.5);
        results.setDistance(1, 0.3);
        results.setDistance(2, 0.4);
        results.setDistance(3, 0.6);
        
        assertEquals(0, results.getClusterForResponse(0));
        assertEquals(1, results.getClusterForResponse(2));
        assertEquals(0.3, results.getDistanceForResponse(1), 1e-10);
    }
    
    @Test
    public void testClusters() {
        ClusterResults results = new ClusterResults(2, 4, 2);
        
        results.setClusterAssignment(0, 0);
        results.setClusterAssignment(1, 0);
        results.setClusterAssignment(2, 1);
        results.setClusterAssignment(3, 1);
        
        List<Cluster> clusters = results.getClusters();
        assertEquals(2, clusters.size());
        
        Cluster cluster0 = clusters.get(0);
        assertEquals(2, cluster0.getSize().intValue());
        assertTrue(cluster0.contains("0"));
        assertTrue(cluster0.contains("1"));
        
        Cluster cluster1 = clusters.get(1);
        assertEquals(2, cluster1.getSize().intValue());
        assertTrue(cluster1.contains("2"));
        assertTrue(cluster1.contains("3"));
    }
}
