package uk.ac.ed.inf.aqmaps.flightplanning;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import uk.ac.ed.inf.aqmaps.geometry.Coords;
import uk.ac.ed.inf.aqmaps.geometry.Polygon;

import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/** Holds information about the obstacles or no-fly zones that the drone must avoid. */
public class Obstacles {
  private final SimpleWeightedGraph<Coords, DefaultWeightedEdge> graph;
  private final List<Line2D> segments;
  private final List<Rectangle2D> boundingBoxes;

  private final List<Polygon> polygons;

  /**
   * Constructs Obstacles out of a list of Polygons specifying their locations
   *
   * @param polygons the Polygons which make up the obstacles
   */
  public Obstacles(List<Polygon> polygons) {
    this.polygons = polygons;
    segments = new ArrayList<>();
    boundingBoxes = new ArrayList<>();

    var outlinePoints = new ArrayList<Coords>();

    // Derive a Polygon from each of the polygons in the mapbox, and get the points, segments and
    // bounding box from each polygon
    for (var polygon : polygons) {
      segments.addAll(polygon.getSegments());
      boundingBoxes.add(polygon.getBoundingBox());

      outlinePoints.addAll(polygon.generateOutlinePoints());
    }
    this.graph = prepareGraph(outlinePoints);
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
   * @return a SimpleWeightedGraph representation of the obstacles
   */
  private SimpleWeightedGraph<Coords, DefaultWeightedEdge> prepareGraph(
      List<Coords> outlinePoints) {
    var graph = new SimpleWeightedGraph<Coords, DefaultWeightedEdge>(DefaultWeightedEdge.class);

    // Add all of the vertices from the outline polygons
    for (var point : outlinePoints) {
      graph.addVertex(point);
    }

    // Create edges between all pairs of points that have line of sight
    for (int i = 0; i < outlinePoints.size(); i++) {
      for (int j = 0; j < i; j++) {
        var start = outlinePoints.get(i);
        var end = outlinePoints.get(j);
        if (!lineCollision(start, end)) {
          var edge = graph.addEdge(start, end);
          graph.setEdgeWeight(edge, start.distance(end));
        }
      }
    }
    return graph;
  }

  /**
   * Determines whether the line segment between the start and end points collides with a obstacle.
   *
   * @param start the coordinates of the start point
   * @param end the coordinates of the end point
   * @return true if the segment collides with an obstacle, false otherwise
   */
  public boolean lineCollision(Coords start, Coords end) {
    // If the line segment leaves the confinement area then that is a collision
    if (!ConfinementArea.isInConfinement(start) || !ConfinementArea.isInConfinement(end)) {
      return true;
    }

    // If the line segment does not enter the bounding boxes of any of the obstacles, we know
    // immediately that there are no collisions. In profiling, doing this first more than halved the
    // total runtime of this method, reducing it from 54% to 34% of the the total.
    boolean insideNoBoxes =
        boundingBoxes.stream().noneMatch(box -> box.intersectsLine(start.x, start.y, end.x, end.y));

    if (insideNoBoxes) {
      return false;
    }

    // Now check for collisions with any of the line segments
    return segments.stream()
        .anyMatch(segment -> segment.intersectsLine(start.x, start.y, end.x, end.y));
  }

  /**
   * Determine whether the given point is inside an obstacle, or outside the confinement area. This
   * is currently only used in testing to generate random starting points.
   *
   * @param coords the point
   * @return true if there is a collision, false otherwise
   */
  public boolean pointCollides(Coords coords) {
    return !ConfinementArea.isInConfinement(coords)
        || polygons.stream().anyMatch(p -> p.contains(coords));
  }

  /**
   * Gets an ObstacleEvader using these Obstacles. The ObstacleEvader uses a (deep) copy of the
   * obstacle graph, allowing it to be used concurrently with other ObstacleEvaders.
   *
   * @return an ObstacleEvader instance with these obstacles
   */
  public ObstacleEvader getObstacleEvader() {
    var graphCopy = new SimpleWeightedGraph<Coords, DefaultWeightedEdge>(DefaultWeightedEdge.class);
    Graphs.addGraph(graphCopy, graph);
    return new ObstacleEvader(graphCopy, this);
  }
}
