package domain.model;

import java.util.List;
import java.util.ArrayList;

/**
 * Representa un cluster de ResponseSets.
 */
public class Cluster {
    
    private String id;
    private String label;
    private Centroid centroid;
    private List<ClusterMembership> members;
    
    public Cluster(String id) {
        this.id = id;
        this.label = "Cluster " + id;
        this.members = new ArrayList<>();
        this.centroid = null;
    }
    
    public String getId() {
        return id;
    }
    
    public String getLabel() {
        return label;
    }
    
    public void setLabel(String label) {
        this.label = label;
    }
    
    public Centroid getCentroid() {
        return centroid;
    }
    
    public void setCentroid(Centroid centroid) {
        this.centroid = centroid;
    }
    
    /**
     * Añade un miembro al cluster.
     * TODO: Cambiar a ResponseSet cuando exista.
     */
    public void addMember(String responseSetId, Double distance) {
        ClusterMembership membership = new ClusterMembership(responseSetId, this.id, distance);
        members.add(membership);
    }
    
    public boolean removeMember(String responseSetId) {
        return members.removeIf(m -> m.getResponseSetId().equals(responseSetId));
    }
    
    public List<ClusterMembership> getMembers() {
        return new ArrayList<>(members);
    }
    
    public Integer getSize() {
        return members.size();
    }
    
    public boolean contains(String responseSetId) {
        return members.stream()
                .anyMatch(m -> m.getResponseSetId().equals(responseSetId));
    }
    
    public Double getAverageDistance() {
        if (members.isEmpty()) {
            return 0.0;
        }
        double sum = members.stream()
                .mapToDouble(ClusterMembership::getDistance)
                .sum();
        return sum / members.size();
    }
    
    /**
     * Actualiza el centroide del cluster.
     * TODO: Implementar cuando exista Question.
     */
    public void updateCentroid(List<Object> questions) {
        throw new UnsupportedOperationException("Question class not yet implemented");
    }
}
