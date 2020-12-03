package uk.ac.ed.inf.aqmaps.flightplanning;

import uk.ac.ed.inf.aqmaps.Move;

import java.util.List;

/**
 * A wrapper class for a flight plan as a list of moves, and the random seed that was used to
 * generate it. This is needed so that results can be sorted by seed before finding the minimum,
 * allowing for consistent operation even when concurrency is used.
 */
public class FlightPlan {
  private final int seed;
  private final List<Move> flightPlan;

  public FlightPlan(int seed, List<Move> flightPlan) {
    this.seed = seed;
    this.flightPlan = flightPlan;
  }

  public int getSeed() {
    return seed;
  }

  public List<Move> getFlightPlan() {
    return flightPlan;
  }

  @Override
  public String toString() {
    return Integer.toString(seed);
  }
}
