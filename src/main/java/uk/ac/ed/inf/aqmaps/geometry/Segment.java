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
