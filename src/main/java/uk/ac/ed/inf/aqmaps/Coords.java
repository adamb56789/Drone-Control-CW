package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.Point;

import java.awt.geom.Point2D;

/** Holds a longitude and latitude pair, using a Point2D. */
public class Coords extends Point2D.Double {

  /**
   * @param lng longitude
   * @param lat latitude
   */
  public Coords(double lng, double lat) {
    super(lng, lat);
  }

  /**
   * Initialise a Coords with a Mapbox Point
   *
   * @param p the point
   */
  public static Coords fromMapboxPoint(Point p) {
    return new Coords(p.longitude(), p.latitude());
  }

  @Override
  public String toString() {
    return "(" + x + ", " + y + ")";
  }
}
