package clustering;

import domain.clustering.Cluster;
import domain.clustering.Centroid;
import domain.model.Response;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class TestCluster {

    @Test
    public void testClusterConstructor() {
        Cluster cluster = new Cluster("cluster1");
        Assert.assertEquals("cluster1", cluster.getId());
        Assert.assertEquals("cluster1", cluster.getLabel());
        Assert.assertNull(cluster.getCentroid());
        Assert.assertTrue(cluster.getMembers().isEmpty());
    }

    @Test
    public void testSettersAndGetters() {
        Cluster cluster = new Cluster("cluster1");
        cluster.setLabel("Test Cluster");
        Centroid centroid = new Centroid(2);
        cluster.setCentroid(centroid);

        Assert.assertEquals("Test Cluster", cluster.getLabel());
        Assert.assertSame(centroid, cluster.getCentroid());
    }

    @Test
    public void testAddAndRemoveMember() {
        Cluster cluster = new Cluster("cluster1");
        Response response = new Response("response1", "survey1", "user1", new ArrayList<>());
        cluster.addMember(response, 0.5);

        Assert.assertEquals(1, cluster.getSize());
        Assert.assertTrue(cluster.contains(response.getRESPONSE_ID()));

        cluster.removeMember(response.getRESPONSE_ID());
        Assert.assertEquals(0, cluster.getSize());
        Assert.assertFalse(cluster.contains(response.getRESPONSE_ID()));
    }

    @Test
    public void testGetAverageDistance() {
        Cluster cluster = new Cluster("cluster1");
        Response r1 = new Response("s1", "u1", new ArrayList<>());
        Response r2 = new Response("s1", "u2", new ArrayList<>());
        cluster.addMember(r1, 0.5);
        cluster.addMember(r2, 1.5);

        Assert.assertEquals(1.0, cluster.getAverageDistance(), 0.0);
    }
}
