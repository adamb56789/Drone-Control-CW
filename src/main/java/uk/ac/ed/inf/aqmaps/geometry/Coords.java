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
   * Convert a mapbox point into a Coords
   *
   * @param p the mapbox Point
   */
  public static Coords buildFromGeojsonPoint(Point p) {
    return new Coords(p.longitude(), p.latitude());
  }

  /**
   * Creates a new Coords which is the result of moving from the current location at the specified
   * angle for the specified length. Angle in degrees version.
   *
   * @param degrees the direction of the move as an angle in degrees
   * @param length the length of the move
   * @return a Coords containing the calculated point
   */
  public Coords getPositionAfterMoveDegrees(double degrees, double length) {
    return getPositionAfterMoveRadians(Math.toRadians(degrees), length);
  }

  /**
   * Creates a new Coords which is the result of moving from the current location at the specified
   * angle for the specified length. Angle in radians version.
   *
   * @param radians the direction of the move as an angle in radians
   * @param length the length of the move
   * @return a Coords containing the calculated point
   */
  public Coords getPositionAfterMoveRadians(double radians, double length) {
    return new Coords(x + length * Math.cos(radians), y + length * Math.sin(radians));
  }

  @Override
  public String toString() {
    return "[" + x + ", " + y + "]";
  }
}
