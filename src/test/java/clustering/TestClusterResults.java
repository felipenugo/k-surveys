package clustering;

import domain.clustering.Cluster;
import domain.clustering.ClusterResults;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TestClusterResults {

    @Test
    public void testClusterResultsConstructor() {
        List<Cluster> clusters = new ArrayList<>();
        clusters.add(new Cluster("c1"));
        clusters.add(new Cluster("c2"));
        ClusterResults results = new ClusterResults(clusters, 10, true);

        Assert.assertEquals(2, results.getNumberOfClusters());
        Assert.assertEquals(10, results.getIterations());
        Assert.assertTrue(results.hasConverged());
    }

    @Test
    public void testGetCluster() {
        List<Cluster> clusters = new ArrayList<>();
        Cluster c1 = new Cluster("c1");
        clusters.add(c1);
        ClusterResults results = new ClusterResults(clusters, 10, true);

        Assert.assertSame(c1, results.getCluster(0));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetClusterOutOfBounds() {
        ClusterResults results = new ClusterResults(new ArrayList<>(), 10, true);
        results.getCluster(0);
    }
}
