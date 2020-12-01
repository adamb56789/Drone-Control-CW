package uk.ac.ed.inf.aqmaps.flightplanning;

import uk.ac.ed.inf.aqmaps.geometry.Coords;

/**
 * A class which holds data about the output values of the flight planning algorithm from a position
 * to a sensor, potentially with a next sensor. This does not hold the actual tour, and is only
 * used for the size of the tour, as it would use a lot of memory.
 */
public class FlightCacheValue {
  private final int length;
  private final Coords endPosition;

  /**
   * Constructor
   *
   * @param length the number of moves in this flight path section
   * @param endPosition the ending position of the drone in this flight path section
   */
  public FlightCacheValue(int length, Coords endPosition) {
    this.length = (byte) length;
    this.endPosition = endPosition;
  }

  /** @return the number of moves in this flight path section */
  public int getLength() {
    return length;
  }

  /** @return the ending position of the drone in this flight path section */
  public Coords getEndPosition() {
    return endPosition;
  }
}
