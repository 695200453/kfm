package kfm.src.KFM;

public class PQEntry {
    public Vertex vertex;
    public double gain;
    public Partition fromPartition;
    public Partition toPartition;

    public PQEntry(Vertex vertex, double gain, Partition from, Partition to) {
        this.vertex = vertex;
        this.gain = gain;
        this.fromPartition = from;
        this.toPartition = to;
    }

    @Override
    public String toString() {
        return "PQEntry{" +
                "vertex=" + vertex.getId() +
                ", gain=" + gain +
                ", from=" + (fromPartition != null ? fromPartition.getId() : "null") +
                ", to=" + (toPartition != null ? toPartition.getId() : "null") +
                '}';
    }
}
