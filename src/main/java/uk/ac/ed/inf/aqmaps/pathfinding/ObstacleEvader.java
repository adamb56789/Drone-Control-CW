package uk.ac.ed.inf.aqmaps.pathfinding;

import org.jgrapht.GraphPath;
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

  /** We keep a separate list of vertices to make scanning through all vertices faster */
  private final List<Coords> vertices;

  /**
   * Initialise a graph for pathfinding around obstacles by generating a graph with all edges that
   * have line of sight between the points in the Obstacles.
   *
   * @param obstacles the Obstacles
   */
  public ObstacleEvader(Obstacles obstacles) {
    this.obstacles = obstacles;
    graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
    vertices = new ArrayList<>();

    // Add all of the vertices from the outline polygons
    for (var point : obstacles.getOutlinePoints()) {
      addVertex(point);
    }

    // Create edges between all pairs of points that have line of sight
    for (int i = 0; i < vertices.size(); i++) {
      for (int j = 0; j < i; j++) {
        addEdgeIfHasLineOfSight(vertices.get(i), vertices.get(j));
      }
    }
  }

  /**
   * Find the shortest path between the start and end points, navigating around obstacles if
   * necessary.
   *
   * @param start the starting point
   * @param end the ending point
   * @return a list of points specifying the route
   */
  public List<Coords> getShortestPathPoints(Coords start, Coords end) {
    // If the straight line between start and end does not collide, there is no need for pathfinding
    if (!obstacles.collidesWith(start, end)) {
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
   * @return a list of points specifying the route
   */
  public double getShortestPathLength(Coords start, Coords end) {
    // If the straight line between start and end does not collide, there is no need for pathfinding
    if (!obstacles.collidesWith(start, end)) {
      return start.distance(end);
    }

    return getShortestPath(start, end).getWeight();
  }

  private GraphPath<Coords, DefaultWeightedEdge> getShortestPath(Coords start, Coords end) {
    // Add the start and end points and all possible edges to and from them
    addVertex(start);
    addVertex(end);
    addEdgeIfHasLineOfSight(start, end);
    for (var vertex : vertices) {
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
    vertices.add(vertex);
  }

  private void removeVertex(Coords vertex) {
    graph.removeVertex(vertex);
    vertices.remove(vertex);
  }

  private void addEdgeIfHasLineOfSight(Coords start, Coords end) {
    if (!obstacles.collidesWith(start, end)) {
      DefaultWeightedEdge e = graph.addEdge(start, end);
      graph.setEdgeWeight(e, start.distance(end));
    }
  }
}
