package uk.ac.ed.inf.aqmaps;

import java.awt.geom.Point2D;

/**
 * Holds a longitude and latitude pair. Values are accessed directly instead of through a getter,
 * this is a deliberate choice for ease of use.
 */
public class Coords {
  public final double lng;
  public final double lat;

  public Coords(double lng, double lat) {
    this.lng = lng;
    this.lat = lat;
  }

  @Override
  public String toString() {
    return "(" + lng + ", " + lat + ")";
  }
}
