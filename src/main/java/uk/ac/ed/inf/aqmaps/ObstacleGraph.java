package uk.ac.ed.inf.aqmaps;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.List;

/**
 * Holds a weighted graph used for evading obstacles, and computes shortest paths. The graph has
 * vertices corresponding to every point of the obstacle polygons, and edges wherever there is line
 * of sight.
 */
public class ObstacleGraph {
  private SimpleWeightedGraph<Coords, DefaultWeightedEdge> graph;

  public ObstacleGraph(Obstacles obstacles) {} // TODO

  /**
   * Find the shortest path between the start and end points, navigating around obstacles if
   * necessary.
   *
   * @param start the starting point
   * @param end the ending point
   * @return a list of points specifying the route
   */
  public List<Coords> getShortestPath(Coords start, Coords end) {
    return null; // TODO
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
}
