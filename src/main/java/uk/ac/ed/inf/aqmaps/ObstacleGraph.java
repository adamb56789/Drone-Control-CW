package uk.ac.ed.inf.aqmaps;

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
public class ObstacleGraph {
  private final SimpleWeightedGraph<Coords, DefaultWeightedEdge> graph;
  private final Obstacles obstacles;

  /** We keep a separate list of vertices to make scanning through all vertices faster */
  private final List<Coords> vertices;

  private int polygonSegmentsAdded = 0;

  public ObstacleGraph(Obstacles obstacles) {
    this.obstacles = obstacles;
    graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
    vertices = new ArrayList<>();

    // Add all of the vertices
    for (var point : obstacles.getOutlinePoints()) {
      addVertex(point);
    }

    // Create edges around all of the polygons and between any points where there is line of sight
    for (var polygon : obstacles.getOutlinePolygons()) {
      addPolygonEdges(polygon.getPoints());
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
    System.out.println(getShortestPath(start, end).getVertexList());
    return getShortestPath(start, end).getWeight();
  }

  // TODO make this private?
  public GraphPath<Coords, DefaultWeightedEdge> getShortestPath(Coords start, Coords end) {
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
      polygonSegmentsAdded++;

      int prevIndex = i - 1; // Get the previous point in the polygon
      if (prevIndex == -1) {
        prevIndex = polygonPoints.size() - 1;
      }
      Coords prevPoint = polygonPoints.get(prevIndex);

      // Try to create an edge between the current point and every point that we have created a
      // segment from before. It is not strictly necessary to only look at the ones we have done
      // before since duplicate edges will be rejected, but it saves time.
      for (int j = 0; j < polygonSegmentsAdded; j++) {
        var vertex = vertices.get(j);
        // Do not make edge from point to itself, or to adjacent points in the polygon since we
        // have already done so.
        if (vertex != currentPoint && vertex != prevPoint && vertex != nextPoint) {
          addEdgeIfHasLineOfSight(currentPoint, vertex);
        }
      }
    }
  }

  private void addEdgeIfHasLineOfSight(Coords start, Coords end) {
    if (!obstacles.collidesWith(start, end)) {
      addEdge(start, end);
    }
  }

  private void addVertex(Coords vertex) {
    graph.addVertex(vertex);
    vertices.add(vertex);
  }

  private void removeVertex(Coords vertex) {
    graph.removeVertex(vertex);
    vertices.remove(vertex);
  }

  private void addEdge(Coords start, Coords end) {
    DefaultWeightedEdge e = graph.addEdge(start, end);
    graph.setEdgeWeight(e, start.distance(end));
  }
}
