package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.*;

import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Holds information about the obstacles or no-fly zones that the drone must avoid. */
public class Obstacles {
  private final FeatureCollection mapbox;
  private final List<Coords> points;
  private final List<Segment> lineSegments;
  private final List<Rectangle2D> boundingBoxes;

  public Obstacles(FeatureCollection mapbox) {
    this.mapbox = mapbox;

    //noinspection ConstantConditions - Ignore warning about Mapbox things being null, they won't be

    points =
        mapbox.features().stream()
            .map(this::getPointsFromFeature) // this is a Stream of Lists of Points
            .flatMap(List::stream) // merge together the lists of Points from each Polygon
            .map(Coords::fromMapboxPoint) // convert Point to Coords
            .collect(Collectors.toList());

    lineSegments =
        mapbox.features().stream()
            .map(this::getSegmentsFromFeature) // this is a Stream of Lists of Segments
            .flatMap(List::stream) // <Segment> merge together the Segments from each Polygon
            .collect(Collectors.toList());

    boundingBoxes =
        mapbox.features().stream()
            .map(this::getBoundingBoxFromFeature)
            .collect(Collectors.toList());
  }

  private List<Point> getPointsFromFeature(Feature feature) {
    // The Geometry interface does not have coordinates(), so we must cast to Polygon first. This is
    // potentially dangerous, but if they are not Polygons then something must have gone very wrong
    // somewhere else already.
    Polygon p = (Polygon) feature.geometry();

    // We need to clone the list here to avoid modifying the original mapbox FeatureCollection.
    // coordinates() has @NonNull, but ignore this for same reason as above
    //noinspection ConstantConditions
    var coordinates = new ArrayList<>(p.coordinates().get(0));

    // In Mapbox Polygons the first and last points are identical, so we remove the duplicate
    coordinates.remove(0);
    return coordinates;
  }

  private List<Segment> getSegmentsFromFeature(Feature feature) {
    var segments = new ArrayList<Segment>();

    List<Coords> points =
        getPointsFromFeature(feature).stream()
            .map(Coords::fromMapboxPoint)
            .collect(Collectors.toList()); // Convert Mapbox Points from the Feature to Coords

    // Create a segment between each adjacent point.
    for (int i = 0; i < points.size(); i++) {
      Coords start = points.get(i);
      Coords end = points.get((i + 1) % points.size());
      segments.add(new Segment(start, end));
    }

    return segments;
  }

  private Rectangle2D getBoundingBoxFromFeature(Feature feature) {
    var points = getPointsFromFeature(feature);

    // Create a path out of all of the points to have access to its getBounds2D() method
    var path = new Path2D.Double();
    boolean isFirst = true;

    for (var point : points) {
      if (isFirst) {
        path.moveTo(point.longitude(), point.latitude());
        isFirst = false;
      } else {
        path.lineTo(point.longitude(), point.latitude());
      }
    }

    return path.getBounds2D();
  }

  /**
   * Determines whether the line segment between the start and end points collides with a obstacle.
   *
   * @param start the coordinates of the start point
   * @param end the coordinates of the end point
   * @return true if the segment collides with an obstacle, false otherwise
   */
  public boolean collidesWith(Coords start, Coords end) {
    return false; // TODO
  }

  public FeatureCollection getMapbox() {
    return mapbox;
  }

  public List<Coords> getPoints() {
    return points;
  }

  public List<Segment> getLineSegments() {
    return lineSegments;
  }

  /** @return a list of Rectangles which form bounding boxes around each of the obstacles */
  public List<Rectangle2D> getBoundingBoxes() {
    return boundingBoxes;
  }
}
