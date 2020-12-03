package uk.ac.ed.inf.aqmaps.noflyzone;

import uk.ac.ed.inf.aqmaps.geometry.Coords;
import uk.ac.ed.inf.aqmaps.geometry.Polygon;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** Holds information about the obstacles or no-fly zones that the drone must avoid. */
public class Obstacles {
  /** A Point representing the northwest corner of the confinement area. */
  public static final Coords TOP_LEFT = new Coords(-3.192473, 55.946233);
  /** A Point representing the southeast corner of the confinement area. */
  public static final Coords BOTTOM_RIGHT = new Coords(-3.184319, 55.942617);
  /**
   * A weighted graph containing all points which form an outline around the polygons as vertices,
   * and edges connecting them if they have line of sight, which have a weight equal to the distance
   * between them.
   */
  private final ObstacleGraph graph;

  /** A list of the Polygon representations of the obstacles */
  private final List<Polygon> polygons;

  /**
   * Constructs Obstacles out of a list of Polygons specifying their locations
   *
   * @param polygons the Polygons which make up the obstacles
   */
  public Obstacles(List<Polygon> polygons) {
    this.polygons = polygons;

    var outlinePoints = new ArrayList<Coords>();

    // Derive a Polygon from each of the polygons in the mapbox, and get the points, segments and
    // bounding box from each polygon
    for (var polygon : polygons) {
      outlinePoints.addAll(polygon.generateOutlinePoints());
    }
    this.graph = ObstacleGraph.prepareGraph(outlinePoints, this);
  }

  /**
   * Determines whether or not a point is inside the confinement area
   *
   * @param point the point to examine
   * @return true if the point is inside the confinement area, false otherwise
   */
  public boolean isInConfinement(Coords point) {
    return TOP_LEFT.x < point.x
        && point.x < BOTTOM_RIGHT.x
        && BOTTOM_RIGHT.y < point.y
        && point.y < TOP_LEFT.y;
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
    if (!isInConfinement(start) || !isInConfinement(end)) {
      return true;
    }

    // Prune to only scan polygons where the line intersects the bounding box
    var prunedPolygons =
        polygons.stream()
            .filter(p -> p.getBoundingBox().intersectsLine(start.x, start.y, end.x, end.y))
            .collect(Collectors.toList());

    // Return true if any of the segment of the polygons intersect with the line
    for (var polygon : prunedPolygons) {
      if (polygon.getSegments().stream()
          .anyMatch(segment -> segment.intersectsLine(start.x, start.y, end.x, end.y))) {
        return true;
      }
    }
    return false;
  }

  /**
   * Determine whether the given point is inside an obstacle, or outside the confinement area. This
   * is currently only used in testing to generate random starting points.
   *
   * @param coords the point
   * @return true if there is a collision, false otherwise
   */
  public boolean pointCollides(Coords coords) {
    return !isInConfinement(coords) || polygons.stream().anyMatch(p -> p.contains(coords));
  }

  /**
   * Gets an ObstaclePathfinder using these Obstacles. The ObstaclePathfinder uses a clone of the
   * obstacle graph, allowing it to be used concurrently with other ObstaclePathfinder.
   *
   * @return an ObstaclePathfinder instance with these obstacles
   */
  public ObstaclePathfinder getObstaclePathfinder() {
    return new ObstaclePathfinder((ObstacleGraph) graph.clone(), this);
  }
}
