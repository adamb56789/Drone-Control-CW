package uk.ac.ed.inf.aqmaps.geometry;

import com.mapbox.geojson.Feature;

import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** Holds a polygon as a list of Coords */
public class Polygon {
  public static final double OUTLINE_MARGIN = 1e-14;
  private final List<Coords> points;
  /**
   * Holds a path around the polygon and is used for checking inside-ness and generating bounding
   * boxes.
   */
  private final Path2D path;

  /**
   * Initialize a Polygon from a mapbox Feature
   *
   * @param feature a mapbox Feature containing a Polygon
   */
  public Polygon(Feature feature) {
    // The Geometry interface does not have coordinates(), so we must cast to Polygon first. This is
    // potentially dangerous, but if they are not Polygons then something must have gone very wrong
    // somewhere else already.
    var p = (com.mapbox.geojson.Polygon) feature.geometry();

    // We need to clone the list here to avoid modifying the original mapbox FeatureCollection.
    // coordinates() has @NonNull, but ignore this for same reason as above
    //noinspection ConstantConditions
    var coordinates = new ArrayList<>(p.coordinates().get(0));

    // In Mapbox Polygons the first and last points are identical, so we remove the duplicate
    coordinates.remove(0);

    // Convert the Mapbox points to Coords
    this.points = coordinates.stream().map(Coords::fromMapboxPoint).collect(Collectors.toList());
    this.path = generatePath(); // Prepare the path for later use
  }

  /**
   * Initialize a Polygon from a list of Coords points
   *
   * @param points a list of Coords
   */
  private Polygon(List<Coords> points) {
    this.points = points;
    this.path = generatePath();
  }

  /**
   * Generates a new Polygon which contains the original by a very tiny margin. It generates points
   * a distance of 1.0e-14 from each point in the original Polygon in the direction of the bisecting
   * angle between the two adjacent sides, or the opposite direction if that point is inside the
   * polygon. The resulting polygon will be larger than the original by a margin of 1.0e-14 on all
   * sides.
   *
   * @return the outlining Polygon
   */
  public Polygon generateOutline() {
    var newPoints = new ArrayList<Coords>();
    for (int i = 0; i < points.size(); i++) {
      // Get the current point and the 2 points on either side
      Coords currentPoint = points.get(i);
      int nextIndex = i + 1;
      if (nextIndex == points.size()) { // Wrap around, this is faster than using %
        nextIndex = 0;
      }
      Coords nextPoint = points.get(nextIndex);

      int prevIndex = i - 1; // Get the previous point in the polygon
      if (prevIndex == -1) {
        prevIndex = points.size() - 1;
      }
      Coords prevPoint = points.get(prevIndex);

      // Calculate the bisecting angle between the current point and its adjacent points
      double angle1 = currentPoint.angleTo(prevPoint);
      double angle2 = currentPoint.angleTo(nextPoint);
      double bisectAngle = (angle1 + angle2) / 2;

      // Create a new point a small distance away in the direction of the bisector
      var newPoint = currentPoint.getPositionAfterMove(bisectAngle, OUTLINE_MARGIN);

      // If the new point is inside the polygon then put it in the opposite direction
      if (contains(newPoint)) {
        newPoint = currentPoint.getPositionAfterMove(bisectAngle + Math.PI, OUTLINE_MARGIN);
      }

      newPoints.add(newPoint);
    }
    return new Polygon(newPoints);
  }

  /**
   * Creates a list of the segments between adjacent points in the polygon.
   *
   * @return a list of Segments
   */
  public List<Segment> getSegments() {
    var segments = new ArrayList<Segment>();

    // Create a segment between each adjacent point.
    for (int i = 0; i < points.size(); i++) {
      Coords start = points.get(i);
      Coords end = points.get((i + 1) % points.size());
      segments.add(new Segment(start, end));
    }
    return segments;
  }

  /**
   * Creates a bounding box that contains the rectangular bounds of the polygon.
   *
   * @return the bounding box as a Rectangle2D
   */
  public Rectangle2D getBoundingBox() {
    return path.getBounds2D();
  }

  public List<Coords> getPoints() {
    return points;
  }

  private boolean contains(Coords p) {
    return path.contains(p);
  }

  private Path2D generatePath() {
    var path = new Path2D.Double();
    boolean first = true; // The first point needs to be added with moveTo()

    for (var point : points) {
      if (first) {
        path.moveTo(point.x, point.y);
        first = false;
      } else {
        path.lineTo(point.x, point.y);
      }
    }
    path.closePath();
    return path;
  }
}
