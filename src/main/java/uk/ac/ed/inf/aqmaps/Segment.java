package uk.ac.ed.inf.aqmaps;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

/** A line segment from one coordinate point to another, using a Line2D */
public class Segment extends Line2D.Double {

  /**
   * @param start start coordinates
   * @param end end coordinates
   */
  public Segment(Coords start, Coords end) {
    super(start, end);
  }

  /**
   * Calculates the length of this line segment using Euclidean distance
   *
   * @return the length of the segment
   */
  public double length() {
    // This could also be done with the following, but it would be slower since it creates new
    // Point2D objects: this.getP1().distance(this.getP1())
    return Point2D.distance(x1, y1, x2, y2);
  }
}
