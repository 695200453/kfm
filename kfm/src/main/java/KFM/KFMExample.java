package kfm.src.main.java.KFM;


import java.util.*;

public class KFMExample {
    public static void main(String[] args) {

        Graph graph = createSampleGraph();

        double rMax = 100.0;
        double mu = 1.2;
        int numSlot = 4;
        int numTask = 8;
        int tau = 10;

       KFM kfm = new KFM(graph, rMax, mu, numSlot, numTask, tau);

        List<Partition> result = kfm.optimize();

        System.out.println("\n=== KFM Optimization Result ===");
        System.out.println("Number of partitions: " + result.size());

        for (Partition p : result) {
            System.out.println("\nPartition " + p.getId() + ":");
            System.out.println("  Total weight: " + p.getTotalWeight());
            System.out.println("  Vertices: " + p.size());
            System.out.print("  Vertex IDs: ");
            for (Vertex v : p.getVertices()) {
                System.out.print(v.getId() + " ");
            }
            System.out.println();
        }

        System.out.println("\n=== Graph in DOT format ===");
        System.out.println(graph.toDotFormat());
    }

    private static Graph createSampleGraph() {
        Graph graph = new Graph();

        Vertex v0 = new Vertex("v0", 10.0);
        Vertex v1 = new Vertex("v1", 20.0);
        Vertex v2 = new Vertex("v2", 15.0);
        Vertex v3 = new Vertex("v3", 25.0);
        Vertex v4 = new Vertex("v4", 18.0);
        Vertex v5 = new Vertex("v5", 22.0);
        Vertex v6 = new Vertex("v6", 12.0);
        Vertex v7 = new Vertex("v7", 16.0);

        graph.addVertex(v0);
        graph.addVertex(v1);
        graph.addVertex(v2);
        graph.addVertex(v3);
        graph.addVertex(v4);
        graph.addVertex(v5);
        graph.addVertex(v6);
        graph.addVertex(v7);

        graph.addEdge(v0, v2, 5.0);
        graph.addEdge(v0, v4, 3.0);
        graph.addEdge(v1, v2, 4.0);
        graph.addEdge(v1, v3, 6.0);
        graph.addEdge(v2, v3, 2.0);
        graph.addEdge(v2, v5, 7.0);
        graph.addEdge(v3, v6, 5.0);
        graph.addEdge(v4, v5, 3.0);
        graph.addEdge(v5, v6, 4.0);
        graph.addEdge(v5, v7, 6.0);
        graph.addEdge(v6, v7, 2.0);

        return graph;
    }
}
