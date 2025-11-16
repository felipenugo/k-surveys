package domain.clustering;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Centroid implements Cloneable {

    private List<Object> components;
    private final int numDimensions;

    public Centroid(int numDimensions) {
        this.numDimensions = numDimensions;
        this.components = new ArrayList<>(Collections.nCopies(numDimensions, null));
    }

    public int getNumDimensions() { return numDimensions; }
    public List<Object> getComponents() { return components; }
    public Object getComponent(int index) { return components.get(index); }
    public void setComponent(int index, Object value) { components.set(index, value); }

    @Override public Centroid clone() {
        try {
            Centroid cloned = (Centroid) super.clone();
            cloned.components = new ArrayList<>(this.components.size());
            for (Object component : this.components) {
                if (component instanceof double[]) cloned.components.add(((double[]) component).clone()); else cloned.components.add(component);
            }
            return cloned;
        } catch (CloneNotSupportedException e) { throw new InternalError(e); }
    }

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
