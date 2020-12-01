package uk.ac.ed.inf.aqmaps.flightplanning;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import uk.ac.ed.inf.aqmaps.geometry.Coords;

import java.util.List;

/**
 * Handles obstacle evasion. Uses Obstacles and an ObstacleGraph to find paths between points which
 * do not collides with any obstacles.
 */
public class ObstacleEvader {
  private final SimpleWeightedGraph<Coords, DefaultWeightedEdge> graph;
  private final Obstacles obstacles;

  /**
   * Construct an Obstacle evader with the given graph and obstacles.
   *
   * @param graph a SimpleWeightedGraph of the obstacles
   * @param obstacles the Obstacles
   */
  public ObstacleEvader(
      SimpleWeightedGraph<Coords, DefaultWeightedEdge> graph, Obstacles obstacles) {
    this.graph = graph;
    this.obstacles = obstacles;
  }

  /**
   * Find the shortest path between the start and end points, navigating around obstacles if
   * necessary.
   *
   * @param start the starting point
   * @param end the ending point
   * @return a list of points specifying the route
   */
  public List<Coords> getPathBetweenPoints(Coords start, Coords end) {
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

  /**
   * Computes a GraphPath containing the shortest path from the start to the end coordinates,
   * navigating around obstacles if required.
   *
   * @param start the start point
   * @param end the end point
   * @return a GraphPath
   */
  private GraphPath<Coords, DefaultWeightedEdge> getShortestPath(Coords start, Coords end) {
    // Add the start and end points and all possible edges to and from them
    graph.addVertex(start);
    graph.addVertex(end);
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
    graph.removeVertex(start);
    graph.removeVertex(end);
    return path;
  }

  /**
   * Adds an edge between points A and B if they have line of sight to each other, or in other words if
   * the line between them does not collide with an obstacle. Sets the edge weight to the distance between the points.
   *
   * @param A point A
   * @param B point B
   */
  private void addEdgeIfHasLineOfSight(Coords A, Coords B) {
    if (!obstacles.lineCollision(A, B)) {
      var edge = graph.addEdge(A, B);
      graph.setEdgeWeight(edge, A.distance(B));
    }
  }
}
