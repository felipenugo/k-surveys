import static org.junit.Assert.*;
import org.junit.Test;
import domain.model.*;
import java.util.List;
import java.util.ArrayList;

/**
 * Test para Cluster.
 */
public class TestCluster {
    
    @Test
    public void testCreacion() {
        Cluster cluster = new Cluster("C1");
        
        assertEquals("C1", cluster.getId());
        assertEquals("Cluster C1", cluster.getLabel());
        assertEquals(0, cluster.getSize().intValue());
    }
    
    @Test
    public void testMiembros() {
        Cluster cluster = new Cluster("C1");
        
        cluster.addMember("RS1", 0.5);
        cluster.addMember("RS2", 0.3);
        cluster.addMember("RS3", 0.7);
        
        assertEquals(3, cluster.getSize().intValue());
        assertTrue(cluster.contains("RS1"));
        assertTrue(cluster.contains("RS2"));
        assertFalse(cluster.contains("RS99"));
        
        cluster.removeMember("RS2");
        assertEquals(2, cluster.getSize().intValue());
        assertFalse(cluster.contains("RS2"));
        
        double avgDist = cluster.getAverageDistance();
        assertEquals(0.6, avgDist, 1e-10);
    }
    
    @Test
    public void testCentroid() {
        Cluster cluster = new Cluster("C1");
        
        List<String> questionIds = new ArrayList<>();
        questionIds.add("Q1");
        questionIds.add("Q2");
        Centroid centroid = new Centroid(questionIds);
        centroid.setComponent(0, 1.5);
        centroid.setComponent(1, 2.5);
        
        cluster.setCentroid(centroid);
        
        Centroid retrieved = cluster.getCentroid();
        assertNotNull(retrieved);
        assertEquals(2, retrieved.getComponents().size());
    }
}
