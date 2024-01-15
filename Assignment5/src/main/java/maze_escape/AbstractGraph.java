package maze_escape;

import java.util.*;
import java.util.function.BiFunction;

public abstract class AbstractGraph<V> {

    /**
     * Graph representation:
     * this class implements graph search algorithms on a graph with abstract vertex type V
     * for every vertex in the graph, its neighbours can be found by use of abstract method getNeighbours(fromVertex)
     * this abstraction can be used for both directed and undirected graphs
     **/

    public AbstractGraph() {
    }

    /**
     * retrieves all neighbours of the given fromVertex
     * if the graph is directed, the implementation of this method shall follow the outgoing edges of fromVertex
     *
     * @param fromVertex
     * @return
     */
    public abstract Set<V> getNeighbours(V fromVertex);

    /**
     * retrieves all vertices that can be reached directly or indirectly from the given firstVertex
     * if the graph is directed, only outgoing edges shall be traversed
     * firstVertex shall be included in the result as well
     * if the graph is connected, all vertices shall be found
     *
     * @param firstVertex the start vertex for the retrieval
     * @return
     */
    public Set<V> getAllVertices(V firstVertex) {
        Set<V> visited = new HashSet<>(); // Set to keep track of visited vertices
        visited.add(firstVertex);
        Queue<V> queue = new LinkedList<>(); // Queue to keep track of vertices to be visited
        queue.add(firstVertex);

        while (!queue.isEmpty()) {
            V v = queue.poll();
            // Add all unvisited neighbours to the queue
            for (V neighbour : getNeighbours(v)) {
                if (!visited.contains(neighbour)) {
                    visited.add(neighbour);
                    queue.add(neighbour);
                }
            }
        }
        return visited;
    }


    /**
     * Formats the adjacency list of the subgraph starting at the given firstVertex
     * according to the format:
     * vertex1: [neighbour11,neighbour12,…]
     * vertex2: [neighbour21,neighbour22,…]
     * …
     * Uses a pre-order traversal of a spanning tree of the sub-graph starting with firstVertex as the root
     * if the graph is directed, only outgoing edges shall be traversed
     * , and using the getNeighbours() method to retrieve the roots of the child subtrees.
     *
     * @param firstVertex
     * @return
     */
    public String formatAdjacencyList(V firstVertex) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Graph adjacency list:\n");

        // Process for firstVertex is separated
        stringBuilder.append(firstVertex.toString()).append(": [");
        appendWithComma(stringBuilder, getNeighbours(firstVertex));
        stringBuilder.append("]\n");

