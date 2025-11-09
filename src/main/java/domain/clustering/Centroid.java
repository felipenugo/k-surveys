package edu.upc.prop.clusterxx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    
    // Lista ordenada de los IDs de las preguntas, que coincide
    // con el orden de los componentes.
    private final List<String> questionIds;

    /**
     * Constructor.
     *
     * @param questionIds La lista ordenada de IDs de preguntas.
     */
    public Centroid(List<String> questionIds) {
        this.questionIds = Collections.unmodifiableList(new ArrayList<>(questionIds));
        
        // Inicializa la lista de componentes con 'null'
        this.components = new ArrayList<>(Collections.nCopies(questionIds.size(), null));
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
     * Obtiene la lista ordenada de IDs de preguntas.
     *
     * @return Una lista inmodificable de IDs de preguntas.
     */
    public List<String> getQuestionIds() {
        return questionIds;
    }

    /**
     * Un método helper crucial para el DistanceCalculator.
     * Devuelve un mapa que vincula el ID de cada pregunta con su
     * valor de componente correspondiente en el centroide.
     *
     * @return Un mapa de String (ID de Pregunta) a Object (Valor del Componente).
     */
    public Map<String, Object> getComponentsAsMap() {
        Map<String, Object> componentMap = new HashMap<>();
        for (int i = 0; i < questionIds.size(); i++) {
            componentMap.put(questionIds.get(i), components.get(i));
        }
        return componentMap;
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
            
            // questionIds es inmodificable, así que no necesita clonación profunda.
            
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
}