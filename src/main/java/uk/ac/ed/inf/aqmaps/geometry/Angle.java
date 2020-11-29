package uk.ac.ed.inf.aqmaps.geometry;

/** Contains various static methods for calculation of angles and directions. */
public class Angle {
  /**
   * Calculates the angle of the line AB with respect to the horizontal, where east is 0, north is
   * pi/2, south is -pi/2, west is pi
   *
   * @param a point A
   * @param b point B
   * @return the direction in radians
   */
  public static double lineDirection(Coords a, Coords b) {
    return Math.atan2(b.y - a.y, b.x - a.x);
  }

  /**
   * Calculate the direction of the bisecting line between lines XA and XB.
   *
   * @param x point X
   * @param a point A
   * @param b point B
   * @return the direction of the bisector in radians
   */
  public static double bisectorDirection(Coords x, Coords a, Coords b) {
    var u = lineDirection(x, a);
    var v = lineDirection(x, b);
    if (u - v < 0 && v - u > Math.PI || v - u < 0 && u - v > Math.PI) {
      return Math.PI + (u + v) / 2;
    }
    return (u + v) / 2;
  }

  /**
   * Convert an angle in radians to the angle in degrees to the nearest 10.
   *
   * @param radians an angle in radians
   * @return a rounded angle in degrees
   */
  public static int roundTo10Degrees(double radians) {
    return (int) (Math.round(Math.toDegrees(radians) / 10.0) * 10);
  }

  /**
   * @param degrees an angle in degrees
   * @return the angle but rotated to be in the range [0,360), if it was not already
   */
  public static int formatAngle(int degrees) {
    if (degrees < 0) {
      degrees += 360;
    } else if (degrees >= 360) {
      degrees -= 360;
    }
    return degrees;
  }
}
