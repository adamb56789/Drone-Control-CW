package uk.ac.ed.inf.aqmaps;

import java.awt.geom.Point2D;

/**
 * Holds a longitude and latitude pair. Values are accessed directly instead of through a getter,
 * this is a deliberate choice for ease of use.
 */
public class Coords {
  public double lng;
  public double lat;

  public Coords(double lng, double lat) {
    this.lng = lng;
    this.lat = lat;
  }

  /** @return this coordinate point as a Point2D.Double object */
  public Point2D toPoint() {
    return new Point2D.Double(lng, lat);
  }

  @Override
  public String toString() {
    return "(" + lng + ", " + lat + ")";
  }
}
