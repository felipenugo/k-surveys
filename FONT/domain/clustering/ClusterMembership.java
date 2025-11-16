package domain.clustering;

import domain.model.Response;

import java.util.Date;

/**
 * Representa la membresía de un Response a un Cluster.
 * Es un registro simple que almacena el ID del miembro, el ID del clúster
 * y la distancia calculada a ese centroide del clúster.
 */
public class ClusterMembership {

    private final String responseId;
    private final String clusterId;
    private double distance; // Distancia al centroide
    private final Date assignmentDate;

    /**
     * Constructor para crear un registro de membresía.
     *
     * @param response El Response que se une al clúster.
     * @param cluster     El Cluster al que se une.
     * @param distance    La distancia calculada entre el Response y el centroide del Cluster.
     */
    public ClusterMembership(Response response, Cluster cluster, double distance) {
        this.responseId = response.getRESPONSE_ID();
        this.clusterId = cluster.getId();
        this.distance = distance;
        this.assignmentDate = new Date(); // Asignar en el momento de la creación
    }

    /**
     * Obtiene el ID del Response (miembro).
     *
     * @return El ID del miembro.
     */
    public String getResponseId() {
        return responseId;
    }

    /**
     * Obtiene el ID del clúster.
     *
     * @return El ID del clúster.
     */
    public String getClusterId() {
        return clusterId;
    }

    /**
     * Obtiene la distancia del miembro al centroide del clúster.
     *
     * @return La distancia calculada.
     */
    public double getDistance() {
        return distance;
    }

    /**
     * Actualiza la distancia (puede ser útil si el centroide se recalcula).
     *
     * @param distance La nueva distancia.
     */
    public void setDistance(double distance) {
        this.distance = distance;
    }
}
