package clustering;

import domain.clustering.Cluster;
import domain.clustering.ClusterMembership;
import domain.model.Response;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class TestClusterMembership {

    @Test
    public void testClusterMembershipConstructor() {
        Response response = new Response("r1", "survey1", "user1", new ArrayList<>());
        Cluster cluster = new Cluster("cluster1");
        ClusterMembership membership = new ClusterMembership(response, cluster, 0.5);

        Assert.assertEquals(response.getRESPONSE_ID(), membership.getResponseId());
        Assert.assertEquals(cluster.getId(), membership.getClusterId());
        Assert.assertEquals(0.5, membership.getDistance(), 0.0);
    }

    @Test
    public void testSetDistance() {
        Response response = new Response("r1", "survey1", "user1", new ArrayList<>());
        Cluster cluster = new Cluster("cluster1");
        ClusterMembership membership = new ClusterMembership(response, cluster, 0.5);
        membership.setDistance(1.0);
        Assert.assertEquals(1.0, membership.getDistance(), 0.0);
    }
}
