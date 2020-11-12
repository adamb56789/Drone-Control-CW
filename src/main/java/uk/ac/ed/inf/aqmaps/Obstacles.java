package uk.ac.ed.inf.aqmaps;

import uk.ac.ed.inf.aqmaps.geometry.Coords;
import uk.ac.ed.inf.aqmaps.geometry.Polygon;
import uk.ac.ed.inf.aqmaps.geometry.Segment;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/** Holds information about the obstacles or no-fly zones that the drone must avoid. */
public class Obstacles {
  private final List<Segment> segments;
  private final List<Rectangle2D> boundingBoxes;

  /**
   * Holds data about the Polygons that outline the obstacles, see {@link Polygon#generateOutline()}
   */
  private final List<Polygon> outlinePolygons;

  private final List<Coords> outlinePoints;

  public Obstacles(List<Polygon> polygons) {
    segments = new ArrayList<>();
    boundingBoxes = new ArrayList<>();

    outlinePolygons = new ArrayList<>();
    outlinePoints = new ArrayList<>();

    // Derive a Polygon from each of the polygons in the mapbox, and get the points, segments and
    // bounding box from each polygon
    for (var polygon : polygons) {
      segments.addAll(polygon.getSegments());
      boundingBoxes.add(polygon.getBoundingBox());

      var outline = polygon.generateOutline();
      outlinePolygons.add(outline);
      outlinePoints.addAll(outline.getPoints());
    }
  }

  /**
   * Determines whether the line segment between the start and end points collides with a obstacle.
   *
   * @param start the coordinates of the start point
   * @param end the coordinates of the end point
   * @return true if the segment collides with an obstacle, false otherwise
   */
  public boolean collidesWith(Coords start, Coords end) {
    // If the line segment leaves the confinement area then that is a collision
    if (!ConfinementArea.isInConfinement(start) || !ConfinementArea.isInConfinement(end)) {
      return true;
    }

    // If the line segment does not enter the bounding boxes of any of the obstacles, we know
    // immediately that there are no collisions
    boolean insideNoBoxes =
        boundingBoxes.stream().noneMatch(box -> box.intersectsLine(start.x, start.y, end.x, end.y));

    if (insideNoBoxes) {
      return false;
    }

    // Now check for collisions with any of the line segments
    return segments.stream()
        .anyMatch(segment -> segment.intersectsLine(start.x, start.y, end.x, end.y));
  }

  /** @return a list of all of the points that make up the obstacle polygons */
  public List<Coords> getOutlinePoints() {
    return outlinePoints;
  }

  /** @return a list of all the line segments that make up the obstacle polygons */
  public List<Segment> getSegments() {
    return segments;
  }
}
