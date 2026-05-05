package src.KFM;

import java.util.*;

/**
 * KFM Heuristic Graph Partitioning Algorithm.
 * 
 * This algorithm partitions a directed acyclic graph (DAG) into k subgraphs (partitions)
 * while optimizing the following objectives:
 * 
 * 1. Minimize the cut edges (communication cost between partitions)
 * 2. Maintain resource balance constraints (each partition's total weight <= rMax)
 * 3. Preserve acyclic topology between partitions
 * 
 * The algorithm follows an iterative improvement approach:
 * - Performs tau iterations of FM pass starting from different initial configurations
 * - In each iteration:
 *   1. Obtains topological ordering of vertices
 *   2. Initializes partitions using greedy balanced allocation
 *   3. Performs FM optimization pass to improve partitioning
 * - Selects the best partition from all iterations
 * 
 * The FM pass works by:
 * - Selecting partition pairs and identifying candidate vertices for movement
 * - Computing gain for each candidate (reduction in cut edges)
 * - Moving vertices with positive gain while maintaining constraints
 * - Updating vertex states and discovering new candidates iteratively
 * 
 * @param graph The input graph to be partitioned
 * @param rMax Maximum resource capacity per partition
 * @param mu Scaling factor for computing number of partitions
 * @param numSlot Number of worker slots available
 * @param numTask Total number of tasks in the application
 * @param tau Number of iterations to perform
 */
public class KFM {
    private Graph graph;
    private double rMax;
    private double mu;
    private int numSlot;
    private int numTask;
    private int tau;
    private List<Partition> partitions;
    private List<Partition[]> history;

    private PriorityQueue pq;

    public KFM(Graph graph, double rMax, double mu, int numSlot, int numTask, int tau) {
        this.graph = graph;
        this.rMax = rMax;
        this.mu = mu;
        this.numSlot = numSlot;
        this.numTask = numTask;
        this.tau = tau;
        this.partitions = new ArrayList<>();
        this.history = new ArrayList<>();
        this.pq = new PriorityQueue();
    }

    public List<Partition> optimize() {
        int k = computeNumberOfPartitions();

        for (int t = 0; t < tau; t++) {
            List<Vertex> topologicalList = graph.getTopologicalList();

            Partition[] currentPartitions = initializePartitions(topologicalList, k);

            enableAllVertices(currentPartitions);

            performFMPass(currentPartitions);

            history.add(currentPartitions);
        }

        return compareAndSelectOptimal();
    }

    private int computeNumberOfPartitions() {
        double totalWeight = 0.0;
        for (Vertex v : graph.getVertices()) {
            totalWeight += v.getWeight();
        }
        double frG = totalWeight;
        return Math.max(1, (int) Math.ceil((mu * rMax * numTask) / (numSlot * frG)));
    }

    private List<Vertex> getTopologicalList() {
        return graph.getTopologicalList();
    }

    private Partition[] initializePartitions(List<Vertex> topologicalList, int k) {
        Partition[] parts = new Partition[k];
        for (int i = 0; i < k; i++) {
            parts[i] = new Partition(i);
        }

        double[] partitionLoads = new double[k];
        double targetLoad = graph.getTotalWeight() / k;

        for (Vertex v : topologicalList) {
            int minIndex = 0;
            double minLoad = partitionLoads[0];
            for (int i = 1; i < k; i++) {
                if (partitionLoads[i] < minLoad) {
                    minLoad = partitionLoads[i];
                    minIndex = i;
                }
            }
            parts[minIndex].addVertex(v);
            partitionLoads[minIndex] += v.getWeight();
            graph.setVertexPartition(v, parts[minIndex]);
        }

        return parts;
    }

    private void enableAllVertices(Partition[] parts) {
        for (Partition p : parts) {
            for (Vertex v : p.getVertices()) {
                v.setEnabled(true);
            }
        }
    }

