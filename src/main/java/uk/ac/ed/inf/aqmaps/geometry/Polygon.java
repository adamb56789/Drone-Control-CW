package uk.ac.ed.inf.aqmaps.geometry;

import com.mapbox.geojson.Feature;

import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/** Holds a polygon as a list of the Coords that make up the vertices, in order */
public class Polygon {
  /**
   * The margin to use when generating a polygon which outlines another, see {@link
   * #generateOutlinePoints}
   */
  public static final double OUTLINE_MARGIN = 1e-14;

  /** A list of the vertices of the polygon */
  private final List<Coords> points;

  /**
   * Holds a path around the polygon and is used for checking inside-ness and generating bounding
   * boxes.
   */
  private final Path2D path;

  /** All of the line segments that make up the edges */
  private final List<Line2D> segments;

  /** A rectangular bounding box which contains the polygon */
  private final Rectangle2D boundingBox;

  /**
   * Initialize a Polygon from a list of Coords points
   *
   * @param points a list of Coords
   */
  private Polygon(List<Coords> points) {
    this.points = points;
    this.path = generatePath2D();
    this.segments = createSegments();
    this.boundingBox = createBoundingBox();
  }

  /**
   * Create a Polygon from a GeoJSON Polygon
   *
   * @param feature a GeoJSON Feature containing a Polygon
   * @return the converted Polygon
   */
  public static Polygon buildFromFeature(Feature feature) {
    // The Geometry interface does not have coordinates(), so we must cast to Polygon first. If it
    // isn't a polygon then something must have gone wrong
    if (!Objects.requireNonNull(feature.geometry()).type().equals("Polygon")) {
      System.out.println("Fatal error: no-fly zone GeoJSON feature was not a Polygon");
      System.exit(1);
    }
    var mapboxPolygon = (com.mapbox.geojson.Polygon) feature.geometry();

    // Polygon features are a lists of lists to handle polygons with holes, but the program doesn't
    // work with holes
    if (mapboxPolygon.coordinates().size() != 1) {
      System.out.println("Fatal error: no-fly zone polygon must not contain any holes");
      System.exit(1);
    }

    // Convert the GeoJSON points to Coords
    var coordsList =
        mapboxPolygon.coordinates().get(0).stream()
            .map(Coords::buildFromGeojsonPoint)
            .collect(Collectors.toList());

    // In GeoJSON Polygons the first and last points are identical, so we remove the duplicate
    coordsList.remove(0);
    return new Polygon(coordsList);
  }

  /**
   * Generates the points of a new polygon which contains the original by a very small margin. It
   * generates points a distance of 1.0e-14 from each point in the original Polygon in the direction
   * of the bisecting angle between the two adjacent sides, or the opposite direction if that point
   * is inside the polygon. The resulting polygon will be larger than the original by a margin of
   * 1.0e-14 on all sides.
   *
   * @return the outlining Polygon
   */
  public List<Coords> generateOutlinePoints() {
    var outlinePoints = new ArrayList<Coords>();
    for (int i = 0; i < points.size(); i++) {
      // Get the current point and the 2 points on either side
      Coords currentPoint = points.get(i);
      int nextIndex = i + 1;
      // Wrap around to the first point if necessary, this is faster than using %
      if (nextIndex == points.size()) {
        nextIndex = 0;
      }
      Coords nextPoint = points.get(nextIndex);

      int prevIndex = i - 1; // Get the previous point in the polygon, wrap around to end if needed
      if (prevIndex == -1) {
        prevIndex = points.size() - 1;
      }
      Coords prevPoint = points.get(prevIndex);

      // Calculate the bisecting angle between the current point and its adjacent points
      var bisector = currentPoint.bisectorDirection(prevPoint, nextPoint);

      // Create a new point a small distance away in the direction of the bisector
      var newPoint = currentPoint.getPositionAfterMoveRadians(bisector, OUTLINE_MARGIN);

      // If the new point is inside the polygon then put it in the opposite direction
      if (this.contains(newPoint)) {
        newPoint = currentPoint.getPositionAfterMoveRadians(bisector + Math.PI, OUTLINE_MARGIN);
      }

      outlinePoints.add(newPoint);
    }
    return outlinePoints;
  }

  /**
   * Determines whether the line segment between the start and end points collides with the polygon.
   *
   * @param start the coordinates of the start point
   * @param end the coordinates of the end point
   * @return true if the segment collides with an obstacle, false otherwise
   */
  public boolean lineCollision(Coords start, Coords end) {
    // If it doesn't intersect the bounding box we do not need to check further
    if (!boundingBox.intersectsLine(start.x, start.y, end.x, end.y)) {
      return false;
    }
    // Check all of the line segments for intersection
    return segments.stream().anyMatch(s -> s.intersectsLine(start.x, start.y, end.x, end.y));
  }

  /**
   * Checks whether a given point is containing within this polygon.
   *
   * @param p the point
   * @return true if the polygon contains the point, false otherwise
   */
  public boolean contains(Coords p) {
    return path.contains(p);
  }

  /** @return the points which make up the vertices of this polygon */
  public List<Coords> getPoints() {
    return points;
  }

  /**
   * This method is currently only used in a test, but it is kept to test whether the segments have
   * been created properly.
   *
   * @return the segments which make up the edges of the polygon
   */
  public List<Line2D> getSegments() {
    return segments;
  }

  /**
   * Creates a list of the segments between adjacent points in the polygon.
   *
   * @return a list of Segments as Line2D
   */
  private List<Line2D> createSegments() {
    var segments = new ArrayList<Line2D>();

    // Create a segment between each adjacent point.
    for (int i = 0; i < points.size(); i++) {
      var start = points.get(i);
      var end = points.get((i + 1) % points.size());
      segments.add(new Line2D.Double(start, end));
    }
    return segments;
  }

  /**
   * Creates a Path2D of the points in this polygon, so we can use its getBounds2D() and contains()
   * methods.
   *
   * @return a Path2D containing the vertices of the polygon
   */
  private Path2D generatePath2D() {
    var path = new Path2D.Double();
    // The first point needs to be added with moveTo(), and subsequent ones with lineTo()
    boolean first = true;
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

  /**
   * Creates a bounding box that contains the rectangular bounds of the polygon.
   *
   * @return the bounding box as a Rectangle2D
   */
  private Rectangle2D createBoundingBox() {
    return path.getBounds2D();
  }
}
