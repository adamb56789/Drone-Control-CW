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
    return (lineDirection(x, a) + lineDirection(x, b)) / 2;
  }

  /**
   * Convert an angle in radians (that ranges from -pi to pi) to the angle in degrees to the nearest
   * 10, ranging from 0 to 350
   */
  public static int roundTo10Degrees(double angle) {
    angle = Math.toDegrees(angle);
    int degrees = (int) (Math.round(angle / 10.0) * 10);

    return formatAngle(degrees);
  }

  /**
   * @param degrees an angle in degrees
   * @return the angle but rotated to be in the range [0,360), if it was not already
   */
  public static int formatAngle(int degrees) {
    // If the angle is negative, rotate it around one revolution so that it no longer is
    if (degrees < 0) {
      degrees += 360;
    } else if (degrees >= 360) {
      degrees -= 360;
    }
    return degrees;
  }
}
