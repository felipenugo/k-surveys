package domain.model;

import java.util.Date;

/**
 * Representa la pertenencia de un ResponseSet a un Cluster.
 */
public class ClusterMembership {
    
    private String responseSetId;
    private String clusterId;
    private Double distance;
    private Date assignmentDate;
    
    /**
     * Constructor.
     * TODO: Cambiar parámetros a ResponseSet y Cluster cuando existan.
     */
    public ClusterMembership(String responseSetId, String clusterId, Double distance) {
        this.responseSetId = responseSetId;
        this.clusterId = clusterId;
        this.distance = distance;
        this.assignmentDate = new Date();
    }
    
    public String getResponseSetId() {
        return responseSetId;
    }
    
    public String getClusterId() {
        return clusterId;
    }
    
    public Double getDistance() {
        return distance;
    }
    
    public void setDistance(Double distance) {
        this.distance = distance;
    }
    
    public Date getAssignmentDate() {
        return assignmentDate;
    }
}
