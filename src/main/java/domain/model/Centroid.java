package domain.model;

import java.util.List;
import java.util.ArrayList;

/**
 * Representa el centroide de un cluster.
 */
public class Centroid {
    
    private List<Object> components;
    private List<String> questionIds;
    
    public Centroid(List<String> questionIds) {
        this.questionIds = new ArrayList<>(questionIds);
        this.components = new ArrayList<>(questionIds.size());
        for (int i = 0; i < questionIds.size(); i++) {
            this.components.add(null);
        }
    }
    
    public List<Object> getComponents() {
        return new ArrayList<>(components);
    }
    
    public void setComponent(Integer index, Object value) {
        if (index >= 0 && index < components.size()) {
            components.set(index, value);
        }
    }
    
    public List<String> getQuestionIds() {
        return new ArrayList<>(questionIds);
    }
    
    /**
     * Calcula distancia a un ResponseSet.
     * TODO: Implementar cuando existan ResponseSet y DistanceCalculator.
     */
    public Double distanceTo(Object responseSet, Object calculator) {
        throw new UnsupportedOperationException("ResponseSet and DistanceCalculator not yet implemented");
    }
    
    @Override
    public Centroid clone() {
        Centroid cloned = new Centroid(this.questionIds);
        for (int i = 0; i < this.components.size(); i++) {
            cloned.components.set(i, this.components.get(i));
        }
        return cloned;
    }
}
