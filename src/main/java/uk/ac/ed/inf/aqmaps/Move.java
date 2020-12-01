package uk.ac.ed.inf.aqmaps;

import uk.ac.ed.inf.aqmaps.geometry.Coords;

/** A class representing a single move to be made by the drone. */
public class Move {
  private final Coords before;
  private final Coords after;
  private final int direction;
  private final W3W sensorW3W;

  /**
   * @param before the position of the drone before the move
   * @param after the position of the drone after the move
   * @param direction the direction of the move in degrees, from 0 to 350 anticlockwise starting
   *     from east
   * @param sensorW3W the location of the sensor visited by the drone at the end of this move, or
   *     null if no sensor is visited
   */
  public Move(Coords before, Coords after, int direction, W3W sensorW3W) {
    this.before = before;
    this.after = after;
    this.direction = direction;
    this.sensorW3W = sensorW3W;
  }

  @Override
  public String toString() {
    String sensorString;
    if (sensorW3W != null) {
      sensorString = sensorW3W.getWords();
    } else {
      sensorString = "null";
    }
    // The double coordinates use %s instead of %f since %f forces us to use a specific precision
    return String.format(
        "%s,%s,%d,%s,%s,%s", before.x, before.y, direction, after.x, after.y, sensorString);
  }

  /** @return the position of the drone before making the move */
  public Coords getBefore() {
    return before;
  }

  /** @return the position of the drone after making the move */
  public Coords getAfter() {
    return after;
  }

  /** @return the direction of move in degrees */
  public int getDirection() {
    return direction;
  }

  /** @return the W3W of the sensor that this move reaches, or null if it does not reach a sensor */
  public W3W getSensorW3W() {
    return sensorW3W;
  }
}
