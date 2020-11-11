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

  //TODO get rid of these
  public Coords getStart() {
    return start;
  }

  public Coords getEnd() {
    return end;
  }
}
