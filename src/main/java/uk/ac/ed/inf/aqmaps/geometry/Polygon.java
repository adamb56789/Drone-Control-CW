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
  public static final double OUTLINE_MARGIN = 1e-14;
  private final List<Coords> points;
  /**
   * Holds a path around the polygon and is used for checking inside-ness and generating bounding
   * boxes.
   */
  private final Path2D path;

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
   * Create a Polygon from a GeoJSON Feature
   *
   * @param feature a GeoJSON Feature containing a Polygon
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

      newPoints.add(newPoint);
    }
    return new Polygon(newPoints);
  }

  /**
   * Creates a list of the segments between adjacent points in the polygon.
   *
   * @return a list of Segments as Line2D
   */
  public List<Line2D> getSegments() {
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

  public boolean contains(Coords p) {
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
