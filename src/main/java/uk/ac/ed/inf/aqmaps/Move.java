package uk.ac.ed.inf.aqmaps;

/** A class representing a single move to be made by the drone. */
public class Move {
  private final Coords before;
  private final Coords after;
  private final int direction;
  private final W3W sensor;

  public Move(Coords before, Coords after, int direction, W3W sensor) {
    this.before = before;
    this.after = after;
    this.direction = direction;
    this.sensor = sensor;
  }

  @Override
  public String toString() {
    return "Move{"
        + "before="
        + before
        + ", after="
        + after
        + ", direction="
        + direction
        + ", sensor="
        + sensor
        + '}';
  }
}
