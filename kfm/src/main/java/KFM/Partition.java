package kfm.src.main.java.KFM;

import java.util.*;

public class Partition {
    private int id;
    private List<Vertex> vertices;

    public Partition(int id) {
        this.id = id;
        this.vertices = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public List<Vertex> getVertices() {
        return vertices;
    }

    public void addVertex(Vertex v) {
        if (!vertices.contains(v)) {
            vertices.add(v);
        }
    }

    public void removeVertex(Vertex v) {
        vertices.remove(v);
    }

    public boolean containsVertex(Vertex v) {
        return vertices.contains(v);
    }

    public double getTotalWeight() {
        double total = 0.0;
        for (Vertex v : vertices) {
            total += v.getWeight();
        }
        return total;
    }

    public int size() {
        return vertices.size();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Partition partition = (Partition) obj;
        return id == partition.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return "Partition{" +
                "id=" + id +
                ", vertices=" + vertices.size() +
                ", totalWeight=" + getTotalWeight() +
                '}';
    }
}
