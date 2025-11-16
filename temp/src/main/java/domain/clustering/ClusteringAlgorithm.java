package domain.clustering;

import domain.model.Question;
import domain.model.Response;

import java.util.List;

/**
 * Interfaz para algoritmos de clustering (patrón Strategy).
 * Agrupa datos en k clusters, calcula centroides y asigna puntos.
 */
public interface ClusteringAlgorithm {

    /**
     * Ejecuta el algoritmo de clustering sobre una lista de respuestas.
     *
     * @param responses La lista de conjuntos de respuestas (puntos de datos).
     * @param questions    La lista de preguntas, necesaria para interpretar los datos.
     * @param k            El número de clusters a formar.
     * @param distance     El objeto DistanceCalculator para medir distancias.
     * @return Un objeto ClusterResults con la lista de clusters y metadatos de la ejecución.
     */
    public ClusterResults execute(List<Response> responses, List<Question> questions, int k, DistanceCalculator distance);

    /** Nombre del algoritmo (ej: "K-Means"). */
    String getName();

    /** Descripción del algoritmo, ventajas y limitaciones. */
    String getDescription();
}