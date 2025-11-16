package domain.clustering;

import domain.model.Response;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Cluster {
    private final String id;
    private String label;
    private Centroid centroid;
    private final List<ClusterMembership> members;

    public Cluster(String id) {
        this.id = id;
        this.label = id;
        this.members = new ArrayList<>();
    }

    public String getId() { return id; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public Centroid getCentroid() { return centroid; }
    public void setCentroid(Centroid centroid) { this.centroid = centroid; }
    public void addMember(Response response, double distance) { ClusterMembership membership = new ClusterMembership(response, this, distance); this.members.add(membership); }
    public boolean removeMember(String responseId) { return this.members.removeIf(member -> member.getResponseId().equals(responseId)); }
    public List<ClusterMembership> getMembers() { return Collections.unmodifiableList(members); }
    public int getSize() { return members.size(); }
    public boolean contains(String responseId) { for (ClusterMembership member : members) { if (member.getResponseId().equals(responseId)) return true; } return false; }
    public double getAverageDistance() { if (members.isEmpty()) return 0.0; double sum=0.0; for (ClusterMembership member : members) sum += member.getDistance(); return sum / members.size(); }
    @Override public boolean equals(Object o) { if (this == o) return true; if (o == null || getClass() != o.getClass()) return false; Cluster cluster = (Cluster) o; return id.equals(cluster.id); }
    @Override public int hashCode() { return Objects.hash(id); }
}