    private void performFMPass(Partition[] parts) {
        Set<String> verticesInQueue = new HashSet<>();
        int maxIterations = 1000;
        int iterationCount = 0;

        while (hasEnabledVertex(parts) && iterationCount < maxIterations) {
            iterationCount++;
            Partition[] pair = selectPartitionPair(parts);
            if (pair == null) break;

            Partition gA = pair[0];
            Partition gB = pair[1];

            if (gA.getId() > gB.getId()) {
                Partition temp = gA;
                gA = gB;
                gB = temp;
            }

            pq.clear();
            verticesInQueue.clear();

            List<Vertex> candidatesA = identifyCandidates(gA, gB);
            List<Vertex> candidatesB = identifyCandidates(gB, gA);

            if (candidatesA.isEmpty() && candidatesB.isEmpty()) {
                disableAllVertices(parts);
                break;
            }

            for (Vertex v : candidatesA) {
                double gain = computeGain(v, gA, gB);
                pq.insert(v, gain, gA, gB);
                verticesInQueue.add(v.getId());
            }

            for (Vertex v : candidatesB) {
                double gain = computeGain(v, gB, gA);
                pq.insert(v, gain, gB, gA);
                verticesInQueue.add(v.getId());
            }

            boolean madeMove = false;
            while (!pq.isEmpty()) {
               PQEntry entry = pq.popMax();
                verticesInQueue.remove(entry.vertex.getId());

                if (!entry.vertex.isEnabled()) {
                    continue;
                }

                double moveGain = entry.gain;
                boolean balanceImprovement = (moveGain == 0) && entry.vertex.getWeight() <
                        Math.abs(getPartitionLoad(entry.toPartition) - getPartitionLoad(entry.fromPartition));

                boolean moveFeasible = acyclicCheck(entry.vertex, entry.fromPartition, entry.toPartition) &&
                        resourceBalanceHolds(entry.vertex, entry.fromPartition, entry.toPartition);

                if (moveFeasible) {
                    if (moveGain > 0 || balanceImprovement) {
                        moveVertex(entry.vertex, entry.fromPartition, entry.toPartition);
                        updateStates(entry.vertex, entry.fromPartition, entry.toPartition);
                        madeMove = true;

                        List<Vertex> newCandidatesA = identifyCandidates(gA, gB);
                        List<Vertex> newCandidatesB = identifyCandidates(gB, gA);
                        
                        for (Vertex nc : newCandidatesA) {
                            if (!verticesInQueue.contains(nc.getId())) {
                                double ncGain = computeGain(nc, gA, gB);
                                pq.insert(nc, ncGain, gA, gB);
                                verticesInQueue.add(nc.getId());
                            }
                        }
                        
                        for (Vertex nc : newCandidatesB) {
                            if (!verticesInQueue.contains(nc.getId())) {
                                double ncGain = computeGain(nc, gB, gA);
                                pq.insert(nc, ncGain, gB, gA);
                                verticesInQueue.add(nc.getId());
                            }
                        }
                    }
                    entry.vertex.setEnabled(false);
                }
            }
            
            if (!madeMove) {
                disableAllVertices(parts);
                break;
            }
        }
    }
    
    private void disableAllVertices(Partition[] parts) {
        for (Partition p : parts) {
            for (Vertex v : p.getVertices()) {
                v.setEnabled(false);
            }
        }
    }

    private boolean hasEnabledVertex(Partition[] parts) {
        for (Partition p : parts) {
            for (Vertex v : p.getVertices()) {
                if (v.isEnabled()) return true;
            }
        }
        return false;
    }

    private Partition[] selectPartitionPair(Partition[] parts) {
        Partition[] bestPair = null;
        int maxEnabled = 0;

        for (int i = 0; i < parts.length; i++) {
            for (int j = i + 1; j < parts.length; j++) {
                int enabledCount = countEnabledVertices(parts[i]) + countEnabledVertices(parts[j]);
                if (enabledCount > maxEnabled) {
                    maxEnabled = enabledCount;
                    bestPair = new Partition[]{parts[i], parts[j]};
                }
            }
        }
        return bestPair;
    }

    private int countEnabledVertices(Partition p) {
        int count = 0;
        for (Vertex v : p.getVertices()) {
            if (v.isEnabled()) count++;
        }
        return count;
    }

    private List<Vertex> identifyCandidates(Partition from, Partition to) {
        List<Vertex> candidates = new ArrayList<>();

        for (Vertex v : from.getVertices()) {
            if (!v.isEnabled()) continue;

            boolean hasEdgeToEarlier = false;
            for (Edge e : v.getOutgoingEdges()) {
                Vertex target = e.getTarget();
                Partition targetPart = graph.getVertexPartition(target);
                if (targetPart != null && targetPart.getId() < to.getId()) {
                    hasEdgeToEarlier = true;
                    break;
                }
            }

            if (!hasEdgeToEarlier && acyclicCheck(v, from, to)) {
                candidates.add(v);
            }
        }

        return candidates;
    }



    private double computeGain(Vertex v, Partition from, Partition to) {
        double cIn = computeCIn(v, from);
        double cOut = computeCOut(v, to);

        if (from.getId() < to.getId()) {
            return cOut - cIn;
        } else {
            return cIn - cOut;
        }
    }

    private double computeCIn(Vertex v, Partition g) {
        double sum = 0.0;
        for (Edge e : v.getIncomingEdges()) {
            Vertex u = e.getSource();
            if (g.containsVertex(u)) {
                sum += e.getWeight();
            }
        }
        return sum;
    }

    private double computeCOut(Vertex v, Partition g) {
        double sum = 0.0;
        for (Edge e : v.getOutgoingEdges()) {
            Vertex u = e.getTarget();
            if (g.containsVertex(u)) {
                sum += e.getWeight();
            }
        }
        return sum;
    }

