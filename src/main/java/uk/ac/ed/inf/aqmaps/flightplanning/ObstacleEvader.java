package uk.ac.ed.inf.aqmaps.flightplanning;

import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import uk.ac.ed.inf.aqmaps.geometry.Coords;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds a weighted graph used for evading obstacles, and computes shortest paths. The graph has
 * vertices corresponding to every point of the obstacle polygons, and edges wherever there is line
 * of sight.
 */
public class ObstacleEvader {
  private final SimpleWeightedGraph<Coords, DefaultWeightedEdge> graph;
  private final Obstacles obstacles;

  /**
   * Initialise a graph for pathfinding around obstacles by generating a graph with all edges that
   * have line of sight between the points in the Obstacles.
   *
   * @param obstacles the Obstacles
   */
  public ObstacleEvader(Obstacles obstacles) {
    this.obstacles = obstacles;
    graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

    // Add all of the vertices from the outline polygons
    for (var point : obstacles.getOutlinePoints()) {
      addVertex(point);
    }

    // Create edges between all pairs of points that have line of sight
    var vertexList = new ArrayList<>(graph.vertexSet());
    for (int i = 0; i < vertexList.size(); i++) {
      for (int j = 0; j < i; j++) {
        addEdgeIfHasLineOfSight(vertexList.get(i), vertexList.get(j));
      }
    }
  }

  private ObstacleEvader(
      SimpleWeightedGraph<Coords, DefaultWeightedEdge> graph, Obstacles obstacles) {
    this.graph = graph;
    this.obstacles = obstacles;
  }

  /**
   * Get a copy of this ObstacleEvader which includes a hard copy of the obstacle graph, to allow it
   * to be used concurrently.
   */
  public ObstacleEvader getCopy() {
    var graphCopy = new SimpleWeightedGraph<Coords, DefaultWeightedEdge>(DefaultWeightedEdge.class);
    Graphs.addGraph(graphCopy, graph);
    return new ObstacleEvader(graphCopy, obstacles);
  }

  /**
   * Find the shortest path between the start and end points, navigating around obstacles if
   * necessary.
   *
   * @param start the starting point
   * @param end the ending point
   * @return a list of points specifying the route
   */
  public List<Coords> getPath(Coords start, Coords end) {
    if (!obstacles.lineCollision(start, end)) {
      // Return a direct path if it does not collide with anything
      // This shortcut decreases the runtime of this method by about 30 times
      return List.of(start, end);
    }

    return getShortestPath(start, end).getVertexList();
  }

  /**
   * Find the length of the shortest path between the start and end points, navigating around
   * obstacles if necessary. Euclidean distance is used as the length measure.
   *
   * @param start the starting point
   * @param end the ending point
   * @return the length of the path in degrees
   */
  public double getPathLength(Coords start, Coords end) {
    if (!obstacles.lineCollision(start, end)) {
      // Return the distance between the points if the direct path does not collide with anything
      return start.distance(end);
    }

    return getShortestPath(start, end).getWeight();
  }

  private GraphPath<Coords, DefaultWeightedEdge> getShortestPath(Coords start, Coords end) {
    // Add the start and end points and all possible edges to and from them
    addVertex(start);
    addVertex(end);
    addEdgeIfHasLineOfSight(start, end);
    for (var vertex : graph.vertexSet()) {
      if (vertex != start && vertex != end) { // Avoid self loops and duplication
        addEdgeIfHasLineOfSight(start, vertex);
        addEdgeIfHasLineOfSight(end, vertex);
      }
    }

    // Run Dijkstra's Algorithm from JGraphT
    var shortestPathAlgorithm = new DijkstraShortestPath<>(graph);
    var path = shortestPathAlgorithm.getPath(start, end);

    // Remove the start and end points from the graph for later reuse
    removeVertex(start);
    removeVertex(end);
    return path;
  }

  private void addVertex(Coords vertex) {
    graph.addVertex(vertex);
  }

  private void removeVertex(Coords vertex) {
    graph.removeVertex(vertex);
  }

  private void addEdgeIfHasLineOfSight(Coords start, Coords end) {
    if (!obstacles.lineCollision(start, end)) {
      DefaultWeightedEdge e = graph.addEdge(start, end);
      graph.setEdgeWeight(e, start.distance(end));
    }
  }
}
