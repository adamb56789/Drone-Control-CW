package uk.ac.ed.inf.aqmaps.geometry;

import com.mapbox.geojson.Point;

import java.awt.geom.Point2D;

/**
 * Holds a longitude and latitude pair, using a Point2D. This class uses euclidean geometry and its
 * calculations do <b>not</b> match with real life.
 */
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
   * @return an equivalent Coords
   */
  public static Coords buildFromGeojsonPoint(Point p) {
    return new Coords(p.longitude(), p.latitude());
  }

  /**
   * Let this point be P. Calculates the direction or angle of the line PA with respect to the
   * horizontal, where east is 0, north is pi/2, south is -pi/2, west is pi
   *
   * @param A point A
   * @return the direction in radians
   */
  public double directionTo(Coords A) {
    return Math.atan2(A.y - y, A.x - x);
  }

  /**
   * Let this point be P. Calculates the direction of the line PA, rounded to the nearest 10
   * degrees, offset by an amount, and expressed in the range [0,350].
   *
   * @param A point A
   * @param offset the offset
   * @return the direction in degrees
   */
  public int roundedDirection10Degrees(Coords A, int offset) {
    var direction = (int) Math.round(Math.toDegrees(directionTo(A)) / 10.0) * 10;
    direction = direction + offset;
    if (direction < 0) {
      direction += 360;
    } else if (direction >= 360) {
      direction -= 360;
    }
    return direction;
  }

  /**
   * Let this point be P. Calculate the direction of the acute bisector between the lines PA and PB.
   *
   * @param A point A
   * @param B point B
   * @return the direction of the bisector in radians
   */
  public double bisectorDirection(Coords A, Coords B) {
    var d1 = directionTo(A);
    var d2 = directionTo(B);
    if (Math.abs(d2 - d1) > Math.PI) {
      // If the difference between them is obtuse, return the opposite angle
      return Math.PI + (d1 + d2) / 2;
    }
    return (d1 + d2) / 2;
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
