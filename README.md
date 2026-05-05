# KFM - Heuristic Graph Partitioning Algorithm

A Java implementation of the KFM heuristic algorithm for partitioning Directed Acyclic Graphs (DAGs) into subgraphs while minimizing cut edges and maintaining resource balance constraints.

## Overview

KFM is an iterative improvement partitioning algorithm designed for DAGs. It optimizes three main objectives:

1. **Minimize cut edges** - Reduce communication cost between partitions
2. **Maintain resource balance** - Ensure each partition's total weight does not exceed the specified capacity (rMax)
3. **Preserve acyclic topology** - Maintain the DAG structure across partitions

## Algorithm

The algorithm performs `tau` iterations of FM passes, each following this workflow:

1. Obtain topological ordering of vertices
2. Initialize partitions using greedy balanced allocation
3. Execute FM optimization pass to improve partitioning
4. Select the best partition from all iterations

### FM Pass Mechanism

- Selects partition pairs with the most enabled vertices
- Computes gain for each candidate vertex (reduction in cut edges)
- Moves vertices with positive gain while respecting constraints
- Dynamically updates candidate discovery during the process

## Project Structure

```
src/KFM/
├── KFM.java          # Core partitioning algorithm implementation
├── Graph.java        # DAG representation with topological sorting
├── Vertex.java       # Vertex data structure with edge management
├── Edge.java         # Edge data structure with weight support
├── Partition.java    # Partition container for vertices
├── PriorityQueue.java # Max-heap for FM gain-based selection
├── PQEntry.java      # Priority queue entry wrapper
└── KFMExample.java   # Usage example demonstrating the algorithm
```

## Building

Requires Java Development Kit (JDK) 8 or higher.

```bash
# Compile
javac -d out src/KFM/*.java

# Run example
java -cp out KFM.KFMExample
```

## Usage Example

```java
// Create a DAG
Graph graph = new Graph();
Vertex v0 = new Vertex("v0", 10.0);
Vertex v1 = new Vertex("v1", 20.0);
graph.addVertex(v0);
graph.addVertex(v1);
graph.addEdge(v0, v1, 5.0);

// Configure partitioning parameters
double rMax = 100.0;      // Maximum resource capacity per partition
double mu = 1.2;          // Scaling factor for partition count
int numSlot = 4;          // Number of worker slots
int numTask = 8;          // Total number of tasks
int tau = 10;             // Number of iterations

// Execute KFM optimization
KFM kfm = new KFM(graph, rMax, mu, numSlot, numTask, tau);
List<Partition> result = kfm.optimize();

// Access results
for (Partition p : result) {
    System.out.println("Partition " + p.getId() + ": " + p.size() + " vertices");
}
```

## Parameters

| Parameter | Description |
|-----------|-------------|
| `rMax` | Maximum resource capacity per partition |
| `mu` | Scaling factor for computing number of partitions |
| `numSlot` | Number of available worker slots |
| `numTask` | Total number of tasks in the application |
| `tau` | Number of FM iterations to perform |

## Output

The algorithm outputs partition assignments and provides DOT format export for visualization:

```bash
# Generate DOT file for Graphviz
System.out.println(graph.toDotFormat());
```

## Open Source Guidelines

### Licensing

This project is released under the **MIT License**. You are free to use, modify, and distribute this software under the terms of the MIT License.

### Contributing

Contributions are welcome. Please ensure your code adheres to the following guidelines:

- **Code Style**: Follow the existing Java conventions in the codebase
- **Testing**: Verify that changes do not break existing functionality
- **Documentation**: Update comments and documentation as needed

### How to Contribute

1. Fork the repository
2. Create a feature branch for your changes
3. Make your modifications
4. Submit a pull request with a clear description of the changes

### Citation

If you use this algorithm in your research, please cite the original authors of the KFM algorithm.

### Disclaimer

This implementation is provided as-is without warranty of any kind. The authors are not responsible for any damages or issues arising from the use of this software.
