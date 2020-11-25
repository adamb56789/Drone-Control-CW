package uk.ac.ed.inf.aqmaps;

import uk.ac.ed.inf.aqmaps.geometry.Coords;

/** A class representing a single move to be made by the drone. */
public class Move {
  private final Coords before;
  private final Coords after;
  private final int direction;
  private final W3W sensor;

  /**
   * @param before the position of the drone before the move
   * @param after the position of the drone after the move
   * @param direction the direction of the move in degrees, from 0 to 350 anticlockwise starting
   *     from east
   * @param sensorW3W the location of the sensor visited by the drone at the end of this move, or null
   *     if no sensor is visited
   */
  public Move(Coords before, Coords after, int direction, W3W sensorW3W) {
    this.before = before;
    this.after = after;
    this.direction = direction;
    this.sensor = sensorW3W;
  }

  @Override
  public String toString() {
    String sensorString;
    if (sensor != null) {
      sensorString = sensor.getWords();
    } else {
      sensorString = "null";
    }
    // The double coordinates use %s instead of %f since %f forces us to use a specific precision
    return String.format(
        "%s,%s,%d,%s,%s,%s", before.x, before.y, direction, after.x, after.y, sensorString);
  }

  public Coords getBefore() {
    return before;
  }

  public Coords getAfter() {
    return after;
  }
}
