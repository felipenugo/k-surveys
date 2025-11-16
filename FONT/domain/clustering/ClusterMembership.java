package domain.clustering;

import domain.model.Response;
import java.util.Date;

public class ClusterMembership {
    private final String responseId;
    private final String clusterId;
    private double distance;
    private final Date assignmentDate;

    public ClusterMembership(Response response, Cluster cluster, double distance) {
        this.responseId = response.getRESPONSE_ID();
        this.clusterId = cluster.getId();
        this.distance = distance;
        this.assignmentDate = new Date();
    }

    public String getResponseId() { return responseId; }
    public String getClusterId() { return clusterId; }
    public double getDistance() { return distance; }
    public void setDistance(double distance) { this.distance = distance; }
}