        // Process for other vertices
        for (V vertex : getAllVertices(firstVertex)) {
            // Skip if it's the firstVertex (already handled)
            if (!vertex.equals(firstVertex)) {
                stringBuilder.append(vertex).append(": [");
                appendWithComma(stringBuilder, getNeighbours(vertex));
                stringBuilder.append("]\n");
            }
        }
        return stringBuilder.toString();
    }

    /**
     * Appends each item in the collection to the StringBuilder with a comma delimiter.
     *
     * @param stringBuilder The StringBuilder to append the items to.
     * @param items         The collection of items to be appended.
     */
    private void appendWithComma(StringBuilder stringBuilder, Collection<V> items) {
        Iterator<V> iterator = items.iterator();
        while (iterator.hasNext()) {
            stringBuilder.append(iterator.next().toString());
            if (iterator.hasNext()) {
                stringBuilder.append(",");
            }
        }
    }

    /**
     * Uses a depth-first search algorithm to find a path from the startVertex to targetVertex in the subgraph
     * All vertices that are being visited by the search should also be registered in path.visited
     *
     * @param startVertex
     * @param targetVertex
     * @return the path from startVertex to targetVertex
     * or null if target cannot be matched with a vertex in the sub-graph from startVertex
     */
    public GPath depthFirstSearch(V startVertex, V targetVertex) {
        // If startVertex or targetVertex is null, return null as there is no possible path to be found.
        if (startVertex == null || targetVertex == null) return null;
        GPath path = new GPath();
        // Add startVertex to visited list
        path.visited.add(startVertex);
        // Call recursive DFS method
        return depthFirstSearchRecursive(startVertex, targetVertex, path);
    }

    /**
     * Uses a depth-first search algorithm to find a path from the startVertex to targetVertex in the subgraph.
     * All vertices that are being visited by the search should also be registered in path.visited.
     *
     * @param startVertex  The starting vertex for the search
     * @param targetVertex The target vertex to find a path to
     * @param path         The GPath object to store the path and visited vertices
     * @return The path from startVertex to targetVertex, or null if targetVertex cannot be reached from startVertex
     */
    private GPath depthFirstSearchRecursive(V startVertex, V targetVertex, GPath path) {
        // If the start vertex is the same as target, we have found a path
        if (startVertex.equals(targetVertex)) {
            path.vertices.add(startVertex);
            return path;
        }

        // If the start vertex is not the target, go through each neighbour of the start vertex
        for (V neighbour : getNeighbours(startVertex)) {

            // If the neighbour has not been visited yet, add it to the visited list and do a recursive search
            if (!path.visited.contains(neighbour)) {
                path.visited.add(neighbour);

                // Perform DFS on the unvisited neighbour
                GPath result = depthFirstSearchRecursive(neighbour, targetVertex, path);

                // If a path is returned (i.e., not null), then add the current vertex at the start of the path
                if (result != null) {
                    path.vertices.addFirst(startVertex);
                    return path;
                }
            }
        }

        return null; // If no path to the targetVertex can be found from the current startVertex
    }

    /**
     * Uses a breadth-first search algorithm to find a path from the startVertex to targetVertex in the subgraph
     * All vertices that are being visited by the search should also be registered in path.visited
     *
     * @param startVertex
     * @param targetVertex
     * @return the path from startVertex to targetVertex
     * or null if target cannot be matched with a vertex in the sub-graph from startVertex
     */
    public GPath breadthFirstSearch(V startVertex, V targetVertex) {
        if (startVertex == null || targetVertex == null) return null;

        Set<V> visited = new HashSet<>();
        Map<V, V> previousVertexMap = new HashMap<>();
        Queue<V> queue = new LinkedList<>();
        GPath path = new GPath();

        queue.add(startVertex);
        visited.add(startVertex);
        while (!queue.isEmpty()) {
            V currentVertex = queue.poll(); // Get the first vertex in the queue

            if (currentVertex.equals(targetVertex)) {
                while (currentVertex != null) {
                    path.getVertices().add(currentVertex); // Add vertex to the path
                    currentVertex = previousVertexMap.get(currentVertex); // Move to its parent node
                }
                Collections.reverse((LinkedList<V>) path.getVertices()); // Reverse the path to get: start -> target
                path.getVisited().addAll(visited); // Add all visited vertices to the path
                return path;
            }

            for (V neighbor : getNeighbours(currentVertex)) {
                // If the neighbor has not been visited yet, add it to the queue and visited list and set its parent
                if (!visited.contains(neighbor)) {
                    queue.offer(neighbor);
                    visited.add(neighbor);
                    previousVertexMap.put(neighbor, currentVertex);
                }
            }
        }

        return null; // Target not found
    }

    /**
     * Calculates the edge-weighted shortest path from the startVertex to targetVertex in the subgraph
     * according to Dijkstra's algorithm of a minimum spanning tree
     *
     * @param startVertex
     * @param targetVertex
     * @param weightMapper provides a function(v1,v2) by which the weight of an edge from v1 to v2
     *                     can be retrieved or calculated
     * @return the shortest path from startVertex to targetVertex
     * or null if target cannot be matched with a vertex in the sub-graph from startVertex
     */
    public GPath dijkstraShortestPath(V startVertex, V targetVertex, BiFunction<V, V, Double> weightMapper) {
        // If startVertex or targetVertex is null, return null as there is no possible path to be found.
        if (startVertex == null || targetVertex == null) return null;

        GPath path = new GPath();
        Map<V, MSTNode> minimumSpanningTree = new HashMap<>(); // Map to keep track of minimum spanning tree

        MSTNode startNode = new MSTNode(startVertex);
        startNode.weightSumTo = 0.0;
        minimumSpanningTree.put(startVertex, startNode);

        PriorityQueue<MSTNode> queue = new PriorityQueue<>(); // Priority Queue to select the least weight node
        queue.add(startNode);

        while (!queue.isEmpty()) {
            MSTNode currentNode = queue.poll();  // Get the node with the lowest weight
            path.visited.add(currentNode.vertex);

            if (currentNode.vertex.equals(targetVertex)) {
                break;  // Stop if target vertex is reached
            }

            for (V neighbour : getNeighbours(currentNode.vertex)) {
                if (!path.visited.contains(neighbour)) {
                    MSTNode neighbourNode = minimumSpanningTree.computeIfAbsent(neighbour, MSTNode::new);

                    path.visited.add(neighbour);
                    // Weight of edge from currentNode to neighbour
                    double weight = weightMapper.apply(currentNode.vertex, neighbour);
                    // Total weight from start node to neighbour through currentNode
                    double totalWeight = currentNode.weightSumTo + weight;

                    // If total weight is smaller than previous distance update it
                    if (totalWeight < neighbourNode.weightSumTo) {
                        neighbourNode.weightSumTo = totalWeight;
                        neighbourNode.parentVertex = currentNode.vertex;
                        // Remove and add the neighbour node to update its position in the queue
                        queue.remove(neighbourNode);
                        queue.add(neighbourNode);
                    }

                    if (neighbour.equals(targetVertex)) {
                        break;  // Stop if target vertex is reached
                    }
                }
            }
        }

        if (!minimumSpanningTree.containsKey(targetVertex)) {
            return null; // If targetVertex is not reachable from startVertex
        }

        // Constructing path from end node by tracking each parent node
        V currentVertex = targetVertex;
        while (currentVertex != null) {
            path.vertices.addFirst(currentVertex);  // Add vertex at the front of the list
            currentVertex = minimumSpanningTree.get(currentVertex).parentVertex;  // Move to its parent node
        }

        path.totalWeight = minimumSpanningTree.get(targetVertex).weightSumTo; // Calculate total weight of the path
        return path;  // Return the shortest path
    }

    /**
     * represents a directed path of connected vertices in the graph
     */
    public class GPath {
        /**
         * representation invariants:
         * 1. vertices contains a sequence of vertices that are neighbours in the graph,
         * i.e. FOR ALL i: 1 < i < vertices.length: getNeighbours(vertices[i-1]).contains(vertices[i])
         * 2. a path with one vertex equal start and target vertex
         * 3. a path without vertices is empty, does not have a start nor a target
         * totalWeight is a helper attribute to capture total path length from a function on two neighbouring vertices
         * visited is a helper set to be able to track visited vertices in searches, only for analysis purposes
         **/
        private static final int DISPLAY_CUT = 10;
        private final Deque<V> vertices = new LinkedList<>();
        private final Set<V> visited = new HashSet<>();
        private double totalWeight = 0.0;

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(
                    String.format("Weight=%.2f Length=%d visited=%d (",
                            this.totalWeight, this.vertices.size(), this.visited.size()));
            String separator = "";
            int count = 0;
            final int tailCut = this.vertices.size() - 1 - DISPLAY_CUT;
            for (V v : this.vertices) {
                // limit the length of the text representation for long paths.
                if (count < DISPLAY_CUT || count > tailCut) {
                    sb.append(separator).append(v.toString());
                    separator = ", ";
                } else if (count == DISPLAY_CUT) {
                    sb.append(separator).append("...");
                }
                count++;
            }
            sb.append(")");
            return sb.toString();
        }

        /**
         * recalculates the total weight of the path from a given weightMapper that calculates the weight of
         * the path segment between two neighbouring vertices.
         *
         * @param weightMapper
         */
        public void reCalculateTotalWeight(BiFunction<V, V, Double> weightMapper) {
            this.totalWeight = 0.0;
            V previous = null;
            for (V v : this.vertices) {
                // the first vertex of the iterator has no predecessor and hence no weight contribution
                if (previous != null) this.totalWeight += weightMapper.apply(previous, v);
                previous = v;
            }
        }

        public Queue<V> getVertices() {
            return this.vertices;
        }

        public double getTotalWeight() {
            return this.totalWeight;
        }

        public Set<V> getVisited() {
            return this.visited;
        }
    }

    // helper class to build the spanning tree of visited vertices in dijkstra's shortest path algorithm
    // your may change this class or delete it altogether follow a different approach in your implementation
    private class MSTNode implements Comparable<MSTNode> {
        protected V vertex;                // the graph vertex that is concerned with this MSTNode
        protected V parentVertex = null;     // the parent's node vertex that has an edge towards this node's vertex
        protected boolean marked = false;  // indicates DSP processing has been marked complete for this vertex
        protected double weightSumTo = Double.MAX_VALUE;   // sum of weights of current shortest path towards this node's vertex

        private MSTNode(V vertex) {
            this.vertex = vertex;
        }

        // comparable interface helps to find a node with the shortest current path, sofar
        @Override
        public int compareTo(MSTNode otherMSTNode) {
            return Double.compare(weightSumTo, otherMSTNode.weightSumTo);
        }
    }
}
