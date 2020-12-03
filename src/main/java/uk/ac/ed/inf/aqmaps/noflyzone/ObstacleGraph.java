package uk.ac.ed.inf.aqmaps.noflyzone;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import uk.ac.ed.inf.aqmaps.geometry.Coords;
import uk.ac.ed.inf.aqmaps.geometry.Polygon;

import java.util.List;

/** A graph of the vertices of the obstacles and edges between them if they have line of sight. */
public class ObstacleGraph extends SimpleWeightedGraph<Coords, DefaultWeightedEdge> {

  /**
   * Constructor
   *
   * @param outlinePoints the points which form the outline of the obstacle polygons
   * @param obstacles the obstacles to prepare the graph for
   */
  private ObstacleGraph(Obstacles obstacles, List<Coords> outlinePoints) {
    super(DefaultWeightedEdge.class);

    // Add all of the vertices from the outline polygons
    for (var point : outlinePoints) {
      addVertex(point);
    }

    // Create edges between all pairs of points that have line of sight
    for (int i = 0; i < outlinePoints.size(); i++) {
      for (int j = 0; j < i; j++) {
        var start = outlinePoints.get(i);
        var end = outlinePoints.get(j);
        if (!obstacles.lineCollision(start, end)) {
          var edge = addEdge(start, end);
          setEdgeWeight(edge, start.distance(end));
        }
      }
    }
  }

  /**
   * Prepare a weighted graph containing all points which form an outline around the polygons as
   * vertices, and edges connecting them if they have line of sight, which have a weight equal to
   * the distance between them. The graph uses outline polygons since if it used the original
   * polygons, their points would occupy the same location and any line emerging from the corner of
   * an obstacle would be considered to be colliding with it. See see {@link
   * Polygon#generateOutlinePoints()}.
   *
   * @param outlinePoints the points which form the outline of the obstacle polygons
   * @param obstacles the obstacles to prepare the graph for
   * @return a graph representation of the obstacles
   */
  public static ObstacleGraph prepareGraph(List<Coords> outlinePoints, Obstacles obstacles) {
    return new ObstacleGraph(obstacles, outlinePoints);
  }
}
