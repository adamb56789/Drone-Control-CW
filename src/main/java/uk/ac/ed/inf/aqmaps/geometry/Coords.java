package uk.ac.ed.inf.aqmaps.geometry;

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

  /**
   * Creates a new Coords which is the result of moving from the current location at the specified angle for the specified length.
   * @param angle the direction of the move as an angle in degrees
   * @param length the length of the move
   * @return a Coords containing the calculated point
   */
  public Coords moveInDirection(double angle, double length) {
    return new Coords(x + length * Math.cos(angle), y + length * Math.sin(angle));
  }

  @Override
  public String toString() {
    return "[" + x + ", " + y + "]";
  }

  public boolean differentTo(Coords coords) {
    return (coords.x - x) != 0.0 || (coords.y - y) != 0.0;
  }
}
