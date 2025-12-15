package domain.clustering;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Representa el centro de un clúster (un centroide).
 * Este centro es un punto en el espacio de n-dimensiones (donde n = número de preguntas).
 * Puede ser un punto artificial (promedio) o un punto real (medoide),
 * o un híbrido de ambos.
 */
public class Centroid implements Cloneable {

    // Lista de valores que componen el centroide.
    // Puede contener Double, double[], o String.
    private List<Object> components;
    // Número de dimensiones (preguntas) del centroide.
    private final int numDimensions;

    /**
     * Constructor.
     *
     * @param numDimensions El número de dimensiones (preguntas).
     */
    public Centroid(int numDimensions) {
        this.numDimensions = numDimensions;
        // Inicializa la lista de componentes con 'null'
        this.components = new ArrayList<>(Collections.nCopies(numDimensions, null));
    }

    /**
     * Obtiene el número de dimensiones del centroide.
     * @return el número de dimensiones.
     */
    public int getNumDimensions() {
        return numDimensions;
    }

    /**
     * Obtiene la lista de componentes (valores) del centroide.
     *
     * @return Una lista de objetos (Double, double[], String).
     */
    public List<Object> getComponents() {
        return components;
    }

    /**
     * Obtiene el componente (valor) en un índice específico.
     *
     * @param index El índice del componente.
     * @return El objeto en ese índice.
     */
    public Object getComponent(int index) {
        return components.get(index);
    }

    /**
     * Establece el componente (valor) en un índice específico.
     *
     * @param index El índice del componente.
     * @param value El nuevo valor (Double, double[], String).
     */
    public void setComponent(int index, Object value) {
        components.set(index, value);
    }

    /**
     * Crea y devuelve una copia de este centroide.
     * Esto es crucial para el bucle de K-Means (comparar oldCentroids con newCentroids).
     *
     * @return Un nuevo objeto Centroid con los mismos datos.
     */
    @Override
    public Centroid clone() {
        try {
            // Inicia con una clonación superficial
            Centroid cloned = (Centroid) super.clone();
            
            // components ES mutable (es un ArrayList) y CONTIENE objetos mutables (double[]).
            // Necesitamos clonar la lista y su contenido mutable.
            cloned.components = new ArrayList<>(this.components.size());
            for (Object component : this.components) {
                if (component instanceof double[]) {
                    // ¡Importante! double[] es mutable, debe ser clonado.
                    cloned.components.add(((double[]) component).clone());
                } else {
                    // Double y String son inmutables, se pueden copiar por referencia.
                    cloned.components.add(component);
                }
            }
            return cloned;
        } catch (CloneNotSupportedException e) {
            // Esto no debería ocurrir ya que implementamos Cloneable
            throw new InternalError(e);
        }
    }

    /**
     * Representación en cadena del centroide para depuración.
     *
     * @return Una cadena que representa el centroide.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Centroid{components=[");
        for (int i = 0; i < components.size(); i++) {
            Object component = components.get(i);
            if (component instanceof double[]) {
                sb.append(java.util.Arrays.toString((double[]) component));
            } else {
                sb.append(component);
            }
            if (i < components.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append("]}");
        return sb.toString();
    }
}