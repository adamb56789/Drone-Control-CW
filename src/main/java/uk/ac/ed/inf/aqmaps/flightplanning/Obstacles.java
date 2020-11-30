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
   * Holds data about the Polygons that outline the obstacles, see {@link Polygon#generateOutline()}
   */
  private final List<Coords> outlinePoints;

  public Obstacles(List<Polygon> polygons) {
    this.polygons = polygons;
    segments = new ArrayList<>();
    boundingBoxes = new ArrayList<>();

    outlinePoints = new ArrayList<>();

    // Derive a Polygon from each of the polygons in the mapbox, and get the points, segments and
    // bounding box from each polygon
    for (var polygon : polygons) {
      segments.addAll(polygon.getSegments());
      boundingBoxes.add(polygon.getBoundingBox());

      var outline = polygon.generateOutline();
      outlinePoints.addAll(outline.getPoints());
    }
    this.graph = prepareGraph();
  }

  private SimpleWeightedGraph<Coords, DefaultWeightedEdge> prepareGraph() {
    var graph = new SimpleWeightedGraph<Coords, DefaultWeightedEdge>(DefaultWeightedEdge.class);

    // Add all of the vertices from the outline polygons
    for (var point : outlinePoints) {
      graph.addVertex(point);
    }

    // Create edges between all pairs of points that have line of sight
    var vertexList = new ArrayList<>(graph.vertexSet());
    for (int i = 0; i < vertexList.size(); i++) {
      for (int j = 0; j < i; j++) {
        var start = vertexList.get(i);
        var end = vertexList.get(j);
        if (!lineCollision(start, end)) {
          DefaultWeightedEdge e = graph.addEdge(start, end);
          graph.setEdgeWeight(e, start.distance(end));
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
   * @return a list of all of the points that make up the obstacle polygons. Currently only used in
   *     tests.
   */
  public List<Coords> getOutlinePoints() {
    return outlinePoints;
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
