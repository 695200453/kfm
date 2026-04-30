package kfm.src.main.java.KFM;

import java.util.*;

public class Graph {
    private List<Vertex> vertices;
    private List<Edge> edges;
    private Map<String, Vertex> vertexMap;
    private Map<Vertex, Partition> vertexPartitionMap;

    public Graph() {
        this.vertices = new ArrayList<>();
        this.edges = new ArrayList<>();
        this.vertexMap = new HashMap<>();
        this.vertexPartitionMap = new HashMap<>();
    }

    public void addVertex(Vertex v) {
        vertices.add(v);
        vertexMap.put(v.getId(), v);
    }

    public void addEdge(Vertex source, Vertex target, double weight) {
        Edge edge = new Edge(source, target, weight);
        edges.add(edge);
        source.addOutgoingEdge(edge);
        target.addIncomingEdge(edge);
    }

    public List<Vertex> getVertices() {
        return vertices;
    }

    public Vertex getVertex(String id) {
        return vertexMap.get(id);
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public List<Vertex> getTopologicalList() {
        List<Vertex> result = new ArrayList<>();
        Map<Vertex, Integer> inDegree = new HashMap<>();
        Queue<Vertex> queue = new LinkedList<>();

        for (Vertex v : vertices) {
            inDegree.put(v, v.getIncomingEdges().size());
            if (v.getIncomingEdges().isEmpty()) {
                queue.offer(v);
            }
        }

        while (!queue.isEmpty()) {
            Vertex current = queue.poll();
            result.add(current);

            for (Edge e : current.getOutgoingEdges()) {
                Vertex neighbor = e.getTarget();
                int degree = inDegree.get(neighbor) - 1;
                inDegree.put(neighbor, degree);
                if (degree == 0) {
                    queue.offer(neighbor);
                }
            }
        }

        if (result.size() != vertices.size()) {
            result = new ArrayList<>(vertices);
        }

        return result;
    }

    public double getTotalWeight() {
        double total = 0.0;
        for (Vertex v : vertices) {
            total += v.getWeight();
        }
        return total;
    }

    public void setVertexPartition(Vertex v, Partition p) {
        vertexPartitionMap.put(v, p);
        v.setCurrentPartition(p);
    }

    public Partition getVertexPartition(Vertex v) {
        return vertexPartitionMap.get(v);
    }

    public String toDotFormat() {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph G {\n");

        for (Vertex v : vertices) {
            sb.append("  ").append(v.getId())
                    .append(" [label=\"").append(v.getId())
                    .append(" (").append(v.getWeight()).append(")\"]\n");
        }

        for (Edge e : edges) {
            sb.append("  ").append(e.getSource().getId())
                    .append(" -> ").append(e.getTarget().getId());
            if (e.getWeight() != 1.0) {
                sb.append(" [label=\"").append(e.getWeight()).append("\"]");
            }
            sb.append("\n");
        }

        sb.append("}\n");
        return sb.toString();
    }
}
