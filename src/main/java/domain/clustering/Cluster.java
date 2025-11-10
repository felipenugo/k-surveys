package domain.clustering;

import domain.model.Response;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Representa un clúster de datos.
 * Contiene un centroide (el centro) y una lista de miembros
 * (Responses) que pertenecen a este clúster.
 */
public class Cluster {

    private final String id;
    private String label;
    private Centroid centroid;
    private final List<ClusterMembership> members;

    /**
     * Constructor.
     *
     * @param id Un identificador único para el clúster (ej. "cluster_1").
     */
    public Cluster(String id) {
        this.id = id;
        this.label = id; // Por defecto, la etiqueta es el ID
        this.members = new ArrayList<>();
    }

    /**
     * Obtiene el ID del clúster.
     *
     * @return El ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Obtiene la etiqueta legible por humanos del clúster.
     *
     * @return La etiqueta.
     */
    public String getLabel() {
        return label;
    }

    /**
     * Establece la etiqueta legible por humanos del clúster.
     *
     * @param label La nueva etiqueta.
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Obtiene el centroide (el centro) de este clúster.
     *
     * @return El objeto Centroid.
     */
    public Centroid getCentroid() {
        return centroid;
    }

    /**
     * Establece el centroide (el centro) de este clúster.
     *
     * @param centroid El objeto Centroid.
     */
    public void setCentroid(Centroid centroid) {
        this.centroid = centroid;
    }

    /**
     * Añade un nuevo miembro a este clúster.
     * Este método es llamado por el algoritmo de clustering.
     *
     * @param response El Response que se añade.
     * @param distance    La distancia de este miembro al centroide.
     */
    public void addMember(Response response, double distance) {
        ClusterMembership membership = new ClusterMembership(response, this, distance);
        this.members.add(membership);
    }

    /**
     * Elimina un miembro del clúster basado en su ID.
     *
     * @param responseId El ID del Response a eliminar.
     * @return true si el miembro fue encontrado y eliminado, false en caso contrario.
     */
    public boolean removeMember(String responseId) {
        return this.members.removeIf(member -> member.getResponseSetId().equals(responseId));
    }

    /**
     * Obtiene la lista de todas las membresías de este clúster.
     *
     * @return Una lista inmodificable de objetos ClusterMembership.
     */
    public List<ClusterMembership> getMembers() {
        return Collections.unmodifiableList(members);
    }

    /**
     * Obtiene el número de miembros en este clúster.
     *
     * @return El tamaño del clúster.
     */
    public int getSize() {
        return members.size();
    }

    /**
     * Comprueba si un Response (por ID) es miembro de este clúster.
     *
     * @param responseId El ID del Response a comprobar.
     * @return true si el miembro existe en este clúster, false en caso contrario.
     */
    public boolean contains(String responseId) {
        for (ClusterMembership member : members) {
            if (member.getResponseSetId().equals(responseId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Calcula la distancia promedio de todos los miembros al centroide del clúster.
     *
     * @return La distancia promedio, o 0.0 si el clúster está vacío.
     */
    public double getAverageDistance() {
        if (members.isEmpty()) {
            return 0.0;
        }

        double sumOfDistances = 0.0;
        for (ClusterMembership member : members) {
            sumOfDistances += member.getDistance();
        }
        return sumOfDistances / members.size();
    }

    // --- Métodos de utilidad (equals/hashCode) ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cluster cluster = (Cluster) o;
        return id.equals(cluster.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
