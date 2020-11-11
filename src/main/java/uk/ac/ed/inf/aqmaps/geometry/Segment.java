package uk.ac.ed.inf.aqmaps.geometry;

import java.awt.geom.Line2D;

/** A line segment from one coordinate point to another, using a Line2D */
public class Segment extends Line2D.Double {
  /**
   * @param start start coordinates
   * @param end end coordinates
   */
  public Segment(Coords start, Coords end) {
    super(start, end);
  }
}
