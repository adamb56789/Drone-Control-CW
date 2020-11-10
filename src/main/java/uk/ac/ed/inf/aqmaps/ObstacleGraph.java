package uk.ac.ed.inf.aqmaps;

import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds a weighted graph used for evading obstacles, and computes shortest paths. The graph has
 * vertices corresponding to every point of the obstacle polygons, and edges wherever there is line
 * of sight.
 */
public class ObstacleGraph {
  private SimpleWeightedGraph<Coords, DefaultWeightedEdge> graph;
  private Obstacles obstacles;

  /** We keep a separate list of vertices to make scanning through all vertices faster */
  private List<Coords> vertices;

  public ObstacleGraph(Obstacles obstacles) {
    this.obstacles = obstacles;
    graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
    vertices = new ArrayList<>();

    // Add all of the vertices
    for (var point : obstacles.getAllPoints()) {
      addVertex(point);
    }

    // Create edges around all of the polygons and between any points where there is line of sight
    for (List<Coords> polygonPoints : obstacles.getPolygonPoints()) {
      addPolygonEdges(polygonPoints);
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
  public List<Coords> getShortestPath(Coords start, Coords end) {
//    var shortestPath = new DijkstraShortestPath<>(graph);
//    shortestPath.getPath(start, end);
    return null;
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
    return 0; // TODO
  }

  private void addPolygonEdges(List<Coords> polygonPoints) {
    for (int i = 0; i < polygonPoints.size(); i++) {

      // Create an edge between two adjacent points, wrapping around to the front if required
      Coords currentPoint = polygonPoints.get(i);
      int nextIndex = i + 1;
      if (nextIndex == polygonPoints.size()) { // Wrap around, this is faster than using %
        nextIndex = 0;
      }
      Coords nextPoint = polygonPoints.get(nextIndex);
      addEdge(currentPoint, nextPoint);

      int prevIndex = i - 1; // Get the previous point in the polygon
      if (prevIndex == -1) {
        prevIndex = polygonPoints.size() - 1;
      }
      Coords prevPoint = polygonPoints.get(prevIndex);

      // Try to create an edge between the current point and every point in the graph
      for (var vertex : vertices) {

        // Do not make edge from point to itself, or to adjacent points in the polygon since we
        // have already done so.
        if (vertex != currentPoint && vertex != prevPoint && vertex != nextPoint) {
          // Check for line of sight
          if (!obstacles.collidesWith(vertex, currentPoint)) {
            addEdge(vertex, currentPoint);
          }
        }
      }
    }
  }

  private void addVertex(Coords coords) {
    graph.addVertex(coords);
    vertices.add(coords);
  }

  private void addEdge(Coords start, Coords end) {
    DefaultWeightedEdge e = graph.addEdge(start, end);
    graph.setEdgeWeight(e, start.distance(end));
  }
}
