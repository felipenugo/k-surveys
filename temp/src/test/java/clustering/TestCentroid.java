package clustering;

import domain.clustering.Centroid;
import org.junit.Assert;
import org.junit.Test;

public class TestCentroid {

    @Test
    public void testCentroidConstructor() {
        Centroid centroid = new Centroid(3);
        Assert.assertEquals(3, centroid.getNumDimensions());
        Assert.assertEquals(3, centroid.getComponents().size());
        Assert.assertNull(centroid.getComponent(0));
        Assert.assertNull(centroid.getComponent(1));
        Assert.assertNull(centroid.getComponent(2));
    }

    @Test
    public void testSetAndGetComponent() {
        Centroid centroid = new Centroid(2);
        centroid.setComponent(0, 1.0);
        centroid.setComponent(1, "test");
        Assert.assertEquals(1.0, centroid.getComponent(0));
        Assert.assertEquals("test", centroid.getComponent(1));
    }

    @Test
    public void testClone() {
        Centroid centroid = new Centroid(3);
        centroid.setComponent(0, 1.0);
        centroid.setComponent(1, new double[]{1.0, 2.0});
        centroid.setComponent(2, "test");

        Centroid cloned = centroid.clone();

        Assert.assertNotSame(centroid, cloned);
        Assert.assertEquals(centroid.getNumDimensions(), cloned.getNumDimensions());
        Assert.assertEquals(centroid.getComponent(0), cloned.getComponent(0));
        Assert.assertArrayEquals((double[]) centroid.getComponent(1), (double[]) cloned.getComponent(1), 0.0);
        Assert.assertEquals(centroid.getComponent(2), cloned.getComponent(2));

        // Modify the original and check that the clone is not affected
        centroid.setComponent(0, 2.0);
        ((double[]) centroid.getComponent(1))[0] = 3.0;

        Assert.assertEquals(1.0, cloned.getComponent(0));
        Assert.assertEquals(1.0, ((double[]) cloned.getComponent(1))[0], 0.0);
    }
}
