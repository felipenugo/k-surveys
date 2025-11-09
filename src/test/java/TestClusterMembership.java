import static org.junit.Assert.*;
import org.junit.Test;
import domain.model.ClusterMembership;

/**
 * Test para ClusterMembership.
 */
public class TestClusterMembership {
    
    @Test
    public void testCreacion() {
        ClusterMembership membership = new ClusterMembership("RS1", "C1", 0.5);
        
        assertEquals("RS1", membership.getResponseSetId());
        assertEquals("C1", membership.getClusterId());
        assertEquals(0.5, membership.getDistance(), 1e-10);
        assertNotNull(membership.getAssignmentDate());
    }
    
    @Test
    public void testDistancia() {
        ClusterMembership membership = new ClusterMembership("RS1", "C1", 0.5);
        
        membership.setDistance(0.8);
        assertEquals(0.8, membership.getDistance(), 1e-10);
    }
}
