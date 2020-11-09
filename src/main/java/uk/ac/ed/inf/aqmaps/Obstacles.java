package uk.ac.ed.inf.aqmaps;

import com.mapbox.geojson.FeatureCollection;

import java.util.ArrayList;
import java.util.List;

/** Holds information about the obstacles or no-fly zones that the drone must avoid. */
public class Obstacles {
  private final FeatureCollection mapbox;
  private final List<Coords> points;
  private final List<Segment> lineSegments;

  public Obstacles(FeatureCollection mapbox) {
    this.mapbox = mapbox;
    this.points = new ArrayList<>();
    this.lineSegments = new ArrayList<>();
  }

  /**
   * Determines whether the line segment between the start and end points collides with a obstacle.
   *
   * @param start the coordinates of the start point
   * @param end the coordinates of the end point
   * @return true if the segment collides with an obstacle, false otherwise
   */
  public boolean collidesWith(Coords start, Coords end) {
    return false; // TODO
  }

  public List<Coords> getPoints() {
    return points;
  }

  public List<Segment> getLineSegments() {
    return lineSegments;
  }
}
