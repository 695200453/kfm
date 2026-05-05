package src.KFM;



public class Edge {
    private Vertex source;
    private Vertex target;
    private double weight;

    public Edge(Vertex source, Vertex target, double weight) {
        this.source = source;
        this.target = target;
        this.weight = weight;
    }

    public Vertex getSource() {
        return source;
    }

    public Vertex getTarget() {
        return target;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        return "Edge{" +
                "source=" + source.getId() +
                ", target=" + target.getId() +
                ", weight=" + weight +
                '}';
    }
}