    private boolean acyclicCheck(Vertex v, Partition from, Partition to) {
        if (from.getId() < to.getId()) {
            // v ∈ gA → move to gB (A < B)
            // Check: ¬∃ (v → u) ∈ E such that block(u) < B
            for (Edge e : v.getOutgoingEdges()) {
                Vertex u = e.getTarget();
                Partition blockU = graph.getVertexPartition(u);
                if (blockU.getId() < to.getId()) {
                    return false;
                }
            }
        } else {
            // v ∈ gB → move to gA (B > A)
            // Check: ¬∃ (u → v) ∈ E such that block(u) > A
            for (Edge e : v.getIncomingEdges()) {
                Vertex u = e.getSource();
                Partition blockU = graph.getVertexPartition(u);
                if (blockU.getId() > from.getId()) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean resourceBalanceHolds(Vertex v, Partition from, Partition to) {
        double fromLoad = getPartitionLoad(from);
        double toLoad = getPartitionLoad(to);
        double vWeight = v.getWeight();

        return (fromLoad - vWeight) >= 0 && (toLoad + vWeight) <= rMax;
    }

    private double getPartitionLoad(Partition p) {
        double load = 0.0;
        for (Vertex v : p.getVertices()) {
            load += v.getWeight();
        }
        return load;
    }

    private void moveVertex(Vertex v, Partition from, Partition to) {
        v.setPreviousPartition(from);
        from.removeVertex(v);
        to.addVertex(v);
        v.setCurrentPartition(to);
        graph.setVertexPartition(v, to);
    }

    private void updateStates(Vertex v, Partition from, Partition to) {
        if (from.getId() < to.getId()) {
            // Forward migration (gA → gB)
            // 1. Disable v and all its neighbors in gB with incoming edges from v
            v.setEnabled(false);
            for (Edge e : v.getOutgoingEdges()) {
                Vertex target = e.getTarget();
                if (graph.getVertexPartition(target).equals(to)) {
                    target.setEnabled(false);
                }
            }
            
            // 2. Enable vertices in gA that point to v or no longer have successors in earlier partitions
            for (Edge e : v.getIncomingEdges()) {
                Vertex source = e.getSource();
                if (graph.getVertexPartition(source).equals(from)) {
                    source.setEnabled(true);
                }
            }
            
            for (Vertex u : from.getVertices()) {
                if (!u.isEnabled() && noSuccessorsInEarlierPartitions(u, to)) {
                    u.setEnabled(true);
                }
            }
        } else {
            // Reverse migration (gB → gA)
            // 1. Disable v and all its neighbors in gA with outgoing edges to v
            v.setEnabled(false);
            for (Edge e : v.getIncomingEdges()) {
                Vertex source = e.getSource();
                if (graph.getVertexPartition(source).equals(to)) {
                    source.setEnabled(false);
                }
            }
            
            // 2. Enable vertices in gB that are pointed to by v or no longer have predecessors in later partitions
            for (Edge e : v.getOutgoingEdges()) {
                Vertex target = e.getTarget();
                if (graph.getVertexPartition(target).equals(from)) {
                    target.setEnabled(true);
                }
            }
            
            for (Vertex u : from.getVertices()) {
                if (!u.isEnabled() && noPredecessorsInLaterPartitions(u, to)) {
                    u.setEnabled(true);
                }
            }
        }
    }

    private boolean noSuccessorsInEarlierPartitions(Vertex u, Partition to) {
        for (Edge e : u.getOutgoingEdges()) {
            Vertex target = e.getTarget();
            Partition targetPart = graph.getVertexPartition(target);
            if (targetPart.getId() < to.getId()) {
                return false;
            }
        }
        return true;
    }

    private boolean noPredecessorsInLaterPartitions(Vertex u, Partition to) {
        for (Edge e : u.getIncomingEdges()) {
            Vertex source = e.getSource();
            Partition sourcePart = graph.getVertexPartition(source);
            if (sourcePart.getId() > to.getId()) {
                return false;
            }
        }
        return true;
    }

    private List<Partition> compareAndSelectOptimal() {
        Partition[] best = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (Partition[] candidate : history) {
            double score = evaluatePartitioning(candidate);
            if (score > bestScore) {
                bestScore = score;
                best = candidate;
            }
        }

        return best != null ? Arrays.asList(best) : new ArrayList<>();
    }

    private double evaluatePartitioning(Partition[] parts) {
        double cutEdges = 0.0;

        for (Partition p : parts) {
            for (Vertex v : p.getVertices()) {
                for (Edge e : v.getOutgoingEdges()) {
                    Vertex target = e.getTarget();
                    if (!p.containsVertex(target)) {
                        cutEdges += e.getWeight();
                    }
                }
            }
        }

        double balancePenalty = computeBalancePenalty(parts);

        return -cutEdges - balancePenalty;
    }

    private double computeBalancePenalty(Partition[] parts) {
        double totalLoad = 0.0;
        for (Partition p : parts) {
            totalLoad += getPartitionLoad(p);
        }
        double avgLoad = totalLoad / parts.length;

        double penalty = 0.0;
        for (Partition p : parts) {
            double diff = Math.abs(getPartitionLoad(p) - avgLoad);
            penalty += diff;
        }

        return penalty;
    }

    public List<Partition[]> getHistory() {
        return history;
    }
}
