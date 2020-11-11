package uk.ac.ed.inf.aqmaps.geometry;

import com.mapbox.geojson.Feature;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** Holds a polygon as a list of Coords */
public class Polygon {
  private List<Coords> points;

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
  }

  /**
   * Initialize a Polygon from a list of Coords points
   *
   * @param points a list of Coords
   */
  public Polygon(List<Coords> points) {
    this.points = points;
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
    return null;
  }

  public List<Coords> getPoints() {
    return points;
  }
}
