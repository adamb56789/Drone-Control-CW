package uk.ac.ed.inf.aqmaps.geometry;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

/** A line segment from one coordinate point to another, using a Line2D */
public class Segment extends Line2D.Double {
  private final Coords start;
  private final Coords end;

  /**
   * @param start start coordinates
   * @param end end coordinates
   */
  public Segment(Coords start, Coords end) {
    super(start, end);
    this.start = start;
    this.end = end;
  }

  /**
   * Calculates the angle of the line between these points with respect to the horizontal, where
   * east is 0, north is pi/2, south is -pi/2, west is pi
   *
   * @param p1 the start point of the line
   * @param p2 the end point of the line
   * @return the angle in radians
   */
  public static double angle(Coords p1, Coords p2) {
    return Math.atan2(p2.y - p1.y, p2.x - p1.x);
  }

  public Coords getStart() {
    return start;
  }

  public Coords getEnd() {
    return end;
  }

  /**
   * Calculates the length of this line segment using Euclidean distance
   *
   * @return the length of the segment
   */
  public double length() {
    return Point2D.distance(x1, y1, x2, y2);
  }
}
