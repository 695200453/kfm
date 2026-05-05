package kfm.src.KFM;

import java.util.*;

public class Vertex {
    private String id;
    private double weight;
    private List<Edge> incomingEdges;
    private List<Edge> outgoingEdges;
    private boolean enabled;
    private Partition currentPartition;
    private Partition previousPartition;
    private Partition targetPartition;

    public Vertex(String id, double weight) {
        this.id = id;
        this.weight = weight;
        this.incomingEdges = new ArrayList<>();
        this.outgoingEdges = new ArrayList<>();
        this.enabled = false;
        this.currentPartition = null;
        this.previousPartition = null;
        this.targetPartition = null;
    }

    public String getId() {
        return id;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public List<Edge> getIncomingEdges() {
        return incomingEdges;
    }

    public void addIncomingEdge(Edge edge) {
        this.incomingEdges.add(edge);
    }

    public List<Edge> getOutgoingEdges() {
        return outgoingEdges;
    }

    public void addOutgoingEdge(Edge edge) {
        this.outgoingEdges.add(edge);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Partition getCurrentPartition() {
        return currentPartition;
    }

    public void setCurrentPartition(Partition currentPartition) {
        this.currentPartition = currentPartition;
    }

    public Partition getPreviousPartition() {
        return previousPartition;
    }

    public void setPreviousPartition(Partition previousPartition) {
        this.previousPartition = previousPartition;
    }

    public Partition getTargetPartition() {
        return targetPartition;
    }

    public void setTargetPartition(Partition targetPartition) {
        this.targetPartition = targetPartition;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Vertex vertex = (Vertex) obj;
        return id != null && id.equals(vertex.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Vertex{" +
                "id='" + id + '\'' +
                ", weight=" + weight +
                ", enabled=" + enabled +
                ", partition=" + (currentPartition != null ? currentPartition.getId() : "null") +
                '}';
    }
}
