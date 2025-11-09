package edu.upc.prop.clusterxx;

import java.util.Date;

/**
 * Representa la membresía de un ResponseSet a un Cluster.
 * Es un registro simple que almacena el ID del miembro, el ID del clúster
 * y la distancia calculada a ese centroide del clúster.
 */
public class ClusterMembership {

    private final String responseSetId;
    private final String clusterId;
    private double distance; // Distancia al centroide
    private final Date assignmentDate;

    /**
     * Constructor para crear un registro de membresía.
     *
     * @param responseSet El ResponseSet que se une al clúster.
     * @param cluster     El Cluster al que se une.
     * @param distance    La distancia calculada entre el ResponseSet y el centroide del Cluster.
     */
    public ClusterMembership(ResponseSet responseSet, Cluster cluster, double distance) {
        this.responseSetId = responseSet.getId();
        this.clusterId = cluster.getId();
        this.distance = distance;
        this.assignmentDate = new Date(); // Asignar en el momento de la creación
    }

    /**
     * Obtiene el ID del ResponseSet (miembro).
     *
     * @return El ID del miembro.
     */
    public String getResponseSetId() {
        return responseSetId;
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